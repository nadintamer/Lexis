package com.example.lexis.fragments;

import android.content.Context;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.example.lexis.R;
import com.example.lexis.databinding.FragmentProfileSettingsBinding;
import com.example.lexis.utilities.Const;
import com.example.lexis.utilities.Utils;
import com.google.android.material.navigation.NavigationView;
import com.mikepenz.materialdrawer.widget.MaterialDrawerSliderView;
import com.parse.ParseException;
import com.parse.ParseUser;

import org.apache.commons.validator.routines.EmailValidator;
import org.parceler.Parcels;

import java.util.List;

public class ProfileSettingsFragment extends Fragment {

    private static final String ARG_USER = "user";
    private static final int FREQUENCY_INTERVAL_RARE = 60;
    private static final int FREQUENCY_INTERVAL_REGULAR = 40;
    private static final int FREQUENCY_INTERVAL_FREQUENT = 20;

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
        binding.spinnerLanguage.setSelection(Const.languageCodes.indexOf(Utils.getCurrentTargetLanguage()));
        binding.btnSave.setOnClickListener(v -> updateUserInformation());

        int translationInterval = Utils.getTranslationInterval(user);
        if (translationInterval == FREQUENCY_INTERVAL_RARE) {
            binding.radioRare.setChecked(true);
        } else if (translationInterval == FREQUENCY_INTERVAL_REGULAR) {
            binding.radioRegular.setChecked(true);
        } else {
            binding.radioFrequent.setChecked(true);
        }
    }

    /*
    Update the current user's e-mail, password, and/or target language; show error message if action
    cannot be completed.
    */
    private void updateUserInformation() {
        String email = binding.etEmail.getText().toString();
        String password = binding.etPassword.getText().toString();
        String passwordConfirm = binding.etPasswordConfirm.getText().toString();
        boolean shouldReturnToProfile = true;

        if (email.isEmpty()) {
            Toast.makeText(getActivity(), "E-mail cannot be empty!", Toast.LENGTH_SHORT).show();
            return;
        } else if (!EmailValidator.getInstance().isValid(email)) {
            Toast.makeText(getActivity(), "E-mail is not valid!", Toast.LENGTH_SHORT).show();
            return;
        } else if (!password.equals(passwordConfirm)) {
            Toast.makeText(getActivity(), "Passwords don't match!", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedItemPosition = binding.spinnerLanguage.getSelectedItemPosition();
        String targetLanguage = Const.languageCodes.get(selectedItemPosition);
        user.put("targetLanguage", targetLanguage);
        if (!user.getEmail().equals(email)) { // e-mail is updated
            user.setEmail(email);
        }
        if (!password.isEmpty()) { // only set new password if field is filled out
            try {
                if (user.fetch().getBoolean("emailVerified")) {
                    user.setPassword(password);
                } else {
                    Toast.makeText(getActivity(), "Please verify your e-mail before updating your password!", Toast.LENGTH_SHORT).show();
                    shouldReturnToProfile = false;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if (binding.radioRare.isChecked()) {
            user.put("frequencyInterval", FREQUENCY_INTERVAL_RARE);
        } else if (binding.radioRegular.isChecked()) {
            user.put("frequencyInterval", FREQUENCY_INTERVAL_REGULAR);
        } else {
            user.put("frequencyInterval", FREQUENCY_INTERVAL_FREQUENT);
        }

        boolean finalShouldReturnToProfile = shouldReturnToProfile;
        user.saveInBackground(e -> {
            if (e != null) {
                showErrorMessage(e);
                return;
            }
            try {
                if (!password.isEmpty() && user.fetch().getBoolean("emailVerified")) {
                    logInWithNewPassword(password);
                } else if (finalShouldReturnToProfile) {
                    String message = "Settings updated successfully!";
                    if (!user.fetch().getBoolean("emailVerified")) {
                        message += " Please verify your e-mail.";
                    }
                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                    resetToProfileInfoFragment();
                }
            } catch (ParseException parseException) {
                parseException.printStackTrace();
            }
        });
    }

    /*
    Display the error message from Parse to the user in a Toast.
    */
    private void showErrorMessage(ParseException e) {
        Log.e("Settings", "Exception", e);
        String errorMessage = Utils.getUserErrorMessage(e, "Error with updating information!");
        Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
    }

    /*
    Reset the currently displayed fragment and selected menu item to the profile info fragment.
    */
    private void resetToProfileInfoFragment() {
        // hide keyboard when switching fragments
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);

        // switch back to profile info fragment
        Fragment infoFragment = ProfileInfoFragment.newInstance(user);
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.profileFragmentContainer, infoFragment).commit();

        // set profile item as selected in drawer navigation
        MaterialDrawerSliderView drawerSliderView = getActivity().findViewById(R.id.slider);
        drawerSliderView.setSelectedItemIdentifier(0);
    }

    /*
    Log the user in again with their new password since changing their password ends the current
    session.
    */
    private void logInWithNewPassword(String password) {
        ParseUser.logInInBackground(user.getUsername(), password, (user, e) -> {
            if (e != null) {
                Toast.makeText(getActivity(), "Error updating password!", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(getActivity(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();
            resetToProfileInfoFragment();
        });
    }
}