package com.example.trailguardian;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.trailguardian.databinding.LoginPageBinding;

public class LoginActivity extends AppCompatActivity {

    private LoginPageBinding binding;
    private Helper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = LoginPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = new Helper(this);

        //listens to button clicks and acts as needed, either rejecting the input or moving to registration page
        binding.loginButton.setOnClickListener(view -> {
            String email = binding.loginEmail.getText().toString().trim(); // Use trim() to remove leading and trailing spaces
            String password = binding.loginPassword.getText().toString().trim();

            // Checks to see if any fields are empty
            if(email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "All fields are mandatory", Toast.LENGTH_SHORT).show();
            } else {
                // If fields are not empty, it checks the database to match email and password
                Boolean checkCredentials = dbHelper.checkEmailPassword(email, password);
                if (checkCredentials) {
                    Toast.makeText(LoginActivity.this, "Login Successfully!", Toast.LENGTH_SHORT).show();
                    PreferencesUtil.setLoggedInUserEmail(this, email);
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish(); // Calls finish to close the LoginActivity once the user has successfully logged in
                } else {
                    Toast.makeText(LoginActivity.this, "Login failed. Incorrect email or password.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //goes to registration page
        binding.registerLinkText.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}
