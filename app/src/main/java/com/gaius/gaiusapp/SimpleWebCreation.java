package com.gaius.gaiusapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.gaius.gaiusapp.adapters.ItemsAdapter;
import com.gaius.gaiusapp.classes.Item;
import com.gaius.gaiusapp.helper.OnStartDragListener;
import com.gaius.gaiusapp.helper.SimpleItemTouchHelperCallback;
import com.gaius.gaiusapp.utils.MamlPageBuilder;
import com.gaius.gaiusapp.utils.ResourceHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


class SimpleWebCreation extends AppCompatActivity implements View.OnClickListener, OnStartDragListener {
    List<Item> itemList;
    RecyclerView recyclerView;
    CardView imageButton, videoButton, textHeaderButton, textParagrahButton;
    ItemsAdapter adapter;
    private String pageName;
    private String pageDescription;
    private final int PICK_ICON_REQUEST = 0;
    private final int PICK_IMAGE_REQUEST = 1;
    private final int PICK_VIDEO_REQUEST = 2;
    private ItemTouchHelper mItemTouchHelper;
    private ImageView iconImageView;
    private Uri iconUri;
    private String iconPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_content_creation);

        recyclerView = findViewById(R.id.simple_recylcerView);
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        itemList = new ArrayList<>();

        //adding the product to product list
        itemList.add(new Item (0,"text", "paragraph",null, null));


        textHeaderButton = findViewById(R.id.text_header_card);
        textHeaderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("yasir","adding text at "+itemList.size());

                itemList.add(itemList.size(), new Item (itemList.size(), "text","header",null, null));
                adapter.notifyItemInserted(itemList.size()-1);
                recyclerView.scrollToPosition(itemList.size()-1);
//                recyclerView.invalidate();
            }
        });

        textParagrahButton = findViewById(R.id.text_paragraph_card);
        textParagrahButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("yasir","adding text2 at "+itemList.size());

                itemList.add(itemList.size(), new Item (itemList.size(), "text","paragraph",null, null));
                adapter.notifyItemInserted(itemList.size()-1);
                recyclerView.scrollToPosition(itemList.size()-1);
