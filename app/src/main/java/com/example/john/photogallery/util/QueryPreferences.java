package com.example.john.photogallery.util;

import android.content.Context;
import android.preference.PreferenceManager;

import com.example.john.photogallery.base.MyApplication;

/**
 * Created by ZheWei on 2016/9/29.
 */
public class QueryPreferences {
    private static final String PREF_SEARCH_QUERY = "search_query";
    public static final String PREF_LAST_RESULT_ID = "lastResultId";
    public static final String PREF_IS_ALARM = "isAlarm";

    public static String getPrefSearchQuery(Context context) {
        return getString(PREF_SEARCH_QUERY);

    }

    public static void setPrefSearchQuery(Context context, String query) {
        setString(PREF_SEARCH_QUERY, query);
    }

    public static String getString(String tag) {
        return PreferenceManager.getDefaultSharedPreferences(MyApplication.context)
                .getString(tag, null);
    }

    public static void setString(String tag, String s) {
        PreferenceManager.getDefaultSharedPreferences(MyApplication.context)
                .edit().putString(tag, s).apply();
    }
    public static boolean getBoolean(String tag) {
        return PreferenceManager.getDefaultSharedPreferences(MyApplication.context)
                .getBoolean(tag, false);
    }

    public static void setBoolean(String tag, boolean b) {
        PreferenceManager.getDefaultSharedPreferences(MyApplication.context)
                .edit().putBoolean(tag, b).apply();
    }
}
