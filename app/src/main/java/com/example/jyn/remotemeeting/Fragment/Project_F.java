package com.example.jyn.remotemeeting.Fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.example.jyn.remotemeeting.Adapter.RCV_project_adapter;
import com.example.jyn.remotemeeting.DataClass.Project;
import com.example.jyn.remotemeeting.Otto.BusProvider;
import com.example.jyn.remotemeeting.R;
import com.example.jyn.remotemeeting.Util.Myapp;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by JYN on 2017-11-10.
 *
 * 프로젝트별 영상회의 결과를 보여주는 프래그먼트, 메인화면 뷰페이저 중 하나
 */

public class Project_F extends Fragment {

    private static final String TAG = "all_"+Project_F.class.getSimpleName();
    LayoutInflater inflater;
    ViewGroup container;
    // 어플리케이션 객체
    Myapp myapp;
    /** 버터나이프*/
    public Unbinder unbinder;

    // 리사이클러뷰 관련 변수, 클래스 선언 =========
    // =========================================
    public static RecyclerView recyclerView;
    public static RelativeLayout no_result;
    public RCV_project_adapter rcv_project_adapter;
    public RecyclerView.LayoutManager layoutManager;

    // 서버로부터 프로젝트 정보 및 종료된 영상회의 정보를 받아서 담아놓을 해쉬맵
    public ArrayList<Project> project_arr;

    // 프로젝트가 지정되어 있지 않은, 영상회의 개수
    int Unspecified_meeting_count = 0;

    //SwipeRefreshLayout -  당겨서 새로고침
    private SwipeRefreshLayout layout;

    public Project_F() {
        // Required empty public constructor
    }

    public static Project_F newInstance() {
        Bundle args = new Bundle();

        Project_F fragment = new Project_F();
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
        layout = (SwipeRefreshLayout) inflater.inflate(R.layout.f_project, container, false);
        // 버터나이프 바인드
        unbinder = ButterKnife.bind(this, layout);
        // 어플리케이션 객체 생성
        myapp = Myapp.getInstance();
        // otto 등록
        BusProvider.getBus().register(this);

        // 뷰찾기 - static 으로 놔야 해서 버터나이프로 안됨
        no_result = layout.findViewById(R.id.no_result);
        recyclerView = layout.findViewById(R.id.recyclerView);

        // 새로고침 아이콘 색깔 변경
        layout.setColorSchemeColors(Color.parseColor("#4CAF50"), Color.parseColor("#43A047"));
        //리스너 정의
        layout.setOnRefreshListener(sRefresh);
        layout.setEnabled(true);

        /** 리사이클러뷰 */
        recyclerView.setHasFixedSize(true);
        // 리사이클러뷰 - GridLayoutManager 사용
        layoutManager = new GridLayoutManager(getActivity().getApplicationContext(), 2);
        recyclerView.setLayoutManager(layoutManager);
        // 애니메이션 설정
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // 초기 View Visibility
        no_result.setVisibility(View.GONE);

        // 서버로부터 받은 프로젝트 리스트를 담을 arrayList
        project_arr = new ArrayList<>();

        // 리사이클러뷰 동작 메소드 호출
        activate_RCV();

        return layout;
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 서버로부터 프로젝트 리스트 받아와서, 어댑터로 넘기기
     ---------------------------------------------------------------------------*/
    public void activate_RCV() {
        if(myapp == null) {
            // 어플리케이션 객체 생성
            myapp = Myapp.getInstance();
        }

        // 서버로부터 프로젝트 리스트 받기
        project_arr = myapp.get_project_list();
        Log.d(TAG, "project_arr.size(): " + project_arr.size());

        // 서버로부터 받은 결과가 하나도 없다면
        if(project_arr.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            no_result.setVisibility(View.VISIBLE);
        }
        else if(!project_arr.isEmpty()) {
            no_result.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            // 어댑터가 생성되지 않았을 때 -> 어댑터를 생성
            if(rcv_project_adapter == null) {
                // 생성자 인수
                // 1. 액티비티
                // 2. 인플레이팅 되는 레이아웃
                // 3. concurrentHashMap project_hash
                // 4. extra 변수
                rcv_project_adapter = new RCV_project_adapter(
                        getActivity(), R.layout.i_project_folder, project_arr, "project");
                recyclerView.setAdapter(rcv_project_adapter);
                rcv_project_adapter.notifyDataSetChanged();
            }
            // 어댑터가 생성되어 있을때는, 들어가는 해쉬맵만 교체
            else {
                rcv_project_adapter.refresh_arr(project_arr);
            }
        }
    }


    /**---------------------------------------------------------------------------
     생명주기 ==> onResume --
     ---------------------------------------------------------------------------*/
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
    }


    /**---------------------------------------------------------------------------
     메소드 ==> pull to refresh 리스너 --
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
        rcv_project_adapter = null;

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
