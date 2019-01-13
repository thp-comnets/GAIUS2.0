package com.gaiusnetworks.gaius;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

public class ChannelsAdapter extends RecyclerView.Adapter<ChannelsAdapter.ChannelViewHolder> {
    private Context mCtx;
    private List<Channel> channelsList;

    public ChannelsAdapter(Context mCtx, List<Channel> channelsList) {
        this.mCtx = mCtx;
        this.channelsList = channelsList;
    }

    @Override
    public ChannelViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.channel_list, null);
        return new ChannelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ChannelViewHolder holder, int position) {
        Channel channel = channelsList.get(position);

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.error(R.drawable.ic_avatar);

        //loading the image
        Glide.with(mCtx)
                .setDefaultRequestOptions(requestOptions)
                .load(channel.getImage())
                .into(holder.imageView);

        holder.textViewTitle.setText(channel.getTitle());

        holder.channelItem.setTag(position);
        holder.channelItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Channel c = channelsList.get((Integer) v.getTag());

                Bundle bundle = new Bundle();
                Intent i = new Intent(mCtx, RenderMAML.class);

                if (!c.getUserID().contains("null")) {
                    Log.d("Yasir", "click " + c.getTitle());
//                    intent = new Intent(view.getContext(), DynamicChannelListViewActivity.class);
//                    bundle.putSerializable("URL_POST_FIX", "?userid=" + clickedChannel.getUserId());
//                    bundle.putBoolean("LOCAL", true);
//                    bundle.putSerializable("CHANNEL_NAME", clickedChannel.getName());
                }
                else {
                    bundle.putSerializable("BASEURL", "http://91.230.41.34:8080/test/");
                    bundle.putSerializable("URL", c.getUrl());
                    i.putExtras(bundle);
                    mCtx.startActivity(i);
                }

//                ((AppCompatActivity) mCtx).getSupportFragmentManager().beginTransaction()
//                        .replace(R.id.fragment_container, new VideosFragment())
//                        .commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return channelsList.size();
    }

    class ChannelViewHolder extends RecyclerView.ViewHolder {

        TextView textViewTitle;
        ImageView imageView;
        LinearLayout channelItem;

        public ChannelViewHolder(View itemView) {
            super(itemView);

            textViewTitle = itemView.findViewById(R.id.textViewName);
            imageView = itemView.findViewById(R.id.imageView);
            channelItem = itemView.findViewById(R.id.channelItem);
        }
    }
}
