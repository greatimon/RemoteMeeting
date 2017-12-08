package com.example.jyn.remotemeeting.Fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.jyn.remotemeeting.Adapter.RCV_chat_adapter;
import com.example.jyn.remotemeeting.Adapter.RCV_partner_adapter;
import com.example.jyn.remotemeeting.DataClass.Chat_room;
import com.example.jyn.remotemeeting.DataClass.Users;
import com.example.jyn.remotemeeting.Otto.BusProvider;
import com.example.jyn.remotemeeting.R;
import com.example.jyn.remotemeeting.Util.Myapp;
import com.example.jyn.remotemeeting.Util.SimpleDividerItemDecoration;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by JYN on 2017-11-10.
 */

public class Chat_F extends Fragment {

    private static final String TAG = "all_"+Chat_F.class.getSimpleName();
    LayoutInflater inflater;
    ViewGroup container;
    View controlView;
    Myapp myapp;

    /** 버터나이프*/
    public Unbinder unbinder;
    @BindView(R.id.recyclerView)        RecyclerView recyclerView;
    @BindView(R.id.no_result)           TextView no_result;

    // 리사이클러뷰 관련 클래스
    public RCV_chat_adapter rcv_partner_adapter;
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
        Log.d(TAG, "usersArrayList 개수: " + rooms.size());
        Log.d(TAG, "usersArrayList.isEmpty(): " + rooms.isEmpty());

        // 채팅방 리스트가 하나도 없다면
        if(rooms.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            no_result.setVisibility(View.VISIBLE);
        }
        else if(!rooms.isEmpty()) {

        }

    }


    /**---------------------------------------------------------------------------
     메소드 ==> 서버 통신 -- 내 파트너 리스트 가져와서 리턴하기
     ---------------------------------------------------------------------------*/
    @SuppressLint("StaticFieldLeak")
    public ArrayList<Chat_room> get_chat_room_list() {
        return null;
    }

    /**---------------------------------------------------------------------------
     생명주기 ==> onResume -- 
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
