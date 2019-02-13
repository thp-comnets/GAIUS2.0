package com.gaius.gaiusapp.adapters;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;

import java.util.List;
import io.brotherjing.galleryview.GalleryAdapter;

public class UrlGalleryAdapter extends GalleryAdapter {

    private List<String> data;

    public UrlGalleryAdapter(Context context, List<String> data) {
        super(context);
        this.data = data;
    }

    @Override
    public int getInitPicIndex() {
        return 0;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public void fillViewAtPosition(int position, ImageView imageView) {
        String url = data.get(position);
        imageView.setImageURI(Uri.parse(url));
    }
}