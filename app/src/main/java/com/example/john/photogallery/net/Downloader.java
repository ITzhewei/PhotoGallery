package com.example.john.photogallery.net;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ZheWei on 2016/9/28.
 */
public class Downloader<T> extends HandlerThread {
    private static final String TAG = "Downloader";
    private static final int MESSAGE_DOWNLOAD = 0;

    private boolean mHasQuit = false;

    private Handler mRequestHandler;
    private ConcurrentHashMap<T, String> mRequestmap = new ConcurrentHashMap<>();

    private Handler mResponseHandler;

    public Downloader(Handler handler) {
        super(TAG);
        mResponseHandler = handler;
    }

    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        mRequestHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == MESSAGE_DOWNLOAD) {
                    T target = (T) msg.obj;
                    handleRequestDownLoad(target);
                }
                return false;
            }
        });
    }

    private void handleRequestDownLoad(final T target) {
        try {
            final String url = mRequestmap.get(target);
            if (url == null) {
                return;
            }
            NetUtil netUtil = new NetUtil();
            byte[] bytes = netUtil.getUrlBytes(url);
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mRequestmap.get(target) != url || mHasQuit) {
                        return;
                    }
                    mRequestmap.remove(target);
                    mLoadListener.onDownLoad(target, bitmap);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean quit() {
        mHasQuit = true;
        return super.quit();
    }

    public void clearQueue() {
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
    }

    public void queueThumbnail(T target, String url) {
        Log.i(TAG, "queueThumbnail: " + url);
        if (url == null) {
            mRequestmap.remove(target);
        } else {
            mRequestmap.put(target, url);
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget();
        }
    }

    private DownLoadListener<T> mLoadListener;

    public void setLoadListener(DownLoadListener<T> listener) {
        mLoadListener = listener;
    }

    public interface DownLoadListener<T> {
        void onDownLoad(T target, Bitmap thumbnail);
    }

}
