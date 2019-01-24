package com.gaius.gaiusapp.adapters;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.gaius.gaiusapp.classes.Friend;
import com.gaius.gaiusapp.R;
import com.gaius.gaiusapp.userFragment;

import java.util.List;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> {

    private Context mCtx;
    private List<Friend> friendsList;

    public FriendsAdapter(Context mCtx, List<Friend> friendsList) {
        this.mCtx = mCtx;
        this.friendsList = friendsList;
    }

    @Override
    public FriendViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.friend_list, null);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FriendViewHolder holder, int position) {
        Friend friend = friendsList.get(position);

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.error(R.drawable.ic_avatar);

        if (friend.getImage().contains("None")) {
            //loading the image
            Glide.with(mCtx)
                    .load(R.drawable.ic_avatar)
                    .into(holder.imageView);
        }
        else {
            //loading the image
            Glide.with(mCtx)
                    .setDefaultRequestOptions(requestOptions)
                    .load(friend.getImage())
                    .into(holder.imageView);
        }


        holder.textViewName.setText(friend.getName());
        holder.textViewPhoneNumber.setText(friend.getPhoneNumber());

        holder.layout.setTag(position);
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();

                Friend f = friendsList.get((Integer) v.getTag());
                Fragment fragment = new userFragment();
                bundle.putString("userID", f.getUserID());
                bundle.putString("name", f.getName());
                bundle.putString("avatar", f.getImage());
                fragment.setArguments(bundle);

                ((AppCompatActivity) mCtx).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }
        });

    }

    @Override
    public int getItemCount() {
        return friendsList.size();
    }

    class FriendViewHolder extends RecyclerView.ViewHolder {

        TextView textViewName, textViewPhoneNumber;
        ImageView imageView;
        RelativeLayout layout;

        public FriendViewHolder(View itemView) {
            super(itemView);

            textViewName = itemView.findViewById(R.id.textViewName);
            textViewPhoneNumber = itemView.findViewById(R.id.textViewPhoneNumber);
            imageView = itemView.findViewById(R.id.imageView);
            layout = itemView.findViewById(R.id.friend_layout);
        }
    }
}