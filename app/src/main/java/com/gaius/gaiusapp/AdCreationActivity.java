package com.gaius.gaiusapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import net.gotev.uploadservice.UploadService;


public class AdCreationActivity extends AppCompatActivity {
    private ImageView imageViewAd;
    private EditText editTextAd;

    private String imageFilePath = null;
    private String videoFilePath = null;

    private Uri mCropImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UploadService.NAMESPACE = BuildConfig.APPLICATION_ID;
        UploadService.NAMESPACE = "com.gaius.adupload";

        setContentView(R.layout.activity_create_ad);

        imageViewAd = findViewById(R.id.imageView);
        imageViewAd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageBrowse();
            }
        });
    }

    public boolean onCreateOptionsMenu (Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.create_ads, menu);
        return true;
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


//            if(requestCode == PICK_VIDEO_REQUEST){
//                Uri videoFileUri = data.getData();
//                String[] videoFile = getVideoPath(videoFileUri);
//                videoFilePath = videoFile[0];
//                Bitmap selectedImage = ThumbnailUtils.createVideoThumbnail(videoFile[0], MediaStore.Images.Thumbnails.MINI_KIND);
//                long fileSize = Long.parseLong(videoFile[1])/1024/1024;
//                if ( fileSize > 5) {
//                    Toast.makeText (getApplicationContext(), "Video size is larger than " + fileSize +" MB. Consider uploading a smaller video!", Toast.LENGTH_SHORT).show ();
//                }
//                videoViewAdPlaceholder.setImageBitmap(selectedImage);
//                videoViewAdPlaceholder.getAdjustViewBounds();
//            }
        }
    }

    private void startCropImageActivity(Uri imageUri) {
        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);
    }
}
