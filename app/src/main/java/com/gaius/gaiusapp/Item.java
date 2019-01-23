package com.gaius.gaiusapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;

public class Item {
    private int id;
    private String type;
    private String text, textType;
    private Bitmap imageBitmap;
    private String imagePath;
    private Bitmap videoBitmap;
    private String videoPath;


    public Item(int id, String type, String textType, String imageUrl, String videoUrl) {
        this.id = id;
        this.type = type;
        this.text = "";
        this.textType = textType;
        this.imagePath = imageUrl;
        this.videoPath = videoUrl;
        this.imageBitmap = BitmapFactory.decodeFile(this.imagePath);
        this.videoBitmap = ThumbnailUtils.createVideoThumbnail(this.videoPath, MediaStore.Images.Thumbnails.FULL_SCREEN_KIND);
    }

    public int getId() {
        return id;
    }

    public String getType() { return type; }

    public String getText() { return text; }

    public String getTextType() { return textType; }

    public void setText(String text) { this.text=text; }

    public String getImagePath() { return imagePath; }

    public String getVideoPath() { return videoPath; }

    public Bitmap getVideoBitmap() {
        return videoBitmap;
    }

    public Bitmap getImageBitmap() {
        return imageBitmap;
    }
}
