package com.gaius.gaiusapp;

import android.Manifest;
import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ANRequest;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.AnalyticsListener;
import com.androidnetworking.interfaces.OkHttpResponseListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import com.gaius.gaiusapp.adapters.FontsAdapter;
import com.gaius.gaiusapp.utils.Constants;
import com.gaius.gaiusapp.utils.CustomScrollView;
import com.gaius.gaiusapp.utils.Font;
import com.gaius.gaiusapp.utils.FontProvider;
import com.gaius.gaiusapp.utils.Layer;
import com.gaius.gaiusapp.utils.MamlPageBuilder;
import com.gaius.gaiusapp.utils.ResourceHelper;
import com.gaius.gaiusapp.utils.TextLayer;
import com.gaius.gaiusapp.widget.MotionView;
import com.gaius.gaiusapp.widget.TextEditorDialogFragment;
import com.gaius.gaiusapp.widget.entity.ImageEntity;
import com.gaius.gaiusapp.widget.entity.MotionEntity;
import com.gaius.gaiusapp.widget.entity.RectEntity;
import com.gaius.gaiusapp.widget.entity.TextEntity;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadService;
import net.gotev.uploadservice.UploadStatusDelegate;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import ly.kite.instagramphotopicker.InstagramPhoto;
import ly.kite.instagramphotopicker.InstagramPhotoPicker;

import static com.gaius.gaiusapp.utils.ResourceHelper.getResizedBitmap;
import static net.gotev.uploadservice.Placeholders.ELAPSED_TIME;
import static net.gotev.uploadservice.Placeholders.PROGRESS;
import static net.gotev.uploadservice.Placeholders.TOTAL_FILES;
import static net.gotev.uploadservice.Placeholders.UPLOADED_FILES;
import static net.gotev.uploadservice.Placeholders.UPLOAD_RATE;


public class CreativeWebCreation extends AppCompatActivity implements TextEditorDialogFragment.OnTextLayerCallback {

    public static final int IMAGE_BITMAP =0;
    public static final int VIDEO_BITMAP =1;
    public static final int VIDEO_BITMAP_THUMB =2;
    public static final int IMAGE_BITMAP_SHARED =3;

    //    Instagram codes
    static final String CLIENT_ID = "2e07e447804249ce820409d3c43338f5";
    static final String REDIRECT_URI = "http://gaiusnetworks.com/app/instagram-callback";

    public AlertDialog alertDSubmit = null;
    public int insagram_images_y = 0;
    public int tmpW = 0;
    public int tmpH = 0;

    ArrayList<String> imagePaths, videoPaths;
    String mamlFilePath;

    protected MotionView motionView;
    protected View textEntityEditPanel;
    protected View rectEntityEditPanel;
    private CustomScrollView customScrollView;
    private ProgressDialog progress;


    private final MotionView.MotionViewCallback motionViewCallback = new MotionView.MotionViewCallback() {
        @Override
        public void onEntitySelected(@Nullable MotionEntity entity) {

            if (entity == null) {
                customScrollView.setEnableScrolling(true);
            } else {
                customScrollView.setEnableScrolling(false);
            }

            if (entity instanceof TextEntity) {
                textEntityEditPanel.setVisibility(View.VISIBLE);

                TextView txtView = findViewById(R.id.text_font_size);
                if (txtView != null) {
                    txtView.setText((int) ((TextEntity) entity).getFontSize()+"");
                }
                entity.fontTxtView = txtView;
            } else {
                textEntityEditPanel.setVisibility(View.GONE);
            }

            if (entity instanceof RectEntity) {
                rectEntityEditPanel.setVisibility(View.VISIBLE);
            } else {
                rectEntityEditPanel.setVisibility(View.GONE);
            }
        }

        @Override
        public void onEntityDoubleTap(@NonNull MotionEntity entity) {
            if (entity instanceof TextEntity) {
                startTextEntityEditing();
            } else if (entity instanceof RectEntity) {
                changeRectEntityColor();
            } else if (entity instanceof ImageEntity) {
                if (entity.getLayer().isVideo()) {
                    videoBrowse();
                } else {
                    // yasir keeping the scaling of the image
                    tmpW = entity.getWidth();
                    tmpH = entity.getHeight();
                    imageBrowse();
                }

            }
        }
    };

    private FontProvider fontProvider;
    private DisplayMetrics mDisplayMetrics;

    CardView textButton, imageButton, videoButton, rectangleButton, backgroundButton, instagramButton;

    private RequestQueue mRequestQueue;
    private String BASE_URL;
    private String iconPath;
    private String pageName;
    private String pageDescription;
    private String shareText=null;
    private String shareImgPath=null;

    private List<String> shareImgsPath=null;
    private Uri iconUri;
    private Bitmap pageIcon=null;
    private ImageView iconImageView;
    private String bgUrl=null;

    private final int PICK_VIDEO_REQUEST = 1;
    private final int EXTERNAL_STORAGE_PERMISSIONS_REQUEST_CODE = 2;
    private static final int PICK_ICON_REQUEST = 3;
    private static final int CROP_ICON_REQUEST = 4;
    static final int REQUEST_CODE_INSTAGRAM_PICKER = 5;
    private int viewId;
    private Uri mCropImageUri;
    private float aspect;
    private boolean EDIT_MODE = false;
    private String currentPageUrl;
    private ProgressDialog progressDialog;
    private boolean doubleBackToExitPressedOnce = false;

