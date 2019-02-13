package com.gaius.gaiusapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ANRequest;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.AnalyticsListener;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.androidnetworking.interfaces.OkHttpResponseListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.gaius.gaiusapp.utils.AdBuilder;
import com.gaius.gaiusapp.utils.Constants;
import com.gaius.gaiusapp.utils.LogOut;
import com.gaius.gaiusapp.utils.ResourceHelper;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;
import okhttp3.Response;

public class AdCreationActivity extends AppCompatActivity {
    private ImageView imageViewAd, videoDummyViewAd;
    private EditText editTextAd;
    JzvdStd videoViewAd;

    private String imageFilePath = null;
    private String videoFilePath = null;

    private final int PICK_VIDEO_REQUEST = 1;
    private final int READ_EXTERNAL_STORAGE_PERMISSIONS_REQUEST_CODE = 2;

    List<String> spinnerArray = new ArrayList<>();
    ArrayList<String> hrefUrls;
    ArrayAdapter<String> adapter;
    private Spinner hrefSpinner;
    private ProgressDialog progress;
    SharedPreferences prefs;
    String adFilePath;
    private Uri mCropImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_create_ad);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        editTextAd = findViewById(R.id.editTextAd);

        imageViewAd = findViewById(R.id.imageView);
        imageViewAd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageBrowse();
            }
        });

        videoViewAd = findViewById(R.id.videoAdView);
        videoDummyViewAd = findViewById(R.id.video_dummy_View);
        videoDummyViewAd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                galleryIntent.setType("video/*");
                galleryIntent.putExtra(Intent.EXTRA_MIME_TYPES, new String[] { "video/*"});
                startActivityForResult(galleryIntent, PICK_VIDEO_REQUEST);
            }
        });

        hrefSpinner = (Spinner) findViewById(R.id.spinner);
        loadMyChannels();
    }

    public boolean onCreateOptionsMenu (Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.create_ads, menu);
        return true;
    }

    private void loadMyChannels () {
        AndroidNetworking.get(prefs.getString("base_url", null) + "listUserPages.py")
                .addQueryParameter("userID", prefs.getString("userID", "null"))
                .addQueryParameter("token", prefs.getString("token", "null"))
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONArray(new JSONArrayRequestListener() {
                    @Override
                    public void onResponse(JSONArray response) {

                        Log.d("thp", "Response as JSON " + response);

                        try {
                            JSONObject page;

                            hrefUrls = new ArrayList<>();
                            spinnerArray = new ArrayList<>();
                            spinnerArray.add("None");

                            //traversing through all the object
                            for (int i = 0; i < response.length(); i++) {
                                page = response.getJSONObject(i);
                                hrefUrls.add(page.getString("url"));
                                spinnerArray.add(page.getString("title"));
                            }

                            adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, spinnerArray);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            hrefSpinner.setAdapter(adapter);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d("thp","Json error "+e);
                        }

                    }
                    @Override
                    public void onError(ANError error) {

                        switch (error.getErrorCode()) {
                            case 401:
                                LogOut.logout(getApplicationContext());
                                Toast.makeText(getApplicationContext(), "You have logged in from another device. Please login again.",
                                        Toast.LENGTH_LONG).show();
                                Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                                startActivity(i);
                                finish();
                                break;
                            case 500:
                                Log.d("thp","Error 500"+error);
                                break;
                            default:
                                Log.d("thp","Error no Internet "+error);

                        }
                    }
                });
    }

    public boolean onOptionsItemSelected (MenuItem item) {

        String uploadId = UUID.randomUUID().toString();
        switch (item.getItemId()) {
            case R.id.action_publish:
                AdBuilder adBuilder = new AdBuilder();

                if (imageFilePath == null && !getIntent().getBooleanExtra("EDIT_MODE", false)) {
                    Toast.makeText (getApplicationContext(), "No image added", Toast.LENGTH_SHORT).show ();
                    return false;
                }

                if (editTextAd.getText().length() != 0) {
                    adBuilder.addText(editTextAd.getText().toString());
                } else {
                    Toast.makeText (getApplicationContext(), "No text added", Toast.LENGTH_SHORT).show ();
                    return false;
                }

                Log.d("thp", "ad creation " + adBuilder.getPageAsString());
                adFilePath = adBuilder.makeFile(Constants.TEMPDIR);
                uploadMultipart();

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void uploadMultipart() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());

        progress = new ProgressDialog(this);
        progress.setMessage("Uploading...");
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setCancelable(false);
        progress.setProgress(0);
        progress.setButton(ProgressDialog.BUTTON_NEUTRAL, "Cancel upload",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        AndroidNetworking.cancelAll();
                    }
                });
        progress.show();


        ANRequest.MultiPartBuilder multiPartBuilder = new ANRequest.MultiPartBuilder(prefs.getString("base_url", null) + "uploadADs.py");

        multiPartBuilder.addMultipartParameter("token", prefs.getString("token", "null"));

        if (imageFilePath != null) {
            String imagePathNew = ResourceHelper.compressImage(this, imageFilePath, 768, 1024);
            multiPartBuilder.addMultipartFile("image", new File(imagePathNew));
        }

        if (videoFilePath != null) {
            multiPartBuilder.addMultipartFile("video", new File(videoFilePath));
        }

        if (hrefSpinner.getSelectedItemPosition() != 0) {
            multiPartBuilder.addMultipartParameter("href", hrefUrls.get(hrefSpinner.getSelectedItemPosition()-1));
        }

        multiPartBuilder.addMultipartFile("ad_file", new File(adFilePath));

        multiPartBuilder.build()
                .setUploadProgressListener(new UploadProgressListener() {
                    @Override
                    public void onProgress(long bytesUploaded, long totalBytes) {
                        progress.setProgress((int)((float)bytesUploaded/totalBytes * 100.0));
                    }
                })
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
                            progress.dismiss();
                             uploadSuccessful("ad");
                        } else {
                            Toast.makeText(getApplicationContext(), "Something went wrong with the upload ("+ response.code()+")", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Toast.makeText(getApplicationContext(), "Something went wrong with the upload ("+ anError.getErrorDetail()+")", Toast.LENGTH_LONG).show();
                        progress.dismiss();
                    }
                });

    }

    private void uploadSuccessful(String contentType) {
        Log.d("thp", "upload successful");

        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Upload successful");
        alertDialog.setMessage("Your " + contentType + " has been successfully submitted. Someone from our team will approve it shortly.");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                finish();
            }
        });
        alertDialog.show();
        ResourceHelper.cleanupFiles();
    }

    @SuppressLint("NewApi")
    private void imageBrowse() {
        if (CropImage.isExplicitCameraPermissionRequired(this)) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE);
        } else {
            CropImage.startPickImageActivity(this);
        }
    }

    @Override
    @SuppressLint("NewApi")
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                Log.d("thp", "crop image activity request");
                imageViewAd.setImageURI(result.getUri());
                imageFilePath = result.getUri().getPath();
            }

            if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE ) {

                Uri imageUri = CropImage.getPickImageResultUri(this, data);

//                // For API >= 23 we need to check specifically that we have permissions to read external storage.
                if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {
                    // request permissions and handle the result in onRequestPermissionsResult()
                    mCropImageUri = imageUri;
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
                } else {
                    // no permissions required or already granted, can start crop image activity
                    startCropImageActivity(imageUri);
                }
            }


            if(requestCode == PICK_VIDEO_REQUEST){
                Uri videoFileUri = data.getData();
                String[] videoFile = getVideoPath(videoFileUri);
                videoFilePath = videoFile[0];
                Bitmap selectedImage = ThumbnailUtils.createVideoThumbnail(videoFile[0], MediaStore.Images.Thumbnails.MINI_KIND);
                long fileSize = Long.parseLong(videoFile[1])/1024/1024;
                if ( fileSize > 5) {
                    Toast.makeText (getApplicationContext(), "Video size is larger than " + fileSize +" MB. Consider uploading a smaller video!", Toast.LENGTH_SHORT).show ();
                }

                videoDummyViewAd.setVisibility(View.GONE);
                videoViewAd.setVisibility(View.VISIBLE);
                videoViewAd.setUp(videoFilePath, "", Jzvd.SCREEN_WINDOW_NORMAL);
                videoViewAd.thumbImageView.setImageBitmap(selectedImage);
            }
        }
    }

    private void startCropImageActivity(Uri imageUri) {
        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);
    }

    private String[] getVideoPath(Uri contentUri) {
        String[] proj = {MediaStore.Video.Media.DATA};
        CursorLoader loader = new CursorLoader(this, contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
        cursor.moveToFirst();
        String[] result = new String[2];
        result[0] = cursor.getString(column_index);


        String[] projSize = {MediaStore.Video.Media.SIZE};
        loader = new CursorLoader(this, contentUri, projSize, null, null, null);
        cursor = loader.loadInBackground();
        cursor.moveToFirst();

        int sizeColInd = cursor.getColumnIndex(projSize[0]);
        result[1] = ""+cursor.getLong(sizeColInd);
        cursor.close();
        return result;
    }


    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

//        if (requestCode == READ_EXTERNAL_STORAGE_PERMISSIONS_REQUEST_CODE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Log.d("Permission", "OnRequestResult granted");
//                Toast.makeText(this, "Required permissions are granted", Toast.LENGTH_LONG).show();
//            } else {
//                Log.d("Permission", "OnRequestResult not granted");
//                Toast.makeText(this, "Required permissions are not granted", Toast.LENGTH_LONG).show();
//                finish();
//            }
//        }

        if (requestCode == CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                CropImage.startPickImageActivity(this);
            } else {
                Toast.makeText(this, "Cancelling, required permissions are not granted", Toast.LENGTH_LONG).show();
            }
        }

        if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
            if (mCropImageUri != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // required permissions granted, start crop image activity
                startCropImageActivity(mCropImageUri);
            } else {
                Toast.makeText(this, "Cancelling, required permissions are not granted", Toast.LENGTH_LONG).show();
            }
        }
    }
}
