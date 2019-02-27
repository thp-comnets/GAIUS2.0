package com.gaius.gaiusapp.networking;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Priority;

import ss.com.bannerslider.ImageLoadingService;

public class GlideImageLoadingService implements ImageLoadingService {
    public Context context;

    public GlideImageLoadingService(Context context) {
        this.context = context;
    }

    @Override
    public void loadImage(String url, ImageView imageView) {
        if (isValidContextForGlide(context)) {
            Log.d("thp", "Loading image " + url);
            GlideApp.with(context)
                    .load(url)
                    .priority(Priority.HIGH)
                    .content()
                    .into(imageView);
        }
    }

    @Override
    public void loadImage(int resource, ImageView imageView) {
        //not implemented
    }

    @Override
    public void loadImage(String url, int placeHolder, int errorDrawable, ImageView imageView) {
        //not implemented
    }

    public static boolean isValidContextForGlide(final Context context) {
        if (context == null) {
            return false;
        }
        if (context instanceof Activity) {
            final Activity activity = (Activity) context;
            if (activity.isDestroyed() || activity.isFinishing()) {
                return false;
            }
        }
        return true;
    }
}
