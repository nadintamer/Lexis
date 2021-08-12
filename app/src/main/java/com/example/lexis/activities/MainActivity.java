package com.example.lexis.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.lexis.R;
import com.example.lexis.databinding.ActivityMainBinding;
import com.example.lexis.fragments.FeedFragment;
import com.example.lexis.fragments.PracticeFlashcardFragment;
import com.example.lexis.fragments.PracticeFragment;
import com.example.lexis.fragments.ProfileFragment;
import com.example.lexis.fragments.WordSearchFragment;
import com.example.lexis.models.WordSearch;
import com.example.lexis.utilities.TranslateUtils;
import com.parse.ParseUser;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        TranslateUtils.getTranslateService(this);
        TranslateUtils.getNLPService(this);

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

    @Override
    public void onBackPressed() {
        WordSearchFragment wordSearchFragment = (WordSearchFragment) getSupportFragmentManager().findFragmentByTag("WordSearchFragment");
        PracticeFlashcardFragment flashcardFragment = (PracticeFlashcardFragment) getSupportFragmentManager().findFragmentByTag("FlashcardFragment");
        if (wordSearchFragment != null && wordSearchFragment.isVisible()) {
            wordSearchFragment.returnToPracticeTab();
        } else if (flashcardFragment != null && flashcardFragment.isVisible()) {
            flashcardFragment.returnToPracticeTab();
        } else {
            super.onBackPressed();
        }
    }
}