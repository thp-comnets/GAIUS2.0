package com.gaius.gaiusapp;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gigamole.navigationtabstrip.NavigationTabStrip;

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
                        fragment = new MyFriendsSearchFragment();
                        loadFragment(fragment);
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

