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

import com.example.jyn.remotemeeting.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by JYN on 2018-01-23.
 *
 * 영상회의가 종료 된 후, 팝업되는 회의 결과 액티비티에서 프로젝트 지정 방법을 선택하는 다이얼로그 액티비티
 */

public class Select_method_for_assign_project_D extends Activity {

    private static final String TAG = "all_"+Select_method_for_assign_project_D.class.getSimpleName();
    Intent intent;

    /** 버터나이프*/
    public Unbinder unbinder;
    @BindView(R.id.unAssign_project_LIN)        LinearLayout unAssign_project_LIN;
    @BindView(R.id.unAssign_project_divider)    View unAssign_project_divider;


    /**---------------------------------------------------------------------------
     생명주기 ==> onCreate
     ---------------------------------------------------------------------------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.v_select_method_for_assign_project);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        this.setFinishOnTouchOutside(true);

        // 버터나이프 바인드
        unbinder = ButterKnife.bind(this);

        intent = getIntent();
        int selected_project_no = intent.getIntExtra("selected_project_no", -1);
        // 현재 선택된 프로젝트가 있는 경우
        if(selected_project_no != -1) {
            unAssign_project_LIN.setVisibility(View.VISIBLE);
            unAssign_project_divider.setVisibility(View.VISIBLE);
        }
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 지정된 프로젝트 unAssign(지정 취소)
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.unAssign_project_LIN)
    public void unAssign_project() {
        Log.d(TAG, "'지정된 프로젝트 unAssign(지정 취소)' 선택");
        Intent return_intent = new Intent();
        return_intent.putExtra("method", "unAssign_project");
        setResult(RESULT_OK, return_intent);
        finish();
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 이미 존재하는 프로젝트에 지정
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.assign_to_existing_project_LIN)
    public void assign_to_existing_project() {
        Log.d(TAG, "'이미 존재하는 프로젝트에 지정' 선택");
        Intent return_intent = new Intent();
        return_intent.putExtra("method", "assign_to_existing_project");
        setResult(RESULT_OK, return_intent);
        finish();
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 새 프로젝트 생성
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.create_new_project_LIN)
    public void create_new_project() {
        Log.d(TAG, "'새 프로젝트 생성' 선택");
        Intent return_intent = new Intent();
        return_intent.putExtra("method", "create_new_project");
        setResult(RESULT_OK, return_intent);
        finish();
    }


    /**---------------------------------------------------------------------------
     오버라이드 ==> onBackPressed
     ---------------------------------------------------------------------------*/
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED, intent);
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
