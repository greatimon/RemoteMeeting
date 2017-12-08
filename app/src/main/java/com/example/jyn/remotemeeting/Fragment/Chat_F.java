package com.example.jyn.remotemeeting.Fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.jyn.remotemeeting.Adapter.RCV_chat_adapter;
import com.example.jyn.remotemeeting.DataClass.Chat_room;
import com.example.jyn.remotemeeting.Etc.Static;
import com.example.jyn.remotemeeting.Otto.BusProvider;
import com.example.jyn.remotemeeting.R;
import com.example.jyn.remotemeeting.Util.Myapp;
import com.example.jyn.remotemeeting.Util.RetrofitService;
import com.example.jyn.remotemeeting.Util.ServiceGenerator;
import com.example.jyn.remotemeeting.Util.SimpleDividerItemDecoration;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import okhttp3.ResponseBody;
import retrofit2.Call;
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
    public RCV_chat_adapter rcv_chat_adapter;
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
        Log.d(TAG, "onCreateView");
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
        // 애니메이션 설정
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // 리사이클러뷰 동작 메소드 호출
        activate_RCV();

        return controlView;
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

            // 어댑터가 생성되지 않았을 때 -> 어댑터를 생성
            if(rcv_chat_adapter == null) {
                // 생성자 인수
                // 1. 액티비티
                // 2. 인플레이팅 되는 레이아웃
                // 3. arrayList rooms
                // 4. extra 변수
                rcv_chat_adapter = new RCV_chat_adapter(getActivity(), R.layout.i_chat_room, rooms, "chat");
                recyclerView.setAdapter(rcv_chat_adapter);
                rcv_chat_adapter.notifyDataSetChanged();
            }
            // 어댑터가 생성되어 있을때는, 들어가는 arrayList만 교체
            else {
                rcv_chat_adapter.refresh_arr(rooms);
            }
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

//                        try {
                            if(result.equals("fail")) {
                                myapp.logAndToast("예외발생: " + result);
                                final_rooms.clear();
                            }
                            else if(result.equals("no_result")) {
                                final_rooms.clear();
                            }
                            else {
                                // php 연결 테스트
                                Log.d(TAG, "레트로핏_ 채팅방 리스트 결과: " + result);
                                // 길이가 긴 JSONString 출력하기
//                                myapp.print_long_Json_logcat(result, TAG);
//                                // jsonString --> jsonObject
//                                JSONObject jsonObject = new JSONObject(result);
//                                // jsonObject --> jsonArray
//                                JSONArray jsonArray = jsonObject.getJSONArray(JSON_TAG_CHAT_ROOM_LIST);
//                                Log.d(TAG, "jsonArray 개수: " + jsonArray.length());
//
//                                // jsonArray에서 jsonObject를 하나씩 가지고 와서,
//                                // gson과 user 데이터클래스를 이용하여 user_arr에 add 하기
//                                for(int i=0; i<jsonArray.length(); i++) {
//                                    String jsonString = jsonArray.getJSONObject(i).toString();
//                                    Gson gson = new Gson();
//                                    Chat_room room = gson.fromJson(jsonString, Chat_room.class);
//                                    final_rooms.add(room);
//                                }
                            }

//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
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
        Log.d(TAG, "onResume()");
    }


    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop()");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach");
    }

    @Override
    public void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView()");
        // 버터나이프 바인드 해제
        if(unbinder != null) {
            unbinder.unbind();
        }
        // 어플리케이션 객체 null 처리
        myapp = null;
        // otto 해제
        BusProvider.getBus().unregister(this);
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach()");
    }
}
