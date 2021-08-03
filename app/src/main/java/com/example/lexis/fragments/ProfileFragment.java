package com.example.lexis.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.lexis.R;
import com.example.lexis.activities.LoginActivity;
import com.example.lexis.databinding.FragmentProfileBinding;
import com.example.lexis.utilities.Utils;
import com.mikepenz.materialdrawer.holder.ImageHolder;
import com.mikepenz.materialdrawer.holder.StringHolder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.util.MaterialDrawerSliderViewExtensionsKt;
import com.mikepenz.materialdrawer.widget.AccountHeaderView;
import com.parse.ParseUser;

import org.parceler.Parcels;

public class ProfileFragment extends Fragment {

    private static final String ARG_USER = "user";

    FragmentProfileBinding binding;
    private ParseUser user;
    private AccountHeaderView header;

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

        // set up toolbar with logo and hamburger menu for drawer navigation
        AppCompatActivity activity = ((AppCompatActivity) getActivity());
        if (activity != null) {
            activity.setSupportActionBar(binding.toolbar.getRoot());
            activity.getSupportActionBar().setTitle("");
        }

        Utils.setLanguageLogo(binding.toolbar.ivLogo);
        binding.toolbar.ibHamburger.setOnClickListener(v -> openDrawerMenu());
        setUpMaterialDrawer();

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
    Open drawer navigation view when hamburger icon is clicked.
    */
    private void openDrawerMenu() {
        // check for updates to user e-mail or profile picture
        header.updateProfile(new ProfileDrawerItem()
        {{
            setIdentifier(100);
            setName(new StringHolder(ParseUser.getCurrentUser().getUsername()));
            setDescription(new StringHolder(ParseUser.getCurrentUser().getEmail()));
            setIcon(new ImageHolder(Utils.getProfilePictureUrl(ParseUser.getCurrentUser())));
        }});

        binding.drawerLayout.openDrawer(binding.slider);
    }

    /*
    Set up the contents of the MaterialDrawer navigation view.
    */
    private void setUpMaterialDrawer() {
        header = new AccountHeaderView(this.getActivity()) {{
            addProfiles(
                    new ProfileDrawerItem()
                    {{
                        setIdentifier(100);
                        setName(new StringHolder(ParseUser.getCurrentUser().getUsername()));
                        setDescription(new StringHolder(ParseUser.getCurrentUser().getEmail()));
                        setIcon(new ImageHolder(Utils.getProfilePictureUrl(ParseUser.getCurrentUser())));
                        setProfileImagesClickable(false);
                    }}
            );
            attachToSliderView(binding.slider);
            setSelectionListEnabledForSingleProfile(false);
        }};

        MaterialDrawerSliderViewExtensionsKt.addItems(binding.slider,
                new PrimaryDrawerItem()
                {
                    {
                        setName(new StringHolder(R.string.profile_label));
                        setIcon(new ImageHolder(R.drawable.profile_icon));
                        setIconTinted(true);
                        setSelectable(true);
                        setSelected(true);
                        setIdentifier(0);
                    }
                },
                new PrimaryDrawerItem()
                {
                    {
                        setName(new StringHolder(R.string.settings_label));
                        setIcon(new ImageHolder(R.drawable.settings_icon));
                        setIconTinted(true);
                        setSelectable(true);
                        setIdentifier(1);
                    }
                },
                new PrimaryDrawerItem()
                {
                    {
                        setName(new StringHolder(R.string.log_out_label));
                        setIcon(new ImageHolder(R.drawable.log_out_icon));
                        setIconTinted(true);
                        setSelectable(true);
                        setIdentifier(2);
                    }
                });

        binding.slider.setOnDrawerItemClickListener(((view, drawerItem, integer) -> {
            if (drawerItem != null) {
                Fragment fragment = null;
                if (drawerItem.getIdentifier() == 0) {
                    fragment = ProfileInfoFragment.newInstance(ParseUser.getCurrentUser());
                } else if (drawerItem.getIdentifier() == 1) {
                    fragment = ProfileSettingsFragment.newInstance(ParseUser.getCurrentUser());
                } else if (drawerItem.getIdentifier() == 2) {
                    ParseUser.logOut();
                    goLoginActivity();
                }

                if (fragment != null) {
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.profileFragmentContainer, fragment).commit();
                }
            }
            return false;
        }));
    }
}