package com.example.jyn.remotemeeting.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.jyn.remotemeeting.Dialog.Calendar_D;
import com.example.jyn.remotemeeting.R;
import com.example.jyn.remotemeeting.Util.Myapp;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by JYN on 2018-01-24.
 *
 * 새 프로젝트를 생성하는 클래스
 */

public class Create_project_A extends Activity {

    private static final String TAG = "all_"+Create_project_A.class.getSimpleName();
    Myapp myapp;

    final int REQUEST_SET_START_DATE = 1234;
    final int REQUEST_SET_END_DATE = 4321;

    /** 버터나이프 */
    public Unbinder unbinder;
    @BindView(R.id.actionBar_LIN)                       LinearLayout actionBar_LIN;
    @BindView(R.id.project_name_LIN)                    LinearLayout project_name_LIN;
    @BindView(R.id.project_start_dt_LIN)                LinearLayout project_start_dt_LIN;
    @BindView(R.id.project_end_dt_LIN)                  LinearLayout project_end_dt_LIN;
    @BindView(R.id.project_color_LIN)                   LinearLayout project_color_LIN;
    @BindView(R.id.create_project_txt)                  TextView create_project_txt;
    @BindView(R.id.project_start_dt_comment_txt)        TextView project_start_dt_comment_txt;
    @BindView(R.id.project_start_dt_txt)                TextView project_start_dt_txt;
    @BindView(R.id.project_end_dt_comment_txt)          TextView project_end_dt_comment_txt;
    @BindView(R.id.project_end_dt_edit)                 TextView project_end_dt_edit;
    @BindView(R.id.project_color_txt)                   TextView project_color_txt;
    @BindView(R.id.project_name_edit)                   EditText project_name_edit;
    @BindView(R.id.project_folder_img)                  ImageView project_folder_img;

    // 프로젝트 시작 년, 월, 일 담을 변수
    int selected_start_year = -1;
    int selected_start_month = -1;
    int selected_start_day = -1;

    // 프로젝트 종료 년, 월, 일 담을 변수
    int selected_end_year = -1;
    int selected_end_month = -1;
    int selected_end_day = -1;

    /**---------------------------------------------------------------------------
     생명주기 ==> onCreate
     ---------------------------------------------------------------------------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_create_project);

        // 버터나이프 바인드
        unbinder = ButterKnife.bind(this);
        // 어플리케이션 객체 생성
        myapp = Myapp.getInstance();
    }


    /**---------------------------------------------------------------------------
     콜백메소드 ==> onActivityResult
     ---------------------------------------------------------------------------*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if((requestCode==REQUEST_SET_START_DATE ||requestCode==REQUEST_SET_END_DATE)
                && resultCode == RESULT_OK) {
            String for_str = data.getStringExtra("for");
            Log.d(TAG, "for_str: " + for_str);

            if(for_str.equals("start_date")) {
                selected_start_year = data.getIntExtra("selected_year", -1);
                selected_start_month = data.getIntExtra("selected_month", -1);
                selected_start_day = data.getIntExtra("selected_day", -1);
                Log.d(TAG, "selected_start_year: " + selected_start_year);
                Log.d(TAG, "selected_start_month: " + selected_start_month);
                Log.d(TAG, "selected_start_day: " + selected_start_day);
            }
            else if(for_str.equals("end_date")) {
                selected_end_year = data.getIntExtra("selected_year", -1);
                selected_end_month = data.getIntExtra("selected_month", -1);
                selected_end_day = data.getIntExtra("selected_day", -1);
                Log.d(TAG, "selected_end_year: " + selected_end_year);
                Log.d(TAG, "selected_end_month: " + selected_end_month);
                Log.d(TAG, "selected_end_day: " + selected_end_day);
            }
        }
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 프로젝트 시작날짜 설정
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.project_start_dt_LIN)
    public void set_project_start_dt() {
        Intent intent = new Intent(this, Calendar_D.class);
        intent.putExtra("for", "start_date");
        startActivityForResult(intent, REQUEST_SET_START_DATE);
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 프로젝트 종료날짜 설정
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.project_end_dt_LIN)
    public void set_project_end_dt() {
        Intent intent = new Intent(this, Calendar_D.class);
        intent.putExtra("for", "end_date");
        startActivityForResult(intent, REQUEST_SET_END_DATE);
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 프로젝트 컬러 설정
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.project_color_LIN)
    public void set_project_color() {

    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 작성 완료
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.create_project_txt)
    public void create_project() {

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
