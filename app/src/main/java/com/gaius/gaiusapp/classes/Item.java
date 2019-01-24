package com.gaius.gaiusapp.classes;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.view.View;

public class Item {

    private int id;
    private View view;
    private int x;
    private int w;
    private int h;
    private String type;
    private String text, textType;
    private int fontSize;
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

    public void setId(int id) {
        this.id = id;
    }

    public View getView() {
        return view;
    }

    public void setView(View v) {
        this.view = v;
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


    public int getX() {
        return x;
    }

    public int getW() {
        return w;
    }

    public int getH() {
        return h;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setW(int w) {
        this.w = w;
    }

    public void setH(int h) {
        this.h = h;
    }


    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }
}
