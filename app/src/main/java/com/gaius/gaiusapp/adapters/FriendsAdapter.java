package com.gaius.gaiusapp.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.gaius.gaiusapp.FriendsFragment;
import com.gaius.gaiusapp.MainActivity;
import com.gaius.gaiusapp.MyFriendsRequestsFragment;
import com.gaius.gaiusapp.R;
import com.gaius.gaiusapp.UserPageFragment;
import com.gaius.gaiusapp.classes.Friend;
import com.gaius.gaiusapp.networking.GlideApp;

import java.util.List;

import me.leolin.shortcutbadger.ShortcutBadger;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> {

    private Context mCtx;
    private List<Friend> friendsList;
    private SharedPreferences prefs;

    public FriendsAdapter(Context mCtx, List<Friend> friendsList) {
        this.mCtx = mCtx;
        this.friendsList = friendsList;
    }

    @Override
    public FriendViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.friend_list, null);

        prefs = PreferenceManager.getDefaultSharedPreferences(mCtx);

        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FriendViewHolder holder, int position) {
        final Friend friend = friendsList.get(position);

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
        holder.textViewPhoneNumber.setText(friend.getPhoneNumber());

        if (friend.getClickable()) {
            holder.layout.setTag(position);
            holder.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();

                    Friend f = friendsList.get((Integer) v.getTag());
                    Fragment fragment = new UserPageFragment();
                    bundle.putString("userID", f.getUserID());
                    bundle.putString("name", f.getName());
                    bundle.putString("avatar", prefs.getString("base_url", null) + f.getImage());
                    fragment.setArguments(bundle);

                    ((AppCompatActivity) mCtx).getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .commit();
                }
            });
        }

        switch (friend.getButtonType()) {
            case "Remove":
                break;
            case "connect":
                // FIXME
                holder.mButton.setBackgroundTintList(mCtx.getResources().getColorStateList(R.color.amber_600));
                holder.mButton.setText("Connect");
                break;
            case "accept":
                holder.mButton.setBackgroundTintList(mCtx.getResources().getColorStateList(R.color.green));
                holder.mButton.setText("Accept");
                break;
            case "withdraw":
                holder.mButton.setBackgroundTintList(mCtx.getResources().getColorStateList(R.color.blue_800));
                holder.mButton.setText("Withdraw");
                break;
        }

        holder.mButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mCtx);
                String token = prefs.getString("token", "null");
                String URL = prefs.getString("base_url", null) + "modifyFriend.py?token=" + token + "&" + friend.getButtonType() + "=" + friend.getUserID();

                StringRequest stringRequest = new StringRequest(Request.Method.GET, URL,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.d("yasir", response);

                                if (response.contains("Success")) {
                                    Log.d("yasir", "removing friend");
                                    friendsList.remove(friend);
                                    notifyDataSetChanged();

                                    if (friend.getButtonType().equals("accept")) {

                                        int number = prefs.getInt("pending-requests", 0);

                                        if (number > 1) {
                                            number -= 1;
                                            FriendsFragment.qBadge.setBadgeNumber(number);
                                            MainActivity.qBadge.setBadgeNumber(number);
                                            ShortcutBadger.applyCount(mCtx, number);
                                        }
                                        else {
                                            number = 0;
                                            FriendsFragment.qBadge.hide(true);
                                            MainActivity.qBadge.hide(true);
                                            ShortcutBadger.removeCount(mCtx);
                                        }

                                        // save the pending requests to the sharedprefs
                                        SharedPreferences.Editor editor = prefs.edit();
                                        editor.putInt("pending-requests", number);
                                        editor.apply();
                                    }
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("Yasir","Error "+error);
                            }
                        });
                Log.d("Yasir","added request "+stringRequest);

                Volley.newRequestQueue(mCtx).add(stringRequest);
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
        Button mButton;

        public FriendViewHolder(View itemView) {
            super(itemView);

            textViewName = itemView.findViewById(R.id.textViewName);
            textViewPhoneNumber = itemView.findViewById(R.id.textViewPhoneNumber);
            imageView = itemView.findViewById(R.id.imageView);
            layout = itemView.findViewById(R.id.friend_layout);
            mButton = itemView.findViewById(R.id.friend_button);
        }
    }
}
