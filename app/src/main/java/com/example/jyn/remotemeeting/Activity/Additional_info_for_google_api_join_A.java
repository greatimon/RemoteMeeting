package com.example.jyn.remotemeeting.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.jyn.remotemeeting.DataClass.Users;
import com.example.jyn.remotemeeting.Etc.Static;
import com.example.jyn.remotemeeting.R;
import com.example.jyn.remotemeeting.Util.Myapp;
import com.example.jyn.remotemeeting.Util.RetrofitService;
import com.example.jyn.remotemeeting.Util.ServiceGenerator;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by JYN on 2018-01-17.
 *
 * 구글 API로 회원가입 시도 할 때, 추가적으로 닉네임을 받는 클래스
 */

public class Additional_info_for_google_api_join_A extends Activity {

    /** 버터나이프 */
    public Unbinder unbinder;
    @BindView(R.id.back_img)        ImageView back_img;
    @BindView(R.id.input_nickName)  EditText input_nickName;

    Myapp myapp;
    private static final String TAG =
            "all_"+Additional_info_for_google_api_join_A.class.getSimpleName();

    String email;
    String firebase_UID;

    /** 이 클래스를 호출한 클래스 SimpleName */
    String request_class;


    /**---------------------------------------------------------------------------
     생명주기 ==> onCreate
     ---------------------------------------------------------------------------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_additional_info_for_google_api_join);
        // 버터나이프 바인드
        unbinder = ButterKnife.bind(this);
        // 어플리케이션 객체 생성
        myapp = Myapp.getInstance();

        // 이 클래스를 호출한 클래스 인텐트 값으로 받기
        Intent get_intent = getIntent();
        request_class = get_intent.getStringExtra(Static.REQUEST_CLASS);

        // editText 밑줄 색 커스텀
        int color = Color.parseColor("#fbfbfb");
        input_nickName.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_IN);

        // back_img 넣기
        int random = new Random().nextInt(7);
        back_img.setImageResource(myapp.back_img[random]);

        // intent 값 받기: 이메일, 파이어베이스 UDI 값
        Intent intent = getIntent();
        email = intent.getStringExtra("email");
        firebase_UID = intent.getStringExtra("firebase_UID");
        Log.d(TAG, "email: " + email);
        Log.d(TAG, "firebase_UID: " + firebase_UID);
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 뒤로가기
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.back)
    public void back() {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }

    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 회원가입
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.join_btn)
    public void join_btn() {

        String nickName_str = input_nickName.getText().toString();

        // 폼 입력확인
        if(nickName_str.length()==0) {
            myapp.logAndToast("양식이 모두 작성되지 않았습니다");
            return;
        }

        // 닉네임 형식 확인
        if(!myapp.nickName_check(nickName_str)) {
            myapp.logAndToast("닉네임이 형식에 맞지 않습니다");
            return;
        }

        // 유니크한 단말 번호 >>> Android ID 사용
        @SuppressLint("HardwareIds")
        String android_id = Settings.Secure.getString(this.getContentResolver(),Settings.Secure.ANDROID_ID);
        Log.d(TAG, "Android_ID >>> "+android_id);

        /** User 객체 생성하여 필요한 변수만 채워넣기 */
        Users user = new Users();
        user.setJoin_path("google");
        user.setUser_email(email);
        user.setUser_pw(firebase_UID);
        user.setUser_nickname(nickName_str);
        user.setAndroid_id(android_id);

        /** user object convert to JsonString using with Gson */
        Gson gson = new Gson();
        String join_info_json = gson.toJson(user);
        Log.d(TAG, "join_info_json: " + join_info_json);

        /** 서버 통신 - 회원가입 (이메일 중복체크 없이 바로 회원가입, 중복여부는 이전에 이미 확인했음) */
        RetrofitService rs = ServiceGenerator.createService(RetrofitService.class);
        Call<ResponseBody> call_result = rs.email_join(
                Static.EMAIL_JOIN,
                join_info_json);
        call_result.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String retrofit_result = response.body().string();
                    Log.d(TAG, "retrofit_result: "+retrofit_result);

                    String[] temp = retrofit_result.split("&");

                    if(temp[0].equals("success")) {
                        myapp.logAndToast("가입이 완료되었습니다");

                        Intent intent = new Intent();
                        intent.putExtra("email", email);
                        intent.putExtra("firebase_UID", firebase_UID);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                    else {
                        myapp.logAndToast("예외발생: " + retrofit_result);
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
        // result_canceled 값 넘기기
        setResult(RESULT_CANCELED);
        super.onDestroy();
    }
}
