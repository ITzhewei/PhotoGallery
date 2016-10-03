package com.example.john.photogallery;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.john.photogallery.net.NetUtil;
import com.example.john.photogallery.receiver.VisiableFragment;
import com.example.john.photogallery.service.PollService;
import com.example.john.photogallery.util.QueryPreferences;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ZheWei on 2016/9/27.
 */
public class PhotoGalleryFragment extends VisiableFragment {

    @BindView(R.id.rv_launcher)
    RecyclerView mRvLauncher;

    private List<GalleryItem> mGalleryItems = new ArrayList<>();//items集合

    private ProgressDialog mProgressDialog;

    //    private Downloader<GalleryAdapter.GalleryHolder> mDownloader;//handlerThread

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true); //设置当Activity销毁时,仍然保存fragment的实例
        setHasOptionsMenu(true); //设置menu
        mProgressDialog = new ProgressDialog(getActivity());
        updateitems();//执行网络任务

        Handler responseHandler = new Handler();//得到当前主线程的handler

        //handlerThread
        //        mDownloader = new Downloader<>(responseHandler);
        //        mDownloader.setLoadListener(new Downloader.DownLoadListener<GalleryAdapter.GalleryHolder>() {
        //            @Override
        //            public void onDownLoad(GalleryAdapter.GalleryHolder target, Bitmap thumbnail) {
        //                BitmapDrawable drawable = new BitmapDrawable(getResources(), thumbnail);
        //                target.bindDrawable(drawable);
        //            }
        //        });
        //        //开启handlerThread,这是在子线程中运行的
        //        mDownloader.start();
        //        mDownloader.getLooper();//确保onLooperPrepared()方法会执行

        //        PollService.setServiceAlarm(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        ButterKnife.bind(this, view);

        initView();


        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //        mTak.cancel(false);//view销毁,取消下载任务
        //        mDownloader.clearQueue();//view视图销毁的时候,取消下载任务中对holder的持有
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        //得到menu通过menu.xml文件
        inflater.inflate(R.menu.fragment_gallery, menu);
        //得到MenuItem
        MenuItem search = menu.findItem(R.id.menu_item_search);
        //得到具体的ActionView
        SearchView searchActionView = (SearchView) search.getActionView();
        //设置SearchView的时刻监听
        searchActionView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                QueryPreferences.setPrefSearchQuery(getActivity(), query);
                updateitems();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        //得到控制alarm开关的menu
        MenuItem alarmMenu = menu.findItem(R.id.menu_item_toggle_polling);
        if (PollService.isServiceAlarmOn()) {
            alarmMenu.setTitle("close Alarm");
        } else {
            alarmMenu.setTitle("start Alarm");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_clear:
                QueryPreferences.setPrefSearchQuery(getActivity(), null);
                updateitems();
                return true;
            case R.id.menu_item_toggle_polling:
                boolean isalarmOn = PollService.isServiceAlarmOn();
                PollService.setServiceAlarm(!isalarmOn);
                getActivity().invalidateOptionsMenu();  //更新menu按钮
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateitems() {
        new FetchItemsTak().execute();
    }

    private void initView() {
        //设置RecycleView的LayoutManager
        mRvLauncher.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        setupAdapter();
    }

    //设置Adapter
    private void setupAdapter() {
        if (isAdded()) {
            mRvLauncher.setAdapter(new GalleryAdapter(mGalleryItems));
        }
    }

    //网络异步任务
    private class FetchItemsTak extends AsyncTask<Void, Void, List<GalleryItem>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog.show();
        }

        @Override
        protected List<GalleryItem> doInBackground(Void... params) {
            NetUtil netUtil = new NetUtil();
            String query = QueryPreferences.getPrefSearchQuery(getActivity());
            List<GalleryItem> items;
            //            if (query == null) {
            //                items = netUtil.fetchRecentPhotos();
            //            } else {
            //                items = netUtil.searchPhotos(query);
            //            }
            items = query == null ? netUtil.fetchRecentPhotos() : netUtil.searchPhotos(query);
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


    //Adapter
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
            //            mDownloader.queueThumbnail(galleryHolder, mGalleryItems.get(position).getMurl());
            //            galleryHolder.bindDrawable(getResources().getDrawable(R.mipmap.ic_launcher));
            galleryHolder.bindGallery(mGalleryItems.get(position));
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }


        //Holder
        private class GalleryHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private ImageView mImageView;
            private GalleryItem mGalleryItem;

            public GalleryHolder(View itemView) {
                super(itemView);
                mImageView = (ImageView) itemView.findViewById(R.id.iv_gallery);

                itemView.setOnClickListener(this);
            }

            //            public void bindDrawable(Drawable drawable) {
            //                mImageView.setImageDrawable(drawable);
            //            }

            public void bindGallery(GalleryItem galleryItem) {
                mGalleryItem = galleryItem;
//                final PhotoViewAttacher viewAttacher = new PhotoViewAttacher(mImageView);
                Picasso.with(getActivity()).load(galleryItem.getMurl()).into(mImageView, new Callback() {
                    @Override
                    public void onSuccess() {
//                        viewAttacher.update();
                    }

                    @Override
                    public void onError() {

                    }
                });
            }

            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW, mGalleryItem.getPhotoPageUri());
                startActivity(i);
            }
        }
    }
}