    @Override
    @SuppressLint("NewApi")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_content_builder);

        this.fontProvider = new FontProvider(getResources());

        motionView = (MotionView) findViewById(R.id.main_motion_view);
        textEntityEditPanel = findViewById(R.id.main_motion_text_entity_edit_panel);
        textEntityEditPanel.setVisibility(View.GONE);

        rectEntityEditPanel = findViewById(R.id.main_motion_rect_entity_edit_panel);
        rectEntityEditPanel.setVisibility(View.GONE);

        motionView.setMotionViewCallback(motionViewCallback);
        customScrollView = (CustomScrollView) findViewById(R.id.main_scrollview);

        mDisplayMetrics = new DisplayMetrics();
        ((WindowManager)getApplicationContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(mDisplayMetrics);
        motionView.getLayoutParams().height = mDisplayMetrics.heightPixels * 20;

        initTextEntitiesListeners();
        initRectEntitiesListeners();


//        UploadService.NAMESPACE = BuildConfig.APPLICATION_ID;
//        UploadService.NAMESPACE = "com.gaius.contentupload";
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        BASE_URL = prefs.getString("base_url", null);

        iconPath = getIntent().getStringExtra("PAGE_ICON");
        pageName = getIntent().getStringExtra("PAGE_NAME");
        pageDescription = getIntent().getStringExtra("PAGE_DESCRIPTION");

        shareText = getIntent().getStringExtra("SHARED_TEXT");
        if (shareText != null){
            int x = (int) (0.1*Resources.getSystem().getDisplayMetrics().widthPixels+0.5);
            int y = 150;
            int width = (int) (0.8*Resources.getSystem().getDisplayMetrics().widthPixels+0.5);
            int height = 129;
            int fontSize = 81;
            String font = "Helvetica";
            String color = "#000000";
            final String data2 = "{\"type\":\"txt\",\"txt\":\"" + shareText +"\",\"x\":" + x + ",\"y\":" +y+ ",\"w\":" + width +",\"h\":" +height + ",\"font\":"+fontSize+",\"font-type\":\""+font+"\",\"color\":\""+color+"\"}";

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    drawText(data2);
                }
            }, 100);
        }

        shareImgPath = getIntent().getStringExtra("SHARED_IMAGE");
        if (shareImgPath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(shareImgPath);
            int x = (int) ((Resources.getSystem().getDisplayMetrics().widthPixels - bitmap.getWidth())/2.0);
            addBitmapToLayout(IMAGE_BITMAP_SHARED, bitmap, shareImgPath, x, 50, bitmap.getWidth(), bitmap.getHeight());
        }

        ArrayList<String> shareImgsPath = getIntent().getStringArrayListExtra("SHARED_IMAGES");

        if (shareImgsPath != null) {
            int y = 50;

            for (int i = 0; i < shareImgsPath.size(); i++) {
                Bitmap bitmap = BitmapFactory.decodeFile(shareImgsPath.get(i));
                int x = (int) ((Resources.getSystem().getDisplayMetrics().widthPixels - bitmap.getWidth())/2.0);
                addBitmapToLayout(IMAGE_BITMAP_SHARED, bitmap, shareImgsPath.get(i), x, y, bitmap.getWidth(), bitmap.getHeight());
                y += bitmap.getHeight()+50;
            }
        }

        textButton = findViewById(R.id.text_card);
        imageButton = findViewById(R.id.image_card);
        videoButton = findViewById(R.id.video_card);
        rectangleButton = findViewById(R.id.rectangle_card);
        backgroundButton = findViewById(R.id.background_card);
        instagramButton = findViewById(R.id.instagram_card);

        textButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!motionView.disableMovement) {
                    addTextSticker();
                }

            }
        });

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!motionView.disableMovement) {
                    tmpW = 0;
                    tmpH = 0;
                    motionView.unselectEntity();

                    //FIXME Move this to the oncreate
                    if (CropImage.isExplicitCameraPermissionRequired(getApplicationContext())) {
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE);
                    } else {
                        imageBrowse();
                    }
                }
            }
        });

        videoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!motionView.disableMovement) {
                    //FIXME Move this to the oncreate
                    if (CropImage.isExplicitCameraPermissionRequired(getApplicationContext())) {
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE);
                    } else {
                        videoBrowse();
                    }
                }
                }
        });

        rectangleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!motionView.disableMovement) {
                    Layer layer = new Layer();
                    RectEntity entity = new RectEntity(layer, motionView.getWidth(), motionView.getHeight(), getApplicationContext(), null,customScrollView.getScrollY() + mDisplayMetrics.heightPixels/2, 300, 300);
                    motionView.addEntityAndPosition(entity);

                    changeRectEntityColor();
                }
            }
        });

        backgroundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!motionView.disableMovement) {
                    int color = Color.WHITE;
                    Drawable background = motionView.getBackground();
                    if (background instanceof ColorDrawable) {
                        color = ((ColorDrawable) background).getColor();
                    }
                    ColorPickerDialogBuilder
                            .with(CreativeWebCreation.this)
                            .setTitle(R.string.select_color)
                            .initialColor(color)
                            .showAlphaSlider(false)
                            .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
                            .density(8) // magic number
                            .setPositiveButton(R.string.ok, new ColorPickerClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                    motionView.setBackgroundColor(selectedColor);
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .build()
                            .show();
                }
            }
        });

        instagramButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.google.android.apps.photos");
