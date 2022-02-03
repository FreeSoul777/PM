package com.mygame.music_alpha;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

public class MusicActivity extends SingleFragmentActivity {


    @Override
    protected Fragment createFragment() {
        return MusicFragment.newInstance();
    }

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        getSupportActionBar().hide();
//    }

}