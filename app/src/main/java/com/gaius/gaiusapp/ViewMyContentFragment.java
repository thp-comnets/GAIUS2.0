package com.gaius.gaiusapp;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gaius.gaiusapp.interfaces.FragmentVisibleInterface;
import com.gaius.gaiusapp.interfaces.OnFragmentInteractionListener;
import com.gaius.gaiusapp.utils.Constants;

public class ViewMyContentFragment extends Fragment {
    private ViewPager mViewPager;
    private OnFragmentInteractionListener mListener;
    private ViewMyContentFragment.SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager.OnPageChangeListener pageChangeListener;
    TabLayout tabLayout;

    public ViewMyContentFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     @return A new instance of fragment ViewContentFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ViewMyContentFragment newInstance(Integer action) {
        ViewMyContentFragment fragment = new ViewMyContentFragment();
        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_my_content, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new ViewMyContentFragment.SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        tabLayout = (TabLayout) view.findViewById(R.id.tabs);
        tabLayout.bringToFront(); //needed to make the tab layout clickable

        // make sure all icons have the correct opacity
        tabLayout.getTabAt(0).getIcon().setAlpha(255);
        tabLayout.getTabAt(1).getIcon().setAlpha(128);
        tabLayout.getTabAt(2).getIcon().setAlpha(128);
        tabLayout.getTabAt(3).getIcon().setAlpha(128);
        tabLayout.getTabAt(4).getIcon().setAlpha(128);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        pageChangeListener = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                switch (i) {
                    case 0:
                        tabLayout.getTabAt(0).getIcon().setAlpha(255);
                        tabLayout.getTabAt(1).getIcon().setAlpha(128);
                        tabLayout.getTabAt(2).getIcon().setAlpha(128);
                        tabLayout.getTabAt(3).getIcon().setAlpha(128);
                        tabLayout.getTabAt(4).getIcon().setAlpha(128);
                        break;
                    case 1:
                        tabLayout.getTabAt(0).getIcon().setAlpha(128);
                        tabLayout.getTabAt(1).getIcon().setAlpha(255);
                        tabLayout.getTabAt(2).getIcon().setAlpha(128);
                        tabLayout.getTabAt(3).getIcon().setAlpha(128);
                        tabLayout.getTabAt(4).getIcon().setAlpha(128);
                        break;
                    case 2:
                        tabLayout.getTabAt(0).getIcon().setAlpha(128);
                        tabLayout.getTabAt(1).getIcon().setAlpha(128);
                        tabLayout.getTabAt(2).getIcon().setAlpha(255);
                        tabLayout.getTabAt(3).getIcon().setAlpha(128);
                        tabLayout.getTabAt(4).getIcon().setAlpha(128);
                        break;
                    case 3:
                        tabLayout.getTabAt(0).getIcon().setAlpha(128);
                        tabLayout.getTabAt(1).getIcon().setAlpha(128);
                        tabLayout.getTabAt(2).getIcon().setAlpha(128);
                        tabLayout.getTabAt(3).getIcon().setAlpha(255);
                        tabLayout.getTabAt(4).getIcon().setAlpha(128);
                        break;
                    case 4:
                        tabLayout.getTabAt(0).getIcon().setAlpha(128);
                        tabLayout.getTabAt(1).getIcon().setAlpha(128);
                        tabLayout.getTabAt(2).getIcon().setAlpha(128);
                        tabLayout.getTabAt(3).getIcon().setAlpha(128);
                        tabLayout.getTabAt(4).getIcon().setAlpha(255);
                        break;
                }
                //only load content if fragment becomes visible
                FragmentVisibleInterface fragment = (FragmentVisibleInterface) mSectionsPagerAdapter.instantiateItem(mViewPager, i);
                if (fragment != null) {
                    fragment.fragmentBecameVisible();
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        };
        mViewPager.addOnPageChangeListener(pageChangeListener);

        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        /*
        Run this in a runnable to make sure the viewPager's views are already instantiated before triggering the onPageSelected call.
        This is required in order to trigger the fragmentBecameVisible()
         */

        mViewPager.post(new Runnable()
        {
            @Override
            public void run()
            {
                pageChangeListener.onPageSelected(0);
            }
        });
    }

    public void onButtonPressed(Integer action) {
        if (mListener != null) {
            mListener.onFragmentInteraction(action);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
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
        mListener = null;
    }

    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            Fragment fragment = null;
            switch (position) {
                case 0:
                    fragment = NewsFeedFragment.newInstance(Constants.REQUEST_TYPE_MYOWN, Constants.REQUEST_CONTENT_PAGES);
                    break;
                case 1:
                    fragment = NewsFeedFragment.newInstance(Constants.REQUEST_TYPE_MYOWN, Constants.REQUEST_CONTENT_IMAGES);
                    break;
                case 2:
                    fragment = NewsFeedFragment.newInstance(Constants.REQUEST_TYPE_MYOWN, Constants.REQUEST_CONTENT_VIDEOS);
                    break;
                case 3:
                    fragment = PlaceholderFragment.newInstance(position);
                    break;
                default:
                    fragment = PlaceholderFragment.newInstance(position); //TODO there will be another tab for ads
//                    fragment = NewsFeedFragment.newInstance(Constants.REQUEST_TYPE_MYOWN, Constants.REQUEST_CONTENT_ADS);
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return 5;
        }

    }
}
