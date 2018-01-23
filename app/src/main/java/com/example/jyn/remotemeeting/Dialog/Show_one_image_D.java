package com.example.jyn.remotemeeting.Dialog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.jyn.remotemeeting.Fragment.Partner_F;
import com.example.jyn.remotemeeting.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by JYN on 2018-01-23.
 */

public class Show_one_image_D extends Activity {

    /** 버터나이프*/
    public Unbinder unbinder;
    @BindView(R.id.back_img)                ImageView back_img;
    @BindView(R.id.show_img)                ImageView show_img;
    @BindView(R.id.file_info_txt)           TextView file_info_txt;

    private static final String TAG = "all_"+Show_one_image_D.class.getSimpleName();
    String image_source;
    String fileName;


    /**---------------------------------------------------------------------------
     생명주기 ==> onCreate
     ---------------------------------------------------------------------------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.v_show_one_image);

        // 버터나이프 바인드
        unbinder = ButterKnife.bind(this);

        /** 디바이스 화면사이즈 가져오기 */
        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        Log.d("device", "width: " + width);
        Log.d("device", "height: " + height);
        if(width > 1080) {
            width = 1080;
        }

        Intent intent = getIntent();
        image_source = intent.getStringExtra("image_source");
        fileName = intent.getStringExtra("fileName");

        String[] this_fileName = new String[2];
        if(fileName.contains("&__&")) {
            this_fileName = fileName.split("&__&");
        }
        else if(fileName.contains("__")) {
            this_fileName = fileName.split("__");
        }

        // 이미지 셋팅
        Glide
            .with(this)
            .load(image_source)
            .override(width, height)
            .fitCenter()
            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
            .into(show_img);

        // 파일 이름 셋팅
        file_info_txt.setText(this_fileName[1]);
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 뒤로 가기 이미지 클릭
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.back_img)
    public void back() {
        onBackPressed();
    }


    /**---------------------------------------------------------------------------
     생명주기 ==> onDestroy
     ---------------------------------------------------------------------------*/
    @Override
    protected void onDestroy() {
        // 버터나이프 바인드 해제
        if(unbinder != null) {
            unbinder.unbind();
        }
        super.onDestroy();
    }
}
