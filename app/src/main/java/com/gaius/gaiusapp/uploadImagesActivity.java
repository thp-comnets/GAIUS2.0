package com.gaius.gaiusapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;

import io.brotherjing.galleryview.GalleryView;

public class uploadImagesActivity extends AppCompatActivity {

    private GalleryView galleryView;
    private TextView label;
    private final int PICK_IMAGE_MULTIPLE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.view_images_popup);

        super.onCreate(savedInstanceState);

        galleryView = (GalleryView)findViewById(R.id.gallery);
        label = (TextView)findViewById(R.id.tvLabel);

        galleryView.setScrollEndListener(new GalleryView.OnScrollEndListener() {
            @Override
            public void onScrollEnd(int index) {
                label.setText((index+1)+"/"+galleryView.getAdapter().getCount());
            }
        });

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_MULTIPLE); //SELECT_PICTURES is simply a global int used to check the calling intent in onActivityResult

    }

    @Override
    @SuppressLint("NewApi")
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Intent intent;

        Log.d("yasir","Upload video2 ");

        if (resultCode == RESULT_OK) {

            if(requestCode == PICK_IMAGE_MULTIPLE) {
                Log.d("yasir","PICK_IMAGE_MULTIPLE");

                ArrayList<String> multiImageViewBitmaps;
                multiImageViewBitmaps = new ArrayList<>();

                if(resultCode == this.RESULT_OK) {
                    if(data.getClipData() != null) {
                        int count = data.getClipData().getItemCount(); //evaluate the count before the for loop --- otherwise, the count is evaluated every loop.

                        for(int i = 0; i < count; i++) {
                            Uri imageUri = data.getClipData().getItemAt(i).getUri();
                            multiImageViewBitmaps.add(imageUri.toString());
                        }
                    }
                    else if(data.getData() != null) {
                        String imagePath = data.getData().toString();
                        multiImageViewBitmaps.add(imagePath);
                    }
                }

                galleryView.setAdapter(new UrlGalleryAdapter(this,multiImageViewBitmaps));
            }
        }
    }
}
