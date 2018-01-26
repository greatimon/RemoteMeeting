package com.example.jyn.remotemeeting.Dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.example.jyn.remotemeeting.Adapter.RCV_project_adapter_for_assign_project;
import com.example.jyn.remotemeeting.DataClass.Project;
import com.example.jyn.remotemeeting.Etc.Static;
import com.example.jyn.remotemeeting.R;
import com.example.jyn.remotemeeting.Util.Myapp;
import com.example.jyn.remotemeeting.Util.SimpleDividerItemDecoration;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by JYN on 2018-01-23.
 */

public class Assign_to_existing_project_D extends Activity {

    private static final String TAG = "all_"+Assign_to_existing_project_D.class.getSimpleName();
    Myapp myapp;
    Intent intent;

    /** 버터나이프*/
    public Unbinder unbinder;
    @BindView(R.id.project_rcv)         RecyclerView recyclerView;

    // 리사이클러뷰 어댑터
    public RCV_project_adapter_for_assign_project rcv_adapter;
    // 리사이클러뷰 레이아웃 매니저
    public RecyclerView.LayoutManager layoutManager;

    // 서버로부터 프로젝트 정보 및 종료된 영상회의 정보를 받아서 담아놓을 어레이리스트
    public ArrayList<Project> project_arr;

    //
    public static Handler receive_project_no_from_adapter_handler;

    /** 이 클래스를 호출한 클래스 SimpleName */
    String request_class;


    /**---------------------------------------------------------------------------
     생명주기 ==> onCreate
     ---------------------------------------------------------------------------*/
    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.v_assign_to_existing_project);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        this.setFinishOnTouchOutside(true);

        intent = new Intent();

        // 버터나이프 바인드
        unbinder = ButterKnife.bind(this);

        // 어플리케이션 객체 생성
        myapp = Myapp.getInstance();

        // 이 클래스를 호출한 클래스 인텐트 값으로 받기
        Intent get_intent = getIntent();
        request_class = get_intent.getStringExtra(Static.REQUEST_CLASS);

        /** 서버로부터 프로젝트 리스트 받기 */
        project_arr = myapp.get_project_list();
        // 로그 찍어 보기
        for(int i=0; i<project_arr.size(); i++) {
            Log.d(TAG, "project_no("+ i +"): " + project_arr.get(i).getProject_no());
            Log.d(TAG, "project_color("+ i +"): " + project_arr.get(i).getProject_color());
            Log.d(TAG, "project_name("+ i +"): " + project_arr.get(i).getProject_name());
        }

        recyclerView.setHasFixedSize(true);
        // LinearLayoutManager 사용, 구분선 표시
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        // 리사이클러뷰 구분선 - 가로 (클래스 생성)
        recyclerView.addItemDecoration(
                new SimpleDividerItemDecoration(this, Assign_to_existing_project_D.class.getSimpleName()));
        // 애니메이션 설정
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        //// 어댑터 생성
        // 매개변수 1. this context
        // 매개변수 2. 인플레이팅 되는 리사이클러뷰 item 레이아웃
        // 매개변수 3. 리사이클러뷰에 표시될 데이터 어레이리스트
        rcv_adapter = new RCV_project_adapter_for_assign_project(
                this, R.layout.i_existing_project, project_arr);

        // 리사이클러뷰에 어댑터 할당
        recyclerView.setAdapter(rcv_adapter);
        rcv_adapter.notifyDataSetChanged();

        // 핸들러 생성
        receive_project_no_from_adapter_handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                // 리사이클러뷰 어댑터로부터 클릭한 프로젝트의 'no' 가 넘어왔을 때,
                if(msg.what == 0) {
                    // 전달된 데이터 확인
                    int selected_project_no = msg.getData().getInt("selected_project_no");
                    String selected_project_color = msg.getData().getString("selected_project_color");
                    String selected_project_name = msg.getData().getString("selected_project_name");
                    Log.d(TAG, "selected_project_no: " + selected_project_no);
                    Log.d(TAG, "selected_project_color: " + selected_project_color);
                    Log.d(TAG, "selected_project_name: " + selected_project_name);

                    // 내부 메소드 호출
                    select_this_project(selected_project_no, selected_project_color, selected_project_name);
                }
            }
        };
    }


    /**---------------------------------------------------------------------------
     메소드(Static) ==> 리사이클러뷰 어댑터로부터 선택한 프로젝트의 'no'를 받아,
                        intent에 담아 setResult를 통해 넘기면서 액티비티를 종료한다
     ---------------------------------------------------------------------------*/
    public void select_this_project(
            int selected_project_no, String selected_project_color, String selected_project_name) {
        intent.putExtra("selected_project_no", selected_project_no);
        intent.putExtra("selected_project_color", selected_project_color);
        intent.putExtra("selected_project_name", selected_project_name);
        setResult(RESULT_OK, intent);
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
        // 어플리케이션 객체 null 처리
        myapp = null;
        super.onDestroy();
    }
}
