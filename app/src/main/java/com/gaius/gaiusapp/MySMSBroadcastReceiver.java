package com.gaius.gaiusapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MySMSBroadcastReceiver extends BroadcastReceiver {
    SharedPreferences prefs;

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (SmsRetriever.SMS_RETRIEVED_ACTION.equals(intent.getAction())) {
            Bundle extras = intent.getExtras();
            Status status = (Status) extras.get(SmsRetriever.EXTRA_STATUS);
            prefs = PreferenceManager.getDefaultSharedPreferences(context);

            switch(status.getStatusCode()) {
                case CommonStatusCodes.SUCCESS:
                    // Get SMS message contents
                    String message = (String) extras.get(SmsRetriever.EXTRA_SMS_MESSAGE);
                    // Extract one-time code from the message and complete verification
                    // by sending the code back to your server.
                    Pattern p = Pattern.compile("(|^)\\d{4}");
                    Matcher m = p.matcher(message);
                    if(m.find()) {
                        AndroidNetworking.get(prefs.getString("base_url","http://192.169.152.158:60001/test/") + "OTP.py")
                                .addQueryParameter("number", prefs.getString("number", "null"))
                                .addQueryParameter("OTP", m.group(0))
                                .setPriority(Priority.HIGH)
                                .build()
                                .getAsJSONArray(new JSONArrayRequestListener() {
                                    @Override
                                    public void onResponse(JSONArray response) {
                                        try {
                                            JSONObject user_info;
                                            user_info = response.getJSONObject(0);

                                            if (user_info.has("token")) {
                                                SharedPreferences.Editor editor = prefs.edit();
                                                editor.putString("name", user_info.getString("name"));
                                                editor.putString("token", user_info.getString("token"));
                                                editor.putString("channel", user_info.getString("channel"));
                                                editor.putString("gender", user_info.getString("gender"));
                                                editor.putString("age", user_info.getString("age"));
                                                editor.putString("email", user_info.getString("email"));
                                                editor.putString("userID", user_info.getString("userID"));
                                                editor.putString("number", user_info.getString("phoneNumber"));
                                                editor.putString("admin", user_info.getString("admin"));
                                                editor.commit();

                                                Intent i = new Intent(context, MainActivity.class);
                                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                context.startActivity(i);
                                            }
                                            else {
                                                SharedPreferences.Editor editor = prefs.edit();
                                                editor.putString("OTP", user_info.getString("OTP"));
                                                editor.commit();

                                                Intent i = new Intent(context, SignUpSMSActivity.class);
                                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                context.startActivity(i);
                                            }
                                        }
                                        catch (JSONException e) {
                                            e.printStackTrace();
                                            Log.d("Yasir", "Json error " + e);
                                        }
                                    }

                                    @Override
                                    public void onError(ANError error) {

                                        switch (error.getErrorCode()) {
                                            case 401:
                                                break;
                                            case 500:
                                                Log.d("SMS", "Error 500" + error);
                                                Toast.makeText(context, "Invalid OTP, please correct it", Toast.LENGTH_SHORT).show();
                                                break;
                                            default:
                                                Log.d("SMS", "Error no Internet " + error);
                                        }
                                    }
                                });                    }



                    break;
                case CommonStatusCodes.TIMEOUT:
                    // Waiting for SMS timed out (5 minutes)
                    // Handle the error ...
                    break;
            }
        }
    }
}

