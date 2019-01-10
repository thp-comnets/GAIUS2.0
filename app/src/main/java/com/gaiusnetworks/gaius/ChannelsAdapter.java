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
    }

    @Override
    public int getItemCount() {
        return channelsList.size();
    }

    class ChannelViewHolder extends RecyclerView.ViewHolder {

        TextView textViewTitle;
        ImageView imageView;

        public ChannelViewHolder(View itemView) {
            super(itemView);

            textViewTitle = itemView.findViewById(R.id.textViewName);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}
