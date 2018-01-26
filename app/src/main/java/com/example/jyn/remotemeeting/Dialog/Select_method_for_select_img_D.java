package com.example.jyn.remotemeeting.Dialog;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.example.jyn.remotemeeting.Etc.Static;
import com.example.jyn.remotemeeting.R;
import com.example.jyn.remotemeeting.Util.Myapp;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by JYN on 2017-12-03.
 */

public class
Select_method_for_select_img_D extends Activity {

    /** 버터나이프*/
    public Unbinder unbinder;
//    @BindView(R.id.camera)Button camera;
//    @BindView(R.id.album)Button album;

    private static final String TAG = "all_"+Select_method_for_select_img_D.class.getSimpleName();
    Intent intent;

    /** 이 클래스를 호출한 클래스 SimpleName */
    String request_class;

    Myapp myapp;


    /**---------------------------------------------------------------------------
     생명주기 ==> onCreate
     ---------------------------------------------------------------------------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.v_select_method_for_image);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        this.setFinishOnTouchOutside(true);

        // 버터나이프 바인드
        unbinder = ButterKnife.bind(this);

        // 어플리케이션 객체 생성
        myapp = Myapp.getInstance();

        intent = new Intent();

        // 이 클래스를 호출한 클래스 인텐트 값으로 받기
        Intent get_intent = getIntent();
        request_class = get_intent.getStringExtra(Static.REQUEST_CLASS);
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 사진 촬영 선택
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.camera)
    public void camera(View view) {
        // TODO: redis - 클릭이벤트
        myapp.Redis_log_click_event(getClass().getSimpleName(), view);

        Log.d(TAG, "카메라 촬영 선택");
        intent.putExtra("method", "camera");
        setResult(RESULT_OK, intent);
        finish();
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 앨범 선택
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.album)
    public void album(View view) {
        // TODO: redis - 클릭이벤트
        myapp.Redis_log_click_event(getClass().getSimpleName(), view);

        Log.d(TAG, "앨범 선택");
        intent.putExtra("method", "album");
        setResult(RESULT_OK, intent);
        finish();
    }


    /**---------------------------------------------------------------------------
     생명주기 ==> onDestroy
     ---------------------------------------------------------------------------*/
    @Override
    protected void onDestroy() {
        // TODO: redis - 화면 이동
        if(request_class != null) {
            myapp.Redis_log_view_crossOver_from_to(
                    getClass().getSimpleName(), request_class);
        }

        // 버터나이프 바인드 해제
        if(unbinder != null) {
            unbinder.unbind();
        }
        // 어플리케이션 객체 null 처리
        myapp = null;
        super.onDestroy();
    }
}
