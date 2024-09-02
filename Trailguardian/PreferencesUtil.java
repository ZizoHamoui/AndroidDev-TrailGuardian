package com.example.trailguardian;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesUtil {

    private static final String PREFS_NAME = "AppPreferences";
    private static final String KEY_LOGGED_IN_USER_EMAIL = "LoggedInUserEmail";

    //sets user email in shared preferences
    public static void setLoggedInUserEmail(Context context, String email) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_LOGGED_IN_USER_EMAIL, email);
        editor.apply();
    }

    //fets user email from shared preferences
    public static String getLoggedInUserEmail(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LOGGED_IN_USER_EMAIL, null);
    }
}
