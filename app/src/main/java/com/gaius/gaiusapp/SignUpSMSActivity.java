package com.gaius.gaiusapp;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.OkHttpResponseListener;
import com.gaius.gaiusapp.utils.ResourceHelper;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Response;


public class SignUpSMSActivity extends AppCompatActivity {
    private static final int PICK_ICON_REQUEST = 1;
    private String filePath, URL_FOR_REGISTRATION;
    private Uri avatarUri;
    private ImageView avatarImageView, loadingImageView;
    private EditText signupInputName, signupInputPassword, signupInputPassword2;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.signup_activity_sms);
        super.onCreate(savedInstanceState);

        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        URL_FOR_REGISTRATION = prefs.getString("base_url", null)+"OTP.py";

        signupInputName = findViewById(R.id.name_edittext);
        signupInputPassword = findViewById(R.id.password_edittext);
        signupInputPassword2 = findViewById(R.id.verify_password_edittext);

        Button customSignupButton = (Button) findViewById(R.id.custom_signup_button);
        customSignupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitForm();
            }
        });

        loadingImageView = (ImageView) findViewById(R.id.signup_animation);

        avatarImageView = (ImageView) findViewById(R.id.logo);
        avatarImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, PICK_ICON_REQUEST);
            }
        });
    }

    private static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private void submitForm() {

        boolean error = false;
        if (signupInputName.getText().toString().matches("")) {
            signupInputName.setError("Name can't be empty");
            error =true;
        }

        if (signupInputPassword.getText().toString().length() < 5) {
            signupInputPassword.setError("Password must be longer than 5 characters");
            error =true;
        }

        if (signupInputPassword2.getText().toString().length() < 5) {
            signupInputPassword2.setError("Password must be longer than 5 characters");
            error =true;
        }

        if (!signupInputPassword.getText().toString().equals(signupInputPassword2.getText().toString())) {
            signupInputPassword2.setError("Passwords don't match");
            error =true;
        }

        if (error) {
            return;
        }

        registerUser(signupInputName.getText().toString(),
                signupInputName.getText().toString()+"'s Channel",
                signupInputPassword.getText().toString()
              );
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_ICON_REQUEST) {
                avatarUri = data.getData();

                Uri imageUri = CropImage.getPickImageResultUri(this, data);

//                // For API >= 23 we need to check specifically that we have permissions to read external storage.
                if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {
                    // request permissions and handle the result in onRequestPermissionsResult()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
                    }

                } else {
                    // no permissions required or already granted, can start crop image activity
                    startCropImageActivity(imageUri);
                }
            }

            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                Bitmap bitmap = BitmapFactory.decodeFile(result.getUri().getPath());

                filePath = ResourceHelper.saveBitmapCompressed(getApplicationContext(), avatarUri, bitmap);
                avatarImageView.setImageBitmap(bitmap);
            }
        }
    }

    private void startCropImageActivity(Uri imageUri) {
        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(this);
    }

    private void registerUser(final String name, final String channel, final String password) {
        loadingImageView.setVisibility(View.VISIBLE);

        if (filePath != null) {
            AndroidNetworking.upload(URL_FOR_REGISTRATION)
                    .addMultipartFile("avatar",new File (filePath))
                    .addMultipartParameter("login", "1")
                    .addMultipartParameter("number", prefs.getString("number", ""))
                    .addMultipartParameter("OTP", prefs.getString("OTP", ""))
                    .addMultipartParameter("name", name)
                    .addMultipartParameter("channel", channel)
                    .addMultipartParameter("password", password)
                    .setTag("uploadTest")
                    .setPriority(Priority.HIGH)
                    .build()
                    .getAsOkHttpResponse(new OkHttpResponseListener() {
                        @Override
                        public void onResponse(Response response) {
                            if (response.code() == 200) {
                                JSONObject jObj = null;
                                try {
                                    jObj = new JSONObject(response.body().string());
                                    boolean error = jObj.getBoolean("error");

                                    if (!error) {
                                        String user = jObj.getJSONObject("user").getString("name");
                                        Log.d("yasir","RegisterActivity: Received token: " + jObj.getJSONObject("user").getString("token"));

                                        Toast.makeText(getApplicationContext(), "Your account was successfully", Toast.LENGTH_SHORT).show();

                                        SharedPreferences.Editor editor = prefs.edit();
                                        editor.putString("name", jObj.getJSONObject("user").getString("name"));
                                        editor.putString("email", jObj.getJSONObject("user").getString("email"));
                                        editor.putString("password", password);
                                        editor.putString("token", jObj.getJSONObject("user").getString("token"));
                                        editor.putString("channel", jObj.getJSONObject("user").getString("channel"));
                                        editor.putString("gender", jObj.getJSONObject("user").getString("gender"));
                                        editor.putString("age", jObj.getJSONObject("user").getString("age"));
                                        editor.putString("userID", jObj.getJSONObject("user").getString("userID"));
                                        editor.putString("number", jObj.getJSONObject("user").getString("phoneNumber"));
                                        editor.putString("admin", jObj.getJSONObject("user").getString("admin"));
                                        editor.commit();

                                        Intent i = new Intent(getApplicationContext(), MainActivity.class);
                                        startActivity(i);

                                        finish();

                                    } else {
                                        String errorMsg = jObj.getString("error_msg");
                                        Toast.makeText(getApplicationContext(),
                                                errorMsg, Toast.LENGTH_LONG).show();
                                        loadingImageView.setVisibility(View.GONE);
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "Something went wrong with the registration ("+ response.code()+")", Toast.LENGTH_LONG).show();
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

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("account_avatar", filePath);
            editor.commit();
        }
        else {
            AndroidNetworking.get(URL_FOR_REGISTRATION)
                    .addQueryParameter("login", "1")
                    .addQueryParameter("number", prefs.getString("number", ""))
                    .addQueryParameter("OTP", prefs.getString("OTP", ""))
                    .addQueryParameter("name", name)
                    .addQueryParameter("channel", channel)
                    .addQueryParameter("password", password)
                    .setPriority(Priority.HIGH)
                    .build()
                    .getAsOkHttpResponse(new OkHttpResponseListener() {
                        @Override
                        public void onResponse(Response response) {
                            if (response.code() == 200) {
                                JSONObject jObj = null;
                                try {
                                    jObj = new JSONObject(response.body().string());
                                    boolean error = jObj.getBoolean("error");

                                    if (!error) {
                                        String user = jObj.getJSONObject("user").getString("name");
                                        Log.d("yasir","RegisterActivity: Received token: " + jObj.getJSONObject("user").getString("token"));

                                        Toast.makeText(getApplicationContext(), "Your account was successfully", Toast.LENGTH_SHORT).show();

                                        SharedPreferences.Editor editor = prefs.edit();
                                        editor.putString("name", jObj.getJSONObject("user").getString("name"));
                                        editor.putString("email", jObj.getJSONObject("user").getString("email"));
                                        editor.putString("password", password);
                                        editor.putString("token", jObj.getJSONObject("user").getString("token"));
                                        editor.putString("channel", jObj.getJSONObject("user").getString("channel"));
                                        editor.putString("gender", jObj.getJSONObject("user").getString("gender"));
                                        editor.putString("age", jObj.getJSONObject("user").getString("age"));
                                        editor.putString("userID", jObj.getJSONObject("user").getString("userID"));
                                        editor.putString("number", jObj.getJSONObject("user").getString("phoneNumber"));
                                        editor.putString("admin", jObj.getJSONObject("user").getString("admin"));
                                        editor.commit();

                                        Intent i = new Intent(getApplicationContext(), MainActivity.class);
                                        startActivity(i);

                                        finish();

                                    } else {
                                        String errorMsg = jObj.getString("error_msg");
                                        Toast.makeText(getApplicationContext(),
                                                errorMsg, Toast.LENGTH_LONG).show();
                                        loadingImageView.setVisibility(View.GONE);
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "Something went wrong with the registration ("+ response.code()+")", Toast.LENGTH_LONG).show();
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

    }
}
