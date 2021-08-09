package com.example.lexis.fragments;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.example.lexis.R;
import com.example.lexis.databinding.FragmentPracticeIntroBinding;
import com.example.lexis.models.Word;
import com.example.lexis.utilities.Utils;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

public class PracticeIntroFragment extends Fragment {

    private static final String TAG = "PracticeIntroFragment";
    private static final int NUM_TO_PRACTICE = 15;

    FragmentPracticeIntroBinding binding;
    int maxNumQuestions;
    int maxNumQuestionsStarred;

    public PracticeIntroFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPracticeIntroBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpToolbar();
        setUpPracticeOptions();
        binding.btnStart.setOnClickListener(v -> startPractice());
    }

    /*
    Set up spinner and radio button components for selecting practice options.
    */
    private void setUpPracticeOptions() {
        String targetLanguageCode = Utils.getCurrentTargetLanguage();
        String targetLanguage = Utils.getSpinnerText(targetLanguageCode);
        List<String> formattedLanguages = Utils.getSpinnerList(Utils.getCurrentStudiedLanguages());
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(
                getActivity(), android.R.layout.simple_spinner_dropdown_item, formattedLanguages);

        binding.spinnerLanguage.setAdapter(spinnerArrayAdapter);
        binding.spinnerLanguage.setSelection(formattedLanguages.indexOf(targetLanguage));

        // set up starred checkbox
        binding.cbStarredOnly.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String numWords;
            if (isChecked) {
                numWords = String.format("of %d words", maxNumQuestionsStarred);
                adjustQuestionLimitToMax(maxNumQuestionsStarred);
            } else {
                numWords = String.format("of %d words", maxNumQuestions);
                adjustQuestionLimitToMax(maxNumQuestions);
            }
            binding.tvQuestionLimit.setText(numWords);
        });

        // answer in target language by default
        binding.radioTarget.setText(targetLanguage);
        binding.radioTarget.setChecked(true);
        setQuestionLimit(NUM_TO_PRACTICE);
        setNumWordsSeen(targetLanguageCode);

        binding.spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String spinnerText = formattedLanguages.get(position);
                binding.radioTarget.setText(spinnerText);
                setNumWordsSeen(Utils.getLanguageCode(spinnerText));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        binding.etQuestionLimit.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String s = binding.etQuestionLimit.getText().toString();
                if (Integer.parseInt(s) > maxNumQuestions) {
                    binding.etQuestionLimit.setText(String.valueOf(maxNumQuestions));
                }
            }
        });

        // set up buttons to select flashcard / word search
        binding.btnFlashcards.setSelected(true); // flashcards is selected by default
        binding.btnFlashcards.setStrokeColorResource(R.color.orange_peel);
        binding.btnFlashcards.setStrokeWidth(6);
        binding.btnWordSearch.setStrokeWidth(2);

        binding.btnFlashcards.setOnClickListener(v -> {
            if (!binding.btnFlashcards.isSelected()) {
                togglePracticeSelection();
            }
        });
        binding.btnWordSearch.setOnClickListener(v -> {
            if (!binding.btnWordSearch.isSelected()) {
                togglePracticeSelection();
            }
        });
    }

    /*
    Toggle between selecting flashcards and word search.
    */
    private void togglePracticeSelection() {
        binding.btnFlashcards.setSelected(!binding.btnFlashcards.isSelected());
        binding.btnWordSearch.setSelected(!binding.btnWordSearch.isSelected());

        if (binding.btnFlashcards.isSelected()) {
            binding.btnFlashcards.setStrokeColorResource(R.color.orange_peel);
            binding.btnFlashcards.setStrokeWidth(6);
            binding.btnWordSearch.setStrokeColorResource(R.color.black);
            binding.btnWordSearch.setStrokeWidth(2);
            binding.flashcardOptions.setVisibility(View.VISIBLE);
        } else {
            binding.btnWordSearch.setStrokeColorResource(R.color.orange_peel);
            binding.btnWordSearch.setStrokeWidth(6);
            binding.btnFlashcards.setStrokeColorResource(R.color.black);
            binding.btnFlashcards.setStrokeWidth(2);
            binding.flashcardOptions.setVisibility(View.GONE);
        }
    }

    /*
    Set up toolbar with a custom back button.
    */
    private void setUpToolbar() {
        AppCompatActivity activity = ((AppCompatActivity) getActivity());
        if (activity != null) {
            activity.setSupportActionBar(binding.toolbar.getRoot());
            ActionBar actionBar = activity.getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle("");
            }

            binding.toolbar.getRoot().setNavigationIcon(R.drawable.back_arrow);
            Drawable navigationIcon = binding.toolbar.getRoot().getNavigationIcon();
            if (navigationIcon != null) {
                navigationIcon.setTint(getResources().getColor(R.color.black));
            }
            binding.toolbar.getRoot().setNavigationOnClickListener(v -> activity.onBackPressed());
        }
    }

    /*
    Start a new practice session with either flashcards or word search.
    */
    private void startPractice() {
        if (binding.btnFlashcards.isSelected()) {
            launchFlashcardSession();
        } else {
            launchWordSearchSession();
        }
    }

    /*
    Launch a new flashcard study session with the options selected in the interface.
    */
    private void launchFlashcardSession() {
        int selectedPosition = binding.spinnerLanguage.getSelectedItemPosition();
        List<String> allLanguages = Utils.getCurrentStudiedLanguages();
        String selectedLanguage = allLanguages.get(selectedPosition);
        boolean answerInEnglish = binding.radioEnglish.isChecked();
        boolean starredWordsOnly = binding.cbStarredOnly.isChecked();

        String flashcardsInput = binding.etQuestionLimit.getText().toString();
        if (flashcardsInput.isEmpty()) {
            Toast.makeText(getActivity(), "Please enter how many words you'd like to practice!", Toast.LENGTH_SHORT).show();
            return;
        }
        int numFlashcards = Integer.parseInt(flashcardsInput);

        Fragment flashcardFragment = PracticeFlashcardFragment.newInstance(
                selectedLanguage, answerInEnglish, starredWordsOnly, numFlashcards);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, flashcardFragment, "FlashcardFragment")
                    .commit();
        }
    }

    /*
    Launch a new word search session with the options selected in the interface.
    */
    private void launchWordSearchSession() {
        int selectedPosition = binding.spinnerLanguage.getSelectedItemPosition();
        List<String> allLanguages = Utils.getCurrentStudiedLanguages();
        String selectedLanguage = allLanguages.get(selectedPosition);

        Fragment wordSearchFragment = WordSearchFragment.newInstance(selectedLanguage);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, wordSearchFragment, "WordSearchFragment")
                    .commit();
        }
    }

    /*
    Set the contents of the text views with information about the number of words the user studied.
    */
    private void setNumWordsSeen(String targetLanguage) {
        ParseQuery<Word> query = ParseQuery.getQuery(Word.class);
        query.include(Word.KEY_USER);
        query.whereEqualTo(Word.KEY_USER, ParseUser.getCurrentUser());
        query.whereEqualTo(Word.KEY_TARGET_LANGUAGE, targetLanguage);
        query.findInBackground((words, e) -> {
            if (e != null) {
                Log.e(TAG, "Issue with getting vocabulary size", e);
                return;
            }

            int numStarred = 0;
            for (Word word : words) {
                if (word.getIsStarred()) {
                    numStarred++;
                }
            }

            maxNumQuestions = words.size();
            maxNumQuestionsStarred = numStarred;
            String numWords = String.format("of %d words", maxNumQuestions);
            binding.tvQuestionLimit.setText(numWords);

            adjustQuestionLimitToMax(maxNumQuestions);
        });
    }

    /*
    Check if the number of questions to study is larger than the maximum and adjust if needed.
    */
    private void adjustQuestionLimitToMax(int max) {
        int questionLimit = Integer.parseInt(binding.etQuestionLimit.getText().toString());
        if (questionLimit > max) {
            questionLimit = max;
            setQuestionLimit(questionLimit);
        }
    }

    private void setQuestionLimit(int limit) {
        binding.etQuestionLimit.setText(String.valueOf(limit));
    }
}