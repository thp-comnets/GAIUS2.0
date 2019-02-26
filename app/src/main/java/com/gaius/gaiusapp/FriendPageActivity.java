package com.gaius.gaiusapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.gaius.gaiusapp.adapters.NewsFeedAdapter;
import com.gaius.gaiusapp.classes.NewsFeed;
import com.gaius.gaiusapp.networking.GlideApp;
import com.gaius.gaiusapp.networking.GlideImageLoadingService;
import com.gaius.gaiusapp.utils.LogOut;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ss.com.bannerslider.Slider;

import static com.gaius.gaiusapp.utils.ResourceHelper.convertImageURLBasedonFidelity;

public class FriendPageActivity extends AppCompatActivity {
    private static String URL = "";
    List<NewsFeed> pagesList;
    RecyclerView recyclerView;
    SharedPreferences prefs;
    RelativeLayout noPages;
    ProgressBar mProgressBar;
    AppCompatButton mButton;
    String base_url, userID;
    Integer position;
    Context mCtx;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_user);

        mCtx = this;

        Slider.init(new GlideImageLoadingService(this));

        String name="", avatar="None", status = "";
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        base_url = prefs.getString("base_url", null);
        URL = base_url + "listUserPages.py";
        noPages = findViewById(R.id.noPages);

        recyclerView = findViewById(R.id.recylcerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            userID = bundle.getString("userID", null);
            name = bundle.getString("name", null);
            avatar = bundle.getString("avatar", null);
            status = bundle.getString("status", null);
            position = bundle.getInt("position", 1000);

            if (userID != null) {
                URL += "?userID="+userID;
                URL += "&token=" + prefs.getString("token", "null");

                recyclerView.setTag("SubFriends");
            }
            bundle.clear();
        }
        else{
            Log.d("yasir","something went wrong no userID in FriendPageActivity bundle");
            finish();
        }

        TextView textViewName = findViewById(R.id.name);
        textViewName.setText(name);

        TextView textViewStatus = findViewById(R.id.status);
        textViewStatus.setText(status);

        ImageView imageViewAvatar = findViewById(R.id.avatarView);

        if (avatar.contains("None")) {
            imageViewAvatar.setImageDrawable(this.getResources().getDrawable(R.drawable.ic_avatar));
        }
        else {
            GlideApp.with(this)
                    .load(avatar)
                    .avatar()
                    .into(imageViewAvatar);
        }

        mProgressBar = findViewById(R.id.friend_progress_bar);

        mButton = findViewById(R.id.friend_button);
        mButton.setSupportBackgroundTintList(this.getResources().getColorStateList(R.color.red_400));
        mButton.setText("Remove");

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String token = prefs.getString("token", "null");
                String URL = prefs.getString("base_url", null) + "modifyFriend.py?token=" + token + "&remove=" + userID;
                v.setVisibility(View.INVISIBLE);
                mProgressBar.setVisibility(View.VISIBLE);

                StringRequest stringRequest = new StringRequest(Request.Method.GET, URL,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Intent returnIntent = new Intent();
                                returnIntent.putExtra("removeIndex", position);
                                setResult(Activity.RESULT_OK, returnIntent);
                                finish();
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                mProgressBar.setVisibility(View.INVISIBLE);
                                mButton.setVisibility(View.VISIBLE);
                                Log.d("Yasir","Error "+error);
                            }
                        });
                Log.d("Yasir","added request "+stringRequest);

                Volley.newRequestQueue(getApplicationContext()).add(stringRequest);
            }
        });

        pagesList = new ArrayList<>();
        loadPages();
    }

    private void loadPages() {

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
                            NewsFeedAdapter adapter = new NewsFeedAdapter(mCtx, pagesList);
                            recyclerView.setAdapter(adapter);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d("Yasir","Json error "+e);

                            if (response.contains("invalid token")) {
                                LogOut.logout(getApplicationContext());
                                Toast.makeText(getApplicationContext(), "You have logged in from another device. Please login again.",
                                        Toast.LENGTH_LONG).show();
                                finish();
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

        Volley.newRequestQueue(getApplicationContext()).add(stringRequest);
    }
}
