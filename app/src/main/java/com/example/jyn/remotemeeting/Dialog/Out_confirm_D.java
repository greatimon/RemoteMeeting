package com.example.jyn.remotemeeting.Dialog;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.example.jyn.remotemeeting.R;
import com.example.jyn.remotemeeting.Util.Myapp;

/**
 * Created by JYN on 2017-11-11.
 */

public class Out_confirm_D extends Activity {

    String request_class;

    Myapp myapp;

    /**---------------------------------------------------------------------------
     생명주기 ==> onCreate
     ---------------------------------------------------------------------------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.v_out_confirm);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        this.setFinishOnTouchOutside(true);

        // 어플리케이션 객체 생성
        myapp = Myapp.getInstance();

        // 이 클래스를 호출한 클래스 인텐트 값으로 받기
        Intent get_intent = getIntent();
        request_class = get_intent.getStringExtra("request_class");
    }

    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 회의 종료 취소
     ---------------------------------------------------------------------------*/
    public void out_no_clicked(View view) {
        // TODO: redis - 클릭이벤트
        myapp.Redis_log_click_event(getClass().getSimpleName(), view);

        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 회의 종료
     ---------------------------------------------------------------------------*/
    public void out_yes_clicked(View view) {
        // TODO: redis - 클릭이벤트
        myapp.Redis_log_click_event(getClass().getSimpleName(), view);

        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        // TODO: redis - 화면 이동
        if(request_class != null) {
            myapp.Redis_log_view_crossOver_from_to(
                    getClass().getSimpleName(), request_class);
        }
        // 어플리케이션 객체 null 처리
        myapp = null;

        super.onDestroy();
    }
}
