package com.gaius.gaiusapp;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.StringRes;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gaius.gaiusapp.utils.ResourceHelper;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadService;
import net.gotev.uploadservice.UploadStatusDelegate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import io.brotherjing.galleryview.GalleryView;

import static com.gaius.gaiusapp.utils.ResourceHelper.getResizedBitmap;
import static net.gotev.uploadservice.Placeholders.ELAPSED_TIME;
import static net.gotev.uploadservice.Placeholders.PROGRESS;
import static net.gotev.uploadservice.Placeholders.TOTAL_FILES;
import static net.gotev.uploadservice.Placeholders.UPLOADED_FILES;
import static net.gotev.uploadservice.Placeholders.UPLOAD_RATE;

public class uploadImagesActivity extends AppCompatActivity {

    private GalleryView galleryView;
    private TextView label;
    private final int PICK_IMAGE_MULTIPLE = 0;
    ArrayList<String> multiImageViewBitmaps;
    ArrayList<String> uploadImagesPath;
    private int currentImagePos;
    private UrlGalleryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UploadService.NAMESPACE = BuildConfig.APPLICATION_ID;
        UploadService.NAMESPACE = "com.gaius.contentupload";

        setContentView(R.layout.activity_upload_album);

        super.onCreate(savedInstanceState);

        ImageView deleteButton = findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                multiImageViewBitmaps.remove(currentImagePos-1);
                uploadImagesPath.remove(currentImagePos-1);

                if (uploadImagesPath.size() == 0) {
                    finish();
                }

                adapter = new UrlGalleryAdapter(getApplicationContext(),multiImageViewBitmaps);
                galleryView.setAdapter(adapter);
                label.setText((1)+"/"+galleryView.getAdapter().getCount());
                currentImagePos = 1;
            }
        });

        galleryView = (GalleryView)findViewById(R.id.gallery);
        label = (TextView)findViewById(R.id.tvLabel);

        galleryView.setScrollEndListener(new GalleryView.OnScrollEndListener() {
            @Override
            public void onScrollEnd(int index) {
                label.setText((index+1)+"/"+galleryView.getAdapter().getCount());
                currentImagePos = index+1;
            }
        });

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_MULTIPLE); //SELECT_PICTURES is simply a global int used to check the calling intent in onActivityResult

    }

    @Override
    @SuppressLint("NewApi")
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Intent intent;

        Log.d("yasir","Upload video2 ");

        if (resultCode == RESULT_OK) {

            if(requestCode == PICK_IMAGE_MULTIPLE) {
                Log.d("yasir","PICK_IMAGE_MULTIPLE");

                multiImageViewBitmaps = new ArrayList<>();
                uploadImagesPath = new ArrayList<>();

                if(data.getClipData() != null) {
                    int count = data.getClipData().getItemCount(); //evaluate the count before the for loop --- otherwise, the count is evaluated every loop.

                    for(int i = 0; i < count; i++) {
                        Uri imageUri = data.getClipData().getItemAt(i).getUri();

                        Bitmap bitmap = null;
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                            bitmap = getResizedBitmap(bitmap,800);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        String imagePath = ResourceHelper.saveBitmapCompressed(getApplicationContext(), imageUri, bitmap);
                        uploadImagesPath.add(imagePath);
                        multiImageViewBitmaps.add(imageUri.toString());
                    }
                }
                else if(data.getData() != null) {
                    Uri imageUri = data.getData();
                    uploadImagesPath.add(imageUri.getPath());
                    multiImageViewBitmaps.add(imageUri.toString());
                }

                adapter = new UrlGalleryAdapter(this,multiImageViewBitmaps);
                galleryView.setAdapter(adapter);
                label.setText((1)+"/"+galleryView.getAdapter().getCount());
            }
        }
    }

    public boolean onCreateOptionsMenu (Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.content_creator, menu);
        return true;
    }

    public boolean onOptionsItemSelected (MenuItem item)
    {
        if (filledFields()) {
            EditText editTextPagename = findViewById(R.id.images_album_title);
            EditText editTextDescription = findViewById(R.id.images_album_description);
            uploadMultipart(editTextPagename.getText().toString(), editTextDescription.getText().toString());
        }

        return true;
    }

    public boolean filledFields() {
        boolean haveContent = true;
        EditText editTextPagename = findViewById(R.id.images_album_title);
        EditText editTextDescription = findViewById(R.id.images_album_description);

        TextInputLayout editTextPagenameLayout = findViewById(R.id.images_album_title_layout) ;
        TextInputLayout editTextDescriptionLayout = findViewById(R.id.images_album_description_layout);

        if (editTextDescription.getText().length() == 0) {
            editTextDescriptionLayout.setError(getString(R.string.error_description));
            haveContent = false;
        } else {
            editTextDescriptionLayout.setErrorEnabled(false);
        }
        if (editTextPagename.getText().length() == 0) {
            editTextPagenameLayout.setError(getString(R.string.error_name));
            haveContent = false;
        } else {
            editTextPagenameLayout.setErrorEnabled(false);
        }

        if (!haveContent) {
            return false;
        }

        return true;
    }

    private void uploadMultipart(final String title, final String description) {

        String uploadId = UUID.randomUUID().toString();
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            MultipartUploadRequest request = new MultipartUploadRequest(this, uploadId, "http://91.230.41.34:8080/test/uploadImages.py")
                    .addParameter("token", prefs.getString("token", "null"))
                    .setUtf8Charset()
                    .setNotificationConfig(getNotificationConfig(uploadId, R.string.notification_title))
                    .setMaxRetries(5)
                    .setDelegate(new UploadStatusDelegate() {
                        @Override
                        public void onProgress(Context context, UploadInfo uploadInfo) {
                            // your code here
                        }

                        @Override
                        public void onError(Context context, UploadInfo uploadInfo, ServerResponse serverResponse, Exception exception) {
                            try {
                                Log.d("GAIUS", "ContentBuilderActivity: onError"+serverResponse.getHeaders() + serverResponse.getBodyAsString() + serverResponse.getHttpCode());
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
                            Log.d("GAIUS", "ContentBuilderActivity: onCompleted response "+serverResponse.getBodyAsString());
                            if (serverResponse.getBodyAsString().contains("@@ERROR##"))  {
                                Toast.makeText(getApplicationContext(), serverResponse.getBodyAsString().replace("@@ERROR##","ERROR:").trim(), Toast.LENGTH_LONG).show();
                            } else {
                                uploadSuccessful();
                            }
                        }

                        @Override
                        public void onCancelled(Context context, UploadInfo uploadInfo) {
                        }
                    });

            request.addParameter("title", title);
            request.addParameter("description", description);

            for (String imagePath: uploadImagesPath) {
                if (imagePath != null) {
                    Log.d("thp", "path to image " + imagePath);

                    String imagePathNew = ResourceHelper.compressImage(this, imagePath, 768, 1024);
                    request.addFileToUpload(imagePathNew, "images");
                }
            }

            Log.d("Yasir","Starting the upload 5");

            request.startUpload();
        } catch (Exception exc) {
            Log.d("yasir", exc.getMessage(), exc);
        }
    }

    private UploadNotificationConfig getNotificationConfig(final String uploadId, @StringRes int title) {
        UploadNotificationConfig config = new UploadNotificationConfig();

        PendingIntent clickIntent = PendingIntent.getActivity(
                this, 1, new Intent(this, uploadImagesActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

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

    private void uploadSuccessful() {
        Log.d("thp", "upload successful");
        Toast.makeText(this, "Your image album has been uploaded successfully", Toast.LENGTH_LONG).show();
        finish();
    }
}
