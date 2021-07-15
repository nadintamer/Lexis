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
import com.example.lexis.databinding.FragmentProfileSettingsBinding;
import com.example.lexis.utilities.Utils;
import com.google.android.material.navigation.NavigationView;
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

        binding.spinnerLanguage.setSelection(languageCodes.indexOf(Utils.getCurrentTargetLanguage()));
        binding.btnSave.setOnClickListener(v -> {
            int selectedItemPosition = binding.spinnerLanguage.getSelectedItemPosition();
            user.put("targetLanguage", languageCodes.get(selectedItemPosition));
            user.saveInBackground(e -> {
                if (e != null) {
                    Log.e(TAG, "Error saving target language", e);
                    Toast.makeText(getActivity(), "Error saving target language!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(getActivity(), "Saved successfully!", Toast.LENGTH_SHORT).show();

                // switch back to profile info fragment
                Fragment infoFragment = ProfileInfoFragment.newInstance(user);
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.profileFragmentContainer, infoFragment).commit();

                // set profile item as selected in drawer navigation
                Menu menuNav = ((NavigationView) getActivity().findViewById(R.id.navView)).getMenu();
                MenuItem profile = menuNav.getItem(0);
                profile.setChecked(true);
            });
        });
    }
}