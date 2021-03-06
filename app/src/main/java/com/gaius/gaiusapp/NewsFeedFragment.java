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
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.gaius.gaiusapp.adapters.NewsFeedAdapter;
import com.gaius.gaiusapp.classes.NewsFeed;
import com.gaius.gaiusapp.interfaces.FragmentVisibleInterface;
import com.gaius.gaiusapp.interfaces.OnAdapterInteractionListener;
import com.gaius.gaiusapp.interfaces.OnFragmentInteractionListener;
import com.gaius.gaiusapp.networking.GlideImageLoadingService;
import com.gaius.gaiusapp.utils.Constants;
import com.gaius.gaiusapp.utils.LogOut;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ss.com.bannerslider.Slider;

import static android.app.Activity.RESULT_OK;
import static com.gaius.gaiusapp.utils.ResourceHelper.convertImageURLBasedonFidelity;

public class NewsFeedFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, FragmentVisibleInterface, OnAdapterInteractionListener {
    String base_URL;
    List<NewsFeed> newsFeedList;
    SwipeRefreshLayout swipeLayout;
    RecyclerView recyclerView;
    SharedPreferences prefs;
    RelativeLayout noContentLayout, noInternet, error500;
    TextView noContentTextView;
    ShimmerFrameLayout mShimmerViewContainer;
    Button buttonReturnToTop, addFriendsButton, subscribeChannelsButton, createContentButton;
    NewsFeedAdapter adapter;
    Context mCtx;
    private static final String ARG_PARAM1 = "typeParam";
    private static final String ARG_PARAM2 = "contentParam";
    private static final String ARG_PARAM3 = "userIDParam";
    Integer contentParam, typeParam;
    String userIDParam;
    private OnFragmentInteractionListener mListener;
    private OnAdapterInteractionListener mAdapterListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     */
    public static NewsFeedFragment newInstance(Integer type, Integer content) {
        NewsFeedFragment fragment = new NewsFeedFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, type);
        args.putInt(ARG_PARAM2, content);
        fragment.setArguments(args);
        return fragment;
    }

    // for specific user pages (not my own)
    public static NewsFeedFragment newInstance(Integer type, Integer content, String userID) {
        NewsFeedFragment fragment = new NewsFeedFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, type);
        args.putInt(ARG_PARAM2, content);
        args.putString(ARG_PARAM3, userID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCtx = getActivity();
        mAdapterListener = this;

        if (getArguments() != null) {
            typeParam = getArguments().getInt(ARG_PARAM1);
            contentParam = getArguments().getInt(ARG_PARAM2);
            userIDParam = getArguments().getString(ARG_PARAM3, "");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 0) {
                Integer pos = data.getIntExtra("position", -1);
                Integer status = data.getIntExtra("friendstatus", -1);
                if (pos != -1) {
                    adapter.updateItemFriendsStatus(pos, status);
                }
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_newsfeed, null);
        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        noContentLayout = view.findViewById(R.id.noContent);
        noContentTextView = view.findViewById(R.id.noContentTextView);
        addFriendsButton = view.findViewById(R.id.add_friend_button);
        subscribeChannelsButton = view.findViewById(R.id.subscribe_button);
        createContentButton = view.findViewById(R.id.create_content_button);

        mShimmerViewContainer = view.findViewById(R.id.shimmer_view_container);
        mShimmerViewContainer.startShimmer();

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
                if (((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition() == 0) {
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
        adapter = new NewsFeedAdapter(getContext(), newsFeedList, typeParam, this);
        recyclerView.setAdapter(adapter);

        //if this instance is the newsfeed or friends page, load content right away
        if (typeParam.equals(Constants.REQUEST_TYPE_NEWSFEED) || typeParam.equals(Constants.REQUEST_TYPE_FRIEND)) {
            this.fragmentBecameVisible();
        }

//        loadPages();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        Log.d("thp", "attach NewsFeedFragment");

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
//        Log.d("thp", "detach NewsFeedFragment " + contentParam + " " + typeParam);
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        Slider.init(new GlideImageLoadingService(mCtx));
    }

    private void loadPages() {

        String query, url;
        switch (typeParam) {
            case Constants.REQUEST_TYPE_NEWSFEED:
                query = typeParam + "" + contentParam + prefs.getString("token", "null") + userIDParam;
                url = base_URL+"listContents.py";
                noContentTextView.setText("It's quite empty here...\n\nSo, let\'s get started and add friends or subscribe to channels.");
                addFriendsButton.setVisibility(View.VISIBLE); //will become visible when the layout becomes visible
                subscribeChannelsButton.setVisibility(View.VISIBLE);

                addFriendsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mListener != null) {
                            mListener.onFragmentInteraction(Constants.SELECT_FRIENDS_FRAGMENT);
                        }
                    }
                });
                subscribeChannelsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mListener != null) {
                            mListener.onFragmentInteraction(Constants.SELECT_VIEW_CONTENT_FRAGMENT);
                        }
                    }
                });
                break;
            case Constants.REQUEST_TYPE_ALL:
                query = typeParam + "" + contentParam + prefs.getString("token", "null");
                url = base_URL+"listContents.py";
                noContentTextView.setText("No content available...");
                break;
            case Constants.REQUEST_TYPE_MYOWN:
                query = typeParam + "" + contentParam + prefs.getString("token", "null");
                url = base_URL+"listContents.py";
                noContentTextView.setText("You haven't created content yet...");
                createContentButton.setVisibility(View.VISIBLE); //will become visible when the layout becomes visible
                createContentButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mListener != null) {
                            mListener.onFragmentInteraction(Constants.SELECT_CREATE_CONTENT_FRAGMENT);
                        }
                    }
                });
                break;
            case Constants.REQUEST_TYPE_FRIEND:
                query = typeParam + "" + contentParam + prefs.getString("token", "null") + userIDParam;
                url = base_URL+"listContents.py";
                noContentTextView.setText("This user hasn't created content yet...");
                break;
            default:
                query = typeParam + "" + contentParam + prefs.getString("token", "null");
                url = base_URL+"listContents.py";
                noContentTextView.setText("No content available..");

        }

        AndroidNetworking.get(url)
                .addQueryParameter("token", prefs.getString("token", "null")) //TODO remove this later
                .addQueryParameter("req", query)
                .addQueryParameter("cm-token", prefs.getString("cm-token", "null"))
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONArray(new JSONArrayRequestListener() {
                    @Override
                    public void onResponse(JSONArray response) {

                        swipeLayout.setRefreshing(false);

                        Log.d("thp", "Response as JSON " + response);

                        try {

                            if (response.length() == 0 ) {
                                noContentLayout.setVisibility(View.VISIBLE);
                                mShimmerViewContainer.stopShimmer();
                                mShimmerViewContainer.setVisibility(View.GONE);
                            } else {
                                recyclerView.setVisibility(View.VISIBLE);
                                noContentLayout.setVisibility(View.GONE);
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

                                    //signal the badge change up to the MainActivity. it can be null if the response arrives when the fragment is detached
                                    // TODO maybe we should skip the return then?
                                    if (mListener != null) {
                                        mListener.onFragmentInteraction(Constants.UPDATE_BADGE_NOTIFICATION_LAUNCHER);
                                    }

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
                                        newsFeed.getString("userID"),
                                        newsFeed.getString("type"),
                                        newsFeed.getString("liked"),
                                        newsFeed.getString("views"),
                                        newsFeed.getString("published"),
                                        Integer.parseInt(newsFeed.getString("friendStatus")),
                                        true,
                                        imagesList

                                ));
                            }
                            //FIXME check context, might be null
                            adapter = new NewsFeedAdapter(getContext(), newsFeedList, typeParam, mAdapterListener);
                            recyclerView.setAdapter(adapter);
                            noInternet.setVisibility(View.GONE);
                            error500.setVisibility(View.GONE);
                            mShimmerViewContainer.stopShimmer();
                            mShimmerViewContainer.setVisibility(View.GONE);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d("thp","Json error "+e);
                        } catch (Exception e) {
                            error500.setVisibility(View.VISIBLE);
                            buttonReturnToTop.setVisibility(View.GONE);
                            noInternet.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.GONE);
                            mShimmerViewContainer.stopShimmer();
                            mShimmerViewContainer.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Exception in parsing the response", Toast.LENGTH_LONG).show();
                            Log.d("thp","Exception " + e);
                        }

                    }
                    @Override
                    public void onError(ANError error) {

                        switch (error.getErrorCode()) {
                            case 401:
                                LogOut.logout(getActivity());
                                Toast.makeText(getContext(), "You have logged in from another device. Please login again.",
                                        Toast.LENGTH_LONG).show();
                                Intent i = new Intent(getContext(), LoginSMSActivity.class);
                                startActivity(i);
                                getActivity().finish();
                                break;
                            case 500:
                                error500.setVisibility(View.VISIBLE);
                                buttonReturnToTop.setVisibility(View.GONE);
                                noInternet.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.GONE);
                                mShimmerViewContainer.stopShimmer();
                                mShimmerViewContainer.setVisibility(View.GONE);
                                Log.d("thp","Error 500"+error);
                                break;
                            default:
                                noInternet.setVisibility(View.VISIBLE);
                                error500.setVisibility(View.GONE);
                                buttonReturnToTop.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.GONE);
                                mShimmerViewContainer.stopShimmer();
                                mShimmerViewContainer.setVisibility(View.GONE);
                                Log.d("thp","Error no Internet "+error);

                        }
                    }
                });
    }

    @Override
    public void onRefresh() {
        noInternet.setVisibility(View.GONE);
        error500.setVisibility(View.GONE);

        if (recyclerView.getVisibility() == View.GONE) {
            mShimmerViewContainer.setVisibility(View.VISIBLE);
            mShimmerViewContainer.startShimmer();
        }
        newsFeedList = new ArrayList<>();
        loadPages();
    }

    @Override
    public void fragmentBecameVisible() {
        newsFeedList = new ArrayList<>();
        loadPages();
    }

    @Override
    public void onAdapterInteraction(Integer position) {
        NewsFeed n = newsFeedList.get(position);
        Intent intent = new Intent(mCtx, FriendPageActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("userID", n.getUserID());
        bundle.putString("name", n.getName());
        bundle.putInt("position", position);
        bundle.putString("status", "I'm using Gaius");
        bundle.putString("avatar", prefs.getString("base_url", null) + n.getAvatar());
        bundle.putInt("friendstatus", n.getFriendStatus());
        intent.putExtras(bundle);
        startActivityForResult(intent, 0);
    }
}

