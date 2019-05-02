package com.gaius.gaiusapp.classes;

public class Friend {
    private int id;
    private String name;
    private String phoneNumber;
    private String image;
    private String userID;
    private Integer friendStatus;
    private Boolean clickable;

    public Friend(int id, String name, String phoneNumber, String image, String userID, Integer buttonType, Boolean clickable) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.image = image;
        this.userID = userID;
        this.friendStatus = buttonType;
        this.clickable = clickable;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getImage() {
        return image;
    }

    public String getUserID() { return userID; }

    public Integer getFriendStatus() {
        return friendStatus;
    }

    public Boolean getClickable() {
        return clickable;
    }
}
