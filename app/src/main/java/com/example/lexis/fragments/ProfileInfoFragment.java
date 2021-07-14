package com.example.lexis.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.lexis.databinding.FragmentProfileInfoBinding;
import com.example.lexis.utilities.Utils;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.parceler.Parcels;

public class ProfileInfoFragment extends Fragment {

    FragmentProfileInfoBinding binding;
    ParseUser user;

    private static final String ARG_USER = "user";

    public ProfileInfoFragment() {}

    public static ProfileInfoFragment newInstance(ParseUser user) {
        ProfileInfoFragment fragment = new ProfileInfoFragment();
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
        binding = FragmentProfileInfoBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setHasOptionsMenu(true);
        binding.tvUsername.setText(user.getUsername());
        // TODO: replace with Utils.getCurrentTargetLanguage() & add language flag once previous
        //  PR is merged
        String targetLanguage = Utils.getFullLanguage(user.getString("targetLanguage"));
        binding.tvTargetLanguage.setText(targetLanguage);
        binding.tvVocabularyTarget.setText(String.format("%s words seen", targetLanguage));
        binding.tvNumTarget.setText(String.valueOf(getNumWordsSeen(true)));
        binding.tvNumTotal.setText(String.valueOf(getNumWordsSeen(false)));
    }

    // TODO: implement once previous PR is merged
    private int getNumWordsSeen(Boolean targetOnly) {
        return 0;
    }
}