//                if (launchIntent != null) {
//                    startActivity(launchIntent);//null pointer check in case package name was not found
//                }
                Toast.makeText (getApplicationContext(), "This feature might not work. Currently its by invite only!", Toast.LENGTH_LONG).show ();
                if (!motionView.disableMovement) {
                    InstagramPhotoPicker.startPhotoPickerForResult(CreativeWebCreation.this, CLIENT_ID, REDIRECT_URI, REQUEST_CODE_INSTAGRAM_PICKER);
                }
            }
        });


        mRequestQueue = Volley.newRequestQueue(getApplicationContext(), new HurlStack());

        EDIT_MODE = getIntent().getBooleanExtra("EDIT_MODE", false);
        if (EDIT_MODE) {
            // requesting the page icon
            iconPath = BASE_URL + getIntent().getStringExtra("PAGE_URL")+"icon.png";
            Log.d("yasir", "icon path "+iconPath);

            final InputStreamVolleyRequest request = new InputStreamVolleyRequest(Request.Method.GET, iconPath,
                    new Response.Listener<byte[]>() {
                        @Override
                        public void onResponse(final byte[] response) {

                            try {
                                if (response != null) {
                                    pageIcon = BitmapFactory.decodeByteArray(response, 0, response.length);
                                    iconPath = null;
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

            // request and parse the index.maml page
            try {
                currentPageUrl = getIntent().getStringExtra("PAGE_URL");
                requestPage(currentPageUrl);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Error parsing MAML: " + e, Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(true);
    }

    @Override
    protected void onResume() {
        isReadStoragePermissionGranted();
        isWriteStoragePermissionGranted();
        super.onResume();
    }

    private void requestPage(final String mPageUrl) {
        String tmpUrl = BASE_URL + "index.maml?url=" + mPageUrl + "&fidelity=1" + "&resolution=" + ResourceHelper.getScreenWidth(this) + "&noADs=1";

        downloadMaml(tmpUrl, new RenderMAML.VolleyCallback() {
            @Override
            public void onSuccess(final String result) {

                if (result.contains("@@ERROR##")) {
                    Toast.makeText(getApplicationContext(), result.replace("@@ERROR##", "ERROR:").trim(), Toast.LENGTH_LONG).show();
                    return;
                }


                final String[] objects = result.split("\n");
                try {
                    String type;
                    JSONObject parser;

                    for (int i = 0; i < objects.length; i++) {
                        parser = new JSONObject(objects[i]);
                        type = parser.getString("type");

                        if (type.equals("img")) {
                            String imgUrl = parser.getString("url");
                            imgUrl = BASE_URL + mPageUrl + "/" + imgUrl;
                            Log.d("GAIUS", "ContentBuilderActivity: downloadImage " + imgUrl);
                            downloadImage(imgUrl, objects[i], mRequestQueue);
                        } else if (type.equals("video")) {
                            String videoUrl = parser.getString("url");
                            videoUrl = BASE_URL + mPageUrl + "/" + videoUrl;
                            videoUrl = ResourceHelper.stripFileExtension(videoUrl) + ".jpg";
                            downloadVideoThumbnail(videoUrl, objects[i], mRequestQueue);
                        } else if (type.equals("rect")) {
                            drawRect(objects[i]);
                        } else if (type.equals("txt")) {
                            drawText(objects[i]);
                        } else if (type.equals("txtField")) {
//                            drawTextField(objects[i], root);
                        } else if (type.equals("button")) {
//                            drawButton(objects[i], root);
                        } else if (type.equals("bg")) {
                            if (parser.has("bgFile")) {
                                String bg = parser.getString("bgFile");
                                downloadBg(bg, mRequestQueue);
                            }
                            else {
                                String c = parser.getString("color");
                                motionView.setBackgroundColor(Color.parseColor(c));
                            }
//                            String c = parser.getString("color");
//                            motionView.setBackgroundColor(Color.parseColor(c)); //"#117AB4"));
                        } else {
                            Log.d("GAIUS", "ContentBuilderActivity: Can't identify this type " + type);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void downloadMaml(final String mUrl, final RenderMAML.VolleyCallback callback) {
        // starting to download main index.maml
        final Date startDate = new Date();

        Log.d("GAIUS", "ContentBuilderActivity: Request index.maml " + mUrl);

        InputStreamVolleyRequest request = new InputStreamVolleyRequest(Request.Method.GET, mUrl,
                new Response.Listener<byte[]>() {
                    @Override
                    public void onResponse(byte[] response) {
                        try {
//                            Log.d("t","objSize:"+response.length);

                            if (response != null) {
                                callback.onSuccess(new String(response));
                            }
                        } catch (Exception e) {
//                            Log.d("KEY_ERROR", "UNABLE TO DOWNLOAD FILE");
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("GAIUS", "ContentBuilderActivity: onErrorResponse from " + mUrl);
                Toast.makeText(getApplicationContext(), "Error Response: " + error, Toast.LENGTH_LONG).show();
                error.printStackTrace();
            }
        }, null);

        mRequestQueue.add(request);
    }

    private void downloadImage(final String mUrl, final String jsonstring, RequestQueue mRequestQueue) {

        final InputStreamVolleyRequest request = new InputStreamVolleyRequest(Request.Method.GET, mUrl,
                new Response.Listener<byte[]>() {
                    @Override
                    public void onResponse(final byte[] response) {

                        try {
                            if (response != null) {
                                Bitmap bmp = BitmapFactory.decodeByteArray(response, 0, response.length);
                                drawImage(jsonstring, bmp);
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

    private void downloadVideoThumbnail(final String mUrl, final String jsonstring, RequestQueue mRequestQueue) {

        final InputStreamVolleyRequest request = new InputStreamVolleyRequest(Request.Method.GET, mUrl,
                new Response.Listener<byte[]>() {
                    @Override
                    public void onResponse(final byte[] response) {

                        try {
                            if (response != null) {
                                Bitmap bmp = BitmapFactory.decodeByteArray(response, 0, response.length);
                                drawVideo(jsonstring, bmp);
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

    private void drawImage(String data, Bitmap img_file) {
        try {
            JSONObject parser = new JSONObject(data);
            int x;
            int y;
            int w;
            int h;
            String path;

            if (data.contains("\"w\":")) {
                x = parser.getInt("x");
                y = parser.getInt("y");
                w = parser.getInt("w");
                h = parser.getInt("h");
                path = parser.getString("url");

                // yasir : rescaling image to the w and h specified
                img_file = Bitmap.createScaledBitmap(img_file, w, h, false);
                addBitmapToLayout(IMAGE_BITMAP, img_file, path, x, y, w, h);

                if (y + h + 50 > insagram_images_y) {
                    insagram_images_y = y + h + 50;
                }

            } else {
                // This is an instagram image we need to read its width and re-scale to fit in screen
                float aspectRatio = (float) img_file.getWidth()/img_file.getHeight();
                w = Math.min (img_file.getWidth(), (int) (0.8 * Resources.getSystem().getDisplayMetrics().widthPixels));
                h = (int) ((float) w/aspectRatio);
                path = parser.getString("url");

                if (insagram_images_y == 0) {
                    insagram_images_y = 150;
                }

                img_file = Bitmap.createScaledBitmap(img_file, w, h, false);
                x = (int) ((Resources.getSystem().getDisplayMetrics().widthPixels - img_file.getWidth())/2.0);
                path = ResourceHelper.saveBitmapCompressed(getApplicationContext(), Uri.parse(path), img_file);
                addBitmapToLayout(IMAGE_BITMAP_SHARED, img_file, path, x, insagram_images_y, w, h);

                insagram_images_y += h + 50;

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void drawRect (String data) {
        try {
            JSONObject parser = new JSONObject(data);
            int x = parser.getInt("x");
            int y = parser.getInt("y");
            int w = parser.getInt("w");
            int h = parser.getInt("h");
            String color = parser.getString("color");

            Layer layer = new Layer();
            RectEntity entity = new RectEntity(layer, motionView.getWidth(), motionView.getHeight(), getApplicationContext(), x, y, w, h);

            entity.changeColor(Color.parseColor(color));

            motionView.addEntityAndPosition(entity);

            if (y + h + 50 > insagram_images_y) {
                insagram_images_y = y + h + 50;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void drawText (String data) {
        try {
            JSONObject parser = new JSONObject(data);
            int x = parser.getInt("x");
            int y = parser.getInt("y");
            int w = parser.getInt("w");
            int h = parser.getInt("h");
            String fontType = parser.getString("font-type");
            int fontSize = parser.getInt("font");
            String color = parser.getString("color");
            String text = parser.getString("txt").replaceAll("<br>", "\n").replaceAll("<doubleQuote>","\"");

            TextLayer textLayer = new TextLayer();
            FontProvider fontProvider = new FontProvider(getResources());

            Font font = new Font();
            font.setColor(Color.parseColor(color));
            font.setSize((float)fontSize/w);
            font.setTypeface(fontType);

            textLayer.setFont(font);
            textLayer.setText(text);

            TextEntity textEntity = new TextEntity(textLayer, motionView.getWidth(), motionView.getHeight(), fontProvider, getApplicationContext(), x,y, w, h);
            motionView.addEntityAndPosition(textEntity);

            if (y + h + 50 > insagram_images_y) {
                insagram_images_y = y + h + 50;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void drawVideo(String data, Bitmap img_file) {
        try {
            JSONObject parser = new JSONObject(data);
            int x = parser.getInt("x");
            int y = parser.getInt("y");
            int w = parser.getInt("w");
            int h = parser.getInt("h");
            String path = parser.getString("url");

            // yasir
            img_file = Bitmap.createScaledBitmap(img_file, w, h, false);

//            addBitmapToLayout(VIDEO_BITMAP, BitmapFactory.decodeResource(getResources(), R.drawable.baseline_music_video_black_48), path, x, y, w, h);
            addBitmapToLayout(VIDEO_BITMAP_THUMB, img_file, path, x, y, w, h);

            if (y + h + 50 > insagram_images_y) {
                insagram_images_y = y + h + 50;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    @SuppressLint("NewApi")
    private void imageBrowse() {
        if (CropImage.isExplicitCameraPermissionRequired(this)) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE);
        } else {
            CropImage.startPickImageActivity(this);
        }
    }

    private void videoBrowse() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("video/*");
        galleryIntent.putExtra(Intent.EXTRA_MIME_TYPES, new String[] { "video/*"});
        startActivityForResult(galleryIntent, PICK_VIDEO_REQUEST);
    }

    //this function is used for editing mode
    private void addBitmapToLayout(final int flag, final Bitmap bmp, final String path, final int x, final int y, final int w, final int h) {
        motionView.post(new Runnable() {
            @Override
            public void run() {
                Layer layer = new Layer();
                layer.setPath(path);
                if (flag == VIDEO_BITMAP) {
                    layer.setIsVideo(true);
                }
                if (flag == VIDEO_BITMAP_THUMB) {
                    layer.setIsVideo(true);
                    layer.videoHasThumbnail(true);
                }
                if (flag == IMAGE_BITMAP_SHARED) {
                    layer.setIsNewContent(true);
                }

                ImageEntity imageEntity = new ImageEntity(layer, bmp, motionView.getWidth(), motionView.getHeight(), getApplicationContext(), x, y);
                motionView.addEntityAndPosition(imageEntity);
                motionView.unselectEntity();
            }
        });
    }

    private void addBitmapToLayout(final int flag, final Bitmap bmp, final String path) {
        motionView.post(new Runnable() {
            @Override
            public void run() {

                ImageEntity selectedEntity = currentImageEntity();

                if (selectedEntity != null) {
                    selectedEntity.updateEntity(bmp, path);
                }
                else {
                    Layer layer = new Layer();
                    layer.setPath(path);
                    layer.setIsNewContent(true);
                    if (flag == VIDEO_BITMAP) {
                        layer.setIsVideo(true);
                    }
                    ImageEntity entity = new ImageEntity(layer, bmp, motionView.getWidth(), motionView.getHeight(), getApplicationContext(), null,customScrollView.getScrollY() + mDisplayMetrics.heightPixels / 2);
                    motionView.addEntityAndPosition(entity);
                }
            }
        });
    }

    private void changeRectEntityColor() {
        final RectEntity rectEntity = currentRectEntity();
        if (rectEntity == null) {
            return;
        }

        ColorPickerDialogBuilder
                .with(CreativeWebCreation.this)
                .setTitle(R.string.select_color)
                .initialColor(rectEntity.getColor())
                .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
                .showAlphaSlider(true)
                .density(8) // magic number
                .setPositiveButton(R.string.ok, new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                        rectEntity.changeColor(selectedColor);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .build()
                .show();
    }

    private void initTextEntitiesListeners() {
        findViewById(R.id.text_entity_font_size_increase).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                increaseTextEntitySize();
            }
        });
        findViewById(R.id.text_entity_font_size_decrease).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                decreaseTextEntitySize();
            }
        });
        findViewById(R.id.text_entity_color_change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeTextEntityColor();
            }
        });
        findViewById(R.id.text_entity_font_change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeTextEntityFont();
            }
        });
        findViewById(R.id.text_entity_edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTextEntityEditing();
            }
        });
    }

    private void initRectEntitiesListeners() {
        findViewById(R.id.rect_entity_width_increase).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                increaseRectEntityWidth();
            }
        });
        findViewById(R.id.rect_entity_width_decrease).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                decreaseRectEntityWidth();
            }
        });
        findViewById(R.id.rect_entity_height_increase).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                increaseRectEntityHeight();
            }
        });
        findViewById(R.id.rect_entity_height_decrease).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                decreaseRectEntityHeight();
            }
        });
        findViewById(R.id.rect_entity_color_change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeRectEntityColor();
            }
        });
    }

    private void increaseRectEntityWidth() {
        RectEntity rectEntity = currentRectEntity();
        if (rectEntity != null) {
            rectEntity.increaseWidth();
            motionView.invalidate();
        }
    }

    private void decreaseRectEntityWidth() {
        RectEntity rectEntity = currentRectEntity();
        if (rectEntity != null) {
            rectEntity.decreaseWidth();
            motionView.invalidate();
        }
    }

    private void increaseRectEntityHeight() {
        RectEntity rectEntity = currentRectEntity();
        if (rectEntity != null) {
            rectEntity.increaseHeight();
            motionView.invalidate();
        }
    }

    private void decreaseRectEntityHeight() {
        RectEntity rectEntity = currentRectEntity();
        if (rectEntity != null) {
            rectEntity.decreaseHeight();
            motionView.invalidate();
        }
    }


    private void increaseTextEntitySize() {
        TextEntity textEntity = currentTextEntity();
        if (textEntity != null) {
            textEntity.getLayer().getFont().increaseSize(TextLayer.Limits.FONT_SIZE_STEP);
            textEntity.updateEntity();
            motionView.invalidate();
        }
    }

    private void decreaseTextEntitySize() {
        TextEntity textEntity = currentTextEntity();
        if (textEntity != null) {
            textEntity.getLayer().getFont().decreaseSize(TextLayer.Limits.FONT_SIZE_STEP);
            textEntity.updateEntity();
            motionView.invalidate();
        }
    }

    private void changeTextEntityColor() {
        TextEntity textEntity = currentTextEntity();
        if (textEntity == null) {
            return;
        }

        int initialColor = textEntity.getLayer().getFont().getColor();

        ColorPickerDialogBuilder
                .with(CreativeWebCreation.this)
                .setTitle(R.string.select_color)
                .initialColor(initialColor)
                .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
                .showAlphaSlider(false)
                .density(8) // magic number
                .setPositiveButton(R.string.ok, new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                        TextEntity textEntity = currentTextEntity();
                        if (textEntity != null) {
                            textEntity.getLayer().getFont().setColor(selectedColor);
                            textEntity.updateEntity();
                            motionView.invalidate();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .build()
                .show();
    }

    private void changeTextEntityFont() {
        final List<String> fonts = fontProvider.getFontNames();
        FontsAdapter fontsAdapter = new FontsAdapter(this, fonts, fontProvider);
        new AlertDialog.Builder(this)
                .setTitle(R.string.select_font)
                .setAdapter(fontsAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        TextEntity textEntity = currentTextEntity();
                        if (textEntity != null) {
                            textEntity.getLayer().getFont().setTypeface(fonts.get(which));
                            textEntity.updateEntity();
                            motionView.invalidate();
                        }
                    }
                })
                .show();
    }

    private void startTextEntityEditing() {
        TextEntity textEntity = currentTextEntity();
        if (textEntity != null) {
            TextEditorDialogFragment fragment = TextEditorDialogFragment.getInstance(textEntity.getLayer().getText());
            fragment.show(getFragmentManager(), TextEditorDialogFragment.class.getName());
        }
    }

    @Nullable
    private TextEntity currentTextEntity() {
        if (motionView != null && motionView.getSelectedEntity() instanceof TextEntity) {
            return ((TextEntity) motionView.getSelectedEntity());
        } else {
            return null;
        }
    }

    @Nullable
    private RectEntity currentRectEntity() {
        if (motionView != null && motionView.getSelectedEntity() instanceof RectEntity) {
            return ((RectEntity) motionView.getSelectedEntity());
        } else {
            return null;
        }
    }

    @Nullable
    private ImageEntity currentImageEntity() {
        if (motionView != null && motionView.getSelectedEntity() instanceof ImageEntity) {
            return ((ImageEntity) motionView.getSelectedEntity());
        } else {
            return null;
        }
    }

    protected void addTextSticker() {
        TextLayer textLayer = createTextLayer();

        TextEntity textEntity = new TextEntity(textLayer, motionView.getWidth(), motionView.getHeight(), fontProvider, getApplicationContext(), null,customScrollView.getScrollY() + mDisplayMetrics.heightPixels/2, motionView.getWidth(), 10);
        motionView.addEntityAndPosition(textEntity);

//        if (txt!=null) {
//            textEntity.setText(txt);
//        }

        // move text sticker up so that its not hidden under keyboard
        PointF center = textEntity.absoluteCenter();
        center.y = center.y * 0.5F + customScrollView.getScrollY() * 0.5F;
        center.x = motionView.getWidth() * 0.5F;
        textEntity.moveCenterTo(center);

        // redraw
        motionView.invalidate();

        startTextEntityEditing();
    }

    private TextLayer createTextLayer() {
        TextLayer textLayer = new TextLayer();
        Font font = new Font();

        font.setColor(TextLayer.Limits.INITIAL_FONT_COLOR);
        font.setSize(TextLayer.Limits.INITIAL_FONT_SIZE);
        font.setTypeface(fontProvider.getDefaultFontName());

        textLayer.setFont(font);

//        if (BuildConfig.DEBUG) {
//            textLayer.setText("Hello, world :))");
//        }

        return textLayer;
    }

    @Override
    public void textChanged(@NonNull String text) {
        TextEntity textEntity = currentTextEntity();
        if (textEntity != null) {
            TextLayer textLayer = textEntity.getLayer();
            if (!text.equals(textLayer.getText())) {
                textLayer.setText(text);
                textEntity.updateEntity();
                motionView.invalidate();
            }
        }
    }

    @Override
    @SuppressLint("NewApi")
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                Bitmap bitmap = BitmapFactory.decodeFile(result.getUri().getPath());

                if (alertDSubmit != null && alertDSubmit.isShowing()) {
                    bitmap = getResizedBitmap(bitmap,200);
                    iconPath = ResourceHelper.saveBitmapCompressed(getApplicationContext(), iconUri, bitmap);
                    iconImageView.setImageBitmap(bitmap);
                }
                else {
                    // yasir resizing the selected image in case its bigger than 80% of the canvas width
                    if (bitmap.getWidth() > (int) (motionView.getWidth()*0.8)) {
                        int width = (int) (motionView.getWidth()*0.8);
                        int height = (int)((float)(width * bitmap.getHeight())/bitmap.getWidth());
                        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
                    }

                    // reset the tmp width and hegiht
                    tmpW = 0;
                    tmpH = 0;

                    addBitmapToLayout(IMAGE_BITMAP, bitmap, result.getUri().getPath());
                }
            }

            if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE ) {
                final Uri imageUri = CropImage.getPickImageResultUri(this, data);

//                // For API >= 23 we need to check specifically that we have permissions to read external storage.
                if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {
                    // request permissions and handle the result in onRequestPermissionsResult()
                    mCropImageUri = imageUri;
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);

                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);

                    builder.setTitle("Cropping");
                    builder.setMessage("Do you want to crop the image?");

                    builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            // Do nothing but close the dialog
                            dialog.dismiss();

                            // no permissions required or already granted, can start crop image activity
                            startCropImageActivity(imageUri, false);
                        }
                    });

                    builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Do nothing
                            dialog.dismiss();

                            Bitmap bitmap = null;
                            try {
                                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver() , imageUri);
                                bitmap = getResizedBitmap(bitmap,800);

                                // yasir resizing the selected image in case its bigger than 80% of the canvas width
                                if (bitmap.getWidth() > (int) (motionView.getWidth()*0.8)) {
                                    int width = (int) (motionView.getWidth()*0.8);
                                    int height = (int)((float)(width * bitmap.getHeight())/bitmap.getWidth());
                                    bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
                                }

                                // reset the tmp width and hegiht
                                tmpW = 0;
                                tmpH = 0;

                                addBitmapToLayout(IMAGE_BITMAP, bitmap, ResourceHelper.saveBitmapCompressed(getApplicationContext(), imageUri, bitmap));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }


            if (requestCode == PICK_VIDEO_REQUEST) {
                Uri fileUri = data.getData();
                String[] videoFile = getVideoPath(fileUri);
                String filePath = videoFile[0];
                Bitmap selectedImage = ThumbnailUtils.createVideoThumbnail(videoFile[0], MediaStore.Images.Thumbnails.MINI_KIND);
                long fileSize = Long.parseLong(videoFile[1])/1024/1024;
                if ( fileSize > 5) {
                    Toast.makeText (getApplicationContext(), "Video size is larger than " + fileSize +" MB. Consider uploading a smaller video!", Toast.LENGTH_SHORT).show ();
                }
                addBitmapToLayout(VIDEO_BITMAP, selectedImage, filePath);
            }

            if (requestCode == PICK_ICON_REQUEST) {
                iconUri = data.getData();

                Uri imageUri = CropImage.getPickImageResultUri(this, data);

//                // For API >= 23 we need to check specifically that we have permissions to read external storage.
                if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {
                    // request permissions and handle the result in onRequestPermissionsResult()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
                    }

                } else {
                    // no permissions required or already granted, can start crop image activity
                    startCropImageActivity(imageUri, true);
                }
            }
        }

        if (requestCode == REQUEST_CODE_INSTAGRAM_PICKER) {
            if (resultCode == CreativeWebCreation.RESULT_OK) {
                InstagramPhoto[] instagramPhotos = InstagramPhotoPicker.getResultPhotos(data);

                for (int i = 0; i < instagramPhotos.length; ++i) {
                    String dataImg = "{\"type\":\"img\",\"url\":\"" + instagramPhotos[i].getFullURL().toString() + "\"}";
                    downloadImage(instagramPhotos[i].getFullURL().toString(), dataImg, mRequestQueue);

                }
            }
        }
    }

    private void startCropImageActivity(Uri imageUri, Boolean isIcon) {
        if (isIcon) {
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setFixAspectRatio(true)
                    .start(this);
        }
        else if (tmpW == 0 && tmpH == 0) {
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this);
        }
        else{
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(tmpW,tmpH)
                    .start(this);
        }
    }

    public boolean onCreateOptionsMenu (Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.content_creator, menu);
        return true;
    }

    public void submitPage(boolean publish) {

        MamlPageBuilder builder = new MamlPageBuilder();

        imagePaths = new ArrayList<String>();
        videoPaths = new ArrayList<String>();

        int color = Color.WHITE;
        Drawable background = motionView.getBackground();
        if (background instanceof ColorDrawable) {
            color = ((ColorDrawable) background).getColor();
        }

        if (bgUrl == null) {
            // adding the background color of maml
            builder.addBackground (String.format("#%06X", (0xFFFFFF & color)));
        }
        else{
            builder.addBackground (String.format("#%06X", (0xFFFFFF & color)), bgUrl);
        }

        for (MotionEntity mE : motionView.getEntities()) {
            PointF coord, coord2;
            coord = mE.getUpperLeftCoordinates();
            coord2 = mE.getLowerRightCoordinates();

            if (mE instanceof TextEntity) {
                builder.addText(((TextEntity) mE).getText(), ((TextEntity) mE).getFont(), ((TextEntity) mE).getFontSize() , (int)coord.x, (int)coord.y, (int) (coord2.x - coord.x), (int) (coord2.y - coord.y), ((TextEntity) mE).getFontColor());
            } else if (mE instanceof ImageEntity) {
                String[] filename;
                filename = new File("" + Uri.parse(mE.getLayer().getPath())).getName().split("/");
                if (mE.getLayer().isVideo()) {
                    if (!mE.getLayer().isNewContent()) {
                        builder.addVideo(mE.getLayer().getPath(), (int)coord.x, (int)coord.y, (int) (coord2.x - coord.x), (int) (coord2.y - coord.y));
                    } else {
                        videoPaths.add(mE.getLayer().getPath());
                        builder.addVideo(filename[filename.length - 1], (int)coord.x, (int)coord.y, (int) (coord2.x - coord.x), (int) (coord2.y - coord.y));
                    }
                } else {
                    if (mE.getLayer().getPath() != "") {
                        if (!mE.getLayer().isNewContent()) {
                            builder.addImage(mE.getLayer().getPath(), (int) coord.x, (int) coord.y, (int) (coord2.x - coord.x), (int) (coord2.y - coord.y));
                        } else {
                            imagePaths.add(mE.getLayer().getPath());
                            builder.addImage(filename[filename.length - 1], (int) coord.x, (int) coord.y, (int) (coord2.x - coord.x), (int) (coord2.y - coord.y));
                        }
                    }
                }
            } else if (mE instanceof  RectEntity) {
                builder.addRect( (int)coord.x, (int)coord.y, (int) (coord2.x - coord.x), (int) (coord2.y - coord.y), ((RectEntity) mE).getColor());
            }
        }

        if (builder.getObjectCount() > 0) {
            Log.d("GAIUS", "ContentBuilderActivity " + builder.getPageAsString());
            mamlFilePath = builder.makeFile(Constants.TEMPDIR);
            uploadMultipart(publish);
        } else {
            Toast.makeText (getApplicationContext(), "No content added", Toast.LENGTH_SHORT).show ();
        }
    }

    public boolean filledFields(View view) {
        boolean haveContent = true;
        EditText editTextPagename = view.findViewById(R.id.title_edittext);
        EditText editTextDescription = view.findViewById(R.id.description_edittext);
        TextInputLayout editTextPagenameLayout = view.findViewById(R.id.title_edittext_layout) ;
        TextInputLayout editTextDescriptionLayout = view.findViewById(R.id.description_edittext_layout);

        if (editTextDescription.getText().length() == 0) {
            editTextDescriptionLayout.setError(getString(R.string.error_description));
            haveContent = false;
        } else {
            editTextDescriptionLayout.setErrorEnabled(false);
        }
        if (editTextPagename.getText().length() == 0) {
            editTextPagenameLayout.setError(getString(R.string.error_name));
            haveContent = false;
        } else {
            editTextPagenameLayout.setErrorEnabled(false);
        }

        if (!haveContent) {
            return false;
        }

        pageName = editTextPagename.getText().toString();
        pageDescription = editTextDescription.getText().toString();
        return true;
    }

    public boolean onOptionsItemSelected (MenuItem item)
    {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        final View promptView = layoutInflater.inflate(R.layout.submit_content_popup, null);

        if (EDIT_MODE) {
            EditText editTextPagename = promptView.findViewById(R.id.title_edittext);
            EditText editTextDescription = promptView.findViewById(R.id.description_edittext);
            editTextPagename.setText(pageName);
            editTextDescription.setText(pageDescription);
        }

        alertDSubmit = new AlertDialog.Builder(this).create();
        alertDSubmit.setView(promptView);
        alertDSubmit.show();

        Button cancel_button = (Button) promptView.findViewById(R.id.cancel_button);
        Button save_button = (Button) promptView.findViewById(R.id.save_button);
        Button publish_button = (Button) promptView.findViewById(R.id.publish_button);
        iconImageView = promptView.findViewById(R.id.logo);

        if (pageIcon != null) {
            iconImageView.setImageBitmap(pageIcon);
        }

        iconImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, PICK_ICON_REQUEST);
            }
        });

        cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDSubmit.dismiss();
            }
        });

        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (filledFields(promptView)) {
                    alertDSubmit.hide();
                    submitPage(false);
                }
            }
        });

        publish_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (filledFields(promptView)) {
                    alertDSubmit.hide();
                    submitPage(true);
                }
            }
        });

        return false;
    }

    private void uploadMultipart(boolean publish) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        progress = new ProgressDialog(this);
        progress.setMessage("Uploading...");
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setCancelable(false);
        progress.setProgress(0);
        progress.setButton(ProgressDialog.BUTTON_NEUTRAL, "Cancel upload",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        AndroidNetworking.cancelAll();
                    }
                });
        progress.show();

        ANRequest.MultiPartBuilder multiPartBuilder = new ANRequest.MultiPartBuilder(prefs.getString("base_url", null) + "upload.py");

        multiPartBuilder.addMultipartParameter("token", prefs.getString("token", "null"));
        multiPartBuilder.addMultipartParameter("title", pageName);
        multiPartBuilder.addMultipartParameter("description", pageDescription);
        multiPartBuilder.addMultipartParameter("resolution", "" + ResourceHelper.getScreenWidth(this));
        multiPartBuilder.addMultipartParameter("category", "100");
        multiPartBuilder.addMultipartFile("maml", new File(mamlFilePath));

        for (String imagePath : imagePaths) {
            if (imagePath != null) {
                String imagePathNew = ResourceHelper.compressImage(this, imagePath, 768, 1024);
                multiPartBuilder.addMultipartFile("images", new File(imagePathNew));
            }
        }

        for (String videoPath : videoPaths) {
            if (videoPath != null) {
                //TODO: add video compression
                multiPartBuilder.addMultipartFile("videos", new File(videoPath));
            }
        }
        if (iconPath != null && !iconPath.equals("None")  && !EDIT_MODE) {
            String iconPathNew = ResourceHelper.compressImage(this, iconPath, 200, 200);
            multiPartBuilder.addMultipartFile("icon", new File(iconPathNew));
        }

        if (EDIT_MODE) {
            multiPartBuilder.addMultipartParameter("path", currentPageUrl);
        }

        if (publish) {
            multiPartBuilder.addMultipartParameter("publish", "-1");
        } else {
            multiPartBuilder.addMultipartParameter("publish", "0");
        }

        multiPartBuilder.build()
                .setUploadProgressListener(new UploadProgressListener() {
                    @Override
                    public void onProgress(long bytesUploaded, long totalBytes) {
                        progress.setProgress((int)((float)bytesUploaded/totalBytes * 100.0));
                    }
                })
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
                .getAsOkHttpResponse(new OkHttpResponseListener() {
                    @Override
                    public void onResponse(okhttp3.Response response) {
                        Log.d("thp", "OnResponse " + response.code() + " " + response.toString());
                        if (response.code() == 200) {
                            progress.dismiss();
                            alertDSubmit.dismiss();
                            uploadSuccessful();
                        } else {
                            Toast.makeText(getApplicationContext(), "Something went wrong with the upload ("+ response.code()+")", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Toast.makeText(getApplicationContext(), "Something went wrong with the upload ("+ anError.getErrorDetail()+")", Toast.LENGTH_LONG).show();
                        progress.dismiss();
                        alertDSubmit.show();
                    }
                });

    }

//    private void uploadMultipart(final Context context, final ArrayList<String> imagePaths, final ArrayList<String> videoPaths, final String mamlFilePath, boolean publish) {
//
//        progressDialog.setMessage(getString(R.string.dialog_processing));
//        showDialog();
//
//        String uploadId = UUID.randomUUID().toString();
//        try {
//            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
//            MultipartUploadRequest request = new MultipartUploadRequest(context, uploadId, BASE_URL + "upload.py")
//                    .addParameter("token", prefs.getString("token", "null"))
//                    .addParameter("resolution", ""+ ResourceHelper.getScreenWidth(this))
//                    .setUtf8Charset()
//                    .setNotificationConfig(getNotificationConfig(uploadId, R.string.notification_title))
//                    .setMaxRetries(5)
//                    .addFileToUpload(mamlFilePath, "maml")
//                    .setDelegate(new UploadStatusDelegate() {
//                        @Override
//                        public void onProgress(Context context, UploadInfo uploadInfo) {
//                            // your code here
//                        }
//
//                        @Override
//                        public void onError(Context context, UploadInfo uploadInfo, ServerResponse serverResponse, Exception exception) {
//                            try {
//                                Log.d("GAIUS", "ContentBuilderActivity: onError"+serverResponse.getHeaders() + serverResponse.getBodyAsString() + serverResponse.getHttpCode());
//                                Toast.makeText(getApplicationContext(), "Something went wrong with the upload ("+ serverResponse.getHttpCode()+")", Toast.LENGTH_LONG).show();
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        }
//
//                        @Override
//                        public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
//                            // remove the notification as it is no longer needed to keep the service alive
//                            if (uploadInfo.getNotificationID() != null) {
//                                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//                                notificationManager.cancel(uploadInfo.getNotificationID());
//                            }
//                            Log.d("GAIUS", "ContentBuilderActivity: onCompleted response "+serverResponse.getBodyAsString());
//                            if (serverResponse.getBodyAsString().contains("@@ERROR##"))  {
//                                Toast.makeText(getApplicationContext(), serverResponse.getBodyAsString().replace("@@ERROR##","ERROR:").trim(), Toast.LENGTH_LONG).show();
//                            } else {
//                                uploadSuccessful();
//                            }
//                            hideDialog();
//                        }
//
//                        @Override
//                        public void onCancelled(Context context, UploadInfo uploadInfo) {
//                            hideDialog();
//                        }
//                    });
//
//            for (String imagePath: imagePaths) {
//                if (imagePath != null) {
////                    Log.d("thp", "path to image " + imagePath);
//                    String imagePathNew = ResourceHelper.compressImage(context, imagePath, 768, 1024);
//                    request.addFileToUpload(imagePathNew, "images");
//                }
//            }
//
//            for (String videoPath: videoPaths) {
//                if (videoPath != null) {
//                    //TODO: add video compression
//                    request.addFileToUpload(videoPath, "videos");
//                }
//            }
//            if (iconPath != null && !iconPath.equals("None") && !EDIT_MODE) {
//                String iconPathNew = ResourceHelper.compressImage(context, iconPath, 200, 200);
//                request.addFileToUpload(iconPathNew, "icon");
//            }
//
//            if (EDIT_MODE) {
//                request.addParameter("path", currentPageUrl);
//            }
//
//            request.addParameter("title", pageName);
//            request.addParameter("description", pageDescription);
//            request.addParameter("category", "100");
//
//            if (publish) {
//                request.addParameter("publish", "-1");
//            } else {
//                request.addParameter("publish", "0");
//            }
//
//            request.startUpload();
//
//            Log.d("GAIUS", " ContentBuilderActivity request " + uploadId + " " + pageName);
//        } catch (Exception exc) {
//            Log.d("GAIUS", exc.getMessage(), exc);
//        }
//    }

