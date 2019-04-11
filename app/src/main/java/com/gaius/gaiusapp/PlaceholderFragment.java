package com.gaius.gaiusapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gaius.gaiusapp.interfaces.FragmentVisibleInterface;

public class PlaceholderFragment extends Fragment implements FragmentVisibleInterface {

    public PlaceholderFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given content creation.
     */
    public static  PlaceholderFragment newInstance(int position) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_create_content_placeholder, container, false);
        return rootView;
    }

    @Override
    public void fragmentBecameVisible() {
        //do nothing
    }
}
