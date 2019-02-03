package com.gaius.gaiusapp;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
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
import android.widget.LinearLayout;
import android.widget.Toast;

import com.gaius.gaiusapp.adapters.ContentsCategoryAdapter;
import com.gaius.gaiusapp.classes.ContentCategory;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadService;
import net.gotev.uploadservice.UploadStatusDelegate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;

import static android.app.Activity.RESULT_OK;
import static net.gotev.uploadservice.Placeholders.ELAPSED_TIME;
import static net.gotev.uploadservice.Placeholders.PROGRESS;
import static net.gotev.uploadservice.Placeholders.TOTAL_FILES;
import static net.gotev.uploadservice.Placeholders.UPLOADED_FILES;
import static net.gotev.uploadservice.Placeholders.UPLOAD_RATE;

public class ContentFragment extends Fragment implements View.OnClickListener {
    List<ContentCategory> contentCategoryList;
    RecyclerView recyclerView;
    private final int PICK_IMAGE_MULTIPLE = 0;
    private final int PICK_VIDEO_REQUEST = 1;
    private String imageEncoded;
    private List<String> imagesEncodedList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_content, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        UploadService.NAMESPACE = BuildConfig.APPLICATION_ID;
        UploadService.NAMESPACE = "com.gaius.contentupload";

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
                final String filePath = videoFile[0];
                Bitmap selectedImage = ThumbnailUtils.createVideoThumbnail(videoFile[0], MediaStore.Images.Thumbnails.MINI_KIND);
                long fileSize = Long.parseLong(videoFile[1]) / 1024 / 1024;
                if (fileSize > 5) {
                    Toast.makeText(getContext(), "Video size is larger than " + fileSize + " MB. Consider uploading a smaller video!", Toast.LENGTH_SHORT).show();
                }
                Log.d("yasir","Upload video " + filePath + " " + selectedImage);

                LayoutInflater layoutInflater = LayoutInflater.from(getContext());
                final View promptView = layoutInflater.inflate(R.layout.upload_video_popup, null);
                final AlertDialog alertD = new AlertDialog.Builder(getContext()).create();
                alertD.setView(promptView);
                alertD.show();

                JzvdStd jzVideoPlayerStandard  = (JzvdStd) promptView.findViewById(R.id.video_view);
                jzVideoPlayerStandard.setUp(filePath, "", Jzvd.SCREEN_WINDOW_NORMAL);
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
                            EditText editTextPagename = promptView.findViewById(R.id.title_edittext);
                            EditText editTextDescription = promptView.findViewById(R.id.description_edittext);
                            alertD.dismiss();
                            uploadMultipart(getContext(), filePath, editTextPagename.getText().toString(), editTextDescription.getText().toString());                        }
                    }
                });
            }

