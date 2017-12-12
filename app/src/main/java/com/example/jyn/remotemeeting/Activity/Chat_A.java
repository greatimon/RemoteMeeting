package com.example.jyn.remotemeeting.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jyn.remotemeeting.DataClass.Chat_room;
import com.example.jyn.remotemeeting.Dialog.Chat_draw_menu_D;
import com.example.jyn.remotemeeting.Otto.BusProvider;
import com.example.jyn.remotemeeting.R;
import com.example.jyn.remotemeeting.Util.Myapp;
import com.github.kimkevin.cachepot.CachePot;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by JYN on 2017-12-11.
 */

public class Chat_A extends Activity {

    private static final String TAG = "all_"+Chat_A.class.getSimpleName();
    int REQUEST_CHAT_DRAW_MENU = 1000;
    Myapp myapp;
    Chat_room chat_room;
    int member_count;
    String chat_room_title;
    String nickName_for_setting;
    String from;

    /** 버터나이프*/
    public Unbinder unbinder;
    @BindView(R.id.setting)         ImageView setting;
    @BindView(R.id.back)            ImageView back;
    @BindView(R.id.attach)          ImageView attach;
    @BindView(R.id.send_msg)        EditText send_msg;
    @BindView(R.id.title)           TextView title;
    @BindView(R.id.counting)        TextView counting;
    @BindView(R.id.send_btn)        TextView send_btn;
    @BindView(R.id.recyclerView)    RecyclerView recyclerView;

    /**---------------------------------------------------------------------------
     생명주기 ==> onCreate
     ---------------------------------------------------------------------------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_chat);

        // 버터나이프 바인드
        unbinder = ButterKnife.bind(this);
        // 어플리케이션 객체 생성
        myapp = Myapp.getInstance();
        // otto 등록
        BusProvider.getBus().register(this);

        Intent intent = getIntent();
        from = intent.getStringExtra("from");

        // CachePot 이용해서 room 객체 받아오기
        chat_room = CachePot.getInstance().pop("chat_room");
        CachePot.getInstance().clear("chat_room");
        Log.d(TAG, "getChatroom_no(): " + chat_room.getChatroom_no());
        Log.d(TAG, "getChat_room_title(): " + chat_room.getChat_room_title());
        Log.d(TAG, "getUser_nickname_arr().toString(): " + chat_room.getUser_nickname_arr().toString());
        Log.d(TAG, "getUser_img_filename_arr().toString(): " + chat_room.getUser_img_filename_arr().toString());

        // 채팅방 리스트로부터 채팅방을 열었을 때
        if(from.equals("list")) {
            Log.d(TAG, "채팅방 리스트로부터 채팅방을 열었다!");
        }
        // 상대방 프로필로부터 채팅방을 열었을 때
        else if(from.equals("profile")) {
            Log.d(TAG, "상대방 프로필로로부터 채팅방을 열었다!");
        }



        // inner 메소드 호출
        set_title_and_counting();

    }


    /**---------------------------------------------------------------------------
     메소드 ==> Chat_room 객체로부터 데이터를 가져와, 방 제목과 방 인원을 표시
     ---------------------------------------------------------------------------*/
    public void set_title_and_counting() {
        member_count = chat_room.getUser_nickname_arr().size();
        chat_room_title = chat_room.getChat_room_title();
        nickName_for_setting = "";

        // 1:1 채팅일 때 - 상대방 프로필로부터 들어왔을 때
        if(chat_room.getUser_img_filename_arr().size() == 1) {
            nickName_for_setting = chat_room.getUser_nickname_arr().get(0);
        }

        // 1:1 채팅일 때 - 채팅방 리스트로부터 들어왔을 때
        else if(chat_room.getUser_img_filename_arr().size() == 2) {
            // 닉네임
            if(chat_room.getUser_nickname_arr().get(0).equals(myapp.getUser_nickname())) {
                nickName_for_setting = chat_room.getUser_nickname_arr().get(1);
            }
            else if(chat_room.getUser_nickname_arr().get(1).equals(myapp.getUser_nickname())) {
                nickName_for_setting = chat_room.getUser_nickname_arr().get(0);
            }
        }
        // TODO: 3명이상의 채팅일 때 - 구현해야함
        else if(chat_room.getUser_img_filename_arr().size() > 2) {

        }

        // 데이터 셋팅 - "none" << 이건 서버에서 방 생성할 때 'chat_room_title' 칼럼의 디폴트 값을 "none"이라고 설정한 것임
        if(chat_room_title.equals("none")) {
            title.setText(nickName_for_setting);
        }
        else if(!chat_room_title.equals("none")) {
            title.setText(chat_room_title);
        }

        // 방 인원 set - 테스트 코드
//        counting.setVisibility(View.VISIBLE);
//        counting.setText(String.valueOf(member_count));

        // 방 인원 set - 채팅 인원이 2명 이상일 때만, 표시함
        if(member_count > 2) {
            counting.setVisibility(View.VISIBLE);
            counting.setText(String.valueOf(member_count));
        }
        if(member_count <= 2) {
            counting.setVisibility(View.GONE);
        }
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 액션바 오른쪽 햄버거 모양 '메뉴' 클릭 -- Chat_draw_menu 다이얼로그 액티비티 열기
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.setting)
    public void setting() {
        Intent intent = new Intent(this, Chat_draw_menu_D.class);
        startActivityForResult(intent, REQUEST_CHAT_DRAW_MENU);
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> back 클릭 -- setResult_and_finish() 메소드 호출
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.back)
    public void back() {
        setResult_and_finish();
    }


