package com.gaius.gaiusapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.gaius.gaiusapp.interfaces.OnFragmentInteractionListener;
import com.gaius.gaiusapp.networking.GlideApp;
import com.gaius.gaiusapp.networking.GlideImageLoadingService;
import com.gaius.gaiusapp.utils.Constants;

import ss.com.bannerslider.Slider;

public class FriendPageActivity extends AppCompatActivity implements OnFragmentInteractionListener {
    SharedPreferences prefs;
    RelativeLayout noPages;
    AppCompatButton mButton;
    String base_url, userID;
    Integer position;
    Context mCtx;
    Toolbar toolbar;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_userpage);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);

        collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));
        collapsingToolbarLayout.setContentScrimColor(getResources().getColor(R.color.colorPrimary));

        //change the color of the back arrow
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {
                TypedArray a = getTheme().obtainStyledAttributes(R.style.AppTheme, new int[] {android.R.attr.homeAsUpIndicator});
                int attributeResourceId = a.getResourceId(0, 0);
                Drawable upArrow = getResources().getDrawable(attributeResourceId);

                if (offset < -450) {
                    upArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
                } else {
                    upArrow.setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);

                }
                getSupportActionBar().setHomeAsUpIndicator(upArrow);
            }
        });

        mCtx = this;

        Slider.init(new GlideImageLoadingService(this));

        String name="", avatar="None", status = "";
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        base_url = prefs.getString("base_url", null);
        noPages = findViewById(R.id.noPages);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            userID = bundle.getString("userID", null);
            name = bundle.getString("name", null);
            avatar = bundle.getString("avatar", null);
            status = bundle.getString("status", null);
            position = bundle.getInt("position", 1000);

            collapsingToolbarLayout.setTitle(name);
            collapsingToolbarLayout.setCollapsedTitleTextColor(getResources().getColor(R.color.white));

//            if (userID != null) {
//                URL += "?userID="+userID;
//                URL += "&token=" + prefs.getString("token", "null");
//                recyclerView.setTag("SubFriends");
//            }
            bundle.clear();
        }
        else{
            Log.d("yasir","something went wrong no userID in FriendPageActivity bundle");
            finish();
        }

        TextView textViewName = findViewById(R.id.name);
        textViewName.setText(name);

        TextView textViewStatus = findViewById(R.id.status);
        textViewStatus.setText(status);

        ImageView imageViewAvatar = findViewById(R.id.avatarView);

        if (avatar.contains("None")) {
            imageViewAvatar.setImageDrawable(this.getResources().getDrawable(R.drawable.ic_avatar));
        }
        else {
            GlideApp.with(this)
                    .load(avatar)
                    .avatar()
                    .into(imageViewAvatar);
        }

        mButton = findViewById(R.id.unfriend_button);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(mCtx);

                builder.setTitle("Remove friend?");
                builder.setMessage("Do you want to remove this friend?");

                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        String token = prefs.getString("token", "null");
                        String URL = prefs.getString("base_url", null) + "modifyFriend.py?token=" + token + "&remove=" + userID;
                        v.setVisibility(View.INVISIBLE);

                        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        Intent returnIntent = new Intent();
                                        returnIntent.putExtra("removeIndex", position);
                                        setResult(Activity.RESULT_OK, returnIntent);
                                        finish();
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        mButton.setVisibility(View.VISIBLE);
                                        Log.d("Yasir","Error "+error);
                                    }
                                });
                        Log.d("Yasir","added request "+stringRequest);

                        Volley.newRequestQueue(getApplicationContext()).add(stringRequest);

                        dialog.dismiss();
                    }
                });

                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        Fragment fragment = NewsFeedFragment.newInstance(Constants.REQUEST_TYPE_FRIEND,Constants.REQUEST_CONTENT_ALL, userID);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    //handle the back arrow press in the toolbar
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onFragmentInteraction(Integer action) {
        //do nothing
    }
}
