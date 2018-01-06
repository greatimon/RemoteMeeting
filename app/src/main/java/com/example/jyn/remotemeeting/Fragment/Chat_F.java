package com.example.jyn.remotemeeting.Fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.example.jyn.remotemeeting.Activity.Main_after_login_A;
import com.example.jyn.remotemeeting.Adapter.RCV_chatRoom_list_adapter;
import com.example.jyn.remotemeeting.DataClass.Chat_log;
import com.example.jyn.remotemeeting.DataClass.Chat_room;
import com.example.jyn.remotemeeting.DataClass.Users;
import com.example.jyn.remotemeeting.Etc.Static;
import com.example.jyn.remotemeeting.Otto.BusProvider;
import com.example.jyn.remotemeeting.Otto.Event;
import com.example.jyn.remotemeeting.R;
import com.example.jyn.remotemeeting.Util.Myapp;
import com.example.jyn.remotemeeting.Util.RetrofitService;
import com.example.jyn.remotemeeting.Util.ServiceGenerator;
import com.example.jyn.remotemeeting.Util.SimpleDividerItemDecoration;
import com.google.gson.Gson;
import com.squareup.otto.Subscribe;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by JYN on 2017-11-10.
 */

public class Chat_F extends Fragment {

    private static final String TAG = "all_"+Chat_F.class.getSimpleName();
    public static String JSON_TAG_CHAT_ROOM_LIST = "chat_room_list";
    LayoutInflater inflater;
    ViewGroup container;
    View controlView;
    Myapp myapp;

    /** 버터나이프*/
    public Unbinder unbinder;
//    @BindView(R.id.recyclerView)        RecyclerView recyclerView;
//    @BindView(R.id.no_result)           TextView no_result;
    public static RecyclerView recyclerView;
    public static TextView no_result;

    // 리사이클러뷰 관련 클래스
    public RCV_chatRoom_list_adapter rcv_chat_Roomlist_adapter;
    public RecyclerView.LayoutManager layoutManager;

    // 서버로 부터 채팅방 리스트 받아서 담아놓을 변수
    public ArrayList<Chat_room> rooms;

    // 서버로 부터 채팅방 리스트중, 그룹채팅방의 개수
    public int group_chat_count = 0;

    // 그룹채팅방 대표 이미지 변환 처리완료 횟수를 저장할 변수
    public int combined_complete_count = 0;

    // 그룹채팅방의 대표 이미지 관련처리가 완료됐을 때 채팅방 리스트에 해당 비트맵을 넣기 위해 사용하는 핸들러
    public static Handler handler;

    // 채팅방 리스트 전체를 서버로부터 받아 올 때,
    // 그룹채팅 대표 이미지들의 Bitmap들을 임시로 담고, 처리가 완료되면 핸들러로 전달하기 위한 해쉬맵
    ConcurrentHashMap<Integer, ArrayList<Bitmap>> temp_bitmap_hash;

    // 새로 생성된 채팅방 한개를 서버로부터 받아 올 때,
    // 그룹채팅 대표 이미지들의 Bitmap들을 임시로 담고, 처리가 완료되면 핸들러로 전달하기 위한 해쉬맵
    ConcurrentHashMap<Integer, ArrayList<Bitmap>> temp_bitmap_hash_new;

    public Chat_F() {
        // Required empty public constructor
    }

    public static Chat_F newInstance() {
        Bundle args = new Bundle();

        Chat_F fragment = new Chat_F();
        fragment.setArguments(args);
        return fragment;
    }


