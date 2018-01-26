package com.example.jyn.remotemeeting.Dialog;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.jyn.remotemeeting.R;
import com.example.jyn.remotemeeting.Util.Myapp;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by JYN on 2018-01-24.
 */

public class Calendar_D extends Activity implements OnDateSelectedListener {


    String request_class;
    private static final String TAG = "all_"+Calendar_D.class.getSimpleName();
    Myapp myapp;

    /** 버터나이프*/
    public Unbinder unbinder;
    @BindView(R.id.comment_txt)                 TextView comment_txt;
    @BindView(R.id.already_selected_date)       TextView already_selected_date;
    @BindView(R.id.select_date)                 TextView select_date;

    // 캘린더뷰
    public MaterialCalendarView calendarView;

    // 데이트 포맷
    private static final DateFormat FORMATTER = SimpleDateFormat.getDateInstance();

    // 설정한 date String 값
    String selected_date_str = "";

    // 설정한 year, month, day 값
    int selected_year = -1;
    int selected_month = -1;
    int selected_day = -1;

    // 이미 앞서 선택한 '프로젝트 시작 날짜'
    int selected_start_year = -1;
    int selected_start_month = -1;
    int selected_start_day = -1;

    // 이미 앞서 선택한 '프로젝트 종료 날짜'
    int selected_end_year = -1;
    int selected_end_month = -1;
    int selected_end_day = -1;

    // 이 액티비티가 설정하려는 date 구분(프로젝트 시작일 or 프로젝트 종료일)
    String for_str;

    // '[2016, 0, 31]' 형식의 int 배열
    int[] year_month_day = new int[3];


