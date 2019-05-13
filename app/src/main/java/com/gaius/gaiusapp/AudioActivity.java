package com.gaius.gaiusapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.gaius.gaiusapp.interfaces.IOnBackPressed;
import com.gaius.gaiusapp.utils.DBHelper;

import java.util.List;

import static java.security.AccessController.getContext;


//import android.support.v7.app.ActionBarActivity;


public class AudioActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private PagerSlidingTabStrip tabs;
    private CustomViewPager pager;
    private static final int ADD_AUDIO = 1001;
    private DBHelper mDatabase;
    AlertDialog alertBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);
        mDatabase = new DBHelper(getApplicationContext());
        pager =  findViewById(R.id.pager);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        String intentFragment = getIntent().getExtras().getString("frgToLoad");


        pager.setAdapter(new MyAdapter(getSupportFragmentManager()));
        tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabs.setViewPager(pager);
        if(intentFragment.equals("Saved")){
            pager.setCurrentItem(2);
        }
         pager.setPagingEnabled(false);


    }




    public class MyAdapter extends FragmentPagerAdapter {
        private String[] titles = { getString(R.string.tab_title_record),
                getString(R.string.tab_title_saved_recordings) };

        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch(position){
                case 0:{
                    Log.v("intentFragment3"," - "+ position);
                    return AudioTrimmerFragment.newInstance(position);
                }
                case 1:{
                    Log.v("intentFragment4"," - "+ position);
                    return FileViewerFragment.newInstance(position);
                }
            }
            return null;
        }

        @Override
        public int getCount() {
            return titles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }

    public AudioActivity() {
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v("onActivityResult"," - "+data + "--"+resultCode);
        if (requestCode == ADD_AUDIO) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    //audio trim result will be saved at below path
                    String path = data.getExtras().getString("INTENT_AUDIO_FILE");
                    Log.v("onActivityResult1"," - " + "--"+path);
                    Toast.makeText(AudioActivity.this, "Audio stored at " + path, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    //handle the back arrow press in the toolbar
    @Override
    public boolean onSupportNavigateUp() {
        List fragmentList = getSupportFragmentManager().getFragments();

        boolean handled = false;
        for(Object f : fragmentList) {
            if(f instanceof AudioTrimmerFragment) {
                handled = ((AudioTrimmerFragment)f).onAudioBackPressed();

                if(handled) {
                    openBackAlert();
                    break;
                }
            }
        }

        if(!handled) {
            super.onBackPressed();
        }
        return true;
    }


    @Override
    public void onBackPressed() {

        List fragmentList = getSupportFragmentManager().getFragments();

        boolean handled = false;
        for(Object f : fragmentList) {
            if(f instanceof AudioTrimmerFragment) {
                handled = ((AudioTrimmerFragment)f).onAudioBackPressed();

                if(handled) {
                    openBackAlert();
                    break;
                }
            }
        }

        if(!handled) {
            super.onBackPressed();
        }
    }

    public void openBackAlert(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Are you sure, you want to exit?");
                alertDialogBuilder.setPositiveButton("yes",
                        new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface arg0, int arg1) {
            finish();
        }
    });

        alertDialogBuilder.setNegativeButton("No",new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {

        }
    });

    AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
}
}

