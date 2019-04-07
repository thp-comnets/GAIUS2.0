package com.gaius.gaiusapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import com.bumptech.glide.Priority;
import com.gaius.gaiusapp.AlbumViewActivity;
import com.gaius.gaiusapp.R;
import com.gaius.gaiusapp.RenderMAML;
import com.gaius.gaiusapp.classes.NewsFeed;
import com.gaius.gaiusapp.networking.GlideApp;

import java.util.ArrayList;
import java.util.List;

import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;
import ss.com.bannerslider.Slider;
import ss.com.bannerslider.adapters.SliderAdapter;
import ss.com.bannerslider.event.OnSlideClickListener;
import ss.com.bannerslider.viewholder.ImageSlideViewHolder;

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

        if (newsfeed.getShowAvatar() == true) {
            if (newsfeed.getAvatar().contains("None")) {
                holder.avatarView.setImageDrawable(mCtx.getResources().getDrawable(R.drawable.ic_avatar));
            }
            else{
                GlideApp.with(mCtx)
                        .load(prefs.getString("base_url", null) + newsfeed.getAvatar())
                        .avatar()
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
            holder.slider.setVisibility(View.GONE);

            GlideApp.with(mCtx)
                    .load(convertImageURLBasedonFidelity(prefs.getString("base_url", null) + newsfeed.getImage(), fidelity))
                    .priority(Priority.LOW)
                    .content()
//                    .transition(withCrossFade())
                    .into(holder.imageView);
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NewsFeed n = newsFeedList.get((Integer) v.getTag(R.id.imageView));
                    Bundle bundle = new Bundle();
                    Intent i = new Intent(mCtx, RenderMAML.class);
                    bundle.putSerializable("BASEURL", prefs.getString("base_url", null));
                    bundle.putSerializable("URL", n.getUrl());
                    bundle.putString("title", n.getTitle());
                    i.putExtras(bundle);
                    mCtx.startActivity(i);
                }
            });
        }
        else if (newsfeed.getType().equals("video")) {
            holder.videoView.setVisibility(View.VISIBLE);
            holder.imageView.setVisibility(View.GONE);
            holder.slider.setVisibility(View.GONE);

            GlideApp.with(mCtx)
                    .load(convertImageURLBasedonFidelity(prefs.getString("base_url", null) + newsfeed.getImage(), fidelity))
                    .priority(Priority.LOW)
                    .content()
                    .into(holder.videoView.thumbImageView);

            holder.videoView.setUp(prefs.getString("base_url", null) + newsfeed.getUrl(), "", Jzvd.SCREEN_WINDOW_NORMAL);
        }
        else if (newsfeed.getType().equals("image")) {
            holder.videoView.setVisibility(View.GONE);
            holder.imageView.setVisibility(View.GONE);
            holder.slider.setVisibility(View.VISIBLE);
            holder.setIsRecyclable(false);

//            HashMap<String,String> url_maps = new HashMap<String, String>();
            holder.multiImageViewBitmaps = newsfeed.getImagesGallery();
            holder.slider.setAdapter(new SliderAdapter() {
                @Override
                public int getItemCount() {
                    return holder.multiImageViewBitmaps.size();
                }

                @Override
                public void onBindImageSlide(int position, ImageSlideViewHolder imageSlideViewHolder) {
                    imageSlideViewHolder.bindImageSlide(holder.multiImageViewBitmaps.get(position));
                }
            });

            holder.slider.setSelectedSlide(0);
            holder.slider.setInterval(2000);
            holder.slider.setOnSlideClickListener(new OnSlideClickListener() {
                @Override
                public void onSlideClick(int position) {
                    Bundle bundle = new Bundle();
                    Intent i = new Intent(mCtx, AlbumViewActivity.class);
                    bundle.putStringArrayList("imagesURLs", holder.multiImageViewBitmaps);
                    i.putExtras(bundle);
                    mCtx.startActivity(i);
                }
            });

//            for (int i=0; i<holder.multiImageViewBitmaps.size(); i++) {
//                url_maps.put(i+"", holder.multiImageViewBitmaps.get(i));
//            }
//
//            for(String name : url_maps.keySet()){
//                final TextSliderView textSliderView = new TextSliderView(mCtx);
//                // initialize a SliderLayout
//                textSliderView
////                        .description(name)
//                        .image(url_maps.get(name))
//                        .setScaleType(BaseSliderView.ScaleType.CenterCrop)
//                        .setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
//                            @Override public void onSliderClick(BaseSliderView slider) {
//                                Bundle bundle = new Bundle();
//                                Intent i = new Intent(mCtx, AlbumViewActivity.class);
//                                bundle.putStringArrayList("imagesURLs", holder.multiImageViewBitmaps);
//                                i.putExtras(bundle);
//                                mCtx.startActivity(i);
//
////                                 Intent target = new Intent(Intent.ACTION_VIEW);
////                                 target.setDataAndType(Uri.parse(slider.getUrl()), "image/*");
////                                 target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
////                                 mCtx.startActivity(target);
//                                Log.d("Thomas", "hello hello");
//                            }
//                        });
//
//                holder.mDemoSlider.addSlider(textSliderView);
//            }
        }

        holder.textViewName.setText(newsfeed.getName());
        holder.textViewUpdateTime.setText(newsfeed.getUpdateTime());
        holder.textViewTitle.setText(newsfeed.getTitle());
        holder.textViewDescription.setText(newsfeed.getDescription());

        //FIXME: thp: Yasir, why is all of this here and not in the if statements above?
        if (newsfeed.getType().contains("page")) {
            holder.imageView.setTag(R.id.imageView, position); //we need the key here, otherwise Glide will complain
            holder.shareButton.setTag(position);
            holder.shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shareItem(v, "Check this page on GAIUS");
                }
            });
        }
        else if (newsfeed.getType().contains("video")) {
            holder.shareButton.setTag(position);
            holder.shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shareItem(v, "Check this video on GAIUS");
                }
            });
        }
        else if (newsfeed.getType().contains("image")) {
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

//        CardView newsFeedCard;
        TextView textViewName, textViewUpdateTime, textViewTitle, textViewDescription;
        ImageView avatarView, imageView, likeButton, shareButton;
        JzvdStd videoView;
//        SliderLayout mDemoSlider;
        Slider slider;
        ArrayList<String> multiImageViewBitmaps;


        public newsFeedViewHolder(View itemView) {
            super(itemView);

            textViewName = itemView.findViewById(R.id.textViewName);
            textViewUpdateTime = itemView.findViewById(R.id.textViewUpdateTime);
            avatarView = itemView.findViewById(R.id.avatarView);
            imageView = itemView.findViewById(R.id.imageView);
            videoView = itemView.findViewById(R.id.videoView);
//            mDemoSlider = itemView.findViewById(R.id.slider);
            slider = itemView.findViewById(R.id.slider);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
//            newsFeedCard =  itemView.findViewById(R.id.imageViewCardView);
            likeButton = itemView.findViewById(R.id.like);
            shareButton = itemView.findViewById(R.id.share);
        }
    }
}