    /**---------------------------------------------------------------------------
     생명주기 ==> onCreateView
     ---------------------------------------------------------------------------*/
    @SuppressLint("HandlerLeak")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "Chat_F_생명주기 onCreateView");
        this.inflater = inflater;
        this.container = container;

        // 프래그먼트 인플레이팅
        controlView = inflater.inflate(R.layout.f_chat, container, false);
        // 뷰 찾기
        recyclerView = controlView.findViewById(R.id.recyclerView);
        no_result = controlView.findViewById(R.id.no_result);

        // 버터나이프 바인드
        unbinder = ButterKnife.bind(this, controlView);
        // 어플리케이션 객체 생성
        myapp = Myapp.getInstance();
        // otto 등록
        BusProvider.getBus().register(this);

        temp_bitmap_hash = new ConcurrentHashMap<>();
        temp_bitmap_hash_new = new ConcurrentHashMap<>();

        /** 리사이클러뷰 */
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        // 리사이클러뷰 구분선 - 가로(클래스 생성)
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity(), "Chat_F"));
        // 애니메이션 설정 - 애니메이션 설정 끔
        ((SimpleItemAnimator)recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        // 애니메이션 설정 - 기본 애니메이션
//        recyclerView.setItemAnimator(new DefaultItemAnimator());

        /** 리사이클러뷰 스크롤 리스너 - 테스트용도로 사용하였음, 실제로는 안씀 */
//        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//            }
//
//            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//                if(newState == SCROLL_STATE_SETTLING) {
//                    Log.d(TAG, "========================SCROLL_STATE_SETTLING========================");
//                    Log.d(TAG, "newState: "  + newState);
//                    Log.d(TAG, "recyclerView.getScrollState(): " + recyclerView.getScrollState());
//                    if(recyclerView.canScrollVertically(1)) {
//                        Log.d(TAG, "최상단임!!!!");
//                    }
//                    else if(!recyclerView.canScrollVertically(1)) {
//                        Log.d(TAG, "최하단임!!!!");
//                    }
//                }
//                if(newState == SCROLL_STATE_DRAGGING) {
//                    Log.d(TAG, "========================SCROLL_STATE_DRAGGING========================");
//                    Log.d(TAG, "newState: "  + newState);
//                    Log.d(TAG, "recyclerView.getScrollState(): " + recyclerView.getScrollState());
//                }
//                if(newState == SCROLL_STATE_IDLE) {
//                    Log.d(TAG, "=======================SCROLL_STATE_IDLE=========================");
//                    Log.d(TAG, "newState: "  + newState);
//                    Log.d(TAG, "recyclerView.getScrollState(): " + recyclerView.getScrollState());
//                }
//
//            }
//        });


        // 그룹채팅방의 대표 이미지 관련처리가 완료됐을 때 채팅방 리스트에 해당 비트맵을 넣기 위해 사용하는 핸들러
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                // Chat_F에서
                // 채팅방 리스트 전체를 받아올 때, 전달되는 핸들러메세지
                if(msg.what == 0) {
                    Log.d(TAG, "Chat_F에서로부터 핸들러 메세지 전달 받음!!!!!!!!!!!!");
                    // Message 객체로부터 채팅방 번호와, 비트맵 객체 가져오기
                    int target_chatRoom_no = Integer.parseInt(msg.getData().getString("target_chatRoom_no"));
                    Bitmap combined_bitmap = (Bitmap)msg.obj;

                    // 어댑터로 메소드 호출하여, 해당 비트맵 이미지 전달하고,
                    // 해당 임시 해쉬맵 삭제
                    rcv_chat_Roomlist_adapter.set_group_chat_representatice_image(target_chatRoom_no, combined_bitmap);
                    if(temp_bitmap_hash.containsKey(target_chatRoom_no)) {
                        temp_bitmap_hash.remove(target_chatRoom_no);
                    }


//                    combined_complete_count++;
//                    Log.d(TAG, "combined_complete_count: " + combined_complete_count);
//
//                    // 총 그룹채팅방의 개수와, 그룹채팅방 대표이미지 관련 처리 완료 개수와 일치 했을 때 어댑터로 arraylist를 넘긴다
//                    if(group_chat_count == combined_complete_count) {
//                        Log.d(TAG, "group_chat_count == combined_complete_count");
//
//                        // 어댑터 메소드 호출 - 서버로부터 받아온 채팅방 리스트 전체 중, 그룹 채팅 대표 이미지를 업데이트 해라
//                        if(rcv_chat_Roomlist_adapter != null) {
//                            rcv_chat_Roomlist_adapter.refresh_group_chat_Representative_image();
//                            group_chat_count = 0;
//                            combined_complete_count = 0;
//                        }
//                    }
                }
                // RCV_chatRoom_list_adapter 로부터
                // 새로운 채팅방 리스트가 추가 되었을 때, 전달되는 핸들러메세지
                else if(msg.what == 1) {
                    Log.d(TAG, "RCV_chatRoom_list_adapter로부터 핸들러 메세지 전달 받음!!!!!!!!!!!!");
//                    // Message 객체로부터 채팅방 번호와, 비트맵 객체 가져오기
//                    int target_chatRoom_no = Integer.parseInt(msg.getData().getString("target_chatRoom_no"));
//                    Bitmap combined_bitmap = (Bitmap)msg.obj;
//
//                    // 어플리케이션 객체의 해쉬맵에 넣기
//                    myapp.getCombined_bitmap_hash().put(target_chatRoom_no, combined_bitmap);
//
//                    // 어댑터 메소드 호출 - 서버로부터 받아온 하나의 채팅방이 그룹채팅방일 경우,
//                    // 그룹채팅방 대표 이미지를 업데이트 해라
//                    rcv_chat_Roomlist_adapter.refresh_group_chat_Representative_image();

                    // Message 객체로부터 채팅방 번호와, 비트맵 객체 가져오기
                    int target_chatRoom_no = msg.getData().getInt("target_chatRoom_no");
                    String img_filename_arr = msg.getData().getString("img_filename_arr");
                    Log.d(TAG, "img_filename_arr: " + img_filename_arr);
                    ArrayList<Users> received_user_arr = (ArrayList<Users>)msg.obj;
                    Log.d(TAG, "핸들러_ received_user_arr.size(): " + received_user_arr.size());

                    boolean contain_me_orNot = false;
                    for(int i=0; i<received_user_arr.size(); i++) {
                        Log.d(TAG, "received_user_arr.get(i).getUser_nickname(): " + received_user_arr.get(i).getUser_nickname());
                        if(received_user_arr.get(i).getUser_no().equals(myapp.getUser_no())) {
                            contain_me_orNot = true;
                        }
                    }

                    // 만약에 내 정보가 없다면, 내 user 객체 만들어서 arr에 add 하기
                    if(!contain_me_orNot) {
                        Users user = new Users();
//                        this.user_no = user_object.getString("user_no");
//                        this.join_path = user_object.getString("join_path");
//                        this.join_dt = user_object.getString("join_dt");
//                        this.user_email = user_object.getString("user_email");
//                        this.user_nickname = user_object.getString("user_nickname");
//                        this.present_meeting_in_ornot = user_object.getString("present_meeting_in_ornot");
//                        this.user_img_filename = user_object.getString("user_img_filename");
                        user.setUser_no(myapp.getUser_no());
                        user.setJoin_path(myapp.getJoin_path());
                        user.setJoin_path(myapp.getJoin_path());
                        user.setUser_email(myapp.getUser_email());
                        user.setUser_nickname(myapp.getUser_nickname());
                        user.setPresent_meeting_in_ornot(myapp.getPresent_meeting_in_ornot());
                        user.setUser_img_filename(myapp.getUser_img_filename());
                        received_user_arr.add(user);
                    }

                    // 이미지 합치기 메소드 호출
                    new_chatRoom_group_img_combine(target_chatRoom_no, received_user_arr);
                }
                // Chat_F 로부터
                // 이미지 합치기가 완료되었을 때, 전달되는 핸들러 메세지
                else if(msg.what == 2) {
//                    Log.d(TAG, "Chat_F 로부터 이미지 합치기가 완료되었다는 핸들러 메세지 전달 받음!!!!!!!!!!!!");
//                    // Message 객체로부터 채팅방 번호와, 비트맵 객체 가져오기
//                    int target_chatRoom_no = Integer.parseInt(msg.getData().getString("target_chatRoom_no"));
//                    Bitmap combined_bitmap = (Bitmap)msg.obj;
//                    // 어플리케이션 객체의 해쉬맵에 넣기
//                    myapp.getCombined_bitmap_hash().put(target_chatRoom_no, combined_bitmap);
//                    // 어댑터 메소드 호출 - 서버로부터 받아온 하나의 채팅방이 그룹채팅방일 경우,
//                    // 그룹채팅방 대표 이미지를 업데이트 해라
//                    rcv_chat_Roomlist_adapter.refresh_group_chat_Representative_image();

                    // Message 객체로부터 채팅방 번호와, 비트맵 객체 가져오기
                    int target_chatRoom_no = Integer.parseInt(msg.getData().getString("target_chatRoom_no"));
                    Bitmap combined_bitmap = (Bitmap)msg.obj;

                    // 어댑터로 메소드 호출하여, 해당 비트맵 이미지 전달하고,
                    // 해당 임시 해쉬맵 삭제
                    rcv_chat_Roomlist_adapter.set_group_chat_representatice_image(target_chatRoom_no, combined_bitmap);
                    if(temp_bitmap_hash.containsKey(target_chatRoom_no)) {
                        temp_bitmap_hash.remove(target_chatRoom_no);
                    }
                }
            }
        };

        // 리사이클러뷰 동작 메소드 호출
        activate_RCV();

        return controlView;
    }

    /**---------------------------------------------------------------------------
     otto ==> Main_after_login_A로 부터 message 수신
     ---------------------------------------------------------------------------*/
    @Subscribe
    public void getMessage(Event.Main_after_login_A__Chat_F event) {
        Log.d(TAG, "otto 받음_ " + event.getMessage());
        if(event.getMessage().equals("activate_File_RCV")) {
            // 리사이클러뷰 동작 메소드 호출
            activate_RCV();
        }
    }


    /**---------------------------------------------------------------------------
     otto ==> Chat_handler로 부터 message 수신
     ---------------------------------------------------------------------------*/
    @Subscribe
    public void getMessage(final Event.Chat_handler__Chat_F event) {
        Log.d(TAG, "otto 받음_ " + event.getMessage());

        // 해당 채팅방에 대한 안읽은 메세지 개수만 서버로 부터 받아오기
        int target_chatroom_no = event.getData().getChat_log().getChat_room_no();

        RetrofitService rs = ServiceGenerator.createService(RetrofitService.class);
        Call<ResponseBody> call = rs.get_unread_msg_count(
                Static.GET_UNREAD_MSG_COUNT,
                myapp.getUser_no(), target_chatroom_no);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String result = response.body().string();
                    Log.d(TAG, "get_unread_msg_count_result: "+result);

                    if(result.equals("-1")) {

                    }
                    else if(result.equals("0")) {

                    }
                    // 받아온 msg_unread_count를 전달할 객체에 set
                    else {
                        event.getData().getChat_log().setMsg_unread_count(Integer.parseInt(result));
                        // 어댑터로 연결
                        // 바로 어댑터로 연결 안하고, Chat_F를 거치는 이유
                        // - 어댑터 쪽에서는 otto 등록은 가능한데, 해제를 어느쪽에서 해야할지 몰라, 안정성을 위해 한번 거침
                        // (Chat_F는 해제할 부분이 확실함)
                        rcv_chat_Roomlist_adapter.update_last_msg(event.getMessage(), event.getData());
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                myapp.logAndToast("onFailure_result" + t.getMessage());
            }
        });
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 서버로부터 채팅방 리스트 받아와서, 어댑터로 넘기기
     ---------------------------------------------------------------------------*/
    public void activate_RCV() {
        if(myapp == null) {
            // 어플리케이션 객체 생성
            myapp = Myapp.getInstance();
        }
        // 서버로 부터 채팅방 리스트 받기
//        final ArrayList<Chat_room> rooms = get_chat_room_list();
        rooms = get_chat_room_list();
        Log.d(TAG, "usersArrayList.isEmpty(): " + rooms.isEmpty());

        group_chat_count = 0;
        // 그룹채팅방 개수 구하기
        for(int k=0; k<rooms.size(); k++) {
            // 채팅방 인원이 2명보다 많을 때, 체크
            Log.d(TAG, "rooms.get(k).getUser_arr().size(): " + rooms.get(k).getUser_arr().size());
            Log.d(TAG, "rooms.get(k).getChatroom_no(): " + rooms.get(k).getChatroom_no());
            if(rooms.get(k).getUser_arr().size() > 2) {
                group_chat_count++;
            }
        }
        Log.d(TAG, "group_chat_count: " + group_chat_count);

        // 임시 해쉬맵 초기화
//        temp_bitmap_hash.clear();

        /** 전달받은 채팅방 중, 참여인원이 2명을 초과하는 채팅방 대표 이미지를 만드는 로직 */
        for(int j=0; j<rooms.size(); j++) {

            if(rooms.get(j).getUser_arr().size() > 2) {
//                Log.d(TAG, "rooms.get(j).getUser_arr().size() > 3: " + rooms.get(j).getUser_arr().size());

                // rooms.get(j)를 복사하여 임시 Chat_room 객체 생성
                final Chat_room temp_Chat_room = rooms.get(j);

                // 해당 chatroom_no를 복사하여, 임시로 target_chatRoom_no 담아놓기
                final int target_chatRoom_no = temp_Chat_room.getChatroom_no();

                // 해당 target_chatRoom_no을 키 값으로 하는 해쉬맵 아이템이 있다면, 지우기(초기화)
                if(temp_bitmap_hash.containsKey(target_chatRoom_no)) {
                    temp_bitmap_hash.remove(target_chatRoom_no);
                }

                // 채팅방에 참여중인 사람들의 수
                final int user_nums = temp_Chat_room.getUser_arr().size();
                Log.d(TAG, "user_nums: " + user_nums);
//                Log.d(TAG, "temp_filename_arr_size: " + temp_filename_arr_size);

                // for 문
                for(int i = 0; i<user_nums; i++) {

                    // target_chatRoom_no을 키 값으로 하는 해쉬맵 아이템이 존재할 때
                    if(temp_bitmap_hash.containsKey(target_chatRoom_no)) {
//                        // 채팅 참여인원이 3명이고, 비트맵 어레이개수가 3개면
//                        if(user_nums == 3 && temp_bitmap_hash.get(target_chatRoom_no).size() == 3) {
//                            // 마지막 비트맵 어레이 아이템으로, 프로필 사진이 없는 회색 배경 비트맵을 추가
//                            temp_bitmap_hash.get(target_chatRoom_no).add(myapp.get_blank_gray_view());
//                            Log.d(TAG, "3명일 때, 빈 회색 비트맵을 추가하고 난 뒤 비트맵 개수: "
//                                    + temp_bitmap_hash.get(target_chatRoom_no).size());
//
//                            // 해당 채팅방의 참여한 유저들의 비트맵들과, 채팅방 번호를 메소드로 넘김
//                            // --> 이미지 4장 합쳐서 해당 아이템에 보여주기 위함
//                            // 매개변수 1. 이 채팅방 번호
//                            // 매개변수 2. 비트맵들을 담은 ArrayList
//                            new Thread() {
//                                @Override
//                                public void run() {
//                                    img_bitmap_put_complete(target_chatRoom_no, temp_bitmap_hash.get(target_chatRoom_no), "whole");
//                                }
//                            }.start();
//                        }
//                        // 채팅 참여 인원이 4명이상일 때, 비트맵 아이템의 개수가 4개 이상이면
//                        // 바로 비트맵 붙이기 메소드 호출
//                        else if(user_nums >= 4 && temp_bitmap_hash.get(target_chatRoom_no).size() >= 4) {
//                            new Thread() {
//                                @Override
//                                public void run() {
//                                    img_bitmap_put_complete(target_chatRoom_no, temp_bitmap_hash.get(target_chatRoom_no), "whole");
//                                }
//                            }.start();
//                        }
                    }

                    final int finalI = i;
                    String img_fileName = "";

                    if(!temp_Chat_room.getUser_arr().get(i).getUser_img_filename().equals("none")) {
                        img_fileName = temp_Chat_room.getUser_arr().get(i).getUser_img_filename();
                    }
                    else if(temp_Chat_room.getUser_arr().get(i).getUser_img_filename().equals("none")) {
                        img_fileName = "default_profile_for_combine.png";
                    }

                    Glide
                        .with(getContext())
                        .load(Static.SERVER_URL_PROFILE_FILE_FOLDER + img_fileName)
                        .asBitmap()
                        .override(100, 100)
                        .fitCenter()
                        .into(new SimpleTarget<Bitmap>() {

                            int this_target_chatRoom_no = target_chatRoom_no;
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {

                                // 이미 해당 채팅방을 키 값으로 하는 해쉬맵 밸류값이 있다면
                                if(temp_bitmap_hash.containsKey(this_target_chatRoom_no)) {
                                    temp_bitmap_hash.get(this_target_chatRoom_no).add(resource);
                                }
                                // 이미 해당 채팅방을 키 값으로 하는 해쉬맵 밸류값이 없다면
                                else if(!temp_bitmap_hash.containsKey(this_target_chatRoom_no)) {

                                    // bitmap들을 담을 새로운 ArrayList를 생성하고,
                                    // 해당 채팅방을 키 값으로 하는 해쉬맵 arr item을 put 한 뒤
                                    ArrayList<Bitmap> bitmap_arr = new ArrayList<>();
                                    temp_bitmap_hash.put(this_target_chatRoom_no, bitmap_arr);
                                    // 해당 해쉬맵 아이템 밸류인 arr에 해당 bitmap을 넣는다
                                    temp_bitmap_hash.get(this_target_chatRoom_no).add(resource);
                                }

                                // 채팅 참여인원이 3명이고, 비트맵 어레이개수가 3개면
                                if(user_nums == 3 && temp_bitmap_hash.get(target_chatRoom_no).size() == 3) {
                                    // 마지막 비트맵 어레이 아이템으로, 프로필 사진이 없는 회색 배경 비트맵을 추가
                                    temp_bitmap_hash.get(target_chatRoom_no).add(myapp.get_blank_gray_view());
                                    Log.d(TAG, "3명일 때, 빈 회색 비트맵을 추가하고 난 뒤 비트맵 개수: "
                                            + temp_bitmap_hash.get(target_chatRoom_no).size());

                                    // 해당 채팅방의 참여한 유저들의 비트맵들과, 채팅방 번호를 메소드로 넘김
                                    // --> 이미지 4장 합쳐서 해당 아이템에 보여주기 위함
                                    // 매개변수 1. 이 채팅방 번호
                                    // 매개변수 2. 비트맵들을 담은 ArrayList
                                    new Thread() {
                                        @Override
                                        public void run() {
                                            img_bitmap_put_complete(target_chatRoom_no, temp_bitmap_hash.get(target_chatRoom_no), "whole");
                                        }
                                    }.start();
                                }
                                // 채팅 참여 인원이 4명이상일 때, 비트맵 아이템의 개수가 4개 이상이면
                                // 바로 비트맵 붙이기 메소드 호출
                                else if(user_nums >= 4 && temp_bitmap_hash.get(target_chatRoom_no).size() == user_nums) {
                                    new Thread() {
                                        @Override
                                        public void run() {
                                            img_bitmap_put_complete(target_chatRoom_no, temp_bitmap_hash.get(target_chatRoom_no), "whole");
                                        }
                                    }.start();
                                }
                            }
                        });
                }
            }

//            // for문 마지막
//            if(j == rooms.size()-1) {
//                handler.sendEmptyMessage(1);
//            }
        }

        // 채팅방 리스트가 하나도 없다면
        if(rooms.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            no_result.setVisibility(View.VISIBLE);
        }
        // 채팅방 리스트가 있다면
        else if(!rooms.isEmpty()) {
            Log.d(TAG, "================================================rooms.size(): " + rooms.size());
            no_result.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        // 어댑터가 생성되지 않았을 때 -> 어댑터를 생성
        if(rcv_chat_Roomlist_adapter == null) {
            // 생성자 인수
            // 1. 액티비티
            // 2. 인플레이팅 되는 레이아웃
            // 3. arrayList rooms
            // 4. extra 변수
            rcv_chat_Roomlist_adapter = new RCV_chatRoom_list_adapter(getActivity(), R.layout.i_chat_room, rooms, "chat");
            recyclerView.setAdapter(rcv_chat_Roomlist_adapter);
            rcv_chat_Roomlist_adapter.notifyDataSetChanged();
        }
        // 어댑터가 생성되어 있을때는, 들어가는 arrayList만 교체
        else {
            rcv_chat_Roomlist_adapter.refresh_arr(rooms);
        }
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 서버 통신 -- 내 채팅방 리스트 받아와서 리턴하기
     ---------------------------------------------------------------------------*/
    @SuppressLint("StaticFieldLeak")
    public ArrayList<Chat_room> get_chat_room_list() {

        ArrayList<Chat_room> rooms = new ArrayList<>();
        final RetrofitService rs = ServiceGenerator.createService(RetrofitService.class);

        // 동기 호출
        try {
            final ArrayList<Chat_room> final_rooms = rooms;
            return new AsyncTask<Void, Void, ArrayList<Chat_room>>() {

                @Override
                protected ArrayList<Chat_room> doInBackground(Void... voids) {
                    try {
                        Call<ResponseBody> call = rs.get_chat_room_list(
                                Static.GET_CHAT_ROOM_LIST,
                                myapp.getUser_no());
                        Response<ResponseBody> call_result = call.execute();
                        String result = call_result.body().string();

                        try {
                            if(result.equals("fail")) {
                                myapp.logAndToast("예외발생: " + result);
                                final_rooms.clear();
                            }
                            else if(result.equals("no_result")) {
                                final_rooms.clear();
                            }
                            else {
                                // php 연결 테스트
//                                Log.d(TAG, "레트로핏_ 채팅방 리스트 결과: " + result);
                                // 길이가 긴 JSONString 출력하기
                                myapp.print_long_Json_logcat(result, TAG);
                                // jsonString --> jsonObject
                                JSONObject jsonObject = new JSONObject(result);
                                // jsonObject --> jsonArray
                                JSONArray jsonArray = jsonObject.getJSONArray(JSON_TAG_CHAT_ROOM_LIST);
                                Log.d(TAG, "jsonArray 개수: " + jsonArray.length());

                                // jsonArray에서 jsonObject를 하나씩 가지고 와서, parsing 하기
                                for(int i=0; i<jsonArray.length(); i++) {
                                    String jsonString = jsonArray.getJSONObject(i).toString();
//                                    Log.d(TAG, "jsonString_ " + i + ": " + jsonString);

                                    // Chat_room 객체안의 세부 ArrayList 객체 생성
                                    ArrayList<Users> user_arr = new ArrayList<>();
                                    ArrayList<String> user_nickname_arr = new ArrayList<>();
                                    ArrayList<String> user_img_filename_arr = new ArrayList<>();

                                    // 채팅방 번호
                                    int chatroom_no = jsonArray.getJSONObject(i).getInt("chatroom_no");
                                    // 채팅방 방장 번호
                                    int chat_room_authority_user_no = jsonArray.getJSONObject(i).getInt("chat_room_authority_user_no");
                                    // 해당 채팅방의 마지막 메세지 번호
                                    int last_msg_no = jsonArray.getJSONObject(i).getInt("last_msg_no");

                                    /**
                                     * 만약 마지막 메세지 번호가 '0' 이라면, 채팅 메세지가 하나도 오고간 적이 없는 것이므로, 무시한다
                                     *  ==> continue;
                                     * */
                                    if(last_msg_no == 0) {
                                        continue;
                                    }

                                    // 해당 채팅방의 메시지들 중에서 내가 안 읽은 메세지의 개수
                                    int unread_msg_count = jsonArray.getJSONObject(i).getInt("unread_msg_count");
                                    // 채팅방 제목
                                    String chat_room_title = jsonArray.getJSONObject(i).getString("chat_room_title");

//                                    Log.d(TAG, "chatroom_no: " + chatroom_no);
//                                    Log.d(TAG, "chat_room_authority_user_no: " + chat_room_authority_user_no);
//                                    Log.d(TAG, "last_msg_no: " + last_msg_no);
//                                    Log.d(TAG, "unread_msg_count: " + unread_msg_count);

                                    // 데이터 클래스로 파싱하기 위한 GSON 객체 생성
                                    Gson gson = new Gson();

                                    // 'user_ob' JSONString을 JSONObect로 파싱
                                    JSONArray jsonArray_for_user = new JSONArray(jsonArray.getJSONObject(i).getString("user_ob"));
//                                    Log.d(TAG, "jsonArray_for_user.toString(): " + jsonArray_for_user.toString());
//                                    Log.d(TAG, "jsonArray_for_user.length(): " + jsonArray_for_user.length());

                                    // gson 이용해서 user 객체로 변환해서, 그 user 객체 안에서 닉네임과, 이미지 URL 값을 가져와서,
                                    // 각 ArrayList 값에 add 한다
                                    for(int k=0; k<jsonArray_for_user.length(); k++) {
                                        Users user = gson.fromJson(jsonArray_for_user.get(k).toString(), Users.class);
//                                        Log.d(TAG, "user.getUser_nickname(): " + user.getUser_nickname());
//                                        Log.d(TAG, "user.getUser_img_filename(): " + user.getUser_img_filename());
                                        user_arr.add(user);
                                        user_nickname_arr.add(user.getUser_nickname());
                                        user_img_filename_arr.add(user.getUser_img_filename());
                                    }

                                    // 채팅방 마지막 메세지, JSONArray를 파싱
                                    JSONArray jsonArray_last_chat_log = new JSONArray(jsonArray.getJSONObject(i).getString("last_chat_log_ob"));
//                                    Log.d(TAG, "jsonArray_last_chat_log.toString(): " + jsonArray_last_chat_log.toString());
//                                    Log.d(TAG, "jsonArray_last_chat_log.length(): " + jsonArray_last_chat_log.length());

                                    // gson 이용해서 Chat_log 객체로 변환
                                    Chat_log last_chat_log = null;
                                    // 마지막 Chat_log가 있을 때만 변환
                                    if(jsonArray_last_chat_log.length() == 1) {
                                        last_chat_log = gson.fromJson(jsonArray_last_chat_log.get(0).toString(), Chat_log.class);
//                                        Log.d(TAG, "jsonArray_last_chat_log.get(0).toString(): " + jsonArray_last_chat_log.get(0).toString());
//                                        Log.d(TAG, "last_chat_log.getMsg_content(): " + last_chat_log.getMsg_content());
//                                        Log.d(TAG, "last_chat_log.getChat_room_no(): " + last_chat_log.getChat_room_no());
                                    }

                                    /** Chat_room 객체에 데이터 넣기 */
                                    Chat_room room = new Chat_room();
                                    room.setChatroom_no(chatroom_no);
                                    room.setLast_msg_no(last_msg_no);
                                    room.setUser_nickname_arr(user_nickname_arr);
                                    room.setUser_img_filename_arr(user_img_filename_arr);
                                    // 마지막 Chat_log가 있을때만 데이터 넣음
                                    if(jsonArray_last_chat_log.length() == 1) {
                                        room.setLast_log(last_chat_log);
                                    }
                                    room.setUnread_msg_count(unread_msg_count);
                                    room.setChat_room_title(chat_room_title);
                                    room.setUser_arr(user_arr);

                                    final_rooms.add(room);
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        return final_rooms;
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }.execute().get();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 전달받은 비트맵 4장을 정사각형의 하나의 배트맵으로 붙이기
     ---------------------------------------------------------------------------*/
    public void img_bitmap_put_complete(
            final int target_chatRoom_no, final ArrayList<Bitmap> this_arr, final String from) {
        Log.d(TAG, "test 메소드 들어옴");
        Log.d(TAG, "target_chatRoom_no: " + target_chatRoom_no);
        Log.d(TAG, "this_arr.size(): " + this_arr.size());
        Log.d(TAG, "from: " + from);

        new Thread() {
            @Override
            public void run() {
                int[] temp = random_pick(this_arr.size());

                BitmapFactory.Options option = new BitmapFactory.Options();
                option.inDither = true;
                option.inPurgeable = true;

                Bitmap bitmap = null;
//                Bitmap b1 = this_arr.get(temp[0]);
//                Bitmap b2 = this_arr.get(temp[1]);
//                Bitmap b3 = this_arr.get(temp[2]);
//                Bitmap b4 = this_arr.get(temp[3]);
                Bitmap b1 = this_arr.get(0);
                Bitmap b2 = this_arr.get(1);
                Bitmap b3 = this_arr.get(2);
                Bitmap b4 = this_arr.get(3);

                b1 = resizeBitmapImage(b1, 100);
                b2 = resizeBitmapImage(b2, 100);
                b3 = resizeBitmapImage(b3, 100);
                b4 = resizeBitmapImage(b4, 100);

                bitmap = Bitmap.createScaledBitmap(b1, b1.getWidth()+b1.getWidth(), b1.getHeight()+b1.getHeight(), true);

                Paint p = new Paint();
                p.setDither(true);
                p.setFlags(Paint.ANTI_ALIAS_FLAG);

                Canvas c = new Canvas(bitmap);
                c.drawBitmap(b1, 0, 0, p);
                c.drawBitmap(b2, 0, b1.getHeight(), p);
                c.drawBitmap(b3, b1.getWidth(), 0, p);
                c.drawBitmap(b4,b1.getWidth(),b1.getHeight(),p);

                b1.recycle();
                b2.recycle();
                b3.recycle();
                b4.recycle();


//                rcv_chat_Roomlist_adapter.set_group_chat_representatice_image(target_chatRoom_no, bitmap);
//                if(temp_bitmap_hash.containsKey(target_chatRoom_no)) {
//                    temp_bitmap_hash.remove(target_chatRoom_no);
//                }
//                return bitmap;

                // 핸들러로 전달할 Message 객체 생성
                Message msg = handler.obtainMessage();
                // Message 객체에 넣을 bundle 객체 생성
                Bundle bundle = new Bundle();
                // bundle 객체에 'target_chatRoom_no' 변수 담기
                bundle.putString("target_chatRoom_no", String.valueOf(target_chatRoom_no));
                // Message 객체에 bundle, 'bitmap' 담기
                msg.setData(bundle);
                msg.obj = bitmap;
                // 핸들러에서 Message 객체 구분을 위한 'what' 값 설정
                // 요청하는 위치에 따라 핸들러 메세지를 구분하여 보냄
                // whole - 서버로부터 전체 채팅방 리스트를 받아 왔을 때.
                // new - 새로운 채팅방 리스트 한개를 받아 왔을 때.
                if(from.equals("whole")) {
                    msg.what = 0;
                }
                else if(from.equals("new")) {
                    msg.what = 2;
                }

                // 핸들러로 Message 객체 전달
                handler.sendMessage(msg);
            }
        }.start();
    }


    /**---------------------------------------------------------------------------
     메소드 ==> bitmap을 원하는 resolution으로 사이즈 조절하기
     ---------------------------------------------------------------------------*/
    private Bitmap resizeBitmapImage(Bitmap source, int maxResolution) {
        int width = source.getWidth();
        int height = source.getHeight();
        int newWidth = width;
        int newHeight = height;
        float rate = 0.0f;

        if(width > height) {
            if(maxResolution < width) {
                rate = maxResolution / (float) width;
                newHeight = (int) (height * rate);
                newWidth = maxResolution;
            }
        }
        else {
            if(maxResolution < height) {
                rate = maxResolution / (float) height;
                newWidth = (int) (width * rate);
                newHeight = maxResolution;
            }
        }

        return Bitmap.createScaledBitmap(source, newWidth, newHeight, true);
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 0 ~ size 사이에 중복되지 않는 랜덤한 숫자 4개들의 배열을 return 하는 메소드
     이미지 URL를 랜덤으로 고르기 위함
     ---------------------------------------------------------------------------*/
    public int[] random_pick(int size) {
        // 배열 생성
        int picked_imgs[] = new int[4];

        for(int i=0; i<picked_imgs.length; i++) {
            // 랜덤 값 반환
            picked_imgs[i] = (int)(Math.random()*size);

            // 중복 값 제거1
            for(int j=0; j<i; j++) {
                if(picked_imgs[i]==picked_imgs[j]){
                    i--;
                    break;
                }
            }
        }
        for(int k=0; k<picked_imgs.length; k++){
//            Log.d(TAG, "picked_imgs["+ k +"]: " + picked_imgs[k]);
        }

        return picked_imgs;
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 서버로부터 새로 받아온 채팅방이 그룹 채팅방일 때, 해당 그룹채팅방의 대표 이미지를
                생성하기 위한 메소드
     ---------------------------------------------------------------------------*/
    public void new_chatRoom_group_img_combine(
            int chatroom_no, ArrayList<Users> user_arr) {

        // 임시 해쉬맵 초기화
        temp_bitmap_hash_new.clear();

        // 해당 chatroom_no를 복사하여, 임시로 target_chatRoom_no 담아놓기
        final int target_chatRoom_no = chatroom_no;

        // 이미지 URL을 담을 임시 ArrayList 생성
        ArrayList<String> temp_user_img_filename = new ArrayList<>();

        // 채팅방에 참여중인 사람들의 수
        final int user_nums = user_arr.size();
        Log.d(TAG, "user_nums: " + user_nums);

        for(int i = 0; i<user_nums; i++) {

            final int finalI = i;
            String img_fileName = "";

            if(!user_arr.get(i).getUser_img_filename().equals("none")) {
                img_fileName = user_arr.get(i).getUser_img_filename();
            }
            else if(user_arr.get(i).getUser_img_filename().equals("none")) {
                img_fileName = "default_profile_for_combine.png";
            }

            Glide
                .with(getContext())
                .load(Static.SERVER_URL_PROFILE_FILE_FOLDER + img_fileName)
                .asBitmap()
                .override(100, 100)
                .fitCenter()
                .into(new SimpleTarget<Bitmap>() {

                    int this_target_chatRoom_no = target_chatRoom_no;

                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {

                        // 이미 해당 채팅방을 키 값으로 하는 해쉬맵 밸류값이 있다면
                        if(temp_bitmap_hash_new.containsKey(this_target_chatRoom_no)) {
                            temp_bitmap_hash_new.get(this_target_chatRoom_no).add(resource);
                        }
                        // 이미 해당 채팅방을 키 값으로 하는 해쉬맵 밸류값이 없다면
                        else if(!temp_bitmap_hash_new.containsKey(this_target_chatRoom_no)) {

                            // bitmap들을 담을 새로운 ArrayList를 생성하고,
                            // 해당 채팅방을 키 값으로 하는 해쉬맵 arr item을 put 한 뒤
                            ArrayList<Bitmap> bitmap_arr = new ArrayList<>();
                            temp_bitmap_hash_new.put(this_target_chatRoom_no, bitmap_arr);
                            // 해당 해쉬맵 아이템 밸류인 arr에 해당 bitmap을 넣는다
                            temp_bitmap_hash_new.get(this_target_chatRoom_no).add(resource);
                        }

                        // for문 마지막일 때
                        if(finalI == user_nums-1) {
                            Log.d(TAG, "finalI: " + finalI);
                            Log.d(TAG, "temp_users_size: " + user_nums);
                            Log.d(TAG, "3명인지 확인하기 위한 로그_ user_nums : " + user_nums);

                            // 채팅 참여인원이 3명일 때 (추가확인 == 현재 비트맵 어레이개수가 3개인지)
                            if(user_nums == 3 && temp_bitmap_hash_new.get(this_target_chatRoom_no).size() == 3) {
                                // 마지막 어레이 아이템을 지우고, 프로필 사진이 없는 회색 배경 비트맵을 추가
                                temp_bitmap_hash_new.get(this_target_chatRoom_no).add(myapp.get_blank_gray_view());
                                Log.d(TAG, "3명일 때, 빈 회색 비트맵을 추가하고 난 뒤 비트맵 개수: "
                                        + temp_bitmap_hash_new.get(this_target_chatRoom_no).size());
                            }

                            // 해당 채팅방의 참여한 유저들의 비트맵들과, 채팅방 번호를 메소드로 넘김
                            // --> 이미지 4장 합쳐서 해당 아이템에 보여주기 위함
                            // 매개변수 1. 이 채팅방 번호
                            // 매개변수 2. 비트맵들을 담은 ArrayList
                            new Thread() {
                                @Override
                                public void run() {
                                    img_bitmap_put_complete(this_target_chatRoom_no, temp_bitmap_hash_new.get(this_target_chatRoom_no), "new");
                                }
                            }.start();

                        }
                    }
                });
        }
    }


    /**---------------------------------------------------------------------------
     생명주기 ==> onResume
     ---------------------------------------------------------------------------*/
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "Chat_F_생명주기 onResume()");
        Main_after_login_A.chat_F_onResume = true;
    }


    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "Chat_F_생명주기 onPause()");
        if(Main_after_login_A.chat_F_onResume) {
            Main_after_login_A.chat_F_onResume = false;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "Chat_F_생명주기 onStop()");
        if(Main_after_login_A.chat_F_onResume) {
            Main_after_login_A.chat_F_onResume = false;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "Chat_F_생명주기 onAttach");
    }

    @Override
    public void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Chat_F_생명주기 onCreate");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "Chat_F_생명주기 onActivityCreated");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "Chat_F_생명주기 onStart");
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "Chat_F_생명주기 onDestroyView()");
        // 버터나이프 바인드 해제
        if(unbinder != null) {
            unbinder.unbind();
        }
        // 어플리케이션 객체 null 처리
        myapp = null;
        // otto 해제
        BusProvider.getBus().unregister(this);

        // onDestroyView() 시에 어댑터를 null 처리 해주어야,
        // 다음 onCreateView 시에 제대로 뷰에 채팅방 리스트가 나온다
        rcv_chat_Roomlist_adapter = null;

        if(Main_after_login_A.chat_F_onResume) {
            Main_after_login_A.chat_F_onResume = false;
        }

        // 임시 해쉬맵 초기화
        temp_bitmap_hash.clear();
        temp_bitmap_hash_new.clear();

        // static handler 없애기
        handler = null;

        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Chat_F_생명주기 onDestroy()");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "Chat_F_생명주기 onDetach()");
    }
}
