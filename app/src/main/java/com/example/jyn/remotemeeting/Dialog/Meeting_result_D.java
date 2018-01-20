package com.example.jyn.remotemeeting.Dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.jyn.remotemeeting.DataClass.File_info;
import com.example.jyn.remotemeeting.DataClass.Meeting_room;
import com.example.jyn.remotemeeting.DataClass.Users;
import com.example.jyn.remotemeeting.Etc.Static;
import com.example.jyn.remotemeeting.R;
import com.example.jyn.remotemeeting.Util.Myapp;
import com.example.jyn.remotemeeting.Util.RetrofitService;
import com.example.jyn.remotemeeting.Util.ServiceGenerator;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
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
    Myapp myapp;
    String ended_meeting_no;

    /** 버터나이프*/
    public Unbinder unbinder;
    @BindView(R.id.actionBar_LIN)                       LinearLayout actionBar_LIN;
    @BindView(R.id.meeting_basic_info_LIN)              LinearLayout meeting_basic_info_LIN;
    @BindView(R.id.project_assign_LIN)                  LinearLayout project_assign_LIN;
    @BindView(R.id.meeting_memo_LIN)                    LinearLayout meeting_memo_LIN;
    @BindView(R.id.handwriting_to_document_LIN)         LinearLayout handwriting_to_document_LIN;
    @BindView(R.id.meeting_subject_user_nickName_txt)   TextView meeting_subject_user_nickName_txt;
    @BindView(R.id.save_meeting_result_txt)             TextView save_meeting_result_txt;
    @BindView(R.id.meeting_title_txt)                   TextView meeting_title_txt;
    @BindView(R.id.today_txt)                           TextView today_txt;
    @BindView(R.id.meeting_start_time_txt)              TextView meeting_start_time_txt;
    @BindView(R.id.meeting_end_time_txt)                TextView meeting_end_time_txt;
    @BindView(R.id.total_meeting_time_txt)              TextView total_meeting_time_txt;
    @BindView(R.id.project_name_txt)                    TextView project_name_txt;
    @BindView(R.id.meeting_subject_user_profile_img)    ImageView meeting_subject_user_profile_img;
    @BindView(R.id.project_folder_img)                  ImageView project_folder_img;
    @BindView(R.id.memo_edit)                           EditText memo_edit;
    @BindView(R.id.handwriting_to_document_rcv)         RecyclerView handwriting_to_document_rcv;
    @BindView(R.id.upload_images_rcv)                   RecyclerView upload_images_rcv;
    @BindView(R.id.drawing_images_rcv)                  RecyclerView drawing_images_rcv;

    // 서버로부터 받아온 회의 결과(jsonString)
    public String jsonString_meeting_result;

    // 서버로부터 받아온, 회의 결과를 담을 'meeting_room' 객체(데이터 클래스)
    private Meeting_room ended_meeting_room;

    // 서버로부터 받아온, 회의 중 업로드한 이미지 파일들의 파일이름을 담을 어레이리스트(요소: 데이터 클래스)
    private ArrayList<File_info> share_img_file_name_arr;

    // 서버로부터 받아온, 회의 대상의 유저 정보를 담을 객체(데이터 클래스)
    private Users subject_user;

    // 아직 서버에 회의 종료 시각이 insert 되기 전이라, 최초 받아온 정보에 회의 종료 시각, 총 회의 시간이 없을때,
    // 회의 종료 시각, 총 회의 시간을 받아오는 메소드를 다시 호출하여 결과 값을 받아 왔을 때,
    // 아직도 DB에 결과가 없을 경우, 다시 정보를 받아오는 메소드를 호출하기 위한 핸들러
    public Handler meeting_end_time_handler;
    // 나중에 추가적으로 DB에서 '회의 종료 시각, 총 회의 시간'을 받아왔을 경우 저장하기 위한 전역 변수
    String receive_delayed_meeting_end_time;
    String receive_delayed_total_meeting_time;

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

        // 핸들러 생성
        meeting_end_time_handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                // 아직 종료한 회의의, 회의 종료 시각과 총 회의 시간을 받아 오지 못했을 때
                // 다시 서버로부터 해당 데이터들을 받아오는 메소드를 호출한다
                // 0.1초 뒤에
                if(msg.what == 0) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            get_meeting_end_time();
                        }
                    }, 100);
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
        jsonString_meeting_result = myapp.get_ended_meeting_result(ended_meeting_no);

        // jsonString_meeting_result --> 필요한 정보에 따라 알맞게 파싱하는 내부 메소드 호출
        parsing_server_data(jsonString_meeting_result);

        // 종료된 영상회의를 뷰에 셋팅
        set_ended_meeting_result_to_view();
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
                // gson 이용해서 File_info 객체로 변환, 어레이리스트에 add
                if(!share_img_file_name_JsArr.isNull(0)) {
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
        if(ended_meeting_room.getMeeting_end_time().equals("") ||
                ended_meeting_room.getTotal_meeting_time().equals("")) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    get_meeting_end_time();
                }
            }, 300);
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
