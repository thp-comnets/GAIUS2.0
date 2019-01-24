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
import com.gaius.gaiusapp.NewsFeed;
import com.gaius.gaiusapp.R;
import com.gaius.gaiusapp.RenderMAML;

import java.util.List;

import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;

public class NewsFeedAdapter extends RecyclerView.Adapter<NewsFeedAdapter.newsFeedViewHolder> {

    private Context mCtx;
    private List<NewsFeed> newsFeedList;

    public NewsFeedAdapter(Context mCtx, List<NewsFeed> newsFeedList) {
        this.mCtx = mCtx;
        this.newsFeedList = newsFeedList;
    }

    @Override
    public newsFeedViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.newsfeed_list, null);
        return new newsFeedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(newsFeedViewHolder holder, int position) {
        NewsFeed newsfeed = newsFeedList.get(position);

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.error(R.drawable.ic_avatar);

        if (newsfeed.getAvatar().contains("None")) {
            //loading the image
            Glide.with(mCtx)
                    .load(R.drawable.ic_avatar)
                    .into(holder.avatarView);
        }
        else{
            //loading the image
            Glide.with(mCtx)
                    .setDefaultRequestOptions(requestOptions)
                    .load(newsfeed.getAvatar())
//                .apply(new RequestOptions().signature(new ObjectKey("signature string")))
                    .apply(new RequestOptions().signature(new ObjectKey(System.currentTimeMillis())))
                    .into(holder.avatarView);
        }


        if (newsfeed.getType().contains("page")) {
            holder.imageView.setVisibility(View.VISIBLE);
            holder.videoView.setVisibility(View.GONE);

            requestOptions = new RequestOptions();
            //loading the image
            Glide.with(mCtx)
                    .setDefaultRequestOptions(requestOptions)
                    .load(newsfeed.getImage())
//                    .apply(new RequestOptions().signature(new ObjectKey("signature string")))
                    .apply(new RequestOptions().signature(new ObjectKey(System.currentTimeMillis())))
                    .into(holder.imageView);
            Log.d("yasir", "image "+newsfeed.getImage());
        }
        else {
            holder.videoView.setVisibility(View.VISIBLE);
            holder.imageView.setVisibility(View.GONE);

            Glide.with(mCtx)
                    .load(newsfeed.getImage())
                    .apply(new RequestOptions().signature(new ObjectKey(System.currentTimeMillis())))
                    .into(holder.videoView.thumbImageView);
            holder.videoView.setUp("http://91.230.41.34:8080/test/"+newsfeed.getUrl(), "", Jzvd.SCREEN_WINDOW_NORMAL);
            Log.d("yasir", "video http://91.230.41.34:8080/test/"+newsfeed.getUrl());
//            drawVideo("http://91.230.41.34:8080/test/"+newsfeed.getUrl(),"http://91.230.41.34:8080/test/"+newsfeed.getImage(), holder.videoView);
        }


        holder.textViewName.setText(newsfeed.getName());
        holder.textViewUpdateTime.setText(newsfeed.getUpdateTime());
        holder.textViewTitle.setText(newsfeed.getTitle());
        holder.textViewDescription.setText(newsfeed.getDescription());

        if (newsfeed.getType().contains("page")) {
            holder.newsFeedCard.setTag(position);
            holder.newsFeedCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NewsFeed n = newsFeedList.get((Integer) v.getTag());

                    Bundle bundle = new Bundle();
                    Intent i = new Intent(mCtx, RenderMAML.class);

                    bundle.putSerializable("BASEURL", "http://91.230.41.34:8080/test/");
                    bundle.putSerializable("URL", n.getUrl());
                    i.putExtras(bundle);
                    mCtx.startActivity(i);

                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return newsFeedList.size();
    }

    class newsFeedViewHolder extends RecyclerView.ViewHolder {

        CardView newsFeedCard;
        TextView textViewName, textViewUpdateTime, textViewTitle, textViewDescription;
        ImageView avatarView, imageView;
        JzvdStd videoView;

        public newsFeedViewHolder(View itemView) {
            super(itemView);

            textViewName = itemView.findViewById(R.id.textViewName);
            textViewUpdateTime = itemView.findViewById(R.id.textViewUpdateTime);
            avatarView = itemView.findViewById(R.id.avatarView);
            imageView = itemView.findViewById(R.id.imageView);
            videoView = itemView.findViewById(R.id.videoView);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            newsFeedCard =  itemView.findViewById(R.id.imageViewCardView);
        }
    }

//    public void drawVideo (final String url, final String thumbnail, final JzvdStd jzVideoPlayerStandard) {
//
//        final InputStreamVolleyRequest request = new InputStreamVolleyRequest(Request.Method.GET, thumbnail,
//                new Response.Listener<byte[]>() {
//                    @Override
//                    public void onResponse(final byte[] response) {
//
//                        try {
//                            if (response != null) {
//                                Bitmap bmp = BitmapFactory.decodeByteArray(response, 0, response.length);
//                                String [] tmp = url.split("/");
//
//                                jzVideoPlayerStandard.setUp(url, "", Jzvd.SCREEN_WINDOW_NORMAL);
//                                jzVideoPlayerStandard.thumbImageView.setImageBitmap(bmp);
//
//                            }
//                        } catch (Exception e) {
//                            Log.d("yasir", "UNABLE TO DOWNLOAD FILE 2");
//                            e.printStackTrace();
//                        }
//
//                    }
//                }, new Response.ErrorListener() {
//
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                error.printStackTrace();
//            }
//        }, null);
//
//        request.setShouldCache(true);
//        mRequestQueue.add(request);
//    }
}
