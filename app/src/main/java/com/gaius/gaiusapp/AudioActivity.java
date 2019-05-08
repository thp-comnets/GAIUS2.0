package com.gaius.gaiusapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.gaius.gaiusapp.utils.DBHelper;


//import android.support.v7.app.ActionBarActivity;


public class AudioActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private PagerSlidingTabStrip tabs;
    private ViewPager pager;
    private static final int ADD_AUDIO = 1001;
    private DBHelper mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);
        mDatabase = new DBHelper(getApplicationContext());
        pager = (ViewPager) findViewById(R.id.pager);

        String intentFragment = getIntent().getExtras().getString("frgToLoad");
   /*     Log.v("intentFragment"," - "+ intentFragment);
        if(intentFragment.equals("Record")){
            pager.setAdapter(new MyAdapter(getSupportFragmentManager()));
        }else{
            pager.setAdapter(new MyAdapter(getSupportFragmentManager()));
            switch (intentFragment){

                case "Record":
                    Log.v("intentFragment1"," - "+ intentFragment);
                    AudioTrimmerFragment.newInstance(1);
                    break;
                case "Saved":
                    Log.v("intentFragment2"," - "+ intentFragment);
                    FileViewerFragment.newInstance(2);
                    break;
            }
        }*/

        pager.setAdapter(new MyAdapter(getSupportFragmentManager()));
        tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabs.setViewPager(pager);
        if(intentFragment.equals("Saved")){
            pager.setCurrentItem(2);
        }


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        toolbar.setPopupTheme(R.style.ThemeOverlay_AppCompat_Light);
        if (toolbar != null) {
//            setSupportActionBar(toolbar);
        }
    }

   /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_settings:
//                Intent i = new Intent(this, SettingsActivity.class);
//                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }*/

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
}

