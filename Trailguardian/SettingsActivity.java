package com.example.trailguardian;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private Button returnB, nameB, passwordB, logoutB, deleteUserB;
    private EditText nameET, passwordET, password2ET;
    private TextView pollingRateTV;
    private SeekBar pollingRateSB;
    private SharedPreferences tempPreferences;
    private Helper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        dbHelper =  new Helper(this);

        tempPreferences = getSharedPreferences("temp", Context.MODE_PRIVATE);
        setupUI();
    }

    private void setupUI() {
        returnB = findViewById(R.id.returnB);
        nameET = findViewById(R.id.nameET);
        nameB = findViewById(R.id.nameB);
        passwordET = findViewById(R.id.passwordET);
        password2ET = findViewById(R.id.password2ET);
        passwordB = findViewById(R.id.passwordB);
        logoutB = findViewById(R.id.logoutB);
        deleteUserB = findViewById(R.id.deleteUserB);
        pollingRateTV = findViewById(R.id.pollingRateTV);
        pollingRateSB = findViewById(R.id.pollingRateSB);

        returnB.setOnClickListener(this);
        nameB.setOnClickListener(this);
        passwordB.setOnClickListener(this);
        logoutB.setOnClickListener(this);
        deleteUserB.setOnClickListener(this);

        float pollingRate = tempPreferences.getFloat("pollingRate", 15);
        pollingRateTV.setText(String.format("Polling rate: %d seconds", (int) pollingRate));
        pollingRateSB.setProgress((int) pollingRate - 1);
        pollingRateSB.setOnSeekBarChangeListener(this);
    }

    //Manages button clicks
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.returnB) {
            finish();
        } else if (v.getId() == R.id.nameB) {
            updateUserName();
        } else if (v.getId() == R.id.passwordB) {
            updateUserPassword();
        } else if (v.getId() == R.id.logoutB) {
            logoutUser();
        } else if (v.getId() == R.id.deleteUserB) {
            deleteUser();
        }
    }

    //Updates username by calling helper updateusername function upon the click of nameB Button
    private void updateUserName() {
        String newName = nameET.getText().toString().trim();
        if (!newName.isEmpty()) {
            String currentUserEmail = PreferencesUtil.getLoggedInUserEmail(SettingsActivity.this);
            if (currentUserEmail != null) {
                Toast.makeText(this, "Name updated successfully.", Toast.LENGTH_SHORT).show();
                dbHelper.updateUserName(currentUserEmail, newName);
            } else {
                Toast.makeText(this, "Failed to update name.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Name field cannot be empty.", Toast.LENGTH_SHORT).show();
        }
    }

    //Updates password by calling helper updateuserPassword function upon the click of passwordB Button
    private void updateUserPassword() {
        String email = PreferencesUtil.getLoggedInUserEmail(SettingsActivity.this);
        String newPassword = passwordET.getText().toString().trim();
        String confirmPassword = password2ET.getText().toString().trim();
        if (!newPassword.isEmpty() && newPassword.equals(confirmPassword) && dbHelper.updateUserPassword(email, newPassword)) {
            Toast.makeText(this, "Password updated successfully.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Passwords do not match or update failed.", Toast.LENGTH_SHORT).show();
        }
    }

    //takes to the login page
    private void logoutUser() {
        SharedPreferences.Editor editor = getSharedPreferences("Preferences", Context.MODE_PRIVATE).edit();
        editor.remove("LoggedInUserEmail").apply();
        startActivity(new Intent(this, LoginActivity.class));
        finishAffinity(); // Close all activities
    }

    // deletes user account from SQL database
    private void deleteUser() {
        String email = PreferencesUtil.getLoggedInUserEmail(SettingsActivity.this);
        if (dbHelper.deleteUserByEmail(email)) {
            Toast.makeText(this, "User deleted successfully.", Toast.LENGTH_SHORT).show();
            logoutUser(); // Log out the user after deletion
        } else {
            Toast.makeText(this, "Failed to delete user.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        progress++; // Adjust the progress
        pollingRateTV.setText(String.format("Polling rate: %d seconds", progress));
        SharedPreferences.Editor editor = tempPreferences.edit();
        editor.putFloat("pollingRate", progress);
        editor.apply();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}