    /**---------------------------------------------------------------------------
     생명주기 ==> onCreate
     ---------------------------------------------------------------------------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.v_calendar);
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
        request_class = get_intent.getStringExtra("request_class");

        // 캘린더뷰 뷰 바인딩, 리스너 등록
        calendarView = findViewById(R.id.calendarView);
        calendarView.setOnDateChangedListener(this);

        // 캐린더 타일 사이즈 조절
//        calendarView.setTileSizeDp(35);
        // 캘린더 색상 설정
        calendarView.setArrowColor(Color.parseColor("#4CAF50"));
        calendarView.setSelectionColor(Color.parseColor("#4CAF50"));

        // 인텐트값 받기
        Intent intent = getIntent();
        for_str = intent.getStringExtra("for");
        selected_year = intent.getIntExtra("selected_year", -1);
        selected_month = intent.getIntExtra("selected_month", -1);
        selected_day = intent.getIntExtra("selected_day", -1);

        if(for_str != null) {
            Log.d(TAG, "for_str: " + for_str);

            // 프로젝트 시작일 설정일 때
            if(for_str.equals("start_date")) {
                comment_txt.setText("프로젝트 시작일 설정");

                // 추가적으로 프로젝트 종료일도 받기
                selected_end_year = intent.getIntExtra("selected_end_year", -1);
                selected_end_month = intent.getIntExtra("selected_end_month", -1);
                selected_end_day = intent.getIntExtra("selected_end_day", -1);

                // 넘겨받은 '년월일' intent 값이 '-1'이 아니라, 설정값이 있다면
                // 설정된 '프로젝트 종료일'을 텍스트뷰에 보여준다
                if(!int_value_is_dafault(selected_end_year, selected_end_month, selected_end_day)) {
                    // 프로젝트 종료일 데이터 setText 하기
                    String set_this_str =
                            "설정된 프로젝트 종료일: " +
                            String.valueOf(selected_end_year) + "." +
                            String.valueOf(selected_end_month+1) + "." +
                            String.valueOf(selected_end_day);
                    already_selected_date.setVisibility(View.VISIBLE);
                    already_selected_date.setText(set_this_str);
                }
            }
            // 프로젝트 종료일 설정일 때
            else if(for_str.equals("end_date")) {
                comment_txt.setText("프로젝트 종료일 설정");

                // 추가적으로 프로젝트 시작일도 받기
                selected_start_year = intent.getIntExtra("selected_start_year", -1);
                selected_start_month = intent.getIntExtra("selected_start_month", -1);
                selected_start_day = intent.getIntExtra("selected_start_day", -1);

                // 넘겨받은 '년월일' intent 값이 '-1'이 아니라, 설정값이 있다면
                // 설정된 '프로젝트 시작일'을 텍스트뷰에 보여준다
                if(!int_value_is_dafault(selected_start_year, selected_start_month, selected_start_day)) {
                    // 프로젝트 시작일 데이터 setText 하기
                    String set_this_str =
                            "설정된 프로젝트 시작일: " +
                            String.valueOf(selected_start_year) + "." +
                            String.valueOf(selected_start_month+1) + "." +
                            String.valueOf(selected_start_day);
                    already_selected_date.setVisibility(View.VISIBLE);
                    already_selected_date.setText(set_this_str);
                }
            }
        }

        // 인텐트로 값을 넘겨 받았음에도, '년, 월, 일' 값이 디폴트 값이 '-1'인 경우,
        // 오늘 날짜를 기준으로 캘린더뷰에 날짜를 선택하기
        if(selected_year==-1 && selected_month==-1 && selected_day==-1) {
            // 오늘 날짜에 해당하는 '년, 월, 일' int 값들을 배열로 리턴받기
            // # '월'은 원래 월의 -1 된 int 값을 리턴 받음
            // ex> 1월 --> '0'을 리턴 받음
            year_month_day = myapp.year_month_day();
            Log.d(TAG, "year_month_day[0]: " + year_month_day[0]);
            Log.d(TAG, "year_month_day[1]: " + year_month_day[1]);
            Log.d(TAG, "year_month_day[2]: " + year_month_day[2]);

            // 캘린더뷰에서, 오늘 날짜를 select 하기
            calendarView.setSelectedDate(
                    CalendarDay.from(year_month_day[0], year_month_day[1], year_month_day[2]));
            // 캘린더뷰에서, 오늘 날짜로 포커스 주기
            calendarView.setCurrentDate(
                    CalendarDay.from(year_month_day[0], year_month_day[1], year_month_day[2]));

            // '년, 월, 일' 값 오늘 날짜 데이터로 설정하기
            selected_year = year_month_day[0];
            selected_month = year_month_day[1];
            selected_day = year_month_day[2];
        }
        // 인텐트로 값을 넘겨 받은 뒤, '년, 월, 일' 값이 디폴트 값이 '-1'이 아닌 경우,
        // 해당 날짜로 캘린더뷰에 select 하기
        else {
            // 캘린더뷰에서, 해당 날짜를 select 하기
            calendarView.setSelectedDate(
                    CalendarDay.from(selected_year, selected_month, selected_day));
            // 캘린더뷰에서, 해당 날짜로 포커스 주기
            calendarView.setCurrentDate(
                    CalendarDay.from(selected_year, selected_month, selected_day));
        }
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 날짜 선택
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.select_date)
    public void select_date(View view) {
        // TODO: redis - 클릭이벤트
        myapp.Redis_log_click_event(getClass().getSimpleName(), view);

        // 프로젝트 시작일 설정일 때
        if(for_str.equals("start_date")) {
            // 인텐트로 넘겨 받은 프로젝트 종료일이 '-1'값이 아니라, 설정된 값이 있을 때 비교 한다
            if(!int_value_is_dafault(selected_end_year, selected_end_month, selected_end_day)) {

                // 비교 결과
                // 1. start_date == end_date 일 경우 return 0
                // 2. start_date > end_date 일 경우 return 1
                // 3. start_date < end_date 일 경우 return -1
                int check_result =
                        compare_date(selected_year, selected_month, selected_day,
                        selected_end_year, selected_end_month, selected_end_day);

                if(check_result == 1) {
                    myapp.logAndToast("프로젝트 시작일이 종료일보다 나중일 수 없습니다.");
                }
                // 조건을 모두 통과한 경우에, 선택한 날짜 데이터를 인텐트로 넘기면서 액티비티를 종료한다
                else {
                    setResult_and_finish();
                }
            }
            // 인텐트로 넘겨받은 프로젝트 종료일이 '-1'인 경우, 즉 넘겨받은 인텐트 값이 없는 경우,
            // 선택한 날짜 데이터를 인텐트로 넘기면서 액티비티를 종료한다
            else {
                setResult_and_finish();
            }
        }
        // 프로젝트 종료일 설정일 때
        else if(for_str.equals("end_date")) {
            // 인텐트로 넘겨 받은 프로젝트 시작일이 '-1'값이 아니라, 설정된 값이 있을 때 비교 한다
            if(!int_value_is_dafault(selected_start_year, selected_start_month, selected_start_day)) {
                // 비교 결과
                // 1. end_date == start_date 일 경우 return 0
                // 2. end_date > start_date 일 경우 return 1
                // 3. end_date < start_date 일 경우 return -1
                int check_result =
                        compare_date(selected_year, selected_month, selected_day,
                                selected_start_year, selected_start_month, selected_start_day);

                if(check_result == -1) {
                    myapp.logAndToast("프로젝트 종료일이 시작일보다 빠를 수 없습니다.");
                }
                // 조건을 모두 통과한 경우에, 선택한 날짜 데이터를 인텐트로 넘기면서 액티비티를 종료한다
                else {
                    setResult_and_finish();
                }
            }
            // 인텐트로 넘겨받은 프로젝트 시작일이 '-1'인 경우, 즉 넘겨받은 인텐트 값이 없는 경우,
            // 선택한 날짜 데이터를 인텐트로 넘기면서 액티비티를 종료한다
            else {
                setResult_and_finish();
            }
        }
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 날짜 초기화
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.cancel)
    public void select_cancel() {
        Intent finish_intent = new Intent();
        finish_intent.putExtra("for", for_str);
        finish_intent.putExtra("selected_year", -1);
        finish_intent.putExtra("selected_month", -1);
        finish_intent.putExtra("selected_day", -1);
        setResult(RESULT_OK, finish_intent);
        finish();
    }


    /**---------------------------------------------------------------------------
     콜백메소드 ==> onDateSelected -- 캘린더, 날짜 선택 시 호출되는 메소드
     ---------------------------------------------------------------------------*/
    @Override
    public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
        selected_date_str = FORMATTER.format(date.getDate());
        Log.d(TAG, "selected_date_str: " + selected_date_str);

