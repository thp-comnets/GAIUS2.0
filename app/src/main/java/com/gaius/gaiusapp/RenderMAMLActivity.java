package com.gaius.gaiusapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.alexvasilkov.gestures.Settings;
import com.alexvasilkov.gestures.views.interfaces.GestureView;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.Volley;
import com.gaius.gaiusapp.utils.FontProvider;
import com.gaius.gaiusapp.utils.ResourceHelper;
import com.jaredrummler.android.device.DeviceName;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;

import static com.gaius.gaiusapp.utils.ResourceHelper.getImageUri;

public class RenderMAMLActivity extends AppCompatActivity {
    SharedPreferences prefs;
    String fidelity;
    String token;
    private RequestQueue mRequestQueue;
    private String response_var = "";
    private GestureView gestureView;
    private int pageSize = 0;
    private AtomicInteger requestsCounter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String mUrl=null;
        String adUrl=null;
        String mPageUrl=null;
        String mNoAds=null;
        String campaign=null;

        setContentView(R.layout.maml_page);

        super.onCreate(savedInstanceState);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        gestureView = findViewById(R.id.drawer_layout);
        gestureView.getController().getSettings()
                .setMaxZoom(4f)
                .setDoubleTapZoom(-1f) // Falls back to max zoom level
                .setPanEnabled(true)
                .setZoomEnabled(true)
                .setDoubleTapEnabled(true)
                .setRotationEnabled(false)
                .setRestrictRotation(false)
                .setOverscrollDistance(0f, 0f)
                .setOverzoomFactor(2f)
                .setFillViewport(false)
                .setFitMethod(Settings.Fit.INSIDE)
                .setGravity(Gravity.CENTER);

        mRequestQueue = Volley.newRequestQueue(getApplicationContext());

        // yasir: adding a counter to know when all request are finished, to record page load time
        requestsCounter = new AtomicInteger(0);

        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        fidelity = prefs.getString("fidelity_level", "high");
        token = prefs.getString("token", "null");

        final FrameLayout root = findViewById(R.id.root);

        Uri data = getIntent().getData();
        if (data != null && data.toString().contains("http://gaiusnetworks.com/content/")) {
            Intent intent = this.getIntent();
            Bundle bundle = intent.getExtras();
            mUrl =  prefs.getString("base_url", null);
            adUrl = mUrl;
//            adUrl =  (String) bundle.getSerializable("BASEURL");
            mPageUrl =  "./content/" + data.toString().replace("http://gaiusnetworks.com/content/","");
            mNoAds =  (String) bundle.getSerializable("LOCAL_CONTENT");
            campaign = (String) bundle.getSerializable("CAMPAIGN");
        }
        else {
            Intent intent = this.getIntent();
            Bundle bundle = intent.getExtras();
            mUrl =  (String) bundle.getSerializable("BASEURL");
            adUrl =  (String) bundle.getSerializable("BASEURL");
            mPageUrl =  (String) bundle.getSerializable("URL");
            mNoAds =  (String) bundle.getSerializable("LOCAL_CONTENT");
            campaign = (String) bundle.getSerializable("CAMPAIGN");
            if (bundle.getString("title") != null) { //TODO handle this in requestPage and send title with the file?
                getSupportActionBar().setTitle(bundle.getString("title"));
            }
        }

