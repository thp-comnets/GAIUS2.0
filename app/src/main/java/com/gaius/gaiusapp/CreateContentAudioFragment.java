package com.gaius.gaiusapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ANRequest;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.AnalyticsListener;
import com.androidnetworking.interfaces.OkHttpResponseListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.gaius.gaiusapp.utils.ResourceHelper;
import com.google.android.gms.common.util.IOUtils;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import nl.changer.audiowife.AudioWife;
import okhttp3.Response;

import static android.app.Activity.RESULT_OK;

public class CreateContentAudioFragment extends Fragment {


    AlertDialog alertD;
    EditText editTextPagename, editTextDescription;
    TextInputLayout editTextPagenameLayout, editTextDescriptionLayout;
//    ProgressDialog progress;
    private static final int REQUEST_ID_PERMISSIONS = 1;
    private static final int ADD_AUDIO = 1001;
    private final int PICK_AUDIO_REQUEST = 111;
    String audioPath;
    ProgressDialog progress;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_create_content_audio, container, false);
        ImageView recordAudio = (ImageView) rootView.findViewById(R.id.iv_RecordAudio);
        recordAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

              /*  if (checkStoragePermission()) {
                    Intent i = new Intent(getContext(), AudioActivity.class);
                    i.putExtra("frgToLoad","Record");
                     getContext().startActivity(i);
                } else {
                    requestStoragePermission();
                }*/

                String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO};
                Permissions.check((Activity) getActivity(), permissions, null, null, new PermissionHandler() {
                    @Override
                    public void onGranted() {
                        Intent i = new Intent(getContext(), AudioActivity.class);
                        i.putExtra("frgToLoad","Record");
                        getContext().startActivity(i);
                    }

                    @Override
                    public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                        super.onDenied(context, deniedPermissions);
                        Toast.makeText((Activity) getActivity(), "If you reject this permission, you can not use this functionality.", Toast.LENGTH_SHORT).show();
                    }
                });


//                Intent i = new Intent(getContext(), AudioTrimmerActivity.class);
//                getContext().startActivity(i);
            }
        });

        ImageView uploadAudio = (ImageView) rootView.findViewById(R.id.iv_uploadAudio);
        uploadAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
//                galleryIntent.setType("audio/*");
//                galleryIntent.putExtra(Intent.EXTRA_MIME_TYPES, new String[] { "audio/*"});
//                startActivityForResult(galleryIntent, PICK_AUDIO_REQUEST);

                Intent intent_upload = new Intent();
                intent_upload.setType("audio/*");
                intent_upload.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent_upload,PICK_AUDIO_REQUEST);
            }
        });
        return rootView;
    }



 /*   @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_ID_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(getActivity(), "Permission granted, Click again", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(getContext(), AudioActivity.class);
                i.putExtra("frgToLoad","Record");
                getContext().startActivity(i);
            }
        }
    }*/

    @Override
    @SuppressLint("NewApi")
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_AUDIO_REQUEST) {
                File myFile = null;

                Uri uri = data.getData();
                try {
                    String uriString = uri.toString();
                    myFile = new File(uriString);
                    //    String path = myFile.getAbsolutePath();
                    String displayName = null;
//                    audioPath = getAudioPath(uri);
//                    String[] videoFile = getAudioPathOne(uri);
                    audioPath = getFilePathFromURI(getActivity(),uri);
                    Log.d("thulasi", "Upload video " + audioPath + " " );

                } catch (Exception e) {
                    //handle exception
                    Log.d("thulasi", "Upload video " + e.getMessage() + " " );
                    Toast.makeText(getActivity(), "Unable to process,try again", Toast.LENGTH_SHORT).show();
                }


//                Uri fileUri = data.getData();
//                String[] videoFile = getVideoPath(fileUri);
//                uploadVideoPath = videoFile[0];
//                Bitmap selectedImage = ThumbnailUtils.createVideoThumbnail(videoFile[0], MediaStore.Images.Thumbnails.MINI_KIND);

                Log.d("yasir", "Upload audio " + myFile + " " + "");

                LayoutInflater layoutInflater = LayoutInflater.from(getContext());
                final View promptView = layoutInflater.inflate(R.layout.upload_audio_popup, null);
                alertD = new AlertDialog.Builder(getContext()).create();
                alertD.setView(promptView);
                alertD.setCancelable(false);
                alertD.show();

                View mPlayMedia = promptView.findViewById(R.id.play);
                View mPauseMedia = promptView.findViewById(R.id.pause);
                SeekBar mMediaSeekBar = promptView.findViewById(R.id.media_seekbar);
                TextView mRunTime = promptView.findViewById(R.id.run_time);
                TextView mTotalTime = promptView.findViewById(R.id.total_time);

                AudioWife.getInstance()
                        .init(getActivity(), uri)
                        .setPlayView(mPlayMedia)
                        .setPauseView(mPauseMedia)
                        .setSeekBar(mMediaSeekBar)
                        .setRuntimeView(mRunTime)
                        .setTotalTimeView(mTotalTime);

                AudioWife.getInstance().addOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        Toast.makeText(getActivity(), "Completed", Toast.LENGTH_SHORT).show();
                        // do you stuff.
                    }
                });

                AudioWife.getInstance().addOnPlayClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getActivity(), "Play", Toast.LENGTH_SHORT).show();
                        // get-set-go. Lets dance.
                    }
                });

                AudioWife.getInstance().addOnPauseClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getActivity(), "Pause", Toast.LENGTH_SHORT).show();
                        // Your on audio pause stuff.
                    }
                });

                editTextPagename = promptView.findViewById(R.id.title_edittext);
                editTextDescription = promptView.findViewById(R.id.description_edittext);
                editTextPagenameLayout = promptView.findViewById(R.id.title_edittext_layout);
                editTextDescriptionLayout = promptView.findViewById(R.id.description_edittext_layout);


