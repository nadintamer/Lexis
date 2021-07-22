package com.example.lexis.fragments;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.lexis.databinding.FragmentVocabularyFilterBinding;
import com.example.lexis.utilities.Utils;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class VocabularyFilterDialogFragment extends DialogFragment {

    private FragmentVocabularyFilterBinding binding;
    private List<CheckBox> checkboxes;
    private ArrayList<String> selectedLanguages;
    private boolean starredOnly;
    private Sort sortBy;

    public enum Sort {
        ALPHABETICALLY, DATE
    }

    public VocabularyFilterDialogFragment() {}

    public static VocabularyFilterDialogFragment newInstance(ArrayList<String> languages, ArrayList<String> selected, boolean starredOnly, Sort sortBy) {
        VocabularyFilterDialogFragment frag = new VocabularyFilterDialogFragment();
        Bundle args = new Bundle();
        args.putStringArrayList("languageOptions", languages);
        args.putStringArrayList("selected", selected);
        args.putBoolean("starredOnly", starredOnly);
        args.putString("sortBy", sortBy.name());
        frag.setArguments(args);
        return frag;
    }

    /*
    Interface for listener used to pass data back to parent fragment.
    */
    public interface VocabularyFilterDialogListener {
        void onFinishDialog(ArrayList<String> selectedLanguages, boolean starredOnly, Sort sortby);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentVocabularyFilterBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpCheckboxes();
        setUpButtons();
    }

    /*
    Create language option checkboxes.
    */
    private void setUpCheckboxes() {
        checkboxes = new ArrayList<>();
        if (getArguments() != null) {
            selectedLanguages = getArguments().getStringArrayList("selected");
            starredOnly = getArguments().getBoolean("starredOnly");
            sortBy = Sort.valueOf(getArguments().getString("sortBy"));
            List<String> languageOptions = getArguments().getStringArrayList("languageOptions");

            for (String language : languageOptions) {
                CheckBox cb = new CheckBox(getActivity());
                cb.setTag(language);
                cb.setText(Utils.getSpinnerText(language));
                cb.setTextSize(16);
                cb.setPadding(15, 0, 0, 0);
                cb.setChecked(selectedLanguages.contains(language));
                binding.languageList.addView(cb);
                checkboxes.add(cb);
            }

            // TODO: decide how checking/unchecking boxes affects "All" checkbox
            binding.cbAll.setChecked(selectedLanguages.containsAll(languageOptions));
            binding.cbAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
                for (CheckBox cb : checkboxes) {
                    cb.setChecked(isChecked);
                }
            });

            binding.cbStarredOnly.setChecked(starredOnly);
            if (sortBy == Sort.ALPHABETICALLY) {
                binding.radioAlphabetical.setChecked(true);
            } else {
                binding.radioCreatedAt.setChecked(true);
            }
        }
    }

    /*
    Set up onClickListeners from cancel and update buttons.
    */
    private void setUpButtons() {
        binding.btnCancel.setOnClickListener(v -> dismiss());
        binding.btnUpdate.setOnClickListener(v -> sendBackSelectedLanguages());
    }

    /*
    Send data about the selected languages back to the parent fragment.
    */
    private void sendBackSelectedLanguages() {
        selectedLanguages.clear();
        for (CheckBox cb : checkboxes) {
            if (cb.isChecked()) {
                selectedLanguages.add(cb.getTag().toString());
            }
        }

        if (selectedLanguages.isEmpty()) {
            Toast.makeText(getActivity(), "You haven't selected any languages!", Toast.LENGTH_SHORT).show();
            return;
        }

        starredOnly = binding.cbStarredOnly.isChecked();
        if (binding.radioAlphabetical.isChecked()) {
            sortBy = Sort.ALPHABETICALLY;
        } else {
            sortBy = Sort.DATE;
        }

        VocabularyFilterDialogListener listener = (VocabularyFilterDialogListener) getTargetFragment();
        if (listener != null) {
            listener.onFinishDialog(selectedLanguages, starredOnly, sortBy);
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