    /**---------------------------------------------------------------------------
     오버라이드 ==> onBackPressed -- setResult_and_finish() 메소드 호출
     ---------------------------------------------------------------------------*/
    @Override
    public void onBackPressed() {
        setResult_and_finish();
    }


    /**---------------------------------------------------------------------------
     메소드 ==>
            -- 상대방 프로필로부터 들어는지, 채팅방 리스트로 들어왔는지,
            -- 그 경로가 담긴 값을 intent로 넣어 반환함
            -- Main_after_login_A 액티비티에서 해당 'from'에 해당하는 뷰페이저 페이지를 보여주기 위함
     ---------------------------------------------------------------------------*/
    public void setResult_and_finish() {
        Intent intent = new Intent();
        intent.putExtra("from", from);
        Log.d(TAG, "from: " + from);
        setResult(RESULT_OK, intent);
        finish();
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> attach 클릭 -- 이미지 업로드 선택 (카메라 촬영, 앨범)
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.attach)
    public void attach() {
        onBackPressed();
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> send_btn 클릭 -- 메세지 전송
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.send_btn)
    public void send_btn() {
        onBackPressed();
    }


    /**---------------------------------------------------------------------------
     오버라이드 ==> onActivityResult
     ---------------------------------------------------------------------------*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==REQUEST_CHAT_DRAW_MENU) {

        }
    }


    //    /**---------------------------------------------------------------------------
//     otto ==> RCV_chat_adapter 로 부터 message 수신
//     ---------------------------------------------------------------------------*/
//    @Subscribe
//    public void getMessage(Event.RCV_chat_adapter__Chat_A event) {
//        Log.d(TAG, "otto 받음_ " + event.getMessage());
//
//        // RCV_chat_adapter 로 부터 온 메세지 종류 확인
//        if(event.getMessage().equals("room_ob")) {
//            Chat_room chat_room = event.getChat_room();
//            Log.d(TAG, "getChatroom_no(): " + chat_room.getChatroom_no());
//            Log.d(TAG, "getUser_nickname_arr().toString(): " + chat_room.getUser_nickname_arr().toString());
//            Log.d(TAG, "getUser_img_filename_arr().toString(): " + chat_room.getUser_img_filename_arr().toString());
//        }
//    }


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
        // otto 등록 해제
        BusProvider.getBus().unregister(this);
        super.onDestroy();
    }
}
