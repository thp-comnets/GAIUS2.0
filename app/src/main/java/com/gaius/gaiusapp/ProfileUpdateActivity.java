package com.gaius.gaiusapp;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ANRequest;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.AnalyticsListener;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.androidnetworking.interfaces.OkHttpResponseListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.gaius.gaiusapp.networking.GlideApp;
import com.gaius.gaiusapp.utils.ResourceHelper;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import net.rimoto.intlphoneinput.IntlPhoneInput;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Response;

public class ProfileUpdateActivity extends AppCompatActivity {

    private static final int PICK_ICON_REQUEST = 1;
    private static final int CROP_ICON_REQUEST = 2;

    private TextInputEditText signupInputName, signupInputChannel, signupInputEmail, signupInputPassword, signupInputPassword2, signupInputAge;
    private RadioGroup genderRadioGroup;
    private ImageView avatarImageView;
    private Uri avatarUri;
    private String URL_FOR_REGISTRATION;
    private ProgressDialog progressDialog;
    private String filePath;
    private String myInternationalNumber=null;
    private IntlPhoneInput phoneInputView;
    private String base_url;
    private ProgressDialog progress;


    private static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_update);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        base_url = prefs.getString("base_url", null);
        URL_FOR_REGISTRATION = base_url + "updateUserInfo.py";

        // Progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        signupInputName = findViewById(R.id.signup_input_name);
        signupInputChannel = findViewById(R.id.signup_input_channel);
        signupInputEmail = findViewById(R.id.signup_input_email);
        signupInputAge = findViewById(R.id.signup_input_age);

        avatarImageView = findViewById(R.id.imageViewAvatar);

        phoneInputView = (IntlPhoneInput) findViewById(R.id.my_phone_input);

        Button btnSignUp = findViewById(R.id.btn_signup);

        genderRadioGroup = findViewById(R.id.gender_radio_group);

        if (prefs.getString("number","null").equals("-1")) {
            phoneInputView.setEnabled(true);
        }
        else {
            phoneInputView.setNumber("+" + prefs.getString("number", "null").substring(2));
            phoneInputView.setEnabled(false);
        }

        signupInputName.setText(prefs.getString("name", "null"));
        signupInputChannel.setText(prefs.getString("channel", "null"));
        signupInputEmail.setText(prefs.getString("email", "null"));
        signupInputEmail.setEnabled(false);

        if (prefs.getString("age", "null").equals("-1")) {
            signupInputAge.setText("");
        } else {
            signupInputAge.setText(prefs.getString("age", "null"));
        }

        GlideApp.with(getBaseContext())
                .load(base_url + "/content/usersIcons/"+prefs.getString("userID","None")+".png")
                .content()
                .into(avatarImageView);


        if (prefs.getString("gender", "null").equals("female")) {
            genderRadioGroup.check(R.id.female_radio_btn);
        } else if (prefs.getString("gender", "null").equals("male")) {
            genderRadioGroup.check(R.id.male_radio_btn);
        }

        TextView btnLinkForgotPassword = findViewById(R.id.link_forgot_password);
        btnLinkForgotPassword.setTextColor(Color.BLUE);
        btnLinkForgotPassword.setVisibility(View.VISIBLE);
        btnLinkForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), ForgotPassword.class);
                startActivity(i);
            }
        });

        avatarImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, PICK_ICON_REQUEST);
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitForm();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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

    private void submitForm() {

        boolean error = false;
        String age = "-1";
        if (signupInputName.getText().toString().matches("")) {
            signupInputName.setError(getString(R.string.error_name));
            error =true;
        }

        if (signupInputChannel.getText().toString().matches("")) {
            signupInputChannel.setError(getString(R.string.error_channel));
            error =true;
        }

        if (signupInputEmail.getText().toString().matches("")) {
            signupInputEmail.setError(getString(R.string.error_email));
            error =true;
        }

        if (!isEmailValid(signupInputEmail.getText().toString())) {
            signupInputEmail.setError(getString(R.string.error_email_format));
            error =true;
        }

        if (!signupInputAge.getText().toString().matches("")) {
            age = signupInputAge.getText().toString();
            if (Integer.parseInt(age) > 99 ) {
                signupInputAge.setError(getString(R.string.error_age));
                error = true;
            }
        }

        if(!phoneInputView.isValid()) {
//            phoneInputView.setError("Wrong phone number");
//            Toast.makeText(getApplicationContext(), "Invalid phone number please correct", Toast.LENGTH_SHORT).show();
//            Log.d("adam","Wrong phone number");
//            error = true;
            myInternationalNumber = "-1";
        }
        else {
            myInternationalNumber = "00"+phoneInputView.getNumber().substring(1);
        }

        if (error) {
            return;
        }

        int selectedId = genderRadioGroup.getCheckedRadioButtonId();
        String gender;
        if (selectedId == R.id.female_radio_btn)
            gender = "female";
        else if (selectedId == R.id.male_radio_btn)
            gender = "male";
        else gender = "none";

        udpateUser(signupInputName.getText().toString(),
                signupInputChannel.getText().toString(),
                signupInputEmail.getText().toString(),
                gender,
                age,
                myInternationalNumber);
    }


    private void udpateUser(final String name,  final String channel, final String email,
                            final String gender, final String dob, final String phoneNumber) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        final SharedPreferences.Editor editor = prefs.edit();
        if (filePath != null) {
            editor.putString("account_avatar", filePath);
            editor.commit();

            AndroidNetworking.upload(URL_FOR_REGISTRATION)
                    .addMultipartFile("avatar",new File (filePath))
                    .addMultipartParameter("token", prefs.getString("token", null))
                    .addMultipartParameter("name", name)
                    .addMultipartParameter("channel", channel)
                    .addMultipartParameter("phoneNumber", phoneNumber)
                    .addMultipartParameter("email", email)
                    .addMultipartParameter("gender", gender)
                    .addMultipartParameter("age", dob)
                    .setTag("uploadTest")
                    .setPriority(Priority.HIGH)
                    .build()
                    .getAsJSONArray(new JSONArrayRequestListener() {
                        @Override
                        public void onResponse(JSONArray response) {
                            try {
                                JSONObject user_info;
                                user_info = response.getJSONObject(0);

                                editor.putString("name", user_info.getString("name"));
                                editor.putString("email", user_info.getString("email"));
                                editor.putString("token", user_info.getString("token"));
                                editor.putString("channel", user_info.getString("channel"));
                                editor.putString("gender", user_info.getString("gender"));
                                editor.putString("age", user_info.getString("age"));
                                editor.putString("userID", user_info.getString("userID"));
                                editor.putString("number", user_info.getString("phoneNumber"));
                                editor.putString("admin", user_info.getString("admin"));
                                editor.commit();

                                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(i);

                                finish();

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(ANError error) {

                            switch (error.getErrorCode()) {
                                case 401:
                                    break;
                                case 500:
                                    Log.d("profile", "Error 500" + error);
                                    Toast.makeText(getApplicationContext(), "Error updating the profile info", Toast.LENGTH_SHORT).show();

                                    break;
                                default:
                                    Log.d("profile", "Error no Internet " + error);
                            }
                        }
                    });

        }
        else {
            AndroidNetworking.get(URL_FOR_REGISTRATION)
                    .addQueryParameter("token", prefs.getString("token", null))
                    .addQueryParameter("name", name)
                    .addQueryParameter("channel", channel)
                    .addQueryParameter("phoneNumber", phoneNumber)
                    .addQueryParameter("email", email)
                    .addQueryParameter("gender", gender)
                    .addQueryParameter("age", dob)
                    .setPriority(Priority.HIGH)
                    .build()
                    .getAsJSONArray(new JSONArrayRequestListener() {
                        @Override
                        public void onResponse(JSONArray response) {
                            try {
                                JSONObject user_info;
                                user_info = response.getJSONObject(0);

                                editor.putString("name", user_info.getString("name"));
                                editor.putString("email", user_info.getString("email"));
                                editor.putString("token", user_info.getString("token"));
                                editor.putString("channel", user_info.getString("channel"));
                                editor.putString("gender", user_info.getString("gender"));
                                editor.putString("age", user_info.getString("age"));
                                editor.putString("userID", user_info.getString("userID"));
                                editor.putString("number", user_info.getString("phoneNumber"));
                                editor.putString("admin", user_info.getString("admin"));
                                editor.commit();

                                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(i);

                                finish();

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(ANError error) {

                            switch (error.getErrorCode()) {
                                case 401:
                                    break;
                                case 500:
                                    Log.d("profile", "Error 500" + error);
                                    Toast.makeText(getApplicationContext(), "Error updating the profile info", Toast.LENGTH_SHORT).show();

                                    break;
                                default:
                                    Log.d("profile", "Error no Internet " + error);
                            }
                        }
                    });
        }
    }
}