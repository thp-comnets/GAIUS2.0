package com.gaius.gaiusapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.mukesh.OtpView;

import net.rimoto.intlphoneinput.IntlPhoneInput;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import swarajsaaj.smscodereader.interfaces.OTPListener;
import swarajsaaj.smscodereader.receivers.OtpReader;


public class LoginSMSActivity extends AppCompatActivity implements OTPListener {
//    private String URL_FOR_LOGIN;
    private Button customSigninButton;
    private IntlPhoneInput phoneInputView;
    private CardView phoneCard;
    private TextView message,message2,message3;
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
        message3 = findViewById(R.id.resend_otp_message);

        otp_view = findViewById(R.id.otp_view);

        customSigninButton = (Button) findViewById(R.id.custom_signin_button);
        customSigninButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!phoneInputView.isValid()) {
                    Toast.makeText(getApplicationContext(), "Invalid phone number please correct", Toast.LENGTH_SHORT).show();
                }
                else {
                    doLogin();
                }
            }
        });

        OtpReader.bind(this,"sms");

    }

    void doLogin () {
        Log.d("sms", "Logging in");

        AndroidNetworking.get("http://192.169.152.158/test/OTP.py")
                .addQueryParameter("number", "00"+phoneInputView.getNumber().substring(1))
                .addQueryParameter("cm-token", prefs.getString("cm-token", "null"))
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONArray(new JSONArrayRequestListener() {
                    @Override
                    public void onResponse(JSONArray response) {
                        customSigninButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                validateOTP();
                            }
                        });

                        message.setText("Enter the 4-digit code sent to you");
                        message2.setVisibility(View.VISIBLE);
                        message3.setVisibility(View.VISIBLE);
                        otp_view.setVisibility(View.VISIBLE);

                        message2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                message.setText("Enter your mobile number");
                                message2.setVisibility(View.INVISIBLE);
                                message3.setVisibility(View.INVISIBLE);
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

                        message3.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                doLogin();
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
                                Toast.makeText(getApplicationContext(), "Invalid phone number", Toast.LENGTH_SHORT).show();
                                break;
                            default:
                                Log.d("SMS", "Error no Internet " + error);
                        }
                    }
                });
    }

    void validateOTP () {
        AndroidNetworking.get("http://192.169.152.158/test/OTP.py")
                .addQueryParameter("number", "00"+phoneInputView.getNumber().substring(1))
                .addQueryParameter("OTP", otp_view.getText().toString())
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

                                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(i);

                                finish();
                            }
                            else {
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString("OTP", user_info.getString("OTP"));
                                editor.putString("number", "00"+phoneInputView.getNumber().substring(1));
                                editor.apply();

                                Intent i = new Intent(getApplicationContext(), SignUpSMSActivity.class);
                                startActivity(i);
                                finish();
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
                                Toast.makeText(getApplicationContext(), "Invalid OTP, please correct it", Toast.LENGTH_SHORT).show();

                                break;
                            default:
                                Log.d("SMS", "Error no Internet " + error);
                        }
                    }
                });
    }
    @Override
    public void otpReceived(String smsText) {
        int indexOfLast = smsText.lastIndexOf(" ");
        String otp_code = smsText;
        if(indexOfLast >= 0) otp_code = smsText.substring(indexOfLast+1, smsText.length());

        Log.d("sms",otp_code);
        otp_view.setText(otp_code);
    }

}
