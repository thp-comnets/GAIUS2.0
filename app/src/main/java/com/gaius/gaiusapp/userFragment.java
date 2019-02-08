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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.gaius.gaiusapp.adapters.NewsFeedAdapter;
import com.gaius.gaiusapp.classes.NewsFeed;
import com.gaius.gaiusapp.utils.LogOut;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.gaius.gaiusapp.utils.ResourceHelper.convertImageURLBasedonFidelity;

public class userFragment extends Fragment {
    private static String URL = "";
    List<NewsFeed> pagesList;
    RecyclerView recyclerView;
    SharedPreferences prefs;
    RelativeLayout noPages;
    String base_url;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        return inflater.inflate(R.layout.fragment_user, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        String userID, name="", avatar="None";
        base_url = prefs.getString("base_url", null);
        URL = base_url + "listUserPages.py";
        noPages = view.findViewById(R.id.noPages);

        recyclerView =  getView().findViewById(R.id.recylcerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // reading if there is a bundle, used to request and display a channel sub-pages
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            userID = bundle.getString("userID", null);
            name = bundle.getString("name", null);
            avatar = bundle.getString("avatar", null);

            if (userID != null) {
                URL += "?userID="+userID;
                URL += "&token=" + prefs.getString("token", "null");

                recyclerView.setTag("SubFriends");
            }
            bundle.clear();
        }
        else{
            Log.d("yasir","something went wrong no userID in userFragment bundle");
            getActivity().finish();
        }

        TextView textViewName = getView().findViewById(R.id.name);
        textViewName.setText(name);

        ImageView imageViewAvatar = getView().findViewById(R.id.avatarView);

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.error(R.drawable.ic_avatar);

        if (avatar.contains("None")) {
            //loading the image
            Glide.with(getContext())
                    .load(R.drawable.ic_avatar)
                    .into(imageViewAvatar);
        }
        else {
            //loading the image
            Glide.with(getContext())
                    .setDefaultRequestOptions(requestOptions)
                    .load(avatar)
                    .into(imageViewAvatar);
        }


        pagesList = new ArrayList<>();
        loadPages();
    }

    private void loadPages() {
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
                                noPages.setVisibility(View.VISIBLE);
                            }

                            String fidelity = prefs.getString("fidelity_level", "high");

                            //traversing through all the object
                            for (int i = 0; i < array.length(); i++) {

                                //getting product object from json array
                                JSONObject newsFeed = array.getJSONObject(i);

                                ArrayList<String> imagesList = new ArrayList<String>();

                                if (newsFeed.has("images")) {
                                    String [] tmp = newsFeed.getString("images").split(";");
                                    for (int j=0; j<tmp.length; j++) {
                                        imagesList.add(convertImageURLBasedonFidelity(base_url+newsFeed.getString("url")+tmp[j], fidelity));
                                    }
                                }

                                pagesList.add(new NewsFeed(
                                        newsFeed.getInt("id"),
                                        newsFeed.getString("name"),
                                        newsFeed.getString("uploadTime"),
                                        newsFeed.getString("avatar"),
                                        newsFeed.getString("thumbnail"),
                                        newsFeed.getString("title"),
                                        newsFeed.getString("description"),
                                        newsFeed.getString("url"),
                                        newsFeed.getString("type"),
                                        newsFeed.getString("liked"),
                                        false,
                                        imagesList
                                ));
                            }

                            //creating adapter object and setting it to recyclerview
                            NewsFeedAdapter adapter = new NewsFeedAdapter(getContext(), pagesList);
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
