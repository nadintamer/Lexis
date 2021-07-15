package com.example.lexis.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.lexis.R;
import com.example.lexis.activities.LoginActivity;
import com.example.lexis.databinding.FragmentProfileBinding;
import com.google.android.material.navigation.NavigationView;
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // set up hamburger menu on the right side of the screen
        AppCompatActivity activity = ((AppCompatActivity) getActivity());
        if (activity != null) {
            activity.setSupportActionBar(binding.toolbar.getRoot());
            binding.toolbar.getRoot().setNavigationIcon(R.drawable.hamburger_menu_icon);
            activity.getSupportActionBar().setTitle("");
        }

        setHasOptionsMenu(true);
        setupDrawerContent(binding.navView);
        ViewCompat.setLayoutDirection(binding.toolbar.getRoot(), ViewCompat.LAYOUT_DIRECTION_RTL);

        // set profile info fragment as default
        Fragment infoFragment = ProfileInfoFragment.newInstance(user);
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.profileFragmentContainer, infoFragment).commit();
    }

    /*
    Go back to the log in activity and finish current activity so user cannot return without
    logging in again.
    */
    private void goLoginActivity() {
        Intent i = new Intent(getActivity(), LoginActivity.class);
        startActivity(i);
        getActivity().finish();
    }

    /*
    Handle menu item clicks - open drawer navigation view when hamburger icon is clicked.
    */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            binding.drawerLayout.openDrawer(GravityCompat.END);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*
    Set up listener to handle selections in the drawer navigation view.
    */
    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            selectDrawerItem(menuItem);
            return true;
        });
    }

    /*
    Navigate to the fragment selected through the drawer navigation view.
    */
    private void selectDrawerItem(MenuItem menuItem) {
        Fragment fragment;
        switch (menuItem.getItemId()) {
            case R.id.nav_settings:
                fragment = ProfileSettingsFragment.newInstance(ParseUser.getCurrentUser());
                break;
            case R.id.nav_log_out:
                ParseUser.logOut();
                goLoginActivity();
                return;
            case R.id.nav_profile:
            default:
                fragment = ProfileInfoFragment.newInstance(ParseUser.getCurrentUser());
        }

        // replace current fragment with the selected one and highlight selected item
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.profileFragmentContainer, fragment).commit();

        menuItem.setChecked(true);
        binding.drawerLayout.closeDrawers();
    }
}