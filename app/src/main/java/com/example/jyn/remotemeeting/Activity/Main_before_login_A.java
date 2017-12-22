package com.example.jyn.remotemeeting.Activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.jyn.remotemeeting.DataClass.Users;
import com.example.jyn.remotemeeting.Etc.Static;
import com.example.jyn.remotemeeting.R;
import com.example.jyn.remotemeeting.Util.Get_currentTime;
import com.example.jyn.remotemeeting.Util.Myapp;
import com.example.jyn.remotemeeting.Util.RetrofitService;
import com.example.jyn.remotemeeting.Util.ServiceGenerator;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.Random;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Main_before_login_A extends AppCompatActivity {

    private static final String TAG = "all_"+Main_before_login_A.class.getSimpleName();
    Myapp myapp;

    ImageView back_IV;
    EditText input_email_ET, input_pw_ET;
    TextView email_join;
    private int[] back_img = {
            R.drawable.back_1,
            R.drawable.back_2,
            R.drawable.back_3,
            R.drawable.back_4,
            R.drawable.back_5,
            R.drawable.back_6,
            R.drawable.back_7,
    };

    /** 버터나이프*/
    public Unbinder unbinder;


    /**---------------------------------------------------------------------------
     생명주기 ==> onCreate
     ---------------------------------------------------------------------------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "===================");
        Log.d(TAG, "onCreate");
        setContentView(R.layout.a_main_before_login);

        // 버터나이프 바인드
        unbinder = ButterKnife.bind(this);
        // 어플리케이션 객체 생성
        myapp = Myapp.getInstance();

        back_IV = findViewById(R.id.background_img);
        input_email_ET = findViewById(R.id.input_email);
        input_pw_ET = findViewById(R.id.input_pw);
        email_join= findViewById(R.id.email_join);

        // todo: 나중에 삭제하기 - 테스트할 때 로그인 편하기 하기 위한 코드
        // 유니크한 단말 번호 >>> Android ID 가져오기
        @SuppressLint("HardwareIds")
        String android_id = Settings.Secure.getString(this.getContentResolver(),Settings.Secure.ANDROID_ID);
        Log.d(TAG, "android_id: " + android_id);
        // 헐크
        if(android_id.equals("40706c04ca1a5ed")) {
            input_email_ET.setText("hulk@naver.com");
            input_pw_ET.setText("asdf1234!");
        }
        // 스파이더맨
        else if(android_id.equals("6074042952871828")) {
            input_email_ET.setText("spiderman@naver.com");
            input_pw_ET.setText("asdf1234!");
        }
        // 아이언맨
        else if(android_id.equals("5b1d8c7360b43504")) {
            input_email_ET.setText("ironman@naver.com");
            input_pw_ET.setText("asdf1234!");
        }
        // 비스트
        else if(android_id.equals("3087048799591849")) {
            input_email_ET.setText("beast@naver.com");
            input_pw_ET.setText("asdf1234!");
        }
        // todo: 나중에 삭제하기 - 테스트할 때 로그인 편하기 하기 위한 코드

        // 핸드폰 DPI 알아내기
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager mgr = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        mgr.getDefaultDisplay().getMetrics(metrics);

        Log.d(TAG, "densityDPI = " + metrics.densityDpi);

        Log.d(TAG, "현재 시간: " + Get_currentTime.get_full());
        Log.d(TAG, "오늘 날짜(Calendar 이용): "
                + (Calendar.getInstance().get(Calendar.MONTH) + 1) + "-"
                + Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
    }


    /**---------------------------------------------------------------------------
     생명주기 ==> onResume
     ---------------------------------------------------------------------------*/
    @Override
    protected void onResume() {
        super.onResume();
        int random = new Random().nextInt(7);

        Glide
            .with(this)
            .load(back_img[random])
            .into(back_IV);
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

    @OnClick(R.id.login)
    public void login_email(View view) {
//        onShared_Initializing();
        String email_str = input_email_ET.getText().toString();
        String pw_str = input_pw_ET.getText().toString();

        // 각 폼 입력확인
        if(email_str.length()==0 || pw_str.length()==0) {
            myapp.logAndToast("로그인 정보를 모두 입력해주세요");
            return;
        }

        // 이메일 정규식 확인
        if(!myapp.email_check(email_str)) {
            myapp.logAndToast("이메일이 형식에 맞지않습니다");
            return;
        }

        /** User 객체 생성하여 필요한 변수만 채워넣기 */
        Users user = new Users();
        user.setUser_email(email_str);
        user.setUser_pw(pw_str);

        /** user object convert to JsonString using with Gson */
        Gson gson = new Gson();
        String login_info_json = gson.toJson(user);
        Log.d(TAG, "login_info_json: " + login_info_json);

        /** 서버 통신 - 회원가입 */
        RetrofitService rs = ServiceGenerator.createService(RetrofitService.class);
        Call<ResponseBody> call_result = rs.email_login(
                Static.EMAIL_LOGIN,
                login_info_json);
        call_result.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String retrofit_result = response.body().string();
                    Log.d(TAG, "retrofit_result: "+retrofit_result);

                    // 리턴 타입 확인해보기 위한 코드
//                    Object obj = response.body().string();
//                    Log.d(TAG, "return type=" + obj.getClass().getName());

                    if(retrofit_result.equals("fail")) {
                        myapp.logAndToast("로그인 정보가 올바르지 않습니다");
                    }
                    else if(retrofit_result.equals("error")) {
                        myapp.logAndToast("예외발생: " + retrofit_result);
                    }
                    else {
                        JSONObject user_jsonObject = new JSONObject(retrofit_result);
                        Log.d(TAG, "user_no: " + user_jsonObject.get("user_no"));

                        // 어플리케이션 객체에 내 정보 저장
                        myapp.set_myInfo(user_jsonObject);
                        myapp.logAndToast(String.valueOf(user_jsonObject.get("user_nickname"))
                                + "님 어서오세요~");

                        Intent intent = new Intent(getBaseContext(), Main_after_login_A.class);
                        startActivity(intent);
                    }




                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                myapp.logAndToast("onFailure_result" + t.getMessage());
            }
        });



    }

    public void login_google(View view) {
    }


    //쉐어드 초기화_ 테스트용
    public void onShared_Initializing() {
        SharedPreferences auto_increament = getSharedPreferences("auto_increament", MODE_PRIVATE);
        SharedPreferences.Editor edit_Auto_incre = auto_increament.edit();
        edit_Auto_incre.clear().apply();

        SharedPreferences meeting_num = getSharedPreferences("meeting_num", MODE_PRIVATE);
        SharedPreferences.Editor edit_meeting_num = meeting_num.edit();
        edit_meeting_num.clear().apply();
//
//        /** 페이스북 email 정보 제공 거절 여부 */
//        SharedPreferences Facebook_doNot_ask_email = getSharedPreferences("facebook_doNot_ask_email", MODE_PRIVATE);
//        SharedPreferences.Editor Facebook_doNot_ask_email_edit = Facebook_doNot_ask_email.edit();
//        Facebook_doNot_ask_email_edit.clear().apply();

//        /** fireBase Token */
//        SharedPreferences fireBase_token_shared = getSharedPreferences("fireBase_token", MODE_PRIVATE);
//        SharedPreferences.Editor fireBase_token_edit = fireBase_token_shared.edit();
//        fireBase_token_edit.clear().apply();
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 회원가입
     ---------------------------------------------------------------------------*/
    public void email_join(View view) {
        Intent intent = new Intent(this, Email_join_A.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }
}
