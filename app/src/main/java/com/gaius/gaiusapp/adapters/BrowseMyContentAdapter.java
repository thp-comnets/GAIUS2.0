package com.gaius.gaiusapp.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.gaius.gaiusapp.CreativeWebCreation;
import com.gaius.gaiusapp.ImageViewActivity;
import com.gaius.gaiusapp.R;
import com.gaius.gaiusapp.VideoViewActivity;
import com.gaius.gaiusapp.classes.Content;
import com.gaius.gaiusapp.networking.GlideApp;

import java.util.ArrayList;
import java.util.List;

import static com.gaius.gaiusapp.utils.ResourceHelper.convertImageURLBasedonFidelity;

public class BrowseMyContentAdapter extends RecyclerView.Adapter<BrowseMyContentAdapter.ContentlViewHolder> {
    private Context mCtx;
    private List<Content> contentsList;
    private SharedPreferences prefs;

    public BrowseMyContentAdapter(Context mCtx, List<Content> contentsList) {
        this.mCtx = mCtx;
        this.contentsList = contentsList;
    }

    @Override
    public ContentlViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.content_list, null);

        prefs = PreferenceManager.getDefaultSharedPreferences(mCtx);

        return new BrowseMyContentAdapter.ContentlViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final BrowseMyContentAdapter.ContentlViewHolder holder, int position) {
        final Content content = contentsList.get(position);

        final String fidelity = prefs.getString("fidelity_level", "high");

        if (content.getThumbnail().contains("None")) {
            //FIXME: are we ever coming here? Shouldn't we always have a thumbnail?
            holder.imageView.setImageDrawable(mCtx.getResources().getDrawable(R.drawable.placeholder));
        }
        else {
            if (content.getType().equals("ad")) {
                GlideApp.with(mCtx)
                        .load(prefs.getString("base_url", null) + content.getThumbnail())
                        .content()
                        .into(holder.imageView);
            }
            else {
                GlideApp.with(mCtx)
                        .load(convertImageURLBasedonFidelity(prefs.getString("base_url", null) + content.getThumbnail(), fidelity))
                        .content()
                        .into(holder.imageView);
            }
        }

        holder.imageStats.setVisibility(View.GONE);
        holder.textStats.setVisibility(View.GONE);
        holder.videoStats.setVisibility(View.GONE);
        holder.videoCardView.setVisibility(View.GONE);
        holder.imageView.setOnClickListener(null);

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

                GlideApp.with(mCtx)
//                        .load(convertImageURLBasedonFidelity(prefs.getString("base_url", null) + content.getUrl(), fidelity))
                        .load(prefs.getString("base_url", null) + content.getUrl())
                        .content()
                        .into(holder.videoView);

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
//                    Intent target = new Intent(Intent.ACTION_VIEW);
//                    target.setDataAndType(Uri.parse(prefs.getString("base_url", null) + content.getThumbnail()), "image/*");
//                    target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
//                    mCtx.startActivity(target);

                    Bundle bundle = new Bundle();
                    Intent target = new Intent(mCtx, ImageViewActivity.class);

                    ArrayList<String> imgList = new ArrayList<>();
                    imgList.add(prefs.getString("base_url", null) + content.getThumbnail());
                    bundle.putStringArrayList("URLs", imgList);
                    bundle.putInt("position", 0);

                    target.putExtras(bundle);
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
                        String URL;

                        // Do nothing but close the dialog
                        dialog.dismiss();

                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mCtx);
                        String token = prefs.getString("token", "null");

                        if (c.getType().equals("ad")) {
                            URL = prefs.getString("base_url", null) + "deleteContent.py?token=" + token + "&" + c.getType() + "=" + c.getAdCampaign();
                        }
                        else {
                            URL = prefs.getString("base_url", null) + "deleteContent.py?token=" + token + "&" + c.getType() + "=" + c.getUrl();
                        }

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