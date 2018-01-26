package com.example.jyn.remotemeeting.Dialog;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.jyn.remotemeeting.Adapter.RCV_add_subject_adapter;
import com.example.jyn.remotemeeting.DataClass.Users;
import com.example.jyn.remotemeeting.Etc.Static;
import com.example.jyn.remotemeeting.R;
import com.example.jyn.remotemeeting.Util.Myapp;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by JYN on 2017-12-03.
 */

public class Add_subject_user_D extends Activity {

    /** 버터나이프*/
    public Unbinder unbinder;
    @BindView(R.id.recyclerView)    RecyclerView recyclerView;
    @BindView(R.id.register)        TextView register;
    @BindView(R.id.cancel)          TextView cancel;
    @BindView(R.id.no_result)       TextView no_result;
    @BindView(R.id.btn_layout)      LinearLayout btn_layout;

    private static final String TAG = "all_"+Add_subject_user_D.class.getSimpleName();
    Myapp myapp;

    /** 이 클래스를 호출한 클래스 SimpleName */
    String request_class;

    // 리사이클러뷰 관련 클래스
    public RCV_add_subject_adapter rcv_add_subject_adapter;
    public RecyclerView.LayoutManager layoutManager;


    /**---------------------------------------------------------------------------
     생명주기 ==> onCreate
     ---------------------------------------------------------------------------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.v_add_subject_user);
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

        /** 리사이클러뷰 */
        recyclerView.setHasFixedSize(true);
        // 리사이클러뷰 - GridLayoutManager 사용
        layoutManager = new GridLayoutManager(this.getApplicationContext(), 3);
        recyclerView.setLayoutManager(layoutManager);
        // 애니메이션 설정
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // 초기 View Visibility
        no_result.setVisibility(View.GONE);

        // resultOK 버튼에 들어갈 text set
        register.setText("지정");

        // 리사이클러뷰 동작 메소드 호출
        activate_RCV();
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 지정 취소
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.cancel)
    public void cancel(View view) {
        // TODO: redis - 클릭이벤트
        myapp.Redis_log_click_event(getClass().getSimpleName(), view);

        setResult(RESULT_CANCELED);
        finish();
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 지정
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.register)
    public void register(View view) {
        // TODO: redis - 클릭이벤트
        myapp.Redis_log_click_event(getClass().getSimpleName(), view);

        String target_user_no = rcv_add_subject_adapter.added_subject_user_no;
        if(target_user_no.equals("")) {
            myapp.logAndToast("회의 대상을 지정해 주세요");
            return;
        }

        Users target_user = rcv_add_subject_adapter.here_is_the_target_user_info();
        Intent intent = new Intent();
        intent.putExtra("user_no", target_user.getUser_no());
        intent.putExtra("nickname", target_user.getUser_nickname());
        intent.putExtra("email", target_user.getUser_email());
        intent.putExtra("filename", target_user.getUser_img_filename());
        setResult(RESULT_OK, intent);
        finish();
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 서버로부터 내 파트너 리스트 받아와서, 어댑터로 넘기기
     ---------------------------------------------------------------------------*/
    public void activate_RCV() {
        // 서버로 부터 파트너 리스트 받기
        ArrayList<Users> usersArrayList = myapp.get_partner_list();
        Log.d(TAG, "usersArrayList 개수: " + usersArrayList.size());
        Log.d(TAG, "usersArrayList.isEmpty(): " + usersArrayList.isEmpty());

        // 파트너 리스트 결과가 하나도 없다면
        if(usersArrayList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            btn_layout.setVisibility(View.GONE);
            no_result.setVisibility(View.VISIBLE);
        }
        // 파트너 리스트 결과가 잇다면
        else if(!usersArrayList.isEmpty()) {
            no_result.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            btn_layout.setVisibility(View.VISIBLE);

            // 어댑터가 생성되지 않았을 때 -> 어댑터를 생성
            if(rcv_add_subject_adapter == null) {
                // 생성자 인수
                // 1. 액티비티
                // 2. 인플레이팅 되는 레이아웃
                // 3. arrayList users
                // 4. extra 변수
                rcv_add_subject_adapter = new RCV_add_subject_adapter(
                        this, R.layout.i_add_subject, usersArrayList, "create_meeting_room");
                recyclerView.setAdapter(rcv_add_subject_adapter);
                rcv_add_subject_adapter.notifyDataSetChanged();
            }
            // 어댑터가 생성되어 있을때는, 들어가는 arrayList만 교체
            else {
                rcv_add_subject_adapter.refresh_arr(usersArrayList);
            }
        }
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
