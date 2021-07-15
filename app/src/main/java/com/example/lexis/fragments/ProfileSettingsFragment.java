package com.example.lexis.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.lexis.R;
import com.example.lexis.activities.SignupActivity;
import com.example.lexis.databinding.FragmentProfileSettingsBinding;
import com.example.lexis.utilities.Utils;
import com.google.android.material.navigation.NavigationView;
import com.parse.ParseException;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.Arrays;
import java.util.List;

public class ProfileSettingsFragment extends Fragment {

    private static final String TAG = "ProfileSettingsFragment";
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

        binding.etEmail.setText(user.getEmail());
        binding.spinnerLanguage.setSelection(languageCodes.indexOf(Utils.getCurrentTargetLanguage()));
        binding.btnSave.setOnClickListener(v -> updateUserInformation());
    }

    private void updateUserInformation() {
        String email = binding.etEmail.getText().toString();
        String password = binding.etPassword.getText().toString();
        String passwordConfirm = binding.etPasswordConfirm.getText().toString();

        if (email.isEmpty()) {
            Toast.makeText(getActivity(), "E-mail cannot be empty!", Toast.LENGTH_SHORT).show();
            return;
        } else if (!password.equals(passwordConfirm)) {
            Toast.makeText(getActivity(), "Passwords don't match!", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedItemPosition = binding.spinnerLanguage.getSelectedItemPosition();
        user.put("targetLanguage", languageCodes.get(selectedItemPosition));
        user.setEmail(email);
        if (!password.isEmpty()) {
            user.setPassword(password); // only set new password if field is filled out
        }

        user.saveInBackground(e -> {
            if (e != null) {
                showErrorMessage(e);
                return;
            }

            Toast.makeText(getActivity(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();

            // switch back to profile info fragment
            Fragment infoFragment = ProfileInfoFragment.newInstance(user);
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.profileFragmentContainer, infoFragment).commit();

            // set profile item as selected in drawer navigation
            Menu menuNav = ((NavigationView) getActivity().findViewById(R.id.navView)).getMenu();
            MenuItem profile = menuNav.getItem(0);
            profile.setChecked(true);
        });
    }

    private void showErrorMessage(ParseException e) {
        String errorMessage;
        switch (e.getCode()) {
            case 101:
                errorMessage = "Invalid username/password!";
                break;
            case 200:
                errorMessage = "Username cannot be empty!";
                break;
            case 201:
                errorMessage = "Password cannot be empty!";
                break;
            case 202:
                errorMessage = "Username is already in use!";
                break;
            case 203:
                errorMessage = "E-mail is already in use!";
                break;
            default:
                errorMessage = "Error with updating information!";
                break;
        }
        Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
    }
}