package com.gaius.gaiusapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.Volley;
import com.gaius.gaiusapp.utils.FontProvider;
import com.gaius.gaiusapp.utils.ResourceHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cn.jzvd.JzvdStd;

public class RenderMAML extends AppCompatActivity {
    SharedPreferences prefs;
    String fidelity;
    String token;
    private RequestQueue mRequestQueue;
    private String response_var = "";
    private boolean EDIT_MODE = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String mUrl=null;
        String adUrl=null;
        String mPageUrl=null;
        String mNoAds=null;
        String campaign=null;
        String hostIP;
        String hostPort;
        String hostPath;

        setContentView(R.layout.maml_page);
        super.onCreate(savedInstanceState);

        mRequestQueue = Volley.newRequestQueue(getApplicationContext());

        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        hostIP = prefs.getString("ip_edge", "91.230.41.34");
        hostPort = prefs.getString("port_edge", "8080");
        hostPath = prefs.getString("path_edge", "test");
        fidelity = prefs.getString("fidelity_level", "high");
        token = prefs.getString("token", "null");

        final FrameLayout root = findViewById(R.id.root);

        Uri data = getIntent().getData();
        if (data != null && data.toString().contains("http://gaiusnetworks.com/content/")) {
            Intent intent = this.getIntent();
            Bundle bundle = intent.getExtras();
            mUrl =  "http://" + hostIP + ":" + hostPort + "/" + hostPath + "/";
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
        }

        // request and parse the index.maml page
        try {
            requestPage(mUrl, adUrl, mPageUrl, mNoAds, root, campaign);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Error parsing MAML: " + e, Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void requestPage(final String mUrl, final String adUrl, final String mPageUrl, final String mNoAds, final FrameLayout root, final String campaign) {
        String tmpUrl = createURL(mUrl, mPageUrl, mNoAds, campaign);

        downloadMaml(tmpUrl, adUrl, new RenderMAML.VolleyCallback() {
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
                            drawImage(objects[i], null, root);
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
                                    String shareBody = "http://gaiusnetworks.com/page/"+mPageUrl.replace("./content/","");
                                    String shareSub = "Check this page on GAIUS";
                                    sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, shareSub);
                                    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                                    startActivity(Intent.createChooser(sharingIntent, "Share using"));
                                }
                            });


                            final ImageView likeImageView = view.findViewById(R.id.imageViewLikes);

                            if (pageLiked.equals("true")) {
                                likeImageView.setImageResource(R.drawable.ic_liked);
                            }

                            likeImageView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(final View v) {
                                    final VolleyCallback callback = new VolleyCallback() {
                                        @Override
                                        public void onSuccess(final String result) {

                                            if (result.contains("Success") && result.contains("true")) {
                                                mLikes.setText((Integer.parseInt(mLikes.getText().toString()) + 1) + "");
                                                likeImageView.setImageResource(R.drawable.ic_liked);
                                            }
                                            else if (result.contains("Success") && result.contains("false")) {
                                                mLikes.setText((Integer.parseInt(mLikes.getText().toString()) - 1) + "");
                                                likeImageView.setImageResource(R.drawable.ic_like);
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
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
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

    private void downloadMaml(final String mUrl, final String adUrl, final RenderMAML.VolleyCallback callback) {
        Log.d("MAML", "MamlPageActivity: Request index.maml " + mUrl);

        InputStreamVolleyRequest request = new InputStreamVolleyRequest(Request.Method.GET, mUrl,
                new Response.Listener<byte[]>() {
                    @Override
                    public void onResponse(byte[] response) {
                        try {

                            if (response!=null) {
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

        request.setShouldCache(false);
        mRequestQueue.add(request);
    }

    private void drawImage(String data, Bitmap img_file, FrameLayout root) {
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
                            Intent intent = new Intent(view.getContext(), RenderMAML.class);
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
//                Log.d(Constants.TAG, "MamlPageActivity: href exception " + e.getMessage());
                //e.printStackTrace();
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
                                Bitmap bmp = BitmapFactory.decodeByteArray(response, 0, response.length);

                                drawImage(jsonstring, bmp, root);
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
                                    Bitmap bmp = BitmapFactory.decodeByteArray(response, 0, response.length);

                                    JzvdStd jzVideoPlayerStandard = new JzvdStd (RenderMAML.this);
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
                FontProvider fontProvider = new FontProvider(getResources());;
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
                            Intent intent = new Intent(view.getContext(), RenderMAML.class);
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