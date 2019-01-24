package com.gaius.gaiusapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.gaius.gaiusapp.R;
import com.gaius.gaiusapp.RenderMAML;
import com.gaius.gaiusapp.classes.Web;
import com.gaius.gaiusapp.WebFragment;

import java.util.List;

public class WebAdapter extends RecyclerView.Adapter<WebAdapter.ChannelViewHolder> {
    private Context mCtx;
    private List<Web> channelsList;

    public WebAdapter(Context mCtx, List<Web> channelsList) {
        this.mCtx = mCtx;
        this.channelsList = channelsList;
    }

    @Override
    public ChannelViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.web_list, null);
        return new ChannelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ChannelViewHolder holder, int position) {
        Web web = channelsList.get(position);

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.error(R.drawable.ic_avatar);

        if (web.getImage().contains("None")) {
            //loading the image
            Glide.with(mCtx)
                    .load(R.drawable.ic_avatar)
                    .into(holder.imageView);
        }
        else {
            //loading the image
            Glide.with(mCtx)
                    .setDefaultRequestOptions(requestOptions)
                    .load(web.getImage())
                    .into(holder.imageView);
        }


        holder.textViewTitle.setText(web.getTitle());

        holder.channelItem.setTag(position);
        holder.channelItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Web c = channelsList.get((Integer) v.getTag());
                Bundle bundle = new Bundle();

                if (!c.getUserID().contains("null")) {
                    Fragment fragment = new WebFragment();
                    bundle.putString("userID", c.getUserID());
                    fragment.setArguments(bundle);

                    ((AppCompatActivity) mCtx).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();
                }
                else {
                    Intent i = new Intent(mCtx, RenderMAML.class);

                    bundle.putSerializable("BASEURL", "http://91.230.41.34:8080/test/");
                    bundle.putSerializable("URL", c.getUrl());
                    i.putExtras(bundle);
                    mCtx.startActivity(i);
                }
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
