package com.example.lexis.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.widget.Toast;

import com.example.lexis.R;
import com.example.lexis.databinding.ActivityLoginBinding;
import com.example.lexis.fragments.ForgotPasswordDialogFragment;
import com.example.lexis.fragments.WordSearchFragment;
import com.example.lexis.fragments.WordSearchHelpDialogFragment;
import com.parse.ParseException;
import com.parse.ParseUser;

public class LoginActivity extends AppCompatActivity implements ForgotPasswordDialogFragment.ForgotPasswordDialogListener {

    ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (ParseUser.getCurrentUser() != null) {
            goMainActivity();
        }

        binding.tvForgotPassword.setOnClickListener(v -> forgotPasswordClicked());
        binding.tvSignup.setOnClickListener(v -> goSignUpActivity());
        binding.btnLogin.setOnClickListener(v -> {
            String username = binding.etUsername.getText().toString();
            String password = binding.etPassword.getText().toString();
            loginUser(username, password);
        });

        binding.ivLogo.setBackgroundResource(R.drawable.logo_animation);
        AnimationDrawable frameAnimation = (AnimationDrawable) binding.ivLogo.getBackground();
        frameAnimation.start();
    }

    /*
    Attempt to log user in with the provided username and password; navigate to main activity
    if successful.
    */
    private void loginUser(String username, String password) {
        ParseUser.logInInBackground(username, password, (user, e) -> {
            if (e != null) {
                showErrorMessage(e);
                return;
            }
            goMainActivity();
        });
    }

    /*
    Show the forgot password dialog fragment.
    */
    private void forgotPasswordClicked() {
        FragmentManager fm = getSupportFragmentManager();
        ForgotPasswordDialogFragment dialog = new ForgotPasswordDialogFragment();
        dialog.show(fm, "fragment_forgot_password");
    }

    /*
    Send a password reset e-mail to the provided address.
    */
    @Override
    public void onFinishDialog(String email) {
        ParseUser.requestPasswordResetInBackground(email, e -> {
            if (e == null) {
                Toast.makeText(this, "Password reset e-mail sent.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /*
    Displays the appropriate log-in error message to the user through a Toast.
    */
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
            case 205:
                errorMessage = "User e-mail is not verified.";
                break;
            default:
                errorMessage = "Error with log in!";
                break;
        }
        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    private void goSignUpActivity() {
        Intent i = new Intent(LoginActivity.this, SignupActivity.class);
        startActivity(i);
    }

    private void goMainActivity() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }
}