package com.gaius.gaiusapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

public class CreateContentPagesFragment extends Fragment {


    AlertDialog alertD;
    EditText editTextPagename, editTextDescription;
    TextInputLayout editTextPagenameLayout, editTextDescriptionLayout;
//    ProgressDialog progress;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_create_content_pages, container, false);
        ImageView uploadImage = (ImageView) rootView.findViewById(R.id.imageViewWebpage);
        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), CreativeWebCreation.class);
                getContext().startActivity(i);
            }
        });

        ImageView captureImage = (ImageView) rootView.findViewById(R.id.imageViewBlog);
        captureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), SimpleWebCreation.class);
                getContext().startActivity(i);
            }
        });
        return rootView;
    }
}
