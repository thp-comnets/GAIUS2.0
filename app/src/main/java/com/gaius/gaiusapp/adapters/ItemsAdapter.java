package com.gaius.gaiusapp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
        implements ItemTouchHelperAdapter, View.OnLayoutChangeListener, View.OnFocusChangeListener {

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
        final Item item = contentsList.get(position);

        switch (item.getType()) {
            case "text":
                holder.editText.setVisibility(View.VISIBLE);
                holder.editText.requestFocus();
                holder.editText.setOnFocusChangeListener(this);

                if (item.getTextType().contains("header")) {
                    holder.editText.setTextSize(30);
                    item.setFontSize(30);
                    holder.editText.setGravity(Gravity.CENTER);
                }
                else if (item.getTextType().contains("paragraph")){
                    holder.editText.setTextSize(20);
                    item.setFontSize(20);
                }

                item.setView(holder.editText);
                holder.editText.addOnLayoutChangeListener(this);

                break;
            case "image":
                holder.imageView.setVisibility(View.VISIBLE);
                holder.imageView.setImageBitmap(item.getImageBitmap());

                item.setView(holder.imageView);
                holder.imageView.addOnLayoutChangeListener(this);

                break;
            case "video":
                holder.videoView.setVisibility(View.VISIBLE);
                holder.videoView.setUp(item.getVideoPath(), "", Jzvd.SCREEN_WINDOW_LIST);
                holder.videoView.thumbImageView.setImageBitmap(item.getVideoBitmap());

                item.setView(holder.videoView);
                holder.videoView.addOnLayoutChangeListener(this);
                break;
        }

        holder.deleteButton.setTag(item.getId());
        holder.deleteButton.setOnClickListener(mOnClickListener);
    }

    @Override
    public int getItemCount() {
        return contentsList.size();
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        for (Item it: contentsList) {
            if (it.getView().equals(v)) {
                it.setH(v.getHeight());
                it.setW(v.getWidth());
                it.setX(left);
            }
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        for (Item it: contentsList) {
            if (it.getView().equals(v)) {
                it.setText(((EditText) v).getText().toString());
            }
        }
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
