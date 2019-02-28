package com.gaius.gaiusapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ANRequest;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.AnalyticsListener;
import com.androidnetworking.interfaces.OkHttpResponseListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.example.videocompressionlibrary.VideoCompress;
import com.gaius.gaiusapp.adapters.ItemsAdapter;
import com.gaius.gaiusapp.classes.Item;
import com.gaius.gaiusapp.helper.OnStartDragListener;
import com.gaius.gaiusapp.helper.SimpleItemTouchHelperCallback;
import com.gaius.gaiusapp.utils.Constants;
import com.gaius.gaiusapp.utils.MamlPageBuilder;
import com.gaius.gaiusapp.utils.ResourceHelper;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Response;

import static com.gaius.gaiusapp.utils.ResourceHelper.getResizedBitmap;

public class SimpleWebCreation extends AppCompatActivity implements OnStartDragListener {
    List<Item> itemList;
    RecyclerView recyclerView;
    CardView imageButton, videoButton, textHeaderButton, textParagrahButton;
    ItemsAdapter adapter;
    private int itemsCnt = 0;
    private String pageName;
    private String pageDescription;
    private final int PICK_ICON_REQUEST = 0;
    private final int PICK_IMAGE_REQUEST = 1;
    private final int PICK_VIDEO_REQUEST = 2;
    private ItemTouchHelper mItemTouchHelper;
    private ImageView iconImageView;
    private Uri iconUri;
    private String iconPath;
    private ProgressDialog progress;
    AlertDialog alertD;
    ArrayList<String> imagePaths, videoPaths, convertedVideoPaths;
    int numVideos;
    String mamlFilePath;
    private boolean doubleBackToExitPressedOnce = false;
    VideoCompress.VideoCompressTask compressTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_content_creation);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        recyclerView = findViewById(R.id.simple_recylcerView);
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        itemList = new ArrayList<>();

        //adding the product to product list
        itemList.add(new Item(itemsCnt, "text", "paragraph", null, null));
        itemsCnt++;

        textHeaderButton = findViewById(R.id.text_header_card);
        textHeaderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("yasir", "adding text at " + itemList.size());

                itemList.add(itemList.size(), new Item(itemsCnt, "text", "header", null, null));
                itemsCnt++;
                adapter.notifyItemInserted(itemList.size() - 1);
                recyclerView.scrollToPosition(itemList.size() - 1);
            }
        });

        textParagrahButton = findViewById(R.id.text_paragraph_card);
        textParagrahButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("yasir", "adding text2 at " + itemList.size());

                itemList.add(itemList.size(), new Item(itemsCnt, "text", "paragraph", null, null));
                itemsCnt++;
                adapter.notifyItemInserted(itemList.size() - 1);
                recyclerView.scrollToPosition(itemList.size() - 1);
            }
        });

        imageButton = findViewById(R.id.image_card);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });

        videoButton = findViewById(R.id.video_card);
        videoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                galleryIntent.setType("video/*");
                galleryIntent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"video/*"});
                startActivityForResult(galleryIntent, PICK_VIDEO_REQUEST);
            }
        });

        //creating adapter object and setting it to recyclerview
        adapter = new ItemsAdapter(this, itemList);
        recyclerView.setAdapter(adapter);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);

    }

    @Override
    @SuppressLint("NewApi")
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("yasir", "Upload video2 ");

        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST) {
//                Uri imageUri = data.getData();
//
//                Bitmap bitmap = null;
//                try {
//                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
//                    bitmap = getResizedBitmap(bitmap, 800);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//                String imagePath = ResourceHelper.saveBitmapCompressed(getApplicationContext(), imageUri, bitmap);
//
//                itemList.add(itemList.size(), new Item(itemsCnt, "image", null, imagePath, null));
//                itemsCnt++;
//                adapter.notifyItemInserted(itemList.size() - 1);
//                recyclerView.scrollToPosition(itemList.size() - 1);
//                recyclerView.invalidate();

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

                        itemList.add(itemList.size(), new Item(itemsCnt, "image", null, imagePath, null));
                        itemsCnt++;
                        adapter.notifyItemInserted(itemList.size() - 1);
                        recyclerView.scrollToPosition(itemList.size() - 1);
                        recyclerView.invalidate();
                    }
                }
                else if(data.getData() != null) {
                    Uri imageUri = data.getData();
                    Bitmap bitmap = null;
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                        bitmap = getResizedBitmap(bitmap,800);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    String imagePath = ResourceHelper.saveBitmapCompressed(getApplicationContext(), imageUri, bitmap);

                    itemList.add(itemList.size(), new Item(itemsCnt, "image", null, imagePath, null));
                    itemsCnt++;
                    adapter.notifyItemInserted(itemList.size() - 1);
                    recyclerView.scrollToPosition(itemList.size() - 1);
                    recyclerView.invalidate();
                }

            } else if (requestCode == PICK_VIDEO_REQUEST) {
                Uri fileUri = data.getData();
                String[] videoFile = getVideoPath(fileUri);
                final String filePath = videoFile[0];

                Log.d("yasir", "video: " + filePath);

                long fileSize = Long.parseLong(videoFile[1]) / 1024 / 1024;
                if (fileSize > 5) {
                    Toast.makeText(this, "Video size is larger than " + fileSize + " MB. Consider uploading a smaller video!", Toast.LENGTH_SHORT).show();
                }

                itemList.add(itemList.size(), new Item(itemsCnt, "video", null, null, filePath));
                itemsCnt++;
                adapter.notifyItemInserted(itemList.size() - 1);
                recyclerView.scrollToPosition(itemList.size() - 1);
                recyclerView.invalidate();
            } else if (requestCode == PICK_ICON_REQUEST) {
                iconUri = data.getData();

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
            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                Bitmap bitmap = BitmapFactory.decodeFile(result.getUri().getPath());
                bitmap = getResizedBitmap(bitmap, 200);

                iconPath = ResourceHelper.saveBitmapCompressed(getApplicationContext(), iconUri, bitmap);
                iconImageView.setImageBitmap(bitmap);
            }
        }
    }

    private void startCropImageActivity(Uri imageUri) {
        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setFixAspectRatio(true)
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
        result[1] = "" + cursor.getLong(sizeColInd);
        cursor.close();
        return result;
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.content_creator, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        recyclerView.clearFocus(); //clear focus to make sure text is saved
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        final View promptView = layoutInflater.inflate(R.layout.submit_content_popup, null);
        alertD = new AlertDialog.Builder(this).create();
        alertD.setView(promptView);
        alertD.show();

        Button cancel_button = (Button) promptView.findViewById(R.id.cancel_button);
        Button save_button = (Button) promptView.findViewById(R.id.save_button);
        Button publish_button = (Button) promptView.findViewById(R.id.publish_button);
        iconImageView = promptView.findViewById(R.id.logo);

        iconImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, PICK_ICON_REQUEST);
            }
        });

        cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertD.dismiss();
            }
        });

        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (filledFields(promptView)) {
                    alertD.hide();
                    submitPage(false);
                }
            }
        });

        publish_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (filledFields(promptView)) {
                    alertD.hide();
                    submitPage(true);
                }
            }
        });

        return false;
    }

    public boolean filledFields(View view) {
        boolean haveContent = true;
        EditText editTextPagename = view.findViewById(R.id.title_edittext);
        EditText editTextDescription = view.findViewById(R.id.description_edittext);
        TextInputLayout editTextPagenameLayout = view.findViewById(R.id.title_edittext_layout);
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

        pageName = editTextPagename.getText().toString();
        pageDescription = editTextDescription.getText().toString();
        hideKeyboard(editTextPagename);
        hideKeyboard(editTextDescription);
        return true;
    }

    public void hideKeyboard(View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public int convertToPixles(int value) {
        Resources r = getResources();
        float px = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                r.getDisplayMetrics()
        );
        return (int) px;
    }

    public void submitPage(boolean publish) {

        MamlPageBuilder builder = new MamlPageBuilder();

        imagePaths = new ArrayList<String>();
        videoPaths = new ArrayList<String>();

        builder.addBackground(String.format("#%06X", (0xFFFFFF & Color.WHITE)));
        Log.d("save", adapter.getItemCount() + "");
        String[] filename;
        int yPos = convertToPixles(5);

        for (int i = 0; i < adapter.getItemCount(); i++) {
            Item item = adapter.getItem(i);
            Log.d("save", i + " " + adapter.getItem(i).getType() + " " + adapter.getItem(i).getW() + " " + adapter.getItem(i).getH());
            switch (item.getType()) {
                case "text":
                    builder.addText(item.getText(), "Arial", (float) convertToPixles(item.getFontSize()), item.getX(), yPos, item.getW(), item.getH(), "#000000");
                    break;
                case "image":
                    filename = new File("" + Uri.parse(item.getImagePath())).getName().split("/");
                    imagePaths.add(item.getImagePath());
                    builder.addImage(filename[filename.length - 1], item.getX(), yPos, item.getW(), item.getH());
                    break;
                case "video":
                    filename = new File("" + Uri.parse(item.getVideoPath())).getName().split("/");
                    videoPaths.add(item.getVideoPath());
                    builder.addVideo(filename[filename.length - 1], item.getX(), yPos, item.getW(), item.getH());
                    break;
            }
            yPos += item.getH() + convertToPixles(5);
        }

        if (builder.getObjectCount() > 0) {
            Log.d("save", "Maml " + builder.getPageAsString());
            mamlFilePath = builder.makeFile(Constants.TEMPDIR);
            numVideos = videoPaths.size();
            convertedVideoPaths = new ArrayList<>();
            uploadMultipart(publish);
        } else {
            Toast.makeText(getApplicationContext(), "No content added", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean videoCompressionToDo(final boolean publish) {
        if (videoPaths.size() == 0) {
            return false;
        }

        String videoPath = videoPaths.remove(0); //pop element from list
        String[] filename = new File(videoPath).getName().split("/");

        try {
            File file = File.createTempFile(filename[filename.length -1].substring(0, filename[filename.length -1].lastIndexOf(".")), ".mp4", this.getCacheDir());
            final String convertedVideoPath = file.getPath();
            compressTask = VideoCompress.compressVideoLow(videoPath, convertedVideoPath, new VideoCompress.CompressListener() {
                @Override
                public void onStart() {
                    progress = new ProgressDialog(SimpleWebCreation.this);
                    progress.setMessage("Compressing video (" + (numVideos - videoPaths.size()) + "/" + numVideos + ")...");
                    progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    progress.setCancelable(false);
                    progress.setProgress(0);
                    progress.setButton(ProgressDialog.BUTTON_NEUTRAL, "Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    compressTask.cancel(true);
                                }
                            });
                    progress.show();
                }

                @Override
                public void onSuccess() {
                    progress.dismiss();
                    convertedVideoPaths.add(convertedVideoPath);
                    uploadMultipart(publish); //call it again and see if there is still work to do
                }

                @Override
                public void onFail() {
                    progress.dismiss();
                    Toast.makeText(getApplicationContext(), "Something went wrong with the compression", Toast.LENGTH_LONG).show();
                    Log.d("videocompression", "fail");
                }

                @Override
                public void onProgress(float percent) {
                    progress.setProgress((int)(percent));
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Something went wrong with the compression", Toast.LENGTH_LONG).show();
        }

        return true;
    }

    private void uploadMultipart(boolean publish) {
        if (videoCompressionToDo(publish)) {
            //there is still work to do, let the videocompressor handle it from now on
            return;
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

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

        final ANRequest.MultiPartBuilder multiPartBuilder = new ANRequest.MultiPartBuilder(prefs.getString("base_url", null) + "upload.py");

        multiPartBuilder.addMultipartParameter("token", prefs.getString("token", "null"));
        multiPartBuilder.addMultipartParameter("title", pageName);
        multiPartBuilder.addMultipartParameter("description", pageDescription);
        multiPartBuilder.addMultipartParameter("resolution", "" + ResourceHelper.getScreenWidth(this));
        multiPartBuilder.addMultipartParameter("category", "100");
        multiPartBuilder.addMultipartFile("maml", new File(mamlFilePath));

        for (String imagePath : imagePaths) {
            if (imagePath != null) {
                String imagePathNew = ResourceHelper.compressImage(this, imagePath, 768, 1024);
                multiPartBuilder.addMultipartFile("images", new File(imagePathNew));
            }
        }

        for (String videoPath : convertedVideoPaths) {
            if (videoPath != null) {
                multiPartBuilder.addMultipartFile("videos", new File(videoPath));
            }
        }
        if (iconPath != null && !iconPath.equals("None")) {
            String iconPathNew = ResourceHelper.compressImage(this, iconPath, 200, 200);
            multiPartBuilder.addMultipartFile("icon", new File(iconPathNew));
        }
        if (publish) {
            multiPartBuilder.addMultipartParameter("publish", "-1");
        } else {
            multiPartBuilder.addMultipartParameter("publish", "0");
        }

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
                        Log.d("thp", "OnResponse " + response.code() + " " + response.toString());
                        if (response.code() == 200) {
                            progress.dismiss();
                            alertD.dismiss();
                            uploadSuccessful();
                        } else {
                            Toast.makeText(getApplicationContext(), "Something went wrong with the upload ("+ response.code()+")", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Toast.makeText(getApplicationContext(), "Something went wrong with the upload ("+ anError.getErrorDetail()+")", Toast.LENGTH_LONG).show();
                        progress.dismiss();
                        alertD.show();
                    }
                });

    }

    private void uploadSuccessful() {
        Log.d("thp", "upload successful");

        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Upload successful");
        alertDialog.setMessage("Your page has been successfully submitted. Someone from our team will approve it shortly.");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });
        alertDialog.show();
        ResourceHelper.cleanupFiles();
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, R.string.toast_back, Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }
}

