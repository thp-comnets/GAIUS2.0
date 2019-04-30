package com.gaius.gaiusapp.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;

import com.alexvasilkov.gestures.views.GestureFrameLayout;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.gaius.gaiusapp.R;
import com.gaius.gaiusapp.networking.GlideApp;

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
        gestureFrameLayout.setBackgroundColor(mCtx.getResources().getColor(R.color.white));

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

        gestureFrameLayout.addView(imageView);
        ((ViewPager) container).addView(gestureFrameLayout, 0);
        return gestureFrameLayout;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        ((ViewPager) container).removeView((GestureFrameLayout) object);
    }

    public Bitmap getBitmapAtPosition(int position) {
        return bitmaps.get(position);
    }

    public String getImageURLAtPosition(int position) {
        return imagesURLs.get(position).substring(imagesURLs.get(position).lastIndexOf('/'));
    }
}
