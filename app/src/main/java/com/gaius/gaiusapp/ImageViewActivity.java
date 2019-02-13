package com.gaius.gaiusapp;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

public class ImageViewActivity extends AppCompatActivity {
    public Bitmap mBitmap;
    private PopupMenu popupMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setContentView(R.layout.image_view_layout);

        super.onCreate(savedInstanceState);

        final ImageView photoView = findViewById(R.id.imageView);

        Bundle bundle = getIntent().getExtras();
        final String url = bundle.getString("URL");
        if (url != null) {
            Glide.with(this)
                    .asBitmap()
                    .load(url)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            mBitmap = resource;
                            photoView.setImageBitmap(resource);
                        }
                    });
        }

        photoView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Log.d("yasir", "on long click");

                popupMenu = new PopupMenu(getApplicationContext(), view);
                popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
                    @Override
                    public void onDismiss(PopupMenu menu) {
                        popupMenu.dismiss();
                    }
                });
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), mBitmap ,url.substring(url.lastIndexOf('/')) , "");
                        popupMenu.dismiss();

                        return true;
                    }
                });
                popupMenu.inflate(R.menu.save_image_popup_menu);
                popupMenu.show();

                return true;
            }
        });
    }
}
