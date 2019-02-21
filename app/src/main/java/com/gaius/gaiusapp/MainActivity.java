package com.gaius.gaiusapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.gaius.gaiusapp.networking.GlideApp;
import com.gaius.gaiusapp.networking.GlideImageLoadingService;
import com.gaius.gaiusapp.utils.LogOut;

import java.util.ArrayList;
import java.util.List;

import cn.jzvd.Jzvd;
import me.leolin.shortcutbadger.ShortcutBadger;
import ss.com.bannerslider.Slider;

import static com.gaius.gaiusapp.utils.Constants.MULTIPLE_PERMISSIONS;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    String[] permissions= new String[]{
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
//            Manifest.permission.ACCESS_COARSE_LOCATION,
//            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.CAMERA,
//            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.VIBRATE};

    static TextView badgeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidNetworking.initialize(getApplicationContext());
        AndroidNetworking.enableLogging();
        // creating the BASE_URL of the GAIUS edge
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        String hostIP = "91.230.41.34";
        String hostPort = "8080";
        String hostPath = "test";
        editor.putString("base_url", "http://" + hostIP + ":" + hostPort + "/" + hostPath + "/");
        editor.commit();

        if (prefs.getString("token", null) == null) {
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
            finish();
            return;
        }

        // asking the user for all required permissions
        checkPermissions ();

        Slider.init(new GlideImageLoadingService(this));

        setContentView(R.layout.activity_main);

        //loading the default fragment
        loadFragment(new NewsFeedFragment());

        BottomNavigationView mBottomNavigationView = findViewById(R.id.navigation);
        mBottomNavigationView.setOnNavigationItemSelectedListener(this);

        BottomNavigationMenuView mBottomNavigationMenuView =
                (BottomNavigationMenuView) mBottomNavigationView.getChildAt(0);

        BottomNavigationItemView itemView = (BottomNavigationItemView) mBottomNavigationMenuView.getChildAt(1);

        View friendBadgeView = LayoutInflater.from(this)
                .inflate(R.layout.badge_layout, mBottomNavigationMenuView, false);

        itemView.addView(friendBadgeView);

        badgeTextView = friendBadgeView.findViewById(R.id.badge);
    }

    public static void setBadge(Context ctx, int num) {

        if (badgeTextView == null) {
            Log.e("Gaius", "badgeView is null");
        }
        if (num <= 0) {
            badgeTextView.setVisibility(View.GONE);
            ShortcutBadger.removeCount(ctx);
        } else {
            badgeTextView.setVisibility(View.VISIBLE);
            badgeTextView.setText(""+num);
            ShortcutBadger.applyCount(ctx, num);
        }

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Jzvd.releaseAllVideos();

        Fragment fragment = null;

        switch (item.getItemId()) {
            case R.id.navigation_home:
                fragment = new NewsFeedFragment();
                setTitle("Home");
                break;

            case R.id.navigation_friends:
                fragment = new FriendsFragment();
                setTitle("Friends");
                break;

            case R.id.navigation_content:
                fragment = new ContentFragment();
                setTitle("Content Browser");
                break;
//
//            case R.id.navigation_videos:
//                fragment = new ContentFragment();
//                setTitle("Videos");
//                break;
        }

        return loadFragment(fragment);
    }

    private boolean loadFragment(Fragment fragment) {
        //switching fragment
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();

            return true;
        }
        return false;
    }

    private  boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p:permissions) {
            result = ContextCompat.checkSelfPermission(this,p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),MULTIPLE_PERMISSIONS );
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissionsList[], int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS:{
                if (grantResults.length > 0) {
                    String permissionsDenied = "";
                    for (String per : permissionsList) {
                        if(grantResults[0] == PackageManager.PERMISSION_DENIED){
                            permissionsDenied += "\n" + per;
                        }
                    }
                    // Show permissionsDenied
//                    updateViews();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.preferences_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.logoutButton) {
            // do something here
            LogOut.logout(getApplicationContext());
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
            finish();
        } else if (id == R.id.settingsButton) {
            startActivity(new Intent(MainActivity.this, PreferencesActivity.class));
        } else if (id == R.id.clearCacheButton) {
            clearGlideCache(this);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed()
    {
        Jzvd.releaseAllVideos();

        BottomNavigationView navigation = findViewById(R.id.navigation);
        RecyclerView recycler = findViewById(R.id.recylcerView);

        View visibleChild = recycler.getChildAt(0);
        int positionOfChild = recycler.getChildAdapterPosition(visibleChild);

        if (positionOfChild > 0) {
            recycler.smoothScrollToPosition(0);
            return;
        }

        switch (navigation.getSelectedItemId()) {
            case R.id.navigation_home:
                moveTaskToBack(false);
                break;

            case R.id.navigation_friends:
                if ((recycler.getTag()+"").contains("SubFriends")) {
                    setTitle("Friends");
                    loadFragment(new FriendsFragment());
                    break;
                }

            case R.id.navigation_content:
                Log.d("tags", "tag is " + recycler.getTag()+"");

                if ((recycler.getTag()+"").contains("SubWeb")) {
                    setTitle("Content Browser");
                    loadFragment(new BrowseWebFragment());
                    break;
                }
                else if ((recycler.getTag()+"").contains("MainWeb") || (recycler.getTag()+"").contains("MainVideo")
                        || (recycler.getTag()+"").contains("MyContent") || (recycler.getTag()+"").contains("ContentApproval")) {
                    navigation.setSelectedItemId(R.id.navigation_content);
                    break;
                }
                // else would be to go back to default because it means we are at the main webFragment

            default:
                navigation.setSelectedItemId(R.id.navigation_home);
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Jzvd.releaseAllVideos();
    }

    static void clearGlideCache(final Context ctx)
    {
        new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... unused) {
                // clearDiskCache() must be called from background thread
                GlideApp.get(ctx).clearDiskCache();
                return null;
            }
            protected void onPostExecute(Void unused) {
                // clearMemory() must be called from main thread
                GlideApp.get(ctx).clearMemory();
            }
        }.execute();
    }
}