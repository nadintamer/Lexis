package com.example.lexis.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

    public PracticeIntroFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPracticeIntroBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String targetLanguage = Utils.getSpinnerText(Utils.getCurrentTargetLanguage());
        List<String> allLanguages = Utils.getCurrentStudiedLanguages();
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

        binding.btnFlashcards.setOnClickListener(v -> {
            int selectedPosition = binding.spinnerLanguage.getSelectedItemPosition();
            String selectedLanguage = allLanguages.get(selectedPosition);
            boolean answerInEnglish = binding.radioEnglish.isChecked();

            Fragment flashcardFragment = PracticeFlashcardFragment.newInstance(selectedLanguage, answerInEnglish);
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer,  flashcardFragment)
                    .commit();
        });

        // set up toolbar with custom back button
        AppCompatActivity activity = ((AppCompatActivity) getActivity());
        if (activity != null) {
            activity.setSupportActionBar(binding.toolbar.getRoot());
            activity.getSupportActionBar().setTitle("");
            binding.toolbar.getRoot().setNavigationIcon(R.drawable.back_arrow);
            binding.toolbar.getRoot().getNavigationIcon().setTint(getResources().getColor(R.color.black));
            binding.toolbar.getRoot().setNavigationOnClickListener(v -> activity.onBackPressed());
        }
    }
}