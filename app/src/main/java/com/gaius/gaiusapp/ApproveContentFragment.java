package com.gaius.gaiusapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.gaius.gaiusapp.adapters.ApproveContentAdapter;
import com.gaius.gaiusapp.classes.Content;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ApproveContentFragment extends Fragment {
    private static String URL = "";
    List<Content> contentList;
    RecyclerView recyclerView;
    SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_web, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        recyclerView =  getView().findViewById(R.id.recylcerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL));

        recyclerView.setTag("ContentApproval");

        contentList = new ArrayList<>();

        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String token = prefs.getString("token", "null");

        URL = prefs.getString("base_url", null) + "listSubmittedContent.py";
        URL += "?token="+token;

        loadChannels();
    }

    private void loadChannels() {
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

                            if (array.length() == 0) {
                                ImageView tmp = getActivity().findViewById(R.id.nothing_to_approve_image);
                                tmp.setVisibility(View.VISIBLE);
                                TextView tmp2 = getActivity().findViewById(R.id.nothing_to_approve_text);
                                tmp2.setVisibility(View.VISIBLE);
                            }

                            //traversing through all the object
                            for (int i = 0; i < array.length(); i++) {

                                //getting product object from json array
                                JSONObject channel = array.getJSONObject(i);

                                //adding the product to product list
                                Content c = new Content(
                                        channel.getInt("id"),
                                        channel.getString("title"),
                                        channel.getString("url"),
                                        channel.getString("uploadTime"),
                                        channel.getString("type"),
                                        channel.getString("description"),
                                        channel.getString("thumbnail"),
                                        "-100"
                                );
                                c.setName(channel.getString("name"));
                                contentList.add(c);
                            }

                            //creating adapter object and setting it to recyclerview
                            ApproveContentAdapter adapter = new ApproveContentAdapter(getContext(), contentList);
                            recyclerView.setAdapter(adapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d("Yasir","Json error "+e);
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