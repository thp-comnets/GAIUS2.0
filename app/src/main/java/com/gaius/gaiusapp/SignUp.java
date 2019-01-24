package com.gaius.gaiusapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


import com.gaius.gaiusapp.utils.ResourceHelper;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadService;
import net.gotev.uploadservice.UploadStatusDelegate;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.gaius.gaiusapp.utils.ResourceHelper.getResizedBitmap;
import static net.gotev.uploadservice.Placeholders.ELAPSED_TIME;
import static net.gotev.uploadservice.Placeholders.PROGRESS;
import static net.gotev.uploadservice.Placeholders.TOTAL_FILES;
import static net.gotev.uploadservice.Placeholders.UPLOADED_FILES;
import static net.gotev.uploadservice.Placeholders.UPLOAD_RATE;

public class SignUp extends AppCompatActivity {
    private static final int PICK_ICON_REQUEST = 1;
    private static final int CROP_ICON_REQUEST = 2;

    private String filePath, URL_FOR_REGISTRATION;
    private Uri avatarUri;
    private ImageView avatarImageView, loadingImageView;
    private EditText signupInputName, signupInputEmail, signupInputPassword, signupInputPassword2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.signup_activity);
        super.onCreate(savedInstanceState);

        UploadService.NAMESPACE = BuildConfig.APPLICATION_ID;
        UploadService.NAMESPACE = "com.gaius.contentupload";

        URL_FOR_REGISTRATION = "http://91.230.41.34:8080/test/register.php";

        signupInputName = findViewById(R.id.name_edittext);
        signupInputEmail = findViewById(R.id.email_edittext);
        signupInputPassword = findViewById(R.id.password_edittext);
        signupInputPassword2 = findViewById(R.id.verify_password_edittext);

        Button customSignupButton = (Button) findViewById(R.id.custom_signup_button);
        customSignupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent i = new Intent(getApplicationContext(), MainActivity.class);
//
//                startActivity(i);
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
                "TBD",
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
            if (requestCode == CROP_ICON_REQUEST) {
                Bundle bundle = data.getExtras();
                Bitmap bitmap = bundle.getParcelable("data");
                bitmap = getResizedBitmap(bitmap,400);
                avatarImageView.setImageBitmap(bitmap);

                filePath = ResourceHelper.saveBitmapCompressed(getApplicationContext(), avatarUri, bitmap);

            }

            if (requestCode == PICK_ICON_REQUEST) {
                avatarUri = data.getData();

                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle("Cropping");
                builder.setMessage("Do you want to crop the image?");

                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing but close the dialog
                        dialog.dismiss();
                        ImageCropFunction();
                    }
                });

                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                        dialog.dismiss();
                        Bitmap bitmap = null;
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver() , avatarUri);
                            bitmap = getResizedBitmap(bitmap,400);
                            avatarImageView.setImageBitmap(bitmap);
                            filePath = ResourceHelper.saveBitmapCompressed(getApplicationContext(), avatarUri, bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
            }
        }
    }

    private void ImageCropFunction() {

        try {
            Intent CropIntent = new Intent("com.android.camera.action.CROP");

            CropIntent.setDataAndType(avatarUri, "image/*");

            CropIntent.putExtra("crop", "true");
            CropIntent.putExtra("outputX", 200);
            CropIntent.putExtra("outputY", 200);
            CropIntent.putExtra("aspectX", 1);
            CropIntent.putExtra("aspectY", 1);
            CropIntent.putExtra("scaleUpIfNeeded", true);
            CropIntent.putExtra("return-data", true);

            startActivityForResult(CropIntent, CROP_ICON_REQUEST);

        } catch (ActivityNotFoundException e) {

        }
    }

    private void registerUser(final String name, final String channel, final String email, final String password,
                              final String gender, final String dob, final boolean edit, final String phoneNumber) {

        Log.d("yasir", "RegisterActivity: register user: " + name + " " + channel + " " + email + " " + " " + URL_FOR_REGISTRATION);

        loadingImageView.setVisibility(View.VISIBLE);

        String uploadId = UUID.randomUUID().toString();

        try {

            MultipartUploadRequest request = new MultipartUploadRequest(getApplicationContext(), uploadId, URL_FOR_REGISTRATION)
                    .addParameter("name", name)
                    .addParameter("channel", channel)
                    .addParameter("phoneNumber", phoneNumber)
                    .addParameter("email", email)
                    .addParameter("password", password)
                    .addParameter("gender", gender)
                    .addParameter("age", dob)
                    .addParameter("type", "Register")
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
                                Log.d("yasir", "RegisterActivity: onError"+serverResponse.getHeaders() + serverResponse.getBodyAsString() + serverResponse.getHttpCode());

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            loadingImageView.setVisibility(View.GONE);

                            Toast.makeText(getApplicationContext(),
                                    serverResponse.getHttpCode(), Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {

                            // remove the notification as it is no longer needed to keep the service alive
                            if (uploadInfo.getNotificationID() != null) {
                                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                                notificationManager.cancel(uploadInfo.getNotificationID());
                            }
                            Log.d("yasir", "RegisterActivity: onCompleted response "+serverResponse.getBodyAsString());

                            JSONObject jObj = null;
                            try {
                                jObj = new JSONObject(serverResponse.getBodyAsString());
                                boolean error = jObj.getBoolean("error");

                                if (!error) {
                                    String user = jObj.getJSONObject("user").getString("name");
                                    Log.d("yasir","RegisterActivity: Received token: " + jObj.getJSONObject("user").getString("token"));


                                    Toast.makeText(getApplicationContext(), "Your account was successfully", Toast.LENGTH_SHORT).show();

                                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                                    SharedPreferences.Editor editor = prefs.edit();

                                    editor.putString("name", jObj.getJSONObject("user").getString("name"));
                                    editor.putString("email", jObj.getJSONObject("user").getString("email"));
                                    editor.putString("password", password);
                                    editor.putString("account_token", jObj.getJSONObject("user").getString("token"));
                                    editor.commit();

                                    finish();

                                } else {
                                    String errorMsg = jObj.getString("error_msg");
                                    Toast.makeText(getApplicationContext(),
                                            errorMsg, Toast.LENGTH_LONG).show();
                                    loadingImageView.setVisibility(View.GONE);
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onCancelled(Context context, UploadInfo uploadInfo) {
                            // your code here
                        }
                    });


            if (filePath != null) {
//                String iconPathNew = ResourceHelper.compressImage(getApplicationContext(), filePath, 80, 80);
                request.addFileToUpload(filePath, "avatar");
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("account_avatar", filePath);
                editor.commit();
//                ImageLoader.getInstance().clearDiskCache();
//                ImageLoader.getInstance().clearMemoryCache();

            }

            request.startUpload();

        } catch (Exception exc) {
            Log.d("yasir", exc.getMessage(), exc);
        }
    }

    private UploadNotificationConfig getNotificationConfig(final String uploadId, @StringRes int title) {
        UploadNotificationConfig config = new UploadNotificationConfig();

        PendingIntent clickIntent = PendingIntent.getActivity(
                this, 1, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

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
