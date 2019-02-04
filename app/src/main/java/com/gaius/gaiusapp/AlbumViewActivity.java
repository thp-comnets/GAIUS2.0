package com.gaius.gaiusapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.gaius.gaiusapp.adapters.AlbumAdapter;

import java.util.ArrayList;


public class AlbumViewActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    ArrayList<String> imagesURLs;
    SharedPreferences prefs;
    String fidelity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setContentView(R.layout.album_view_activity);

        super.onCreate(savedInstanceState);

        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        fidelity = prefs.getString("fidelity_level", "high");

        Uri data = getIntent().getData();

        if (data != null && data.toString().contains("http://gaiusnetworks.com/images/")) {
            Intent intent = this.getIntent();
            Bundle bundle = intent.getExtras();
            String albumURL =  "." + data.toString().replace("http://gaiusnetworks.com","");
            String mPageUrl = "http://91.230.41.34:8080/test/getAlbum.py?albumID="+albumURL;
            loadAlbum(mPageUrl, albumURL);
        }
        else {
            Bundle bundle = getIntent().getExtras();
            imagesURLs = bundle.getStringArrayList("imagesURLs");
            renderAlbum();
        }
    }

    private void renderAlbum () {
        recyclerView = findViewById(R.id.images_recyclerview);
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // managing fidelity for images requests
        if (! fidelity.equals("high")) {
            for (int i=0; i < imagesURLs.size(); i++) {

                int index = imagesURLs.get(i).lastIndexOf(".");
                String[] tmp =  {imagesURLs.get(i).substring(0, index), imagesURLs.get(i).substring(index)};
                imagesURLs.set(i, tmp[0]+"_"+fidelity+tmp[1]);
            }
        }

        AlbumAdapter adapter = new AlbumAdapter(this, imagesURLs);
        recyclerView.setAdapter(adapter);
    }

    private void loadAlbum(final String URL, final String albumURL) {
        /*
         * Creating a String Request
         * The request type is GET defined by first parameter
         * The URL is defined in the second parameter
         * Then we have a Response Listener and a Error Listener
         * In response listener we will get the JSON response as a String
         * */
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        imagesURLs = new ArrayList<String>();
                        String [] tmp = response.replace("\n", "").split(";");

                        for (int j=0; j<tmp.length; j++) {
                            imagesURLs.add("http://91.230.41.34:8080/test/"+albumURL+tmp[j]);
                        }
                        renderAlbum();
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
}
