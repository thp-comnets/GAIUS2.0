package com.gaius.gaiusapp;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
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
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.OkHttpResponseListener;
import com.gaius.gaiusapp.utils.ResourceHelper;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Response;

import static com.gaius.gaiusapp.utils.ResourceHelper.getResizedBitmap;


public class SignUpSMSActivity extends AppCompatActivity {
    private static final int PICK_ICON_REQUEST = 1;
    private static final int PICK_CHANNEL_REQUEST = 2;
    private String iconPath, channelThumbnailPath, URL_FOR_REGISTRATION, croppedImage;
    private Uri avatarUri, channelThumbnailUri;
    private ImageView avatarImageView, channelImageView;
    private EditText signupInputName, signupInputChannel;
    SharedPreferences prefs;
    Context mCtx;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.signup_activity_sms);
        super.onCreate(savedInstanceState);

        mCtx = this;

        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        URL_FOR_REGISTRATION = prefs.getString("base_url", null)+"OTP.py";

        signupInputName = findViewById(R.id.name_edittext);
        signupInputChannel = findViewById(R.id.channel_edittext);

        Button customSignupButton = (Button) findViewById(R.id.custom_signup_button);
        customSignupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitForm();
            }
        });

        avatarImageView = (ImageView) findViewById(R.id.logo);
        avatarImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                Permissions.check((Activity) mCtx, permissions, null, null, new PermissionHandler() {
                    @Override
                    public void onGranted() {
                        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(galleryIntent, PICK_ICON_REQUEST);
                    }

                    @Override
                    public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                        super.onDenied(context, deniedPermissions);
                        Toast.makeText((Activity) mCtx, "If you reject this permission, you can not use this functionality.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        channelImageView = (ImageView) findViewById(R.id.channel_thumbnail);
        channelImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                Permissions.check((Activity) mCtx, permissions, null, null, new PermissionHandler() {
                    @Override
                    public void onGranted() {
                        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(galleryIntent, PICK_CHANNEL_REQUEST);
                    }

                    @Override
                    public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                        super.onDenied(context, deniedPermissions);
                        Toast.makeText((Activity) mCtx, "If you reject this permission, you can not use this functionality.", Toast.LENGTH_SHORT).show();
                    }
                });
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

        if (signupInputChannel.getText().toString().matches("")) {
            signupInputChannel.setError("Channel can't be empty");
            error =true;
        }

        if (error) {
            return;
        }

        registerUser(signupInputName.getText().toString(),
                signupInputChannel.getText().toString()
              );
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_CHANNEL_REQUEST) {
                croppedImage = "channelThumbnailCropping";
                channelThumbnailUri = data.getData();

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

            if (requestCode == PICK_ICON_REQUEST) {
                croppedImage = "iconCropping";
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
                if (croppedImage.equals("iconCropping")) {
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    Bitmap bitmap = BitmapFactory.decodeFile(result.getUri().getPath());
                    bitmap = getResizedBitmap(bitmap, 200);

                    iconPath = ResourceHelper.saveBitmapCompressed(getApplicationContext(), avatarUri, bitmap);
                    avatarImageView.setImageBitmap(bitmap);
                }
                else if (croppedImage.equals("channelThumbnailCropping")) {
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    Bitmap bitmap = BitmapFactory.decodeFile(result.getUri().getPath());
                    bitmap = getResizedBitmap(bitmap, 1080);

                    channelThumbnailPath = ResourceHelper.saveBitmapCompressed(getApplicationContext(), channelThumbnailUri, bitmap);
                    channelImageView.setImageBitmap(bitmap);

                }
            }
        }
    }

    private void startCropImageActivity(Uri imageUri) {
        if (croppedImage.equals("iconCropping")) {
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }
        else {
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this);
        }
    }

    private void registerUser(final String name, final String channel) {

        dialog = ProgressDialog.show(this, "",
                "Loading. Please wait...", true);

        ANRequest.MultiPartBuilder request = new ANRequest.MultiPartBuilder(URL_FOR_REGISTRATION);

        if (iconPath != null) {
            request.addMultipartFile("avatar",new File (iconPath));
        }

        if (channelThumbnailPath != null) {
            request.addMultipartFile("channelThumbnail",new File (channelThumbnailPath));
        }

        request.addMultipartParameter("login", "1")
                .addMultipartParameter("numbe", prefs.getString("number", ""))
                .addMultipartParameter("OTP", prefs.getString("OTP", ""))
                .addMultipartParameter("name", name)
                .addMultipartParameter("channel", channel)
                .addMultipartParameter("password", "test")
                .setPriority(Priority.HIGH);

        ANRequest anRequest = request.build();

        anRequest.getAsOkHttpResponse(new OkHttpResponseListener() {
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
                            editor.putString("password", "test");
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
                            dialog.dismiss();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        dialog.dismiss();
                    } catch (IOException e) {
                        e.printStackTrace();
                        dialog.dismiss();
                    }
                } else {
                    dialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Something went wrong with the registration ("+ response.code()+")", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(ANError error) {

                dialog.dismiss();

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
        editor.putString("account_avatar", iconPath);
        editor.commit();
    }
}