//                recyclerView.invalidate();
            }
        });

        imageButton = findViewById(R.id.image_card);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
            }
        });

        videoButton = findViewById(R.id.video_card);
        videoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                galleryIntent.setType("video/*");
                galleryIntent.putExtra(Intent.EXTRA_MIME_TYPES, new String[] { "video/*"});
                startActivityForResult(galleryIntent, PICK_VIDEO_REQUEST);
            }
        });

        //creating adapter object and setting it to recyclerview
        adapter = new ItemsAdapter(this, itemList, this);
        recyclerView.setAdapter(adapter);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    @SuppressLint("NewApi")
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("yasir", "Upload video2 ");

        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST) {
                Uri imageUri = data.getData();
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String imagePath = ResourceHelper.saveBitmapCompressed(getApplicationContext(), imageUri, bitmap);

                itemList.add(itemList.size(), new Item(itemList.size(), "image", null, imagePath, null));
                adapter.notifyItemInserted(itemList.size() - 1);
                recyclerView.scrollToPosition(itemList.size() - 1);
                recyclerView.invalidate();

            } else if (requestCode == PICK_VIDEO_REQUEST) {
                Uri fileUri = data.getData();
                String[] videoFile = getVideoPath(fileUri);
                final String filePath = videoFile[0];

                Log.d("yasir", "video: "+filePath);

                long fileSize = Long.parseLong(videoFile[1]) / 1024 / 1024;
                if (fileSize > 5) {
                    Toast.makeText(this, "Video size is larger than " + fileSize + " MB. Consider uploading a smaller video!", Toast.LENGTH_SHORT).show();
                }

                itemList.add(itemList.size(), new Item(itemList.size(), "video", null, null, filePath));
                adapter.notifyItemInserted(itemList.size() - 1);
                recyclerView.scrollToPosition(itemList.size() - 1);
                recyclerView.invalidate();
            }
        }
    }

    private String[] getVideoPath(Uri contentUri) {
        String[] proj = {MediaStore.Video.Media.DATA};
        CursorLoader loader = new CursorLoader(this, contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
        cursor.moveToFirst();
        String[] result = new String[2];
        result[0] = cursor.getString(column_index);


        String[] projSize = {MediaStore.Video.Media.SIZE};
        loader = new CursorLoader(this, contentUri, projSize, null, null, null);
        cursor = loader.loadInBackground();
        cursor.moveToFirst();

        int sizeColInd = cursor.getColumnIndex(projSize[0]);
        result[1] = ""+cursor.getLong(sizeColInd);
        cursor.close();
        return result;
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public void onClick(View view) {
        int id = (Integer) view.getTag();

        recyclerView.invalidate();
        Log.d("yasir","\n\ntotal children count: "+recyclerView.getChildCount());

        for (int childCount = recyclerView.getChildCount(), i = 0; i < childCount; ++i) {
            final ItemsAdapter.ItemViewHolder holder = (ItemsAdapter.ItemViewHolder) recyclerView.getChildViewHolder(recyclerView.getChildAt(i));

            int itemID = (Integer) holder.deleteButton.getTag();

            Log.d("yasir","item number: "+itemID);

            for (int j=0; j<itemList.size(); j++) {
                if (itemList.get(j).getId() == itemID) {
                    itemList.get(i).setText(holder.editText.getText() + "");
                    Log.d("yasir","setting text: "+holder.editText.getText());
                    break;
                }
            }
        }

        for (int i=0; i<itemList.size(); i++) {
            if (itemList.get(i).getId() == id ) {
                Item item = itemList.get(i);
                itemList.remove(item);
                adapter.notifyItemRemoved(i);
                recyclerView.setAdapter(adapter);
                return;
            }
        }

    }

    public boolean onCreateOptionsMenu (Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.content_creator, menu);
        return true;
    }

    public boolean onOptionsItemSelected (MenuItem item)
    {
        recyclerView.clearFocus(); //clear focus to make sure text is saved
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        final View promptView = layoutInflater.inflate(R.layout.submit_content_popup, null);
        final AlertDialog alertD = new AlertDialog.Builder(this).create();
        alertD.setView(promptView);
        alertD.show();

        Button cancel_button = (Button) promptView.findViewById(R.id.cancel_button);
        Button save_button = (Button) promptView.findViewById(R.id.save_button);
        Button publish_button = (Button) promptView.findViewById(R.id.publish_button);
        iconImageView = promptView.findViewById(R.id.logo);

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
                alertD.dismiss();
            }
        });

        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (filledFields(promptView)) {
                    alertD.dismiss();
                    submitPage(false);
                }
            }
        });

        publish_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (filledFields(promptView)) {
                    alertD.dismiss();
                    submitPage(true);
                }
            }
        });

        return false;
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

    public void submitPage(boolean publish) {

        MamlPageBuilder builder = new MamlPageBuilder();

        ArrayList<String> imagePaths = new ArrayList<String>();
        ArrayList<String> videoPaths = new ArrayList<String>();

        builder.addBackground (String.format("#%06X", (0xFFFFFF & Color.WHITE)));
        Log.d("save", adapter.getItemCount()+"");
        String[] filename;
        int yPos = 0;
        for (int i = 0; i < adapter.getItemCount(); i++) {
            Item item = adapter.getItem(i);
            Log.d("save", i+ " "+ adapter.getItem(i).getType()+" "+adapter.getItem(i).getW()+ " "+adapter.getItem(i).getH());
            switch (item.getType()) {
                case "text":
                    builder.addText(item.getText(), "Arial", item.getFontSize(), item.getX(), yPos, item.getW(), item.getH(), "#ffffff");
                    break;
                case "image":
                    filename = new File("" + Uri.parse(item.getImagePath())).getName().split("/");
                    imagePaths.add(item.getImagePath());
                    builder.addImage(filename[filename.length - 1], item.getX(),yPos, item.getW(), item.getH());

                    break;
                case "video":
                    filename = new File("" + Uri.parse(item.getVideoPath())).getName().split("/");
                    videoPaths.add(item.getVideoPath());
                    builder.addVideo(filename[filename.length - 1], item.getX(), yPos, item.getW(), item.getH());
                    break;
            }
            yPos += item.getH();
        }
//            final ItemsAdapter.ItemViewHolder holder = (ItemsAdapter.ItemViewHolder) recyclerView.getChildViewHolder(recyclerView.getChildAt(0));
//
////            int itemID = (Integer) holder.deleteButton.getTag();
////            for (int j = 0; j < itemList.size(); j++) {
////                if (itemList.get(j).getId() == itemID) {
////                    Item item = itemList.get(i);
//
//                    String[] filename;
//                    int [] pos = new int[2];
//                    int  itemWidth, itemHeight=0;
//
//            Log.d("save","type: " + holder.type);
//
//                    switch (holder.type) {
//                        case "text":
//                            holder.itemView.getLocationInWindow(pos);
//                            itemWidth = holder.editText.getWidth();
//                            itemHeight = holder.editText.getHeight();
//
//                            Log.d("GAIUS", holder.getLayoutPosition()+"");
//
//                            int fontSize = 20;
//                            if (holder.textType.contains("header")) {
//                                fontSize = 30;
//                            }
//                            builder.addText(holder.editText.getText().toString(), "Arial", fontSize , pos[0], pos[1], itemWidth, itemHeight, "#ffffff");
//                            break;
//
//                        case "video":
//                            holder.itemView.getLocationInWindow(pos);
//                            itemWidth = holder.videoView.getWidth();
//                            itemHeight = holder.videoView.getHeight();
//
////                            filename = new File("" + Uri.parse(item.getVideoPath())).getName().split("/");
////                            videoPaths.add(item.getVideoPath());
////                            builder.addVideo(filename[filename.length - 1], pos[0], pos[1], itemWidth, itemHeight);
//                            break;
//
//                        case "image":
//                            holder.itemView.getLocationInWindow(pos);
//                            itemWidth = holder.imageView.getWidth();
//                            itemHeight = holder.imageView.getHeight();
////                            filename = new File("" + Uri.parse(item.getImagePath())).getName().split("/");
////                            imagePaths.add(item.getImagePath());
////                            builder.addImage(filename[filename.length - 1], pos[0], pos[1], itemWidth, itemHeight);
//                            builder.addImage("xx", pos[0], pos[1], itemWidth, itemHeight);
////                            break;
////                            break;
//                    }
//            Log.d("save","moving: " + itemHeight);
//
//                    recyclerView.scrollBy(0,itemHeight);
////                    break;
////                }
//            }
////        }
//
        if (builder.getObjectCount() > 0) {
            Log.d("save", "ContentBuilderActivity " + builder.getPageAsString());
//            uploadMultipart(getApplicationContext(), imagePaths, videoPaths, builder.makeFile(Constants.TEMPDIR), publish);
        } else {
            Toast.makeText (getApplicationContext(), "No content added", Toast.LENGTH_SHORT).show ();
        }
    }
}

