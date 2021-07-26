package com.example.lexis.fragments;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.lexis.databinding.FragmentVocabularyNewBinding;
import com.example.lexis.utilities.Const;
import com.example.lexis.utilities.TranslateUtils;

import javax.annotation.Nullable;

public class VocabularyNewDialogFragment extends DialogFragment {

    FragmentVocabularyNewBinding binding;

    public VocabularyNewDialogFragment() {}

    /*
    Interface for listener used to pass data back to parent fragment.
    */
    public interface VocabularyNewDialogListener {
        void onFinishDialog(String targetLanguage, String targetWord, String englishWord);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentVocabularyNewBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpButtons();
        setUpSpinner();
        setUpEditText();
    }

    /*
    Set up onClickListeners from cancel and update buttons.
    */
    private void setUpButtons() {
        binding.btnCancel.setOnClickListener(v -> dismiss());
        binding.btnUpdate.setOnClickListener(v -> sendBackNewWord());
    }

    /*
    Set up spinner for selecting target language.
    */
    private void setUpSpinner() {
        binding.spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                binding.etLayoutTarget.setHint(String.format("%s translation", Const.languageNames.get(position)));
                String targetLanguage = Const.languageCodes.get(position);

                String englishWord = binding.etEnglishWord.getText().toString();
                String targetWord = binding.etTargetWord.getText().toString();
                if (!englishWord.isEmpty() && !targetWord.isEmpty()) {
                    String newTarget = TranslateUtils.translateSingleWord(englishWord, targetLanguage);
                    binding.etTargetWord.setText(newTarget);
                    binding.etTargetWord.setSelection(newTarget.length());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setUpEditText() {
        String currentLanguage = Const.languageNames.get(binding.spinnerLanguage.getSelectedItemPosition());
        binding.etLayoutTarget.setHint(String.format("%s translation", currentLanguage));

        binding.etTargetWord.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                int selectedItemPosition = binding.spinnerLanguage.getSelectedItemPosition();
                String targetLanguage = Const.languageCodes.get(selectedItemPosition);

                String englishWord = binding.etEnglishWord.getText().toString();
                if (!englishWord.isEmpty()) {
                    String targetWord = TranslateUtils.translateSingleWord(englishWord, targetLanguage);
                    binding.etTargetWord.setText(targetWord);
                    binding.etTargetWord.setSelection(targetWord.length());
                }
            }
        });
    }

    /*
    Send data about the new word back to the parent fragment.
    */
    private void sendBackNewWord() {
        int selectedItemPosition = binding.spinnerLanguage.getSelectedItemPosition();
        String targetLanguage = Const.languageCodes.get(selectedItemPosition);
        String targetWord = binding.etTargetWord.getText().toString();
        String englishWord = binding.etEnglishWord.getText().toString();

        VocabularyNewDialogListener listener = (VocabularyNewDialogListener) getTargetFragment();
        if (listener != null) {
            listener.onFinishDialog(targetLanguage, targetWord, englishWord);
            dismiss();
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
