package com.example.lexis.fragments;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.lexis.databinding.FragmentWordSearchHelpBinding;
import com.example.lexis.utilities.Utils;

import java.util.ArrayList;

import javax.annotation.Nullable;

public class WordSearchHelpDialogFragment extends DialogFragment  {
    FragmentWordSearchHelpBinding binding;

    public WordSearchHelpDialogFragment() {}

    public static WordSearchHelpDialogFragment newInstance(String targetLanguage) {
        WordSearchHelpDialogFragment frag = new WordSearchHelpDialogFragment();
        Bundle args = new Bundle();
        args.putString("targetLanguage", targetLanguage);
        frag.setArguments(args);
        return frag;
    }

    /*
    Interface for listener used to pass data back to parent fragment.
    */
    public interface WordSearchHelpDialogListener {
        void onFinishDialog();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentWordSearchHelpBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // set up OK button
        binding.btnDone.setOnClickListener(v -> {
            WordSearchHelpDialogListener listener = (WordSearchHelpDialogListener) getTargetFragment();
            if (listener != null) {
                listener.onFinishDialog();
                dismiss();
            }
        });

        // set up text views
        if (getArguments() != null) {
            String targetLanguage = getArguments().getString("targetLanguage");
            String hint = String.format("Look for the %s words in the word search grid. Tap on an English word to see its translation!", Utils.getSpinnerText(targetLanguage));
            binding.tvHint.setText(hint);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            // set a transparent background for the dialog so that rounded corners are visible
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }
}
