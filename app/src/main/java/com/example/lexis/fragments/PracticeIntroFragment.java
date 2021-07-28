package com.example.lexis.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.example.lexis.R;
import com.example.lexis.databinding.FragmentPracticeIntroBinding;
import com.example.lexis.utilities.Utils;

import java.util.List;

public class PracticeIntroFragment extends Fragment {

    FragmentPracticeIntroBinding binding;

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
        String targetLanguage = Utils.getSpinnerText(Utils.getCurrentTargetLanguage());
        List<String> formattedLanguages = Utils.getSpinnerList(Utils.getCurrentStudiedLanguages());
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(
                getActivity(), android.R.layout.simple_spinner_dropdown_item, formattedLanguages);

        binding.spinnerLanguage.setAdapter(spinnerArrayAdapter);
        binding.spinnerLanguage.setSelection(formattedLanguages.indexOf(targetLanguage));

        // answer in target language by default
        binding.radioTarget.setText(targetLanguage);
        binding.radioTarget.setChecked(true);

        binding.spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                binding.radioTarget.setText(formattedLanguages.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
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

        Fragment flashcardFragment = PracticeFlashcardFragment.newInstance(selectedLanguage, answerInEnglish, starredWordsOnly);
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
}