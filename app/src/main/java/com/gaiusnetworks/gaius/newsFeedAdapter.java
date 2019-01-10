package com.gaiusnetworks.gaius;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import static java.security.AccessController.getContext;

public class newsFeedAdapter extends RecyclerView.Adapter<newsFeedAdapter.newsFeedViewHolder> {

    private Context mCtx;
    private List<NewsFeed> newsFeedList;

    public newsFeedAdapter(Context mCtx, List<NewsFeed> newsFeedList) {
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
                .into(holder.avatarView);

        requestOptions = new RequestOptions();
        requestOptions.error(R.drawable.ic_home);

        //loading the image
        Glide.with(mCtx)
                .setDefaultRequestOptions(requestOptions)
                .load(newsfeed.getImage())
                .into(holder.imageView);

        holder.textViewName.setText(newsfeed.getName());
        holder.textViewUpdateTime.setText(newsfeed.getUpdateTime());
        holder.textViewTitle.setText(newsfeed.getTitle());
        holder.textViewDescription.setText(newsfeed.getDescription());
    }

    @Override
    public int getItemCount() {
        return newsFeedList.size();
    }

    class newsFeedViewHolder extends RecyclerView.ViewHolder {

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
        }
    }
}
