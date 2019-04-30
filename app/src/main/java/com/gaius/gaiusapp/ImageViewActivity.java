package com.gaius.gaiusapp;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.gaius.gaiusapp.adapters.AlbumImageSwipeAdapter;
import com.gaius.gaiusapp.utils.ResourceHelper;

import java.util.ArrayList;

public class ImageViewActivity extends AppCompatActivity {
    Toolbar toolbar;
    ViewPager viewPager;
    AlbumImageSwipeAdapter imageSwipeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.image_view_layout);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        Bundle bundle = getIntent().getExtras();
        ArrayList<String> imageURLs = bundle.getStringArrayList("URLs");
        int position = bundle.getInt("position");

        viewPager = (ViewPager) findViewById(R.id.album_view_pager);
        imageSwipeAdapter = new AlbumImageSwipeAdapter(this, imageURLs, position);
        viewPager.setAdapter(imageSwipeAdapter);
        viewPager.setOffscreenPageLimit(imageURLs.size()-1);
        viewPager.setCurrentItem(position);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater findMenuItems = getMenuInflater();
        findMenuItems.inflate(R.menu.save_image_popup_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int position = viewPager.getCurrentItem();
        switch (item.getItemId()) {
            case R.id.save_image:
                MediaStore.Images.Media.insertImage(this.getContentResolver(), imageSwipeAdapter.getBitmapAtPosition(position), imageSwipeAdapter.getImageURLAtPosition(position) , "");
                Toast.makeText(this, "Image saved on your device.", Toast.LENGTH_LONG).show();
                break;
            case R.id.share_image:
                Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, ResourceHelper.getImageUri(this, imageSwipeAdapter.getBitmapAtPosition(position)));
                shareIntent.setType("image/jpeg");
                this.startActivity(Intent.createChooser(shareIntent, "Share image with:"));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //handle the back arrow press in the toolbar
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
