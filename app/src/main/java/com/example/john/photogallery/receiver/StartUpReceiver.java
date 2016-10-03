package com.example.john.photogallery.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.john.photogallery.service.PollService;
import com.example.john.photogallery.util.QueryPreferences;

/**
 * Created by ZheWei on 2016/10/3.
 */
public class StartUpReceiver extends BroadcastReceiver {

    private static final String TAG = "StartUpReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isOn = QueryPreferences.getBoolean(QueryPreferences.PREF_IS_ALARM);
        PollService.setServiceAlarm(isOn);
    }
}
