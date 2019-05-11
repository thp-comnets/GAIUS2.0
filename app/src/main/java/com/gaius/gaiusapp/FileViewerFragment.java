package com.gaius.gaiusapp;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ANRequest;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.AnalyticsListener;
import com.androidnetworking.interfaces.OkHttpResponseListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.gaius.gaiusapp.adapters.FileViewerAdapter;
import com.gaius.gaiusapp.helper.OnDatabaseChangedListener;
import com.gaius.gaiusapp.utils.DBHelper;
import com.gaius.gaiusapp.utils.ExternalDbOpenHelper;
import com.gaius.gaiusapp.utils.ResourceHelper;
import com.google.android.gms.common.util.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import nl.changer.audiowife.AudioWife;
import okhttp3.Response;

import static android.app.Activity.RESULT_OK;


public class FileViewerFragment extends Fragment implements OnDatabaseChangedListener {
    private static final String ARG_POSITION = "position";
    private static final String LOG_TAG = "FileViewerFragment";

    private int position;
    private FileViewerAdapter mFileViewerAdapter;

    private final int PICK_AUDIO_REQUEST = 111;
    String audioPath;
    AlertDialog alertD;
    EditText editTextPagename, editTextDescription;
    TextInputLayout editTextPagenameLayout, editTextDescriptionLayout;
    ProgressDialog progress;
    Context mContext;
    private DBHelper mDatabase;
    SQLiteDatabase database;

    public static FileViewerFragment newInstance(int position) {
        FileViewerFragment f = new FileViewerFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt(ARG_POSITION);
        ExternalDbOpenHelper dbOpenHelper = new ExternalDbOpenHelper(getActivity(), "gaius.sqlite");
        database = dbOpenHelper.openDataBase();
        observer.startWatching();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_file_viewer, container, false);

        RecyclerView mRecyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);

        //newest to oldest order (database stores from oldest to newest)
        llm.setReverseLayout(true);
        llm.setStackFromEnd(true);

        mRecyclerView.setLayoutManager(llm);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mFileViewerAdapter = new FileViewerAdapter(getActivity(), llm,this);
        mRecyclerView.setAdapter(mFileViewerAdapter);

        return v;
    }

    FileObserver observer =
            new FileObserver(Environment.getExternalStorageDirectory().toString()
                    + "/Gaius") {
                // set up a file observer to watch this directory on sd card
                @Override
                public void onEvent(int event, String file) {
                    if(event == FileObserver.DELETE){
                        // user deletes a recording file out of the app

                        String filePath = Environment.getExternalStorageDirectory().toString()
                                + "/Gaius" + file + "]";

                        Log.d(LOG_TAG, "File deleted ["
                                + Environment.getExternalStorageDirectory().toString()
                                + "/Gaius" + file + "]");

                        // remove file from database and recyclerview
                        mFileViewerAdapter.removeOutOfApp(filePath);
                    }
                }
            };

    public void uploadAudio(int ids){
        String file_path = null;
        Cursor audio = database.rawQuery("SELECT * FROM saved_recordings WHERE _id = "+ids+" ",null);
        audio.moveToFirst();
        if(audio.getCount() > 0){
          String id = audio.getString(audio.getColumnIndex("_id"));
          file_path = audio.getString(audio.getColumnIndex("file_path"));
          Log.v("file_path", " "+ id+" - "+ file_path);
        }
        audio.close();


        File audioFile = new File(file_path);
        Uri mUri = Uri.fromFile(audioFile);
        Log.i("", Uri.fromFile(audioFile).toString());
        audioPath = file_path;
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
                .init(getActivity(), mUri)
                .setPlayView(mPlayMedia)
                .setPauseView(mPauseMedia)
                .setSeekBar(mMediaSeekBar)
                .setRuntimeView(mRunTime)
                .setTotalTimeView(mTotalTime);

        AudioWife.getInstance().addOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
//                Toast.makeText(getContext(), "Completed", Toast.LENGTH_SHORT).show();
            }
        });

        AudioWife.getInstance().addOnPlayClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
//                Toast.makeText(getContext(), "Play", Toast.LENGTH_SHORT).show();
            }
        });

        AudioWife.getInstance().addOnPauseClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
//                Toast.makeText(getContext(), "Pause", Toast.LENGTH_SHORT).show();
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
                        alertD.dismiss();
                        uploadMultipart(editTextPagename.getText().toString(), editTextDescription.getText().toString());

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Something went wrong with the audio", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });





//        Intent intent_upload = new Intent();
//        intent_upload.setType("audio/*");
//        intent_upload.setAction(Intent.ACTION_GET_CONTENT);
//        startActivityForResult(intent_upload,PICK_AUDIO_REQUEST);
    }

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
                    audioPath = getFilePathFromURI(getActivity(),uri);
                    Log.v("audioPath","sds"+audioPath);
//                    File f = new File(audioPath);
//                    long fileSizeInBytes = f.length();
//                    long fileSizeInKB = fileSizeInBytes / 1024;
//                    long fileSizeInMB = fileSizeInKB / 1024;
//                    if (fileSizeInMB > 8) {
//                        customAlterDialog("Can't Upload ", "sorry file size is large");
//                    } else {
//                        profilePicUrl = path2;
//                        isPicSelect = true;
//                    }
                } catch (Exception e) {
                    //handle exception
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
//                        Toast.makeText(getActivity(), "Completed", Toast.LENGTH_SHORT).show();
                        // do you stuff.
                    }
                });

                AudioWife.getInstance().addOnPlayClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
//                        Toast.makeText(getActivity(), "Play", Toast.LENGTH_SHORT).show();
                        // get-set-go. Lets dance.
                    }
                });

                AudioWife.getInstance().addOnPauseClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
//                        Toast.makeText(getActivity(), "Pause", Toast.LENGTH_SHORT).show();
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
                        progress = new ProgressDialog(getActivity());
                        progress.setMessage("Uploading audio...");
                        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        progress.setCancelable(false);
                        progress.setProgress(0);
                        progress.setButton(ProgressDialog.BUTTON_NEUTRAL, "Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                });
                        progress.show();

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

        ANRequest.MultiPartBuilder multiPartBuilder = new ANRequest.MultiPartBuilder(url);

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

    @Override
    public void onNewDatabaseEntryAdded() {

    }

    @Override
    public void onDatabaseEntryRenamed() {

    }

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