//            if(requestCode == PICK_IMAGE_MULTIPLE) {
//                Log.d("yasir","PICK_IMAGE_MULTIPLE");
//
//                ArrayList<Uri> multiImageViewBitmaps;
//                multiImageViewBitmaps = new ArrayList<>();
//
//                if(resultCode == getActivity().RESULT_OK) {
//                    if(data.getClipData() != null) {
//                        int count = data.getClipData().getItemCount(); //evaluate the count before the for loop --- otherwise, the count is evaluated every loop.
//
//                        Log.d("yasir", "count: "+count);
//
//                        for(int i = 0; i < count; i++) {
//                            Uri imageUri = data.getClipData().getItemAt(i).getUri();
//                            multiImageViewBitmaps.add(imageUri);
//                            Log.d("yasir ", i+": "+imageUri+"");
//                            //do something with the image (save it to some directory or whatever you need to do with it here)
//                        }
//                    }
//                } else if(data.getData() != null) {
//                    String imagePath = data.getData().getPath();
//                    Log.d("yasir", imagePath+"");
//                    //do something with the image (save it to some directory or whatever you need to do with it here)
//                }
//
//                LayoutInflater layoutInflater = LayoutInflater.from(getContext());
//                final View promptView = layoutInflater.inflate(R.layout.upload_images_popup, null);
//                final AlertDialog alertD = new AlertDialog.Builder(getContext()).create();
//                alertD.setView(promptView);
//                alertD.show();
//
//                LinearLayout layout = (LinearLayout) promptView.findViewById(R.id.images_view);
//                for (int i=0; i<multiImageViewBitmaps.size(); i++) {
//                    ImageView image = new ImageView(promptView.getContext());
//                    image.setLayoutParams(new android.view.ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,500));
//                    image.setImageURI(multiImageViewBitmaps.get(i));
//                    layout.addView(image);
//                }
//
//                Button cancel_button = (Button) promptView.findViewById(R.id.cancel_button);
//                Button upload_button = (Button) promptView.findViewById(R.id.upload_button);
//
//                cancel_button.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        alertD.dismiss();
//                    }
//                });
//
//                upload_button.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        if (filledFields(promptView)) {
//                            EditText editTextPagename = promptView.findViewById(R.id.title_edittext);
//                            EditText editTextDescription = promptView.findViewById(R.id.description_edittext);
//                            alertD.dismiss();
////                            uploadMultipart(getContext(), filePath, editTextPagename.getText().toString(), editTextDescription.getText().toString());
//                        }
//                    }
//                });
//            }
        }
    }

    public boolean filledFields(View view) {
        boolean haveContent = true;
        EditText editTextPagename = view.findViewById(R.id.title_edittext);
        EditText editTextDescription = view.findViewById(R.id.description_edittext);
        TextInputLayout editTextPagenameLayout = view.findViewById(R.id.title_edittext_layout) ;
        TextInputLayout editTextDescriptionLayout = view.findViewById(R.id.description_edittext_layout);

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
//                alertD.dismiss();

//                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                intent.setType("image/*");
//                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
//                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_MULTIPLE); //SELECT_PICTURES is simply a global int used to check the calling intent in onActivityResult

                Intent i = new Intent(getContext(), uploadImagesActivity.class);
                getContext().startActivity(i);

            }
        });
        ad_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Yasir", "clicked on ad creation");
                alertD.dismiss();
// fixme: ad creation
//                Intent i = new Intent(getContext(), AdCreationActivity.class);
//                getContext().startActivity(i);
            }
        });
    }

    private void uploadMultipart(final Context context, final String videoPath, final String title, final String description) {

        String uploadId = UUID.randomUUID().toString();
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            MultipartUploadRequest request = new MultipartUploadRequest(context, uploadId, "http://91.230.41.34:8080/test/uploadVideo.py")
                    .addParameter("token", prefs.getString("token", "null"))
                    .setUtf8Charset()
                    .setNotificationConfig(getNotificationConfig(uploadId, R.string.notification_title))
                    .setMaxRetries(5)
                    .addFileToUpload(videoPath, "video")
                    .setDelegate(new UploadStatusDelegate() {
                        @Override
                        public void onProgress(Context context, UploadInfo uploadInfo) {
                            // your code here
                        }

                        @Override
                        public void onError(Context context, UploadInfo uploadInfo, ServerResponse serverResponse, Exception exception) {
                            try {
                                Log.d("GAIUS", "ContentBuilderActivity: onError"+serverResponse.getHeaders() + serverResponse.getBodyAsString() + serverResponse.getHttpCode());
                                Toast.makeText(getContext(), "Something went wrong with the upload ("+ serverResponse.getHttpCode()+")", Toast.LENGTH_LONG).show();
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
                                Toast.makeText(getContext(), serverResponse.getBodyAsString().replace("@@ERROR##","ERROR:").trim(), Toast.LENGTH_LONG).show();
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
            request.addParameter("category", "100");

            Log.d("Yasir","Starting the upload 5");

            request.startUpload();
        } catch (Exception exc) {
            Log.d("yasir", exc.getMessage(), exc);
        }
    }

    private UploadNotificationConfig getNotificationConfig(final String uploadId, @StringRes int title) {
        UploadNotificationConfig config = new UploadNotificationConfig();

        PendingIntent clickIntent = PendingIntent.getActivity(
                getContext(), 1, new Intent(getContext(), ContentFragment.class), PendingIntent.FLAG_UPDATE_CURRENT);

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
        Toast.makeText(getContext(), "Your video has been uploaded successfully", Toast.LENGTH_LONG).show();
    }
}

