package com.gaius.gaiusapp.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.gaius.gaiusapp.classes.Item;
import com.gaius.gaiusapp.R;
import com.gaius.gaiusapp.helper.ItemTouchHelperAdapter;
import com.gaius.gaiusapp.utils.FontProvider;

import java.util.Collections;
import java.util.List;

import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;

public class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.ItemViewHolder>
        implements ItemTouchHelperAdapter, View.OnLayoutChangeListener {

    private Context mCtx;
    private List<Item> contentsList;
    private View.OnClickListener mOnClickListener;

    public ItemsAdapter(Context mCtx, List<Item> contentsList) {
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
        holder.setIsRecyclable(false);

        switch (item.getType()) {
            case "text":
                holder.editText.setVisibility(View.VISIBLE);
                holder.imageView.setVisibility(View.GONE);
                holder.videoView.setVisibility(View.GONE);

                item.setView(holder.editText);
                holder.editText.setText(item.getText());
                holder.editText.requestFocus();
                holder.editText.addOnLayoutChangeListener(this);
                holder.editText.setTag(item.getId());

                FontProvider fontProvider = new FontProvider(mCtx.getResources());;
                holder.editText.setTypeface(fontProvider.getTypeface("Arial"));

                if (item.getTextType().contains("header")) {
                    holder.editText.setTextSize(30);
                    item.setFontSize(30);
                }
                else if (item.getTextType().contains("paragraph")){
                    holder.editText.setTextSize(20);
                    item.setFontSize(20);
                }
                break;
            case "image":
                holder.editText.setVisibility(View.GONE);
                holder.imageView.setVisibility(View.VISIBLE);
                holder.videoView.setVisibility(View.GONE);

                holder.imageView.setImageBitmap(item.getImageBitmap());

                item.setView(holder.imageView);
                holder.imageView.addOnLayoutChangeListener(this);

                break;
            case "video":
                holder.editText.setVisibility(View.GONE);
                holder.imageView.setVisibility(View.GONE);
                holder.videoView.setVisibility(View.VISIBLE);

                holder.videoView.setUp(item.getVideoPath(), "", Jzvd.SCREEN_WINDOW_LIST);
                holder.videoView.thumbImageView.setImageBitmap(item.getVideoBitmap());

                item.setView(holder.videoView);
                holder.videoView.addOnLayoutChangeListener(this);
                break;
        }

        item.setDeleteView(holder.deleteButton);
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(mCtx);

                builder.setTitle("Delete item");
                builder.setMessage("Do you want to delete this item?");

                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing but close the dialog
                        dialog.dismiss();

                        for (int i=0; i<contentsList.size(); i++) {
                            Item it = contentsList.get(i);
                            if (it.getDeleteView().equals(v)) {
                                contentsList.remove(it);
                                notifyDataSetChanged();
                                break;
                            }
                        }
                    }
                });

                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                        dialog.dismiss();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();

            }
        });

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

    public class ItemViewHolder extends RecyclerView.ViewHolder {

        public EditText editText;
        public ImageView imageView, deleteButton;
        public JzvdStd videoView;
        public RelativeLayout relativeLayout;

        public ItemViewHolder(View itemView) {
            super(itemView);

            deleteButton = itemView.findViewById(R.id.item_delete);
            editText = itemView.findViewById(R.id.edit_text);
            MyTextWatcher textWatcher = new MyTextWatcher(editText);
            editText.addTextChangedListener(textWatcher);

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

    public class MyTextWatcher implements TextWatcher {
        public EditText editText;

        public MyTextWatcher(EditText editText) {
            this.editText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (editText.getTag() != null) {
                int itemID = (int) editText.getTag();

                for (Item it: contentsList) {
                    if (it.getType().equals("text") && itemID == it.getId()) {
                        it.setText(s.toString());
//                        Log.d("yasir", "Text: "+ s.toString() + " "+ editText.getTag());
                        break;
                    }
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }
}
