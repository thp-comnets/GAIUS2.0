package com.gaius.gaiusapp.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.gaius.gaiusapp.AlbumViewActivity;
import com.gaius.gaiusapp.CreativeWebCreation;
import com.gaius.gaiusapp.VideoViewActivity;
import com.gaius.gaiusapp.classes.Content;
import com.gaius.gaiusapp.R;

import java.util.List;

import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;

import static com.gaius.gaiusapp.utils.ResourceHelper.convertImageURLBasedonFidelity;

public class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.ContentlViewHolder> {
    private Context mCtx;
    private List<Content> contentsList;
    private SharedPreferences prefs;

    public ContentAdapter(Context mCtx, List<Content> contentsList) {
        this.mCtx = mCtx;
        this.contentsList = contentsList;
    }

    @Override
    public ContentlViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.content_list, null);

        prefs = PreferenceManager.getDefaultSharedPreferences(mCtx);

        return new ContentAdapter.ContentlViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ContentAdapter.ContentlViewHolder holder, int position) {
        final Content content = contentsList.get(position);

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.error(R.drawable.ic_avatar);

        String fidelity = prefs.getString("fidelity_level", "high");

        if (content.getThumbnail().contains("None")) {
            //loading the image
            Glide.with(mCtx)
                    .load(R.drawable.ic_avatar)
                    .into(holder.imageView);
        }
        else {
            //loading the image
            Glide.with(mCtx)
                    .setDefaultRequestOptions(requestOptions)
                    .load(convertImageURLBasedonFidelity(prefs.getString("base_url", null) + content.getThumbnail(), fidelity))
                    .into(holder.imageView);
        }

        holder.imageStats.setVisibility(View.GONE);
        holder.textStats.setVisibility(View.GONE);
        holder.videoStats.setVisibility(View.GONE);
        holder.videoCardView.setVisibility(View.GONE);

        if (content.getType().equals("video")) {
            holder.typeView.setImageResource(R.drawable.ic_video_create);
            holder.editButton.setVisibility(View.GONE);
        }
        else if (content.getType().equals("page")) {

            holder.editButton.setVisibility(View.VISIBLE);
            holder.typeView.setImageResource(R.drawable.ic_simple_creation);

            holder.editButton.setTag(position);
            holder.editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Content c = contentsList.get((Integer) v.getTag());

                    Intent intent = new Intent(mCtx, CreativeWebCreation.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("PAGE_URL", c.getUrl());
                    bundle.putSerializable("EDIT_MODE", true);
                    bundle.putSerializable("PAGE_NAME", c.getTitle());
                    bundle.putSerializable("PAGE_DESCRIPTION", c.getDescription());
                    intent.putExtras(bundle);

                    mCtx.startActivity(intent);
                }
            });
        }
        else if (content.getType().equals("image")) {
            holder.typeView.setImageResource(R.drawable.images_app);
            holder.editButton.setVisibility(View.GONE);
        }
        else if (content.getType().equals("ad")) {
            holder.typeView.setImageResource(R.drawable.ic_ad_create);
            holder.editButton.setVisibility(View.GONE);

            holder.imageStats.setVisibility(View.VISIBLE);
            holder.textStats.setVisibility(View.VISIBLE);

            if (!content.getUrl().equals("")) {
                holder.videoCardView.setVisibility(View.VISIBLE);
                holder.videoStats.setVisibility(View.VISIBLE);

                Glide.with(mCtx)
                        .setDefaultRequestOptions(requestOptions)
                        .load(convertImageURLBasedonFidelity(prefs.getString("base_url", null) + content.getUrl(), fidelity))
                        .into(holder.videoView);
//                holder.videoView.setUp(prefs.getString("base_url", null) + content.getUrl(), "", Jzvd.SCREEN_WINDOW_NORMAL);
                holder.videoView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        Intent i = new Intent(mCtx, VideoViewActivity.class);
                        bundle.putString("URL", content.getUrl());
                        i.putExtras(bundle);
                        mCtx.startActivity(i);
                    }
                });
            }

            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("ad", content.getThumbnail());

                    Intent target = new Intent(Intent.ACTION_VIEW);
                    target.setDataAndType(Uri.parse(prefs.getString("base_url", null) + content.getThumbnail()), "image/*");
                    target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    mCtx.startActivity(target);
                }
            });

            holder.textViewed.setText(content.getTextViewed());
            holder.imageViewed.setText(content.getImageViewed());
            holder.videoViewed.setText(content.getVideoViewed());
            holder.textClicked.setText(content.getTextClicked());
            holder.imageClicked.setText(content.getImageClicked());
            holder.videoClicked.setText(content.getVideoClicked());

        }

        holder.textViewTitle.setText(content.getTitle());
        holder.getTextViewDescription.setText(content.getDescription());
        switch (content.getPublished()) {
            case "0":
                holder.status.setText("Saved");
                holder.status.setTextColor(mCtx.getResources().getColor(R.color.blue_500));
                break;
            case "1":
                holder.status.setText("Published");
                holder.status.setTextColor(mCtx.getResources().getColor(R.color.green_500));
                break;
            case "-1":
                holder.status.setText("Pending approval");
                holder.status.setTextColor(mCtx.getResources().getColor(R.color.orange_500));
                break;
        }


        holder.deleteButton.setTag(position);
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Content c = contentsList.get((Integer) v.getTag());


                AlertDialog.Builder builder = new AlertDialog.Builder(mCtx);

                builder.setTitle("Delete Content");
                builder.setMessage("Do you want to delete this "+c.getType()+"?");

                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing but close the dialog
                        dialog.dismiss();

                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mCtx);
                        String token = prefs.getString("token", "null");
                        String URL = prefs.getString("base_url", null) + "deleteContent.py?token=" + token + "&" + c.getType() + "=" + c.getUrl();

                        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        Log.d("yasir", response);

                                        if (response.contains("Success")) {
                                            contentsList.remove(c);
                                            notifyDataSetChanged();
                                        }
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Log.d("Yasir","Error "+error);
                                    }
                                });
                        Log.d("Yasir","added request "+stringRequest);

                        Volley.newRequestQueue(mCtx).add(stringRequest);
                    }
                });

                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                        dialog.dismiss();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return contentsList.size();
    }

    class ContentlViewHolder extends RecyclerView.ViewHolder {

        TextView textViewTitle, getTextViewDescription, status;
        ImageView imageView, typeView, deleteButton, editButton, videoView;
        CardView videoCardView;
        LinearLayout imageStats, videoStats, textStats;
        TextView textViewed, imageViewed, videoViewed, textClicked, imageClicked, videoClicked;

        public ContentlViewHolder(View itemView) {
            super(itemView);

            textViewTitle = itemView.findViewById(R.id.textViewName);
            status = itemView.findViewById(R.id.status);
            getTextViewDescription = itemView.findViewById(R.id.textViewDescription);
            imageView = itemView.findViewById(R.id.imageView);
            typeView = itemView.findViewById(R.id.typeView);
            deleteButton = itemView.findViewById(R.id.binButton);
            editButton = itemView.findViewById(R.id.editButton);
            videoCardView = itemView.findViewById(R.id.cardview2);
            videoView = itemView.findViewById(R.id.videoView);

            imageStats = itemView.findViewById(R.id.image_ad_stats);
            videoStats = itemView.findViewById(R.id.video_ad_stats);
            textStats = itemView.findViewById(R.id.text_ad_stats);

            textViewed = itemView.findViewById(R.id.text_ad_viewed);
            imageViewed = itemView.findViewById(R.id.image_ad_viewed);
            videoViewed = itemView.findViewById(R.id.video_ad_viewed);
            textClicked = itemView.findViewById(R.id.text_ad_clicked);
            imageClicked = itemView.findViewById(R.id.image_ad_clicked);
            videoClicked = itemView.findViewById(R.id.video_ad_clicked);
        }
    }
}