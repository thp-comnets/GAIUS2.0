package com.gaius.gaiusapp.networking;

import android.support.annotation.NonNull;

import com.bumptech.glide.Priority;
import com.bumptech.glide.annotation.GlideExtension;
import com.bumptech.glide.annotation.GlideOption;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.gaius.gaiusapp.R;

@GlideExtension
public class MyGlideExtension {

    private MyGlideExtension() {}

    @NonNull
    @GlideOption
    public static RequestOptions avatar(RequestOptions options) {
        return options.placeholder(R.drawable.ic_avatar)
                .timeout(10000)
                .priority(Priority.IMMEDIATE)
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);
    }

    @NonNull
    @GlideOption
    public static RequestOptions content(RequestOptions options) {
        return options.placeholder(R.drawable.placeholder)
                .timeout(10000)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);
    }
}