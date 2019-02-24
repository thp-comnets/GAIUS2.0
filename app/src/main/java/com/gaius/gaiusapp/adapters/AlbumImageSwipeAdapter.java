package com.gaius.gaiusapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.alexvasilkov.gestures.views.GestureFrameLayout;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.gaius.gaiusapp.R;
import com.gaius.gaiusapp.networking.GlideApp;
import com.gaius.gaiusapp.utils.ResourceHelper;

import java.util.ArrayList;

public class AlbumImageSwipeAdapter extends PagerAdapter {
    private ArrayList<String> imagesURLs;
    private ArrayList<Bitmap> bitmaps;
    private PopupMenu popupMenu;
    Context mCtx;

    public AlbumImageSwipeAdapter(Context mCtx, ArrayList<String> imagesURLs, int position) {
        this.mCtx = mCtx;
        this.imagesURLs = imagesURLs;
        this.bitmaps = new ArrayList<>();
        while(this.bitmaps.size() < 10) bitmaps.add(null); //initialize array to prevent out of bounds exception
    }

    @Override
    public int getCount() {
        return imagesURLs.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == ((GestureFrameLayout) o);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        final ImageView imageView = new ImageView(mCtx);

        GestureFrameLayout gestureFrameLayout = new GestureFrameLayout(mCtx);
        gestureFrameLayout.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        gestureFrameLayout.setBackgroundColor(mCtx.getResources().getColor(R.color.black));

        imageView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        GlideApp.with(mCtx)
                .asBitmap()
                .load(imagesURLs.get(position))
                .content()
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        Log.d("thp", "add at " + position);
                        bitmaps.add(position, resource);
                        imageView.setImageBitmap(resource);
                    }
                });

        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                popupMenu = new PopupMenu(mCtx, view);
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
                                MediaStore.Images.Media.insertImage(mCtx.getContentResolver(), bitmaps.get(position) ,imagesURLs.get(position).substring(imagesURLs.get(position).lastIndexOf('/')) , "");
                                Toast.makeText(mCtx, "Image saved locally on device.", Toast.LENGTH_LONG).show();

                            case "Share":
                                Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
                                shareIntent.putExtra(Intent.EXTRA_STREAM, ResourceHelper.getImageUri(mCtx, bitmaps.get(position)));
                                shareIntent.setType("image/jpeg");
                                mCtx.startActivity(Intent.createChooser(shareIntent, "Share image with:"));
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
        gestureFrameLayout.addView(imageView);
        ((ViewPager) container).addView(gestureFrameLayout, 0);
        return gestureFrameLayout;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        ((ViewPager) container).removeView((GestureFrameLayout) object);
    }

}
