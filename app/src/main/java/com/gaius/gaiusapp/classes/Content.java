package com.gaius.gaiusapp.classes;

public class Content {
    int id;
    String title;
    String url;
    String uploadTime;
    String type;
    String description;
    String thumbnail;
    String published;
    String name;
    String textViewed, textClicked, imageViewed, imageClicked, videoViewed, videoClicked;
    String adCampaign;


    public Content(int id, String title, String url, String uploadTime, String type, String description, String thumbnail, String published) {
        this.id = id;
        this.title = title;
        this.url = url;
        this.uploadTime = uploadTime;
        this.type = type;
        this.description = description;
        this.thumbnail = thumbnail;
        this.published = published;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(String uploadTime) {
        this.uploadTime = uploadTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getPublished() {
        return published;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTextViewed() {
        return textViewed;
    }

    public void setTextViewed(String textViewed) {
        this.textViewed = textViewed;
    }

    public String getTextClicked() {
        return textClicked;
    }

    public void setTextClicked(String textClicked) {
        this.textClicked = textClicked;
    }

    public String getImageViewed() {
        return imageViewed;
    }

    public void setImageViewed(String imageViewed) {
        this.imageViewed = imageViewed;
    }

    public String getImageClicked() {
        return imageClicked;
    }

    public void setImageClicked(String imageClicked) {
        this.imageClicked = imageClicked;
    }

    public String getVideoViewed() {
        return videoViewed;
    }

    public void setVideoViewed(String videoViewed) {
        this.videoViewed = videoViewed;
    }

    public String getVideoClicked() {
        return videoClicked;
    }

    public void setVideoClicked(String videoClicked) {
        this.videoClicked = videoClicked;
    }

    public String getAdCampaign() {
        return adCampaign;
    }

    public void setAdCampaign(String adCampaign) {
        this.adCampaign = adCampaign;
    }
}