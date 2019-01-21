package com.gaiusnetworks.gaius;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ContentFragment extends Fragment {
    List<Content> contentList;
    RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_content, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        recyclerView = getView().findViewById(R.id.recylcerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        contentList = new ArrayList<>();

//        // reading if there is a bundle, used to request and display a channel sub-pages
//        Bundle bundle = this.getArguments();
//        if (bundle != null) {
//            String userID = bundle.getString("userID", null);
//            if (userID != null) {
//                URL += "?userID="+userID;
//                recyclerView.setTag("Sub");
//            }
//            bundle.clear();
//        }
//        else{
//            recyclerView.setTag("Main");
//        }

        loadItems();
    }

    private void loadItems() {

        //adding the product to product list
        contentList.add(new Content(0, "Browse Web", R.drawable.ic_web_animation));
        contentList.add(new Content(1, "Browse Videos", R.drawable.ic_video_animation));
        contentList.add(new Content(2, "Browse Photos", R.drawable.ic_photos_animation));
        contentList.add(new Content(3, "Create Content", R.drawable.ic_create));
        contentList.add(new Content(4, "My Content", R.drawable.ic_my_content));

        //creating adapter object and setting it to recyclerview
        ContentsAdapter adapter = new ContentsAdapter(getContext(), contentList);
        recyclerView.setAdapter(adapter);
    }
}

