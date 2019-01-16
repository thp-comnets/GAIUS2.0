package com.gaiusnetworks.gaius;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

public class ContentsAdapter extends RecyclerView.Adapter<ContentsAdapter.ContentViewHolder> {

    private Context mCtx;
    private List<Content> contentsList;

    public ContentsAdapter(Context mCtx, List<Content> contentsList) {
        this.mCtx = mCtx;
        this.contentsList = contentsList;
    }

    @Override
    public ContentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.content_list, null);
        return new ContentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ContentViewHolder holder, int position) {
        Content content = contentsList.get(position);

        holder.textViewTitle.setText(content.getTitle());
        holder.imageView.setImageResource(content.getImage());

        holder.cardView.setTag(position);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Content c = contentsList.get((Integer) v.getTag());

                if (c.getTitle().contains("Web")) {
                    Fragment fragment = new WebFragment();

                    ((AppCompatActivity) mCtx).getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .commit();
                }
                else if (c.getTitle().contains("Videos")) {
                    Fragment fragment = new VideosFragment();

                    ((AppCompatActivity) mCtx).getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .commit();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return contentsList.size();
    }

    class ContentViewHolder extends RecyclerView.ViewHolder {

        TextView textViewTitle;
        ImageView imageView;
        CardView cardView;

        public ContentViewHolder(View itemView) {
            super(itemView);

            textViewTitle = itemView.findViewById(R.id.content_title);
            imageView = itemView.findViewById(R.id.content_animation);
            cardView = itemView.findViewById(R.id.content_card);
        }
    }
}
