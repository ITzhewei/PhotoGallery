package com.example.john.photogallery.receiver;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.example.john.photogallery.view.MainActivity;
import com.example.john.photogallery.service.PollService;

/**
 * Created by ZheWei on 2016/10/3.
 */
public class NotificationReceiver extends BroadcastReceiver {

    private static final String TAG = "NotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive: " + getResultCode());
        if (getResultCode() != Activity.RESULT_OK) {
            return;
        }
        Log.i(TAG, "onReceive: " + getResultCode() + "get it");
        int intExtra = intent.getIntExtra(PollService.REQUEST_CODE, 0);
        Notification notification = (Notification) intent.getSerializableExtra(PollService.NOTIFICATION);
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(context);
        managerCompat.notify(intExtra, notification);
        //另外添加的
        Intent intent1 = MainActivity.newIntent(context);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent1, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        Notification notification1 = builder.setTicker("New Pictures")
                .setSmallIcon(android.R.drawable.ic_menu_report_image)
                .setContentTitle("New Pictures")
                .setContentText("You have new Pictures in Gallery")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(0, notification1);
    }
}
