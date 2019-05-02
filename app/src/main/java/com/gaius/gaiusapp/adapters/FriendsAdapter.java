package com.gaius.gaiusapp.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
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

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.gaius.gaiusapp.R;
import com.gaius.gaiusapp.classes.Friend;
import com.gaius.gaiusapp.interfaces.OnAdapterInteractionListener;
import com.gaius.gaiusapp.networking.GlideApp;
import com.gaius.gaiusapp.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> {

    private Context mCtx;
    private List<Friend> friendsList;
    private SharedPreferences prefs;
    private Integer friendStatus;
    View.OnClickListener friendOnClickListener;
    OnAdapterInteractionListener mAdapterListener;

    public FriendsAdapter(Context mCtx, List<Friend> friendsList, OnAdapterInteractionListener mListener) {
        this.mCtx = mCtx;
        this.friendsList = friendsList;
        this.mAdapterListener = mListener;
    }

    public FriendsAdapter(Context mCtx, List<Friend> friendsList, View.OnClickListener onClickListener, OnAdapterInteractionListener mListener) {
        this.mCtx = mCtx;
        this.friendsList = friendsList;
        friendOnClickListener = onClickListener;
        this.mAdapterListener = mListener;
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
        holder.textViewPhoneNumber.setText("I'm using Gaius");

//        if (friend.getClickable()) { FIXME is this still being used?
            holder.layout.setTag(position);
            holder.layout.setOnClickListener(friendOnClickListener);
//        }

        setActionButton(holder, friend.getFriendStatus());

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

    private void setActionButton(final FriendViewHolder holder, int friendStatus) {
        this.friendStatus = friendStatus;

        holder.mProgressBar.setVisibility(View.GONE);
        holder.actionButton.setVisibility(View.VISIBLE);
        switch (friendStatus) {
            case Constants.FRIEND_STATUS_NOT_CONNECTED:
                holder.actionButton.setBackground(mCtx.getResources().getDrawable(R.drawable.friend_connect_button));
                holder.actionButton.setText(mCtx.getResources().getString(R.string.connect));
                holder.actionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        modifyFriend(holder, Constants.FRIEND_STATUS_NOT_CONNECTED);
                    }
                });
                break;
            case Constants.FRIEND_STATUS_PENDING:
                holder.actionButton.setBackground(mCtx.getResources().getDrawable(R.drawable.friend_withdraw_button));
                holder.actionButton.setText(mCtx.getResources().getString(R.string.withdraw));
                holder.actionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        modifyFriend(holder, Constants.FRIEND_STATUS_PENDING);
                    }
                });
                break;
            case Constants.FRIEND_STATUS_CONNECTED:
                holder.actionButton.setBackground(mCtx.getResources().getDrawable(R.drawable.friend_unfriend_button));
                holder.actionButton.setText(mCtx.getResources().getString(R.string.unfriend));
                holder.actionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showUnfriendDialog(holder ,v);
                    }
                });
                break;
            case Constants.FRIEND_STATUS_ACCEPT:
                holder.actionButton.setBackground(mCtx.getResources().getDrawable(R.drawable.friend_accept_button));
                holder.actionButton.setText(mCtx.getResources().getString(R.string.accept));
                holder.actionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        modifyFriend(holder, Constants.FRIEND_STATUS_ACCEPT);
                    }
                });
                break;
            default:
                //we should not come here
                holder.actionButton.setVisibility(View.GONE);
                break;
        }
    }

    private void modifyFriend(final FriendViewHolder holder, final int friendstatus) {
        Friend f = friendsList.get((int) holder.layout.getTag());

        holder.mProgressBar.setVisibility(View.VISIBLE);
        holder.actionButton.setVisibility(View.GONE);

//        String URL = prefs.getString("base_url", null) + "modifyFriend.py?token=" + prefs.getString("token", null) + "&" + Constants.FRIEND_ACTION_LIST.get(friendstatus) + "=" + f.getUserID();
//        Log.d("thp", "modifyFreinds.py " + URL);

        AndroidNetworking.get(prefs.getString("base_url", null) + "modifyFriend.py")
                .addQueryParameter(Constants.FRIEND_ACTION_LIST.get(friendstatus), f.getUserID())
                .addQueryParameter("token", prefs.getString("token", "null"))
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONArray(new JSONArrayRequestListener() {
                    @Override
                    public void onResponse(JSONArray response) {

                        Log.d("thp", "Response as JSON " + response);

                        try {
                            JSONObject status = response.getJSONObject(0);
                            Integer action = Integer.parseInt(status.getString("status"));
                            setActionButton(holder, action);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d("thp","Json error "+e);
                        }

                    }
                    @Override
                    public void onError(ANError error) {
                        holder.mProgressBar.setVisibility(View.GONE);
                        holder.actionButton.setVisibility(View.VISIBLE);

                        switch (error.getErrorCode()) {
                            case 401:
//                                LogOut.logout(mCtxgetApplicationContext());
//                                Toast.makeText(getApplicationContext(), "You have logged in from another device. Please login again.",
//                                        Toast.LENGTH_LONG).show();
//                                Intent i = new Intent(getApplicationContext(), LoginSMSActivity.class);
//                                startActivity(i);
//                                finish();
                                break;
                            case 500:
                                Log.d("thp","Error 500"+error);
                                break;
                            default:
                                Log.d("thp","Error no Internet "+error);

                        }
                    }
                });
    }

    private void showUnfriendDialog(final FriendViewHolder holder, final View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mCtx);

        builder.setTitle("Remove friend?");
        builder.setMessage("Do you want to remove this friend?");

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                modifyFriend(holder, Constants.FRIEND_STATUS_CONNECTED);
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    class FriendViewHolder extends RecyclerView.ViewHolder {

        TextView textViewName, textViewPhoneNumber;
        ImageView imageView;
        RelativeLayout layout;
        AppCompatButton actionButton;
        ProgressBar mProgressBar;

        public FriendViewHolder(View itemView) {
            super(itemView);

            textViewName = itemView.findViewById(R.id.textViewName);
            textViewPhoneNumber = itemView.findViewById(R.id.textViewPhoneNumber);
            imageView = itemView.findViewById(R.id.imageView);
            layout = itemView.findViewById(R.id.friend_layout);
            actionButton = itemView.findViewById(R.id.friend_button);
            mProgressBar = itemView.findViewById(R.id.friend_progress_bar);
        }
    }
}
