package com.gaius.gaiusapp;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ANRequest;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.AnalyticsListener;
import com.androidnetworking.interfaces.OkHttpResponseListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.gaius.gaiusapp.adapters.ContentsCategoryAdapter;
import com.gaius.gaiusapp.adapters.UrlGalleryAdapter;
import com.gaius.gaiusapp.classes.ContentCategory;
import com.gaius.gaiusapp.utils.ResourceHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;
import io.brotherjing.galleryview.GalleryView;
import okhttp3.Response;

import static android.app.Activity.RESULT_OK;
import static com.gaius.gaiusapp.utils.ResourceHelper.getResizedBitmap;

public class ContentFragment extends Fragment implements View.OnClickListener {
    int currentImagePos = 0;
    List<ContentCategory> contentCategoryList;
    RecyclerView recyclerView;
    private final int PICK_IMAGE_MULTIPLE = 0;
    private final int PICK_VIDEO_REQUEST = 1;
    ArrayList<String> multiImageViewBitmaps;
    ArrayList<String> uploadImagesPath;
    String uploadVideoPath;
    SharedPreferences prefs;
    UrlGalleryAdapter adapter;
    GalleryView galleryView=null;
    TextView label=null;
    EditText editTextPagename, editTextDescription;
    TextInputLayout editTextPagenameLayout, editTextDescriptionLayout;
    AlertDialog alertD;
    private ProgressDialog progress;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        return inflater.inflate(R.layout.fragment_content, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
//        UploadService.NAMESPACE = BuildConfig.APPLICATION_ID;
//        UploadService.NAMESPACE = "com.gaius.contentupload";

        recyclerView = getView().findViewById(R.id.recylcerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        contentCategoryList = new ArrayList<>();

        loadItems();
    }

    private void loadItems() {

        //adding the product to product list
        contentCategoryList.add(new ContentCategory(0, "Browse Web", R.drawable.ic_web_animation));
        contentCategoryList.add(new ContentCategory(1, "Browse Videos", R.drawable.ic_video_animation));
        contentCategoryList.add(new ContentCategory(2, "Browse Photos", R.drawable.ic_photos_animation));
        contentCategoryList.add(new ContentCategory(3, "Create Content", R.drawable.ic_create));
        contentCategoryList.add(new ContentCategory(4, "My Content", R.drawable.ic_my_content));

        if (prefs.getString("admin", "0").equals("1")) {
            contentCategoryList.add(new ContentCategory(5, "Approve Content", R.drawable.ic_content_curation));
        }

        //creating adapter object and setting it to recyclerview
        ContentsCategoryAdapter adapter = new ContentsCategoryAdapter(getContext(), contentCategoryList, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    @SuppressLint("NewApi")
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            if (requestCode == PICK_VIDEO_REQUEST) {
                Uri fileUri = data.getData();
                String[] videoFile = getVideoPath(fileUri);
                uploadVideoPath = videoFile[0];
                Bitmap selectedImage = ThumbnailUtils.createVideoThumbnail(videoFile[0], MediaStore.Images.Thumbnails.MINI_KIND);
                long fileSize = Long.parseLong(videoFile[1]) / 1024 / 1024;
                if (fileSize > 5) {
                    Toast.makeText(getContext(), "Video size is larger than " + fileSize + " MB. Consider uploading a smaller video!", Toast.LENGTH_SHORT).show();
                }
                Log.d("yasir","Upload video " + uploadVideoPath + " " + selectedImage);

                LayoutInflater layoutInflater = LayoutInflater.from(getContext());
                final View promptView = layoutInflater.inflate(R.layout.upload_video_popup, null);
                alertD = new AlertDialog.Builder(getContext()).create();
                alertD.setView(promptView);
                alertD.show();

                editTextPagename = promptView.findViewById(R.id.title_edittext);
                editTextDescription = promptView.findViewById(R.id.description_edittext);
                editTextPagenameLayout = promptView.findViewById(R.id.title_edittext_layout) ;
                editTextDescriptionLayout = promptView.findViewById(R.id.description_edittext_layout);


                JzvdStd jzVideoPlayerStandard  = (JzvdStd) promptView.findViewById(R.id.video_view);
                jzVideoPlayerStandard.setUp(uploadVideoPath, "", Jzvd.SCREEN_WINDOW_NORMAL);
                jzVideoPlayerStandard.thumbImageView.setImageBitmap(selectedImage);

                Button cancel_button = (Button) promptView.findViewById(R.id.cancel_button);
                Button upload_button = (Button) promptView.findViewById(R.id.upload_button);

                cancel_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertD.dismiss();
                    }
                });

                upload_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (filledFields(promptView)) {
                            alertD.hide();
//                            uploadMultipartVideo(getContext(), filePath, editTextPagename.getText().toString(), editTextDescription.getText().toString());
                            uploadMultipart(editTextPagename.getText().toString(), editTextDescription.getText().toString(), true);
                        }
                    }
                });
            }

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
                            bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), imageUri);
                            bitmap = getResizedBitmap(bitmap,800);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        String imagePath = ResourceHelper.saveBitmapCompressed(getContext(), imageUri, bitmap);
                        uploadImagesPath.add(imagePath);
                        multiImageViewBitmaps.add(imageUri.toString());
                    }
                }
                else if(data.getData() != null) {
                    Uri imageUri = data.getData();
                    Bitmap bitmap = null;
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), imageUri);
                        bitmap = getResizedBitmap(bitmap,800);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    String imagePath = ResourceHelper.saveBitmapCompressed(getContext(), imageUri, bitmap);

                    uploadImagesPath.add(imagePath);
                    multiImageViewBitmaps.add(imageUri.toString());
                }

                LayoutInflater layoutInflater = LayoutInflater.from(getContext());
                final View promptView = layoutInflater.inflate(R.layout.activity_upload_album, null);
                alertD = new AlertDialog.Builder(getContext()).create();
                alertD.setView(promptView);
                alertD.show();

                editTextPagename = promptView.findViewById(R.id.images_album_title);
                editTextDescription = promptView.findViewById(R.id.images_album_description);
                editTextPagenameLayout = promptView.findViewById(R.id.images_album_title_layout) ;
                editTextDescriptionLayout = promptView.findViewById(R.id.images_album_description_layout);

                currentImagePos = 1;
                galleryView = (GalleryView) promptView.findViewById(R.id.gallery);
                label = (TextView) promptView.findViewById(R.id.tvLabel);

                galleryView.setScrollEndListener(new GalleryView.OnScrollEndListener() {
                    @Override
                    public void onScrollEnd(int index) {
                        incrementCount(index);
                    }
                });

                adapter = new UrlGalleryAdapter(getContext(), multiImageViewBitmaps);
                galleryView.setAdapter(adapter);
                label.setText((1)+"/"+galleryView.getAdapter().getCount());

                ImageView deleteButton = promptView.findViewById(R.id.delete_button);
                deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        multiImageViewBitmaps.remove(currentImagePos-1);
                        uploadImagesPath.remove(currentImagePos-1);

                        if (uploadImagesPath.size() == 0) {
                            alertD.dismiss();
                        }

                        adapter = new UrlGalleryAdapter(getContext(),multiImageViewBitmaps);
                        galleryView.setAdapter(adapter);
                        label.setText((1)+"/"+galleryView.getAdapter().getCount());
                        currentImagePos = 1;
                    }
                });

                Button cancel_button = (Button) promptView.findViewById(R.id.cancel_button);
                Button upload_button = (Button) promptView.findViewById(R.id.upload_button);

                cancel_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertD.dismiss();
                    }
                });

                upload_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (filledFields(promptView)) {
                            alertD.hide();
                            uploadMultipart(editTextPagename.getText().toString(), editTextDescription.getText().toString(), false);
                        }
                    }
                });
            }
        }
    }

    public void incrementCount(int index) {
        label.setText((index+1)+"/"+galleryView.getAdapter().getCount());
        currentImagePos = index+1;
    }

    public boolean filledFields(View view) {
        boolean haveContent = true;

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

    private String[] getVideoPath(Uri contentUri) {
        String[] proj = {MediaStore.Video.Media.DATA};
        CursorLoader loader = new CursorLoader(getContext(), contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
        cursor.moveToFirst();
        String[] result = new String[2];
        result[0] = cursor.getString(column_index);


        String[] projSize = {MediaStore.Video.Media.SIZE};
        loader = new CursorLoader(getContext(), contentUri, projSize, null, null, null);
        cursor = loader.loadInBackground();
        cursor.moveToFirst();

        int sizeColInd = cursor.getColumnIndex(projSize[0]);
        result[1] = ""+cursor.getLong(sizeColInd);
        cursor.close();
        return result;
    }

    @Override
    public void onClick(View v) {
        ContentCategory c = contentCategoryList.get((Integer) v.getTag());

        if (c.getTitle().equals("Browse Web")) {
            Fragment fragment = new WebFragment();

            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
        else if (c.getTitle().equals("Browse Videos")) {
            Fragment fragment = new VideosFragment();

            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
        else if (c.getTitle().equals("Browse Photos")) {
            Fragment fragment = new PhotosFragment();

            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
        else if (c.getTitle().equals("Create Content")) {
            displayIntentOptions();
        }
        else if (c.getTitle().equals("My Content")) {
            Fragment fragment = new MyContentFragment();

            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
        else if (c.getTitle().equals("Approve Content")) {
            Fragment fragment = new ApproveContentFragment();

            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    private void displayIntentOptions(){

        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View promptView = layoutInflater.inflate(R.layout.create_content_popup, null);
        final AlertDialog alertD = new AlertDialog.Builder(getContext()).create();
        alertD.setView(promptView);
        alertD.show();

        CardView web_button = (CardView) promptView.findViewById(R.id.web_card);
        CardView video_button = (CardView) promptView.findViewById(R.id.video_card);
        CardView image_button = (CardView) promptView.findViewById(R.id.image_card);
        CardView ad_button = (CardView) promptView.findViewById(R.id.ad_card);

        web_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Yasir", "clicked on web creation");

                alertD.dismiss();

                LayoutInflater layoutInflater = LayoutInflater.from(getContext());
                View promptView = layoutInflater.inflate(R.layout.create_content_web_popup, null);
                final AlertDialog alertD2 = new AlertDialog.Builder(getContext()).create();
                alertD2.setView(promptView);
                alertD2.show();

                CardView simple_web_button = (CardView) promptView.findViewById(R.id.simple_web_card);
                CardView creative_web_button = (CardView) promptView.findViewById(R.id.creative_web_card);

                simple_web_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertD2.dismiss();
                        Intent i = new Intent(getContext(), SimpleWebCreation.class);
                        getContext().startActivity(i);
                    }
                });

                creative_web_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertD2.dismiss();
                        Intent i = new Intent(getContext(), CreativeWebCreation.class);
                        getContext().startActivity(i);
                    }
                });

            }
        });
        video_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Yasir", "clicked on video creation");
                alertD.dismiss();

                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                galleryIntent.setType("video/*");
                galleryIntent.putExtra(Intent.EXTRA_MIME_TYPES, new String[] { "video/*"});
                startActivityForResult(galleryIntent, PICK_VIDEO_REQUEST);
            }
        });
        image_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Yasir", "clicked on image creation");
                alertD.dismiss();

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_MULTIPLE); //SELECT_PICTURES is simply a global int used to check the calling intent in onActivityResult
            }
        });
        ad_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Yasir", "clicked on ad creation");
                alertD.dismiss();
                Intent i = new Intent(getContext(), AdCreationActivity.class);
                getContext().startActivity(i);
            }
        });
    }


    private void uploadMultipart(final String title, final String description, final boolean isVideo) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

        progress = new ProgressDialog(getActivity());
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

        String url = "";
        if (isVideo) {
            url = prefs.getString("base_url", null) + "uploadVideo.py";
        } else {
            url = prefs.getString("base_url", null) + "uploadImages.py";
        }

        ANRequest.MultiPartBuilder multiPartBuilder = new ANRequest.MultiPartBuilder(url);

        if (isVideo) {
            multiPartBuilder.addMultipartFile("video", new File(uploadVideoPath));
            multiPartBuilder.addMultipartParameter("category", "100");
        } else {
            for (String imagePath: uploadImagesPath) {
                if (imagePath != null) {
                    Log.d("thp", "path to image " + imagePath);

                    String imagePathNew = ResourceHelper.compressImage(getActivity().getApplicationContext(), imagePath, 768, 1024);
                    multiPartBuilder.addMultipartFile("images", new File(imagePathNew));

                }
            }
        }

        multiPartBuilder.addMultipartParameter("token", prefs.getString("token", "null"));
        multiPartBuilder.addMultipartParameter("title", title);
        multiPartBuilder.addMultipartParameter("description", description);
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
                            alertD.dismiss();
                            if (isVideo) {
                                uploadSuccessful("video");
                            } else {
                                uploadSuccessful("image album");
                            }

                        } else {
                            Toast.makeText(getContext(), "Something went wrong with the upload ("+ response.code()+")", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Toast.makeText(getContext(), "Something went wrong with the upload ("+ anError.getErrorDetail()+")", Toast.LENGTH_LONG).show();
                        progress.dismiss();
                        alertD.show();
                    }
                });

    }
