package com.example.lexis.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.lexis.R;
import com.example.lexis.activities.LoginActivity;
import com.example.lexis.databinding.FragmentProfileBinding;
import com.example.lexis.utilities.Utils;
import com.parse.ParseUser;

import org.parceler.Parcels;

public class ProfileFragment extends Fragment {

    private static final String ARG_USER = "user";

    FragmentProfileBinding binding;
    private ParseUser user;

    public ProfileFragment() {}

    public static ProfileFragment newInstance(ParseUser user) {
        ProfileFragment fragment = new ProfileFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.tvUsername.setText(user.getUsername());
        // TODO: replace with Utils.getCurrentTargetLanguage() after previous PR is merged
        binding.tvTargetLanguage.setText(Utils.getFullLanguage(user.getString("targetLanguage")));

        binding.btnLogout.setOnClickListener(v -> {
            ParseUser.logOut();
            goLoginActivity();
        });
    }

    private void goLoginActivity() {
        Intent i = new Intent(getActivity(), LoginActivity.class);
        startActivity(i);
        getActivity().finish();
    }
}