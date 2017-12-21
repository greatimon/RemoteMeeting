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
import com.example.jyn.remotemeeting.R;
import com.example.jyn.remotemeeting.Util.Myapp;
import com.google.gson.Gson;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by JYN on 2017-12-21.
 */

public class Add_chat_room_subject_users_D extends Activity {

    /** 버터나이프*/
    public Unbinder unbinder;
    @BindView(R.id.recyclerView)    RecyclerView recyclerView;
    @BindView(R.id.register)        TextView register;
    @BindView(R.id.cancel)          TextView cancel;
    @BindView(R.id.no_result)       TextView no_result;
    @BindView(R.id.btn_layout)      LinearLayout btn_layout;

    private static final String TAG = "all_"+Add_chat_room_subject_users_D.class.getSimpleName();
    Myapp myapp;

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
        register.setText("확인");

        // 리사이클러뷰 동작 메소드 호출
        activate_RCV();
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 지정 취소
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.cancel)
    public void cancel() {
        setResult(RESULT_CANCELED);
        finish();
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 지정
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.register)
    public void register() {

        int target_user_no_hashMap_size = rcv_add_subject_adapter.user_no_hashMap.size();
        if(target_user_no_hashMap_size == 0) {
            myapp.logAndToast("채팅 대상을 선택해 주세요");
            return;
        }

        ArrayList<String> target_user_info_arr = rcv_add_subject_adapter.hers_is_the_target_user_info_arr();

        // 1명일 때, 1:1 채팅방 생성 로직으로
        if(target_user_info_arr.size() == 1) {
            Log.d(TAG, "1:1 채팅방 생성");
        }
        // 2명 이상일 때, 그룹 채팅방 생성 로직으로
        else if(target_user_info_arr.size() > 1) {
            Log.d(TAG, "그룹 채팅방 생성");
        }

        // gson을 이용해서, 채팅방 개설에 필요한 target_user_info_arr를 jsonString 변환 / intent에 넣어
        // Main_after_login_A 로 리턴하고 액티비티를 종료 한다
        Intent intent = new Intent();
        Gson gson = new Gson();
        String return_jsonString = gson.toJson(target_user_info_arr);
        Log.d(TAG, "return_jsonString: " + return_jsonString);
        intent.putExtra("target_user_no_jsonString", return_jsonString);
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
                        this, R.layout.i_add_subject, usersArrayList, "create_chat_room");
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
        // 버터나이프 바인드 해제
        if(unbinder != null) {
            unbinder.unbind();
        }
        // 어플리케이션 객체 null 처리
        myapp = null;
        super.onDestroy();
    }
}
