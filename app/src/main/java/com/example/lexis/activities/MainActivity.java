package com.example.lexis.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.lexis.R;
import com.example.lexis.databinding.ActivityMainBinding;
import com.example.lexis.fragments.FeedFragment;
import com.example.lexis.fragments.PracticeFragment;
import com.example.lexis.fragments.ProfileFragment;
import com.example.lexis.utilities.TranslateUtils;
import com.parse.ParseUser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

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
                /*SharedPreferences prefs = getSharedPreferences("nlp_models", Context.MODE_PRIVATE);
                Gson gson = new Gson();
                String jsonPersonFinder = prefs.getString("personFinder", "");
                String jsonLocationFinder = prefs.getString("locationFinder", "");
                String jsonOrganizationFinder = prefs.getString("organizationFinder", "");*/

                File filePerson = new File("personModel.txt");
                if (!filePerson.exists()) {
                    TranslateUtils.getPersonModel(MainActivity.this);
                } else {
                    FileInputStream isPerson = new FileInputStream(filePerson);
                    ObjectInputStream ois = new ObjectInputStream(isPerson);
                    TokenNameFinderModel personModel = (TokenNameFinderModel) ois.readObject();
                    TranslateUtils.setPersonFinder(new NameFinderME(personModel));
                }

                File fileLocation = new File("locationModel.txt");
                if (!fileLocation.exists()) {
                    TranslateUtils.getLocationModel(MainActivity.this);
                } else {
                    FileInputStream isLocation = new FileInputStream(fileLocation);
                    ObjectInputStream ois = new ObjectInputStream(isLocation);
                    TokenNameFinderModel locationModel = (TokenNameFinderModel) ois.readObject();
                    TranslateUtils.setLocationFinder(new NameFinderME(locationModel));
                }

                File fileOrganization = new File("organizationModel.txt");
                if (!fileOrganization.exists()) {
                    TranslateUtils.getOrganizationModel(MainActivity.this);
                } else {
                    FileInputStream isOrganization = new FileInputStream(fileOrganization);
                    ObjectInputStream ois = new ObjectInputStream(isOrganization);
                    TokenNameFinderModel organizationModel = (TokenNameFinderModel) ois.readObject();
                    TranslateUtils.setOrganizationFinder(new NameFinderME(organizationModel));
                }
            } catch (IOException | ClassNotFoundException e) {
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