//    private void showDialog() {
//        if (!progressDialog.isShowing())
//            progressDialog.show();
//    }
//
//    private void hideDialog() {
//        if (progressDialog.isShowing())
//            progressDialog.dismiss();
//    }

//    private UploadNotificationConfig getNotificationConfig(final String uploadId, @StringRes int title) {
//        UploadNotificationConfig config = new UploadNotificationConfig();
//
//        PendingIntent clickIntent = PendingIntent.getActivity(
//                this, 1, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT); //fixme
//
//        config.setTitleForAllStatuses(getString(title))
//                .setRingToneEnabled(false)
//                .setClickIntentForAllStatuses(clickIntent)
//                .setClearOnActionForAllStatuses(true);
//
//        config.getProgress().message = "Uploaded " + UPLOADED_FILES + " of " + TOTAL_FILES
//                + " at " + UPLOAD_RATE + " - " + PROGRESS;
//        config.getProgress().iconResourceID = R.drawable.ic_upload;
//        config.getProgress().iconColorResourceID = Color.BLUE;
//
//        config.getCompleted().message = "Upload completed successfully in " + ELAPSED_TIME;
//        config.getCompleted().iconResourceID = R.drawable.ic_upload_success;
//        config.getCompleted().iconColorResourceID = Color.GREEN;
//
//        config.getError().message = "Error while uploading";
//        config.getError().iconResourceID = R.drawable.ic_upload_error;
//        config.getError().iconColorResourceID = Color.RED;
//
//        config.getCancelled().message = "Upload has been cancelled";
//        config.getCancelled().iconResourceID = R.drawable.ic_cancelled;
//        config.getCancelled().iconColorResourceID = Color.YELLOW;
//
//        return config;
//    }

    private void uploadSuccessful() {
        Log.d("thp", "upload successful");

        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Upload successful");
        alertDialog.setMessage("Your page has been successfully submitted. Someone from our team will approve it shortly.");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });
        alertDialog.show();
    }

    private boolean isReadStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
