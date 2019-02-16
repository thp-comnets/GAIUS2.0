package com.gaius.gaiusapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gaius.gaiusapp.BrowseWebFragment;
import com.gaius.gaiusapp.R;
import com.gaius.gaiusapp.RenderMAML;
import com.gaius.gaiusapp.classes.Web;
import com.gaius.gaiusapp.networking.GlideApp;

import java.util.List;

public class BrowseWebAdapter extends RecyclerView.Adapter<BrowseWebAdapter.ChannelViewHolder> {
    private Context mCtx;
    private List<Web> channelsList;
    private SharedPreferences prefs;

    public BrowseWebAdapter(Context mCtx, List<Web> channelsList) {
        this.mCtx = mCtx;
        this.channelsList = channelsList;
    }

    @Override
    public ChannelViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.web_list, null);

        prefs = PreferenceManager.getDefaultSharedPreferences(mCtx);

        return new ChannelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ChannelViewHolder holder, int position) {
        Web web = channelsList.get(position);

        if (web.getImage().contains("None")) {
            holder.imageView.setImageDrawable(mCtx.getResources().getDrawable(R.drawable.ic_gaius_round));
        }
        else {
            GlideApp.with(mCtx)
                    .load(prefs.getString("base_url", null) + web.getImage())
                    .avatar()
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
                    Fragment fragment = new BrowseWebFragment();
                    bundle.putString("userID", c.getUserID());
                    fragment.setArguments(bundle);

                    ((AppCompatActivity) mCtx).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();
                }
                else {
                    Intent i = new Intent(mCtx, RenderMAML.class);

                    bundle.putSerializable("BASEURL", prefs.getString("base_url", null));
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
