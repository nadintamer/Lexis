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
import com.example.lexis.utilities.Utils;
import com.google.android.material.navigation.NavigationView;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.Set;

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

        AppCompatActivity activity = ((AppCompatActivity) getActivity());
        if (activity != null) {
            activity.setSupportActionBar(binding.toolbar.getRoot());
            binding.toolbar.getRoot().setNavigationIcon(R.drawable.hamburger_menu_icon);
            activity.getSupportActionBar().setTitle("");
        }

        setHasOptionsMenu(true);
        ViewCompat.setLayoutDirection(binding.toolbar.getRoot(), ViewCompat.LAYOUT_DIRECTION_RTL);

        setupDrawerContent(binding.navView);

        Fragment infoFragment = ProfileInfoFragment.newInstance(user);
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.profileFragmentContainer, infoFragment).commit();
    }

    private void goLoginActivity() {
        Intent i = new Intent(getActivity(), LoginActivity.class);
        startActivity(i);
        getActivity().finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        switch (item.getItemId()) {
            case android.R.id.home:
                binding.drawerLayout.openDrawer(GravityCompat.END);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            selectDrawerItem(menuItem);
            return true;
        });
    }

    private void selectDrawerItem(MenuItem menuItem) {
        // Create a new fragment and specify the fragment to show based on nav item clicked
        Fragment fragment;
        switch (menuItem.getItemId()) {
            case R.id.nav_profile:
                fragment = ProfileInfoFragment.newInstance(ParseUser.getCurrentUser());
                break;
            case R.id.nav_settings:
                fragment = ProfileSettingsFragment.newInstance(ParseUser.getCurrentUser());
                break;
            case R.id.nav_log_out:
                ParseUser.logOut();
                goLoginActivity();
                return;
            default:
                fragment = ProfileInfoFragment.newInstance(ParseUser.getCurrentUser());
        }

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.profileFragmentContainer, fragment).commit();

        // Highlight the selected item has been done by NavigationView
        menuItem.setChecked(true);
        // Set action bar title
        // setTitle(menuItem.getTitle());
        // Close the navigation drawer
        binding.drawerLayout.closeDrawers();
    }
}