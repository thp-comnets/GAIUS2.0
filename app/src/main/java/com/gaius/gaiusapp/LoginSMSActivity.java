package com.gaius.gaiusapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.gaius.gaiusapp.utils.ServerInfo;
import com.google.firebase.iid.FirebaseInstanceId;
import com.mukesh.OnOtpCompletionListener;
import com.mukesh.OtpView;

import net.rimoto.intlphoneinput.IntlPhoneInput;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import swarajsaaj.smscodereader.interfaces.OTPListener;
import swarajsaaj.smscodereader.receivers.OtpReader;


public class LoginSMSActivity extends AppCompatActivity implements OTPListener {
//    private String URL_FOR_LOGIN;
    private Button customSigninButton;
    private IntlPhoneInput phoneInputView;
    private CardView phoneCard;
    private TextView message,message2,message3, serverList;
    private OtpView otp_view;
    private Spinner spinner;
    private ArrayList<ServerInfo> serversArrayList;
    private ArrayList<String> names;

    SharedPreferences prefs;

    // easter egg
    private AtomicInteger mCounter = new AtomicInteger();
    private Handler handler = new Handler();
    private Runnable mRunnable = new Runnable(){
        @Override
        public void run(){
            mCounter = new AtomicInteger();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setContentView(R.layout.login_activity_sms);
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        phoneInputView = (IntlPhoneInput) findViewById(R.id.my_phone_input);

        ImageView easterEgg = findViewById(R.id.logo2a);
        easterEgg.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                handler.removeCallbacks(mRunnable);
                handler.postDelayed(mRunnable, 1000);
                if(mCounter.incrementAndGet() == 6){
                    spinner.setVisibility(View.VISIBLE);
                    serverList.setVisibility(View.VISIBLE);
                    loadServers();
                }
            }
        });

        phoneCard = findViewById(R.id.phoneCard);
        message = findViewById(R.id.message);
        message2 = findViewById(R.id.message2);
        message3 = findViewById(R.id.resend_otp_message);

        otp_view = findViewById(R.id.otp_view);
        otp_view.setOtpCompletionListener(new OnOtpCompletionListener() {
            @Override
            public void onOtpCompleted(String otp) {
                validateOTP();
            }
        });

        customSigninButton = (Button) findViewById(R.id.custom_signin_button);
        customSigninButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!phoneInputView.isValid()) {
                    Toast.makeText(getApplicationContext(), "Invalid phone number please correct", Toast.LENGTH_SHORT).show();
                }
                else {
                    getLocalServer();
                }
            }
        });

        OtpReader.bind(this,"sms");

        serverList = findViewById(R.id.switch_server);
        spinner = findViewById(R.id.server_selection);
        names = new ArrayList<String>();
        if (prefs.getString("admin", "0").equals("1")) {
            spinner.setVisibility(View.VISIBLE);
            serverList.setVisibility(View.VISIBLE);
            loadServers();
        }
        // request a new firebase token
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    FirebaseInstanceId.getInstance().deleteInstanceId();
                    FirebaseInstanceId.getInstance().getToken();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void loadServers() {
        AndroidNetworking.get("http://192.169.152.158:60001/test/getLocalServer.py")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONArray(new JSONArrayRequestListener() {
                    @Override
                    public void onResponse(JSONArray response) {

                        Log.d("thp", "Server list response as JSON " + response);

                        try {

                            JSONObject servers;
                            serversArrayList = new ArrayList<ServerInfo>();

                            for (int i = 0; i < response.length(); i++) {

                                servers = response.getJSONObject(i);

                                ServerInfo server = new ServerInfo(servers.getString("server_name"), servers.getString("server_ip"), servers.getString("server_port"), servers.getString("server_path"));
                                serversArrayList.add(server);

                            }

                            for (int i = 0; i < serversArrayList.size(); i++) {
                                names.add(serversArrayList.get(i).getName().toString());
                            }

                            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(LoginSMSActivity.this, android.R.layout.simple_spinner_item, names);
                            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinner.setAdapter(spinnerArrayAdapter);

                        } catch (JSONException e) {
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
                                Log.d("Yasir", "Error 500" + error);
                                break;
                            default:
                                Log.d("Yasir", "Error no Internet " + error);
                        }
                    }
                });

    }

    private void getLocalServer() {
        // get local server information
        AndroidNetworking.get("http://192.169.152.158:60001/test/getLocalServer.py")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONArray(new JSONArrayRequestListener() {
                    @Override
                    public void onResponse(JSONArray response) {

                        Log.d("thp", "Server response as JSON " + response);

                        try {

                            JSONObject servers;

                            //getting first object from json array, this is the global server
                            servers = response.getJSONObject(0);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("base_url", "http://" + servers.getString("server_ip") + ":" + servers.getString("server_port") + "/" + servers.getString("server_path") + "/");
                            editor.putString("server_name", servers.getString("server_name"));
                            editor.commit();

                            doLogin();

                        } catch (JSONException e) {
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
                                Log.d("Yasir", "Error 500" + error);
                                break;
                            default:
                                Log.d("Yasir", "Error no Internet " + error);
                        }
                    }
                });

    }

    void doLogin () {
        if (prefs.getString("admin", "0").equals("1")) {
            ServerInfo server = serversArrayList.get(spinner.getSelectedItemPosition());

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("base_url", "http://" + server.getIp() + ":" + server.getPort() + "/" + server.getPath() + "/");
            editor.putString("server_name", names.get(spinner.getSelectedItemPosition()));
            editor.commit();
        }

        AndroidNetworking.get(prefs.getString("base_url","http://192.169.152.158:60001/test/") + "OTP.py")
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

                        customSigninButton.setVisibility(View.GONE);
                        message.setText("Enter the 4-digit code sent to you");
                        message2.setVisibility(View.VISIBLE);
                        message3.setVisibility(View.VISIBLE);
                        otp_view.setVisibility(View.VISIBLE);
                        spinner.setVisibility(View.GONE);
                        serverList.setVisibility(View.GONE);

                        message2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                message.setText("Enter your mobile number");
                                message2.setVisibility(View.GONE);
                                message3.setVisibility(View.GONE);
                                phoneCard.setVisibility(View.VISIBLE);
                                otp_view.setVisibility(View.GONE);
                                customSigninButton.setVisibility(View.VISIBLE);
                                if (prefs.getString("admin", "0").equals("1")) {
                                    spinner.setVisibility(View.VISIBLE);
                                    serverList.setVisibility(View.VISIBLE);
                                    loadServers();
                                }

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

                        phoneCard.setVisibility(View.GONE);
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
        if (prefs.getString("admin", "0").equals("1")) {
            ServerInfo server = serversArrayList.get(spinner.getSelectedItemPosition());

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("base_url", "http://" + server.getIp() + ":" + server.getPort() + "/" + server.getPath() + "/");
            editor.putString("server_name", names.get(spinner.getSelectedItemPosition()));
            editor.commit();
        }

        AndroidNetworking.get(prefs.getString("base_url","http://192.169.152.158:60001/test/") + "OTP.py")
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
