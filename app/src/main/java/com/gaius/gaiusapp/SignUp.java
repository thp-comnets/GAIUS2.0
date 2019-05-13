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

import com.androidnetworking.common.ANRequest;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.AnalyticsListener;
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


public class SignUp extends AppCompatActivity {
    private static final int PICK_ICON_REQUEST = 1;
    private String filePath, URL_FOR_REGISTRATION;
    private Uri avatarUri;
    private ImageView avatarImageView, loadingImageView;
    private EditText signupInputName, signupInputEmail, signupInputPassword, signupInputPassword2;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.signup_activity);
        super.onCreate(savedInstanceState);

        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        URL_FOR_REGISTRATION = prefs.getString("base_url", null)+"register.php";

        signupInputName = findViewById(R.id.name_edittext);
        signupInputEmail = findViewById(R.id.email_edittext);
        signupInputPassword = findViewById(R.id.password_edittext);
        signupInputPassword2 = findViewById(R.id.verify_password_edittext);

        Button customSignupButton = (Button) findViewById(R.id.custom_signup_button);
        customSignupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitForm();
            }
        });

//        loadingImageView = (ImageView) findViewById(R.id.signup_animation);

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

        if (signupInputEmail.getText().toString().matches("")) {
            signupInputEmail.setError("Email can't be empty");
            error =true;
        }

        if (!isEmailValid(signupInputEmail.getText().toString())) {
            signupInputEmail.setError("Email has invalid format");
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
                signupInputEmail.getText().toString(),
                signupInputPassword.getText().toString(),
                "none",
                "-1",
                false,
                "00");
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

    private void registerUser(final String name, final String channel, final String email, final String password,
                              final String gender, final String dob, final boolean edit, final String phoneNumber) {

        Log.d("yasir", "RegisterActivity: register user: " + name + " " + channel + " " + email + " " + " " + URL_FOR_REGISTRATION);

//        loadingImageView.setVisibility(View.VISIBLE);

        ANRequest.MultiPartBuilder multiPartBuilder = new ANRequest.MultiPartBuilder(URL_FOR_REGISTRATION);

        if (filePath != null) {
            multiPartBuilder.addMultipartFile("avatar", new File (filePath));
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("account_avatar", filePath);
            editor.commit();
        }

        multiPartBuilder.addMultipartParameter("name", name);
        multiPartBuilder.addMultipartParameter("channel", channel);
        multiPartBuilder.addMultipartParameter("phoneNumber", phoneNumber);
        multiPartBuilder.addMultipartParameter("email", email);
        multiPartBuilder.addMultipartParameter("password", password);
        multiPartBuilder.addMultipartParameter("gender", gender);
        multiPartBuilder.addMultipartParameter("age", dob);
        multiPartBuilder.addMultipartParameter("type", "Register");

        multiPartBuilder.build()
                .setAnalyticsListener(new AnalyticsListener() {
                    @Override
                    public void onReceived(long timeTakenInMillis, long bytesSent,
                                           long bytesReceived, boolean isFromCache) {
                        Log.d("thp", " timeTakenInMillis : " + timeTakenInMillis);
                        Log.d("thp", " bytesSent : " + bytesSent);
                        Log.d("thp", " bytesReceived : " + bytesReceived);
                        Log.d("thp", " isFromCache : " + isFromCache);
                    }
                })
                .getAsOkHttpResponse(new OkHttpResponseListener() {
                    @Override
                    public void onResponse(Response response) {
                        Log.d("thp", "OnResponse " + response.code());
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
                                    editor.commit();

                                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(i);

                                    finish();

                                } else {
                                    String errorMsg = jObj.getString("error_msg");
                                    Toast.makeText(getApplicationContext(),
                                            errorMsg, Toast.LENGTH_LONG).show();
//                                    loadingImageView.setVisibility(View.GONE);
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
                    public void onError(ANError anError) {
//                        loadingImageView.setVisibility(View.GONE);

                        Toast.makeText(getApplicationContext(),
                                anError.getErrorDetail(), Toast.LENGTH_LONG).show();
                    }
                });


    }
}

