package com.gaius.gaiusapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import java.util.ArrayList;

public class CreateContentPagesFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_create_content_pages, container, false);
        ImageView uploadImage = (ImageView) rootView.findViewById(R.id.imageViewWebpage);
        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
                Permissions.check(getActivity(), permissions, null, null, new PermissionHandler() {
                    @Override
                    public void onGranted() {
                        Intent i = new Intent(getContext(), CreatePageActivity.class);
                        getContext().startActivity(i);
                    }

                    @Override
                    public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                        super.onDenied(context, deniedPermissions);
                        Toast.makeText(getActivity(), "If you reject this permission, you can not use this functionality.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        ImageView captureImage = (ImageView) rootView.findViewById(R.id.imageViewBlog);
        captureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
                Permissions.check(getActivity(), permissions, null, null, new PermissionHandler() {
                    @Override
                    public void onGranted() {
                        Intent i = new Intent(getContext(), CreateBlogActivity.class);
                        getContext().startActivity(i);
                    }

                    @Override
                    public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                        super.onDenied(context, deniedPermissions);
                        Toast.makeText(getActivity(), "If you reject this permission, you can not use this functionality.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        return rootView;
    }
}
