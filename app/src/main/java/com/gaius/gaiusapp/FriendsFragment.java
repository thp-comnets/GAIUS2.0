package com.gaius.gaiusapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gaius.gaiusapp.interfaces.FragmentVisibleInterface;

public class FriendsFragment extends Fragment {

    String[] tabArray;
    TabLayout tabLayout;
    private ViewPager mViewPager;
    private FriendsFragment.FragmentPagerAdapter fragmentPagerAdapter;
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

        fragmentPagerAdapter = new FriendsFragment.FragmentPagerAdapter(getFragmentManager());

        mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        mViewPager.setAdapter(fragmentPagerAdapter);

        tabLayout = (TabLayout) view.findViewById(R.id.tabs);
        tabLayout.bringToFront(); //needed to make the tab layout clickable

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                TextView tv;
                //set all titles to inactive
                for (int i = 0; i < tabLayout.getTabCount(); i++) {
                    tv = tabLayout.getTabAt(i).getCustomView().findViewById(R.id.tab_title);
                    tv.setTextColor(getResources().getColor(R.color.white_50));
                }
                //now set the active tab
                tv = tabLayout.getTabAt(position).getCustomView().findViewById(R.id.tab_title);
                tv.setTextColor(getResources().getColor(R.color.white));

                FragmentVisibleInterface fragment = (FragmentVisibleInterface) fragmentPagerAdapter.instantiateItem(mViewPager, position);
                if (fragment != null) {
                    updateNotificationBadge();
                    fragment.fragmentBecameVisible();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        // add custom tab views
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            tabLayout.getTabAt(i).setCustomView(prepareTabView(i));
        }
        updateNotificationBadge();
    }

    private View prepareTabView(int pos) {

        ViewGroup view;

        if (pos == 2) {
            view = (ViewGroup) getLayoutInflater().inflate(R.layout.tab_layout_badge, null);
        } else {
            view = (ViewGroup) getLayoutInflater().inflate(R.layout.tab_layout, null);

        }

        TextView tabTitle = (TextView) view.findViewById(R.id.tab_title);
        tabTitle.setText(tabArray[pos]);

        //set the initial color
        if (pos == 0) {
            tabTitle.setTextColor(getResources().getColor(R.color.white));
        }
        return view;
    }

    public void updateNotificationBadge() {
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
//        MainActivity.setBadge(number);
    }

    public class FragmentPagerAdapter extends FragmentStatePagerAdapter {

        public FragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

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
//
//        @Nullable
//        @Override
//        public CharSequence getPageTitle(int position) {
//            switch (position) {
//                case 0:
//                    return tabArray[0];
//                case 1:
//                    return tabArray[1];
//                case 2:
//                    return tabArray[2];
//            }
//            return null;
//        }

        @Override
        public int getCount() {
            return 3;
        }
    };
}

