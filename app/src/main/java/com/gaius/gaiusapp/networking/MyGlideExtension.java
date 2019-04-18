package com.gaius.gaiusapp.networking;

import android.support.annotation.NonNull;

import com.bumptech.glide.Priority;
import com.bumptech.glide.annotation.GlideExtension;
import com.bumptech.glide.annotation.GlideOption;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.BaseRequestOptions;
import com.gaius.gaiusapp.R;

import jp.wasabeef.glide.transformations.CropTransformation;

@GlideExtension
public class MyGlideExtension {

    private MyGlideExtension() {}

    @NonNull
    @GlideOption
    public static BaseRequestOptions<?> avatar(BaseRequestOptions<?> options) {
        return options.placeholder(R.drawable.ic_avatar)
                .timeout(10000)
                .priority(Priority.IMMEDIATE)
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);
    }

    @NonNull
    @GlideOption
    public static BaseRequestOptions<?> content(BaseRequestOptions<?> options) {
        return options.placeholder(R.drawable.placeholder)
                .timeout(10000)
                .transform(new CropTransformation(0,0,CropTransformation.CropType.TOP)) //required to make sure the page image is not messed up
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);
    }
}