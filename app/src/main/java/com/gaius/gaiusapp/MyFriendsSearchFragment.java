package com.gaius.gaiusapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.gaius.gaiusapp.adapters.FriendsAdapter;
import com.gaius.gaiusapp.classes.Friend;
import com.gaius.gaiusapp.interfaces.FragmentVisibleInterface;
import com.gaius.gaiusapp.utils.LogOut;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadService;
import net.gotev.uploadservice.UploadStatusDelegate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.gotev.uploadservice.Placeholders.ELAPSED_TIME;
import static net.gotev.uploadservice.Placeholders.UPLOAD_RATE;

import static net.gotev.uploadservice.Placeholders.PROGRESS;
import static net.gotev.uploadservice.Placeholders.TOTAL_FILES;
import static net.gotev.uploadservice.Placeholders.UPLOADED_FILES;

public class MyFriendsSearchFragment extends Fragment implements FragmentVisibleInterface {
    SharedPreferences prefs;
    public static File path;
    private static ProgressDialog progressDialog;
    public static Context context;
    List<Friend> friendList, friendList2;
    RecyclerView recyclerView, recyclerView2;
    RelativeLayout noFriendsSearch;
    TextView importFriendsTitle;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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

        UploadService.NAMESPACE = BuildConfig.APPLICATION_ID;
        UploadService.NAMESPACE = "com.gaius.contentupload";

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

    public void loadPossibleFriends() {
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(true);

        path = context.getExternalFilesDir(null);
        File file = new File(path, "contacts.txt");
        ArrayList<String> phoneNumbers = new ArrayList<String>();

        try {
            FileOutputStream stream = new FileOutputStream(file);

            Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

            while (phones.moveToNext()) {
                String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
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

        progressDialog.setMessage("Checking");
        showDialog();

        String uploadId = UUID.randomUUID().toString();
        try {
            MultipartUploadRequest request = new MultipartUploadRequest(context, uploadId,  prefs.getString("base_url", null) + "findFriends2.py")
                    .addParameter("token", prefs.getString("token", "null"))
                    .setUtf8Charset()
                    .setNotificationConfig(getNotificationConfig(uploadId, R.string.notification_title))
                    .setMaxRetries(5)
                    .addFileToUpload(contactPath, "contacts")
                    .setDelegate(new UploadStatusDelegate() {
                        @Override
                        public void onProgress(Context context, UploadInfo uploadInfo) {
                            // your code here
                        }

                        @Override
                        public void onError(Context context, UploadInfo uploadInfo, ServerResponse serverResponse, Exception exception) {
                            try {
                                Log.d("Account Requests", "onError"+serverResponse.getHeaders() + serverResponse.getBodyAsString() + serverResponse.getHttpCode());
//                                Toast.makeText(getApplicationContext(), "Something went wrong with the upload ("+ serverResponse.getHttpCode()+")", Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
                            // remove the notification as it is no longer needed to keep the service alive
                            if (uploadInfo.getNotificationID() != null) {
                                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                                notificationManager.cancel(uploadInfo.getNotificationID());
                            }

                            if (serverResponse.getBodyAsString().contains("@@ERROR##"))  {
                                Toast.makeText(context, serverResponse.getBodyAsString().replace("@@ERROR##","ERROR:").trim(), Toast.LENGTH_LONG).show();
                            } else {
                                Log.d("yasir", serverResponse.getBodyAsString()+"");
                                try {
                                    //converting the string to json array object
                                    JSONArray array = new JSONArray(serverResponse.getBodyAsString());

                                    if (array.length() == 0 ) {
                                        importFriendsTitle.setVisibility(View.GONE);
                                    }
                                    else {
                                        importFriendsTitle.setVisibility(View.VISIBLE);
                                    }

                                    //traversing through all the object
                                    for (int i = 0; i < array.length(); i++) {

                                        //getting product object from json array
                                        JSONObject friend = array.getJSONObject(i);

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
                                    FriendsAdapter adapter = new FriendsAdapter(getContext(), friendList);
                                    recyclerView.setAdapter(adapter);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Log.d("Yasir","Json error "+e);

                                    if (serverResponse.getBodyAsString().contains("invalid token")) {
                                        LogOut.logout(getContext());
                                        Toast.makeText(getContext(), "You have logged in from another device. Please login again.",
                                                Toast.LENGTH_LONG).show();
                                        Intent i = new Intent(getContext(), LoginActivity.class);
                                        startActivity(i);
                                        getActivity().finish();
                                    }
                                }
                            }
                            hideDialog();
                        }

                        @Override
                        public void onCancelled(Context context, UploadInfo uploadInfo) {
                            hideDialog();
                        }
                    });

            if (button != null) {
                request.addParameter(button, phoneNumber);
            }

            Log.d("thp",request.toString());

            request.startUpload();
        } catch (Exception exc) {
            Log.d("Account Requests", exc.getMessage(), exc);
        }
    }

    private static void showDialog() {
        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    private static void hideDialog() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }

    private static UploadNotificationConfig getNotificationConfig(final String uploadId, @StringRes int title) {
        UploadNotificationConfig config = new UploadNotificationConfig();

        PendingIntent clickIntent = PendingIntent.getActivity(
                context, 1, new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        config.setTitleForAllStatuses("title")
                .setRingToneEnabled(false)
                .setClickIntentForAllStatuses(clickIntent)
                .setClearOnActionForAllStatuses(true);

        config.getProgress().message = "Uploaded " + UPLOADED_FILES + " of " + TOTAL_FILES
                + " at " + UPLOAD_RATE + " - " + PROGRESS;
        config.getProgress().iconResourceID = R.drawable.ic_upload;
        config.getProgress().iconColorResourceID = Color.BLUE;

        config.getCompleted().message = "Upload completed successfully in " + ELAPSED_TIME;
        config.getCompleted().iconResourceID = R.drawable.ic_upload_success;
        config.getCompleted().iconColorResourceID = Color.GREEN;

        config.getError().message = "Error while uploading";
        config.getError().iconResourceID = R.drawable.ic_upload_error;
        config.getError().iconColorResourceID = Color.RED;

        config.getCancelled().message = "Upload has been cancelled";
        config.getCancelled().iconResourceID = R.drawable.ic_cancelled;
        config.getCancelled().iconColorResourceID = Color.YELLOW;

        return config;
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
                            FriendsAdapter adapter = new FriendsAdapter(getContext(), friendList2);
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
}
