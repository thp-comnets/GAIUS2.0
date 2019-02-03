package com.gaius.gaiusapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;


import com.gaius.gaiusapp.adapters.AlbumAdapter;

import java.util.ArrayList;


public class AlbumViewActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    ArrayList<String> imagesURLs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setContentView(R.layout.album_view_activity);

        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        ArrayList<String> imagesURLs = bundle.getStringArrayList("imagesURLs");

        recyclerView = findViewById(R.id.images_recyclerview);
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        AlbumAdapter adapter = new AlbumAdapter(this, imagesURLs);
        recyclerView.setAdapter(adapter);
    }
}
