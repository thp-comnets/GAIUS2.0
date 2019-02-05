package com.gaius.gaiusapp;


import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
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
import android.support.annotation.StringRes;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.gaius.gaiusapp.utils.Constants;
import com.gaius.gaiusapp.utils.ResourceHelper;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadService;
import net.gotev.uploadservice.UploadStatusDelegate;
import net.rimoto.intlphoneinput.IntlPhoneInput;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.gotev.uploadservice.Placeholders.ELAPSED_TIME;
import static net.gotev.uploadservice.Placeholders.PROGRESS;
import static net.gotev.uploadservice.Placeholders.TOTAL_FILES;
import static net.gotev.uploadservice.Placeholders.UPLOADED_FILES;
import static net.gotev.uploadservice.Placeholders.UPLOAD_RATE;

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


    private static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
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

        registerUser(signupInputName.getText().toString(),
                signupInputChannel.getText().toString(),
                signupInputEmail.getText().toString(),
                gender,
                age,
                myInternationalNumber);
    }

    private void registerUser(final String name,  final String channel, final String email,
                              final String gender, final String dob, final String phoneNumber) {

        progressDialog.setMessage(getString(R.string.dialog_processing));
        showDialog();

        String uploadId = UUID.randomUUID().toString();
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

            MultipartUploadRequest request = new MultipartUploadRequest(getApplicationContext(), uploadId, URL_FOR_REGISTRATION)
                    .addParameter("name", name)
                    .addParameter("channel", channel)
                    .addParameter("phoneNumber", phoneNumber)
                    .addParameter("email", email)
                    .addParameter("password", prefs.getString("password", "null"))
                    .addParameter("gender", gender)
                    .addParameter("age", dob)
                    .addParameter("type", "Update")
                    .setUtf8Charset()
                    .setNotificationConfig(getNotificationConfig(uploadId, R.string.notification_title))
                    .setMaxRetries(5)
                    .setDelegate(new UploadStatusDelegate() {
                        @Override
                        public void onProgress(Context context, UploadInfo uploadInfo) {
                            // your code here
                        }

                        @Override
                        public void onError(Context context, UploadInfo uploadInfo, ServerResponse serverResponse,
                                            Exception exception) {
                            try {
                                Log.d("GAIUS", "RegisterActivity: onError"+serverResponse.getHeaders() + serverResponse.getBodyAsString() + serverResponse.getHttpCode());

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            Toast.makeText(getApplicationContext(),
                                    serverResponse.getHttpCode(), Toast.LENGTH_LONG).show();
                            hideDialog();
                        }

                        @Override
                        public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {

                            // remove the notification as it is no longer needed to keep the service alive
                            if (uploadInfo.getNotificationID() != null) {
                                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                                notificationManager.cancel(uploadInfo.getNotificationID());
                            }
                            Log.d("GAIUS", "RegisterActivity: onCompleted response "+serverResponse.getBodyAsString());

                            JSONObject jObj = null;
                            try {
                                jObj = new JSONObject(serverResponse.getBodyAsString());
                                boolean error = jObj.getBoolean("error");

                                if (!error) {
                                    String user = jObj.getJSONObject("user").getString("name");
                                    Log.d("GAIUS","RegisterActivity: Received token: " + jObj.getJSONObject("user").getString("token"));

                                    Toast.makeText(getApplicationContext(), getString(R.string.toast_update_success), Toast.LENGTH_SHORT).show();

                                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                                    SharedPreferences.Editor editor = prefs.edit();

                                    editor.putString("name", jObj.getJSONObject("user").getString("name"));
                                    editor.putString("channel", jObj.getJSONObject("user").getString("channel"));
                                    editor.putString("email", jObj.getJSONObject("user").getString("email"));
                                    editor.putString("gender", jObj.getJSONObject("user").getString("gender"));
                                    editor.putString("age", jObj.getJSONObject("user").getString("age"));
                                    editor.putString("token", jObj.getJSONObject("user").getString("token"));
                                    editor.putString("userID", jObj.getJSONObject("user").getString("userID"));
                                    editor.putString("number", jObj.getJSONObject("user").getString("phoneNumber"));

                                    editor.commit();

                                    finish();

                                } else {

                                    String errorMsg = jObj.getString("error_msg");
                                    Toast.makeText(getApplicationContext(),
                                            errorMsg, Toast.LENGTH_LONG).show();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            hideDialog();
                        }

                        @Override
                        public void onCancelled(Context context, UploadInfo uploadInfo) {
                            // your code here
                            hideDialog();
                        }
                    });


            if (filePath != null) {
                request.addFileToUpload(filePath, "avatar");
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("avatar", filePath);
                editor.commit();
            }
            request.startUpload();

        } catch (Exception exc) {
            Log.d("GAIUS", exc.getMessage(), exc);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_update);

        UploadService.NAMESPACE = BuildConfig.APPLICATION_ID;
        UploadService.NAMESPACE = "com.gaius.contentupload";
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        base_url = prefs.getString("base_url", null);
        URL_FOR_REGISTRATION = base_url + "register.php";

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

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.error(R.drawable.ic_avatar);

        Glide.with(getBaseContext())
                .setDefaultRequestOptions(requestOptions)
                .load(base_url + "/content/usersIcons/"+prefs.getString("userID","None")+".png")
                .apply(new RequestOptions().signature(new ObjectKey(System.currentTimeMillis())))
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

    private void showDialog() {
        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    private void hideDialog() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }

    private UploadNotificationConfig getNotificationConfig(final String uploadId, @StringRes int title) {
        UploadNotificationConfig config = new UploadNotificationConfig();

        PendingIntent clickIntent = PendingIntent.getActivity(
                this, 1, new Intent(this, ProfileUpdateActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        config.setTitleForAllStatuses(getString(title))
                .setRingToneEnabled(false)
                .setClickIntentForAllStatuses(clickIntent)
                .setClearOnActionForAllStatuses(true);

        config.getProgress().message = "Uploaded " + UPLOADED_FILES + " of " + TOTAL_FILES
                + " at " + UPLOAD_RATE + " - " + PROGRESS;
        config.getProgress().iconResourceID = R.drawable.ic_upload;
        config.getProgress().iconColorResourceID = Color.BLUE;

        config.getCompleted().message = "Upload completed successfully in " + ELAPSED_TIME;
        config.getCompleted().iconResourceID = R.drawable.ic_upload_success;
        config.getCompleted().iconColorResourceID = Color.GREEN;

        config.getError().message = "Error while uploading";
        config.getError().iconResourceID = R.drawable.ic_upload_error;
        config.getError().iconColorResourceID = Color.RED;

        config.getCancelled().message = "Upload has been cancelled";
        config.getCancelled().iconResourceID = R.drawable.ic_cancelled;
        config.getCancelled().iconColorResourceID = Color.YELLOW;

        return config;
    }
}