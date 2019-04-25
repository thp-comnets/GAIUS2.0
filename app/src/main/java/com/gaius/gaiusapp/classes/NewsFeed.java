package com.gaius.gaiusapp.classes;

import java.util.ArrayList;

public class NewsFeed {
    private int id;
    private String name;
    private String updateTime;
    private String avatar;
    private String image;
    private String title;
    private String description;
    private String url;
    private String userID;
    private String type;
    private Boolean showAvatar;
    private String published;
    private ArrayList<String> imagesGallery;
    private String liked;

    public NewsFeed (int id, String name, String updateTime, String avatar, String image, String title, String description, String url, String userID, String type, String liked, String published, Boolean showAvatar, ArrayList<String> imagesGallery) {
        this.id = id;
        this.name = name;
        this.updateTime = updateTime;
        this.avatar = avatar;
        this.image = image;
        this.title = title;
        this.description = description;
        this.url = url;
        this.userID = userID;
        this.type = type;
        this.showAvatar = showAvatar;
        this.published = published;
        this.liked = liked;
        this.imagesGallery = imagesGallery;
    }


    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getImage() {
        return image;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {return url; }

    public String getUserID() { return userID; }

    public void setUserID(String userID) { this.userID = userID; }

    public String getType() {return type; }

    public String getPublished() {
        return published;
    }

    public void setPublished(String published) {
        this.published = published;
    }


    public Boolean getShowAvatar() {
        return showAvatar;
    }

    public String getLiked() {
        return liked;
    }

    public ArrayList<String> getImagesGallery() {
        return imagesGallery;
    }
}
