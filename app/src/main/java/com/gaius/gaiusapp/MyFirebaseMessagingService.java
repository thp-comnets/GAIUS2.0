package com.gaius.gaiusapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.OkHttpResponseListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

import me.leolin.shortcutbadger.ShortcutBadger;
import okhttp3.Response;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID ="60001";
    private NotificationManager mNotificationManager;

    public MyFirebaseMessagingService() {
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.putExtra("notification", true);

        final PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent,
                PendingIntent.FLAG_ONE_SHOT);

        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        int notificationId = new Random().nextInt(60000);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();
        int number = Integer.parseInt(remoteMessage.getData().get("pending_requests"));
        editor.putInt("pending-requests", number);
        editor.apply();
        ShortcutBadger.applyCount(getApplicationContext(), number);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            setupChannels();
        }

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_gaius_round)
                        .setContentTitle(remoteMessage.getData().get("title"))
//                        .setStyle(new NotificationCompat.BigPictureStyle()
//                                .setSummaryText(remoteMessage.getData().get("message"))
//                                .bigPicture(bitmap))
                        .setContentText(remoteMessage.getData().get("message"))
                        .setAutoCancel(true)
                        .setLights(Color.GREEN, 1000, 500)
                        .setSound(defaultSoundUri)
                        .setVibrate(new long[] {0, 200, 200, 200 })
                        .setContentIntent(pendingIntent);

        mNotificationManager.notify(notificationId, notificationBuilder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupChannels(){
        CharSequence channelName = "GaiusChannelName"; //FIXME
//        String channelDescription = "GaiusDescription"; //FIXME

        NotificationChannel channel;
        channel = new NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_DEFAULT);
//        adminChannel.setDescription(channelDescription);
        channel.enableLights(true);
        channel.setLightColor(Color.GREEN);
        channel.enableVibration(true);
        if (mNotificationManager != null) {
            mNotificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onNewToken(String token) {
        // this is being called on app install
        Log.d("firebase", "Refreshed token: " + token);

        // save token for later
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("cm-token", token);
        editor.apply();

        if (prefs.getString("token", null) != null) {
            AndroidNetworking.get(prefs.getString("base_url", null)+"updateCmToken.py")
                    .addQueryParameter("token", prefs.getString("token", "null"))
                    .addQueryParameter("cm-token", prefs.getString("cm-token", "null"))
                    .setPriority(Priority.HIGH)
                    .build()
                    .getAsOkHttpResponse(new OkHttpResponseListener() {
                        @Override
                        public void onResponse(Response response) {
                            Log.d("firebase","Token updated ");
                        }

                        @Override
                        public void onError(ANError anError) {
                            Log.d("firebase","ANError "+anError);
                        }
                    });
        }

    }
}
