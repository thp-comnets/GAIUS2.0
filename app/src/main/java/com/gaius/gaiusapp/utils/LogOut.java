package com.gaius.gaiusapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class LogOut {
    public static void logout (Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("token", null);
        editor.putString("email", null);
        editor.putString("password", null);
        editor.putString("name", null);
        editor.putString("channel", null);
        editor.putString("gender", null);
        editor.putString("age", null);
        editor.putString("userID", null);
        editor.putString("number", null);
        editor.commit();

    }
}
