package com.example.trailguardian;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    TextView welcomeTV;
    ImageView profileIV;
    CardView profileCV;
    ConstraintLayout profileCL;
    TextView profileNameTV;
    TextView profileDetailsTV;
    Button navigationB;
    Button historyB;
    Button settingsB;
    Button articleB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initiate buttons and activates all listeners
        welcomeTV = findViewById(R.id.welcomeTV);
        profileIV = findViewById(R.id.profileIV);
        profileCV = findViewById(R.id.profileCV);
        profileCL = findViewById(R.id.profileCL);
        profileNameTV = findViewById(R.id.profileNameTV);
        profileDetailsTV = findViewById(R.id.profileDetailsTV);
        navigationB = findViewById(R.id.navigationB);
        navigationB.setOnClickListener(this);
        historyB = findViewById(R.id.historyB);
        historyB.setOnClickListener(this);
        settingsB = findViewById(R.id.settingsB);
        settingsB.setOnClickListener(this);
        articleB = findViewById(R.id.guideB);
        articleB.setOnClickListener(this);

        //use share preference to get email and then extract user name from it
        String loggedInUserEmail = PreferencesUtil.getLoggedInUserEmail(MainActivity.this);
        if (loggedInUserEmail != null && !loggedInUserEmail.isEmpty()) {
            Helper dbHelper =  new Helper(this);
            String userName = dbHelper.getNameByEmail(loggedInUserEmail.trim());

            if (userName != null) {
                profileNameTV.setText(userName);
            } else {
                profileNameTV.setText("User not found");
            }
        } else {
            profileNameTV.setText("Error");
        }

        updateTrailCount();
        updateDisplayedUsername();
    }

    //Intents to navigate to different activities of the app
    @Override
    public void onClick(View v) {
        if (v == navigationB) {
            startActivity(new Intent(this, NavigationActivity.class));
        } else if (v == historyB) {
            startActivity(new Intent(this, HistoryActivity.class));
        } else if (v == settingsB) {
            startActivity(new Intent(this, SettingsActivity.class));
        }
        //implicit intent to access online articles
        else if (v == articleB) {
            String url = "https://www.rei.com/learn/c/hiking";
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        }
    }

    //onResume updates the trail count and user name if they changed
    @Override
    protected void onResume() {
        super.onResume();
        updateDisplayedUsername();
        updateTrailCount();
    }

    //Checks for username stored in shared preferences and displays in card
    private void updateDisplayedUsername() {
        String loggedInUserEmail = PreferencesUtil.getLoggedInUserEmail(MainActivity.this);
        if (loggedInUserEmail != null && !loggedInUserEmail.isEmpty()) {
            Helper dbHelper = new Helper(this);
            String userName = dbHelper.getNameByEmail(loggedInUserEmail.trim());

            if (userName != null) {
                profileNameTV.setText(userName);
            } else {
                profileNameTV.setText("User not found");
            }
        } else {
            profileNameTV.setText("Error");
        }
    }

    //update trail count based on size of items store in trails shared preferences
    private void updateTrailCount() {
        SharedPreferences prefs = getSharedPreferences("Trails", MODE_PRIVATE);
        int trailCount = prefs.getAll().size();
        String trailText = trailCount + " trails recorded";
        profileDetailsTV.setText(trailText);
    }
}