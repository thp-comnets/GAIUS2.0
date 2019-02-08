package com.gaius.gaiusapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.gaius.gaiusapp.AlbumViewActivity;
import com.gaius.gaiusapp.classes.NewsFeed;
import com.gaius.gaiusapp.R;
import com.gaius.gaiusapp.RenderMAML;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;

import static com.gaius.gaiusapp.utils.ResourceHelper.convertImageURLBasedonFidelity;

public class NewsFeedAdapter extends RecyclerView.Adapter<NewsFeedAdapter.newsFeedViewHolder> {
    private Context mCtx;
    private List<NewsFeed> newsFeedList;
    private SharedPreferences prefs;

    public NewsFeedAdapter(Context mCtx, List<NewsFeed> newsFeedList) {
        this.mCtx = mCtx;
        this.newsFeedList = newsFeedList;
    }

    @Override
    public newsFeedViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.newsfeed_list, null);

        prefs = PreferenceManager.getDefaultSharedPreferences(mCtx);

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
                        .load(prefs.getString("base_url", null) + newsfeed.getAvatar())
//                .apply(new RequestOptions().signature(new ObjectKey("signature string")))
                        .apply(new RequestOptions().signature(new ObjectKey(System.currentTimeMillis())))
                        .into(holder.avatarView);
            }
        }
        else {
            holder.avatarView.setVisibility(View.GONE);
            holder.textViewName.setVisibility(View.GONE);
        }

        String fidelity = prefs.getString("fidelity_level", "high");

        if (newsfeed.getType().contains("page")) {
            holder.imageView.setVisibility(View.VISIBLE);
            holder.videoView.setVisibility(View.GONE);
            holder.mDemoSlider.setVisibility(View.GONE);

            requestOptions = new RequestOptions();
            //loading the image
            Glide.with(mCtx)
                    .setDefaultRequestOptions(requestOptions)
                    .load(convertImageURLBasedonFidelity(prefs.getString("base_url", null) + newsfeed.getImage(), fidelity))
//                    .apply(new RequestOptions().signature(new ObjectKey("signature string")))
                    .apply(new RequestOptions().signature(new ObjectKey(System.currentTimeMillis())))
                    .into(holder.imageView);
        }
        else if (newsfeed.getType().equals("video")) {
            holder.videoView.setVisibility(View.VISIBLE);
            holder.imageView.setVisibility(View.GONE);
            holder.mDemoSlider.setVisibility(View.GONE);

            Glide.with(mCtx)
                    .load(convertImageURLBasedonFidelity(prefs.getString("base_url", null) + newsfeed.getImage(), fidelity))
                    .apply(new RequestOptions().signature(new ObjectKey(System.currentTimeMillis())))
                    .into(holder.videoView.thumbImageView);
            holder.videoView.setUp(prefs.getString("base_url", null) + newsfeed.getUrl(), "", Jzvd.SCREEN_WINDOW_NORMAL);
        }
        else if (newsfeed.getType().equals("image")) {
            holder.videoView.setVisibility(View.GONE);
            holder.imageView.setVisibility(View.GONE);
            holder.mDemoSlider.setVisibility(View.VISIBLE);
            holder.setIsRecyclable(false);

            HashMap<String,String> url_maps = new HashMap<String, String>();
            holder.multiImageViewBitmaps = newsfeed.getImagesGallery();
            for (int i=0; i<holder.multiImageViewBitmaps.size(); i++) {
                url_maps.put(i+"", holder.multiImageViewBitmaps.get(i));
            }

            for(String name : url_maps.keySet()){
                final TextSliderView textSliderView = new TextSliderView(mCtx);
                // initialize a SliderLayout
                textSliderView
//                        .description(name)
                        .image(url_maps.get(name))
                        .setScaleType(BaseSliderView.ScaleType.CenterCrop)
                        .setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                            @Override public void onSliderClick(BaseSliderView slider) {
                                Bundle bundle = new Bundle();
                                Intent i = new Intent(mCtx, AlbumViewActivity.class);
                                bundle.putStringArrayList("imagesURLs", holder.multiImageViewBitmaps);
                                i.putExtras(bundle);
                                mCtx.startActivity(i);

//                                 Intent target = new Intent(Intent.ACTION_VIEW);
//                                 target.setDataAndType(Uri.parse(slider.getUrl()), "image/*");
//                                 target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
//                                 mCtx.startActivity(target);
                                Log.d("Thomas", "hello hello");
                            }
                        });

                holder.mDemoSlider.addSlider(textSliderView);
            }
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
                    bundle.putSerializable("BASEURL", prefs.getString("base_url", null));
                    bundle.putSerializable("URL", n.getUrl());
                    i.putExtras(bundle);
                    mCtx.startActivity(i);
                }
            });

            holder.shareButton.setTag(position);
            holder.shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shareItem(v, "Check this page on GAIUS");
                }
            });
        }
        else if (newsfeed.getType().contains("video")) {
            // video sharing

            holder.shareButton.setTag(position);
            holder.shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shareItem(v, "Check this video on GAIUS");
                }
            });
        }
        else if (newsfeed.getType().contains("image")) {
            // video sharing

            holder.shareButton.setTag(position);
            holder.shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shareItem(v, "Check this image album on GAIUS");
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

                String url = prefs.getString("base_url", null) + "like.py?url=" + n.getUrl() + "&token=" + prefs.getString("token", "null");
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

    public void shareItem (View v, String shareSub) {
        NewsFeed n = newsFeedList.get((Integer) v.getTag());
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareBody = "http://gaiusnetworks.com/"+n.getUrl().replace("./","");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, shareSub);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        mCtx.startActivity(Intent.createChooser(sharingIntent, "Share using"));
    }

    @Override
    public int getItemCount() {
        return newsFeedList.size();
    }

    public NewsFeed getItem(int pos) {
        return newsFeedList.get(pos);
    }

//    @Override
//    public void onSliderClick(BaseSliderView slider) {
//        Bundle bundle = new Bundle();
////        bundle.putStringArrayList("imagesURLs", holder.multiImageViewBitmaps);
//        Intent i = new Intent(mCtx, RenderPhotoActivity.class);
//        i.putExtras(bundle);
//
//        mCtx.startActivity(i);
//    }

    public class newsFeedViewHolder extends RecyclerView.ViewHolder {

        CardView newsFeedCard;
        TextView textViewName, textViewUpdateTime, textViewTitle, textViewDescription;
        ImageView avatarView, imageView, likeButton, shareButton;
        JzvdStd videoView;
        SliderLayout mDemoSlider;
        ArrayList<String> multiImageViewBitmaps;


        public newsFeedViewHolder(View itemView) {
            super(itemView);

            textViewName = itemView.findViewById(R.id.textViewName);
            textViewUpdateTime = itemView.findViewById(R.id.textViewUpdateTime);
            avatarView = itemView.findViewById(R.id.avatarView);
            imageView = itemView.findViewById(R.id.imageView);
            videoView = itemView.findViewById(R.id.videoView);
            mDemoSlider = itemView.findViewById(R.id.slider);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            newsFeedCard =  itemView.findViewById(R.id.imageViewCardView);
            likeButton = itemView.findViewById(R.id.like);
            shareButton = itemView.findViewById(R.id.share);
        }
    }
}
