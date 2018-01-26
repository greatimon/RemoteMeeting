package com.example.jyn.remotemeeting.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

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
 * Created by JYN on 2017-12-01.
 */

public class Email_join_A extends Activity {

    /** 버터나이프 */
    public Unbinder unbinder;
    @BindView(R.id.back_img)        ImageView back_img;
    @BindView(R.id.back)            ImageView back;
    @BindView(R.id.input_email)     EditText input_email;
    @BindView(R.id.input_pw)        EditText input_pw;
    @BindView(R.id.input_pw2)       EditText input_pw2;
    @BindView(R.id.input_nickName)  EditText input_nickName;
    @BindView(R.id.join_btn)        TextView join_btn;

    private static final String TAG = "all_"+Email_join_A.class.getSimpleName();
    Myapp myapp;

    /** 이 클래스를 호출한 클래스 SimpleName */
    String request_class;


    /**---------------------------------------------------------------------------
     생명주기 ==> onCreate
     ---------------------------------------------------------------------------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_email_join);
        // 버터나이프 바인드
        unbinder = ButterKnife.bind(this);
        // 어플리케이션 객체 생성
        myapp = Myapp.getInstance();

        // 이 클래스를 호출한 클래스 인텐트 값으로 받기
        Intent get_intent = getIntent();
        request_class = get_intent.getStringExtra(Static.REQUEST_CLASS);

        // editText 밑줄 색 커스텀
        int color = Color.parseColor("#fbfbfb");
        input_email.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        input_pw.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        input_pw2.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        input_nickName.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_IN);

        // back_img 넣기
        int random = new Random().nextInt(7);
        back_img.setImageResource(myapp.back_img[random]);

        TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 뒤로가기
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.back)
    public void back(View view) {
        // TODO: redis - 클릭이벤트
        myapp.Redis_log_click_event(getClass().getSimpleName(), view);
        onBackPressed();
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 회원가입
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.join_btn)
    public void join_btn(View view) {
        // TODO: redis - 클릭이벤트
        myapp.Redis_log_click_event(getClass().getSimpleName(), view);

        String email_str = input_email.getText().toString();
        String pw_str = input_pw.getText().toString();
        String pw2_str = input_pw2.getText().toString();
        String nickName_str = input_nickName.getText().toString();

        // 각 폼 입력확인
        if(email_str.length()==0 || pw_str.length()==0 || pw2_str.length()==0 || nickName_str.length()==0) {
            myapp.logAndToast("양식이 모두 작성되지 않았습니다");
            return;
        }

        // 이메일 정규식 확인
        if(!myapp.email_check(email_str)) {
            myapp.logAndToast("이메일이 형식에 맞지않습니다");
            return;
        }

        // 비밀번호 정규식 확인
        if(!myapp.pw_check(pw_str)) {
            myapp.logAndToast("비밀번호가 형식에 맞지 않습니다");
            return;
        }

        // 비밀번호 재입력 일치 확인
        if(!pw_str.equals(pw2_str)) {
            myapp.logAndToast("비밀번호 재입력값이 일치하지 않습니다");
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
        user.setJoin_path("email");
        user.setUser_email(email_str);
        user.setUser_pw(pw2_str);
        user.setUser_nickname(nickName_str);
        user.setAndroid_id(android_id);

        /** user object convert to JsonString using with Gson */
        Gson gson = new Gson();
        String join_info_json = gson.toJson(user);
        Log.d(TAG, "join_info_json: " + join_info_json);

        /** 서버 통신 - 회원가입 */
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

                        finish();
                    }
                    else if(temp[0].equals("overlap")) {
                        myapp.logAndToast("이미 사용중인 이메일입니다");
                        input_email.setText("");
                        input_email.requestFocus();
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
        // TODO: redis - 화면 이동
        if(request_class != null) {
            myapp.Redis_log_view_crossOver_from_to(
                    getClass().getSimpleName(), request_class);
        }

        // 버터나이프 바인드 해제
        if(unbinder != null) {
            unbinder.unbind();
        }
        // 어플리케이션 객체 null 처리
        myapp = null;
        super.onDestroy();
    }
}
