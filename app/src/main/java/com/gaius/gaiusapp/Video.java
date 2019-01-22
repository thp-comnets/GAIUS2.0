package com.gaius.gaiusapp;

public class Video {
    private int id, views;
    private String title, description, url, avatar, thumbnail, userID, uploadedSince;

    public Video (int id, String title, String description, String url, String avatar, String thumbnail, String userID, String uploadedSince, int views) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.url = url;
        this.avatar = "http://91.230.41.34:8080/test/"+avatar;
        this.thumbnail = "http://91.230.41.34:8080/test/"+thumbnail;
        this.userID = userID;
        this.uploadedSince = uploadedSince;
        this.views = views;
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
}
