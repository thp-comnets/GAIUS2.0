package com.gaius.gaiusapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.gaius.gaiusapp.ImageViewActivity;
import com.gaius.gaiusapp.R;

import java.util.ArrayList;


public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.albumViewHolder> {

    private Context mCtx;
    private ArrayList<String> imagesList;

    public AlbumAdapter(Context mCtx, ArrayList<String> imagesList) {
        this.mCtx = mCtx;
        this.imagesList = imagesList;
    }

    @NonNull
    @Override
    public AlbumAdapter.albumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("thomas", "on create");

        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.album_list, null);

        return new albumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumAdapter.albumViewHolder holder, int position) {
        final String imageURI = imagesList.get(position);
//        holder.setIsRecyclable(false);

        holder.imageView.setImageResource(R.drawable.ic_avatar);

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.error(R.drawable.ic_avatar);

        Glide.with(mCtx)
                .setDefaultRequestOptions(requestOptions)
                .load(imageURI)
//                .apply(new RequestOptions().signature(new ObjectKey("signature string")))
                .apply(new RequestOptions().signature(new ObjectKey(System.currentTimeMillis())))
                .into(holder.imageView);

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                Intent target = new Intent(mCtx, ImageViewActivity.class);
                bundle.putString("URL", imageURI);
                target.putExtras(bundle);
                mCtx.startActivity(target);
            }
        });

        Log.d("thomas", imageURI);
    }

    @Override
    public int getItemCount() {
        return imagesList.size();
    }

    public class albumViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;


        public albumViewHolder(View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.image_view);
        }
    }
}
