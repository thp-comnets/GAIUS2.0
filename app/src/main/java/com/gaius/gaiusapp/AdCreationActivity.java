package com.gaius.gaiusapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
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
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.gaius.gaiusapp.adapters.NewsFeedAdapter;
import com.gaius.gaiusapp.classes.NewsFeed;
import com.gaius.gaiusapp.utils.AdBuilder;
import com.gaius.gaiusapp.utils.Constants;
import com.gaius.gaiusapp.utils.LogOut;
import com.gaius.gaiusapp.utils.ResourceHelper;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadService;
import net.gotev.uploadservice.UploadStatusDelegate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;

import static com.gaius.gaiusapp.utils.ResourceHelper.convertImageURLBasedonFidelity;


public class AdCreationActivity extends AppCompatActivity {
    private ImageView imageViewAd, videoDummyViewAd;
    private EditText editTextAd;
    JzvdStd videoViewAd;

    private String imageFilePath = null;
    private String videoFilePath = null;

    private Uri mCropImageUri;
    private final int PICK_VIDEO_REQUEST = 1;

    List<String> spinnerArray = new ArrayList<>();
    ArrayAdapter<String> adapter;
    private Spinner hrefSpinner;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UploadService.NAMESPACE = BuildConfig.APPLICATION_ID;
        UploadService.NAMESPACE = "com.gaius.adupload";

        setContentView(R.layout.activity_create_ad);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

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

    public boolean onCreateOptionsMenu (Menu menu)
    {
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

                            spinnerArray = new ArrayList<>();
                            spinnerArray.add("None");

                            //traversing through all the object
                            for (int i = 0; i < response.length(); i++) {
                                page = response.getJSONObject(i);
                                spinnerArray.add(page.getString("title"));
                            }

                            adapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_item, spinnerArray);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            hrefSpinner.setAdapter(adapter);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d("Yasir","Json error "+e);
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
                                Log.d("Yasir","Error 500"+error);
                                break;
                            default:
                                Log.d("Yasir","Error no Internet "+error);

                        }
                    }
                });
    }

    public boolean onOptionsItemSelected (MenuItem item)
    {

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
                uploadMultipart(getApplicationContext(), uploadId, imageFilePath, videoFilePath, adBuilder.makeFile(Constants.TEMPDIR));

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void uploadMultipart(final Context context, final String uploadId, final String imagePath, final String videoPath, final String adFilePath) {
        progressDialog.setMessage(getString(R.string.dialog_processing));
        showDialog();

        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
//            String channelName = prefs.getString("channel_name", "tmp");
            MultipartUploadRequest request = new MultipartUploadRequest(context, uploadId, BASE_URL + "uploadADs.py")
//                    .addParameter("ad_id_hash", uploadId)
                    .setNotificationConfig(getNotificationConfig(uploadId, R.string.notification_title))
                    .setMaxRetries(5)
                    .addParameter("token", prefs.getString("account_token", "null"))
                    .addFileToUpload(adFilePath, "ad_file")
                    .setDelegate(new UploadStatusDelegate() {
                        @Override
                        public void onProgress(Context context, UploadInfo uploadInfo) {
                            // your code here
                        }

                        @Override
                        public void onError(Context context, UploadInfo uploadInfo, ServerResponse serverResponse,
                                            Exception exception) {
                            try {
                                Log.d(Constants.TAG, "CreateAdActivity: onError"+serverResponse.getHeaders() + serverResponse.getBodyAsString() + serverResponse.getHttpCode());
                                Toast.makeText(getApplicationContext(), "Something went wrong with the upload ("+ serverResponse.getHttpCode()+")", Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
                            // remove the notification as it is no longer needed to keep the service alive
                            if (uploadInfo.getNotificationID() != null) {
                                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                                notificationManager.cancel(uploadInfo.getNotificationID());
                            }
                            Log.d(Constants.TAG, "CreateAdActivity: onCompleted response "+serverResponse.getBodyAsString());
                            if (serverResponse.getBodyAsString().contains("@@ERROR##"))  {
                                Toast.makeText(getApplicationContext(), serverResponse.getBodyAsString().replace("@@ERROR##","ERROR:").trim(), Toast.LENGTH_LONG).show();
                            } else {
                                uploadSuccessful();
                            }
                            hideDialog();
                        }

                        @Override
                        public void onCancelled(Context context, UploadInfo uploadInfo) {
                            hideDialog();
                        }
                    });

            if (imagePath != null) {
                String imagePathNew = ResourceHelper.compressImage(context, imagePath, 768, 1024);
                request.addFileToUpload(imagePathNew, "image");
            }

            if (videoPath != null) {
                request.addFileToUpload(videoPath, "video");
            }

            if (hrefSpinner.getSelectedItemPosition() != 0) {
                request.addParameter("href", ""+channels.get(hrefSpinner.getSelectedItemPosition()-1).getPageUrl());
            }

            request.startUpload();

            Log.d(Constants.TAG, "CreateAdActivity request " + uploadId);
        } catch (Exception exc) {
            Log.d("AndroidUploadService", exc.getMessage(), exc);
        }
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
}
