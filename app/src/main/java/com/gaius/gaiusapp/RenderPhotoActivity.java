package com.gaius.gaiusapp;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;

import com.veinhorn.scrollgalleryview.ScrollGalleryView;
import com.veinhorn.scrollgalleryview.builder.GallerySettings;

import java.util.ArrayList;

import static com.veinhorn.scrollgalleryview.loader.picasso.dsl.DSL.image;

public class RenderPhotoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setContentView(R.layout.view_images_popup);
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        //Extract each value from the bundle for usage
        ArrayList<String> imagesURLs = bundle.getStringArrayList("imagesURLs");

        ScrollGalleryView mScrollGalleryView = findViewById(R.id.popView);
        ScrollGalleryView
                .from(mScrollGalleryView)
                .settings(
                        GallerySettings
                                .from(((FragmentActivity) this).getSupportFragmentManager())
                                .thumbnailSize(100)
                                .enableZoom(true)
                                .build()
                )
                .build();

        for (int i=0; i < imagesURLs.size(); i++) {
            mScrollGalleryView.addMedia(image(imagesURLs.get(i)));
        }
    }
}
