package com.gaius.gaiusapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;

public class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.ItemViewHolder> {

    private Context mCtx;
    private List<Item> contentsList;
    private View.OnClickListener mOnClickListener;

    public ItemsAdapter(Context mCtx, List<Item> contentsList, View.OnClickListener mOnClickListener) {
        this.mCtx = mCtx;
        this.contentsList = contentsList;
        this.mOnClickListener = mOnClickListener;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.item_list, null);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        Item item = contentsList.get(position);

        switch (item.getType()) {
            case "text":
                holder.textView.setVisibility(View.VISIBLE);
                holder.textView.setText(item.getText());
                break;
            case "image":
                holder.imageView.setVisibility(View.VISIBLE);
                holder.imageView.setImageBitmap(item.getImageBitmap());
                break;
            case "video":
                holder.videoView.setVisibility(View.VISIBLE);
                holder.videoView.setUp(item.getVideoPath(), "", Jzvd.SCREEN_WINDOW_LIST);
                holder.videoView.thumbImageView.setImageBitmap(item.getVideoBitmap());

                break;
        }

        holder.moveUP.setTag(position);
        holder.moveUP.setOnClickListener(mOnClickListener);

//        holder.textViewTitle.setText(item.getTitle());
//        holder.imageView.setImageResource(item.getImage());
//
//        holder.cardView.setTag(position);
//        holder.cardView.setOnClickListener(mOnClickListener);
    }

    @Override
    public int getItemCount() {
        return contentsList.size();
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {

        TextView textView;
        ImageView imageView;
        JzvdStd videoView;
        ImageView moveUP;

        public ItemViewHolder(View itemView) {
            super(itemView);

            textView = itemView.findViewById(R.id.item_text);
            imageView = itemView.findViewById(R.id.item_image);
            videoView = itemView.findViewById(R.id.item_video);
            moveUP = itemView.findViewById(R.id.moveUP);
        }
    }
}
