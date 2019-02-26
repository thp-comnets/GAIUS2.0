package com.gaius.gaiusapp.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.gaius.gaiusapp.FriendsFragment;
import com.gaius.gaiusapp.MainActivity;
import com.gaius.gaiusapp.R;
import com.gaius.gaiusapp.classes.Friend;
import com.gaius.gaiusapp.networking.GlideApp;
import com.gaius.gaiusapp.utils.Constants;

import java.util.List;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> {

    private Context mCtx;
    private List<Friend> friendsList;
    private SharedPreferences prefs;
    View.OnClickListener friendOnClickListener;

    public FriendsAdapter(Context mCtx, List<Friend> friendsList) {
        this.mCtx = mCtx;
        this.friendsList = friendsList;
    }

    public FriendsAdapter(Context mCtx, List<Friend> friendsList, View.OnClickListener onClickListener) {
        this.mCtx = mCtx;
        this.friendsList = friendsList;
        friendOnClickListener = onClickListener;
    }

    @Override
    public FriendViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.friend_list, null);
        prefs = PreferenceManager.getDefaultSharedPreferences(mCtx);

        return new FriendViewHolder(view);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onBindViewHolder(final FriendViewHolder holder, final int position) {
        final Friend friend = friendsList.get(position);
        holder.setIsRecyclable(false);

        if (friend.getImage().contains("None")) {
            holder.imageView.setImageDrawable(mCtx.getResources().getDrawable(R.drawable.ic_avatar));
        }
        else {
            GlideApp.with(mCtx)
                    .load(prefs.getString("base_url", null) + friend.getImage())
                    .avatar()
                    .into(holder.imageView);
        }


        holder.textViewName.setText(friend.getName());
//        holder.textViewPhoneNumber.setText(friend.getPhoneNumber());
        holder.textViewPhoneNumber.setText("I'm using Gaius");

        if (friend.getClickable()) {
            holder.layout.setTag(position);
            holder.layout.setOnClickListener(friendOnClickListener);
        }

        switch (friend.getButtonType()) {
            case "connect":
                holder.mButton.setVisibility(View.VISIBLE);
                holder.mButton.setSupportBackgroundTintList(mCtx.getResources().getColorStateList(R.color.amber_600));
                holder.mButton.setText("Connect");
                break;
            case "accept":
                holder.mButton.setVisibility(View.VISIBLE);
                holder.mButton.setSupportBackgroundTintList(mCtx.getResources().getColorStateList(R.color.green));
                holder.mButton.setText("Accept");
                break;
            case "withdraw":
                holder.mButton.setVisibility(View.VISIBLE);
                holder.mButton.setSupportBackgroundTintList(mCtx.getResources().getColorStateList(R.color.blue_800));
                holder.mButton.setText("Withdraw");
                break;
        }

        holder.mButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mCtx);
                String token = prefs.getString("token", "null");
                String URL = prefs.getString("base_url", null) + "modifyFriend.py?token=" + token + "&" + friend.getButtonType() + "=" + friend.getUserID();
                v.setVisibility(View.INVISIBLE);
                holder.mProgressBar.setVisibility(View.VISIBLE);

                StringRequest stringRequest = new StringRequest(Request.Method.GET, URL,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                holder.mProgressBar.setVisibility(View.INVISIBLE);
                                holder.mButton.setVisibility(View.VISIBLE);

                                if (response.contains("Success")) {
                                    friendsList.remove(friend);
                                    notifyDataSetChanged();

                                    if (friend.getButtonType().equals("accept")) {

                                        int number = prefs.getInt("pending-requests", 0);

                                        if (number > 1) {
                                            number -= 1;
                                        }
                                        else {
                                            number = 0;
                                        }

                                        // save the pending requests to the sharedprefs
                                        SharedPreferences.Editor editor = prefs.edit();
                                        editor.putInt("pending-requests", number);
                                        editor.apply();

                                        FriendsFragment.updateNotificationBadge();
                                        MainActivity.setBadge(number);
                                    }
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                holder.mProgressBar.setVisibility(View.INVISIBLE);
                                holder.mButton.setVisibility(View.VISIBLE);
                                Log.d("Yasir","Error "+error);
                            }
                        });
                Log.d("Yasir","added request "+stringRequest);

                Volley.newRequestQueue(mCtx).add(stringRequest);
            }
        });

    }

    public Friend getItemFromFriendsList(int position) {
        return friendsList.get(position);
    }

    public void removeItemFromFriendsList(int position) {
        if (position != Constants.INVALID_POSITION) {
            friendsList.remove(position);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount() {
        return friendsList.size();
    }


    class FriendViewHolder extends RecyclerView.ViewHolder {

        TextView textViewName, textViewPhoneNumber;
        ImageView imageView;
        RelativeLayout layout;
        AppCompatButton mButton;
        ProgressBar mProgressBar;

        public FriendViewHolder(View itemView) {
            super(itemView);

            textViewName = itemView.findViewById(R.id.textViewName);
            textViewPhoneNumber = itemView.findViewById(R.id.textViewPhoneNumber);
            imageView = itemView.findViewById(R.id.imageView);
            layout = itemView.findViewById(R.id.friend_layout);
            mButton = itemView.findViewById(R.id.friend_button);
            mProgressBar = itemView.findViewById(R.id.friend_progress_bar);
        }
    }
}
