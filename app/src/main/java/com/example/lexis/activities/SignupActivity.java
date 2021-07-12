package com.example.lexis.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.lexis.databinding.ActivityLoginBinding;
import com.example.lexis.databinding.ActivitySignupBinding;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    ActivitySignupBinding binding;
    Map<Integer, String> languageCodes = new HashMap<Integer, String>() {{
        put(0, "fr");
        put(1, "es");
        put(2, "de");
        put(3, "tr");
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnSignUp.setOnClickListener(v -> {
            String username = binding.etUsername.getText().toString();
            String email = binding.etEmail.getText().toString();
            String password = binding.etPassword.getText().toString();
            signUpUser(username, email, password);
        });
    }

    private void signUpUser(String username, String email, String password) {
        if (email.isEmpty()) {
            Toast.makeText(SignupActivity.this, "E-mail cannot be empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        ParseUser user = new ParseUser();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        int selectedItemPosition = binding.spinnerLanguage.getSelectedItemPosition();
        user.put("targetLanguage", languageCodes.get(selectedItemPosition));

        user.signUpInBackground(e -> {
            if (e != null) {
                showErrorMessage(e);
                return;
            }
            goMainActivity();
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
                errorMessage = "Error with sign-up!";
                break;
        }
        Toast.makeText(SignupActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    private void goMainActivity() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }
}