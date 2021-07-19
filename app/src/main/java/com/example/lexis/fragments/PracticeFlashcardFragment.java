package com.example.lexis.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.lexis.R;
import com.example.lexis.databinding.FragmentPracticeFlashcardBinding;

public class PracticeFlashcardFragment extends Fragment {

    FragmentPracticeFlashcardBinding binding;

    public PracticeFlashcardFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPracticeFlashcardBinding.inflate(inflater);
        return binding.getRoot();
    }
}