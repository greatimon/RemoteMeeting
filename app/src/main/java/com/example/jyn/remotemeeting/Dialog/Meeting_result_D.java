package com.example.jyn.remotemeeting.Dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.example.jyn.remotemeeting.Adapter.RCV_call_adapter;
import com.example.jyn.remotemeeting.Adapter.RCV_show_uploaded_images_after_end_meeting_adapter;
import com.example.jyn.remotemeeting.DataClass.File_info;
import com.example.jyn.remotemeeting.DataClass.Meeting_room;
import com.example.jyn.remotemeeting.DataClass.Users;
import com.example.jyn.remotemeeting.Etc.Static;
import com.example.jyn.remotemeeting.R;
import com.example.jyn.remotemeeting.Util.GridLayout_itemOffsetDecoration_rcv;
import com.example.jyn.remotemeeting.Util.Myapp;
import com.example.jyn.remotemeeting.Util.RetrofitService;
import com.example.jyn.remotemeeting.Util.ServiceGenerator;
import com.google.gson.Gson;
import com.scanlibrary.ScanConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
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
    Myapp myapp;
    String ended_meeting_no;
    String ended_subject_user_no;

    /** 버터나이프*/
    public Unbinder unbinder;
    @BindView(R.id.actionBar_LIN)                           LinearLayout actionBar_LIN;
    @BindView(R.id.meeting_basic_info_LIN)                  LinearLayout meeting_basic_info_LIN;
    @BindView(R.id.project_assign_LIN)                      LinearLayout project_assign_LIN;
    @BindView(R.id.meeting_memo_LIN)                        LinearLayout meeting_memo_LIN;
    @BindView(R.id.handwriting_to_document_LIN)             LinearLayout handwriting_to_document_LIN;
    @BindView(R.id.handwriting_to_document_LIN_for_add_img) LinearLayout handwriting_to_document_LIN_for_add_img;
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

    // 아직 서버에 회의 종료 시각이 insert 되기 전이라, 최초 받아온 정보에 회의 종료 시각, 총 회의 시간이 없을때,
    // 회의 종료 시각, 총 회의 시간을 받아오는 메소드를 다시 호출하여 결과 값을 받아 왔을 때,
    // 아직도 DB에 결과가 없을 경우, 다시 정보를 받아오는 메소드를 호출하기 위한 핸들러
    public Handler meeting_end_time_handler;
    // 나중에 추가적으로 DB에서 '회의 종료 시각, 총 회의 시간'을 받아왔을 경우 저장하기 위한 전역 변수
    String receive_delayed_meeting_end_time;
    String receive_delayed_total_meeting_time;
    // 회의 종료시각, 총 회의 시간을 최초에 받아오지 못했을 때, 총 5번만 더 시도하기 위해, 시도 횟수를 세는 변수
    int get_meeting_end_time_try_count;

    // 리사이클러뷰 관련 변수, 클래스 선언 =========
    // =========================================
    public RCV_show_uploaded_images_after_end_meeting_adapter rcv_upload_images_after_end_meeting_adapter;



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

        // 어플리케이션 객체로부터 방금 종료한 회의의 primary key인 meeting_no를 가져와서 전역변수에 넣는다
        ended_meeting_no = myapp.getMeeting_no();
        Log.d(TAG, "ended_meeting_no: " + ended_meeting_no);
        // meeting_no를 가지고 온뒤에, 초기화(해당 영상회의에 더이상 참여하고 있지 않기 때문에)
        myapp.setMeeting_no("");
        Log.d(TAG, "myapp.getMeeting_no()_ after set '': " + myapp.getMeeting_no());

        // 어플리케이션 객체로부터 방금 종료한 회의의, 회의대상 user_no를 가져와서 전역변수에 넣는다
        ended_subject_user_no = myapp.getMeeting_subject_user_no();
        Log.d(TAG, "ended_subject_user_no: " + ended_subject_user_no);
        // 회의대상 user_no를 가지고 온뒤에, 초기화(해당 영상회의에 더이상 참여하고 있지 않기 때문에)
        myapp.setMeeting_subject_user_no("");
        Log.d(TAG, "myapp.getMeeting_subject_user_no()_ after set '': " + myapp.getMeeting_subject_user_no());

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

        // 혹시 있을수도 있는 공유 파일 이미지들의 파일이름을 담을 어레이리스트 생성
        share_img_file_name_arr = new ArrayList<>();

        // 종료된 영상회의의 정보를 가져오는 어플리케이션 객체 내 메소드 호출
        jsonString_meeting_result = myapp.get_ended_meeting_result(ended_meeting_no, ended_subject_user_no);

        // jsonString_meeting_result --> 필요한 정보에 따라 알맞게 파싱하는 내부 메소드 호출
        // 파싱한 뒤, 종료된 영상회의를 뷰에 셋팅
        parsing_server_data(jsonString_meeting_result);

        // 서버로부터 공유 파일리스트 받기
        ArrayList<File_info> files = myapp.get_uploaded_file_list(this, ended_meeting_no);
        Log.d(TAG, "어댑터에 넘길 project_files.isEmpty(): " + files.isEmpty());
        if(!files.isEmpty()) {
            Log.d(TAG, "어댑터에 넘길 project_files 개수: " + files.size());
            // 생성자 인수
            // 1. 액티비티
            // 2. 인플레이팅 되는 레이아웃
            // 3. arrayList 데이터
            // 4. 변별 변수
            rcv_upload_images_after_end_meeting_adapter
                    = new RCV_show_uploaded_images_after_end_meeting_adapter(this, R.layout.i_uploaded_images, files, "meeting_result");

            // 그리드 레이아수 적용, 한줄에 2개
            upload_images_rcv.setLayoutManager(new GridLayoutManager(this, 2));

            // 아이템 간 일정한 패딩을 주기 위한 ItemDecoration
            GridLayout_itemOffsetDecoration_rcv itemDecoration = new GridLayout_itemOffsetDecoration_rcv(this, R.dimen.item_offset);
            upload_images_rcv.addItemDecoration(itemDecoration);

            // set Adapter
            upload_images_rcv.setAdapter(rcv_upload_images_after_end_meeting_adapter);
            rcv_upload_images_after_end_meeting_adapter.notifyDataSetChanged();
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
//        LinearLayout actionBar_LIN;
//        LinearLayout meeting_basic_info_LIN;
//        LinearLayout project_assign_LIN;
//        LinearLayout meeting_memo_LIN;
//        LinearLayout handwriting_to_document_LIN;
//        TextView meeting_subject_user_nickName_txt;
//        TextView save_meeting_result_txt;
//        TextView meeting_title_txt;
//        TextView today_txt;
//        TextView meeting_start_time_txt;
//        TextView meeting_end_time_txt;
//        TextView total_meeting_time_txt;
//        TextView project_name_txt;
//        ImageView meeting_subject_user_profile_img;
//        ImageView project_folder_img;
//        EditText memo_edit;
//        RecyclerView handwriting_to_document_rcv;
//        RecyclerView upload_images_rcv;
//        RecyclerView drawing_images_rcv;

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
                                ended_meeting_no);
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
     클릭이벤트 ==> 필기작성한 메모지나 종이를 스캐너 처럼 스캔하는 액티비티 열기
     ---------------------------------------------------------------------------*/
    @OnClick({R.id.handwriting_to_document_icon, R.id.handwriting_to_document_txt})
    public void handwriting_to_document() {
        Intent intent = new Intent(this, Image_scan_to_document_D.class);
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


            // intent 값으로 저장한 비트맵 파일의 절대 경로를 받아온다.
//            String canonicalPath = data.getStringExtra("canonicalPath");
//            if(myapp.getScanned_bitmap() != null) {
                // 받아온 절대 경로값으로 뷰에 셋팅한다
//                Glide
//                    .with(this)
//                    .load(myapp.getScanned_bitmap())
//                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
////                    .bitmapTransform(new CropCircleTransformation(context))
//                    .into(scanned_img);
//            }
        }
    }


    // 사진첨부 방법 선택하는 dialog
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
