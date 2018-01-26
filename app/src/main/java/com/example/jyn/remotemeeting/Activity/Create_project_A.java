package com.example.jyn.remotemeeting.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.jyn.remotemeeting.DataClass.Project;
import com.example.jyn.remotemeeting.Dialog.Calendar_D;
import com.example.jyn.remotemeeting.Dialog.Select_project_color_D;
import com.example.jyn.remotemeeting.Etc.Static;
import com.example.jyn.remotemeeting.R;
import com.example.jyn.remotemeeting.Util.Myapp;
import com.example.jyn.remotemeeting.Util.RetrofitService;
import com.example.jyn.remotemeeting.Util.ServiceGenerator;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by JYN on 2018-01-24.
 *
 * 새 프로젝트를 생성하는 클래스

 * but!!!!!!
 * 처음에는 이 액티비티를 프로젝트 생성에만 사용하려고 했으나
 * 나중에 '수정'에도 사용하기로 함
 *
 * 그래서, 변수들이 수정과 상관없는 'create...' 이렇게 시작하는 변수가 많음. 참고!
 */

public class Create_project_A extends Activity {

    private static final String TAG = "all_"+Create_project_A.class.getSimpleName();
    Myapp myapp;
    /** 이 클래스를 호출한 클래스 SimpleName */
    String request_class;

    final int REQUEST_SET_START_DATE = 1234;
    final int REQUEST_SET_END_DATE = 4321;
    final int REQUEST_SET_COLOR = 9874;

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
    @BindView(R.id.title_txt)                           TextView title_txt;
    @BindView(R.id.project_name_edit)                   EditText project_name_edit;
    @BindView(R.id.project_folder_img)                  ImageView project_folder_img;

    /** '작성완료' 시, 반드시 있어야 하는 변수 */
    String project_name_str;

    /** '작성완료' 시, 반드시 있어야 하는 변수 */
    // 프로젝트 시작 년, 월, 일 담을 변수
    int selected_start_year = -1;
    int selected_start_month = -1;
    int selected_start_day = -1;

    // 프로젝트 종료 년, 월, 일 담을 변수
    int selected_end_year = -1;
    int selected_end_month = -1;
    int selected_end_day = -1;

    // 프로젝트 컬러 리소스 인덱스를 담을 변수
    int color_resource_index = -1;

    // 프로젝트를 생성하고 서버로부터 '정상 생성' 응답을 받은 후,
    // 액티비티를 종료하기 위해 retrofit onResponse 에서 보내는 메세지를 받기 위한 핸들러
    private Handler handler_for_finish;

    // 이 액티비티를 연 주체
    String opened_from;

    // 이미 생성된 프로젝트를 '수정'하기 위해 열었을 때,
    // intent 값으로 넘어오는 'project_no'
    int exist_project_no;


