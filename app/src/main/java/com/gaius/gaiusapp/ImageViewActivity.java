package com.gaius.gaiusapp;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.gaius.gaiusapp.adapters.AlbumImageSwipeAdapter;

import java.util.ArrayList;

public class ImageViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setContentView(R.layout.image_view_layout);

        getSupportActionBar().hide();

        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        ArrayList<String> imageURLs = bundle.getStringArrayList("URLs");
        int position = bundle.getInt("position");

        ViewPager viewPager = (ViewPager) findViewById(R.id.album_view_pager);
        AlbumImageSwipeAdapter adapter = new AlbumImageSwipeAdapter(this, imageURLs, position);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(imageURLs.size()-1);
        viewPager.setCurrentItem(position);
    }
}
