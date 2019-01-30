package com.gaius.gaiusapp.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gaius.gaiusapp.classes.ContentCategory;
import com.gaius.gaiusapp.R;

import java.util.List;

public class ContentsCategoryAdapter extends RecyclerView.Adapter<ContentsCategoryAdapter.ContentViewHolder> {

    private Context mCtx;
    private List<ContentCategory> contentsList;
    private View.OnClickListener mOnClickListener;

    public ContentsCategoryAdapter(Context mCtx, List<ContentCategory> contentsList, View.OnClickListener mOnClickListener) {
        this.mCtx = mCtx;
        this.contentsList = contentsList;
        this.mOnClickListener = mOnClickListener;
    }

    @Override
    public ContentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.content_category_list, null);
        return new ContentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ContentViewHolder holder, int position) {
        ContentCategory contentCategory = contentsList.get(position);

        holder.textViewTitle.setText(contentCategory.getTitle());
        holder.imageView.setImageResource(contentCategory.getImage());

        holder.cardView.setTag(position);
        holder.cardView.setOnClickListener(mOnClickListener);
//        holder.cardView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ContentCategory c = contentsList.get((Integer) v.getTag());
//
//
//
//                if (c.getTitle().contains("Browse Web")) {
//                    Fragment fragment = new WebFragment();
//
//                    ((AppCompatActivity) mCtx).getSupportFragmentManager().beginTransaction()
//                            .replace(R.id.fragment_container, fragment)
//                            .commit();
//                }
//                else if (c.getTitle().contains("Browse Videos")) {
//                    Fragment fragment = new VideosFragment();
//
//                    ((AppCompatActivity) mCtx).getSupportFragmentManager().beginTransaction()
//                            .replace(R.id.fragment_container, fragment)
//                            .commit();
//                }
//                else if (c.getTitle().contains("Create ContentCategory")) {
//                    displayIntentOptions();
//                }
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return contentsList.size();
    }

    class ContentViewHolder extends RecyclerView.ViewHolder {

        TextView textViewTitle;
        ImageView imageView;
        CardView cardView;

        public ContentViewHolder(View itemView) {
            super(itemView);

            textViewTitle = itemView.findViewById(R.id.content_title);
            imageView = itemView.findViewById(R.id.content_animation);
            cardView = itemView.findViewById(R.id.content_card);
        }
    }
}
