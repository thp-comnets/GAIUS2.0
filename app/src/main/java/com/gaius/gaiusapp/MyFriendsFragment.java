package com.gaius.gaiusapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.gaius.gaiusapp.adapters.FriendsAdapter;
import com.gaius.gaiusapp.classes.Friend;
import com.gaius.gaiusapp.interfaces.FragmentVisibleInterface;
import com.gaius.gaiusapp.interfaces.OnAdapterInteractionListener;
import com.gaius.gaiusapp.interfaces.OnFragmentInteractionListener;
import com.gaius.gaiusapp.utils.Constants;
import com.gaius.gaiusapp.utils.LogOut;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MyFriendsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, FragmentVisibleInterface, View.OnClickListener, OnAdapterInteractionListener {
    private static String URL = "";
    List<Friend> friendList;
    SharedPreferences prefs;
    RelativeLayout noFriendsLayout, noInternet;
    RecyclerView recyclerView;
    TextView noFriendTextView;
    SwipeRefreshLayout swipeLayout;
    FriendsAdapter adapter;
    View.OnClickListener mOnClickListener;
    private OnFragmentInteractionListener mListener;
    private OnAdapterInteractionListener mAdapterListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.my_friends_fragment, null);
        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setEnabled(true);
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        mOnClickListener = this;
        mAdapterListener = this;
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        String token, base_url;
        noFriendsLayout = view.findViewById(R.id.noFriends);
        noFriendTextView = view.findViewById(R.id.noFriendTextView);
        noFriendTextView.setText("You don't have friends yet.\\nPlease consider adding some.");

        token = prefs.getString("token", "null");
        base_url = prefs.getString("base_url", null);
        URL = base_url+"listFriends2.py?token=" + token;

        noInternet = view.findViewById(R.id.no_internet);
        noInternet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFriends();
            }
        });

        recyclerView = getView().findViewById(R.id.recylcerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setVisibility(View.GONE);
        friendList = new ArrayList<>();
        adapter = new FriendsAdapter(getContext(), friendList, this, this);
        recyclerView.setAdapter(adapter);
        this.fragmentBecameVisible();
    }

    private void loadFriends() {

        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            //converting the string to json array object
                            JSONArray array = new JSONArray(response);

                            noFriendsLayout.setVisibility(View.GONE);

                            if (array.length() == 0 ) {
                                noFriendsLayout.setVisibility(View.VISIBLE);
                            }

                            friendList = new ArrayList<>();

                            //traversing through all the object
                            for (int i = 0; i < array.length(); i++) {

                                //getting product object from json array
                                JSONObject friend = array.getJSONObject(i);

                                if (friend.has("pending-requests")) {
                                    int number = friend.getInt("pending-requests");

                                    // save the pending requests to the sharedprefs
                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.putInt("pending-requests", number);
                                    editor.commit();

                                    //signal the badge change up to the MainActivity. it can be null if the response arrives when the fragment is detached
                                    // TODO maybe we should skip the return then?
                                    if (mListener != null) {
                                        mListener.onFragmentInteraction(Constants.UPDATE_BADGE_NOTIFICATION_FRIENDS);
                                    }

                                }

                                //adding the product to product list
                                friendList.add(new Friend(
                                        friend.getInt("id"),
                                        friend.getString("name"),
                                        "current status",
                                        friend.getString("avatar"),
                                        friend.getString("userID"),
                                        "remove",
                                        true
                                ));
                            }

                            adapter = new FriendsAdapter(getContext(), friendList, mOnClickListener, mAdapterListener);
                            recyclerView.setAdapter(adapter);
                            noInternet.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d("Yasir","Json error "+e);

                            if (response.contains("invalid token")) {
                                LogOut.logout(getContext());
                                Toast.makeText(getContext(), "You have logged in from another device. Please login again.",
                                        Toast.LENGTH_LONG).show();
                                Intent i = new Intent(getContext(), LoginSMSActivity.class);
                                startActivity(i);
                                getActivity().finish();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Yasir","Error "+error);
                        noInternet.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }
                });

        Log.d("Yasir","added request "+stringRequest);

        //adding our stringrequest to queue
        Volley.newRequestQueue(getContext()).add(stringRequest);
    }

    @Override
    public void onRefresh() {
        loadFriends();
        swipeLayout.setRefreshing(false);
    }

    @Override
    public void fragmentBecameVisible() {
        loadFriends();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0) {
            if(resultCode == Activity.RESULT_OK){
                adapter.removeItemFromFriendsList(data.getIntExtra("removeIndex", Constants.INVALID_POSITION));
                if (adapter.getItemCount() == 0) {
                    noFriendsLayout.setVisibility(View.VISIBLE);
                }
            }
            if (resultCode == Activity.RESULT_CANCELED) {
               // do nothing
            }
        }
    }

    @Override
    public void onClick(View v) {
        Friend friend = adapter.getItemFromFriendsList((Integer) v.getTag());
        Intent intent = new Intent(getContext(), FriendPageActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("userID", friend.getUserID());
        bundle.putString("name", friend.getName());
        bundle.putInt("position",(Integer) v.getTag());
        bundle.putString("status", "I'm using Gaius");
        bundle.putString("avatar", prefs.getString("base_url", null) + friend.getImage());
        intent.putExtras(bundle);
        startActivityForResult(intent, 0);
    }

    @Override
    public void onAdapterInteraction(Integer action) {
        //signal the badge change up to the MainActivity
        mListener.onFragmentInteraction(Constants.UPDATE_BADGE_NOTIFICATION_FRIENDS);
    }
}