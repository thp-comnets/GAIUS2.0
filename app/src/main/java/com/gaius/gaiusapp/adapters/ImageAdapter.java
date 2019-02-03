package com.gaius.gaiusapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.gaius.gaiusapp.AlbumViewActivity;
import com.gaius.gaiusapp.R;
import com.gaius.gaiusapp.classes.Image;
import com.gaius.gaiusapp.classes.Video;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private Context mCtx;
    private List<Image> imagesList;

    public ImageAdapter(Context mCtx, List<Image> imagesList) {
        this.mCtx = mCtx;
        this.imagesList = imagesList;
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.image_list, null);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ImageViewHolder holder, int position) {
        Image image = imagesList.get(position);

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.error(R.drawable.ic_avatar);

        if (image.getAvatar().contains("None")) {
            //loading the image
            Glide.with(mCtx)
                    .load(R.drawable.ic_avatar)
                    .into(holder.avatarView);
        }
        else{
            //loading the image
            Glide.with(mCtx)
                    .setDefaultRequestOptions(requestOptions)
                    .load(image.getAvatar())
                    .apply(new RequestOptions().signature(new ObjectKey(System.currentTimeMillis())))
                    .into(holder.avatarView);
        }

        holder.setIsRecyclable(false);

        HashMap<String,String> url_maps = new HashMap<String, String>();
        holder.multiImageViewBitmaps = image.getImagesGallery();
        for (int i=0; i<holder.multiImageViewBitmaps.size(); i++) {
            url_maps.put(i+"", holder.multiImageViewBitmaps.get(i));
        }

        for(String name : url_maps.keySet()) {
            final TextSliderView textSliderView = new TextSliderView(mCtx);
            // initialize a SliderLayout
            textSliderView
                    .image(url_maps.get(name))
                    .setScaleType(BaseSliderView.ScaleType.CenterCrop)
                    .setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                        @Override
                        public void onSliderClick(BaseSliderView slider) {
                            Bundle bundle = new Bundle();
                            Intent i = new Intent(mCtx, AlbumViewActivity.class);
                            bundle.putStringArrayList("imagesURLs", holder.multiImageViewBitmaps);
                            i.putExtras(bundle);
                            mCtx.startActivity(i);
                        }
                    });

            holder.mDemoSlider.addSlider(textSliderView);
        }

        holder.textViewUpdateTime.setText(image.getUploadedSince());
        holder.textViewTitle.setText(image.getTitle());
        holder.textViewDescription.setText(image.getDescription());
    }

    @Override
    public int getItemCount() {
        return imagesList.size();
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {

        CardView videoCard;
        TextView textViewUpdateTime, textViewTitle, textViewDescription;
        ImageView avatarView;
        SliderLayout mDemoSlider;
        ArrayList<String> multiImageViewBitmaps;

        public ImageViewHolder(View itemView) {
            super(itemView);

            textViewUpdateTime = itemView.findViewById(R.id.textViewUpdateTime);
            avatarView = itemView.findViewById(R.id.avatarView);
            mDemoSlider = itemView.findViewById(R.id.slider);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            videoCard =  itemView.findViewById(R.id.imageViewCardView);
        }
    }
}
