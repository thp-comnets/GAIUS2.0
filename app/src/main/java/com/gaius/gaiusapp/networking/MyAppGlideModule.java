package com.gaius.gaiusapp.networking;

import android.content.Context;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator;
import com.bumptech.glide.module.AppGlideModule;

@GlideModule
public final class MyAppGlideModule extends AppGlideModule {
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        MemorySizeCalculator calculator = new MemorySizeCalculator.Builder(context).build();
        int defaultMemoryCacheSize = calculator.getMemoryCacheSize();
        int defaultBitmapPoolSize = calculator.getBitmapPoolSize();

        int customMemoryCacheSize = (int) (1.2 * defaultMemoryCacheSize);
        int customBitmapPoolSize = (int) (1.2 * defaultBitmapPoolSize);

        builder.setMemoryCache(new LruResourceCache(customMemoryCacheSize));
        builder.setBitmapPool(new LruBitmapPool(customBitmapPoolSize));

        int cacheSize100MegaBytes = 104857600;

        builder.setDiskCache(new InternalCacheDiskCacheFactory(context, cacheSize100MegaBytes));

//        builder.setDefaultRequestOptions(requestOptions());
//        builder.setDefaultTransitionOptions(Drawable.class, DrawableTransitionOptions.withCrossFade());
//        builder.setDefaultTransitionOptions(Bitmap.class, BitmapTransitionOptions.withCrossFade());

    }

//    private static RequestOptions requestOptions(){
//        return new RequestOptions()
//                .signature(new ObjectKey(
//                        System.currentTimeMillis() / (24 * 60 * 60 * 1000)))
//                .override(200, 200)
//                .centerCrop()
//                .placeholder(R.drawable.placeholder)
//                .encodeFormat(Bitmap.CompressFormat.PNG)
//                .encodeQuality(100)
//                .format(PREFER_ARGB_8888)
//                .timeout(10000)
//                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);
//    }
}