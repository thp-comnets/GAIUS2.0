package com.gaius.gaiusapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gaius.gaiusapp.AlbumViewActivity;
import com.gaius.gaiusapp.R;
import com.gaius.gaiusapp.classes.Image;
import com.gaius.gaiusapp.networking.GlideApp;

import java.util.ArrayList;
import java.util.List;

import ss.com.bannerslider.Slider;
import ss.com.bannerslider.adapters.SliderAdapter;
import ss.com.bannerslider.event.OnSlideClickListener;
import ss.com.bannerslider.viewholder.ImageSlideViewHolder;

public class BrowseImagesAdapter extends RecyclerView.Adapter<BrowseImagesAdapter.ImageViewHolder> {
    private Context mCtx;
    private List<Image> imagesList;
    private SharedPreferences prefs;

    public BrowseImagesAdapter(Context mCtx, List<Image> imagesList) {
        this.mCtx = mCtx;
        this.imagesList = imagesList;
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.image_list, null);

        prefs = PreferenceManager.getDefaultSharedPreferences(mCtx);

        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ImageViewHolder holder, int position) {
        Image image = imagesList.get(position);

        if (image.getAvatar().contains("None")) {
            holder.avatarView.setImageDrawable(mCtx.getResources().getDrawable(R.drawable.ic_avatar));
        }
        else{
            GlideApp.with(mCtx)
                    .load(prefs.getString("base_url", null) + image.getAvatar())
                    .avatar()
                    .into(holder.avatarView);
        }

        holder.setIsRecyclable(false);

        holder.multiImageViewBitmaps = image.getImagesGallery();
        holder.slider.setAdapter(new SliderAdapter() {
            @Override
            public int getItemCount() {
                return holder.multiImageViewBitmaps.size();
            }

            @Override
            public void onBindImageSlide(int position, ImageSlideViewHolder imageSlideViewHolder) {
                imageSlideViewHolder.bindImageSlide(holder.multiImageViewBitmaps.get(position));
            }
        });

        holder.slider.setSelectedSlide(0);
        holder.slider.setInterval(2000);
        holder.slider.setOnSlideClickListener(new OnSlideClickListener() {
            @Override
            public void onSlideClick(int position) {
                Bundle bundle = new Bundle();
                Intent i = new Intent(mCtx, AlbumViewActivity.class);
                bundle.putStringArrayList("imagesURLs", holder.multiImageViewBitmaps);
                i.putExtras(bundle);
                mCtx.startActivity(i);
            }
        });

//        HashMap<String,String> url_maps = new HashMap<String, String>();
//        holder.multiImageViewBitmaps = image.getImagesGallery();
//        for (int i=0; i<holder.multiImageViewBitmaps.size(); i++) {
//            url_maps.put(i+"", holder.multiImageViewBitmaps.get(i));
//        }
//
//        for(String name : url_maps.keySet()) {
//            final TextSliderView textSliderView = new TextSliderView(mCtx);
//            // initialize a SliderLayout
//            textSliderView
//                    .image(url_maps.get(name))
//                    .setScaleType(BaseSliderView.ScaleType.CenterCrop)
//                    .setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
//                        @Override
//                        public void onSliderClick(BaseSliderView slider) {
//                            Bundle bundle = new Bundle();
//                            Intent i = new Intent(mCtx, AlbumViewActivity.class);
//                            bundle.putStringArrayList("imagesURLs", holder.multiImageViewBitmaps);
//                            i.putExtras(bundle);
//                            mCtx.startActivity(i);
//                        }
//                    });
//
//            holder.mDemoSlider.addSlider(textSliderView);
//        }

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
//        SliderLayout mDemoSlider;
        Slider slider;
        ArrayList<String> multiImageViewBitmaps;

        public ImageViewHolder(View itemView) {
            super(itemView);

            textViewUpdateTime = itemView.findViewById(R.id.textViewUpdateTime);
            avatarView = itemView.findViewById(R.id.avatarView);
//            mDemoSlider = itemView.findViewById(R.id.slider);
            slider = itemView.findViewById(R.id.slider);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            videoCard =  itemView.findViewById(R.id.imageViewCardView);
        }
    }
}
