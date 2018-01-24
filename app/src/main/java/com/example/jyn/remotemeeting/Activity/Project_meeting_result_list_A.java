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
import com.example.jyn.remotemeeting.DataClass.Project;
import com.example.jyn.remotemeeting.R;
import com.example.jyn.remotemeeting.Util.Myapp;
import com.example.jyn.remotemeeting.Util.SimpleDividerItemDecoration;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by JYN on 2018-01-23.
 */

public class Project_meeting_result_list_A extends Activity {


    private static final String TAG = "all_"+Project_meeting_result_list_A.class.getSimpleName();
    Myapp myapp;
    public static final int REQUEST_OPEN_THIS_MEETING_RESULT = 1111;

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
    Project project;


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
        // intent로 Project 클래스를 jsonString으로 변환한 값을 받는다
        String project_str = intent.getStringExtra("project_str");
        if(project_str != null) {
            Log.d(TAG, "project_str: " + project_str);
            Gson gson = new Gson();
            // Gson을 이용해서 Project 객체로 변환시킨다
            project = gson.fromJson(project_str, Project.class);
        }

        if(project != null) {
            // Project 객체에서 Color 값을 가져온다
            String project_color = project.getProject_color();
            // 해당 Color String 값으로, Color int 값을 가져온다
            int color_value = myapp.project_color(project_color);
            // ActionBar 위치에 있는 레이아웃의 색을 변경한다.
            actionBar_LIN.setBackgroundColor(color_value);
        }


        /** 리사이클러뷰 */
        recyclerView.setHasFixedSize(true);
        // LinearLayoutManager 사용, 구분선 표시
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        // 리사이클러뷰 구분선 - 가로 (클래스 생성)
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(this, "Project_meeting_result_list_A"));
        // 애니메이션 설정
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // 서버로부터 meeting_room_arr 리스트 받아와서, 어댑터로 넘기는 내부 메소드 호출
        activate_RCV();
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 서버로부터 meeting_room_arr 리스트 받아와서, 어댑터로 넘기기
     ---------------------------------------------------------------------------*/
    public void activate_RCV() {
        // 서버로부터 해당 프로젝트에 지정된 회의결과 리스트 받아오기
        ArrayList<Meeting_room> meeting_room_arr = myapp.get_meeting_room_list(project.getProject_no());
        /** 'meeting_no'를 기준으로, 가장 최근에 한 회의부터 리스팅 될 수 있도록
         *  meeting_room_arr arrayList sort 하기 */
        Collections.sort(meeting_room_arr, new Comparator<Meeting_room>() {
            @Override
            public int compare(Meeting_room o1, Meeting_room o2) {
//                                Log.d(TAG, "o1.getLast_log().getMsg_no(): " + o1.getLast_log().getMsg_no());
//                                Log.d(TAG, "o2.getLast_log().getMsg_no(): " + o2.getLast_log().getMsg_no());
                if(Integer.parseInt(o1.getMeeting_no()) < Integer.parseInt(o2.getMeeting_no())) {
                    return 1;
                }
                else if(Integer.parseInt(o1.getMeeting_no()) > Integer.parseInt(o2.getMeeting_no())) {
                    return -1;
                }
                else {
                    return 0;
                }
            }
        });

        // 어댑터가 생성되지 않았을 때 -> 어댑터를 생성
        if(rcv_project_meeting_result_list_adapter == null) {
            // 매개변수 1. this context
            // 매개변수 2. 인플레이팅 되는 리사이클러뷰 item 레이아웃
            // 매개변수 3. 리사이클러뷰에 표시될 데이터 어레이리스트 - meeting_room
            // 매개변수 4. 이 프로젝트의 color
            rcv_project_meeting_result_list_adapter = new RCV_project_meeting_result_list_adapter(
                    this, R.layout.i_meeting_result_brief, meeting_room_arr, project.getProject_color());

            recyclerView.setAdapter(rcv_project_meeting_result_list_adapter);
            rcv_project_meeting_result_list_adapter.notifyDataSetChanged();
        }
        // 어댑터가 생성되어 있을때는, 들어가는 arrayList만 교체
        else if(rcv_project_meeting_result_list_adapter != null) {
            rcv_project_meeting_result_list_adapter.refresh_arr(meeting_room_arr);
        }
    }



    /**---------------------------------------------------------------------------
     콜백메소드 ==> onActivityResult
     ---------------------------------------------------------------------------*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Meeting_result_D 액티비티로 부터 돌아왔을 때
        if(requestCode==REQUEST_OPEN_THIS_MEETING_RESULT && resultCode == RESULT_OK) {
            boolean resultOK = data.getBooleanExtra("resultOK", false);
            if(resultOK) {
                // 서버로부터 meeting_room_arr 리스트 받아와서, 어댑터로 넘기는 내부 메소드 호출
                activate_RCV();
            }
        }
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
