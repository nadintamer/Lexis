package com.example.lexis.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.Toast;

import com.example.lexis.R;
import com.example.lexis.databinding.ActivitySignupBinding;
import com.example.lexis.utilities.Const;
import com.example.lexis.utilities.Utils;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;


public class SignupActivity extends AppCompatActivity {

    ActivitySignupBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setUpToolbar();

        binding.btnSignUp.setOnClickListener(v -> {
            String username = binding.etUsername.getText().toString();
            String email = binding.etEmail.getText().toString();
            String password = binding.etPassword.getText().toString();
            int selectedItemPosition = binding.spinnerLanguage.getSelectedItemPosition();
            String targetLanguage = Const.languageCodes.get(selectedItemPosition);
            signUpUser(username, email, password, targetLanguage);
        });

        binding.ivLogo.setBackgroundResource(R.drawable.logo_animation);
        AnimationDrawable frameAnimation = (AnimationDrawable) binding.ivLogo.getBackground();
        frameAnimation.start();
    }

    /*
    Set up toolbar with custom back button.
    */
    private void setUpToolbar() {
        setSupportActionBar(binding.toolbar.getRoot());
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("");
        }

        binding.toolbar.getRoot().setNavigationIcon(R.drawable.back_arrow);
        Drawable navigationIcon = binding.toolbar.getRoot().getNavigationIcon();
        if (navigationIcon != null) {
            navigationIcon.setTint(getResources().getColor(R.color.black));
        }
        binding.toolbar.getRoot().setNavigationOnClickListener(v -> onBackPressed());
    }

    /*
    Sign up a new user with the provided username, e-mail, password, and target language
    to the Parse database.
    */
    private void signUpUser(String username, String email, String password, String targetLanguage) {
        if (email.isEmpty()) {
            Toast.makeText(SignupActivity.this, "E-mail cannot be empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        ParseUser user = new ParseUser();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.put("targetLanguage", targetLanguage);
        List<String> allLanguages = new ArrayList<>();
        user.put("studyingLanguages", allLanguages);

        user.signUpInBackground(e -> {
            if (e != null) {
                showErrorMessage(e);
                return;
            }
            Toast.makeText(this, "Account created! Please verify your e-mail before logging in.", Toast.LENGTH_SHORT).show();
            ParseUser.logOut();
            goLoginActivity();
        });
    }

    /*
    Display the error message from Parse to the user in a Toast.
    */
    private void showErrorMessage(ParseException e) {
        String errorMessage = Utils.getUserErrorMessage(e, "Error with sign-up!");
        Toast.makeText(SignupActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    /*
    Navigate to the login activity.
    */
    private void goLoginActivity() {
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
        finish();
    }
}