        selected_year = date.getYear();
        selected_month = date.getMonth();
        selected_day = date.getDay();
        Log.d(TAG, "date.getYear(): " + date.getYear());
        Log.d(TAG, "date.getMonth: " + date.getMonth());
        Log.d(TAG, "date.getDate: " + date.getDay());
    }


    /**---------------------------------------------------------------------------
     메소드 ==> '년, 월, 일' 값이 '-1' 인지 확인하고, 그 결과를 리턴하는 메소드
     ---------------------------------------------------------------------------*/
    private boolean int_value_is_dafault(int year, int month, int day) {
        if(year==-1 && month==-1 && day==-1) {
            return true;
        }
        else {
            return false;
        }
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 두 날짜를 비교하여, 비교 결과를 리턴하는 메소드
     ---------------------------------------------------------------------------*/
    // 첫번째 3개의 '년월일' - 캘린더에서 설정한 '년월일'
    // 두번째 3개의 '년월일' - 비교 대상 '년월일'
    private int compare_date(int year_1, int month_1, int day_1,
                              int year_2, int month_2, int day_2) {
        Calendar subject_cal = Calendar.getInstance();
        subject_cal.set(year_1, month_1, day_1);

        Calendar comparison_cal = Calendar.getInstance();
        comparison_cal.set(year_2, month_2, day_2);

        int result = subject_cal.compareTo(comparison_cal);
        Log.d(TAG, "compare_result: " + result);
        // 1. subject_cal == comparison_cal 일 경우 return 0
        // 2. subject_cal > comparison_cal 일 경우 return 1
        // 3. subject_cal < comparison_cal 일 경우 return -1

        return result;
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 캘린더에서 선택한 '년월일' 데이터를 intent로 반환하며 액티비티를 종료
     ---------------------------------------------------------------------------*/
    public void setResult_and_finish() {
        Intent finish_intent = new Intent();
        finish_intent.putExtra("for", for_str);
        finish_intent.putExtra("selected_year", selected_year);
        finish_intent.putExtra("selected_month", selected_month);
        finish_intent.putExtra("selected_day", selected_day);
        setResult(RESULT_OK, finish_intent);
        finish();
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
        super.onDestroy();
    }
}
