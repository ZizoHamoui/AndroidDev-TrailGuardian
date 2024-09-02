package com.example.trailguardian;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import com.example.trailguardian.databinding.RegisterPageBinding;

public class RegisterActivity extends AppCompatActivity {

    private RegisterPageBinding binding;
    private Helper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = RegisterPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper =  new Helper(this);

        binding.registerButton.setOnClickListener(view -> {
            String name = binding.signupName.getText().toString().trim(); // Trim input
            String email = binding.signupEmail.getText().toString().trim();
            String password = binding.signupPassword.getText().toString().trim();
            String confirmPassword = binding.signupConfirm.getText().toString().trim();

            // Check if all fields are filled
            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "All fields are mandatory", Toast.LENGTH_SHORT).show();
            } else if (!password.equals(confirmPassword)) {
                // Check if passwords match
                Toast.makeText(RegisterActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            } else {
                // Check for if user exists; if not, create account and add to database
                boolean checkUserEmail = dbHelper.checkEmail(email);
                if (!checkUserEmail) {
                    boolean insert = dbHelper.insertData(name, email, password);
                    if (insert) {
                        Toast.makeText(RegisterActivity.this, "Registered Successfully!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish(); // Close the RegisterActivity
                    } else {
                        Toast.makeText(RegisterActivity.this, "Registration Failed!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "User already exists! Please login.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // If loginLinkText clicked, goes to LoginActivity
        binding.loginLinkText.setOnClickListener(view -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }
}
