package com.example.john.photogallery.receiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.example.john.photogallery.service.PollService;

/**
 * Created by ZheWei on 2016/10/3.
 */
public class VisiableFragment extends Fragment {
    @Override
    public void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(PollService.ACTION_SHOW_NOTIFICATION);
        getActivity().registerReceiver(mBroadcastReceiver, filter, PollService.PERM_PRIVATE, null);
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(mBroadcastReceiver);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(getActivity(), "123456", Toast.LENGTH_SHORT).show();

            setResultCode(Activity.RESULT_CANCELED);
        }
    };



}
