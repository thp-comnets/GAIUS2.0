package com.gaius.gaiusapp;

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
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.gaius.gaiusapp.adapters.NewsFeedAdapter;
import com.gaius.gaiusapp.classes.NewsFeed;
import com.gaius.gaiusapp.utils.LogOut;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.gaius.gaiusapp.utils.ResourceHelper.convertImageURLBasedonFidelity;

public class NewsFeedFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    String base_URL;
    List<NewsFeed> newsFeedList;
    SwipeRefreshLayout swipeLayout;
    RecyclerView recyclerView;
    SharedPreferences prefs;
    RelativeLayout noFriends, noInternet, error500;
    Button buttonReturnToTop;
    NewsFeedAdapter adapter;
    Context mCtx;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_newsfeed, null);
        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
        mCtx = getActivity().getApplicationContext();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        noFriends = view.findViewById(R.id.noFriends);

        prefs = PreferenceManager.getDefaultSharedPreferences(mCtx);
        base_URL = prefs.getString("base_url", null);

        noInternet = view.findViewById(R.id.no_internet);
        noInternet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadPages();
            }
        });

        error500 = view.findViewById(R.id.error_500);

        recyclerView =  getView().findViewById(R.id.recylcerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setVisibility(View.GONE);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (((LinearLayoutManager) recyclerView.getLayoutManager()).findLastVisibleItemPosition() <= 1) {
                    buttonReturnToTop.setVisibility(View.GONE);
                } else {
                    buttonReturnToTop.setVisibility(View.VISIBLE);
                }
            }
        });

        buttonReturnToTop = (Button) view.findViewById(R.id.button_return_to_top);
        buttonReturnToTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.smoothScrollToPosition(0);
                buttonReturnToTop.setVisibility(View.GONE);
            }
        });

        newsFeedList = new ArrayList<>();
        adapter = new NewsFeedAdapter(getContext(), newsFeedList);
        recyclerView.setAdapter(adapter);
        loadPages();
    }

    @Override
    public void onResume() {
        super.onResume();

//        thp: disabled this to disable refreshing when activity brought to foreground
//        newsFeedList = new ArrayList<>();
//        loadPages();
    }

    private void loadPages() {
        AndroidNetworking.get(base_URL+"listPages.py")
                .addQueryParameter("token", prefs.getString("token", "null"))
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONArray(new JSONArrayRequestListener() {
                    @Override
                    public void onResponse(JSONArray response) {

                        Log.d("thp", "Response as JSON " + response);

                        try {
                            noFriends.setVisibility(View.GONE);

                            if (response.length() == 0 ) {
                                noFriends.setVisibility(View.VISIBLE);
                            }

                            String fidelity = prefs.getString("fidelity_level", "high");
                            JSONObject newsFeed;
                            //traversing through all the object
                            for (int i = 0; i < response.length(); i++) {

                                //getting product object from json array
                                newsFeed = response.getJSONObject(i);

                                if (newsFeed.has("pending-requests")) {
                                    int number = newsFeed.getInt("pending-requests");

                                    // save the pending requests to the sharedprefs
                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.putInt("pending-requests", number);
                                    editor.apply();

                                    MainActivity.setBadge(number);

                                }

                                ArrayList<String> imagesList = new ArrayList<String>();
                                String [] images = newsFeed.getString("images").split(";");
                                for (int j=0; j<images.length; j++) {
                                    imagesList.add(convertImageURLBasedonFidelity(base_URL+newsFeed.getString("url")+images[j], fidelity));
                                }

                                newsFeedList.add(new NewsFeed(
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
                                        true,
                                        imagesList

                                ));
                            }

                            adapter = new NewsFeedAdapter(getContext(), newsFeedList);
                            recyclerView.setAdapter(adapter);
                            noInternet.setVisibility(View.GONE);
                            error500.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d("Yasir","Json error "+e);
                        }

                    }
                    @Override
                    public void onError(ANError error) {

                        switch (error.getErrorCode()) {
                            case 401:
                                LogOut.logout(getActivity());
                                Toast.makeText(getContext(), "You have logged in from another device. Please login again.",
                                        Toast.LENGTH_LONG).show();
                                Intent i = new Intent(getContext(), LoginActivity.class);
                                startActivity(i);
                                getActivity().finish();
                                break;
                            case 500:
                                error500.setVisibility(View.VISIBLE);
                                buttonReturnToTop.setVisibility(View.GONE);
                                noInternet.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.GONE);
                                Log.d("Yasir","Error 500"+error);
                                break;
                            default:
                                noInternet.setVisibility(View.VISIBLE);
                                error500.setVisibility(View.GONE);
                                buttonReturnToTop.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.GONE);
                                Log.d("Yasir","Error no Internet "+error);

                        }
                    }
                });
    }

    @Override
    public void onRefresh() {
        newsFeedList = new ArrayList<>();
        loadPages();
        swipeLayout.setRefreshing(false);
    }
}

