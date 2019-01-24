package com.gaius.gaiusapp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.gaius.gaiusapp.classes.Item;
import com.gaius.gaiusapp.R;
import com.gaius.gaiusapp.helper.ItemTouchHelperAdapter;

import java.util.Collections;
import java.util.List;

import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;

public class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.ItemViewHolder>
        implements ItemTouchHelperAdapter {

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
    public void onBindViewHolder(final ItemViewHolder holder, int position) {
        Item item = contentsList.get(position);
        int [] pos = new int[2];

        switch (item.getType()) {
            case "text":
                holder.editText.setVisibility(View.VISIBLE);
                holder.editText.setText(item.getText());
                holder.editText.requestFocus();

                item.setW(holder.editText.getWidth());
                item.setH(holder.editText.getHeight());
                holder.itemView.getLocationInWindow(pos);
                item.setX(pos[0]);

                if (item.getTextType().contains("header")) {
                    holder.editText.setTextSize(30);
                    holder.editText.setGravity(Gravity.CENTER);
                }
                else if (item.getTextType().contains("paragraph")){
                    holder.editText.setTextSize(20);
                }

                break;
            case "image":
                holder.imageView.setVisibility(View.VISIBLE);
                holder.imageView.setImageBitmap(item.getImageBitmap());

                item.setW(holder.imageView.getWidth());
                item.setH(holder.imageView.getHeight());
                holder.imageView.getLocationInWindow(pos);
                item.setX(pos[0]);

                break;
            case "video":
                holder.videoView.setVisibility(View.VISIBLE);
                holder.videoView.setUp(item.getVideoPath(), "", Jzvd.SCREEN_WINDOW_LIST);
                holder.videoView.thumbImageView.setImageBitmap(item.getVideoBitmap());

                item.setW(holder.videoView.getWidth());
                item.setH(holder.videoView.getHeight());
                holder.videoView.getLocationInWindow(pos);
                item.setX(pos[0]);

                break;
        }

        holder.deleteButton.setTag(item.getId());
        holder.deleteButton.setOnClickListener(mOnClickListener);
    }

    @Override
    public int getItemCount() {
        return contentsList.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {

        public EditText editText;
        public ImageView imageView, deleteButton;
        public JzvdStd videoView;
        public RelativeLayout relativeLayout;

        public ItemViewHolder(View itemView) {
            super(itemView);

            deleteButton = itemView.findViewById(R.id.item_delete);
            editText = itemView.findViewById(R.id.edit_text);
            imageView = itemView.findViewById(R.id.item_image);
            videoView = itemView.findViewById(R.id.item_video);
            relativeLayout = itemView.findViewById(R.id.relative_layout);
        }
    }

    public Item getItem(int pos) {
        return contentsList.get(pos);
    }

    @Override
    public void onItemDismiss(int position) {
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(contentsList, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);

        return true;
    }
}
