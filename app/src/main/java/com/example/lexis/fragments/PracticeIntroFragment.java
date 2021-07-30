package com.example.lexis.fragments;

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
import android.widget.Toast;

import com.example.lexis.R;
import com.example.lexis.databinding.FragmentPracticeIntroBinding;
import com.example.lexis.models.Word;
import com.example.lexis.utilities.Const;
import com.example.lexis.utilities.Utils;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

public class PracticeIntroFragment extends Fragment {

    private static final String TAG = "PracticeIntroFragment";
    private static final int NUM_TO_PRACTICE = 15;

    FragmentPracticeIntroBinding binding;
    int maxNumQuestions;

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
        binding.btnFlashcards.setOnClickListener(v -> launchFlashcardSession());
        // TODO: temporary - remove!
        binding.btnWordSearch.setOnClickListener(v -> launchWordSearchSession());
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

        // answer in target language by default
        binding.radioTarget.setText(targetLanguage);
        binding.radioTarget.setChecked(true);
        setNumWordsSeen(targetLanguageCode);
        setQuestionLimit(NUM_TO_PRACTICE);

        binding.spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                binding.radioTarget.setText(formattedLanguages.get(position));
                setNumWordsSeen(Const.languageCodes.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        binding.etQuestionLimit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String s = binding.etQuestionLimit.getText().toString();
                    if (Integer.parseInt(s) > maxNumQuestions) {
                        binding.etQuestionLimit.setText(String.valueOf(maxNumQuestions));
                    }
                }
            }
        });
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
                    .replace(R.id.fragmentContainer, flashcardFragment)
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

        Fragment flashcardFragment = WordSearchFragment.newInstance(selectedLanguage);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, flashcardFragment)
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

            maxNumQuestions = words.size();
            String numWords = String.format("of %d words", maxNumQuestions);
            binding.tvQuestionLimit.setText(numWords);

            int questionLimit = Integer.parseInt(binding.etQuestionLimit.getText().toString());
            if (questionLimit > maxNumQuestions) {
                questionLimit = maxNumQuestions;
                setQuestionLimit(questionLimit);
            }
        });
    }

    private void setQuestionLimit(int limit) {
        binding.etQuestionLimit.setText(String.valueOf(limit));
    }
}