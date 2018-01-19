package com.example.jyn.remotemeeting.Dialog;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.jyn.remotemeeting.R;
import com.example.jyn.remotemeeting.Util.Myapp;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

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
    Myapp myapp;

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


    /**---------------------------------------------------------------------------
     생명주기 ==> onCreate
     ---------------------------------------------------------------------------*/
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

        // 종료된 영상회의의 정보를 가져오는 내부 메소드 호출
        get_meeting_result();
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 서버 통신 -- 종료된 영상회의의 정보를 서버로부터 받아온다
     ---------------------------------------------------------------------------*/
    private void get_meeting_result() {

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
