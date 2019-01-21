package com.gaiusnetworks.gaius;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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

                if (c.getTitle().contains("Browse Web")) {
                    Fragment fragment = new WebFragment();

                    ((AppCompatActivity) mCtx).getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .commit();
                }
                else if (c.getTitle().contains("Browse Videos")) {
                    Fragment fragment = new VideosFragment();

                    ((AppCompatActivity) mCtx).getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .commit();
                }
                else if (c.getTitle().contains("Create Content")) {
                    displayIntentOptions();
                }
            }
        });
    }

    private void displayIntentOptions(){

        LayoutInflater layoutInflater = LayoutInflater.from(mCtx);
        View promptView = layoutInflater.inflate(R.layout.create_content_popup, null);
        final AlertDialog alertD = new AlertDialog.Builder(mCtx).create();
        alertD.setView(promptView);
        alertD.show();

        CardView web_button = (CardView) promptView.findViewById(R.id.web_card);
        CardView video_button = (CardView) promptView.findViewById(R.id.video_card);
        CardView image_button = (CardView) promptView.findViewById(R.id.image_card);
        CardView ad_button = (CardView) promptView.findViewById(R.id.ad_card);

        web_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Yasir", "clicked on web creation");

                Intent i = new Intent(mCtx, creativeWebCreation.class);
                mCtx.startActivity(i);
                alertD.dismiss();

            }
        });
        video_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Yasir", "clicked on video creation");
                alertD.dismiss();
            }
        });
        image_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Yasir", "clicked on image creation");
                alertD.dismiss();
            }
        });
        ad_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Yasir", "clicked on ad creation");
                alertD.dismiss();
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
