package com.gaiusnetworks.gaius;

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

import java.util.List;

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

        //loading the image
        Glide.with(mCtx)
                .setDefaultRequestOptions(requestOptions)
                .load(newsfeed.getAvatar())
//                .apply(new RequestOptions().signature(new ObjectKey("signature string")))
                .apply(new RequestOptions().signature(new ObjectKey(System.currentTimeMillis())))
                .into(holder.avatarView);

        requestOptions = new RequestOptions();
//        requestOptions.error(R.drawable.ic_home);

        //loading the image
        Glide.with(mCtx)
                .setDefaultRequestOptions(requestOptions)
                .load(newsfeed.getImage())
                .apply(new RequestOptions().signature(new ObjectKey(System.currentTimeMillis())))
                .into(holder.imageView);

        Log.d("yasir", newsfeed.getImage());

        holder.textViewName.setText(newsfeed.getName());
        holder.textViewUpdateTime.setText(newsfeed.getUpdateTime());
        holder.textViewTitle.setText(newsfeed.getTitle());
        holder.textViewDescription.setText(newsfeed.getDescription());

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

    @Override
    public int getItemCount() {
        return newsFeedList.size();
    }

    class newsFeedViewHolder extends RecyclerView.ViewHolder {

        CardView newsFeedCard;
        TextView textViewName, textViewUpdateTime, textViewTitle, textViewDescription;
        ImageView avatarView, imageView;

        public newsFeedViewHolder(View itemView) {
            super(itemView);

            textViewName = itemView.findViewById(R.id.textViewName);
            textViewUpdateTime = itemView.findViewById(R.id.textViewUpdateTime);
            avatarView = itemView.findViewById(R.id.avatarView);
            imageView = itemView.findViewById(R.id.imageView);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            newsFeedCard =  itemView.findViewById(R.id.imageViewCardView);
        }
    }
}
