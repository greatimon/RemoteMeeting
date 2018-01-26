package com.example.jyn.remotemeeting.Dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.jyn.remotemeeting.Activity.Create_project_A;
import com.example.jyn.remotemeeting.Adapter.RCV_show_drawing_images_adapter;
import com.example.jyn.remotemeeting.Adapter.RCV_show_uploaded_images_adapter;
import com.example.jyn.remotemeeting.DataClass.Drawing_images_saveFile;
import com.example.jyn.remotemeeting.DataClass.File_info;
import com.example.jyn.remotemeeting.DataClass.Meeting_room;
import com.example.jyn.remotemeeting.DataClass.Project;
import com.example.jyn.remotemeeting.DataClass.Users;
import com.example.jyn.remotemeeting.Etc.Static;
import com.example.jyn.remotemeeting.R;
import com.example.jyn.remotemeeting.Recycler_helper.GridLayout_itemOffsetDecoration_rcv;
import com.example.jyn.remotemeeting.Util.Myapp;
import com.example.jyn.remotemeeting.Util.RetrofitService;
import com.example.jyn.remotemeeting.Util.ServiceGenerator;
import com.google.gson.Gson;
import com.scanlibrary.ScanConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.wasabeef.glide.transformations.CropCircleTransformation;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by JYN on 2018-01-19.
 *
 * # 영상회의 종료 후, 팝업되는 다이얼로그 액티비티
 *
 * - 간략한 회의 결과를 보여주고
 * - 추가적으로 메모할 부분이 있으면 메모할 수 있게 EditText 가 있음
 * - 이 외에 OpenCV를 이용한 'Image To Document' 기능 추가
 *   : 회의 중 종이에 메모한 내용이 있으면 그 종이를 카메라로 사진을 찍어,
 *     그 종이의 네 모퉁이를 잘라내어, 평평하게 스캔하는 효과를 내는 라이브러리(기능)
 */

public class Meeting_result_D extends Activity {

    private static final String TAG = "all_"+Meeting_result_D.class.getSimpleName();
    private String JSON_TAG_ENDED_MEETING_RESULT = "ended_meeting_result";
    private static final int REQUEST_IMAGE_SCAN_TO_DOCUMENT = 9999;
    private static final int REQUEST_SELECT_METHOD_FOR_ASSIGN_PROJECT = 1234;
    private static final int REQUEST_ASSIGN_TO_EXISTING_PROJECT = 4321;
    public static final int REQUEST_SHOW_THIS_IMAGE = 6565;
    public static final int REQUEST_CREATE_PROJECT_FROM_MEETING_RESULT_D = 6537;
    Myapp myapp;
    // 회의 번호
    String target_meeting_no;
    // 회의 대상
    String target_subject_user_no;
    // Meeting_result_D 액티비티를 연 위치
    String opened_from;

    /** 이 클래스를 호출한 클래스 SimpleName */
    String request_class;

    /** 버터나이프*/
    public Unbinder unbinder;
    @BindView(R.id.actionBar_LIN)                           LinearLayout actionBar_LIN;
    @BindView(R.id.meeting_basic_info_LIN)                  LinearLayout meeting_basic_info_LIN;
    @BindView(R.id.project_assign_LIN)                      LinearLayout project_assign_LIN;
    @BindView(R.id.meeting_memo_LIN)                        LinearLayout meeting_memo_LIN;
    @BindView(R.id.handwriting_to_document_LIN)             LinearLayout handwriting_to_document_LIN;
    @BindView(R.id.handwriting_to_document_LIN_for_add_img) LinearLayout handwriting_to_document_LIN_for_add_img;
    @BindView(R.id.drawing_images_LIN)                      LinearLayout drawing_images_LIN;
    @BindView(R.id.meeting_upload_images_LIN)               LinearLayout meeting_upload_images_LIN;
    @BindView(R.id.meeting_subject_user_nickName_txt)       TextView meeting_subject_user_nickName_txt;
    @BindView(R.id.save_meeting_result_txt)                 TextView save_meeting_result_txt;
    @BindView(R.id.meeting_title_txt)                       TextView meeting_title_txt;
    @BindView(R.id.today_txt)                               TextView today_txt;
    @BindView(R.id.meeting_start_time_txt)                  TextView meeting_start_time_txt;
    @BindView(R.id.meeting_end_time_txt)                    TextView meeting_end_time_txt;
    @BindView(R.id.total_meeting_time_txt)                  TextView total_meeting_time_txt;
    @BindView(R.id.project_name_txt)                        TextView project_name_txt;
    @BindView(R.id.meeting_subject_user_profile_img)        ImageView meeting_subject_user_profile_img;
    @BindView(R.id.project_folder_img)                      ImageView project_folder_img;
    @BindView(R.id.scanned_img)                             ImageView scanned_img;
    @BindView(R.id.memo_edit)                               EditText memo_edit;
//    @BindView(R.id.handwriting_to_document_rcv)         RecyclerView handwriting_to_document_rcv;
    @BindView(R.id.upload_images_rcv)                       RecyclerView upload_images_rcv;
    @BindView(R.id.drawing_images_rcv)                      RecyclerView drawing_images_rcv;
    @BindView(R.id.drawing_images_divider)                  View drawing_images_divider;

    // 서버로부터 받아온 회의 결과(jsonString)
    public String jsonString_meeting_result;

    // 서버로부터 받아온, 회의 결과를 담을 'meeting_room' 객체(데이터 클래스)
    private Meeting_room ended_meeting_room;

    // 서버로부터 받아온, 회의 중 업로드한 이미지 파일들의 파일이름을 담을 어레이리스트(요소: 데이터 클래스)
    private ArrayList<File_info> share_img_file_name_arr;

    // 서버로부터 받아온, 회의 대상의 유저 정보를 담을 객체(데이터 클래스)
    private Users subject_user;

    // 손글씨 사진 uri 값을 임시로 담을 해쉬
    private ConcurrentHashMap<Integer, Uri> temp_uri_hash;

    // 지정된 프로젝트의 'project_no' (default 값: -1)
    private int selected_project_no = -1;

    // 아직 서버에 회의 종료 시각이 insert 되기 전이라, 최초 받아온 정보에 회의 종료 시각, 총 회의 시간이 없을때,
    // 회의 종료 시각, 총 회의 시간을 받아오는 메소드를 다시 호출하여 결과 값을 받아 왔을 때,
    // 아직도 DB에 결과가 없을 경우, 다시 정보를 받아오는 메소드를 호출하기 위한 핸들러
    public Handler meeting_end_time_handler;
    // 나중에 추가적으로 DB에서 '회의 종료 시각, 총 회의 시간'을 받아왔을 경우 저장하기 위한 전역 변수
    String receive_delayed_meeting_end_time;
    String receive_delayed_total_meeting_time;
    // 회의 종료시각, 총 회의 시간을 최초에 받아오지 못했을 때, 총 5번만 더 시도하기 위해, 시도 횟수를 세는 변수
    int get_meeting_end_time_try_count;

