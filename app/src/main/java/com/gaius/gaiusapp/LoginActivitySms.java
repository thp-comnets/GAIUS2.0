package com.gaius.gaiusapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.gaius.gaiusapp.utils.ServerInfo;
import com.mukesh.OtpView;

import net.rimoto.intlphoneinput.IntlPhoneInput;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class LoginActivitySms extends AppCompatActivity {
//    private String URL_FOR_LOGIN;
    private Button customSigninButton;
    private IntlPhoneInput phoneInputView;
    private CardView phoneCard;
    private TextView message,message2;
    private OtpView otp_view;

//    private TextView custom_signup_button, forgotpassword_button, serverList;
//    private EditText loginInputEmail, loginInputPassword;
//    private Spinner spinner;
//    private ArrayList<ServerInfo> serversArrayList;
//    private ArrayList<String> names;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setContentView(R.layout.login_activity_sms);
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        phoneInputView = (IntlPhoneInput) findViewById(R.id.my_phone_input);
        phoneCard = findViewById(R.id.phoneCard);
        message = findViewById(R.id.message);
        message2 = findViewById(R.id.message2);
        otp_view = findViewById(R.id.otp_view);

        customSigninButton = (Button) findViewById(R.id.custom_signin_button);
        customSigninButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doLogin();
            }
        });
    }

    void doLogin () {
        Log.d("sms", "Logging in");

        String baseURL = prefs.getString("base_url", "http://192.169.152.158/test/");

        customSigninButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("token", "45103155f1");
//                editor.putString("email", email);
//                editor.putString("password", password);
//                editor.putString("name", jObj.getJSONObject("user").getString("name"));
//                editor.putString("channel", jObj.getJSONObject("user").getString("channel"));
//                editor.putString("gender", jObj.getJSONObject("user").getString("gender"));
//                editor.putString("age", jObj.getJSONObject("user").getString("age"));
//                editor.putString("userID", jObj.getJSONObject("user").getString("userID"));
//                editor.putString("number", jObj.getJSONObject("user").getString("phoneNumber"));
                editor.putString("admin", "1");
                editor.apply();


                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
                finish();
            }
        });

        AndroidNetworking.get("http://192.169.152.158/test/sendSMS.py")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONArray(new JSONArrayRequestListener() {
                    @Override
                    public void onResponse(JSONArray response) {
                        message.setText("Enter the 4-digit code sent to you");
                        message2.setVisibility(View.VISIBLE);
                        otp_view.setVisibility(View.VISIBLE);
                        message2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                message.setText("Enter your mobile number");
                                message2.setVisibility(View.INVISIBLE);
                                phoneCard.setVisibility(View.VISIBLE);
                                otp_view.setVisibility(View.INVISIBLE);

                                customSigninButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        doLogin();
                                    }
                                });
                            }
                        });

                        phoneCard.setVisibility(View.INVISIBLE);
                        Log.d("SMS", "all ok");
                    }

                    @Override
                    public void onError(ANError error) {

                        switch (error.getErrorCode()) {
                            case 401:
                                break;
                            case 500:
                                Log.d("SMS", "Error 500" + error);
                                break;
                            default:
                                Log.d("SMS", "Error no Internet " + error);
                        }
                    }
                });
    }
}
