package com.gaius.gaiusapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
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
import com.gaius.gaiusapp.utils.ResourceHelper;

import java.io.IOException;
import java.util.List;

import static com.gaius.gaiusapp.utils.ResourceHelper.getResizedBitmap;

class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.ContentlViewHolder> {
    private Context mCtx;
    private List<Content> contentsList;

    public ContentAdapter(Context mCtx, List<Content> contentsList) {
        this.mCtx = mCtx;
        this.contentsList = contentsList;
    }

    @Override
    public ContentlViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.content_list, null);
        return new ContentAdapter.ContentlViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ContentAdapter.ContentlViewHolder holder, int position) {
        Content content = contentsList.get(position);

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.error(R.drawable.ic_avatar);

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
                    .load(content.getThumbnail())
                    .into(holder.imageView);
        }

        if (content.getType().equals("video")) {
            holder.typeView.setImageResource(R.drawable.ic_video_create);
        }
        else if (content.getType().equals("page")) {
            holder.typeView.setImageResource(R.drawable.ic_web_animation);
        }

        holder.textViewTitle.setText(content.getTitle());
        holder.getTextViewDescription.setText(content.getDescription());

        holder.channelItem.setTag(position);
        holder.channelItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Content c = contentsList.get((Integer) v.getTag());
                Bundle bundle = new Bundle();

//                if (!c.getUserID().contains("null")) {
//                    Fragment fragment = new WebFragment();
//                    bundle.putString("userID", c.getUserID());
//                    fragment.setArguments(bundle);
//
//                    ((AppCompatActivity) mCtx).getSupportFragmentManager().beginTransaction()
//                            .replace(R.id.fragment_container, fragment)
//                            .commit();
//                }
//                else {
//                    Intent i = new Intent(mCtx, RenderMAML.class);
//
//                    bundle.putSerializable("BASEURL", "http://91.230.41.34:8080/test/");
//                    bundle.putSerializable("URL", c.getUrl());
//                    i.putExtras(bundle);
//                    mCtx.startActivity(i);
//                }
            }
        });

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
                        String URL = "http://91.230.41.34:8080/test/deleteContent.py?token=" + token + "&" + c.getType() + "=" + c.getUrl();

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

        TextView textViewTitle, getTextViewDescription;
        ImageView imageView, typeView, deleteButton;
        LinearLayout channelItem;

        public ContentlViewHolder(View itemView) {
            super(itemView);

            textViewTitle = itemView.findViewById(R.id.textViewName);
            getTextViewDescription = itemView.findViewById(R.id.textViewDescription);
            imageView = itemView.findViewById(R.id.imageView);
            typeView = itemView.findViewById(R.id.typeView);
            channelItem = itemView.findViewById(R.id.channelItem);
            deleteButton = itemView.findViewById(R.id.binButton);
        }
    }
}