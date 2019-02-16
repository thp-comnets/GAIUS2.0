package com.gaius.gaiusapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import com.gaius.gaiusapp.networking.GlideApp;

import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;

import static com.gaius.gaiusapp.utils.ResourceHelper.convertImageURLBasedonFidelity;

public class VideoViewActivity extends AppCompatActivity {
    JzvdStd videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setContentView(R.layout.video_view_activity);

        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        Uri data = getIntent().getData();

        if (data != null && data.toString().contains("http://gaiusnetworks.com/videos/")) {
            String videoURL =  prefs.getString("base_url", null) + data.toString().replace("http://gaiusnetworks.com","");

            videoView = findViewById(R.id.videoView);

            GlideApp.with(this)
                    .load(convertImageURLBasedonFidelity(videoURL.replace(".mp4",".jpg"), prefs.getString("fidelity_level", "high"))) //fixme: if not .mp4
                    .content()
                    .into(videoView.thumbImageView);

            videoView.setUp(videoURL, "", Jzvd.SCREEN_WINDOW_NORMAL);

        }
        else {
            Intent intent = this.getIntent();
            Bundle bundle = intent.getExtras();

            String videoURL =  prefs.getString("base_url", null) + bundle.getString("URL");

            videoView = findViewById(R.id.videoView);

            GlideApp.with(this)
                    .load(convertImageURLBasedonFidelity(videoURL.replace(".mp4",".jpg"), prefs.getString("fidelity_level", "high"))) //fixme: if not .mp4
                    .content()
                    .into(videoView.thumbImageView);

            videoView.setUp(videoURL, "", Jzvd.SCREEN_WINDOW_NORMAL);
        }
    }

    @Override
    public void onBackPressed() {
        Jzvd.releaseAllVideos();
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Jzvd.releaseAllVideos();
    }
}