        // request and parse the index.maml page
        try {
            requestPage(mUrl, adUrl, mPageUrl, mNoAds, root, campaign);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Error parsing MAML: " + e, Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    //handle the back arrow press in the toolbar
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void requestPage(final String mUrl, final String adUrl, final String mPageUrl, final String mNoAds, final FrameLayout root, final String campaign) {
        String tmpUrl = createURL(mUrl, mPageUrl, mNoAds, campaign);

        final Long startTime = System.currentTimeMillis();
        pageSize=0;

        downloadMaml(tmpUrl, adUrl, new RenderMAMLActivity.VolleyCallback() {
            @Override
            public void onSuccess(final String result) {

                if (result.contains("@@ERROR##"))  {
                    Toast.makeText(getApplicationContext(), result.replace("@@ERROR##","ERROR:").trim(), Toast.LENGTH_LONG).show();
                    return;
                }

                final String[] objects = result.split("\n");
                try {
                    String type;
                    JSONObject parser;

                    for (int i = 0; i < objects.length; i++) {
                        parser = new JSONObject(objects[i]); //TODO: handle error in maml file
                        type = parser.getString("type");

                        if (type.equals("img")) {
                            String imgUrl = parser.getString("url");

                            if (imgUrl.contains("./ads")) {
                                imgUrl = adUrl + "ads/" + imgUrl.substring(6).replace(" ","%20");
                            } else
                                imgUrl = mUrl + mPageUrl + "/" + imgUrl;
                            Log.d("MAML", "MamlPageActivity: downloadImage " + imgUrl);

                            downloadImage(imgUrl, objects[i], mRequestQueue, root);

                        } else if (type.equals("video")) {
                            String videoUrl = parser.getString("url");

                            if (videoUrl.contains("./ads")) {
                                drawVideo(adUrl + "ads/" + videoUrl.substring(6).replace(" ","%20"), objects[i], root);
                            } else
                                drawVideo(mUrl + "/" + mPageUrl + "/" + videoUrl, objects[i], root);
                        }
                        else if (type.equals("rect")) {
                            drawImage(objects[i], null, root, "");
                        } else if (type.equals("txt")) {
                            drawText(objects[i], root);
                        } else if (type.equals("txtField")) {
                            drawTextField(objects[i], root);
                        } else if (type.equals("bg")) {
                            if (parser.has("bgFile")) {
                                String bg = parser.getString("bgFile");
                                downloadBg(bg, mRequestQueue);
                            }
                            else {
                                String c = parser.getString("color");
                                findViewById(android.R.id.content).setBackgroundColor(Color.parseColor(c));
                            }
                        }

                        else if (type.equals("stats")) {
                            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            params.setMarginStart(50);
                            params.topMargin = Integer.parseInt(parser.getString("y"));

                            View view = getLayoutInflater().inflate(R.layout.activity_maml_views_likes, root, false);

                            final String pageLiked = parser.getString("pageLiked");

                            String text = parser.getString("views");
                            TextView mViews = view.findViewById(R.id.views);
                            mViews.setText(text + " views");

                            final TextView mLikes = view.findViewById(R.id.likesCounter);
                            text = parser.getString("likes");
                            mLikes.setText(text);

                            final ImageView shareView = view.findViewById(R.id.share);
                            shareView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                                    sharingIntent.setType("text/plain");
                                    String shareBody = "http://gaiusnetworks.com/"+mPageUrl.replace("./","");
                                    String shareSub = "Check this page on GAIUS";
                                    sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, shareSub);
                                    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                                    startActivity(Intent.createChooser(sharingIntent, "Share using"));
                                }
                            });


                            final ImageView likeImageView = view.findViewById(R.id.imageViewLikes);

                            if (pageLiked.equals("true")) {
                                likeImageView.setImageResource(R.drawable.icon_liked);
                            }

                            likeImageView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(final View v) {
                                    final VolleyCallback callback = new VolleyCallback() {
                                        @Override
                                        public void onSuccess(final String result) {

                                            if (result.contains("Success") && result.contains("true")) {
                                                mLikes.setText((Integer.parseInt(mLikes.getText().toString()) + 1) + "");
                                                likeImageView.setImageResource(R.drawable.icon_liked);
                                            }
                                            else if (result.contains("Success") && result.contains("false")) {
                                                mLikes.setText((Integer.parseInt(mLikes.getText().toString()) - 1) + "");
                                                likeImageView.setImageResource(R.drawable.icon_like);
                                            }
                                        }
                                    };

                                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                    String url = mUrl + "like.py?url=" + mPageUrl + "&token=" + prefs.getString("token", "null");
                                    Log.d("MAML", "MamlPageActivity: user liked page " + url);

                                    InputStreamVolleyRequest request = new InputStreamVolleyRequest(Request.Method.GET, url,
                                            new Response.Listener<byte[]>() {
                                                @Override
                                                public void onResponse(byte[] response) {
                                                    try {
                                                        if (response != null) {
                                                            response_var = new String(response);
                                                            callback.onSuccess(response_var);
                                                        }
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            Log.d("MAML", "MamlPageActivity: onErrorResponse from " + mUrl);
                                            Toast.makeText(getApplicationContext(), "Error Response: " + error, Toast.LENGTH_LONG).show();
                                            error.printStackTrace();
                                        }
                                    }, null);
                                    requestsCounter.incrementAndGet(); // yasir
                                    request.setShouldCache(false);
                                    mRequestQueue.add(request);
                                }
                            });

                            root.addView(view, params);
                        }
                        else {
                            Log.d("MAML", "MamlPageActivity: Can't identify this type " + type);
                        }
                    }

                    mRequestQueue.addRequestFinishedListener(new RequestQueue.RequestFinishedListener<Object>() {
                        @Override
                        public void onRequestFinished(Request<Object> request) {
                            requestsCounter.decrementAndGet();

                            if (requestsCounter.get() == 0) {
                                Long endTime = System.currentTimeMillis();
                                String mUrl=prefs.getString("base_url", "192.169.152.158:60001") + "stats.py?PLT="+(endTime-startTime)+"&page="+mPageUrl;
                                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

                                String f = prefs.getString("fidelity_level", "high");
                                mUrl+="&fidelity="+f;
                                mUrl+="&size="+pageSize;

                                List<String> stats = getPhoneStats();
                                mUrl+="&networkType="+stats.get(0);
                                mUrl+="&manufacturer="+stats.get(1);
                                mUrl+="&deviceName="+stats.get(4).replace(" ","_");
                                mUrl+="&model="+stats.get(3).replace(" ","_");

                                InputStreamVolleyRequest statReq = new InputStreamVolleyRequest(Request.Method.GET, mUrl,
                                        new Response.Listener<byte[]>() {
                                            @Override
                                            public void onResponse(byte[] response) {
                                                try {

                                                    if (response!=null) {
                                                        response_var = new String(response);
                                                        Log.d("maml", "finished loading page " + response_var);
                                                    }
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        },new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Log.d("MAML", "MamlPageActivity: onErrorResponse");
                                        Toast.makeText(getApplicationContext(), "Error Response: " + error, Toast.LENGTH_LONG).show();
                                        error.printStackTrace();
                                    }
                                }, null);
                                request.setShouldCache(false);
                                mRequestQueue.add(statReq);
                            }
                        }
                    });


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private List<String> getPhoneStats () {
        ConnectivityManager mConnectivity = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);;
        NetworkInfo info = mConnectivity.getActiveNetworkInfo();
        String networkType = "";

        if (info != null) {
            if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                networkType = "Wifi";
            }
            else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                int tmp = info.getSubtype();
                switch (tmp) {
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                        networkType = "2G,GPRS";
                        break;
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                        networkType = "2G,GPRS";
                        break;
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                        networkType = "2G,CDMA";
                        break;
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                        networkType = "2G,1xRTT";
                        break;
                    case TelephonyManager.NETWORK_TYPE_IDEN:
                        networkType = "2G,IDEN";
                        break;
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                        networkType = "3G,UMTS";
                        break;
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                        networkType = "3G,EVDO_0";
                        break;
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                        networkType = "3G,EVDO_A";
                        break;
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                        networkType = "3G,HSDPA";
                        break;
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                        networkType = "3G,HSUPA";
                        break;
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                        networkType = "3G,HSPA";
                        break;
                    case TelephonyManager.NETWORK_TYPE_EVDO_B:
                        networkType = "3G,EVDO_B";
                        break;
                    case TelephonyManager.NETWORK_TYPE_EHRPD:
                        networkType = "3G,EHRPD";
                        break;
                    case TelephonyManager.NETWORK_TYPE_HSPAP:
                        networkType = "3G,HSPAP";
                        break;
                    case TelephonyManager.NETWORK_TYPE_TD_SCDMA:
                        networkType = "3G,TD_SCDMA";
                        break;
                    case TelephonyManager.NETWORK_TYPE_LTE:
                        networkType = "4G,LTE";
                        break;
                    case TelephonyManager.NETWORK_TYPE_IWLAN:
                        networkType = "4G,IWLAN";
                        break;
                    default:
                        networkType = "Unknown subtype: "+tmp;
                }
            }
        }

        DeviceName.DeviceInfo deviceInfo = DeviceName.getDeviceInfo(getApplicationContext());
        String manufacturer = deviceInfo.manufacturer;  // "Samsung"
        String name = deviceInfo.marketName;            // "Galaxy S8+"
        String model = deviceInfo.model;                // "SM-G955W"
        String codename = deviceInfo.codename;          // "dream2qltecan"
        String deviceName = deviceInfo.getName();       // "Galaxy S8+"

        return Arrays.asList(networkType, manufacturer, name, model, deviceName);
    }

    private String createURL(final String mUrl, final String mPageUrl, final String mNoAds, final String campaign) {
        String tmpUrl = mUrl+"index.maml?url="+mPageUrl;

        switch (fidelity) {
            case "high":
                tmpUrl+="&fidelity=1";
                break;
            case "medium":
                tmpUrl+="&fidelity=2";
                break;
            case "low":
                tmpUrl+="&fidelity=3";
                break;
        }

        tmpUrl += "&resolution="+ ResourceHelper.getScreenWidth(this);

        if (mNoAds != null) {
            tmpUrl += "&noADs=1";
        }
        if (campaign != null) {
            tmpUrl += "&campaign="+campaign;
        }
        if (!token.equals("null")) {
            tmpUrl += "&token=" + token;
        }

        return tmpUrl;
    }

    private void downloadMaml(final String mUrl, final String adUrl, final RenderMAMLActivity.VolleyCallback callback) {
        Log.d("MAML", "MamlPageActivity: Request index.maml " + mUrl);

        InputStreamVolleyRequest request = new InputStreamVolleyRequest(Request.Method.GET, mUrl,
                new Response.Listener<byte[]>() {
                    @Override
                    public void onResponse(byte[] response) {
                        try {

                            if (response!=null) {
                                pageSize += response.length;
                                response_var = new String(response);
                                callback.onSuccess(response_var);
                            }
                        } catch (Exception e) {
//                            Log.d("KEY_ERROR", "UNABLE TO DOWNLOAD FILE");
                            e.printStackTrace();
                        }
                    }
                },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("MAML", "MamlPageActivity: onErrorResponse from " + mUrl);
                Toast.makeText(getApplicationContext(), "Error Response: " + error, Toast.LENGTH_LONG).show();
                error.printStackTrace();
            }
        }, null);

        requestsCounter.incrementAndGet(); // yasir
        request.setShouldCache(false);
        mRequestQueue.add(request);
    }

    private void drawImage(String data, Bitmap img_file, FrameLayout root, final String url) {
        try {
            JSONObject parser = new JSONObject(data);
            int x = parser.getInt("x");
            int y = parser.getInt("y");
            int w = parser.getInt("w");
            int h = parser.getInt("h");

            ImageView img = new ImageView(this);
            if (img_file == null) {
                String color = parser.getString("color");
                img_file = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888); //, Bitmap.Config.RGB_565);
                Canvas canvas = new Canvas(img_file);
                canvas.drawColor(Color.parseColor(color));
            }
            img.setImageBitmap(img_file);
            img.setScaleType(ImageView.ScaleType.FIT_XY);

            try {
                Intent intent = this.getIntent();
                Bundle bundle = intent.getExtras();
                final String mUrl =  (String) bundle.getSerializable("BASEURL");
                final String adUrl =  (String) bundle.getSerializable("ADURL");
                final String mChannel =  (String) bundle.getSerializable("CHANNEL");
                final String href = parser.getString("href");
                final String campaign = parser.getString("campaign");

                img.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // request the sub-page
                        if (href.contains("http")) {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(href));
                            startActivity(browserIntent);
                        }
                        else {
                            Intent intent = new Intent(view.getContext(), RenderMAMLActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("BASEURL", mUrl);
                            bundle.putSerializable("ADURL", adUrl);
                            bundle.putSerializable("URL", href);
                            bundle.putSerializable("CAMPAIGN", campaign);
                            bundle.putSerializable("CHANNEL", mChannel + "/" + href);
                            intent.putExtras(bundle);
                            startActivity(intent);
                        }
                    }
                });

            }
            catch(Exception e) {
//                Log.d("MAML", "MamlPageActivity: href exception " + e.getMessage());
                //e.printStackTrace();
            }

            // enable image saving
            if (img_file != null) {
                final Bitmap finalImg_file = img_file;
                img.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        final PopupMenu popupMenu = new PopupMenu(getApplicationContext(), view);
                        popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
                            @Override
                            public void onDismiss(PopupMenu menu) {
                                popupMenu.dismiss();
                            }
                        });
                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.toString()) {
                                    case "Save":
                                        MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), finalImg_file ,url.substring(url.lastIndexOf('/')) , "");
                                        Toast.makeText(getApplicationContext(), "Image saved locally on device.", Toast.LENGTH_LONG).show();
                                        break;

                                    case "Share":
                                        Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
                                        shareIntent.putExtra(Intent.EXTRA_STREAM, getImageUri(getApplicationContext(), finalImg_file));
                                        shareIntent.setType("image/jpeg");
                                        startActivity(Intent.createChooser(shareIntent, "Share image with:"));
                                        break;
                                }
                                popupMenu.dismiss();

                                return true;
                            }
                        });
                        popupMenu.inflate(R.menu.save_image_popup_menu);
                        popupMenu.show();

                        return true;
                    }
                });
            }


            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(w, h);
            if (root.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
                params.setMarginStart(ResourceHelper.getScreenWidth(this) - x - w);
            } else {
                params.leftMargin = x;
            }

            params.topMargin  = y;

            root.addView(img, params);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void downloadImage(final String mUrl, final String jsonstring, RequestQueue mRequestQueue, final FrameLayout root) {
        final InputStreamVolleyRequest request = new InputStreamVolleyRequest(Request.Method.GET, mUrl,
                new Response.Listener<byte[]>() {
                    @Override
                    public void onResponse(final byte[] response) {

                        try {
                            if (response != null) {
                                pageSize += response.length;
                                Bitmap bmp = BitmapFactory.decodeByteArray(response, 0, response.length);

                                drawImage(jsonstring, bmp, root, mUrl);
                            }
                        } catch (Exception e) {
//                                    Log.d("KEY_ERROR", "UNABLE TO DOWNLOAD FILE 2");
                            e.printStackTrace();
                        }

                    }
                },new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }, null);

        requestsCounter.incrementAndGet(); // yasir
        request.setShouldCache(true);
        mRequestQueue.add(request);
    }

    public void drawVideo (final String url, final String jsonstring, final FrameLayout root) {
        try {
            JSONObject parser = new JSONObject(jsonstring);
            final int x = parser.getInt("x");
            final int y = parser.getInt("y");
            final int w = parser.getInt("w");
            final int h = parser.getInt("h");

            String mUrl = url.replace(".mp4", ".jpg");
            final InputStreamVolleyRequest request = new InputStreamVolleyRequest(Request.Method.GET, mUrl,
                    new Response.Listener<byte[]>() {
                        @Override
                        public void onResponse(final byte[] response) {

                            try {
                                if (response != null) {
                                    pageSize += response.length;
                                    Bitmap bmp = BitmapFactory.decodeByteArray(response, 0, response.length);

                                    JzvdStd jzVideoPlayerStandard = new JzvdStd (RenderMAMLActivity.this);
                                    jzVideoPlayerStandard.setUp(url,"", JzvdStd.SCREEN_WINDOW_NORMAL);
                                    jzVideoPlayerStandard.thumbImageView.setImageBitmap(bmp);

                                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(w, h);
                                    params.leftMargin = x;
                                    params.topMargin  = y;
                                    root.addView(jzVideoPlayerStandard, params);
                                }
                            } catch (Exception e) {
//                                    Log.d("KEY_ERROR", "UNABLE TO DOWNLOAD FILE 2");
                                e.printStackTrace();
                            }

                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            }, null);

            requestsCounter.incrementAndGet(); // yasir
            request.setShouldCache(true);
            mRequestQueue.add(request);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private EditText drawTextField(String data, FrameLayout root) {
        JSONObject parser;
        EditText editText = new EditText(this);

        try {
            parser = new JSONObject(data);

            int x = parser.getInt("x");
            int y = parser.getInt("y");
            int w = parser.getInt("w");
            int h = parser.getInt("h");
            String text = parser.getString("txt");

            editText.setHint(text);
            editText.setPadding(10,0,0,0);
            editText.setHintTextColor(Color.GRAY);
            editText.setTextColor(Color.BLACK);
            editText.setTextSize(10);
            editText.setSingleLine();
            editText.setBackgroundResource(R.drawable.edittext);

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(w,h);
            if (root.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
                params.setMarginStart(ResourceHelper.getScreenWidth(this) - x - w);
            } else {
                params.leftMargin = x;
            }
            params.topMargin  = y;
            root.addView(editText, params);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return editText;
    }

    private void downloadBg (final String mUrl, RequestQueue mRequestQueue) {
        final InputStreamVolleyRequest request = new InputStreamVolleyRequest(Request.Method.GET, mUrl,
                new Response.Listener<byte[]>() {
                    @Override
                    public void onResponse(final byte[] response) {

                        try {
                            if (response != null) {
                                Bitmap bgBitmap = BitmapFactory.decodeByteArray(response, 0, response.length);
                                Drawable bgDrawable = new BitmapDrawable(getResources(), bgBitmap);
                                findViewById(android.R.id.content).setBackground(bgDrawable);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }, null);

        mRequestQueue.add(request);
    }

    private void drawText(String data, FrameLayout root) {
        JSONObject parser;
        try {
            parser = new JSONObject(data);

            int x = parser.getInt("x");
            int y = parser.getInt("y");
            int w = parser.getInt("w");
            int h = parser.getInt("h");
            int font = parser.getInt("font");
            String text = parser.getString("txt").replaceAll("<br>","\n").replaceAll("<doubleQuote>","\"");
            String color = parser.getString("color");

//            int opacity;
            String fontType = parser.getString("font-type");

            TextView txt = new TextView(this);
            txt.setText(text);
            txt.setTextSize(TypedValue.COMPLEX_UNIT_PX, font);

            try {
                FontProvider fontProvider = new FontProvider(getResources());
                txt.setTypeface(fontProvider.getTypeface(fontType));
            } catch (Exception e) {
                e.printStackTrace();
            }

            txt.setTextColor(Color.parseColor(color));
//            txt.setZ(100);

            try {
                Intent intent = this.getIntent();
                Bundle bundle = intent.getExtras();
                final String mUrl =  (String) bundle.getSerializable("BASEURL");
                final String adUrl =  (String) bundle.getSerializable("ADURL");
                final String mChannel =  (String) bundle.getSerializable("CHANNEL");
                final String href = parser.getString("href");
                final String campaign = parser.getString("campaign");

                txt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (href.contains("http")) {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(href));
                            startActivity(browserIntent);
                        }
                        else {
                            Intent intent = new Intent(view.getContext(), RenderMAMLActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("BASEURL", mUrl);
                            bundle.putSerializable("ADURL", adUrl);
                            bundle.putSerializable("URL", href);
                            bundle.putSerializable("CAMPAIGN", campaign);
                            bundle.putSerializable("CHANNEL", mChannel + "/" + href);
                            intent.putExtras(bundle);
                            startActivity(intent);
                        }
                    }
                });
            } catch (JSONException e) {
//                e.printStackTrace();
            }
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(w, h);
            if (root.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
                params.setMarginStart(ResourceHelper.getScreenWidth(this) - x - w);
            } else {
                params.leftMargin = x;
            }
            params.topMargin  = y;
            root.addView(txt, params);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    interface VolleyCallback {
        void onSuccess(String result);
    }

    @Override
    public void onBackPressed() {
        Jzvd.releaseAllVideos();
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Jzvd.releaseAllVideos(); // thp: I think this should go to onStop()
    }
}

class InputStreamVolleyRequest extends Request<byte[]> {
    private final Response.Listener<byte[]> mListener;
    private Map<String, String> mParams;

    //create a static map for directly accessing headers
    private Map<String, String> responseHeaders;

    public InputStreamVolleyRequest(int method, String mUrl , Response.Listener<byte[]> listener,
                                    Response.ErrorListener errorListener, HashMap<String, String> params) {
        // TODO Auto-generated constructor stub

        super(Method.GET, mUrl, errorListener);
        // this request would never use cache.
        setShouldCache(false);
        mListener = listener;
        mParams=params;
    }

    @Override
    protected Map<String, String> getParams() {
        return mParams;
    }

    @Override
    protected void deliverResponse(byte[] response) {
        mListener.onResponse(response);
    }

    @Override
    protected Response<byte[]> parseNetworkResponse(NetworkResponse response) {
        //Initialise local responseHeaders map with response headers received
        responseHeaders = response.headers;

        //Pass the response data here
        return Response.success( response.data, HttpHeaderParser.parseCacheHeaders(response));
    }
}