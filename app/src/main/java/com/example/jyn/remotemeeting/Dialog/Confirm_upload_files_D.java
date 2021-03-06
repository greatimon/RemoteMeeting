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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.jyn.remotemeeting.Etc.Static;
import com.example.jyn.remotemeeting.R;
import com.example.jyn.remotemeeting.Util.Myapp;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by JYN on 2017-12-06.
 */

public class Confirm_upload_files_D extends Activity {

    /** 버터나이프*/
    public Unbinder unbinder;
    @BindView(R.id.comment)     TextView comment;
    @BindView(R.id.container)   LinearLayout container;

    private static final String TAG = "all_" + Confirm_upload_files_D.class.getSimpleName();
    int upload_files_count;
    boolean contain_pdf_file_orNot = false;

    /** 이 클래스를 호출한 클래스 SimpleName */
    String request_class;

    Myapp myapp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.v_confirm_upload_files);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        this.setFinishOnTouchOutside(true);

        // 어플리케이션 객체 생성
        myapp = Myapp.getInstance();

        // 버터나이프 바인드
        unbinder = ButterKnife.bind(this);

        // 이 클래스를 호출한 클래스 인텐트 값으로 받기
        Intent get_intent = getIntent();
        request_class = get_intent.getStringExtra(Static.REQUEST_CLASS);

        Intent intent = getIntent();
        upload_files_count = intent.getIntExtra("upload_files_count", 0);
        contain_pdf_file_orNot = intent.getBooleanExtra("contain_pdf_file_orNot", false);
        Log.d(TAG, "upload_files_count: " + upload_files_count);

        // 코멘트 셋팅
        String comment_str = "";
        if(!contain_pdf_file_orNot) {
            comment_str = String.valueOf(upload_files_count) + "개의 파일을 업로드 하시겠습니까?";
        }
        else if(contain_pdf_file_orNot) {
            comment_str = String.valueOf(upload_files_count) + "개의 파일을 업로드 하시겠습니까?"
                        + "\n\n파일 중에 PDF 파일이 포함되어 있습니다."
                        + "\n일부 PDF는 업로드 중, 에러가 발생할 수 있습니다.";
        }
        comment.setText(comment_str);
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 파트너 끊기
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.yes)
    public void yes(View view) {
        // TODO: redis - 클릭이벤트
        myapp.Redis_log_click_event(getClass().getSimpleName(), view);

        Intent intent = new Intent();
        intent.putExtra("contain_pdf_file_orNot", contain_pdf_file_orNot);
        setResult(RESULT_OK, intent);
        finish();
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 파트너 유지
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.no)
    public void no(View view) {
        // TODO: redis - 클릭이벤트
        myapp.Redis_log_click_event(getClass().getSimpleName(), view);

        setResult(RESULT_CANCELED);
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
