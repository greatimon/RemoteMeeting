package com.example.jyn.remotemeeting.Fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

import butterknife.BindView;
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
    String JSON_TAG_CHAT_ROOM_LIST = "chat_room_list";
    LayoutInflater inflater;
    ViewGroup container;
    View controlView;
    Myapp myapp;

    /** 버터나이프*/
    public Unbinder unbinder;
    @BindView(R.id.recyclerView)        RecyclerView recyclerView;
    @BindView(R.id.no_result)           TextView no_result;

    // 리사이클러뷰 관련 클래스
    public RCV_chatRoom_list_adapter rcv_chat_Roomlist_adapter;
    public RecyclerView.LayoutManager layoutManager;

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
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "Chat_F_생명주기 onCreateView");
        this.inflater = inflater;
        this.container = container;

        // 프래그먼트 인플레이팅
        controlView = inflater.inflate(R.layout.f_chat, container, false);
        // 버터나이프 바인드
        unbinder = ButterKnife.bind(this, controlView);
        // 어플리케이션 객체 생성
        myapp = Myapp.getInstance();
        // otto 등록
        BusProvider.getBus().register(this);

        /** 리사이클러뷰 */
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        // 리사이클러뷰 구분선 - 가로(클래스 생성)
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
        // 애니메이션 설정 - 애니메이션 설정 끔
        ((SimpleItemAnimator)recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        // 애니메이션 설정 - 기본 애니메이션
//        recyclerView.setItemAnimator(new DefaultItemAnimator());

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
        if(event.getMessage().equals("activate_RCV")) {
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
        ArrayList<Chat_room> rooms = get_chat_room_list();
        Log.d(TAG, "usersArrayList.isEmpty(): " + rooms.isEmpty());

        // 채팅방 리스트가 하나도 없다면
        if(rooms.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            no_result.setVisibility(View.VISIBLE);
        }
        // 채팅방 리스트가 있다면
        else if(!rooms.isEmpty()) {
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
                                    Log.d(TAG, "jsonString_ " + i + ": " + jsonString);

                                    // Chat_room 객체안의 세부 ArrayList 객체들 생성
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

                                    Log.d(TAG, "chatroom_no: " + chatroom_no);
                                    Log.d(TAG, "chat_room_authority_user_no: " + chat_room_authority_user_no);
                                    Log.d(TAG, "last_msg_no: " + last_msg_no);
                                    Log.d(TAG, "unread_msg_count: " + unread_msg_count);

                                    // 데이터 클래스로 파싱하기 위한 GSON 객체 생성
                                    Gson gson = new Gson();

                                    // 'user_ob' JSONString을 JSONObect로 파싱
                                    JSONArray jsonArray_for_user = new JSONArray(jsonArray.getJSONObject(i).getString("user_ob"));
                                    Log.d(TAG, "jsonArray_for_user.toString(): " + jsonArray_for_user.toString());
                                    Log.d(TAG, "jsonArray_for_user.length(): " + jsonArray_for_user.length());

                                    // gson 이용해서 user 객체로 변환해서, 그 user 객체 안에서 닉네임과, 이미지 URL 값을 가져와서,
                                    // 각 ArrayList 값에 add 한다
                                    for(int k=0; k<jsonArray_for_user.length(); k++) {
                                        Users user = gson.fromJson(jsonArray_for_user.get(k).toString(), Users.class);
                                        Log.d(TAG, "user.getUser_nickname(): " + user.getUser_nickname());
                                        Log.d(TAG, "user.getUser_img_filename(): " + user.getUser_img_filename());
                                        user_nickname_arr.add(user.getUser_nickname());
                                        user_img_filename_arr.add(user.getUser_img_filename());
                                    }

                                    // 채팅방 마지막 메세지, JSONArray를 파싱
                                    JSONArray jsonArray_last_chat_log = new JSONArray(jsonArray.getJSONObject(i).getString("last_chat_log_ob"));
                                    Log.d(TAG, "jsonArray_last_chat_log.toString(): " + jsonArray_last_chat_log.toString());
                                    Log.d(TAG, "jsonArray_last_chat_log.length(): " + jsonArray_last_chat_log.length());

                                    // gson 이용해서 Chat_log 객체로 변환
                                    Chat_log last_chat_log = null;
                                    // 마지막 Chat_log가 있을 때만 변환
                                    if(jsonArray_last_chat_log.length() == 1) {
                                        last_chat_log = gson.fromJson(jsonArray_last_chat_log.get(0).toString(), Chat_log.class);
                                        Log.d(TAG, "jsonArray_last_chat_log.get(0).toString(): " + jsonArray_last_chat_log.get(0).toString());
                                        Log.d(TAG, "last_chat_log.getMsg_content(): " + last_chat_log.getMsg_content());
                                        Log.d(TAG, "last_chat_log.getChat_room_no(): " + last_chat_log.getChat_room_no());
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
