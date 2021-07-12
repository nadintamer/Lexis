package com.example.lexis.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.lexis.databinding.FragmentWordBinding;

public class WordDialogFragment extends DialogFragment {

    FragmentWordBinding binding;

    public WordDialogFragment() {
    }

    public static WordDialogFragment newInstance(String target, String english) {
        WordDialogFragment frag = new WordDialogFragment();
        Bundle args = new Bundle();
        args.putString("targetLanguage", target);
        args.putString("english", english);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentWordBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String target = getArguments().getString("targetLanguage", "");
        String english = getArguments().getString("english", "");
        binding.tvTargetLanguage.setText(target);
        binding.tvEnglish.setText(english);
    }
}
