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
import com.gaius.gaiusapp.AlbumViewActivity;
import com.gaius.gaiusapp.R;
import com.gaius.gaiusapp.RenderMAMLActivity;
import com.gaius.gaiusapp.VideoViewActivity;
import com.gaius.gaiusapp.classes.Content;
import com.gaius.gaiusapp.networking.GlideApp;

import java.util.List;

import static com.gaius.gaiusapp.utils.ResourceHelper.convertImageURLBasedonFidelity;

public class ApproveContentAdapter extends RecyclerView.Adapter<ApproveContentAdapter.ApproveContentViewHolder> {
    private Context mCtx;
    private List<Content> contentsList;
    private SharedPreferences prefs;

    public ApproveContentAdapter(Context mCtx, List<Content> contentsList) {
        this.mCtx = mCtx;
        this.contentsList = contentsList;
    }

    @Override
    public ApproveContentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.content_list, null);

        prefs = PreferenceManager.getDefaultSharedPreferences(mCtx);

        return new ApproveContentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ApproveContentViewHolder holder, int position) {
        final Content content = contentsList.get(position);

        String fidelity = prefs.getString("fidelity_level", "high");

        if (content.getThumbnail().contains("None")) {
            //FIXME: are we ever coming here? Shouldn't we always have a thumbnail?
            holder.imageView.setImageDrawable(mCtx.getResources().getDrawable(R.drawable.placeholder));
        }
        else {
            GlideApp.with(mCtx)
                    .load(convertImageURLBasedonFidelity(prefs.getString("base_url", null) + content.getThumbnail(), fidelity))
                    .content()
                    .into(holder.imageView);
        }

        holder.imageStats.setVisibility(View.GONE);
        holder.textStats.setVisibility(View.GONE);
        holder.videoStats.setVisibility(View.GONE);
        holder.videoCardView.setVisibility(View.GONE);

        holder.textViewTitle.setText(content.getTitle());
        holder.getTextViewDescription.setText(content.getDescription());

        holder.status.setText(content.getName() + " " + content.getUploadTime());
        holder.status.setTextColor(mCtx.getResources().getColor(R.color.blue_500));

        //TODO the icons need to be properly aligned
        if (content.getType().equals("video")) {
            holder.typeView.setImageResource(R.drawable.create_content_video);
        } else if (content.getType().equals("page")) {
            holder.typeView.setImageResource(R.drawable.create_content_web);
        } else if (content.getType().equals("image")) {
            holder.typeView.setImageResource(R.drawable.create_content_image);
        } else if (content.getType().equals("ad")) {
                holder.typeView.setImageResource(R.drawable.create_content_ad);

                holder.imageStats.setVisibility(View.VISIBLE);
                holder.textStats.setVisibility(View.VISIBLE);

                if (!content.getUrl().equals("")) {
                    holder.videoCardView.setVisibility(View.VISIBLE);
                    holder.videoStats.setVisibility(View.VISIBLE);

                    GlideApp.with(mCtx)
                            .load(convertImageURLBasedonFidelity(prefs.getString("base_url", null) + content.getUrl(), fidelity))
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

        holder.linearLayout.setTag(position);
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Content c = contentsList.get((Integer) v.getTag());
                Bundle bundle = new Bundle();
                Intent i=null;

                if (content.getType().equals("ad")) {
                    //clicks are handled by their individual views
                    return;
                }
                if (content.getType().equals("page")) {
                    i = new Intent(mCtx, RenderMAMLActivity.class);
                    bundle.putSerializable("BASEURL", prefs.getString("base_url", null));
                    bundle.putString("title", content.getTitle());
                }
                else if (content.getType().equals("image")) {
                    i = new Intent(mCtx, AlbumViewActivity.class);
                }
                else if (content.getType().equals("video")) {
                    i = new Intent(mCtx, VideoViewActivity.class);
                }

                bundle.putSerializable("URL", c.getUrl());
                i.putExtras(bundle);
                mCtx.startActivity(i);
            }
        });


        holder.rejectButton.setTag(position);
        holder.rejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Content c = contentsList.get((Integer) v.getTag());

                AlertDialog.Builder builder = new AlertDialog.Builder(mCtx);

                builder.setTitle("Reject Content");
                builder.setMessage("Do you want to reject this "+c.getType()+"?");

                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mCtx);
                        String token = prefs.getString("token", "null");
                        String URL;
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

        holder.approveButton.setTag(position);
        holder.approveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Content c = contentsList.get((Integer) v.getTag());

                AlertDialog.Builder builder = new AlertDialog.Builder(mCtx);

                builder.setTitle("Approve Content");
                builder.setMessage("Do you want to approve this "+c.getType()+"?");

                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing but close the dialog
                        dialog.dismiss();

                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mCtx);
                        String token = prefs.getString("token", "null");
                        String URL;
                        if (c.getType().equals("ad")) {
                            URL = prefs.getString("base_url", null) + "approveContent.py?token=" + token + "&" + c.getType() + "=" + c.getAdCampaign();
                        } else {
                            URL = prefs.getString("base_url", null) + "approveContent.py?token=" + token + "&" + c.getType() + "=" + c.getUrl();
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

    class ApproveContentViewHolder extends RecyclerView.ViewHolder {

        TextView textViewTitle, getTextViewDescription, status;
        ImageView imageView, typeView, rejectButton, approveButton, videoView;
        CardView videoCardView;
        LinearLayout linearLayout, imageStats, videoStats, textStats;
        TextView textViewed, imageViewed, videoViewed, textClicked, imageClicked, videoClicked;


        public ApproveContentViewHolder(View itemView) {
            super(itemView);

            textViewTitle = itemView.findViewById(R.id.textViewName);
            status = itemView.findViewById(R.id.status);
            getTextViewDescription = itemView.findViewById(R.id.textViewDescription);
            imageView = itemView.findViewById(R.id.imageView);
            typeView = itemView.findViewById(R.id.typeView);
            rejectButton = itemView.findViewById(R.id.rejectButton);
            approveButton = itemView.findViewById(R.id.approveButton);
            linearLayout = itemView.findViewById(R.id.channelItem);

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