    // 스캔한 이미지 파일이 있을경우 -- 'remoteMeeting' 폴더로 이미지 파일 복사가 완료됨을 확인
    public Handler saveFile_scanned_images_handler;

    // 리사이클러뷰 관련 변수, 클래스 선언 =========
    // =========================================
    public RCV_show_uploaded_images_adapter rcv_upload_images_adapter;
    public RCV_show_drawing_images_adapter rcv_drawing_images_adapter;



    /**---------------------------------------------------------------------------
     생명주기 ==> onCreate
     ---------------------------------------------------------------------------*/
    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.v_meeting_result);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        this.setFinishOnTouchOutside(false);

        // 버터나이프 바인드
        unbinder = ButterKnife.bind(this);
        // 어플리케이션 객체 생성
        myapp = Myapp.getInstance();

        // 이 클래스를 호출한 클래스 인텐트 값으로 받기
        Intent get_intent = getIntent();
        request_class = get_intent.getStringExtra(Static.REQUEST_CLASS);

        Intent intent = getIntent();

        // 인텐트로 넘어온 'meeting_no'를 가져와서 전역변수에 넣는다
        target_meeting_no = intent.getStringExtra("meeting_no");
        Log.d(TAG, "target_meeting_no: " + target_meeting_no);

        // 인텐트로 넘어온, 'from' 가져와서 전역변수에 넣는다
        opened_from = intent.getStringExtra("from");
        Log.d(TAG, "opened_from: " + opened_from);

        // 인텐트로 넘어온 'subject_user_no'를 가져와서 전역변수에 넣는다
        target_subject_user_no = intent.getStringExtra("subject_user_no");
        Log.d(TAG, "target_subject_user_no: " + target_subject_user_no);

        // 손글씨 메모 스캔한 사진의 uri 값들을 임시로 담아놓을 해쉬맵
        temp_uri_hash = new ConcurrentHashMap<>();


        // 핸들러 생성
        meeting_end_time_handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                // 아직 종료한 회의의, 회의 종료 시각과 총 회의 시간을 받아 오지 못했을 때
                // 다시 서버로부터 해당 데이터들을 받아오는 메소드를 호출한다
                // 0.1초 뒤에
                if(msg.what == 0) {

                    Log.d(TAG, "get_meeting_end_time_try_count: " + get_meeting_end_time_try_count);
                    // 5번만 시도함
                    if(get_meeting_end_time_try_count < 5) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                get_meeting_end_time();
                                get_meeting_end_time_try_count++;
                            }
                        }, 100);
                    }
                }
                // 회의 종료 시각과 총 회의 시간을 받아왔을 때, 해당 데이터들을 뷰에 셋팅한다
                else if(msg.what == 1) {
                    meeting_end_time_txt.setText(receive_delayed_meeting_end_time);
                    total_meeting_time_txt.setText(receive_delayed_total_meeting_time);
                }
            }
        };
        saveFile_scanned_images_handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                // 스캔한 이미지 파일이 있어서, 'remoteMeeting' 폴더로 파일 복사하는 작업을 완료하고 나서
                // 완료됐음을 알리는 핸들러 메세지
                // 다음 로직을 호출
                if(msg.what == 0) {
                    save_meeting_result();
                }
            }
        };

        // 혹시 있을수도 있는 공유 파일 이미지들의 파일이름을 담을 어레이리스트 생성
        share_img_file_name_arr = new ArrayList<>();

        // 종료된 영상회의의 정보를 가져오는 어플리케이션 객체 내 메소드 호출
        jsonString_meeting_result = myapp.get_ended_meeting_result(target_meeting_no, target_subject_user_no);

        // jsonString_meeting_result --> 필요한 정보에 따라 알맞게 파싱하는 내부 메소드 호출
        // 파싱한 뒤, 종료된 영상회의를 뷰에 셋팅
        parsing_server_data(jsonString_meeting_result);

        /** 서버로부터, 회의 때 업로드 했던 파일 리스트들의 name 받아오기 */
        ArrayList<File_info> files = myapp.get_uploaded_file_list(this, target_meeting_no);
        Log.d(TAG, "files.isEmpty(): " + files.isEmpty());
        // 파일 리스트가 있다면
        if(!files.isEmpty()) {

            // 해당 파일을 보여줄 리사이클러뷰를 담고 있는 LinearLayout 'Visible' 처리 하기
            meeting_upload_images_LIN.setVisibility(View.VISIBLE);

            Log.d(TAG, "어댑터에 넘길 files 개수: " + files.size());
            // 생성자 인수
            // 1. 액티비티
            // 2. 인플레이팅 되는 레이아웃
            // 3. arrayList 데이터
            // 4. 변별 변수
            rcv_upload_images_adapter
                    = new RCV_show_uploaded_images_adapter(this, R.layout.i_uploaded_images, files, "meeting_result");

            // 그리드 레이아수 적용, 한줄에 2개
            upload_images_rcv.setLayoutManager(new GridLayoutManager(this, 2));

            // 아이템 간 일정한 패딩을 주기 위한 ItemDecoration
            GridLayout_itemOffsetDecoration_rcv itemDecoration
                    = new GridLayout_itemOffsetDecoration_rcv(this, R.dimen.item_offset);
            upload_images_rcv.addItemDecoration(itemDecoration);

            // set Adapter
            upload_images_rcv.setAdapter(rcv_upload_images_adapter);
            rcv_upload_images_adapter.notifyDataSetChanged();
        }

        /** 해당 영상회의에서 저장했던, 파일 이름 리스트들 쉐어드로부터 가져오기 */
        Gson gson = new Gson();
        SharedPreferences drawing_imgs_fileName = getSharedPreferences(Static.DRAWING_IMGS_FOR_SHARED, MODE_PRIVATE);
        // 지금 참여하고 있는 회의 번호로 저장되어 있는 쉐어드 String 값을 찾아온다
        String drawing_file_str = drawing_imgs_fileName.getString(target_meeting_no, "");
        // 지금 참여하고 있는 회의번호로 저장되어 있는 쉐어드 값이 있다면,
        if(!drawing_file_str.equals("")) {

            // 해당 쉐어드 Str 값을 gson과 데이터 클래스를 이용하여, 데이터 객체로 파싱하여, 어댑터로 넘긴다
            Drawing_images_saveFile drawing_images_saveFile = gson.fromJson(drawing_file_str, Drawing_images_saveFile.class);
//            Log.d(TAG, "drawing_images_saveFile.getMeeting_no(): "
//                    + drawing_images_saveFile.getMeeting_no());
//            Log.d(TAG, "drawing_images_saveFile.getDrawing_images_fileName_arr(): "
//                    + drawing_images_saveFile.getDrawing_images_fileName_arr().toString());

            ArrayList<String> drawing_imgs_fileName_arr = drawing_images_saveFile.getDrawing_images_fileName_arr();

            // 실제 파일이 있는지 없는지 확인하기
            boolean there_is_file = false;

            for(int i=0; i<drawing_imgs_fileName_arr.size(); i++) {
                File f = new File(drawing_imgs_fileName_arr.get(i));
                if(f.isFile()) {
                    there_is_file = true;
                    Log.d(TAG, drawing_images_saveFile.getDrawing_images_fileName_arr().get(i) + "_ 파일이 존재하지 않음");
                }
            }

            // 한개의 이미지 파일이라도 디렉토리에 존재한다면,
            // // 해당 파일을 보여줄 리사이클러뷰를 담고 있는 LinearLayout, divider Visibility 'Visible' 처리 하기
            if(there_is_file) {
                drawing_images_LIN.setVisibility(View.VISIBLE);
                drawing_images_divider.setVisibility(View.VISIBLE);
            }

            Log.d(TAG, "어댑터에 넘길 files 개수: " + drawing_imgs_fileName_arr.size());
            // 생성자 인수
            // 1. 액티비티
            // 2. 인플레이팅 되는 레이아웃
            // 3. arrayList 데이터
            rcv_drawing_images_adapter
                    = new RCV_show_drawing_images_adapter(this, R.layout.i_uploaded_images, drawing_imgs_fileName_arr);

            // 그리드 레이아수 적용, 한줄에 2개
            drawing_images_rcv.setLayoutManager(new GridLayoutManager(this, 2));

            // 아이템 간 일정한 패딩을 주기 위한 ItemDecoration
            GridLayout_itemOffsetDecoration_rcv itemDecoration
                    = new GridLayout_itemOffsetDecoration_rcv(this, R.dimen.item_offset);
            drawing_images_rcv.addItemDecoration(itemDecoration);

            // set Adapter
            drawing_images_rcv.setAdapter(rcv_drawing_images_adapter);
            rcv_drawing_images_adapter.notifyDataSetChanged();
        }

        /**
            프로젝트 폴더의 리스트 아이템을 클릭해서 이 액티비티를 열었다면, 추가로
             1. 액션바 위치의 '저장하기' 를 '수정하기'로 변경하기
             2. 쉐어드에 저장되어 있는 메모있는지 확인하기
             3. 쉐어드에 '손필기 메모 스캔'한 이미지가 있는지 확인하기(실제로 파일 존재하는지도 확인)
             4. 지정한 프로젝트가 있는지 확인하기(DB에서 확인)
         */
        if(opened_from.equals(Static.PROJECT_FOLDER)) {
            // 1. 액션바 위치의 '저장하기' 를 '수정하기'로 변경하기
            save_meeting_result_txt.setText("수정완료");

            // 2. 쉐어드에 저장되어 있는 메모있는지 확인하기 / 있으면 EditText에 setText 하기
            /**
             - 쉐어드, 미팅 메모 -
             [key: 회의 번호] | [value: memo_edit_str]
             */
            SharedPreferences meeting_memo = getSharedPreferences(Static.MEETING_MEMO, MODE_PRIVATE);
            String get_memo_str = meeting_memo.getString(target_meeting_no, null);
            if(get_memo_str != null) {
                Log.d(TAG, "get_memo_str_["+ target_meeting_no +"]: " + get_memo_str);
                memo_edit.setText(get_memo_str);
                // EditText 커서의 위치를 맨 마지막에 놓기
                memo_edit.setSelection(memo_edit.length());
            }

            // 3. 쉐어드에 '손필기 메모 스캔'한 이미지가 있는지 확인하기(실제로 파일 존재하는지도 확인)
            /**
             - 쉐어드, 스캔한 이미지파일 -
             [쉐어드 파일이름: Static.MEETING_SCANNED_IMAGE + Static.SPLIT + 회의 번호]
             [key: 회의 번호] | [value: 파일 절대 경로 + 파일이름]
             */
            SharedPreferences meeting_scanned_image =
                    getSharedPreferences(Static.MEETING_SCANNED_IMAGE + Static.SPLIT + target_meeting_no, MODE_PRIVATE);
            String get_scanned_image_absolutePath = meeting_scanned_image.getString(target_meeting_no, null);
            if(get_scanned_image_absolutePath != null) {
                Log.d(TAG, "get_scanned_image_absolutePath: " + get_scanned_image_absolutePath);
                // 해당 위치에, 파일 실제 존재 여부 확인
                File f = new File(get_scanned_image_absolutePath);
                if (f.isFile()) {

                    /** add할, ImageView 및 ImageView 속성 셋팅을 위한 LinearLayout.LayoutParams 생성 */
                    LinearLayout.LayoutParams vp =
                            new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT);
                    ImageView imageView = new ImageView(this);
                    // 마진 셋팅
                    vp.setMargins(0, 20, 0, 0);
                    // 패딩 셋팅
                    imageView.setPadding(0, 0, 20, 0);
                    // width, height 속성 및 마진 속성 적용
                    imageView.setLayoutParams(vp);
                    // 이미지 비율에 맞게 Height 조절되는 속성 적용
                    imageView.setAdjustViewBounds(true);
                    Glide
                        .with(this)
                        .load(get_scanned_image_absolutePath)
                        .into(imageView);

                    // 최종 셋팅된 ImageView를 '손글씨 메모 스캔' 부모뷰에 add 하기
                    handwriting_to_document_LIN_for_add_img.addView(imageView);
                }
            }

            // 4. 지정한 프로젝트가 있는지 확인하기(DB에서 확인)
            Project assigned_project = myapp.assigned_project(target_meeting_no);

            // 에러인 경우
            if(assigned_project.getProject_no() == -1) {}
            // 지정된 프로젝트가 없는 경우
            else if(assigned_project.getProject_no() == 0) {}

            // 지정된 프로젝트가 있는 경우
            else if(assigned_project.getProject_no() != -1 && assigned_project.getProject_no() != 0) {
                selected_project_no = assigned_project.getProject_no();
                String selected_project_color = assigned_project.getProject_color();
                String selected_project_name = assigned_project.getProject_name();

                // 지정된 프로젝트 정보에 따라 컬러 변경 및, 프로젝트 폴더 아이콘과 이름을 set 하는 내부 메소드 호출
                set_assign_project_result(selected_project_color, selected_project_name);
            }
        }
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 서버로부터 받은, 종료된 회의 정보를 알맞게 파싱하여, 전역변수에 넣는다
     ---------------------------------------------------------------------------*/
    private void parsing_server_data(String result) {
        try {
            // jsonString --> jsonObject
            JSONObject jsonObject = new JSONObject(result);
            // jsonObject --> jsonArray
            JSONArray jsonArray = jsonObject.getJSONArray(JSON_TAG_ENDED_MEETING_RESULT);
            Log.d(TAG, "jsonArray 개수: " + jsonArray.length());

            // 데이터 클래스로 파싱하기 위한 GSON 객체 생성
            Gson gson = new Gson();

            // jsonArray에서 jsonObject를 가지고 와서, parsing 하기
            if(jsonArray.length() == 1) {

                /** ended_meeting_room, 파싱 */
                //// 종료된 회의정보 parsing 해서 어레이에 넣기
                // 'specified_project_data' JSONString을 JSONObect로 파싱
                JSONArray ended_meeting_info_JsArr = new JSONArray(jsonArray.getJSONObject(0).getString("ended_meeting_info_arr"));
                // gson 이용해서 meeting_room 객체로 변환
                if(ended_meeting_info_JsArr.length() == 1) {
                    ended_meeting_room = gson.fromJson(ended_meeting_info_JsArr.get(0).toString(), Meeting_room.class);
                    // 서버로부터 받은 데이터가 잘 파싱되어 있는지 확인하기 위한 Log
                    Log.d(TAG, "ended_meeting_room.getMeeting_no(): " + ended_meeting_room.getMeeting_no());
                    Log.d(TAG, "ended_meeting_room.getReal_meeting_title(): " + ended_meeting_room.getReal_meeting_title());
                    Log.d(TAG, "ended_meeting_room.getTransform_meeting_title(): " + ended_meeting_room.getTransform_meeting_title());
                    Log.d(TAG, "ended_meeting_room.getMeeting_creator_user_no(): " + ended_meeting_room.getMeeting_creator_user_no());
                    Log.d(TAG, "ended_meeting_room.getMeeting_subject_user_no(): " + ended_meeting_room.getMeeting_subject_user_no());
                    Log.d(TAG, "ended_meeting_room.getMeeting_authority_user_no(): " + ended_meeting_room.getMeeting_authority_user_no());
                    Log.d(TAG, "ended_meeting_room.getMeeting_start_time(): " + ended_meeting_room.getMeeting_start_time());
                    Log.d(TAG, "ended_meeting_room.getMeeting_end_time(): " + ended_meeting_room.getMeeting_end_time());
                    Log.d(TAG, "ended_meeting_room.getProject_no(): " + ended_meeting_room.getProject_no());
                    Log.d(TAG, "ended_meeting_room.getMeeting_status(): " + ended_meeting_room.getMeeting_status());
                }

                /** subject_user, 파싱 */
                JSONArray subject_user_JsArr = new JSONArray(jsonArray.getJSONObject(0).getString("subject_user_info_arr"));
                // gson 이용해서 Users 객체로 변환
                if(subject_user_JsArr.length() == 1) {
                    subject_user = gson.fromJson(subject_user_JsArr.get(0).toString(), Users.class);
                    // 서버로부터 받은 데이터가 잘 파싱되어 있는지 확인하기 위한 Log
                    Log.d(TAG, "subject_user.getUser_nickname(): " + subject_user.getUser_nickname());
                    Log.d(TAG, "subject_user.getUser_email(): " + subject_user.getUser_email());
                }

                /** share_img_file_name_arr, 파싱 */
                JSONArray share_img_file_name_JsArr = new JSONArray(jsonArray.getJSONObject(0).getString("share_img_file_name_arr"));
                Log.d(TAG, "share_img_file_name_JsArr.length(): " + share_img_file_name_JsArr.length());
                // gson 이용해서 File_info 객체로 변환, 어레이리스트에 add
                if(share_img_file_name_JsArr.length() > 0) {
                    for(int k=0; k<share_img_file_name_JsArr.length(); k++) {

                        File_info received_file_info = gson.fromJson(share_img_file_name_JsArr.get(k).toString(), File_info.class);
                        share_img_file_name_arr.add(received_file_info);

                        // 서버로부터 받은 데이터가 잘 파싱되어 있는지 확인하기 위한 Log
                        Log.d(TAG, "received_file_info.getFile_name(): " + received_file_info.getFile_name());
                    }
                    Log.d(TAG, "share_img_file_name_arr.size(): " + share_img_file_name_arr.size());
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        finally {
            // 종료된 영상회의를 뷰에 셋팅
            set_ended_meeting_result_to_view();
        }
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 파싱한, 종료된 회의 데이터를 뷰에 셋팅한다
     ---------------------------------------------------------------------------*/
    @SuppressLint("SetTextI18n")
    private void set_ended_meeting_result_to_view() {
        // 회의 제목
        meeting_title_txt.setText(ended_meeting_room.getReal_meeting_title());

        // 회의 대상 닉네임
        meeting_subject_user_nickName_txt.setText("회의 대상: " + subject_user.getUser_nickname());

        // 회의 대상 프로필 사진
        Glide
            .with(this)
            .load(Static.SERVER_URL_PROFILE_FILE_FOLDER + subject_user.getUser_img_filename())
            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
            .bitmapTransform(new CropCircleTransformation(this))
            .into(meeting_subject_user_profile_img);

        // '2018-01-20 09:07:06' 형식의 Meeting_start_time을, 공백(' ')으로 스플릿 한다
        // 스플릿 결과 1. '2018-01-20'
        // 스플릿 결과 1. '09:07:06'
        String[] temp_1 = ended_meeting_room.getMeeting_start_time().split("[ ]");

        // 회의 '년월일'
        String[] temp_2 = temp_1[0].split("[-]");
        String modified_date = temp_2[0] + "년 " + temp_2[1] + "월 " + temp_2[2] + "일";
        today_txt.setText(modified_date);

        // 회의 시작 시각
        String[] temp_3 = temp_1[1].split("[:]");
        String modified_start_time = temp_3[0] + "시 " + temp_3[1] + "분";
        meeting_start_time_txt.setText(modified_start_time);

        // 회의 종료 시각 and 총 회의 시간
        // 만약, 회의 종료 시각이 받아온 정보에 없다면, 종료시각만 다시 받아오는 내부 메소드를 호출한다
        // (0.3초 뒤에 실행)
        // 회의 종료 시각은, 아래 호출하는 내부 메소드내에서 데이터 셋팅
        Log.d(TAG, "ended_meeting_room.getMeeting_end_time(): " + ended_meeting_room.getMeeting_end_time());
        Log.d(TAG, "ended_meeting_room.getTotal_meeting_time(): " + ended_meeting_room.getTotal_meeting_time());
        if(ended_meeting_room.getMeeting_end_time().equals("") ||
                ended_meeting_room.getTotal_meeting_time().equals("")) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    get_meeting_end_time();
                }
            }, 300);
        }
        // 회의 종료시각/총 회의시간이 받아온 정보에 있다면, 바로 뷰에 셋팅한다
        else {
            // '2018-01-20 09:07:06' 형식의 Meeting_end_time을, 공백(' ')으로 스플릿 한다
            // 스플릿 결과 1. '2018-01-20'
            // 스플릿 결과 1. '09:07:06'
            String[] temp_4 = ended_meeting_room.getMeeting_end_time().split("[ ]");
            Log.d(TAG, "ended_meeting_room.getMeeting_end_time()[0]: " + temp_4[0]);
            Log.d(TAG, "ended_meeting_room.getMeeting_end_time()[1]: " + temp_4[1]);

            // 회의 종료 시각
            String[] temp_5 = temp_4[1].split("[:]");
            Log.d(TAG, "ended_meeting_room.getTotal_meeting_time()[0]: " + temp_5[0]);
            Log.d(TAG, "ended_meeting_room.getTotal_meeting_time()[0]: " + temp_5[1]);
            Log.d(TAG, "ended_meeting_room.getTotal_meeting_time()[0]: " + temp_5[2]);
            String modified_end_time = temp_5[0] + "시 " + temp_5[1] + "분";

            // 회의 종료 시각
            receive_delayed_meeting_end_time = modified_end_time;
            // 총 회의 시간
            receive_delayed_total_meeting_time = ended_meeting_room.getTotal_meeting_time();
            // 원하는 데이터를 잘 받아 왔음을 핸들러를 통해 알리기
            meeting_end_time_handler.sendEmptyMessage(1);
        }

    }


    /**---------------------------------------------------------------------------
     메소드 ==> 회의 종료 시각 받아오기, 종료시각을 받아올때까지 반복 호출 (재귀 메소드)
     ---------------------------------------------------------------------------*/
    @SuppressLint("StaticFieldLeak")
    private void get_meeting_end_time() {
        final RetrofitService rs = ServiceGenerator.createService(RetrofitService.class);
        // 동기 호출
        try {
            new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... voids) {
                    try {
                        Call<ResponseBody> call_result = rs.get_meeting_end_time(
                                Static.GET_MEETING_END_TIME,
                                target_meeting_no);
                        Response<ResponseBody> list = call_result.execute();
                        String result = list.body().string();
                        Log.d(TAG, "get_meeting_end_time_ result: " + result);

                        if(result.equals("fail")) {
                            myapp.logAndToast("예외발생: " + result);
                        }
                        // 아직도 서버에 저장이 안됐음을 핸들러를 통해 알리기
                        else if(result.equals("yet")) {
                            meeting_end_time_handler.sendEmptyMessage(0);
                        }
                        else {
                            String[] temp = result.split(Static.SPLIT);
                            String temp_meeting_end_time = temp[0];
                            String temp_total_meeting_time = temp[1];
                            Log.d(TAG, "temp_meeting_end_time: " + temp_meeting_end_time);
                            Log.d(TAG, "temp_total_meeting_time: " + temp_total_meeting_time);
                            // '2018-01-20 09:07:06' 형식의 Meeting_end_time을, 공백(' ')으로 스플릿 한다
                            // 스플릿 결과 1. '2018-01-20'
                            // 스플릿 결과 1. '09:07:06'
                            String[] temp_1 = temp_meeting_end_time.split("[ ]");
                            Log.d(TAG, "temp_meeting_end_time[0]: " + temp_1[0]);
                            Log.d(TAG, "temp_meeting_end_time[1]: " + temp_1[1]);

                            // 회의 종료 시각
                            String[] temp_2 = temp_1[1].split("[:]");
                            Log.d(TAG, "temp_2[0]: " + temp_2[0]);
                            Log.d(TAG, "temp_2[0]: " + temp_2[1]);
                            Log.d(TAG, "temp_2[0]: " + temp_2[2]);
                            String modified_end_time = temp_2[0] + "시 " + temp_2[1] + "분";

                            // 회의 종료 시각
                            receive_delayed_meeting_end_time = modified_end_time;
                            // 총 회의 시간
                            receive_delayed_total_meeting_time = temp_total_meeting_time;
                            // 원하는 데이터를 잘 받아 왔음을 핸들러를 통해 알리기
                            meeting_end_time_handler.sendEmptyMessage(1);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }.execute();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 회의 결과 저장하기 or 수정완료 버튼 클릭
        -- 스캔한 이미지가 있는지 없는지 확인하여, 경우에 따라 맞는 메소드 및 핸들러 메세지 전달
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.save_meeting_result_txt)
    public void check_there_is_scanned_image_orNot(View view) {
        // TODO: redis - 클릭이벤트
        myapp.Redis_log_click_event(getClass().getSimpleName(), view);

        //// 회의 끝나고, 이 액티비티가 팝업된 경우라면
        // 작성한 목록이 하나도 없다면
        if(!check_have_any_list_i_have_created() && !opened_from.equals(Static.PROJECT_FOLDER)) {
            // 해당 회의를 프로젝트에 지정하여 서버에 내용 전송하고
            myapp.assign_project(selected_project_no, target_meeting_no);
            myapp.logAndToast("'기본' 회의결과가 저장되었습니다.");
            finish();
        }
        // 작성한 목록이 하나라도 있다면
        else if(check_have_any_list_i_have_created() && opened_from.equals(Static.RIGHT_AFTER_END_MEETING)) {
            // 스캔한 이미지가 있음
            if(temp_uri_hash.size() != 0) {

                // 스캔 이미지 복사 중에, progressDialog 보여주기
                myapp.show_progress(this, "스캔 이미지 저장 중..");
                // 스캔한 이미지 파일을 '/storage/emulated/0/remoteMeeting/' 경로로 복사하고
                // 원래 파일은 삭제(경로: /storage/emulated/0/Pictures/) 하는 내부 메소드 호출
                saveFile_scanned_images();
            }
            // 스캔한 이미지가 없음
            else if(temp_uri_hash.size() == 0) {
                // 내부 메소드 호출
                save_meeting_result();
            }
        }
        //// 프로젝트 폴더의 아이템 리스트로 부터 클릭해서, 이 액티비티가 열린 경우라면
        else if(opened_from.equals(Static.PROJECT_FOLDER)) {
            // 내부 메소드 호출
            save_meeting_result();
        }
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 'EditText에 작성한 메모'는 Shared에 저장,
                지정한 프로젝트'는 DB에 저장 한뒤 액티비티 종료
     ---------------------------------------------------------------------------*/
    public void save_meeting_result() {
        // EditText memo에 작성된 내용 있는지 확인
        String memo_edit_str_for_check = memo_edit.getText().toString().replace(" ", "");
        Log.d(TAG, "memo_edit_str_for_check: " + memo_edit_str_for_check);

        String memo_edit_str="";

        // 작성한 메모 내용이 있으면
        if(!memo_edit_str_for_check.equals("")) {
            memo_edit_str = memo_edit.getText().toString();

            /**
             쉐어드에 미팅 메모, 저장하기 -
             [key: 회의 번호] | [value: memo_edit_str]
             */
            SharedPreferences meeting_memo = getSharedPreferences(Static.MEETING_MEMO, MODE_PRIVATE);
            SharedPreferences.Editor meeting_memo_edit = meeting_memo.edit();
            meeting_memo_edit.putString(target_meeting_no, memo_edit_str).apply();
        }
        // 작성한 메모 내용이 없으면
        else if(memo_edit_str_for_check.equals("")) {
            /**
             쉐어드 미팅 메모 삭제하기 -
             [key: 회의 번호] | [value: memo_edit_str]
             */
            SharedPreferences meeting_memo = getSharedPreferences(Static.MEETING_MEMO, MODE_PRIVATE);
            SharedPreferences.Editor meeting_memo_edit = meeting_memo.edit();
            meeting_memo_edit.remove(target_meeting_no).apply();
        }

//        // 지정한 프로젝트가 있으면
//        if(selected_project_no != -1) {
            // 해당 회의를 프로젝트에 지정하여 서버에 내용 전송하고
            myapp.assign_project(selected_project_no, target_meeting_no);
//        }

        //// 프로젝트 폴더의 아이템 리스트로 부터 클릭해서, 이 액티비티가 열린 경우라면
        if(opened_from.equals(Static.PROJECT_FOLDER)) {
            Intent result_intent = new Intent();
            result_intent.putExtra("resultOK", true);
            setResult(RESULT_OK, result_intent);
        }
        //// 회의종료 후, 이 액티비티가 팝업 된 경우라면
        else if(opened_from.equals(Static.RIGHT_AFTER_END_MEETING)) {
            // 액티비티 종료
            myapp.logAndToast("회의 결과가 저장되었습니다.");
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 300);
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 스캔한 이미지 파일을, 'remotemeeting' 폴더로 복사하고, 원래 파일은 삭제하는 메소드
     ---------------------------------------------------------------------------*/
    private void saveFile_scanned_images() {

        // 별도 쓰레드로 시행
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // 파일 저장 경로 및 이름 설정
                    String ex_storage = Environment.getExternalStorageDirectory().getAbsolutePath();
                    String folder_name = "/remoteMeeting/";
                    String string_path = ex_storage+folder_name;
                    String file_name = target_meeting_no + "__" + myapp.get_time("yyyyMMdd HH_mm_ss:SSS") + "_scanned_";

                    // temp_uri_hash에 들어있는 Uri 값을 차례로 가져와서 이미지 파일을 바로 위에서 설정한 경로로 복사하고
                    // 원래 이미지 파일은 삭제
                    for (int key : temp_uri_hash.keySet()) {

                        // temp_uri_hash에 key값을 가져와서 파일 이름 끝부분에 붙이고, 확장자인 '.png' 까지 붙이기
                        file_name = file_name + String.valueOf(key) + ".png";

                        // temp_uri_hash에 Uri의 파일 절대 경로 가져오기
                        String path = getPathFromUri(temp_uri_hash.get(key));
                        Log.d(TAG, "path: " + path);
                        Log.d(TAG, "string_path + file_name: " + string_path + file_name);

                        /** 파일 복사 로직 */
                        try {
                            // 복사할 파일이 있는 filePath+fileName 으로 inputStream 생성
                            FileInputStream fis = new FileInputStream(path);
                            // 생성할 filePath+fileName 으로 OutputStream 생성
                            FileOutputStream fos = new FileOutputStream(string_path+file_name);

                            int data = 0;

                            // 파일 복사
                            while((data=fis.read())!=-1) {
                                fos.write(data);
                            }

                            // 스트림 닫기
                            fis.close();
                            fos.close();

                        } catch(Exception e) {
                            e.printStackTrace();
                        }

                        // 최종적으로
                        finally {
                            /**
                             쉐어드에 파일 절대경로 값 저장하기 -
                             [쉐어드 파일이름: Static.MEETING_SCANNED_IMAGE + Static.SPLIT + 회의 번호]
                             [key: 회의 번호] | [value: 파일 절대 경로 + 파일이름]
                             */
                            SharedPreferences meeting_scanned_image =
                                    getSharedPreferences(Static.MEETING_SCANNED_IMAGE + Static.SPLIT + target_meeting_no, MODE_PRIVATE);
                            SharedPreferences.Editor meeting_scanned_image_edit = meeting_scanned_image.edit();
                            meeting_scanned_image_edit.putString(
                                    target_meeting_no, string_path+file_name).apply();

                            // 복사한 원본 파일은 삭제
                            File from_file = new File(path);
                            from_file.delete();
                            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(path))));
                            from_file = null;

                            // 복사한 파일은 sendBroadcast로 '갤러리'에서도 확인할 수 있게 하기
                            File to_file = new File(string_path+file_name);
                            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(string_path+file_name))));
                            to_file = null;
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // 작업이 다 끝나면, 작업이 끝났음을 핸들러를 통해 알리기
                finally {
                    myapp.dismiss_progress();
                    saveFile_scanned_images_handler.sendEmptyMessage(0);
                }
            }
        });
    }

    /**---------------------------------------------------------------------------
     메소드 ==> Uri 값으로 해당 파일의 절대 경로를 가져오는 메소드
     ---------------------------------------------------------------------------*/
    public String getPathFromUri(Uri uri){

        Cursor cursor = getContentResolver().query(uri, null, null, null, null );
        cursor.moveToNext();

        String path = cursor.getString( cursor.getColumnIndex( "_data" ) );
        cursor.close();

        return path;
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 방금 종료된 영상회의를 특정 프로젝트에 지정하는데, 지정하는 방법을 선택하는 다이얼로그
                선택 1. 기존 프로젝트에 지정
                선택 2. 새 프로젝트 생성
                가변 선택 3. 프로젝터 지정 취소
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.project_assign_LIN)
    public void select_method_for_assign_project(View view) {
        // TODO: redis - 클릭이벤트
        myapp.Redis_log_click_event(getClass().getSimpleName(), view);

        // TODO: redis - 화면 이동
        myapp.Redis_log_view_crossOver_from_to(
                getClass().getSimpleName(), Select_method_for_assign_project_D.class.getSimpleName());

        Intent intent = new Intent(this, Select_method_for_assign_project_D.class);
        // 'selected_project_no'를 intent 값으로 넘기는 이유
        // - 'selected_project_no' 값이 '-1'이라면 지정된 프로젝트가 없는 경우 이므로
        // Select_method_for_assign_project 클래스에서 '지정 취소' view를 GONE 처리 하기위함
        intent.putExtra("selected_project_no", selected_project_no);
        intent.putExtra(Static.REQUEST_CLASS, getClass().getSimpleName());
        startActivityForResult(intent, REQUEST_SELECT_METHOD_FOR_ASSIGN_PROJECT);
    }



    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 필기작성한 메모지나 종이를 스캐너 처럼 스캔하는 액티비티 열기
     ---------------------------------------------------------------------------*/
    @OnClick({R.id.handwriting_to_document_icon, R.id.handwriting_to_document_txt})
    public void handwriting_to_document(View view) {
        // TODO: redis - 클릭이벤트
        myapp.Redis_log_click_event(getClass().getSimpleName(), view);

        // TODO: redis - 화면 이동
        myapp.Redis_log_view_crossOver_from_to(
                getClass().getSimpleName(), Image_scan_to_document_D.class.getSimpleName());

        Intent intent = new Intent(this, Image_scan_to_document_D.class);
        intent.putExtra(Static.REQUEST_CLASS, getClass().getSimpleName());
        startActivityForResult(intent, REQUEST_IMAGE_SCAN_TO_DOCUMENT);
    }


    /**---------------------------------------------------------------------------
     콜백메소드 ==> onActivityResult
     ---------------------------------------------------------------------------*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 문서 스캔을 완료 하고 돌아왔을 때
        if(requestCode == REQUEST_IMAGE_SCAN_TO_DOCUMENT && resultCode == RESULT_OK) {
            try {
                // Uri 값 받아오기
                Uri uri = data.getExtras().getParcelable(ScanConstants.SCANNED_RESULT);
                Log.d(TAG, "onActivityResult_ uri: " + uri);
                // uri 값으로 비트맵 생성
                Bitmap bitmap = null;
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

                // 해당 uri 값에서 '/'으로 스플릿한 결과의 마지막 값(고유값임)을 추출
                String[] split_uri = uri.toString().split("[/]");
                final int extract_uri_Unique_value = Integer.valueOf(split_uri[split_uri.length-1]);
                Log.d(TAG, "extract_uri_Unique_value: " + extract_uri_Unique_value);

                // uri에서 추출한 고유값을 key로 하고, 해당 uri를 value로 하여 임시 해쉬맵에 put
                temp_uri_hash.put(extract_uri_Unique_value, uri);
                Log.d(TAG, "temp_uri_hash.size(): " + temp_uri_hash.size());

                // uri 값 삭제 - 서버로 사진 전송이 완료 된 이후에 해야할듯
//                getContentResolver().delete(uri, null, null);

//                scanned_img.setVisibility(View.VISIBLE);

                /** add할, ImageView 및 ImageView 속성 셋팅을 위한 LinearLayout.LayoutParams 생성 */
                LinearLayout.LayoutParams vp =
                        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                ImageView imageView = new ImageView(this);
                // 마진 셋팅
                vp.setMargins(0, 20, 0, 0);
                // 패딩 셋팅
                imageView.setPadding(0, 0, 20, 0);
                // width, height 속성 및 마진 속성 적용
                imageView.setLayoutParams(vp);
                // 이미지 비율에 맞게 Height 조절되는 속성 적용
                imageView.setAdjustViewBounds(true);
                // 클릭리스너 달기
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // TODO: redis - 클릭이벤트
                        myapp.Redis_log_click_event(getClass().getSimpleName(), view);

                        scanned_img_delete_confirm(extract_uri_Unique_value, view);
                    }
                });

                // onActivity 'data' 값에 담겨 온 스캔된 'uri' 값을 받아 생성한 bitmap을
                // glide를 통해 셋팅하기
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                Glide
                    .with(this)
                    .load(stream.toByteArray())
                    .asBitmap()
                    .into(imageView);

                // 최종 셋팅된 ImageView를 '손글씨 메모 스캔' 부모뷰에 add 하기
                handwriting_to_document_LIN_for_add_img.addView(imageView);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // 프로젝트 지정 방법을 선택하고 돌아왔을 때
        else if(requestCode==REQUEST_SELECT_METHOD_FOR_ASSIGN_PROJECT && resultCode==RESULT_OK) {
            String method = data.getStringExtra("method");
            Log.d(TAG, "method: " + method);

            // 선택한 방법이, 기존 프로젝트에 지정하는 방법일 때
            if(method.equals("assign_to_existing_project")) {
                // TODO: redis - 화면 이동
                myapp.Redis_log_view_crossOver_from_to(
                        getClass().getSimpleName(), Assign_to_existing_project_D.class.getSimpleName());

                Intent intent = new Intent(this, Assign_to_existing_project_D.class);
                intent.putExtra(Static.REQUEST_CLASS, getClass().getSimpleName());
                startActivityForResult(intent, REQUEST_ASSIGN_TO_EXISTING_PROJECT);
            }
            // 선택한 방법이, 새 프로젝트를 생성하는 방법일 때
            else if(method.equals("create_new_project")) {
                // TODO: redis - 화면 이동
                myapp.Redis_log_view_crossOver_from_to(
                        getClass().getSimpleName(), Create_project_A.class.getSimpleName());

                Intent intent = new Intent(this, Create_project_A.class);
                intent.putExtra("from", "meeting_result");
                intent.putExtra(Static.REQUEST_CLASS, getClass().getSimpleName());
                startActivityForResult(intent, REQUEST_CREATE_PROJECT_FROM_MEETING_RESULT_D);
            }
            // 선택한 방법이, 이미 지정된 프로젝트를 취소하는 것일 때
            else if(method.equals("unAssign_project")) {
                // 지정된 프로젝트 번호를 담고 있는 변수 초기화
                selected_project_no = -1;
                // 프로젝트 폴더 초기화
                Glide
                    .with(this)
                    .load(R.drawable.unspecified_project)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(project_folder_img);

                // 프로젝트 이름 초기화
                project_name_txt.setText("프로젝트 지정");

                //' 액션바' 와 그 바로 밑 '레이아웃'의 백그라운드 컬러 초기화
                actionBar_LIN.setBackgroundColor(0xff9e9e9e);
                meeting_basic_info_LIN.setBackgroundColor(0xff9e9e9e);
            }
        }
        // 지정할 프로젝트를 선택하고 돌아왔을 때
        else if(requestCode==REQUEST_ASSIGN_TO_EXISTING_PROJECT && resultCode==RESULT_OK) {
            // 지정한 프로젝트의 'no'를 전역변수에 저장
            selected_project_no = data.getIntExtra("selected_project_no", -1);
            String selected_project_color = data.getStringExtra("selected_project_color");
            String selected_project_name = data.getStringExtra("selected_project_name");

            // 지정된 프로젝트 정보에 따라 컬러 변경 및, 프로젝트 폴더 아이콘과 이름을 set 하는 내부 메소드 호출
            set_assign_project_result(
                    selected_project_color, selected_project_name);
        }
        // 새로운 프로젝트를 생성하고 돌아왔을 때
        // 생성한 프로젝트를 바로 적용하기
        else if(requestCode==REQUEST_CREATE_PROJECT_FROM_MEETING_RESULT_D && resultCode==RESULT_OK) {

            // intent로, 생성한 project 객체의 jsonString 받아옴
            String created_project_jsonString = data.getStringExtra("created_project_jsonString");
            Log.d(TAG, "created_project_jsonString (Main_after_login_A - onActivityResult): "
                    + created_project_jsonString);

            // jsonString을 Project.class로 파싱할 때 사용할 Gson객체 생성
            Gson gson = new Gson();

            Project created_project = gson.fromJson(created_project_jsonString, Project.class);
            Log.d(TAG, "created_project.getProject_color(): " + created_project.getProject_color());
            Log.d(TAG, "created_project.getProject_name(): " + created_project.getProject_name());

            // 생성한 프로젝트의 'no'를 전역변수에 저장
            selected_project_no = created_project.getProject_no();

            // 지정된 프로젝트 정보에 따라 컬러 변경 및, 프로젝트 폴더 아이콘과 이름을 set 하는 내부 메소드 호출
            set_assign_project_result(
                    created_project.getProject_color(), created_project.getProject_name());
        }
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 지정된 프로젝트 정보에 따라 컬러 변경 및, 프로젝트 폴더 아이콘과 이름을 set 한다
     ---------------------------------------------------------------------------*/
    public void set_assign_project_result(String selected_project_color, String selected_project_name) {

        // '내부 메소드로'부터 가져온 int value 값으로,
        // '액션바' 와 그 바로 밑 '레이아웃'의 백그라운드 컬러를 변경한다
        int color_int_value = myapp.project_color(selected_project_color);
        actionBar_LIN.setBackgroundColor(color_int_value);
        meeting_basic_info_LIN.setBackgroundColor(color_int_value);

        // intent 값을 모두 제대로 전달 받았을 때, 그 전달받은 데이터를 가지고,
        // 프로젝트 폴더 아이콘과, 프로젝트 이름을 set 한다
        if(selected_project_no != -1 && selected_project_color != null && selected_project_name != null) {
            // 폴더 설정
            Glide
                .with(this)
                .load(myapp.getFolder_color_hash().get(selected_project_color))
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(project_folder_img);

            // 프로젝트 이름 설정
            project_name_txt.setText(selected_project_name);
        }
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 스캔이미지 삭제 여부를 확인하는 AlertDialog 띄우기
     ---------------------------------------------------------------------------*/
    private void scanned_img_delete_confirm(final int extract_uri_Unique_value, final View view) {

        AlertDialog.Builder Writing_Restore_choice = new AlertDialog.Builder(this);

        Writing_Restore_choice
                .setMessage("해당 스캔이미지를 삭제하시겠습니까?")
                .setCancelable(true)
                .setPositiveButton("네",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // '네'를 선택했을 때 실행되는 로직

                                // 해당 이미지뷰를 삭제
                                handwriting_to_document_LIN_for_add_img.removeView(view);

                                // 해당 이미지뷰를 '손글씨 필기 메모' uri 값을 담아놓는 해쉬맵에서 삭제
                                temp_uri_hash.remove(extract_uri_Unique_value);
                                Log.d(TAG, "temp_uri_hash.size(): " + temp_uri_hash.size());
                            }
                        })
                .setNegativeButton("아니오",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // '아니오'를 선택했을 때 실행되는 로직

                            }
                        });
        final AlertDialog alert = Writing_Restore_choice.create();
        // dialog 제목
        alert.setTitle("확인");
        // '네' 텍스트 컬러 변경
        alert.setOnShowListener( new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#4CAF50"));
            }
        });
        // AlertDialog 띄우기
        alert.show();

    }


    /**---------------------------------------------------------------------------
     메소드 ==> 손글씨 스캔파일, EditText의 메모, 프로젝트 지정 -- 이 세 개가 '작성'되어 있는지 확인하는 메소드
                소프트 백키를 누르거나 액티비티를 종료 하려고 할 때, 세개 항목중 하나라도 작성이 되어 있다면
                작성된 항목이 저장되지 않는다는 AlertDialog를 띄워서 사용자에게 알리기 위함.
     ---------------------------------------------------------------------------*/
    public boolean check_have_any_list_i_have_created() {

        String memo_edit_str_for_check = memo_edit.getText().toString().replace(" ", "");

        // 체크 1. 손글씨 스캔 파일이름을 담고 있는 해쉬맵 사이즈가 0이 아니거나,
        // 체크 2. EditText memo에 작성된 내용이 있거나
        // 체크 3. 지정한 프로젝트가 있거나
        // 위 내용에 하나라도 해당이 된다면 'true'를 반환한다
        if(temp_uri_hash.size() != 0 || !memo_edit_str_for_check.equals("") || selected_project_no != -1) {
            return true;
        }
        // 그 반대의 경우에는 'false'를 반환한다
        else if(temp_uri_hash.size() == 0 || !memo_edit_str_for_check.equals("") || selected_project_no == -1) {
            return false;
        }
        else {
            return false;
        }
    }


    /**---------------------------------------------------------------------------
     오버라이드 ==> onBackPressed -- 소프트키보드를 눌러 액티비티를 종료하기 전,
            내가 작성한 '항목'이 있으면, AlertDialog를 호출
            내가 작성항 '항목'이 없으면, 원래대로 액티비티를 종료하고
            '기본 회의결과'가 저장되었음을 toast로 알리기(단, 방금 회의를 종료하고 나서 '회의 결과'를 봤을 때만)
     ---------------------------------------------------------------------------*/
    @Override
    public void onBackPressed() {
        if(check_have_any_list_i_have_created()) {

        }
        else if(!check_have_any_list_i_have_created() && !opened_from.equals(Static.PROJECT_FOLDER)) {
            // 해당 회의를 프로젝트에 지정하여 서버에 내용 전송하고
            myapp.assign_project(selected_project_no, target_meeting_no);
            myapp.logAndToast("'기본' 회의결과가 저장되었습니다.");
        }
        super.onBackPressed();
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

        // '손필기 메모 스캔' 이미지가 있는 uri 값들 삭제
        if(!temp_uri_hash.isEmpty()) {
            Iterator<Integer> it = temp_uri_hash.keySet().iterator();
            while(it.hasNext()) {
                int key = it.next();
                getContentResolver().delete(temp_uri_hash.get(key), null, null);
                temp_uri_hash.remove(key);
            }
        }
        Log.d(TAG, "temp_uri_hash.size()_ onDestroy: " + temp_uri_hash.size());

        super.onDestroy();
    }
}
