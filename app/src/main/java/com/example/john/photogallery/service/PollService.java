package com.example.john.photogallery.service;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.john.photogallery.GalleryItem;
import com.example.john.photogallery.view.MainActivity;
import com.example.john.photogallery.base.MyApplication;
import com.example.john.photogallery.net.NetUtil;
import com.example.john.photogallery.util.QueryPreferences;

import java.util.List;

/**
 * Created by ZheWei on 2016/9/30.
 */
public class PollService extends IntentService {

    private static final String TAG = "PollService";
    private static final int POLL_INTERVEL = 1000 * 60;  //60seconds 秒---最小1分钟

    //action
    public static final String ACTION_SHOW_NOTIFICATION = "com.zzw.android.photogallery.SHOW_NOTIFICATION";
    //permission
    public static final String PERM_PRIVATE = "com.zzw.android.photogallery.PRIVATE";

    public static final String REQUEST_CODE = "REQUEST_CODE";
    public static final String NOTIFICATION = "NOTIFICATION";

    public PollService() {
        super(TAG);
    }

    //规范化启动当前服务
    public static Intent newIntent() {
        return new Intent(MyApplication.context, PollService.class);
    }

    //处理逻辑
    @Override
    protected void onHandleIntent(Intent intent) {
        RequestNet();

    }

    //请求网络数据
    private void RequestNet() {
        String query = QueryPreferences.getPrefSearchQuery(this);
        String lastResultId = QueryPreferences.getString(QueryPreferences.PREF_LAST_RESULT_ID);
        List<GalleryItem> galleryItems;
        if (query == null) {
            galleryItems = new NetUtil().fetchRecentPhotos();
        } else {
            galleryItems = new NetUtil().searchPhotos(query);
        }
        if (galleryItems.size() == 0) {
            return;
        }

        String resultId = galleryItems.get(0).getId();
        if (resultId.equals(lastResultId)) {
            Log.i(TAG, "Got an old : " + resultId);
        } else {
            Log.i(TAG, "got an new id: " + resultId);

            Intent intent = MainActivity.newIntent(getApplicationContext());
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
            Notification notification = builder.setTicker("New Pictures")
                    .setSmallIcon(android.R.drawable.ic_menu_report_image)
                    .setContentTitle("New Pictures")
                    .setContentText("You have new Pictures in Gallery")
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build();

            /*NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
            notificationManagerCompat.notify(0, notification);

            sendBroadcast(new Intent(ACTION_SHOW_NOTIFICATION), PERM_PRIVATE);*/
            showBackGroundNotification(0, notification);

        }
        QueryPreferences.setString(QueryPreferences.PREF_LAST_RESULT_ID, resultId);
    }

    private void showBackGroundNotification(int requestcode, Notification notification) {
        Intent i = new Intent(ACTION_SHOW_NOTIFICATION);
        i.putExtra(REQUEST_CODE, requestcode);
        i.putExtra(NOTIFICATION, notification);
        sendOrderedBroadcast(i, PERM_PRIVATE, null, null, Activity.RESULT_OK, null, null);
    }

    //判断是否允许后台下载数据
    private boolean isNetWorkAvailableAndConnection() {
        ConnectivityManager cm = (ConnectivityManager)
                getSystemService(CONNECTIVITY_SERVICE);
        //这里需要添加权限 state
        boolean isNetWorkAvailable = cm.getActiveNetworkInfo() != null;
        boolean isNetWorkConnected;
        isNetWorkConnected = isNetWorkAvailable && cm.getActiveNetworkInfo().isConnected();
        return isNetWorkConnected;
    }

    //设置打开和关闭定时循环唤醒当前service
    public static void setServiceAlarm(boolean isOn) {
        Intent intent = PollService.newIntent();
        PendingIntent pendingIntent = PendingIntent.getService(MyApplication.context, 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager) MyApplication.context.getSystemService(Context.ALARM_SERVICE);
        if (isOn) {
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime()
                    , POLL_INTERVEL, pendingIntent);
        } else {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
        QueryPreferences.setBoolean(QueryPreferences.PREF_IS_ALARM, isOn);
    }

    //
    public static boolean isServiceAlarmOn() {
        Intent intent = PollService.newIntent();
        PendingIntent pi = PendingIntent.getService(MyApplication.context, 0, intent, PendingIntent.FLAG_NO_CREATE);
        return pi != null;
    }
}
