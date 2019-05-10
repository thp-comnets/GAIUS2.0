package com.gaius.gaiusapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;

public class AudioViewActivity extends AppCompatActivity implements OnClickListener, OnTouchListener, OnCompletionListener, OnBufferingUpdateListener {

    private FloatingActionButton buttonPlayPause;
    private SeekBar seekBarProgress;
    private AppCompatTextView textAudioName;

    private MediaPlayer mediaPlayer;
    private int mediaFileLengthInMilliseconds;

    private final Handler handler = new Handler();

    /**
     * Called when the activity is first created.
     */

    String audioPath;
    String audioName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.audio_view_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Uri data = getIntent().getData();

        if (data != null && data.toString().contains("http://gaiusnetworks.com/audios/")) {
            String videoURL =  prefs.getString("base_url", null) + data.toString().replace("http://gaiusnetworks.com","");
            Log.v("audioPath1", " - " + videoURL);
            audioPath = videoURL;
            audioPath = audioPath.replace("t//","t/");
            Log.v("audioPath1", " - " + audioPath);

        }else {

            Intent intent = this.getIntent();
            Bundle bundle = intent.getExtras();
            audioPath = bundle.getString("URL");
            audioName = bundle.getString("URL");
            if (audioPath != null)
                audioPath = audioPath.replace("./", "");

            audioPath = prefs.getString("base_url", null) + audioPath;
            Log.v("audioPath", " - " + audioPath);
        }
        initView();



    }

    /**
     * This method initialise all the views in project
     */
    private void initView() {
        buttonPlayPause =  findViewById(R.id.ButtonTestPlayPause);
        textAudioName =  findViewById(R.id.audioName);
        buttonPlayPause.setOnClickListener(this);

        seekBarProgress = (SeekBar) findViewById(R.id.SeekBarTestPlay);
        seekBarProgress.setMax(99); // It means 100% .0-99
        seekBarProgress.setOnTouchListener(this);

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnCompletionListener(this);
        if(audioName != null)
        audioName = audioName.replace("./audios/","");
        textAudioName.setText(audioName);
    }

    /**
     * Method which updates the SeekBar primary progress by current audio playing position
     */
    private void primarySeekBarProgressUpdater() {

        try {

            seekBarProgress.setProgress((int) (((float) mediaPlayer.getCurrentPosition() / mediaFileLengthInMilliseconds) * 100)); // This math construction give a percentage of "was playing"/"song length"
            if (mediaPlayer.isPlaying()) {
                Runnable notification = new Runnable() {
                    public void run() {
                        primarySeekBarProgressUpdater();
                    }
                };
                handler.postDelayed(notification, 1000);
            }

        } catch (IllegalStateException e) {
            mediaPlayer.reset();
//            currentPosition = mediaPlayer.getCurrentPosition();
        }

//        mediaPlayer.prepareAsync();

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ButtonTestPlayPause) {
            /** ImageButton onClick event handler. Method which start/pause mediaplayer playing */
            try {
                mediaPlayer.setDataSource(audioPath);
                mediaPlayer.prepare();
            } catch (Exception e) {
                e.printStackTrace();
            }

            mediaFileLengthInMilliseconds = mediaPlayer.getDuration(); // gets the audio length in milliseconds from URL

            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                buttonPlayPause.setImageResource(R.drawable.ic_media_pause);
            } else {
                mediaPlayer.pause();
                buttonPlayPause.setImageResource(R.drawable.ic_media_play);
            }

            primarySeekBarProgressUpdater();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.SeekBarTestPlay) {
            /** Seekbar onTouch event handler. Method which seeks MediaPlayer to seekBar primary progress position*/
            if (mediaPlayer.isPlaying()) {
                SeekBar sb = (SeekBar) v;
                int playPositionInMillisecconds = (mediaFileLengthInMilliseconds / 100) * sb.getProgress();
                mediaPlayer.seekTo(playPositionInMillisecconds);
            }
        }
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

        buttonPlayPause.setImageResource(R.drawable.ic_media_play);
        mediaPlayer.seekTo(0);
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

        seekBarProgress.setSecondaryProgress(percent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mediaPlayer.reset();
    }

    //handle the back arrow press in the toolbar
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

