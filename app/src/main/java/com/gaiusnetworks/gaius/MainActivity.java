package com.gaiusnetworks.gaius;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import static com.gaiusnetworks.gaius.utils.Constants.MULTIPLE_PERMISSIONS;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    String[] permissions= new String[]{
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.VIBRATE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // asking the user for all required permissions
        checkPermissions ();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //loading the default fragment
        loadFragment(new NewsFeedFragment());

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(this);
//        navigation.setItemIconTintList(null);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
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
                return;
            }
        }
    }

    @Override
    public void onBackPressed()
    {
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

            case R.id.navigation_content:
                Log.d("tags", "tag is " + recycler.getTag()+"");

                if ((recycler.getTag()+"").contains("SubWeb")) {
                    setTitle("Content Browser");
                    loadFragment(new WebFragment());
                    break;
                }
                else if ((recycler.getTag()+"").contains("MainWeb") || (recycler.getTag()+"").contains("MainVideo")) {
                    navigation.setSelectedItemId(R.id.navigation_content);
                    break;
                }
                // else would be to go back to default because it means we are at the main webFragment

            default:
                navigation.setSelectedItemId(R.id.navigation_home);
                break;
        }
    }
}
