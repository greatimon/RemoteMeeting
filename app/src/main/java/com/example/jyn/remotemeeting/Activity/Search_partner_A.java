package com.example.jyn.remotemeeting.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.jyn.remotemeeting.Adapter.RCV_search_partner_adapter;
import com.example.jyn.remotemeeting.DataClass.Users;
import com.example.jyn.remotemeeting.R;
import com.example.jyn.remotemeeting.Util.Myapp;
import com.example.jyn.remotemeeting.Util.SimpleDividerItemDecoration;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by JYN on 2017-12-02.
 */

public class Search_partner_A extends Activity {

    private static final String TAG = "all_"+Search_partner_A.class.getSimpleName();
    Myapp myapp;
    String with_domain = "";
    String without_domain = "";
    public static int REQUEST_SUBTRACT_CONFIRM = 7246;

    /** 버터나이프 */
    public Unbinder unbinder;
    @BindView(R.id.back)                ImageView back;
    @BindView(R.id.ini_editText)        ImageView ini_editText;
    @BindView(R.id.search_keyword)      EditText search_keyword;
    @BindView(R.id.recyclerView)        RecyclerView recyclerView;
    @BindView(R.id.no_result)           RelativeLayout no_result;

    // 리사이클러뷰 관련 클래스
    public RCV_search_partner_adapter rcv_search_partner_adapter;
    public RecyclerView.LayoutManager layoutManager;


    /**---------------------------------------------------------------------------
     생명주기 ==> onCreate
     ---------------------------------------------------------------------------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_search_partner);
        // 버터나이프 바인드
        unbinder = ButterKnife.bind(this);
        // 어플리케이션 객체 생성
        myapp = Myapp.getInstance();

        /** 리사이클러뷰 */
        recyclerView.setHasFixedSize(true);
        // LinearLayoutManager 사용, 구분선 표시
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        // 리사이클러뷰 구분선 - 가로 (클래스 생성)
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(this, "Search_partner_A"));
        // 애니메이션 설정
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // search_keyword 텍스트 와쳐
        search_keyword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() > 0) {
                    ini_editText.setImageResource(R.drawable.cancel_activated);
                    ini_editText.setClickable(true);
                }
                if(s.length() == 0) {
                    ini_editText.setImageResource(R.drawable.cancel_none_activated);
                    ini_editText.setClickable(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // search_keyword_ET 소프트 키보드 앤터 속성 `검색`으로 바꾸기
        search_keyword.setImeOptions(EditorInfo.IME_ACTION_SEARCH);

        // search_keyword 소프트 키보드 액션 리스너 정의, 등록
        EditText.OnEditorActionListener Edittext_Listener = new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    // 소프트 키보드 검색 클릭했을 때
                    onSearchTextClicked(getCurrentFocus());

                    // 소프트 키보드 숨기기
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        };
        search_keyword.setOnEditorActionListener(Edittext_Listener);

        // 초기 뷰 셋팅
        no_result.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 검색 결과 가져오기
     ---------------------------------------------------------------------------*/
    public void onSearchTextClicked(View view) {

        String search_word_str = search_keyword.getText().toString();
        search_word_str = search_word_str.replace(" ", "");

        // 검색 미진행_ 입력값 없음
        if(search_word_str.equals("")) {
            myapp.logAndToast("검색 단어를 입력해주세요");
            if(no_result.getVisibility() == View.VISIBLE) {
                no_result.setVisibility(View.GONE);
            }
            return;
        }

        // 검색어 길이 부족
        if(!search_word_str.equals("") && search_word_str.length()<=1) {
            myapp.logAndToast("검색어는 2자이상 입력해주세요");
            if(no_result.getVisibility() == View.VISIBLE) {
                no_result.setVisibility(View.GONE);
            }
            return;
        }

        // 검색어에 '@'가 있을 때
        if(search_word_str.contains("@")) {
            with_domain = search_word_str;
        }
        else if(!search_word_str.contains("@")) {
            without_domain = search_word_str;
        }

        // 검색 진행
        // 리사이클러뷰 동작 메소드 호출
        activate_RCV(with_domain, without_domain);

    }


    /**---------------------------------------------------------------------------
     메소드 ==> 서버로부터 검색결과에 따른 리스트 받아와서, 어댑터로 넘기기
     ---------------------------------------------------------------------------*/
    public void activate_RCV(String with_domain, String without_domain) {
        // 서버로 부터 검색 결과 받기
        ArrayList<Users> usersArrayList = myapp.search_partners(with_domain, without_domain);
        Log.d(TAG, "usersArrayList 개수: " + usersArrayList.size());
        Log.d(TAG, "usersArrayList.isEmpty(): " + usersArrayList.isEmpty());

        // 검색 결과가 하나도 없다면
        if(usersArrayList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            no_result.setVisibility(View.VISIBLE);
        }
        // 검색 결과가 잇다면
        else if(!usersArrayList.isEmpty()) {
            no_result.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            // 어댑터가 생성되지 않았을 때 -> 어댑터를 생성
            if(rcv_search_partner_adapter == null) {
                // 생성자 인수
                // 1. 액티비티
                // 2. 인플레이팅 되는 레이아웃
                // 3. arrayList users
                // 4. extra 변수
                rcv_search_partner_adapter = new RCV_search_partner_adapter(this, R.layout.i_search_partner, usersArrayList, "search");
                recyclerView.setAdapter(rcv_search_partner_adapter);
                rcv_search_partner_adapter.notifyDataSetChanged();
            }
            // 어댑터가 생성되어 있을때는, 들어가는 arrayList만 교체
            else {
                rcv_search_partner_adapter.refresh_arr(usersArrayList);
            }
        }
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 뒤로가기(액티비티 이동) == 소프트 키보드 백버튼 매소드 연결
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.back)
    public void back() {
        onBackPressed();
        setResult(RESULT_OK);
        finish();
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 검색 EditText 초기화, 이미지 리소스 변경 및 clickable 상태 변경
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.ini_editText)
    public void ini_editText() {
        search_keyword.setText("");
        ini_editText.setImageResource(R.drawable.cancel_none_activated);
        ini_editText.setClickable(false);
        no_result.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
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


    /**---------------------------------------------------------------------------
     오버라이드 ==> onActivityResult
     ---------------------------------------------------------------------------*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 파트너 끊기로 했다면
        if(requestCode==REQUEST_SUBTRACT_CONFIRM && resultCode==RESULT_OK) {
            int position = data.getIntExtra("position", -1);
            String target_user_no = data.getStringExtra("target_user_no");
            if(position != -1 && !target_user_no.equals("")) {
                rcv_search_partner_adapter.break_partner_reflect(position, target_user_no);
            }
        }
    }
}