    /**---------------------------------------------------------------------------
     생명주기 ==> onCreate
     ---------------------------------------------------------------------------*/
    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_create_project);

        // 버터나이프 바인드
        unbinder = ButterKnife.bind(this);
        // 어플리케이션 객체 생성
        myapp = Myapp.getInstance();

        // 이 클래스를 호출한 클래스 인텐트 값으로 받기
        Intent get_intent = getIntent();
        request_class = get_intent.getStringExtra(Static.REQUEST_CLASS);

        // 인텐트값 받기
        Intent intent = getIntent();
        // case 1. from: "main" - 생성
        // case 2. from: "meeting_result" - 생성
        // case 3. from: "meeting_result_list" - 수정
        opened_from = intent.getStringExtra("from");
        Log.d(TAG, "opened_from: " + opened_from);

        // 'Project_meeting_result_list_A.class' 로 부터 열렸을 때 - 수정
        if(opened_from.equals("meeting_result_list")) {
            create_project_txt.setText("수정완료");
            exist_project_no = intent.getIntExtra("exist_project_no", 0);

            /** 이미 설정된 프로젝트 정보 가져오기 */
            if(exist_project_no != 0) {
                Project exist_project = myapp.get_project_info(exist_project_no);
                Log.d(TAG, "exist_project.getProject_name(): " + exist_project.getProject_name());
                Log.d(TAG, "exist_project.getProject_color(): " + exist_project.getProject_color());
                Log.d(TAG, "exist_project.getProject_start_dt(): " + exist_project.getProject_start_dt());
                Log.d(TAG, "exist_project.getProject_end_dt(): " + exist_project.getProject_end_dt());

//                테스트 프로젝트 16
//                light_green
//                2018-05-02
//                0000-00-00

                // 프로젝트 시작일, 변수에 저장
                if(!exist_project.getProject_start_dt().equals("0000-00-00")) {
                    String[] temp = exist_project.getProject_start_dt().split("-");
                    selected_start_year = Integer.parseInt(temp[0]);
                    selected_start_month = Integer.parseInt(temp[1])-1;
                    selected_start_day = Integer.parseInt(temp[2]);
                }
                // 프로젝트 종료일, 변수에 저장
                if(!exist_project.getProject_end_dt().equals("0000-00-00")) {
                    String[] temp_1 = exist_project.getProject_end_dt().split("-");
                    selected_end_year = Integer.parseInt(temp_1[0]);
                    selected_end_month = Integer.parseInt(temp_1[1])-1;
                    selected_end_day = Integer.parseInt(temp_1[2]);
                }
                // 프로젝트 컬러, 변수에 저장
                for(int i=0; i<myapp.folder_color_str.length; i++) {
                    if(exist_project.getProject_color().equals(myapp.folder_color_str[i])) {
                        color_resource_index = i;
                        break;
                    }
                }

                //// 타이틀 변경
                title_txt.setText("프로젝트 수정");

                //// 프로젝트 이름 셋팅
                project_name_edit.setText(exist_project.getProject_name());

                //// 프로젝트 시작일 셋팅
                // '프로젝트 시작일' 텍스트 사이즈 12dp로 설정
                project_start_dt_comment_txt.setTextSize(12);
                // 프로젝트 시작일 데이터 setText 하기
                String set_start_dt_str =
                        String.valueOf(selected_start_year) + "." +
                        String.valueOf(selected_start_month+1) + "." +
                        String.valueOf(selected_start_day);
                project_start_dt_txt.setText(set_start_dt_str);
                // 날짜를 표시하는 TextView, visibility 'VISIBLE' 처리
                project_start_dt_txt.setVisibility(View.VISIBLE);

                //// 프로젝트 종료일 셋팅
                if(!exist_project.getProject_end_dt().equals("0000-00-00")) {
                    // '프로젝트 종료일' 텍스트 사이즈 12dp로 설정
                    project_end_dt_comment_txt.setTextSize(12);
                    // 프로젝트 종료일 텍스트,
                    // "프로젝트 종료일 : 미정" ==> "프로젝트 종료일" 로 변경하기
                    project_end_dt_comment_txt.setText("프로젝트 종료일");
                    // 프로젝트 종료일 데이터 setText 하기
                    String set_end_dt_str =
                            String.valueOf(selected_end_year) + "." +
                                    String.valueOf(selected_end_month+1) + "." +
                                    String.valueOf(selected_end_day);
                    project_end_dt_edit.setText(set_end_dt_str);
                    // 날짜를 표시하는 TextView, visibility 'VISIBLE' 처리
                    project_end_dt_edit.setVisibility(View.VISIBLE);
                }

                //// 프로젝트 컬러 셋팅
                // 액션바 위치의 뷰, 백그라운드 컬러 변경
                actionBar_LIN.setBackgroundColor(myapp.folder_color_int_value[color_resource_index]);

                // 설정한 해당 프로젝트 폴더 아이콘으로 변경
                Glide
                    .with(this)
                    .load(myapp.folder_color_resource[color_resource_index])
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(project_folder_img);
            }
        }

        // 액티비티 종료하는, 핸들러 생성
        handler_for_finish = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                // 정상적으로 프로젝트가 생성되었으니,
                // 서버로부터 리턴받은, 생성한 프로젝트의 'no'를 인텐트로 반환하면서
                // 액티비티를 종료하라는 핸들러 메세지를 전달 받았을 때,
                if(msg.what == 0) {
                    String created_project_jsonString = msg.getData().getString("created_project_jsonString");
                    Log.d(TAG, "created_project_jsonString(handler): " + created_project_jsonString);

                    Intent finish_intent = new Intent();
                    finish_intent.putExtra("created_project_jsonString", created_project_jsonString);
                    setResult(RESULT_OK, finish_intent);
                    finish();
                }
            }
        };
    }


    /**---------------------------------------------------------------------------
     콜백메소드 ==> onActivityResult
     ---------------------------------------------------------------------------*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 캘린더뷰 액티비티(날짜 선택 액티비티)에서 돌아왔을 때
        if((requestCode==REQUEST_SET_START_DATE ||requestCode==REQUEST_SET_END_DATE)
                && resultCode == RESULT_OK) {
            // '프로젝트 시작날짜' or '프로젝트 종료날짜' 설정에서 돌아왔는지 확인하는 변수
            String for_str = data.getStringExtra("for");
            Log.d(TAG, "for_str: " + for_str);

            // 프로젝트 시작날짜 설정에서 돌아왔을 때
            if(for_str.equals("start_date")) {
                selected_start_year = data.getIntExtra("selected_year", -1);
                selected_start_month = data.getIntExtra("selected_month", -1);
                selected_start_day = data.getIntExtra("selected_day", -1);
                Log.d(TAG, "selected_start_year: " + selected_start_year);
                Log.d(TAG, "selected_start_month: " + selected_start_month);
                Log.d(TAG, "selected_start_day: " + selected_start_day);

                // 프로젝트 시작날짜 초기화를 했을 경우
                if(selected_start_year==-1 && selected_start_month==-1 && selected_start_day==-1) {
                    // '프로젝트 시작일' 텍스트 사이즈 15dp로 설정
                    project_start_dt_comment_txt.setTextSize(15);
                    // 날짜를 표시하는 TextView, visibility 'GONE' 처리
                    project_start_dt_txt.setVisibility(View.GONE);
                }
                // 프로젝트 시작날짜를 설정했을 경우
                else {
                    // '프로젝트 시작일' 텍스트 사이즈 12dp로 설정
                    project_start_dt_comment_txt.setTextSize(12);
                    // 프로젝트 시작일 데이터 setText 하기
                    String set_this_str =
                            String.valueOf(selected_start_year) + "." +
                            String.valueOf(selected_start_month+1) + "." +
                            String.valueOf(selected_start_day);
                    project_start_dt_txt.setText(set_this_str);
                    // 날짜를 표시하는 TextView, visibility 'VISIBLE' 처리
                    project_start_dt_txt.setVisibility(View.VISIBLE);
                }
            }
            // 프로젝트 종료날짜 설정에서 돌아왔을 때
            else if(for_str.equals("end_date")) {
                selected_end_year = data.getIntExtra("selected_year", -1);
                selected_end_month = data.getIntExtra("selected_month", -1);
                selected_end_day = data.getIntExtra("selected_day", -1);
                Log.d(TAG, "selected_end_year: " + selected_end_year);
                Log.d(TAG, "selected_end_month: " + selected_end_month);
                Log.d(TAG, "selected_end_day: " + selected_end_day);

                // 프로젝트 종료날짜 초기화를 했을 경우
                if(selected_end_year==-1 && selected_end_month==-1 && selected_end_day==-1) {
                    // '프로젝트 종료일' 텍스트 사이즈 15dp로 설정
                    project_end_dt_comment_txt.setTextSize(15);
                    // 프로젝트 종료일 텍스트,
                    // "프로젝트 종료일 : 미정" 로 변경하기
                    project_end_dt_comment_txt.setText("프로젝트 종료일 : 미정");
                    // 날짜를 표시하는 TextView, visibility 'GONE' 처리
                    project_end_dt_edit.setVisibility(View.GONE);
                }
                // 프로젝트 종료날짜를 설정했을 경우
                else {
                    // '프로젝트 종료일' 텍스트 사이즈 12dp로 설정
                    project_end_dt_comment_txt.setTextSize(12);
                    // 프로젝트 종료일 텍스트,
                    // "프로젝트 종료일 : 미정" ==> "프로젝트 종료일" 로 변경하기
                    project_end_dt_comment_txt.setText("프로젝트 종료일");
                    // 프로젝트 종료일 데이터 setText 하기
                    String set_this_str =
                            String.valueOf(selected_end_year) + "." +
                                    String.valueOf(selected_end_month+1) + "." +
                                    String.valueOf(selected_end_day);
                    project_end_dt_edit.setText(set_this_str);
                    // 날짜를 표시하는 TextView, visibility 'VISIBLE' 처리
                    project_end_dt_edit.setVisibility(View.VISIBLE);
                }
            }
        }
        // 프로젝트 컬러 설정에서 돌아왔을 때
        else if(requestCode==REQUEST_SET_COLOR && resultCode==RESULT_OK) {
            // intent 값 받기
            color_resource_index = data.getIntExtra("color_resource_index", -1);
            Log.d(TAG, "color_resource_index: " + color_resource_index);

            if(color_resource_index != -1) {
                // 액션바 위치의 뷰, 백그라운드 컬러 변경
                actionBar_LIN.setBackgroundColor(myapp.folder_color_int_value[color_resource_index]);

                // 설정한 해당 프로젝트 폴더 아이콘으로 변경
                Glide
                    .with(this)
                    .load(myapp.folder_color_resource[color_resource_index])
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(project_folder_img);
            }
        }
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 프로젝트 시작날짜 설정
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.project_start_dt_LIN)
    public void set_project_start_dt() {
        // TODO: redis - 화면 이동
        myapp.Redis_log_view_crossOver_from_to(
                getClass().getSimpleName(), Calendar_D.class.getSimpleName());

        Intent intent = new Intent(this, Calendar_D.class);
        intent.putExtra("for", "start_date");
        intent.putExtra("selected_year", selected_start_year);
        intent.putExtra("selected_month", selected_start_month);
        intent.putExtra("selected_day", selected_start_day);
        intent.putExtra("selected_end_year", selected_end_year);
        intent.putExtra("selected_end_month", selected_end_month);
        intent.putExtra("selected_end_day", selected_end_day);
        intent.putExtra("request_class", getClass().getSimpleName());
        startActivityForResult(intent, REQUEST_SET_START_DATE);
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 프로젝트 종료날짜 설정
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.project_end_dt_LIN)
    public void set_project_end_dt() {
        // TODO: redis - 화면 이동
        myapp.Redis_log_view_crossOver_from_to(
                getClass().getSimpleName(), Calendar_D.class.getSimpleName());

        Intent intent = new Intent(this, Calendar_D.class);
        intent.putExtra("for", "end_date");
        intent.putExtra("selected_year", selected_end_year);
        intent.putExtra("selected_month", selected_end_month);
        intent.putExtra("selected_day", selected_end_day);
        intent.putExtra("selected_start_year", selected_start_year);
        intent.putExtra("selected_start_month", selected_start_month);
        intent.putExtra("selected_start_day", selected_start_day);
        intent.putExtra("request_class", getClass().getSimpleName());
        startActivityForResult(intent, REQUEST_SET_END_DATE);
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 프로젝트 컬러 설정 다이얼로그 액티비티 열기
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.project_color_LIN)
    public void set_project_color() {
        // TODO: redis - 화면 이동
        myapp.Redis_log_view_crossOver_from_to(
                getClass().getSimpleName(), Select_project_color_D.class.getSimpleName());

        Intent intent = new Intent(this, Select_project_color_D.class);
        intent.putExtra("color_resource_index", color_resource_index);
        intent.putExtra("request_class", getClass().getSimpleName());
        startActivityForResult(intent, REQUEST_SET_COLOR);
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 필수 항목 작성여부 체크
            -- '프로젝트 이름, 프로젝트 시작일' --> 이 두개는 반드시 설정되어 있어야함
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.create_project_txt)
    public void form_fillout_check() {

        String project_name_for_check = project_name_edit.getText().toString().replace(" ", "");

        // '프로젝트 이름' 이 작성되지 않았을 때
        if(project_name_for_check.length() == 0) {
            myapp.logAndToast("프로젝트 이름을 작성해주세요.");
            return;
        }
        // '프로젝트 시작일'이 설정되지 않았을 때
        if(int_value_is_dafault("start")) {
            myapp.logAndToast("프로젝트 시작일을 설정해주세요.");
            return;
        }
        // 위 두 조건은 통과함
        // 근데, '프로젝트 종료일'이 설정되지 않았을 때, 이대로 프로젝트를 생성할지 물어보는 다이얼로그 띄우기
        if(int_value_is_dafault("end") || color_resource_index == -1) {

            String alertDialog_comment = "";

            // '프로젝트 종료일, 프로젝트 컬러' 둘 다 미설정일 때
            if(int_value_is_dafault("end") && color_resource_index == -1) {
                alertDialog_comment = "'종료일', '컬러' 가 설정되지 않았습니다.\n이대로 프로젝트를 생성하시겠습니까?";
            }
            // '프로젝트 종료일' 만 미설정일 때
            else if(int_value_is_dafault("end") && color_resource_index != -1) {
                alertDialog_comment = "'종료일'이 설정되지 않았습니다.\n이대로 프로젝트를 생성하시겠습니까?";
            }
            // '프로젝트 컬러' 만 미설정일 때
            else if(!int_value_is_dafault("end") && color_resource_index == -1) {
                alertDialog_comment = "'컬러'가 설정되지 않았습니다.\n이대로 프로젝트를 생성하시겠습니까?";
            }

            // 프로젝트 생성인 경우에만, 확인 다이얼로그 띄우기
            if(!opened_from.equals("meeting_result_list")) {
                create_project_proceed_ofNot(alertDialog_comment);
            }
            // 프로젝트 수정인 경우 바로, 서버전송 메소드를 호출한다
            else if(opened_from.equals("meeting_result_list")) {
                create_project();
            }
        }
        // 모든 항목이 작성되었을 때,
        else if(!int_value_is_dafault("end") && color_resource_index != -1) {
            // 프로젝트 생성 - DB에 업로드, 하는 내부 메소드 호출
            create_project();
        }
    }


    /**---------------------------------------------------------------------------
     메소드 ==> '프로젝트 종료일' 이 설정되지 않은 경우, 그대로 프로젝트를 저장할 것인지 물어보는 AlertDialog
     ---------------------------------------------------------------------------*/
    private void create_project_proceed_ofNot(String comment) {

        AlertDialog.Builder Writing_Restore_choice = new AlertDialog.Builder(this);

        Writing_Restore_choice
                .setMessage(comment)
                .setCancelable(true)
                .setPositiveButton("네",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // '네'를 선택했을 때 실행되는 로직
                                // 프로젝트 생성 - DB에 업로드, 하는 내부 메소드 호출
                                create_project();

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
     메소드 ==> 서버 통신 -- 프로젝트 생성 - DB에 업로드
     ---------------------------------------------------------------------------*/
    private void create_project() {

        // 프로젝트 이름 - 필수
        String project_name = project_name_edit.getText().toString();
        
        
        // 프로젝트 시작월
        // --> 1~9월 사이는 String 으로 변환 시, 자리수가 한자리가 되므로 앞에 "0" 을 추가해준다
        String start_month = String.valueOf(selected_start_month+1);
        if((selected_start_month+1) < 10) {
            start_month = "0" + start_month;
        }

        // 프로젝트 시작월
        // --> 1~9일 사이는 String 으로 변환 시, 자리수가 한자리가 되므로 앞에 "0" 을 추가해준다
        String start_day = String.valueOf(selected_start_day);
        if(selected_start_day < 10) {
            start_day = "0" + start_day;
        }
        
        // 프로젝트 시작일 - 필수
        String project_start_dt_str =
                String.valueOf(selected_start_year) + "-" +
                start_month + "-" +
                start_day;


        // 프로젝트 컬러 str 값 가져오기 - 옵션
        String project_color = "";
        if(color_resource_index != -1) {
            project_color = myapp.folder_color_str[color_resource_index];
        }


        // 프로젝트 종료월
        // --> 1~9월 사이는 String 으로 변환 시, 자리수가 한자리가 되므로 앞에 "0" 을 추가해준다
        String end_month = String.valueOf(selected_end_month+1);
        if((selected_end_month+1) < 10) {
            end_month = "0" + end_month;
        }

        // 프로젝트 종료월
        // --> 1~9일 사이는 String 으로 변환 시, 자리수가 한자리가 되므로 앞에 "0" 을 추가해준다
        String end_day = String.valueOf(selected_end_day);
        if(selected_end_day < 10) {
            end_day = "0" + end_day;
        }

        // 프로젝트 종료일 - 옵션
        String project_end_dt_str = "";
        if(!int_value_is_dafault("end")) {
            project_end_dt_str =
                    String.valueOf(selected_end_year) + "-" +
                    end_month + "-" +
                    end_day;
        }


        RetrofitService rs = ServiceGenerator.createService(RetrofitService.class);

        Call<ResponseBody> call_result = rs.create_project(
                Static.CREATE_PROJECT,
                myapp.getUser_no(),
                project_name,
                project_color,
                project_start_dt_str,
                project_end_dt_str,
                exist_project_no);

        call_result.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String retrofit_result = response.body().string();
                    Log.d(TAG, "retrofit_result: "+retrofit_result);

                    String[] temp = retrofit_result.split("&");

                    if(temp[0].equals("success")) {
                        String created_project_jsonString = temp[1];

                        // 생성한 프로젝트의 'no'를 넣을 번들 객체 생성하여, 넣기
                        Bundle bundle = new Bundle();
                        bundle.putString("created_project_jsonString", created_project_jsonString);

                        // 전달할 msg 객체에 bundle 객체 넣기
                        // msg를 구분할 int 값 넣기
                        Message msg = handler_for_finish.obtainMessage();
                        msg.setData(bundle);
                        msg.what = 0;

                        // 핸들러 메세지 전달
                        handler_for_finish.sendMessage(msg);
                    }
                    else if(temp[0].equals("error")) {
                        myapp.logAndToast("error: " + response.errorBody().string());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                myapp.logAndToast("onFailure_result: " + t.getMessage());
            }
        });

    }


    /**---------------------------------------------------------------------------
     메소드 ==> '년, 월, 일' 값이 '-1' 인지 확인하고, 그 결과를 리턴하는 메소드
     ---------------------------------------------------------------------------*/
    private boolean int_value_is_dafault(String start_or_end) {

        int year = -1;
        int month = -1;
        int day = -1;

        // 체크하려는 프로젝트 설정일이 '시작일' 일 때
        if(start_or_end.equals("start")) {
            year = selected_start_year;
            month = selected_start_month;
            day = selected_start_day;
        }
        // 체크하려는 프로젝트 설정일이 '종료일' 일 때
        else if(start_or_end.equals("end")) {
            year = selected_end_year;
            month = selected_end_month;
            day = selected_end_day;
        }

        if(year==-1 && month==-1 && day==-1) {
            return true;
        }
        else {
            return false;
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
