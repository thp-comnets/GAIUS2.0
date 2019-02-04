package com.gaius.gaiusapp.classes;

import java.util.ArrayList;

public class Image {
    private int id, views;
    private String title, description, url, avatar, thumbnail, userID, uploadedSince;
    private ArrayList<String> imagesGallery;

    public Image (int id, String title, String description, String url, String avatar, String thumbnail, String userID, String uploadedSince, int views, ArrayList<String> imagesGallery) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.url = url;
        this.avatar = avatar;
        this.thumbnail = thumbnail;
        this.userID = userID;
        this.uploadedSince = uploadedSince;
        this.views = views;
        this.imagesGallery = imagesGallery;
    }

    public int getId() {
        return id;
    }

    public int getViews() { return views; }

    public String getTitle() {
        return title;
    }

    public String getDescription() { return description; }

    public String getUrl() {
        return url;
    }

    public String getAvatar() { return avatar; }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getUserID() { return userID; }

    public String getUploadedSince() { return uploadedSince; }

    public ArrayList<String> getImagesGallery() {
        return imagesGallery;
    }
}