//                Log.d("Permission","Permission is granted");
                return true;
            } else {
//                Log.d("Permission","Permission is revoked1");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSIONS_REQUEST_CODE);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
//            Log.d("Permission","Permission is granted");
            return true;
        }
    }

    public  boolean isWriteStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
//                Log.d("thp","Write Permission is granted2");
                return true;
            } else {

//                Log.d("thp","Permission is revoked2");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
//            Log.d("thp","Write Permission is granted20");
            return true;
        }
    }

    private String[] getVideoPath(Uri contentUri) {
        String[] proj = {MediaStore.Video.Media.DATA};
        CursorLoader loader = new CursorLoader(getApplicationContext(), contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
        cursor.moveToFirst();
        String[] result = new String[2];
        result[0] = cursor.getString(column_index);


        String[] projSize = {MediaStore.Video.Media.SIZE};
        loader = new CursorLoader(getApplicationContext(), contentUri, projSize, null, null, null);
        cursor = loader.loadInBackground();
        cursor.moveToFirst();

        int sizeColInd = cursor.getColumnIndex(projSize[0]);
        result[1] = ""+cursor.getLong(sizeColInd);
        cursor.close();
        return result;
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, R.string.toast_back, Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    public String loadJSONFromAsset(String file) {
        String json = null;
        try {
            InputStream is = this.getAssets().open(file);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    private void downloadBg (final String mUrl, RequestQueue mRequestQueue) {

        final InputStreamVolleyRequest request = new InputStreamVolleyRequest(Request.Method.GET, mUrl,
                new Response.Listener<byte[]>() {
                    @Override
                    public void onResponse(final byte[] response) {

                        try {
                            if (response != null) {
                                bgUrl = mUrl;
                                Bitmap bgBitmap = BitmapFactory.decodeByteArray(response, 0, response.length);
                                Drawable bgDrawable = new BitmapDrawable(getResources(), bgBitmap);
                                customScrollView.setBackground(bgDrawable);
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
}