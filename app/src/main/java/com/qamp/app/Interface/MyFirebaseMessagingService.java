package com.qamp.app.Interface;

import static org.webrtc.ContextUtils.getApplicationContext;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;


import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.mesibo.calls.api.MesiboCall;
import com.qamp.app.MessagingModule.MesiboMessagingActivity;
import com.qamp.app.MessagingModule.MesiboUI;
import com.qamp.app.R;


public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private String title;
    private String body;
    private String myNumber;
    private String receiverNumber;
    private String postId;
    private String postType;
    private String groupID;

    public static final String channelId = "Notification_Channel";
    public static final String channelName = "com.qampmessenger.app";

    String notificationTag = "Messaging";

    @Override
    public void onNewToken(String token) {
        Log.e("TAG", "Refreshed token: " + token);
    }

    private int generateNotificationId() {
        // Generate a unique notification ID (e.g., based on a timestamp)
        return (int) System.currentTimeMillis();
    }

    @Override
    public void onMessageReceived(RemoteMessage message) {
        MesiboCall.getInstance().init(getApplicationContext());


        title = message.getData().get("title");
        body = message.getData().get("body");
        myNumber = message.getData().get("destinationId");
        receiverNumber = message.getData().get("sendersAdd");
        groupID = message.getData().get("groupid");


        Log.e("id",myNumber);


        createNotification();


        super.onMessageReceived(message);
    }

    private void createNotification() {
        int notificationId = 1;

        RemoteInput remoteInput = new RemoteInput.Builder("key_text_reply")
                .setLabel("Your answer...")
                .build();

        String number = "91"+myNumber;

        Intent replyIntent = new Intent(this, DirectReplyReceiver.class);
        replyIntent.putExtra("myNumber", myNumber);
        replyIntent.putExtra("receiverNumber", receiverNumber);
        replyIntent.putExtra("notificationId",notificationId);
        replyIntent.putExtra("tag",notificationTag);
        PendingIntent replyPendingIntent = PendingIntent.getBroadcast(this,
                notificationId, replyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action replyAction = new NotificationCompat.Action.Builder(
                R.drawable.ico_forward,
                "Reply",
                replyPendingIntent
        ).addRemoteInput(remoteInput).build();

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.app_icon)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .addAction(replyAction)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_ALL);

                if (myNumber.length() == 4){
                    Intent fullScreenIntent = new Intent(this, MesiboMessagingActivity.class);
                    fullScreenIntent.putExtra(MesiboUI.GROUP_ID, Long.parseLong(myNumber));
                    fullScreenIntent.putExtra("page", "2");
                    fullScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(this, 0, fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                    notificationBuilder.setContentIntent(fullScreenPendingIntent);
                }else {
                    Intent fullScreenIntent = new Intent(this, MesiboMessagingActivity.class);
                    fullScreenIntent.putExtra(MesiboUI.PEER, number);
                    fullScreenIntent.putExtra("page", "2");
                    fullScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(this, 0, fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                    notificationBuilder.setContentIntent(fullScreenPendingIntent);
                }


        Notification notification = notificationBuilder.build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("My Channel Description");
            notificationManager.createNotificationChannel(channel);
        }


        notificationBuilder.setOnlyAlertOnce(true);
        notificationManager.notify(notificationTag,notificationId, notification);

    }
}
