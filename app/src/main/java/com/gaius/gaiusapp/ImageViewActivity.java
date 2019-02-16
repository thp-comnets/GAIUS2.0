package com.gaius.gaiusapp;

import android.content.Intent;
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
import android.widget.Toast;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.gaius.gaiusapp.networking.GlideApp;
import com.gaius.gaiusapp.utils.ResourceHelper;

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
            GlideApp.with(this)
                    .asBitmap()
                    .load(url)
                    .content()
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
                        switch (item.toString()) {
                            case "Save":
                                MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), mBitmap ,url.substring(url.lastIndexOf('/')) , "");
                                Toast.makeText(getApplicationContext(), "Image saved locally on device.", Toast.LENGTH_LONG).show();

                            case "Share":
                                Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
                                shareIntent.putExtra(Intent.EXTRA_STREAM, ResourceHelper.getImageUri(getApplicationContext(), mBitmap));
                                shareIntent.setType("image/jpeg");
                                startActivity(Intent.createChooser(shareIntent, "Share image with:"));
                        }
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
