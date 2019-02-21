package com.gaius.gaiusapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gaius.gaiusapp.interfaces.FragmentVisibleInterface;

public class FriendsFragment extends Fragment {
    String[] tabArray;
    static TabLayout tabLayout;
    static Context mCtx;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_friends, null);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tabArray = getResources().getStringArray(R.array.tabTitles);
        mCtx = getContext();

        final FragmentPagerAdapter mFragmentPagerAdapter = new FragmentPagerAdapter(getChildFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        return new MyFriendsFragment();
                    case 1:
                        return new MyFriendsSearchFragment();
                    case 2:
                        return new MyFriendsRequestsFragment();
                    default:
                        return null;
                }
            }

            @Override
            public int getCount() {
                return 3;
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case 0:
                        return tabArray[0];
                    case 1:
                        return tabArray[1];
                    case 2:
                        return tabArray[2];
                }
                return null;
            }
        };

        final ViewPager viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        viewPager.setAdapter(mFragmentPagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                FragmentVisibleInterface fragment = (FragmentVisibleInterface) mFragmentPagerAdapter.instantiateItem(viewPager, position);
                if (fragment != null) {
                    updateNotificationBadge();
                    fragment.fragmentBecameVisible();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        tabLayout = (TabLayout) view.findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

        // add tab views
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            tabLayout.getTabAt(i).setCustomView(prepareTabView(i));
        }
        updateNotificationBadge();
    }

    private View prepareTabView(int pos) {
        ViewGroup view = (ViewGroup) getLayoutInflater().inflate(R.layout.tab_layout, null);
        TextView tabTitle = (TextView) view.findViewById(R.id.tab_title);
        tabTitle.setText(tabArray[pos]);

        if (pos == 2) {
            View friendBadgeView = getLayoutInflater().inflate(R.layout.badge_layout_tab, view, false);
            view.addView(friendBadgeView);
        }
        return view;
    }

    static public void updateNotificationBadge() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mCtx);
        int number = prefs.getInt("pending-requests", 0);

        View v = ((ViewGroup) tabLayout.getChildAt(0)).getChildAt(2);
        TextView badgeView = (TextView)v.findViewById(R.id.badge);

        if (number <= 0) {
            badgeView.setVisibility(View.GONE);
        } else {
            badgeView.setVisibility(View.VISIBLE);
            badgeView.setText(""+number);
        }
        MainActivity.setBadge(mCtx, number);
    }
}

