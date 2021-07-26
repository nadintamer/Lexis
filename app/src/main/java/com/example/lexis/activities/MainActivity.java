package com.example.lexis.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.example.lexis.R;
import com.example.lexis.databinding.ActivityMainBinding;
import com.example.lexis.fragments.FeedFragment;
import com.example.lexis.fragments.PracticeFragment;
import com.example.lexis.fragments.ProfileFragment;
import com.example.lexis.utilities.Const;
import com.example.lexis.utilities.TranslateUtils;
import com.parse.ParseUser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        TranslateUtils.getTranslateService(this);
        // get named-entity recognition model in background thread
        new Thread(() -> {
            try {
                File filePerson = new File(getFilesDir(), Const.personModelFile);
                if (!filePerson.exists()) {
                    Log.i("MainActivity", "creating personModel.txt!");
                    TranslateUtils.getPersonModel(MainActivity.this);
                } else {
                    Log.i("MainActivity", "personModel.txt exists!");
                    FileInputStream isPerson = openFileInput(Const.personModelFile);
                    TokenNameFinderModel personModel = new TokenNameFinderModel(isPerson);
                    TranslateUtils.setPersonFinder(new NameFinderME(personModel));
                }

                File fileLocation = new File(getFilesDir(), Const.locationModelFile);
                if (!fileLocation.exists()) {
                    Log.i("MainActivity", "creating locationModel.txt!");
                    TranslateUtils.getLocationModel(MainActivity.this);
                } else {
                    Log.i("MainActivity", "locationModel.txt exists!");
                    FileInputStream isLocation = openFileInput(Const.locationModelFile);
                    TokenNameFinderModel locationModel = new TokenNameFinderModel(isLocation);
                    TranslateUtils.setLocationFinder(new NameFinderME(locationModel));
                }

                File fileOrganization = new File(getFilesDir(), Const.organizationModelFile);
                if (!fileOrganization.exists()) {
                    Log.i("MainActivity", "creating organizationModel.txt!");
                    TranslateUtils.getOrganizationModel(MainActivity.this);
                } else {
                    Log.i("MainActivity", "organizationModel.txt exists!");
                    FileInputStream isOrganization = openFileInput(Const.organizationModelFile);
                    TokenNameFinderModel organizationModel = new TokenNameFinderModel(isOrganization);
                    TranslateUtils.setOrganizationFinder(new NameFinderME(organizationModel));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        // set up tab navigation
        binding.bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            switchToSelectedFragment(item);
            return true;
        });
        binding.bottomNavigation.setSelectedItemId(R.id.action_home); // default tab is home
    }

    /*
    Switch to the appropriate fragment selected through the bottom tab navigation.
    */
    private void switchToSelectedFragment(MenuItem item) {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        final Fragment feedFragment = new FeedFragment();
        final Fragment practiceFragment = new PracticeFragment();
        final Fragment profileFragment = ProfileFragment.newInstance(ParseUser.getCurrentUser());

        Fragment fragment;
        switch (item.getItemId()) {
            case R.id.action_home:
                fragment = feedFragment;
                break;
            case R.id.action_practice:
                fragment = practiceFragment;
                break;
            case R.id.action_profile:
            default:
                fragment = profileFragment;
                break;
        }
        fragmentManager.beginTransaction().replace(R.id.fragmentContainer, fragment).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false; // need to return false so that control is passed down to fragments
    }
}