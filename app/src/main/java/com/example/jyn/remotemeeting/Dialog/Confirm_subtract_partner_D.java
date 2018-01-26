package com.example.jyn.remotemeeting.Dialog;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.example.jyn.remotemeeting.Etc.Static;
import com.example.jyn.remotemeeting.R;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by JYN on 2017-12-02.
 */

public class Confirm_subtract_partner_D extends Activity {

    /** 버터나이프*/
    public Unbinder unbinder;
    int position = -1;
    String target_user_no = "";
    private static final String TAG = "all_"+Confirm_subtract_partner_D.class.getSimpleName();

    /** 이 클래스를 호출한 클래스 SimpleName */
    String request_class;

    /**---------------------------------------------------------------------------
     생명주기 ==> onCreate
     ---------------------------------------------------------------------------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.v_confirm_subtract_partner_);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        this.setFinishOnTouchOutside(true);

        // 버터나이프 바인드
        unbinder = ButterKnife.bind(this);

        // 이 클래스를 호출한 클래스 인텐트 값으로 받기
        Intent get_intent = getIntent();
        request_class = get_intent.getStringExtra(Static.REQUEST_CLASS);

        Intent intent = getIntent();
        position = intent.getIntExtra("position", -1);
        target_user_no = intent.getStringExtra("target_user_no");
        Log.d(TAG, "position: " + position);
        Log.d(TAG, "target_user_no: " + target_user_no);
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 파트너 끊기
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.yes)
    public void yes() {
        Intent intent = new Intent();
        if(position != -1 && !target_user_no.equals("")) {
            intent.putExtra("position", position);
            intent.putExtra("target_user_no", target_user_no);
        }
        setResult(RESULT_OK, intent);
        finish();
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 파트너 유지
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
