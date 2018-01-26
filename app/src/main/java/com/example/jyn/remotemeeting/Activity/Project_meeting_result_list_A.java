package com.example.jyn.remotemeeting.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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
import com.example.jyn.remotemeeting.DataClass.Drawing_images_saveFile;
import com.example.jyn.remotemeeting.DataClass.Meeting_room;
import com.example.jyn.remotemeeting.DataClass.Project;
import com.example.jyn.remotemeeting.Etc.Static;
import com.example.jyn.remotemeeting.R;
import com.example.jyn.remotemeeting.Util.Myapp;
import com.example.jyn.remotemeeting.Util.RetrofitService;
import com.example.jyn.remotemeeting.Util.ServiceGenerator;
import com.example.jyn.remotemeeting.Util.SimpleDividerItemDecoration;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

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

        // 서버로부터 데이터 로딩 및 sotring되는 동안 ProgressDialog 보여주기
        myapp.show_progress(this, "loading");

       // 서버로부터 해당 프로젝트에 지정된 회의결과 리스트 받아오기
        ArrayList<Meeting_room> temp_meeting_room_arr = myapp.get_meeting_room_list(project.getProject_no());
        Log.d(TAG, "temp_meeting_room_arr.size(): " + temp_meeting_room_arr.size());

        // 서버로부터 받아온 회의결과의 각 데이터(메모, 스캔이미지, 업로드이미지, 드로잉이미지)가
        // 있는지 없는지 확인해서, 있으면 그 데이터를 Meeting_room 객체에 결과를 넣기
        ArrayList<Meeting_room> meeting_room_arr = check_exist_data_orNot(temp_meeting_room_arr);

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

        // ProgressDialog dismiss
        myapp.dismiss_progress();
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
     메소드 ==> 서버로부터 받아온 회의결과의 각 데이터(메모, 스캔이미지, 업로드이미지, 드로잉이미지)가
     있는지 없는지 확인해서, 있으면 그 데이터를 Meeting_room 객체에 결과를 넣는 메소드
     ---------------------------------------------------------------------------*/
    public ArrayList<Meeting_room> check_exist_data_orNot(ArrayList<Meeting_room> meeting_room_arr) {
        for(int i=0; i<meeting_room_arr.size(); i++) {
            // 임시 meeting_room 객체 생성
            Meeting_room temp_meeting_room = meeting_room_arr.get(i);

            //// 메모, 스캔 이미지 파일, 업로드 파일, 드로잉 파일이 존재여부 확인
            // 최초로 리스트가 보여질 때, 각 데이터가 있는지 없는지 확인해서 있으면 내용 가져오기
            if(temp_meeting_room.getMemo_state() == 0) {
                String check_memo = isMemo_exist(this, temp_meeting_room.getMeeting_no());
                // 메모가 있다면
                if(check_memo != null) {
                    meeting_room_arr.get(i).setMemo(check_memo);
                    meeting_room_arr.get(i).setMemo_state(1);
                }
                // 메모가 있다면
                else if(check_memo == null) {
                    meeting_room_arr.get(i).setMemo_state(-1);
                }
            }
            if(temp_meeting_room.getScanned_img_state() == 0) {
                String get_scanned_image_absolutePath = isScanned_image(this, temp_meeting_room.getMeeting_no());
                // 스캔한 이미지 파일이 있다면
                if(get_scanned_image_absolutePath != null) {
                    meeting_room_arr.get(i).setScanned_img_path(get_scanned_image_absolutePath);
                    meeting_room_arr.get(i).setScanned_img_state(1);
                }
                // 스캔한 이미지 파일이 있다면
                else if(get_scanned_image_absolutePath == null) {
                    meeting_room_arr.get(i).setScanned_img_state(-1);
                }
            }
            if(temp_meeting_room.getUplopaded_img_state() == 0) {
                String jsonString_uploaded_fileName = isUploaded_file(temp_meeting_room.getMeeting_no());
                // 업로드한 이미지 파일이 있다면
                if(jsonString_uploaded_fileName != null) {
                    meeting_room_arr.get(i).setUplopaded_img_fileNames(jsonString_uploaded_fileName);
                    meeting_room_arr.get(i).setUplopaded_img_state(1);
                }
                // 업로드한 이미지 파일이 없다면
                else if(jsonString_uploaded_fileName == null) {
                    meeting_room_arr.get(i).setUplopaded_img_state(-1);
                }
            }
            if(temp_meeting_room.getDrawing_img_state() == 0) {
                String isDrawing_image_fileName = isDrawing_image(this, temp_meeting_room.getMeeting_no());
                // 드로잉한 이미지 파일이 있다면
                if(isDrawing_image_fileName != null) {
                    meeting_room_arr.get(i).setDrawing_img_fileNames(isDrawing_image_fileName);
                    meeting_room_arr.get(i).setDrawing_img_state(1);
                }
                // 드로잉한 이미지 파일이 없다면
                else if(isDrawing_image_fileName == null) {
                    meeting_room_arr.get(i).setDrawing_img_state(-1);
                }
            }
        }

        return meeting_room_arr;
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 로컬 확인 -- 해당 영상회의 결과에, 저장한 메모가 존재하는지 확인
     ---------------------------------------------------------------------------*/
    private String isMemo_exist(Context context, String meeting_no) {
        /**
         - 쉐어드, 미팅 메모 -
         [key: 회의 번호] | [value: memo_edit_str]
         */
        SharedPreferences meeting_memo = context.getSharedPreferences(Static.MEETING_MEMO, MODE_PRIVATE);
        String get_memo_str = meeting_memo.getString(meeting_no, null);
        if(get_memo_str == null) {
            return null;
        }
        else {
            Log.d(TAG, "get_memo_str_["+ meeting_no +"]: " + get_memo_str);
            return get_memo_str;
        }
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 로컬 확인 -- 해당 영상회의 결과에, 저장한 '스캔한 이미지'가 있는지 확인
     ---------------------------------------------------------------------------*/
    private String isScanned_image(Context context, String meeting_no) {

        /**
         - 쉐어드, 스캔한 이미지파일 -
         [쉐어드 파일이름: Static.MEETING_SCANNED_IMAGE + Static.SPLIT + 회의 번호]
         [key: 회의 번호] | [value: 파일 절대 경로 + 파일이름]
         */
        SharedPreferences meeting_scanned_image =
                context.getSharedPreferences(Static.MEETING_SCANNED_IMAGE + Static.SPLIT + meeting_no, MODE_PRIVATE);
        String get_scanned_image_absolutePath = meeting_scanned_image.getString(meeting_no, null);
        if(get_scanned_image_absolutePath == null) {
            return null;
        }
        else {
            Log.d(TAG, "get_scanned_image_absolutePath_["+ meeting_no +"]: " + get_scanned_image_absolutePath);

            // 해당 위치에, 파일 실제 존재 여부 확인
            File f = new File(get_scanned_image_absolutePath);
            if (f.isFile()) {
                return get_scanned_image_absolutePath;
            }
            else {
                return null;
            }

        }
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 서버 통신 -- 해당 영상회의에서 업로드 했던 이미지파일들이 있었는지 확인
     ---------------------------------------------------------------------------*/
    @SuppressLint("StaticFieldLeak")
    private String isUploaded_file(final String meeting_no) {
        final RetrofitService rs = ServiceGenerator.createService(RetrofitService.class);

        // 동기 호출
        try {
            return new AsyncTask<Void, Void, String>() {

                // 통신 로직
                @Override
                protected String doInBackground(Void... voids) {

                    String return_this = null;

                    try {
                        Call<ResponseBody> call_result = rs.is_uploaded_file(
                                Static.IS_UPLOADED_FILE,
                                meeting_no);
                        Response<ResponseBody> list = call_result.execute();
                        String result = list.body().string();
                        Log.d(TAG, "retrofit_result: " + result);

                        // 에러
                        if(result.equals("fail")) {
                            myapp.logAndToast("예외발생: " + result);
                        }
                        // 업로드된 파일이 한개도 없는 경우
                        else if(result.equals("no_result")) {

                        }
                        // 업로드된 파일이 잇는 경우
                        else {
                            return_this = result;
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return return_this;
                }
            }.execute().get();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 로컬 확인 -- 해당 영상회의에서, 저장한 드로잉 이미지 파일이 있는지 확인
     ---------------------------------------------------------------------------*/
    private String isDrawing_image(Context context, String meeting_no) {

        /**
         - 쉐어드, 드로잉 이미지파일 -
         [key: 회의 번호] | [value: Drawing_images_saveFile_jsonString]
         */
        SharedPreferences drawing_imgs_fileName = context.getSharedPreferences(Static.DRAWING_IMGS_FOR_SHARED, MODE_PRIVATE);
        // 지금 참여하고 있는 회의 번호로 저장되어 있는 쉐어드 String 값을 찾아온다
        String drawing_file_str = drawing_imgs_fileName.getString(meeting_no, null);
        Log.d(TAG, "drawing_file_str_["+ meeting_no +"]: " + drawing_file_str);
        // Drawing_images_saveFile <--> String, 변환을 위한 Gson 선언
        Gson gson = new Gson();

        if(drawing_file_str == null) {
            return null;
        }
        else {
            // 해당 쉐어드 Str 값을 gson과 데이터 클래스를 이용하여, 데이터 객체로 파싱
            Drawing_images_saveFile drawing_images_saveFile = gson.fromJson(drawing_file_str, Drawing_images_saveFile.class);

            boolean there_is_no_file = false;

            for(int i=0; i<drawing_images_saveFile.file_nums(); i++) {
                // 해당 위치에, 파일 실제 존재 여부 확인
                File f = new File(drawing_images_saveFile.getDrawing_images_fileName_arr().get(i));
                if (!f.isFile()) {
                    there_is_no_file = true;
                    Log.d(TAG, drawing_images_saveFile.getDrawing_images_fileName_arr().get(i) + "_ 파일이 존재하지 않음");
                    break;
                }
            }

            if(there_is_no_file) {
                return null;
            }
            else {
                return drawing_file_str;
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
