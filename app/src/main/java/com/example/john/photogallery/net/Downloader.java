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
    private static final int MESSAGE_DOWNLOAD = 0;//msg.what

    private boolean mHasQuit = false;//是否退出了该线程

    private Handler mRequestHandler;//子线程的handler
    private ConcurrentHashMap<T, String> mRequestmap = new ConcurrentHashMap<>();//线程安全的map集合

    private Handler mResponseHandler;//主线程的handler

    public Downloader(Handler handler) {
        super(TAG);
        mResponseHandler = handler;//获取主线程的handler
    }

    //handlerThread必须要执行的方法,可是用getLooper()确保该方法一定执行
    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        mRequestHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == MESSAGE_DOWNLOAD) {
                    T target = (T) msg.obj;
                    //对图片进行下载
                    handleRequestDownLoad(target);
                }
                return false;
            }
        });
    }

    private void handleRequestDownLoad(final T target) {
        //在子线程中执行
        try {
            final String url = mRequestmap.get(target);
            if (url == null) {
                return;
            }
            NetUtil netUtil = new NetUtil();
            byte[] bytes = netUtil.getUrlBytes(url);
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

            //这是主线程的handler,post方法是在主线程中执行的
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

    //退出handlerThread
    @Override
    public boolean quit() {
        mHasQuit = true;
        return super.quit();
    }

    //清除requestHandler的所有message
    public void clearQueue() {
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
    }

    //当得到url时,发送消息下载图片
    public void queueThumbnail(T target, String url) {
        Log.i(TAG, "queueThumbnail: " + url);
        if (url == null) {
            mRequestmap.remove(target);
        } else {
            mRequestmap.put(target, url);
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget();
        }
    }

    //以下是回调接口
    private DownLoadListener<T> mLoadListener;

    public void setLoadListener(DownLoadListener<T> listener) {
        mLoadListener = listener;
    }

    public interface DownLoadListener<T> {
        void onDownLoad(T target, Bitmap thumbnail);
    }

}
