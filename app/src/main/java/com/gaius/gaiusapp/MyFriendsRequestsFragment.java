package com.gaius.gaiusapp;

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
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.gaius.gaiusapp.adapters.FriendsAdapter;
import com.gaius.gaiusapp.classes.Friend;
import com.gaius.gaiusapp.interfaces.FragmentVisibleInterface;
import com.gaius.gaiusapp.utils.LogOut;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MyFriendsRequestsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, FragmentVisibleInterface {
    private static String URL = "";
    List<Friend> friendList;
    SharedPreferences prefs;
    RelativeLayout noFriends, noInternet;
    RecyclerView recyclerView;
    SwipeRefreshLayout swipeLayout;
    FriendsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.my_friends_fragment, null);
        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setEnabled(true);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        String token, base_url;
        noFriends = view.findViewById(R.id.noFriends);
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        token = prefs.getString("token", "null");
        base_url = prefs.getString("base_url", null);
        URL = base_url+"listRequests.py?token=" + token;

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
        adapter = new FriendsAdapter(getContext(), friendList);
        recyclerView.setAdapter(adapter);
    }

    private void loadFriends() {
        /*
         * Creating a String Request
         * The request type is GET defined by first parameter
         * The URL is defined in the second parameter
         * Then we have a Response Listener and a Error Listener
         * In response listener we will get the JSON response as a String
         * */
        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            //converting the string to json array object
                            JSONArray array = new JSONArray(response);

                            noFriends.setVisibility(View.GONE);

                            if (array.length() == 0 ) {
                                noFriends.setVisibility(View.VISIBLE);
                            }

                            //traversing through all the object
                            for (int i = 0; i < array.length(); i++) {

                                //getting product object from json array
                                JSONObject friend = array.getJSONObject(i);

                                //adding the product to product list
                                friendList.add(new Friend(
                                        friend.getInt("id"),
                                        friend.getString("name"),
                                        "current status",
                                        friend.getString("avatar"),
                                        friend.getString("userID"),
                                        friend.getString("type"),
                                        false
                                ));
                            }

                            //creating adapter object and setting it to recyclerview
                            adapter = new FriendsAdapter(getContext(), friendList);
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
                                Intent i = new Intent(getContext(), LoginActivity.class);
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
        friendList = new ArrayList<>();
        loadFriends();
        swipeLayout.setRefreshing(false);
    }

    @Override
    public void fragmentBecameVisible() {
        friendList = new ArrayList<>();
        loadFriends();
    }
}