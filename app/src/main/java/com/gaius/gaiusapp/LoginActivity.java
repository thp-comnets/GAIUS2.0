package com.gaius.gaiusapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private String URL_FOR_LOGIN;
    private Button customSigninButton;
    private TextView custom_signup_button, forgotpassword_button;
    private EditText loginInputEmail, loginInputPassword;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setContentView(R.layout.login_activity);
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        VideoView view = (VideoView)findViewById(R.id.logo);
        String path = "android.resource://" + getPackageName() + "/" + R.raw.gaius_logo;
        view.setVideoURI(Uri.parse(path));
        view.start();
        view.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setVolume(0, 0);
            }
        });

        loginInputEmail = findViewById(R.id.email_edittext);

        loginInputPassword = findViewById(R.id.password_edittext);
        loginInputPassword.setTransformationMethod(new PasswordTransformationMethod());
        loginInputPassword.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    doLogin();
                    return true;
                }
                return false;
            }
        });

        customSigninButton = (Button) findViewById(R.id.custom_signin_button);
        customSigninButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doLogin();
            }
        });

        custom_signup_button = (TextView) findViewById(R.id.custom_signup_button);
        custom_signup_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), SignUp.class);
                startActivity(i);
                finish();
                }
        });

        forgotpassword_button = (TextView) findViewById(R.id.custom_forgotpassword);
        forgotpassword_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), ForgotPassword.class);
                startActivity(i);
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();

        VideoView view = (VideoView)findViewById(R.id.logo);
        String path = "android.resource://" + getPackageName() + "/" + R.raw.gaius_logo;
        view.setVideoURI(Uri.parse(path));
        view.start();

//        loginFromSaveData();
    }

    private void doLogin () {
        boolean error = false;
        if (loginInputEmail.getText().toString().matches("")) {
            loginInputEmail.setError("Email can't be empty", null);
            error = true;
        }
        if (loginInputPassword.getText().toString().matches("")) {
            loginInputPassword.setError("Password can't be empty", null);
            error = true;
        }
        if (!error) {
            getLocalServer(loginInputEmail.getText().toString(), loginInputPassword.getText().toString());
        }
    }

    private void getLocalServer (final String email, final String password) {
        // get local server information

        // Tag used to cancel the request
        String cancel_req_tag = "login";
        StringRequest strReq = new StringRequest(Request.Method.POST,
                "http://192.169.152.158/test/getLocalServer.py", new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jObj = new JSONObject(response);

                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("base_url", "http://" + jObj.getString("server_ip") + ":" + jObj.getString("server_port") + "/" + jObj.getString("server_path") + "/");
                    editor.commit();

                    loginUser(email, password);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("get local server", "Login Error: " + error.getMessage());
                Log.d("get local server", "token "+prefs.getString("token", "XXXXX"));
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
            }
        })
//        {
//
//            @Override
//            protected Map<String, String> getParams() {
//                // Posting params to login url
//                Map<String, String> params = new HashMap<String, String>();
//                params.put("email", email);
//                params.put("password", password);
//                return params;
//            }
//        }
        ;
        // Adding request to request queue
        AppSingleton.getInstance(getApplicationContext()).addToRequestQueue(strReq,cancel_req_tag);
    }

    private void loginUser(final String email, final String password) {
        String baseURL = prefs.getString("base_url", "http://192.169.152.158/test/");
        URL_FOR_LOGIN = baseURL+"login.php";

        // Tag used to cancel the request
        String cancel_req_tag = "login";
        StringRequest strReq = new StringRequest(Request.Method.POST,
                URL_FOR_LOGIN, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    if (!error) {
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("token", jObj.getJSONObject("user").getString("token"));
                        editor.putString("email", email);
                        editor.putString("password", password);
                        editor.putString("name", jObj.getJSONObject("user").getString("name"));
                        editor.putString("channel", jObj.getJSONObject("user").getString("channel"));
                        editor.putString("gender", jObj.getJSONObject("user").getString("gender"));
                        editor.putString("age", jObj.getJSONObject("user").getString("age"));
                        editor.putString("userID", jObj.getJSONObject("user").getString("userID"));
                        editor.putString("number", jObj.getJSONObject("user").getString("phoneNumber"));
                        editor.putString("admin", jObj.getJSONObject("user").getString("admin"));
                        editor.apply();

                        Intent i = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(i);
                        finish();

                    } else {
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("User Login", "Login Error: " + error.getMessage());
                Log.d("User Login", "token "+prefs.getString("token", "XXXXX"));
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);
                params.put("password", password);
                return params;
            }
        };
        // Adding request to request queue
        AppSingleton.getInstance(getApplicationContext()).addToRequestQueue(strReq,cancel_req_tag);
    }
}
