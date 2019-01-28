package com.gaius.gaiusapp;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
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
import com.gaius.gaiusapp.utils.LogOut;
import com.gigamole.navigationtabstrip.NavigationTabStrip;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FriendsFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_friends, null);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        final NavigationTabStrip navigationTabStrip = (NavigationTabStrip) getActivity().findViewById(R.id.top_navigation_bar);
        navigationTabStrip.setBackgroundColor(Color.BLACK);

        navigationTabStrip.setOnTabStripSelectedIndexListener(new NavigationTabStrip.OnTabStripSelectedIndexListener() {
            @Override
            public void onStartTabSelected(String title, int index) {
                Fragment fragment = null;

                switch (index) {
                    case 0:
                        fragment = new MyFriendsFragment();
                        loadFragment(fragment);
                        break;
                    case 1:
                        break;
                    case 2:
                        fragment = new MyFriendsRequestsFragment();
                        loadFragment(fragment);
                        break;
                }
            }

            @Override
            public void onEndTabSelected(String title, int index) {

            }
        });

        navigationTabStrip.setTabIndex(0, true);
        loadFragment(new MyFriendsFragment());
    }

    private boolean loadFragment(Fragment fragment) {
        //switching fragment
        if (fragment != null) {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.tab_container, fragment)
                    .commit();

            return true;
        }
        return false;
    }
}

