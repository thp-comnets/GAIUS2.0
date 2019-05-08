package com.gaius.gaiusapp.utils;

import java.util.Arrays;
import java.util.List;

public class Constants {
    public static String TEMPDIR = "MyFolder/Images";

    public static final int INVALID_POSITION = -1;

    public static final int REQUEST_TYPE_NEWSFEED = 0;
    public static final int REQUEST_TYPE_ALL = 1;
    public static final int REQUEST_TYPE_MYOWN = 2;
    public static final int REQUEST_TYPE_FRIEND = 3;

    public static final int REQUEST_CONTENT_ALL = 1;
    public static final int REQUEST_CONTENT_PAGES = 2;
    public static final int REQUEST_CONTENT_IMAGES = 3;
    public static final int REQUEST_CONTENT_VIDEOS = 4;
    public static final int REQUEST_CONTENT_ADS = 5;
    public static final int REQUEST_CONTENT_AUDIOS = 6;

    public static final int UPDATE_BADGE_NOTIFICATION_FRIENDS = 0;
    public static final int UPDATE_BADGE_NOTIFICATION_LAUNCHER = 1;

    public static final int FRIEND_STATUS_NOT_CONNECTED = 0;
    public static final int FRIEND_STATUS_PENDING = 1;
    public static final int FRIEND_STATUS_CONNECTED = 2;
    public static final int FRIEND_STATUS_ACCEPT = 3;
    public static final int FRIEND_STATUS_SUBSCRIBE = 4;
    public static final int FRIEND_STATUS_UNSUBSCRIBE = 5;

    public static final List<String> FRIEND_ACTION_LIST = Arrays.asList("connect", "withdraw", "remove", "accept", "subscribe", "unsubscribe");


}
