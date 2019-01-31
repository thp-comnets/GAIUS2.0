package com.gaius.gaiusapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.gaius.gaiusapp.RenderPhotoActivity;
import com.gaius.gaiusapp.classes.NewsFeed;
import com.gaius.gaiusapp.R;
import com.gaius.gaiusapp.RenderMAML;
import com.veinhorn.scrollgalleryview.ScrollGalleryView;
import com.veinhorn.scrollgalleryview.builder.GallerySettings;

import java.util.ArrayList;
import java.util.List;

import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;

import static com.veinhorn.scrollgalleryview.loader.picasso.dsl.DSL.image;

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
    public void onBindViewHolder(final newsFeedViewHolder holder, int position) {
        NewsFeed newsfeed = newsFeedList.get(position);

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.error(R.drawable.ic_avatar);

        if (newsfeed.getShowAvatar() == true) {
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
        }
        else {
            holder.avatarView.setVisibility(View.GONE);
            holder.textViewName.setVisibility(View.GONE);
        }



        if (newsfeed.getType().contains("page")) {
            holder.imageView.setVisibility(View.VISIBLE);
            holder.videoView.setVisibility(View.GONE);
            holder.scrollGalleryView.setVisibility(View.GONE);

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
        else if (newsfeed.getType().equals("video")) {
            holder.videoView.setVisibility(View.VISIBLE);
            holder.imageView.setVisibility(View.GONE);
            holder.scrollGalleryView.setVisibility(View.GONE);

            Glide.with(mCtx)
                    .load(newsfeed.getImage())
                    .apply(new RequestOptions().signature(new ObjectKey(System.currentTimeMillis())))
                    .into(holder.videoView.thumbImageView);
            holder.videoView.setUp("http://91.230.41.34:8080/test/"+newsfeed.getUrl(), "", Jzvd.SCREEN_WINDOW_NORMAL);
            Log.d("yasir", "video http://91.230.41.34:8080/test/"+newsfeed.getUrl());
//            drawVideo("http://91.230.41.34:8080/test/"+newsfeed.getUrl(),"http://91.230.41.34:8080/test/"+newsfeed.getImage(), holder.videoView);
        }
        else if (newsfeed.getType().equals("image")) {
            holder.videoView.setVisibility(View.GONE);
            holder.imageView.setVisibility(View.GONE);
            holder.scrollGalleryView.setVisibility(View.VISIBLE);

            holder.multiImageViewBitmaps = new ArrayList<>();
            holder.multiImageViewBitmaps.add("http://91.230.41.34:8080/test/content/680dd9321ef418b15f1aee30cc35b499/cropped7203186553735326789.jpg");
            holder.multiImageViewBitmaps.add("http://91.230.41.34:8080/test/content/680dd9321ef418b15f1aee30cc35b499/cropped1202584801526790588.jpg");
            holder.multiImageViewBitmaps.add("http://91.230.41.34:8080/test/content/680dd9321ef418b15f1aee30cc35b499/cropped2951741151875150408.jpg");

            ScrollGalleryView
                    .from(holder.scrollGalleryView)
                    .settings(
                            GallerySettings
                                    .from(((FragmentActivity)mCtx).getSupportFragmentManager())
                                    .thumbnailSize(100)
                                    .enableZoom(true)
                                    .build()
                    )
                    .build();

            for (int i=0; i < holder.multiImageViewBitmaps.size(); i++) {
                holder.scrollGalleryView.addMedia(image(holder.multiImageViewBitmaps.get(i)));
            }


            holder.scrollGalleryView.addOnImageClickListener(new ScrollGalleryView.OnImageClickListener() {
                @Override
                public void onClick() {
                    Bundle bundle = new Bundle();
                    bundle.putString("imageURL", holder.multiImageViewBitmaps.get(holder.scrollGalleryView.getCurrentItem()));
                    Intent i = new Intent(mCtx, RenderPhotoActivity.class);
                    i.putExtras(bundle);
                    mCtx.startActivity(i);
                }
            });
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

            holder.shareButton.setTag(position);
            holder.shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NewsFeed n = newsFeedList.get((Integer) v.getTag());
                    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                    sharingIntent.setType("text/plain");
                    String shareBody = "http://gaiusnetworks.com/page/"+n.getUrl().replace("./content/","");
                    String shareSub = "Check this page on GAIUS";
                    sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, shareSub);
                    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                    mCtx.startActivity(Intent.createChooser(sharingIntent, "Share using"));
                }
            });
        }
        else if (newsfeed.getType().contains("video")) {
            // video sharing

            holder.shareButton.setTag(position);
            holder.shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NewsFeed n = newsFeedList.get((Integer) v.getTag());
                    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                    sharingIntent.setType("text/plain");
                    String shareBody = "http://gaiusnetworks.com/"+n.getUrl();
                    String shareSub = "Check this video on GAIUS";
                    sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, shareSub);
                    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                    mCtx.startActivity(Intent.createChooser(sharingIntent, "Share using"));
                }
            });
        }

        if (newsfeed.getLiked().equals("true")) {
            holder.likeButton.setImageResource(R.drawable.ic_liked);
        }

        holder.likeButton.setTag(position);
        holder.likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("yasir", "clicked on liked ");

                NewsFeed n = newsFeedList.get((Integer) v.getTag());
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mCtx);

                String url = " http://91.230.41.34:8080/test/like.py?url=" + n.getUrl() + "&token=" + prefs.getString("token", "null");
                StringRequest request = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                if (response.contains("Success") && response.contains("true")) {
//                                    mLikes.setText((Integer.parseInt(mLikes.getText().toString()) + 1) + "");
                                    holder.likeButton.setImageResource(R.drawable.ic_liked);
                                }
                                else if (response.contains("Success") && response.contains("false")) {
//                                    mLikes.setText((Integer.parseInt(mLikes.getText().toString()) - 1) + "");
                                    holder.likeButton.setImageResource(R.drawable.ic_like);
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("Yasir","Error "+error);
                            }
                        });

                Volley.newRequestQueue(mCtx).add(request);
            }
        });

    }

    @Override
    public int getItemCount() {
        return newsFeedList.size();
    }

    public NewsFeed getItem(int pos) {
        return newsFeedList.get(pos);
    }

    public class newsFeedViewHolder extends RecyclerView.ViewHolder {

        CardView newsFeedCard;
        TextView textViewName, textViewUpdateTime, textViewTitle, textViewDescription;
        ImageView avatarView, imageView, likeButton, shareButton;
        JzvdStd videoView;
        ScrollGalleryView scrollGalleryView;
        ArrayList<String> multiImageViewBitmaps;


        public newsFeedViewHolder(View itemView) {
            super(itemView);

            textViewName = itemView.findViewById(R.id.textViewName);
            textViewUpdateTime = itemView.findViewById(R.id.textViewUpdateTime);
            avatarView = itemView.findViewById(R.id.avatarView);
            imageView = itemView.findViewById(R.id.imageView);
            videoView = itemView.findViewById(R.id.videoView);
            scrollGalleryView = itemView.findViewById(R.id.scroll_gallery_view);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            newsFeedCard =  itemView.findViewById(R.id.imageViewCardView);
            likeButton = itemView.findViewById(R.id.like);
            shareButton = itemView.findViewById(R.id.share);
        }
    }
}
