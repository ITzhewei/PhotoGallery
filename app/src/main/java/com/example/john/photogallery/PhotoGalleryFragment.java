package com.example.john.photogallery;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.john.photogallery.net.Downloader;
import com.example.john.photogallery.net.NetUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ZheWei on 2016/9/27.
 */
public class PhotoGalleryFragment extends Fragment {

    @BindView(R.id.rv_launcher)
    RecyclerView mRvLauncher;

    private List<GalleryItem> mGalleryItems = new ArrayList<>();

    private ProgressDialog mProgressDialog;
    private FetchItemsTak mTak;

    private Downloader<GalleryAdapter.GalleryHolder> mDownloader;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mProgressDialog = new ProgressDialog(getActivity());

        Handler responseHandler = new Handler();
        mDownloader = new Downloader<>(responseHandler);
        mDownloader.setLoadListener(new Downloader.DownLoadListener<GalleryAdapter.GalleryHolder>() {
            @Override
            public void onDownLoad(GalleryAdapter.GalleryHolder target, Bitmap thumbnail) {
                BitmapDrawable drawable = new BitmapDrawable(getResources(), thumbnail);
                target.bindGallery(drawable);
            }
        });
        mDownloader.start();
        mDownloader.getLooper();//确保onLooperPrepared()方法会执行


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        ButterKnife.bind(this, view);

        initView();
        mTak = new FetchItemsTak();
        mTak.execute();

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //        mTak.cancel(false);//view销毁,取消下载任务
        mDownloader.clearQueue();//view视图销毁的时候,取消下载任务中对holder的持有
    }

    private void initView() {
        mRvLauncher.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        setupAdapter();
    }

    private void setupAdapter() {
        if (isAdded()) {
            mRvLauncher.setAdapter(new GalleryAdapter(mGalleryItems));
        }
    }

    private class FetchItemsTak extends AsyncTask<Void, Void, List<GalleryItem>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog.show();
        }

        @Override
        protected List<GalleryItem> doInBackground(Void... params) {
            NetUtil netUtil = new NetUtil();
            List<GalleryItem> items = netUtil.fetchItems();
            return items;
        }

        @Override
        protected void onPostExecute(List<GalleryItem> items) {
            super.onPostExecute(items);
            mGalleryItems = items;
            setupAdapter();
            mProgressDialog.dismiss();
        }
    }


    private class GalleryAdapter extends RecyclerView.Adapter {
        List<GalleryItem> mGalleryItems;

        public GalleryAdapter(List<GalleryItem> galleryItems) {
            mGalleryItems = galleryItems;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.gallery_item, parent, false);
            return new GalleryHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            GalleryHolder galleryHolder = (GalleryHolder) holder;
            mDownloader.queueThumbnail(galleryHolder, mGalleryItems.get(position).getMurl());
            galleryHolder.bindGallery(getResources().getDrawable(R.mipmap.ic_launcher));
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }


        private class GalleryHolder extends RecyclerView.ViewHolder {
            private ImageView mImageView;

            public GalleryHolder(View itemView) {
                super(itemView);
                mImageView = (ImageView) itemView.findViewById(R.id.iv_gallery);
            }

            public void bindGallery(Drawable drawable) {
                mImageView.setImageDrawable(drawable);
            }
        }
    }
}
