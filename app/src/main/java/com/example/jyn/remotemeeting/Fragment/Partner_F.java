package com.example.jyn.remotemeeting.Fragment;

import android.content.Context;
import android.content.Intent;
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
import android.widget.Button;
import android.widget.RelativeLayout;

import com.example.jyn.remotemeeting.Activity.Main_after_login_A;
import com.example.jyn.remotemeeting.Activity.Search_partner_A;
import com.example.jyn.remotemeeting.Adapter.RCV_partner_adapter;
import com.example.jyn.remotemeeting.DataClass.Users;
import com.example.jyn.remotemeeting.Etc.Static;
import com.example.jyn.remotemeeting.Otto.BusProvider;
import com.example.jyn.remotemeeting.Otto.Event;
import com.example.jyn.remotemeeting.R;
import com.example.jyn.remotemeeting.Util.Myapp;
import com.example.jyn.remotemeeting.Util.SimpleDividerItemDecoration;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by JYN on 2017-11-10.
 */

public class Partner_F extends Fragment {

    private static final String TAG = "all_"+Partner_F.class.getSimpleName();
    LayoutInflater inflater;
    ViewGroup container;
    Myapp myapp;

    /** 버터나이프*/
    public Unbinder unbinder;
    @BindView(R.id.recyclerView)        RecyclerView recyclerView;
    @BindView(R.id.no_result)           RelativeLayout no_result;
    @BindView(R.id.search_partner)      Button search_partner;

    // 리사이클러뷰 관련 클래스
    public RCV_partner_adapter rcv_partner_adapter;
    public RecyclerView.LayoutManager layoutManager;

    //SwipeRefreshLayout -  당겨서 새로고침
    private SwipeRefreshLayout layout;

    public Partner_F() {
        // Required empty public constructor
    }

    public static Partner_F newInstance() {
        Bundle args = new Bundle();

        Partner_F fragment = new Partner_F();
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
        layout = (SwipeRefreshLayout)inflater.inflate(R.layout.f_partner, container, false);
        // 버터나이프 바인드
        unbinder = ButterKnife.bind(this, layout);
        // 어플리케이션 객체 생성
        myapp = Myapp.getInstance();
        // otto 등록
        BusProvider.getBus().register(this);

        // 뷰찾기
        search_partner = layout.findViewById(R.id.search_partner);
        no_result = layout.findViewById(R.id.no_result);

        // 새로고침 아이콘 색깔 변경
        layout.setColorSchemeColors(Color.parseColor("#4CAF50"), Color.parseColor("#43A047"));
        //리스너 정의
        layout.setOnRefreshListener(sRefresh);
        layout.setEnabled(true);

        /** 리사이클러뷰 */
        recyclerView.setHasFixedSize(true);
        // LinearLayoutManager 사용, 구분선 표시
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        // 리사이클러뷰 구분선 - 가로(클래스 생성)
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity(), "Partner_F"));
        // 애니메이션 설정
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // 리사이클러뷰 동작 메소드 호출
        activate_RCV();

        return layout;
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 서버로부터 내 파트너 리스트 받아와서, 어댑터로 넘기기
     ---------------------------------------------------------------------------*/
    public void activate_RCV() {
        if(myapp == null) {
            // 어플리케이션 객체 생성
            myapp = Myapp.getInstance();
        }
        // 서버로 부터 파트너 리스트 받기
        ArrayList<Users> usersArrayList = myapp.get_partner_list();
        Log.d(TAG, "usersArrayList.isEmpty(): " + usersArrayList.isEmpty());

        // 파트너 리스트 결과가 하나도 없다면
        if(usersArrayList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            no_result.setVisibility(View.VISIBLE);
        }
        // 파트너 리스트 결과가 있다면
        else if(!usersArrayList.isEmpty()) {
            no_result.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            // 어댑터가 생성되지 않았을 때 -> 어댑터를 생성
            if(rcv_partner_adapter == null) {
                // 생성자 인수
                // 1. 액티비티
                // 2. 인플레이팅 되는 레이아웃
                // 3. arrayList users
                // 4. extra 변수
                rcv_partner_adapter = new RCV_partner_adapter(
                        getActivity(), R.layout.i_mypartner, usersArrayList, "partner");
                recyclerView.setAdapter(rcv_partner_adapter);
                rcv_partner_adapter.notifyDataSetChanged();
            }
            // 어댑터가 생성되어 있을때는, 들어가는 arrayList만 교체
            else {
                rcv_partner_adapter.refresh_arr(usersArrayList);
            }
        }
    }


    /**---------------------------------------------------------------------------
     otto ==> Main_after_login_A로 부터 message 수신
     ---------------------------------------------------------------------------*/
    @Subscribe
    public void getMessage(Event.Main_after_login_A__Partner_F event) {
        Log.d(TAG, "otto 받음_ " + event.getMessage());
        if(event.getMessage().equals("activate_File_RCV")) {
            // 리사이클러뷰 동작 메소드 호출
            activate_RCV();
        }
    }


    /**---------------------------------------------------------------------------
     생명주기 ==> onResume
     ---------------------------------------------------------------------------*/
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 파트너 검색 액티비티 이동
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.search_partner)
    public void search_partner(View view) {
        // TODO: redis - 클릭이벤트
        myapp.Redis_log_click_event(getClass().getSimpleName(), view);

        // TODO: redis - 화면 이동
        myapp.Redis_log_view_crossOver_from_to(
                getClass().getSimpleName(), Search_partner_A.class.getSimpleName());

        Intent intent = new Intent(getActivity(), Search_partner_A.class);
        intent.putExtra(Static.REQUEST_CLASS, getClass().getSimpleName());
        startActivityForResult(intent, Main_after_login_A.REQUEST_SEARCH_PARTNER);
    }


    /**---------------------------------------------------------------------------
     메소드 ==> pull to refresh 리스너 -- 서버로부터 파트너 리스트 받아서 뷰에 뿌리기
     ---------------------------------------------------------------------------*/
    private SwipeRefreshLayout.OnRefreshListener sRefresh = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            Log.d(TAG, "onRefresh()");

            // 리사이클러뷰 동작 메소드 호출
            activate_RCV();

            // 새로고침 아이콘 없애기
            layout.setRefreshing(false);
        }
    };


    /**---------------------------------------------------------------------------
     콜백메소드 ==> 뷰페이저 focus 에 따른 콜백메소드
     ---------------------------------------------------------------------------*/
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {
            //화면에 실제로 보일때
            Log.d("프래그먼트", "Partner_F 화면에 실제로 보일 때");

            if(myapp == null) {
                // 어플리케이션 객체 생성
                myapp = Myapp.getInstance();
            }

            // TODO: redis - 이전 화면 이동내용을, 이동완료된 클래스에서 처리
            // 'Main_after_login_A'에서, 프래그먼트끼리의 이동 정보를 Redis에 전송하기 위함
            myapp.Redis_log_view_crossOver_from_to(
                    myapp.getCurr_frag_at_main(), getClass().getSimpleName());

            // TODO: 현재 프래그먼트 클래스의 simpleName을 어플리케이션 객체에 저장
            myapp.setCurr_frag_at_main(getClass().getSimpleName());
        }
        else {
            //preload 될때(전페이지에 있을때)
            Log.d("프래그먼트", "Partner_F 될때(전페이지에 있을때)");
        }
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

        // onDestroyView() 시에 어댑터를 null 처리 해주어야,
        // 다음 onCreateView 시에 제대로 뷰에 채팅방 리스트가 나온다
        rcv_partner_adapter = null;

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
