package com.gaius.gaiusapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


import java.util.ArrayList;

public class RenderPhotoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setContentView(R.layout.activity_upload_album);
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        //Extract each value from the bundle for usage
        ArrayList<String> imagesURLs = bundle.getStringArrayList("imagesURLs");

//        ScrollGalleryView mScrollGalleryView = findViewById(R.id.popView);
//        ScrollGalleryView
//                .from(mScrollGalleryView)
//                .settings(
//                        GallerySettings
//                                .from(((FragmentActivity) this).getSupportFragmentManager())
//                                .thumbnailSize(100)
//                                .enableZoom(true)
//                                .build()
//                )
//                .build();
//
//        for (int i=0; i < imagesURLs.size(); i++) {
//            mScrollGalleryView.addMedia(image(imagesURLs.get(i)));
//        }
    }
}
