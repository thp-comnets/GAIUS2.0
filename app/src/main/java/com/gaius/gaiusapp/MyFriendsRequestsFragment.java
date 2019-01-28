package com.gaius.gaiusapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.gaius.gaiusapp.classes.NewsFeed;
import com.gaius.gaiusapp.utils.LogOut;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MyFriendsRequestsFragment extends Fragment {
    private static String URL = "";
    List<Friend> friendList;
    SharedPreferences prefs;
    RelativeLayout noFriends;
    RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.my_friends_fragment, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        String token;
        noFriends = view.findViewById(R.id.noFriends);
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        token = prefs.getString("token", "null");
        URL = "http://91.230.41.34:8080/test/listRequests.py?token=" + token;

        recyclerView = getView().findViewById(R.id.recylcerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        friendList = new ArrayList<>();
        loadFriends();
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
                                        friend.getString("type")
                                ));
                            }

                            //creating adapter object and setting it to recyclerview
                            FriendsAdapter adapter = new FriendsAdapter(getContext(), friendList);
                            recyclerView.setAdapter(adapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d("Yasir","Json error "+e);

                            if (response.contains("invalid token")) {
                                LogOut.logout(getContext());
                                Toast.makeText(getContext(), "You have logged in from another device. Please login again.",
                                        Toast.LENGTH_LONG).show();
                                getActivity().finish();
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

        //adding our stringrequest to queue
        Volley.newRequestQueue(getContext()).add(stringRequest);
    }
}