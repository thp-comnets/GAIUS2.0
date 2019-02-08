package com.gaius.gaiusapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.gaius.gaiusapp.adapters.ImageAdapter;
import com.gaius.gaiusapp.classes.Image;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.gaius.gaiusapp.utils.ResourceHelper.convertImageURLBasedonFidelity;

public class PhotosFragment extends Fragment {
    private static String URL;
    List<Image> imageList;
    RecyclerView recyclerView;
    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        URL = prefs.getString("base_url", null) + "listImages.py";

        return inflater.inflate(R.layout.fragment_images, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        recyclerView =  getView().findViewById(R.id.recylcerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setTag("MainImage");

        imageList = new ArrayList<>();

        loadImages();
    }

    private void loadImages() {
        /*
         * Creating a String Request
         * The request type is GET defined by first parameter
         * The URL is defined in the second parameter
         * Then we have a Response Listener and a Error Listener
         * In response listener we will get the JSON response as a String
         * */
        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            //converting the string to json array object
                            JSONArray array = new JSONArray(response);

                            String fidelity = prefs.getString("fidelity_level", "high");

                            //traversing through all the object
                            for (int i = 0; i < array.length(); i++) {

                                //getting product object from json array
                                JSONObject image = array.getJSONObject(i);

                                ArrayList<String> imagesList = new ArrayList<String>();
                                String [] tmp = image.getString("images").split(";");
                                for (int j=0; j<tmp.length; j++) {
                                    imagesList.add(convertImageURLBasedonFidelity(prefs.getString("base_url", null) + image.getString("url")+tmp[j], fidelity));
                                }

                                //adding the product to product list
                                imageList.add(new Image(
                                        image.getInt("id"),
                                        image.getString("title"),
                                        image.getString("description"),
                                        image.getString("url"),
                                        image.getString("avatar"),
                                        image.getString("thumbnail"),
                                        image.getString("userID"),
                                        image.getString("uploadedSince"),
                                        image.getInt("views"),
                                        imagesList
                                ));
                            }

                            //creating adapter object and setting it to recyclerview
                            ImageAdapter adapter = new ImageAdapter(getContext(), imageList);
                            recyclerView.setAdapter(adapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d("Yasir","Json error "+e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Yasir","Error "+error);
                    }
                });

        Log.d("Yasir","added request "+stringRequest);

        //adding our stringrequest to queue
        Volley.newRequestQueue(getContext()).add(stringRequest);
    }
}
