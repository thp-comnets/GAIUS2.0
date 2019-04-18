package com.gaius.gaiusapp;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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
import com.gaius.gaiusapp.adapters.UrlGalleryAdapter;
import com.gaius.gaiusapp.utils.ResourceHelper;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import io.brotherjing.galleryview.GalleryView;
import okhttp3.Response;

import static android.app.Activity.RESULT_OK;
import static com.gaius.gaiusapp.utils.ResourceHelper.getResizedBitmap;

public class CreateContentImagesFragment extends Fragment {

    private final int PICK_IMAGE_MULTIPLE = 0;
    private final int REQUEST_TAKE_PHOTO = 1;
    String currentPhotoPathCaptured;
    int currentImagePos = 0;
    ArrayList<String> multiImageViewBitmaps;
    ArrayList<String> uploadImagesPath;

    UrlGalleryAdapter adapter;

    AlertDialog alertD;

    GalleryView galleryView = null;
    TextView label = null;
    EditText editTextPagename, editTextDescription;
    TextInputLayout editTextPagenameLayout, editTextDescriptionLayout;
    ProgressDialog progress;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_create_content_images, container, false);
        ImageView uploadImage = (ImageView) rootView.findViewById(R.id.imageViewUpload);
        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });

        TextView uploadImageTextView = (TextView) rootView.findViewById(R.id.textViewUpload);
        uploadImageTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });

        ImageView captureImage = (ImageView) rootView.findViewById(R.id.imageViewCapture);
        captureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureImage();
            }
        });

        TextView captureImageTextView = (TextView) rootView.findViewById(R.id.textViewCapture);
        captureImageTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureImage();
            }
        });
        return rootView;
    }

    @Override
    @SuppressLint("NewApi")
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_MULTIPLE) {
                Log.d("yasir", "PICK_IMAGE_MULTIPLE");

                multiImageViewBitmaps = new ArrayList<>();
                uploadImagesPath = new ArrayList<>();

                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount(); //evaluate the count before the for loop --- otherwise, the count is evaluated every loop.

                    for (int i = 0; i < count; i++) {
                        Uri imageUri = data.getClipData().getItemAt(i).getUri();

                        Bitmap bitmap = null;
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), imageUri);
                            bitmap = getResizedBitmap(bitmap, 800);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        String imagePath = ResourceHelper.saveBitmapCompressed(getContext(), imageUri, bitmap);
                        uploadImagesPath.add(imagePath);
                        multiImageViewBitmaps.add(imageUri.toString());
                    }
                } else if (data.getData() != null) {
                    Uri imageUri = data.getData();
                    Bitmap bitmap = null;
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), imageUri);
                        bitmap = getResizedBitmap(bitmap, 800);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    String imagePath = ResourceHelper.saveBitmapCompressed(getContext(), imageUri, bitmap);

                    uploadImagesPath.add(imagePath);
                    multiImageViewBitmaps.add(imageUri.toString());
                }
            }

            if (requestCode == REQUEST_TAKE_PHOTO) {
                File file = new File(currentPhotoPathCaptured);
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), Uri.fromFile(file));
                    bitmap = ResourceHelper.rotateImageIfRequired(getContext(), bitmap, Uri.fromFile(file));
                    bitmap = ResourceHelper.getResizedBitmap(bitmap, 800);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (bitmap != null) {
                    String imagePath = ResourceHelper.saveBitmapCompressed(getContext(), Uri.fromFile(file), bitmap);
                    multiImageViewBitmaps = new ArrayList<>();
                    uploadImagesPath = new ArrayList<>();
                    uploadImagesPath.add(imagePath);
                    multiImageViewBitmaps.add(Uri.fromFile(file).toString());
                }
            }

            if (!uploadImagesPath.isEmpty()) {
                LayoutInflater layoutInflater = LayoutInflater.from(getContext());
                final View promptView = layoutInflater.inflate(R.layout.activity_upload_album, null);
                alertD = new AlertDialog.Builder(getContext()).create();
                alertD.setView(promptView);
                alertD.show();

                editTextPagename = promptView.findViewById(R.id.images_album_title);
                editTextDescription = promptView.findViewById(R.id.images_album_description);
                editTextPagenameLayout = promptView.findViewById(R.id.images_album_title_layout);
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
                label.setText((1) + "/" + galleryView.getAdapter().getCount());

                ImageView deleteButton = promptView.findViewById(R.id.delete_button);
                deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        multiImageViewBitmaps.remove(currentImagePos - 1);
                        uploadImagesPath.remove(currentImagePos - 1);

                        if (uploadImagesPath.size() == 0) {
                            alertD.dismiss();
                        }

                        adapter = new UrlGalleryAdapter(getContext(), multiImageViewBitmaps);
                        galleryView.setAdapter(adapter);
                        label.setText((1) + "/" + galleryView.getAdapter().getCount());
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
                            uploadMultipart(editTextPagename.getText().toString(), editTextDescription.getText().toString());
                        }
                    }
                });
            }
        }
    }

    private void uploadImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_MULTIPLE); //SELECT_PICTURES is simply a global int used to check the calling intent in onActivityResult
    }

    private void captureImage() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Toast.makeText(getContext(), "Something went wrong when saving the image", Toast.LENGTH_LONG).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getContext(), "com.gaius.gaiusapp.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    public void incrementCount(int index) {
        label.setText((index + 1) + "/" + galleryView.getAdapter().getCount());
        currentImagePos = index + 1;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        currentPhotoPathCaptured = image.getAbsolutePath();
        return image;
    }

    private void uploadMultipart(final String title, final String description) {
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

        String url = prefs.getString("base_url", null) + "uploadImages.py";

        ANRequest.MultiPartBuilder multiPartBuilder = new ANRequest.MultiPartBuilder(url);

        for (String imagePath : uploadImagesPath) {
            if (imagePath != null) {
                Log.d("thp", "path to image " + imagePath);

                String imagePathNew = ResourceHelper.compressImage(getActivity().getApplicationContext(), imagePath, 768, 1024);
                multiPartBuilder.addMultipartFile("images", new File(imagePathNew));

            }
        }

        multiPartBuilder.addMultipartParameter("token", prefs.getString("token", "null"));
        multiPartBuilder.addMultipartParameter("title", title);
        multiPartBuilder.addMultipartParameter("description", description);
        multiPartBuilder.build()
                .setUploadProgressListener(new UploadProgressListener() {
                    @Override
                    public void onProgress(long bytesUploaded, long totalBytes) {
                        progress.setProgress((int) ((float) bytesUploaded / totalBytes * 100.0));
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
                            uploadSuccessful();

                        } else {
                            Toast.makeText(getContext(), "Something went wrong with the upload (" + response.code() + ")", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Toast.makeText(getContext(), "Something went wrong with the upload (" + anError.getErrorDetail() + ")", Toast.LENGTH_LONG).show();
                        progress.dismiss();
                        alertD.show();
                    }
                });

    }

    private void uploadSuccessful() {
        Log.d("thp", "upload successful");

        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setTitle("Upload successful");
        alertDialog.setMessage("Your image ablbum has been successfully submitted. Someone from our team will approve it shortly.");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
        ResourceHelper.cleanupFiles();
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
        hideKeyboard(editTextPagename);
        hideKeyboard(editTextDescription);
        return true;
    }

    public void hideKeyboard(View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