//                JzvdStd jzVideoPlayerStandard = (JzvdStd) promptView.findViewById(R.id.video_view);
//                jzVideoPlayerStandard.setUp(uploadVideoPath, "", Jzvd.SCREEN_WINDOW_NORMAL);
//                jzVideoPlayerStandard.thumbImageView.setImageBitmap(selectedImage);

                Button cancel_button = (Button) promptView.findViewById(R.id.cancel_button);
                final Button upload_button = (Button) promptView.findViewById(R.id.upload_button);

                cancel_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertD.dismiss();
                        AudioWife.getInstance().release();
                    }
                });

                upload_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AudioWife.getInstance().release();

                        if (filledFields(promptView)) {
                            String sourcePath = audioPath;
                            try {

                                uploadMultipart(editTextPagename.getText().toString(), editTextDescription.getText().toString());

                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(getContext(), "Something went wrong with the compression", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
            }
        }
    }

    private String getAudioPath(Uri uri) {
        String[] data = {MediaStore.Audio.Media.DATA};
        CursorLoader loader = new CursorLoader(getActivity(), uri, data, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
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

        String url = prefs.getString("base_url", null) + "uploadAudio.py";
//        String url = "http://192.168.1.37:5000/uploadAudio.py";

        ANRequest.MultiPartBuilder multiPartBuilder = new ANRequest.MultiPartBuilder(url);
        Log.v("audiopath", " -- "+ audioPath);
        multiPartBuilder.addMultipartFile("audio", new File(audioPath));
        multiPartBuilder.addMultipartParameter("category", "101");


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
        alertDialog.setMessage("Your audio has been successfully submitted. Someone from our team will approve it shortly.");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
        ResourceHelper.cleanupFiles();
    }



    /*private class UploadFileToServer extends AsyncTask<Void, Integer, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            return uploadFile();
        }

        private String uploadFile() {
            String responseString = null;
            Log.d("Log", "File path" + opFilePath);
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(Bitmap.Config.FILE_UPLOAD_URL);
            try {
                AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
                        new AndroidMultiPartEntity.ProgressListener() {

                            @Override
                            public void transferred(long num) {
                                publishProgress((int) ((num / (float) totalSize) * 100));
                            }
                        });
                ExifInterface newIntef = new ExifInterface(opFilePath);
                newIntef.setAttribute(ExifInterface.TAG_ORIENTATION,String.valueOf(2));
                File file = new File(opFilePath);
                entity.addPart("pic", new FileBody(file));
                totalSize = entity.getContentLength();
                httppost.setEntity(entity);

                // Making server call
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity r_entity = response.getEntity();


                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    // Server response
                    responseString = EntityUtils.toString(r_entity);
                    Log.d("Log", responseString);
                } else {
                    responseString = "Error occurred! Http Status Code: "
                            + statusCode + " -> " + response.getStatusLine().getReasonPhrase();
                    Log.d("Log", responseString);
                }

            } catch (ClientProtocolException e) {
                responseString = e.toString();
            } catch (IOException e) {
                responseString = e.toString();
            }

            return responseString;
        }
    }*/




    public static String getFilePathFromURI(Context context, Uri contentUri) {
        //copy file and send new file path
        String fileName = getFileName(contentUri);
        if (!TextUtils.isEmpty(fileName)) {
            File copyFile = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + fileName);
            copy(context, contentUri, copyFile);
            return copyFile.getAbsolutePath();
        }
        return null;
    }

    public static String getFileName(Uri uri) {
        if (uri == null) return null;
        String fileName = null;
        String path = uri.getPath();
        int cut = path.lastIndexOf('/');
        if (cut != -1) {
            fileName = path.substring(cut + 1);
        }
        return fileName;
    }

    public static void copy(Context context, Uri srcUri, File dstFile) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(srcUri);
            if (inputStream == null) return;
            OutputStream outputStream = new FileOutputStream(dstFile);
            IOUtils.copyStream(inputStream, outputStream);
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
