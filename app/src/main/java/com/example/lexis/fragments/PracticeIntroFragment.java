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

import com.example.lexis.R;
import com.example.lexis.databinding.FragmentPracticeIntroBinding;
import com.example.lexis.utilities.Utils;

import org.jetbrains.annotations.NotNull;

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

        binding.btnFlashcards.setOnClickListener(v -> {
            // TODO: insert arguments here
            Fragment flashcardFragment = PracticeFlashcardFragment.newInstance(Utils.getCurrentTargetLanguage(), true);
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