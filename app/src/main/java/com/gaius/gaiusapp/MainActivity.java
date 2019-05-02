package com.gaius.gaiusapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
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
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.gaius.gaiusapp.interfaces.OnFragmentInteractionListener;
import com.gaius.gaiusapp.networking.GlideApp;
import com.gaius.gaiusapp.utils.Constants;
import com.gaius.gaiusapp.utils.LogOut;

import java.util.ArrayList;
import java.util.List;

import cn.jzvd.Jzvd;
import me.leolin.shortcutbadger.ShortcutBadger;
import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

import static com.gaius.gaiusapp.utils.Constants.MULTIPLE_PERMISSIONS;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, OnFragmentInteractionListener {

    String[] permissions= new String[]{
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
//            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.CAMERA,
//            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.VIBRATE};

    TextView badgeTextView;
    Context mCtx;
    private SharedPreferences prefs;
    Bundle contentBundle;
    private ActionBar actionBar;
    private String userID=null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidNetworking.initialize(getApplicationContext());
        AndroidNetworking.enableLogging();
        // creating the BASE_URL of the GAIUS edge
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (prefs.getString("token", null) == null) {
            Intent i = new Intent(this, LoginSMSActivity.class);
            startActivity(i);
            finish();
            return;
        }

        // asking the user for all required permissions
        checkPermissions ();

//        Slider.init(new GlideImageLoadingService(this));

        setContentView(R.layout.activity_main);

        //loading the default fragment
//        loadFragment(NewsFeedFragment.newInstance(0, 0));

        BottomNavigationView mBottomNavigationView = findViewById(R.id.navigation);

        if (prefs.getString("admin", "0").equals("1")) {
            mBottomNavigationView.inflateMenu(R.menu.bottom_navigation_admin);
        } else {
            mBottomNavigationView.inflateMenu(R.menu.bottom_navigation);
        }

        mBottomNavigationView.setOnNavigationItemSelectedListener(this);
        mBottomNavigationView.setItemIconTintList(null); //disable icon tinting - otherwise it will show squares not the icons

        BottomNavigationMenuView mBottomNavigationMenuView =
                (BottomNavigationMenuView) mBottomNavigationView.getChildAt(0);

        BottomNavigationItemView itemView = (BottomNavigationItemView) mBottomNavigationMenuView.getChildAt(2);

        View friendBadgeView = LayoutInflater.from(this)
                .inflate(R.layout.badge_layout, mBottomNavigationMenuView, false);

        itemView.addView(friendBadgeView);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();

        badgeTextView = friendBadgeView.findViewById(R.id.badge);
        mCtx = this;
        contentBundle = new Bundle();

        if (getIntent().getBooleanExtra("navigation_friends", false)) {
            mBottomNavigationView.setSelectedItemId(R.id.navigation_friends);
        } else if (getIntent().getBooleanExtra("navigation_content", false)) {
            mBottomNavigationView.setSelectedItemId(R.id.navigation_content);
            contentBundle.putBoolean("approval", true);
        } else {
            mBottomNavigationView.setSelectedItemId(R.id.navigation_home);
        }

        // yasir: handlign sharing of a channel
        Uri data = getIntent().getData();
        if (data != null && data.toString().contains("http://gaiusnetworks.com/channel/")) {
            actionBar.setTitle("Viewing shared channel");

            Intent intent = this.getIntent();
            Bundle bundle = intent.getExtras();
            userID =  data.toString().replace("http://gaiusnetworks.com/channel/","");

            Log.d("yasir","channel user id "+userID);

            Fragment fragment = new BrowseWebFragment();
            bundle.putString("userID", userID);
            fragment.setArguments(bundle);

            ((AppCompatActivity) this).getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    private void setBadge(int num) {

        if (badgeTextView == null) {
            Log.e("Gaius", "badgeView is null");
            return;
        }

        if (mCtx == null) {
            Log.e("Gaius", "context is null");
            return;
        }

        if (num <= 0) {
            badgeTextView.setVisibility(View.GONE);
            ShortcutBadger.removeCount(mCtx);

        } else {
            badgeTextView.setVisibility(View.VISIBLE);
            badgeTextView.setText(""+num);
            ShortcutBadger.applyCount(mCtx, num);
        }

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Jzvd.releaseAllVideos();

        Fragment fragment = null;

        switch (item.getItemId()) {
            case R.id.navigation_home:
                fragment = NewsFeedFragment.newInstance(Constants.REQUEST_TYPE_NEWSFEED, Constants.REQUEST_CONTENT_ALL);
                disableTitleDropdownMenu();
                getSupportActionBar().setTitle("Home");
                break;
            case R.id.navigation_content:
                // creating the fragment is handled in the enableTitleDrowdownMenu() when the spinner is initialized. true has to be returned, otherwise bottomnavigation is not properly selected
                enableTitleDropdownMenu();
                return true;
            case R.id.navigation_friends:
                fragment = new FriendsFragment();
                disableTitleDropdownMenu();
                getSupportActionBar().setTitle("Friends");
                break;
             case R.id.navigation_add_content:
                startActivity(new Intent(this, CreateContentActivity.class));
                break;
            case R.id.navigation_approve_content:
                startActivity(new Intent(this, ApproveContentActivity.class));
                break;
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

    private void enableTitleDropdownMenu() {
        // Hide the action bar title
        actionBar.setDisplayShowTitleEnabled(false);

        // Enabling Spinner dropdown bottom_navigation
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        ArrayList<String> mNavigationItems=new ArrayList<String>();
        mNavigationItems.add("Browse Content");
        mNavigationItems.add("My Content");
        ArrayAdapter<CharSequence> mArrayAdapter;
        mArrayAdapter = new ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, mNavigationItems);
        mArrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

        actionBar.setListNavigationCallbacks(mArrayAdapter, new ActionBar.OnNavigationListener() {
            @Override
            public boolean onNavigationItemSelected(int i, long l) {
                switch (i) {
                    case 0:
                        loadFragment(new ViewContentFragment());
                        break;
                    case 1:
                        loadFragment(new ViewMyContentFragment());
                        break;
                }
                return false;
            }
        });

    }

    private void disableTitleDropdownMenu() {
        // Show the action bar title
        actionBar.setDisplayShowTitleEnabled(true);

        // Enabling Spinner dropdown bottom_navigation
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
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
        if (prefs.getString("admin", "0").equals("1")) {
            getMenuInflater().inflate(R.menu.preferences_menu_admin, menu);
        } else {
            getMenuInflater().inflate(R.menu.preferences_menu, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.logoutButton:
                LogOut.logout(getApplicationContext());
                Intent i = new Intent(this, LoginSMSActivity.class);
                startActivity(i);
                finish();
                break;
            case  R.id.settingsButton:
                startActivity(new Intent(MainActivity.this, PreferencesActivity.class));
                break;
            case R.id.clearCacheButton:
                clearGlideCache(this);
                break;
            case R.id.aboutButton:
                createAbout();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createAbout() {
        String version = "";
        int versionCode = -1;
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
            versionCode = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        Element serverInfoElement = new Element();
        if (prefs.getString("admin", "0").equals("1")) {
            Uri uri = Uri.parse(prefs.getString("base_url", "NA"));
            serverInfoElement.setTitle(uri.getHost() + " (" +prefs.getString("server_name", "NA") +  ")");
        } else {
            serverInfoElement.setTitle(prefs.getString("server_name", "NA"));
        }

        View aboutPage = new AboutPage(this)
                .setDescription(this.getResources().getString(R.string.about_description))
                .isRTL(false)
                .setImage(R.drawable.gaius_logo)
                .addItem(new Element().setTitle("Version " + version + " (" +versionCode+ ")"))
                .addGroup("Connect with us")
                .addEmail("info@gaiusnetworks.com")
                .addWebsite("http://gaiusnetworks.com")
                .addTwitter("GAIUSNetworks")
                .addPlayStore("https://play.google.com/store/apps/details?id=com.gaius.gaiusapp")
                .addGroup("Advertise with us")
                .addEmail("advertise@gaiusnetworks.com")
                .addGroup("Server info")
                .addItem(serverInfoElement)
                .create();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.gaius_logo);
        builder.setTitle("Gaius Networks");
        builder.setView(aboutPage);
        builder.create();
        builder.show();
    }

    @Override
    public void onBackPressed()
    {
        Jzvd.releaseAllVideos();

        if (userID!=null) {
            finish();
            return;
        }

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
                moveTaskToBack(true);
                break;

            case R.id.navigation_friends:
                if ((recycler.getTag()+"").contains("SubFriends")) {
                    setTitle("Friends");
                    loadFragment(new FriendsFragment());
                    break;
                }

            case R.id.navigation_content:

                if ((recycler.getTag()+"").contains("channelView")) {
                    navigation.setSelectedItemId(R.id.navigation_content);
//                    setTitle("Content Browser");
//                    loadFragment(new BrowseWebFragment());
                    break;
                } else if ((recycler.getTag()+"").contains("MainWeb") || (recycler.getTag()+"").contains("MainVideo")
                        || (recycler.getTag()+"").contains("MyContent") || (recycler.getTag()+"").contains("ContentApproval")) {
                    // FIXME we probably never come here anymore
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
        Jzvd.releaseAllVideos(); // thp: I think this should go to onStop()
    }

    static void clearGlideCache(final Context ctx)
    {

        // request a new firebase token
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    FirebaseInstanceId.getInstance().deleteInstanceId();
//                    FirebaseInstanceId.getInstance().getToken();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();

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

    @Override
    public void onFragmentInteraction(Integer action) {

        // update the notification badge in the FriendsFragment
        // check for FriendsFragment instance in case the user switched to another fragment before the FriendsFragment was fully loaded and not detached/destroyed
        if (action == Constants.UPDATE_BADGE_NOTIFICATION_FRIENDS && getSupportFragmentManager().findFragmentById(R.id.fragment_container) instanceof FriendsFragment) {
            FriendsFragment fragment = (FriendsFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            fragment.updateNotificationBadge();
        }
        //TODO add badge for approve pages
//        if (action == Constants.UPDATE_BADGE_NOTIFICATION_FRIENDS) {
//            FriendsFragment fragment = (FriendsFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
//            fragment.updateNotificationBadge();
//        }

        setBadge(prefs.getInt("pending-requests", 0));

    }
}