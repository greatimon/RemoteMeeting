package com.example.jyn.remotemeeting.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.jyn.remotemeeting.Adapter.RCV_project_meeting_result_list_adapter;
import com.example.jyn.remotemeeting.DataClass.Meeting_room;
import com.example.jyn.remotemeeting.DataClass.Project;
import com.example.jyn.remotemeeting.Etc.Static;
import com.example.jyn.remotemeeting.R;
import com.example.jyn.remotemeeting.Util.Myapp;
import com.example.jyn.remotemeeting.Util.SimpleDividerItemDecoration;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by JYN on 2018-01-23.
 */

public class Project_meeting_result_list_A extends Activity {


    private static final String TAG = "all_"+Project_meeting_result_list_A.class.getSimpleName();
    Myapp myapp;
    public static final int REQUEST_OPEN_THIS_MEETING_RESULT = 1111;
    public static final int REQUEST_MODIFY_PROJECT = 2222;

    /** 버터나이프 */
    public Unbinder unbinder;
    @BindView(R.id.actionBar_LIN)                   LinearLayout actionBar_LIN;
    @BindView(R.id.project_folder_img)              ImageView project_folder_img;
    @BindView(R.id.project_start_dt_txt)            TextView project_start_dt_txt;
    @BindView(R.id.no_result)                       TextView no_result;
    @BindView(R.id.project_name_txt)                TextView project_name_txt;
    @BindView(R.id.modify_project)                  TextView modify_project;
    @BindView(R.id.meeting_result_recyclerView)     RecyclerView recyclerView;

    // 리사이클러뷰 관련 클래스
    public RCV_project_meeting_result_list_adapter rcv_project_meeting_result_list_adapter;
    public RecyclerView.LayoutManager layoutManager;

    // 프로젝트 객체
    Project project;

    /** 이 클래스를 호출한 클래스 SimpleName */
    String request_class;

    /**---------------------------------------------------------------------------
     생명주기 ==> onCreate
     ---------------------------------------------------------------------------*/
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_project_meeting_result_list);

        // 버터나이프 바인드
        unbinder = ButterKnife.bind(this);
        // 어플리케이션 객체 생성
        myapp = Myapp.getInstance();

        // 이 클래스를 호출한 클래스 인텐트 값으로 받기
        Intent get_intent = getIntent();
        request_class = get_intent.getStringExtra(Static.REQUEST_CLASS);

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
            // 해당 Color String 값으로, Color int value 값을 가져온다
            int color_value = myapp.project_color(project_color);
            // ActionBar 위치에 있는 레이아웃의 색을 변경한다.
            actionBar_LIN.setBackgroundColor(color_value);

            // Project 이름, 시작날짜를 셋팅한다
            project_start_dt_txt.setText(project.getProject_start_dt().replace("-", "."));
            project_name_txt.setText(project.getProject_name());

            // 프로젝트 미지정 회의목록을 보여주는 상황이라면
            if(project.getProject_no() == 0) {
                project_start_dt_txt.setText(
                        "미지정 회의결과: " + project.getMeeting_count() + "개");
                // 수정 버튼은 안보이게 처리, INVISIBLE
                modify_project.setVisibility(View.INVISIBLE);
            }
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
     클릭이벤트 ==> 프로젝트 수정하기
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.modify_project)
    public void modify_project(View view) {

        // TODO: redis - 클릭이벤트
        myapp.Redis_log_click_event(getClass().getSimpleName(), view);

        // TODO: redis - 화면 이동
        myapp.Redis_log_view_crossOver_from_to(
                getClass().getSimpleName(), Create_project_A.class.getSimpleName());

        Intent intent = new Intent(this, Create_project_A.class);
        intent.putExtra("from", "meeting_result_list");
        intent.putExtra("exist_project_no", project.getProject_no());
        intent.putExtra(Static.REQUEST_CLASS, getClass().getSimpleName());
        startActivityForResult(intent, REQUEST_MODIFY_PROJECT);
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 서버로부터 meeting_room_arr 리스트 받아와서, 어댑터로 넘기기
     ---------------------------------------------------------------------------*/
    public void activate_RCV() {
        // 서버로부터 해당 프로젝트에 지정된 회의결과 리스트 받아오기
        ArrayList<Meeting_room> meeting_room_arr = myapp.get_meeting_room_list(project.getProject_no());
        Log.d(TAG, "meeting_room_arr.size(): " + meeting_room_arr.size());

        // 회의 결과 리스트가 하나도 없을 때
        if(meeting_room_arr.size() == 0) {
            if(rcv_project_meeting_result_list_adapter != null) {
                rcv_project_meeting_result_list_adapter.clear_item_arr();
            }
            no_result.setVisibility(View.VISIBLE);
            recyclerView.setItemViewCacheSize(View.GONE);
        }
        // 회의 결과 리스트가 있을 때
        else if(meeting_room_arr.size() > 0) {
            no_result.setVisibility(View.GONE);
            recyclerView.setItemViewCacheSize(View.VISIBLE);

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
                // 기존에 있는 arr 아이템 초기화
                if(rcv_project_meeting_result_list_adapter != null) {
                    rcv_project_meeting_result_list_adapter.clear_item_arr();
                }
                // 서버로부터 meeting_room_arr 리스트 받아와서, 어댑터로 넘기는 내부 메소드 호출
                activate_RCV();
            }
        }
        // Create_project_A 액티비티로부터 돌아왔을 때, '프로젝트' 수정
        else if(requestCode==REQUEST_MODIFY_PROJECT && resultCode==RESULT_OK) {
            // intent로, 생성한 project 객체의 jsonString 받아옴
            String created_project_jsonString = data.getStringExtra("created_project_jsonString");
            Log.d(TAG, "created_project_jsonString (Main_after_login_A - onActivityResult): "
                    + created_project_jsonString);

            // jsonString을 Project.class로 파싱할 때 사용할 Gson객체 생성
            Gson gson = new Gson();

            project = gson.fromJson(created_project_jsonString, Project.class);
            Log.d(TAG, "created_project.getProject_color(): " + project.getProject_color());
            Log.d(TAG, "created_project.getProject_name(): " + project.getProject_name());

            // Project 객체에서 Color 값을 가져온다
            String project_color = project.getProject_color();
            // 해당 Color String 값으로, Color int value 값을 가져온다
            int color_value = myapp.project_color(project_color);
            // ActionBar 위치에 있는 레이아웃의 색을 변경한다.
            actionBar_LIN.setBackgroundColor(color_value);

            // Project 이름, 시작날짜를 셋팅한다
            project_start_dt_txt.setText(project.getProject_start_dt().replace("-", "."));
            project_name_txt.setText(project.getProject_name());
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
