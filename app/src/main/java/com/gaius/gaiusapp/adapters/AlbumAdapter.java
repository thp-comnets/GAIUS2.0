package com.gaius.gaiusapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.gaius.gaiusapp.ImageViewActivity;
import com.gaius.gaiusapp.R;
import com.gaius.gaiusapp.networking.GlideApp;

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

        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.album_list, null);

        return new albumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumAdapter.albumViewHolder holder, final int position) {
        final String imageURI = imagesList.get(position);
        holder.setIsRecyclable(false);

        GlideApp.with(mCtx)
                .load(imageURI)
                .content()
                .into(holder.imageView);

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                Intent target = new Intent(mCtx, ImageViewActivity.class);
                bundle.putString("URL", imageURI);
                bundle.putStringArrayList("URLs", imagesList);
                bundle.putInt("position", position);
                target.putExtras(bundle);
                mCtx.startActivity(target);
            }
        });

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
