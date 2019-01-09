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

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> {

    private Context mCtx;
    private List<Friend> friendtList;

    public FriendsAdapter(Context mCtx, List<Friend> friendtList) {
        this.mCtx = mCtx;
        this.friendtList = friendtList;
    }

    @Override
    public FriendViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.friend_list, null);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FriendViewHolder holder, int position) {
        Friend friend = friendtList.get(position);

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.error(R.drawable.ic_avatar);

        //loading the image
        Glide.with(mCtx)
                .setDefaultRequestOptions(requestOptions)
                .load(friend.getImage())
                .into(holder.imageView);

        holder.textViewName.setText(friend.getName());
        holder.textViewPhoneNumber.setText(friend.getPhoneNumber());
    }

    @Override
    public int getItemCount() {
        return friendtList.size();
    }

    class FriendViewHolder extends RecyclerView.ViewHolder {

        TextView textViewName, textViewPhoneNumber;
        ImageView imageView;

        public FriendViewHolder(View itemView) {
            super(itemView);

            textViewName = itemView.findViewById(R.id.textViewName);
            textViewPhoneNumber = itemView.findViewById(R.id.textViewPhoneNumber);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}
