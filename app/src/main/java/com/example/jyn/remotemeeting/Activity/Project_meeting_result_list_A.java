package com.example.jyn.remotemeeting.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.jyn.remotemeeting.Adapter.RCV_project_meeting_result_list_adapter;
import com.example.jyn.remotemeeting.DataClass.Meeting_room;
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

public class Project_meeting_result_list_A extends Activity {


    private static final String TAG = "all_"+Project_meeting_result_list_A.class.getSimpleName();
    Myapp myapp;

    /** 버터나이프 */
    public Unbinder unbinder;
    @BindView(R.id.actionBar_LIN)                   LinearLayout actionBar_LIN;
    @BindView(R.id.project_folder_img)              ImageView project_folder_img;
    @BindView(R.id.project_name_txt)                TextView project_name_txt;
    @BindView(R.id.modify_project)                  TextView modify_project;
    @BindView(R.id.meeting_result_recyclerView)     RecyclerView recyclerView;

    // 리사이클러뷰 관련 클래스
    public RCV_project_meeting_result_list_adapter rcv_project_meeting_result_list_adapter;
    public RecyclerView.LayoutManager layoutManager;

    // 프로젝트 번호
    int project_no;


    /**---------------------------------------------------------------------------
     생명주기 ==> onCreate
     ---------------------------------------------------------------------------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_project_meeting_result);

        // 버터나이프 바인드
        unbinder = ButterKnife.bind(this);
        // 어플리케이션 객체 생성
        myapp = Myapp.getInstance();

        Intent intent = getIntent();
        project_no = intent.getIntExtra("project_no", -1);
        Log.d(TAG, "project_no: " + project_no);

        /** 리사이클러뷰 */
        recyclerView.setHasFixedSize(true);
        // LinearLayoutManager 사용, 구분선 표시
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        // 리사이클러뷰 구분선 - 가로 (클래스 생성)
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(this, "Project_meeting_result_list_A"));
        // 애니메이션 설정
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        ArrayList<Meeting_room> meeting_room_arr= myapp.get_meeting_room_list(project_no);

        rcv_project_meeting_result_list_adapter = new RCV_project_meeting_result_list_adapter(
                this, R.layout.i_meeting_result_brief, meeting_room_arr);

        recyclerView.setAdapter(rcv_project_meeting_result_list_adapter);
        rcv_project_meeting_result_list_adapter.notifyDataSetChanged();
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
