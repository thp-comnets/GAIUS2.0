package com.gaius.gaiusapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.gaius.gaiusapp.utils.Constants;
import com.nostra13.universalimageloader.core.ImageLoader;

public class PreferencesActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    public static class MyPreferenceFragment extends PreferenceFragment {
        //        private SeekBarPreference _seekBarPref;
        Preference logout;

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            InputFilter[] filters = new InputFilter[1];
            filters[0] = new InputFilter() {
                @Override
                public CharSequence filter(CharSequence source, int start, int end,
                                           android.text.Spanned dest, int dstart, int dend) {
                    if (end > start) {
                        String destTxt = dest.toString();
                        String resultingTxt = destTxt.substring(0, dstart)
                                + source.subSequence(start, end)
                                + destTxt.substring(dend);
                        if (!resultingTxt.matches("^\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?")) {
//                            Log.d("dd", "no macth");
                            return "";
                        } else {
                            String[] splits = resultingTxt.split("\\.");
                            for (int i = 0; i < splits.length; i++) {
                                if (Integer.valueOf(splits[i]) > 255) {
                                    return "";
                                }
                            }
                        }
                    }
                    return null;
                }
            };

//            Preference account = getPreferenceScreen().findPreference("account");
//            account.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//                @Override
//                public boolean onPreferenceClick(Preference preference) {
//
//                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
//                    Intent intent;
//                    if (prefs.getString("account_token", "null").equals("null")) {
//                        //TODO launch login
//                        intent = new Intent(getActivity(), LoginActivity.class);
//                        intent.putExtra("REF", Constants.PREFERENCES_ACTIVITY);
////                        return false;
//                    } else {
//                        intent = new Intent(getActivity(), RegisterActivity.class);
//                        intent.putExtra("REF", Constants.PREFERENCES_ACTIVITY);
//                        intent.putExtra("EDIT", "");
//                    }
//                    startActivity(intent);
//
//
//                    return false;
//                }
//            });
        }

        @Override
        public void onStart() {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            super.onStart();
        }
    }

}