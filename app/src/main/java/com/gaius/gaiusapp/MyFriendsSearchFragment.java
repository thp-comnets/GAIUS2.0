package com.gaius.gaiusapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.androidnetworking.common.ANRequest;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.AnalyticsListener;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.gaius.gaiusapp.adapters.FriendsAdapter;
import com.gaius.gaiusapp.classes.Friend;
import com.gaius.gaiusapp.interfaces.FragmentVisibleInterface;
import com.gaius.gaiusapp.interfaces.OnAdapterInteractionListener;
import com.gaius.gaiusapp.interfaces.OnFragmentInteractionListener;
import com.gaius.gaiusapp.utils.Constants;
import com.gaius.gaiusapp.utils.LogOut;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//TODO: implement onclicklistener. this needs a change in the two adapaters we are using in case we want the user to be clickable

public class MyFriendsSearchFragment extends Fragment implements FragmentVisibleInterface, OnAdapterInteractionListener {
    SharedPreferences prefs;
    public static File path;
    public static Context context;
    List<Friend> friendList, friendList2;
    RecyclerView recyclerView, recyclerView2;
    RelativeLayout noFriendsSearch;
    TextView importFriendsTitle;
    private OnFragmentInteractionListener mListener;
    private OnAdapterInteractionListener mAdapterListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mAdapterListener = this;
        return inflater.inflate(R.layout.search_friends_fragment, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        context = getActivity().getApplicationContext();
        noFriendsSearch = getView().findViewById(R.id.noFriends);
        importFriendsTitle = getView().findViewById(R.id.contacts_title);

        recyclerView = getView().findViewById(R.id.recylcerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        recyclerView2 = getView().findViewById(R.id.recylcerView_search);
        recyclerView2.setHasFixedSize(true);
        recyclerView2.setLayoutManager(new LinearLayoutManager(getContext()));

        friendList = new ArrayList<>();

        loadPossibleFriends();

        final EditText searchName  = getView().findViewById(R.id.search_name_edittext);
        ImageView searchButton = getView().findViewById(R.id.search_button);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchFriend(searchName.getText().toString());
            }
        });

        searchName.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    searchFriend(searchName.getText().toString());
                    return true;
                }
                return false;
            }
        });

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void loadPossibleFriends() {
        path = context.getExternalFilesDir(null);
        File file = new File(path, "contacts.txt");
        ArrayList<String> phoneNumbers = new ArrayList<String>();

        try {
            FileOutputStream stream = new FileOutputStream(file);

            Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

            while (phones.moveToNext()) {
//                String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                phoneNumber = phoneNumber.replace("+","00").replace("(","").replace(")","").replace("-","").replace(" ","").replace("/","");
// fixme: need to figure out a way to give international code for numbers without a +(code)
//                if (!phoneNumber.substring(0, 2).contains("00") && phoneNumber.substring(0, 1).contains("0")) {
//                    phoneNumber = "00971"+phoneNumber.substring(1,phoneNumber.length());
//                }

                if (! phoneNumbers.contains(phoneNumber)) {
                    phoneNumbers.add(phoneNumber);
                    stream.write((phoneNumber+"\n").getBytes());
                }

            }
            phones.close();
            stream.close();

            uploadMultipart(context, path.toString()+"/contacts.txt",null,null);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void uploadMultipart(final Context context, final String contactPath, final String button, final String phoneNumber) {

        ANRequest.MultiPartBuilder multiPartBuilder = new ANRequest.MultiPartBuilder(prefs.getString("base_url", null) + "findFriends2.py");

        multiPartBuilder.addMultipartFile("contacts", new File (contactPath));
        multiPartBuilder.addMultipartParameter("token", prefs.getString("token", "null"));
        multiPartBuilder.build()
                .setAnalyticsListener(new AnalyticsListener() {
                    @Override
                    public void onReceived(long timeTakenInMillis, long bytesSent,
                                           long bytesReceived, boolean isFromCache) {
                        Log.d("thp", " timeTakenInMillis : " + timeTakenInMillis);
                        Log.d("thp", " bytesSent : " + bytesSent);
                        Log.d("thp", " bytesReceived : " + bytesReceived);
                        Log.d("thp", " isFromCache : " + isFromCache);
                    }
                })
                .getAsJSONArray(new JSONArrayRequestListener() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            //converting the string to json array object
                            JSONObject friend;

                            if (response.length() == 0 ) {
                                importFriendsTitle.setVisibility(View.GONE);
                            }
                            else {
                                importFriendsTitle.setVisibility(View.VISIBLE);
                            }

                            //traversing through all the object
                            for (int i = 0; i < response.length(); i++) {

                                //getting product object from json array
                                friend = response.getJSONObject(i);

                                //adding the product to product list
                                friendList.add(new Friend(
                                        friend.getInt("id"),
                                        friend.getString("name"),
                                        "current status",
                                        friend.getString("avatar"),
                                        friend.getString("userID"),
                                        friend.getString("type"),
                                        false
                                ));
                            }

                            //creating adapter object and setting it to recyclerview
                            FriendsAdapter adapter = new FriendsAdapter(getContext(), friendList, mAdapterListener);
                            recyclerView.setAdapter(adapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d("Yasir", "Json error " + e);
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        switch (error.getErrorCode()) {
                            case 401:
                                LogOut.logout(getActivity());
                                Toast.makeText(getContext(), "You have logged in from another device. Please login again.",
                                        Toast.LENGTH_LONG).show();
                                Intent i = new Intent(getContext(), LoginActivity.class);
                                startActivity(i);
                                getActivity().finish();
                                break;
                            case 500:
                                Log.d("Yasir","Error 500"+error);
                                break;
                            default:
                                Log.d("Yasir","Error no Internet "+error);
                    }
                }});
    }

    private void searchFriend(String name) {
        /*
         * Creating a String Request
         * The request type is GET defined by first parameter
         * The URL is defined in the second parameter
         * Then we have a Response Listener and a Error Listener
         * In response listener we will get the JSON response as a String
         */
        String token = prefs.getString("token", "null");
        String URL = prefs.getString("base_url", null) + "searchFriend.py?token=" + token + "&name="+name;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            friendList2 = new ArrayList<>();

                            //converting the string to json array object
                            JSONArray array = new JSONArray(response);

                            if (array.length() == 0 ) {
                                noFriendsSearch.setVisibility(View.VISIBLE);
                            }
                            else {
                                noFriendsSearch.setVisibility(View.GONE);
                            }

                            //traversing through all the object
                            for (int i = 0; i < array.length(); i++) {

                                //getting product object from json array
                                JSONObject friend = array.getJSONObject(i);

                                //adding the product to product list
                                friendList2.add(new Friend(
                                        friend.getInt("id"),
                                        friend.getString("name"),
                                        "current status",
                                        friend.getString("avatar"),
                                        friend.getString("userID"),
                                        friend.getString("type"),
                                        false
                                ));
                            }

                            //creating adapter object and setting it to recyclerview
                            FriendsAdapter adapter = new FriendsAdapter(getContext(), friendList2, mAdapterListener);
                            recyclerView2.setAdapter(adapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d("Yasir","Json error "+e);

                            if (response.contains("invalid token")) {
                                LogOut.logout(getContext());
                                Toast.makeText(getContext(), "You have logged in from another device. Please login again.",
                                        Toast.LENGTH_LONG).show();
                                getActivity().finish();
                            }
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
        Volley.newRequestQueue(getContext()).add(stringRequest);
    }

    @Override
    public void fragmentBecameVisible() {
        //do nothing
    }

    @Override
    public void onAdapterInteraction(Integer action) {
        //signal the badge change up to the MainActivity
        mListener.onFragmentInteraction(Constants.UPDATE_BADGE_NOTIFICATION_FRIENDS);
    }
}
