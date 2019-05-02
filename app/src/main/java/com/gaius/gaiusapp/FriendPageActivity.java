package com.gaius.gaiusapp;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
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
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.gaius.gaiusapp.interfaces.OnFragmentInteractionListener;
import com.gaius.gaiusapp.networking.GlideApp;
import com.gaius.gaiusapp.networking.GlideImageLoadingService;
import com.gaius.gaiusapp.utils.Constants;
import com.gaius.gaiusapp.utils.LogOut;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ss.com.bannerslider.Slider;

public class FriendPageActivity extends AppCompatActivity implements OnFragmentInteractionListener {
    SharedPreferences prefs;
    RelativeLayout noPages;
    AppCompatButton actionButton;
    ProgressBar progressBar;
    String base_url, userID, avatar="None";
    Integer position, friendStatus;
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

        String name="", status = "";
        Integer friendStatus = -1;
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
            friendStatus = bundle.getInt("friendstatus", -1);

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

        imageViewAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAvatarPopup((ImageView) v);
            }
        });

        progressBar = findViewById(R.id.action_progress_bar);

        actionButton = findViewById(R.id.action_button);

        // don't show the button if its my own profile
        if (userID.equals(prefs.getString("userID", null))) {
            actionButton.setVisibility(View.GONE);
        } else {
            setActionButton(friendStatus);
        }

        Fragment fragment = NewsFeedFragment.newInstance(Constants.REQUEST_TYPE_FRIEND, Constants.REQUEST_CONTENT_ALL, userID);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void setActionButton(int friendStatus) {
        this.friendStatus = friendStatus;

        progressBar.setVisibility(View.GONE);
        actionButton.setVisibility(View.VISIBLE);
        switch (friendStatus) {
            case Constants.FRIEND_STATUS_NOT_CONNECTED:
                actionButton.setBackground(this.getResources().getDrawable(R.drawable.friend_connect_button));
                actionButton.setText(this.getResources().getString(R.string.connect));
                actionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        modifyFriend(Constants.FRIEND_STATUS_NOT_CONNECTED);
                    }
                });
                break;
            case Constants.FRIEND_STATUS_PENDING:
                actionButton.setBackground(this.getResources().getDrawable(R.drawable.friend_withdraw_button));
                actionButton.setText(this.getResources().getString(R.string.withdraw));
                actionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        modifyFriend(Constants.FRIEND_STATUS_PENDING);
                    }
                });
                break;
            case Constants.FRIEND_STATUS_CONNECTED:
                actionButton.setBackground(this.getResources().getDrawable(R.drawable.friend_unfriend_button));
                actionButton.setText(this.getResources().getString(R.string.unfriend));
                actionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showUnfriendDialog(v);
                    }
                });
                break;
            case Constants.FRIEND_STATUS_ACCEPT:
                actionButton.setBackground(this.getResources().getDrawable(R.drawable.friend_accept_button));
                actionButton.setText(this.getResources().getString(R.string.accept));
                actionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        modifyFriend(Constants.FRIEND_STATUS_ACCEPT);
                    }
                });
                break;
            case Constants.FRIEND_STATUS_SUBSCRIBE:
                actionButton.setBackground(this.getResources().getDrawable(R.drawable.friend_subscribe_button));
                actionButton.setText(this.getResources().getString(R.string.subscribe));
                actionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        modifyFriend(Constants.FRIEND_STATUS_SUBSCRIBE);
                    }
                });
                break;
            case Constants.FRIEND_STATUS_UNSUBSCRIBE:
                actionButton.setBackground(this.getResources().getDrawable(R.drawable.friend_unsubscribe_button));
                actionButton.setText(this.getResources().getString(R.string.unsubscribe));
                actionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        modifyFriend(Constants.FRIEND_STATUS_UNSUBSCRIBE);
                    }
                });
                break;
            default:
                //we should not come here
                actionButton.setVisibility(View.GONE);
                break;
        }
    }

    private void modifyFriend(final int friendstatus) {

        progressBar.setVisibility(View.VISIBLE);
        actionButton.setVisibility(View.GONE);

//        String URL = prefs.getString("base_url", null) + "modifyFriend.py?token=" + prefs.getString("token", null) + "&" + Constants.FRIEND_ACTION_LIST.get(friendstatus) + "=" + userID;
//        Log.d("thp", "modifyFreinds.py " + URL);
        AndroidNetworking.get(prefs.getString("base_url", null) + "modifyFriend.py")
                .addQueryParameter(Constants.FRIEND_ACTION_LIST.get(friendstatus), userID)
                .addQueryParameter("token", prefs.getString("token", "null"))
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONArray(new JSONArrayRequestListener() {
                    @Override
                    public void onResponse(JSONArray response) {

                        Log.d("thp", "Response as JSON " + response);

                        try {
                            JSONObject status = response.getJSONObject(0);
                            Integer action = Integer.parseInt(status.getString("status"));
                            setActionButton(action);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d("thp","Json error "+e);
                        }

                    }
                    @Override
                    public void onError(ANError error) {
                        progressBar.setVisibility(View.GONE);
                        actionButton.setVisibility(View.VISIBLE);

                        switch (error.getErrorCode()) {
                            case 401:
                                LogOut.logout(getApplicationContext());
                                Toast.makeText(getApplicationContext(), "You have logged in from another device. Please login again.",
                                        Toast.LENGTH_LONG).show();
                                Intent i = new Intent(getApplicationContext(), LoginSMSActivity.class);
                                startActivity(i);
                                finish();
                                break;
                            case 500:
                                Log.d("thp","Error 500"+error);
                                break;
                            default:
                                Log.d("thp","Error no Internet "+error);

                        }
                    }
                });
    }

    private void showUnfriendDialog(final View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mCtx);

        builder.setTitle("Remove friend?");
        builder.setMessage("Do you want to remove this friend?");

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                modifyFriend(Constants.FRIEND_STATUS_CONNECTED);
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

    private void showAvatarPopup(ImageView avatarView) {

        float scale = mCtx.getResources().getDisplayMetrics().density;

        Dialog builder = new Dialog(this);
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));

        ImageView imageView = new ImageView(this);
        imageView.setImageDrawable(avatarView.getDrawable());

        RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(
                (int) (300 * scale),
                (int) (300 * scale));
        builder.addContentView(imageView, relativeParams);
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);

        WindowManager.LayoutParams wmlp = builder.getWindow().getAttributes();
        wmlp.gravity = Gravity.TOP;
        wmlp.y = (int) (150 * scale);

        builder.show();
    }

    //handle the back arrow press in the toolbar
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("position", position);
        resultIntent.putExtra("friendstatus", friendStatus);
        if (friendStatus == Constants.FRIEND_STATUS_CONNECTED) {
            //nothing has changed
            setResult(RESULT_CANCELED, resultIntent);
        } else {
            setResult(RESULT_OK, resultIntent);
        }

        finish();
    }

    @Override
    public void onFragmentInteraction(Integer action) {
        //do nothing
    }
}
