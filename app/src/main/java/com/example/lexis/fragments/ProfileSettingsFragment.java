package com.example.lexis.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.lexis.R;
import com.example.lexis.databinding.FragmentProfileSettingsBinding;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileSettingsFragment extends Fragment {

    private static final String ARG_USER = "user";
    private static final List<String> languageCodes = Arrays.asList("fr", "es", "de", "tr");

    FragmentProfileSettingsBinding binding;
    ParseUser user;

    public ProfileSettingsFragment() {}

    public static ProfileSettingsFragment newInstance(ParseUser user) {
        ProfileSettingsFragment fragment = new ProfileSettingsFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_USER, Parcels.wrap(user));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user = Parcels.unwrap(getArguments().getParcelable(ARG_USER));
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileSettingsBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // TODO: replace with Utils.getCurrentTargetLanguage()
        binding.spinnerLanguage.setSelection(languageCodes.indexOf(user.getString("targetLanguage")));
        binding.btnSave.setOnClickListener(v -> {
            int selectedItemPosition = binding.spinnerLanguage.getSelectedItemPosition();
            user.put("targetLanguage", languageCodes.get(selectedItemPosition));
            user.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    Toast.makeText(getActivity(), "Saved successfully!", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}