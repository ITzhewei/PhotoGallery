package com.example.john.photogallery.view;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.example.john.photogallery.R;

/**
 * Created by john on 2016/10/3.
 */

public class PhotoPageActivity extends AppCompatActivity {

    public static Intent newIntent(Context context, Uri photoUri) {
        Intent intent = new Intent(context, PhotoPageActivity.class);
        //添加data-->Uri
        intent.setData(photoUri);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);
        if (fragment == null) {
            fragment = createFragment();
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    private Fragment createFragment() {
        return PhotoPageFragment.getInstance(getIntent().getData());
    }

}
