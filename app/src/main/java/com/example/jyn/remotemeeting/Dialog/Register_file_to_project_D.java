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

/**
 * Created by JYN on 2017-11-17.
 */

public class Register_file_to_project_D extends Activity {

    Intent intent;
    private static final String TAG = "all_"+Register_file_to_project_D.class.getSimpleName();

    /** 이 클래스를 호출한 클래스 SimpleName */
    String request_class;

    Myapp myapp;

//    @BindView(R.id.pdf)     public Button pdf_click;
//    @BindView(R.id.img)     public Button img_click;
//    @BindView(R.id.all)     public Button all_click;


    /**---------------------------------------------------------------------------
     생명주기 ==> onCreate
     ---------------------------------------------------------------------------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.v_register_file_to_project);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        this.setFinishOnTouchOutside(true);
        ButterKnife.bind(this);

        // 어플리케이션 객체 생성
        myapp = Myapp.getInstance();

        intent = new Intent();
        Log.d(TAG, "onCreate");

        // 이 클래스를 호출한 클래스 인텐트 값으로 받기
        Intent get_intent = getIntent();
        request_class = get_intent.getStringExtra(Static.REQUEST_CLASS);
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> PDF 파일 보기 선택
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.pdf)
    public void show_pdf(View view) {
        // TODO: redis - 클릭이벤트
        myapp.Redis_log_click_event(getClass().getSimpleName(), view);

        Log.d(TAG, "show_pdf() 클릭!");
        intent.putExtra("FORMAT", "pdf");
        setResult(RESULT_OK, intent);
        finish();
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> img 파일 보기 선택
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.img)
    public void show_img(View view) {
        // TODO: redis - 클릭이벤트
        myapp.Redis_log_click_event(getClass().getSimpleName(), view);

        Log.d(TAG, "show_img() 클릭!");
        intent.putExtra("FORMAT", "img");
        setResult(RESULT_OK, intent);
        finish();
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> PDF, img 파일 모두 보기 선택
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.all)
    public void show_all(View view) {
        // TODO: redis - 클릭이벤트
        myapp.Redis_log_click_event(getClass().getSimpleName(), view);

        Log.d(TAG, "show_all() 클릭!");
        intent.putExtra("FORMAT", "all");
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
        // 어플리케이션 객체 null 처리
        myapp = null;
        super.onDestroy();
    }
}
