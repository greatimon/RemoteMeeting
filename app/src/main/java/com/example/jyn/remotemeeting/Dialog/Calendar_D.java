package com.example.jyn.remotemeeting.Dialog;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.DatePicker;
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


    private static final String TAG = "all_"+Calendar_D.class.getSimpleName();
    Myapp myapp;

    /** 버터나이프*/
    public Unbinder unbinder;
    @BindView(R.id.comment_txt)         TextView comment_txt;
    @BindView(R.id.select_date)         TextView select_date;

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


    // 이 액티비티가 설정하려는 date 구분(프로젝트 시작일 or 프로젝트 종료일)
    String for_str;

    // [2016, 1, 31] 형식의 int 배열
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

        if(for_str != null) {
            Log.d(TAG, "for_str: " + for_str);

            // 프로젝트 시작일 설정일 때
            if(for_str.equals("start_date")) {
                comment_txt.setText("프로젝트 시작일 설정");
            }
            // 프로젝트 종료일 설정일 때
            else if(for_str.equals("end_date")) {
                comment_txt.setText("프로젝트 종료일 설정");
            }
        }

        // TODO: intent로 넘어오는 '년, 월, 일' 이 있는지 확인해서, 만약에 있으면 해당 '년, 월, 일'로 셋팅하고
        // TODO:    만약에 없으면, 오늘 날짜를 셋팅한다
        // TODO: 위에 내용 코딩하기


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
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 날짜 선택
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.select_date)
    public void select_date() {
        Intent finish_intent = new Intent();
        finish_intent.putExtra("for", for_str);
        finish_intent.putExtra("selected_year", selected_year);
        finish_intent.putExtra("selected_month", selected_month);
        finish_intent.putExtra("selected_day", selected_day);
        setResult(RESULT_OK, finish_intent);
        finish();
    }

    //
    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 날짜 선택 취소
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
