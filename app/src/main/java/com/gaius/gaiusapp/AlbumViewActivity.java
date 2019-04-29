package com.gaius.gaiusapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.gaius.gaiusapp.adapters.AlbumAdapter;
import com.gaius.gaiusapp.utils.StringHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.gaius.gaiusapp.utils.ResourceHelper.convertImageURLBasedonFidelity;


public class AlbumViewActivity extends AppCompatActivity {
    Toolbar toolbar;
    CollapsingToolbarLayout collapsingToolbarLayout;
    RecyclerView recyclerView;
    ArrayList<String> imagesURLs;
    private SharedPreferences prefs;
    Context mCtx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.album_view_activity);

        mCtx = this;

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);

        collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));
        collapsingToolbarLayout.setContentScrimColor(getResources().getColor(R.color.colorPrimary));

        //change the color of the back arrow
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {
                TypedArray a = getTheme().obtainStyledAttributes(R.style.AppTheme, new int[] {android.R.attr.homeAsUpIndicator});
                int attributeResourceId = a.getResourceId(0, 0);
                Drawable upArrow = ContextCompat.getDrawable(mCtx, attributeResourceId);

                if (offset < -50) {
                    upArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
                } else {
                    upArrow.setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);

                }
                getSupportActionBar().setHomeAsUpIndicator(upArrow);
            }
        });

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        Uri data = getIntent().getData();

        if (data != null && data.toString().contains("http://gaiusnetworks.com/images/")) {
            String albumURL =  "." + data.toString().replace("http://gaiusnetworks.com","");
            String mPageUrl = prefs.getString("base_url", null) + "getAlbum.py?albumID="+albumURL;
            loadAlbum(mPageUrl, albumURL);
        }
        else {
            Bundle bundle = getIntent().getExtras();
            String url = bundle.getString("URL");
            if (url != null) {
                String mPageUrl = prefs.getString("base_url", null) + "getAlbum.py?albumID="+url;
                Log.d("thp", "album " + mPageUrl);
                loadAlbum(mPageUrl, url);
            }
            else {
                String name = bundle.getString("name", "No name");
                String description = bundle.getString("description", "No title");
                String uploadtime = bundle.getString("uploadtime", "");
                imagesURLs = bundle.getStringArrayList("imagesURLs");
                renderAlbum(name, description, uploadtime);
            }
        }
    }

    private void renderAlbum (String name, String description, String uploadtime) {

        TextView nameTextView = findViewById(R.id.name);
        nameTextView.setText(name);

        TextView descriptionTextView = findViewById(R.id.description);
        descriptionTextView.setText(description);

        TextView dateTextView = findViewById(R.id.date);
        dateTextView.setText(uploadtime);

        // use only the first 3 words of the description
        collapsingToolbarLayout.setTitle(StringHelper.getFirstNWords(description, 3));
        collapsingToolbarLayout.setCollapsedTitleTextColor(getResources().getColor(R.color.white));

        recyclerView = findViewById(R.id.images_recyclerview);
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

//        // managing fidelity for images requests
//        if (! fidelity.equals("high")) {
//            for (int i=0; i < imagesURLs.size(); i++) {
//                int index = imagesURLs.get(i).lastIndexOf(".");
//                String[] tmp =  {imagesURLs.get(i).substring(0, index), imagesURLs.get(i).substring(index)};
//                imagesURLs.set(i, tmp[0]+"_"+fidelity+tmp[1]);
//            }
//        }

        AlbumAdapter adapter = new AlbumAdapter(this, imagesURLs);
        recyclerView.setAdapter(adapter);
    }

    private void loadAlbum(final String URL, final String albumURL) {

        Log.d("thp", "Requesting album " + URL);
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {

                            //converting the string to json array object
                            JSONArray array = new JSONArray(response);

                            if (array.length() == 0) {
                                Log.d("Yasir", "JSON is empty ");
                            }

                            //there should be only one entry
                            for (int i = 0; i < array.length(); i++) {

                                //getting product object from json array
                                JSONObject item = array.getJSONObject(i);

                                imagesURLs = new ArrayList<String>();
                                String[] tmp = item.getString("images").split(";");

                                String fidelity = prefs.getString("fidelity_level", "high");

                                for (int j = 0; j < tmp.length; j++) {
                                    imagesURLs.add(convertImageURLBasedonFidelity(prefs.getString("base_url", null) + albumURL + tmp[j], fidelity));
                                }

                                renderAlbum(item.getString("name"), item.getString("description"), item.getString("uploadTime"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d("Yasir", "Json error " + e);
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
        Volley.newRequestQueue(this).add(stringRequest);
    }

    //handle the back arrow press in the toolbar
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
