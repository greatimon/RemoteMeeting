package com.example.jyn.remotemeeting.Dialog;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.example.jyn.remotemeeting.R;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by JYN on 2017-12-02.
 */

public class Confirm_logout_D extends Activity {

    /** 버터나이프*/
    public Unbinder unbinder;
    private static final String TAG = "all_"+Confirm_logout_D.class.getSimpleName();

    /**---------------------------------------------------------------------------
     생명주기 ==> onCreate
     ---------------------------------------------------------------------------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.v_confirm_logout);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        this.setFinishOnTouchOutside(true);

        // 버터나이프 바인드
        unbinder = ButterKnife.bind(this);
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 로그아웃 진행
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.yes)
    public void yes() {
        setResult(RESULT_OK);
        finish();
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 로그아웃 취소
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.no)
    public void no() {
        setResult(RESULT_CANCELED);
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