//    private void uploadMultipartImagesOld(final String title, final String description) {
//
//        String uploadId = UUID.randomUUID().toString();
//        try {
//            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
//            MultipartUploadRequest request = new MultipartUploadRequest(getContext(), uploadId, prefs.getString("base_url", null) + "uploadImages.py")
//                    .addParameter("token", prefs.getString("token", "null"))
//                    .setUtf8Charset()
//                    .setNotificationConfig(getNotificationConfig(uploadId, R.string.notification_title))
//                    .setMaxRetries(5)
//                    .setDelegate(new UploadStatusDelegate() {
//                        @Override
//                        public void onProgress(Context context, UploadInfo uploadInfo) {
//                            // your code here
//                        }
//
//                        @Override
//                        public void onError(Context context, UploadInfo uploadInfo, ServerResponse serverResponse, Exception exception) {
//                            try {
//                                Log.d("GAIUS", "ContentBuilderActivity: onError"+serverResponse.getHeaders() + serverResponse.getBodyAsString() + serverResponse.getHttpCode());
//                                Toast.makeText(getContext(), "Something went wrong with the upload ("+ serverResponse.getHttpCode()+")", Toast.LENGTH_LONG).show();
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        }
//
//                        @Override
//                        public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
//                            // remove the notification as it is no longer needed to keep the service alive
//                            if (uploadInfo.getNotificationID() != null) {
//                                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//                                notificationManager.cancel(uploadInfo.getNotificationID());
//                            }
//                            Log.d("GAIUS", "ContentBuilderActivity: onCompleted response "+serverResponse.getBodyAsString());
//                            if (serverResponse.getBodyAsString().contains("@@ERROR##"))  {
//                                Toast.makeText(getContext(), serverResponse.getBodyAsString().replace("@@ERROR##","ERROR:").trim(), Toast.LENGTH_LONG).show();
//                            } else {
//                                uploadSuccessful("image album");
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(Context context, UploadInfo uploadInfo) {
//                        }
//                    });
//
//            request.addParameter("title", title);
//            request.addParameter("description", description);
//
//            for (String imagePath: uploadImagesPath) {
//                if (imagePath != null) {
//                    Log.d("thp", "path to image " + imagePath);
//
//                    String imagePathNew = ResourceHelper.compressImage(getContext(), imagePath, 768, 1024);
//                    request.addFileToUpload(imagePathNew, "images");
//                }
//            }
//
//            Log.d("Yasir","Starting the upload 5");
//
//            request.startUpload();
//        } catch (Exception exc) {
//            Log.d("yasir", exc.getMessage(), exc);
//        }
//    }

//    private void uploadMultipartVideo(final Context context, final String videoPath, final String title, final String description) {
//
//        String uploadId = UUID.randomUUID().toString();
//        try {
//            MultipartUploadRequest request = new MultipartUploadRequest(context, uploadId, prefs.getString("base_url", null)+"uploadVideo.py")
//                    .addParameter("token", prefs.getString("token", "null"))
//                    .setUtf8Charset()
//                    .setNotificationConfig(getNotificationConfig(uploadId, R.string.notification_title))
//                    .setMaxRetries(5)
//                    .addFileToUpload(videoPath, "video")
//                    .setDelegate(new UploadStatusDelegate() {
//                        @Override
//                        public void onProgress(Context context, UploadInfo uploadInfo) {
//                            // your code here
//                        }
//
//                        @Override
//                        public void onError(Context context, UploadInfo uploadInfo, ServerResponse serverResponse, Exception exception) {
//                            try {
//                                Log.d("GAIUS", "ContentBuilderActivity: onError"+serverResponse.getHeaders() + serverResponse.getBodyAsString() + serverResponse.getHttpCode());
//                                Toast.makeText(getContext(), "Something went wrong with the upload ("+ serverResponse.getHttpCode()+")", Toast.LENGTH_LONG).show();
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        }
//
//                        @Override
//                        public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
//                            // remove the notification as it is no longer needed to keep the service alive
//                            if (uploadInfo.getNotificationID() != null) {
//                                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//                                notificationManager.cancel(uploadInfo.getNotificationID());
//                            }
//                            Log.d("GAIUS", "ContentBuilderActivity: onCompleted response "+serverResponse.getBodyAsString());
//                            if (serverResponse.getBodyAsString().contains("@@ERROR##"))  {
//                                Toast.makeText(getContext(), serverResponse.getBodyAsString().replace("@@ERROR##","ERROR:").trim(), Toast.LENGTH_LONG).show();
//                            } else {
//                                uploadSuccessful("video");
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(Context context, UploadInfo uploadInfo) {
//                        }
//                    });
//
//            request.addParameter("title", title);
//            request.addParameter("description", description);
//            request.addParameter("category", "100");
//
//            Log.d("Yasir","Starting the upload 5");
//
//            request.startUpload();
//        } catch (Exception exc) {
//            Log.d("yasir", exc.getMessage(), exc);
//        }
//    }

//    private UploadNotificationConfig getNotificationConfig(final String uploadId, @StringRes int title) {
//        UploadNotificationConfig config = new UploadNotificationConfig();
//
//        PendingIntent clickIntent = PendingIntent.getActivity(
//                getContext(), 1, new Intent(getContext(), ContentFragment.class), PendingIntent.FLAG_UPDATE_CURRENT);
//
//        config.setTitleForAllStatuses(getString(title))
//                .setRingToneEnabled(false)
//                .setClickIntentForAllStatuses(clickIntent)
//                .setClearOnActionForAllStatuses(true);
//
//        config.getProgress().message = "Uploaded " + UPLOADED_FILES + " of " + TOTAL_FILES
//                + " at " + UPLOAD_RATE + " - " + PROGRESS;
//        config.getProgress().iconResourceID = R.drawable.ic_upload;
//        config.getProgress().iconColorResourceID = Color.BLUE;
//
//        config.getCompleted().message = "Upload completed successfully in " + ELAPSED_TIME;
//        config.getCompleted().iconResourceID = R.drawable.ic_upload_success;
//        config.getCompleted().iconColorResourceID = Color.GREEN;
//
//        config.getError().message = "Error while uploading";
//        config.getError().iconResourceID = R.drawable.ic_upload_error;
//        config.getError().iconColorResourceID = Color.RED;
//
//        config.getCancelled().message = "Upload has been cancelled";
//        config.getCancelled().iconResourceID = R.drawable.ic_cancelled;
//        config.getCancelled().iconColorResourceID = Color.YELLOW;
//
//        return config;
//    }

    private void uploadSuccessful(String contentType) {
        Log.d("thp", "upload successful");

        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setTitle("Upload successful");
        alertDialog.setMessage("Your " + contentType + " has been successfully submitted. Someone from our team will approve it shortly.");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
        ResourceHelper.cleanupFiles();
    }
}

