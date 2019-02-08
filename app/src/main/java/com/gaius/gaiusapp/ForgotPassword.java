package com.gaius.gaiusapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ForgotPassword extends AppCompatActivity {

    private String URL_FOR_FORGOT_PASSWORD;
    private ProgressDialog progressDialog;
    private EditText loginInputEmail, loginInputNewPassword, loginInputNewPassword2, loginInputToken;

    private static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        URL_FOR_FORGOT_PASSWORD = prefs.getString("base_url", null) + "forgotPassword.py";

        loginInputEmail = findViewById(R.id.login_input_email);
        loginInputNewPassword = findViewById(R.id.login_input_new_password);
        loginInputNewPassword2 = findViewById(R.id.login_input_new_password2);
        loginInputToken = findViewById(R.id.login_input_token);
        Button btnResetPassword = findViewById(R.id.btn_reset_password);
        Button btnSendToken = findViewById(R.id.btn_send_token);

        // Progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(true);


        btnSendToken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isEmailValid(loginInputEmail.getText().toString())) {
                    loginInputEmail.setError(getString(R.string.error_email_format));
                    return;
                }
                forgotPassword(loginInputEmail.getText().toString());
            }
        });

        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean error = false;
                if (loginInputEmail.getText().toString().matches("")) {
                    loginInputEmail.setError(getString(R.string.error_email));
                    error = true;
                }
                if (loginInputToken.getText().toString().length() != 8) {
                    loginInputToken.setError(getString(R.string.error_token_empty));
                    error = true;
                }
                if (loginInputNewPassword.getText().toString().length() < 5) {
                    loginInputNewPassword.setError(getString(R.string.error_password_length));
                    error = true;
                }
                if (loginInputNewPassword2.getText().toString().length() < 5) {
                    loginInputNewPassword2.setError(getString(R.string.error_password_length));
                    error = true;
                }

                if (!loginInputNewPassword.getText().toString().equals(loginInputNewPassword2.getText().toString())) {
                    loginInputNewPassword2.setError(getString(R.string.error_password_mismatch));
                    error = true;
                }
                if (!error) {
                    loginUser(loginInputEmail.getText().toString(),
                            loginInputNewPassword.getText().toString(), loginInputToken.getText().toString());
                }
            }
        });
    }

    private void forgotPassword(final String email) {
        String cancel_req_tag = "forgot";
        Log.d("GAIUS", "forgotPassword ");
        progressDialog.setMessage(getText(R.string.dialog_sending_token));
        showDialog();

        //hide keyboard
        InputMethodManager in = (InputMethodManager) getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(loginInputEmail.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        StringRequest strReq = new StringRequest(Request.Method.POST,
                URL_FOR_FORGOT_PASSWORD, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d("GAIUS", "Response: " + response);
                if (response.contains("Success")) {
                    Toast.makeText(getApplicationContext(), getText(R.string.token_sent), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(),
                            response.replace("@@ERROR##","ERROR:").trim() , Toast.LENGTH_LONG).show();
                }

                hideDialog();
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("GAIUS", "Reset Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);
                params.put("reset", "1");
                return params;
            }

        };
        strReq.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        AppSingleton.getInstance(getApplicationContext()).addToRequestQueue(strReq, cancel_req_tag);
    }

    private void loginUser(final String email, final String password, final String token) {
        // Tag used to cancel the request
        String cancel_req_tag = "login";
        progressDialog.setMessage(getString(R.string.dialog_login));
        showDialog();
        StringRequest strReq = new StringRequest(Request.Method.POST,
                URL_FOR_FORGOT_PASSWORD, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d("GAIUS", "Reset Response: " + response);
                hideDialog();

                if (response.contains("Success")) {
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    intent.putExtra("REF", getIntent().getIntExtra("REF", -1));
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(),
                            response.replace("@@ERROR##","ERROR:").trim() , Toast.LENGTH_LONG).show();
                }


            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("GAIUS", "Login Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);
                params.put("password", password);
                params.put("token", token);
                params.put("update", "1");
                return params;
            }

        };
        Log.d("GAIUS", "ResetPasswordActivity: send new passwort " + strReq);
        // Adding request to request queue
        AppSingleton.getInstance(getApplicationContext()).addToRequestQueue(strReq, cancel_req_tag);
    }

    private void showDialog() {
        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    private void hideDialog() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }
}



