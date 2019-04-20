package com.gaius.gaiusapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class CreateContentAdsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_create_content_ads, container, false);
        ImageView uploadImage = (ImageView) rootView.findViewById(R.id.adViewUpload);
        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadAd();
            }
        });

        TextView uploadImageTextView = (TextView) rootView.findViewById(R.id.textViewUpload);
        uploadImageTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadAd();
            }
        });
        return rootView;
    }

    private void uploadAd() {
        Intent i = new Intent(getContext(), CreateContentAdActivity.class);
        getContext().startActivity(i);
    }
}
