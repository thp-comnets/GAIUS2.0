package com.gaiusnetworks.gaius.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class LogOut {
    public static void logout (Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("account_token", null);
        editor.putString("email", null);
        editor.putString("password", null);
//        editor.putString("account_name", jObj.getJSONObject("user").getString("name"));
//        editor.putString("account_channel", jObj.getJSONObject("user").getString("channel"));
//        editor.putString("account_email", jObj.getJSONObject("user").getString("email"));
//        editor.putString("account_gender", jObj.getJSONObject("user").getString("gender"));
//        editor.putString("account_age", jObj.getJSONObject("user").getString("age"));
//        editor.putString("account_userID", jObj.getJSONObject("user").getString("userID"));
//        editor.putString("account_number", jObj.getJSONObject("user").getString("phoneNumber"));
        editor.commit();

    }
}
