package com.example.jyn.remotemeeting.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
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
 * Created by JYN on 2017-12-03.
 */

public class Preview_img_A extends Activity {

    /** 버터나이프*/
    public Unbinder unbinder;
    @BindView(R.id.preview_img)     ImageView preview_img;
    @BindView(R.id.yes)             TextView yes;
    @BindView(R.id.no)              TextView no;

    private static final String TAG = "all_"+Preview_img_A.class.getSimpleName();
    String absolutePath;

    // 이 클래스를 호출한 클래스 SimpleName
    String request_class;


    /**---------------------------------------------------------------------------
     생명주기 ==> onCreate
     ---------------------------------------------------------------------------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_preview_img);
        // 버터나이프 바인드
        unbinder = ButterKnife.bind(this);

        // 이 클래스를 호출한 클래스 인텐트 값으로 받기
        Intent get_intent = getIntent();
        request_class = get_intent.getStringExtra("request_class");

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
        absolutePath = intent.getStringExtra("absolutePath");

        Glide
            .with(this)
            .load(absolutePath)
            .override(width, height)
            .fitCenter()
            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
            .into(preview_img);
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 사진 선택
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.yes)
    public void yes() {
        Intent intent = new Intent();
        intent.putExtra("absolutePath", absolutePath);
        setResult(RESULT_OK, intent);
        finish();
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 취소
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.no)
    public void no() {
        finish();
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
