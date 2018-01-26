package com.example.jyn.remotemeeting.Dialog;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.jyn.remotemeeting.Etc.Static;
import com.example.jyn.remotemeeting.R;
import com.example.jyn.remotemeeting.Util.Myapp;
import com.example.jyn.remotemeeting.Util.RetrofitService;
import com.example.jyn.remotemeeting.Util.ServiceGenerator;

import java.io.IOException;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.CropCircleTransformation;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by JYN on 2017-12-02.
 */

public class Profile_detail_D extends Activity {

    /** 버터나이프*/
    public Unbinder unbinder;
    @BindView(R.id.profile_background_img)      ImageView profile_background_img;
    @BindView(R.id.profile_img)                 ImageView profile_img;
    @BindView(R.id.back)                        ImageView back;
    @BindView(R.id.on_meeting)                  ImageView on_meeting;
    @BindView(R.id.unFollow)                    LinearLayout unFollow;
    @BindView(R.id.go_chat)                     LinearLayout go_chat;
    @BindView(R.id.go_video_call)               LinearLayout go_video_call;
    @BindView(R.id.follow)                      TextView follow;
    @BindView(R.id.nickName)                    TextView nickName;
    @BindView(R.id.email)                       TextView email;

    private static final String TAG = "all_"+Profile_detail_D.class.getSimpleName();
    Myapp myapp;
    String user_no;
    String user_nickname;
    String user_email;
    String user_img_fileName;
    String on_meeting_orNot;
    int REQUEST_SUBTRACT_CONFIRM = 1643;

    /** 이 클래스를 호출한 클래스 SimpleName */
    String request_class;


    /**---------------------------------------------------------------------------
     생명주기 ==> onCreate
     ---------------------------------------------------------------------------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.v_profile_detail);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        this.setFinishOnTouchOutside(false);

        // 버터나이프 바인드
        unbinder = ButterKnife.bind(this);
        // 어플리케이션 객체 생성
        myapp = Myapp.getInstance();

        // 이 클래스를 호출한 클래스 인텐트 값으로 받기
        Intent get_intent = getIntent();
        request_class = get_intent.getStringExtra(Static.REQUEST_CLASS);

        Intent intent = getIntent();
        user_no = intent.getExtras().getString("user_no");
        user_nickname = intent.getExtras().getString("nickname");
        user_email = intent.getExtras().getString("email");
        user_img_fileName = intent.getExtras().getString("img_fileName");
        on_meeting_orNot = intent.getExtras().getString("on_meeting");
        Log.d(TAG, "getIntent: " + user_no + user_nickname + email + user_img_fileName);

        // 닉네임, 이메일 셋팅
        nickName.setText(user_nickname);
        email.setText(user_email);

        // 이미지 셋팅
        if(user_img_fileName.equals("none")) {
            profile_img.setImageResource(R.drawable.default_profile);
            int random = new Random().nextInt(7);
            Glide
                .with(this)
                .load(myapp.back_img[random])
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .bitmapTransform(new BlurTransformation(this))
                .into(profile_background_img);
        }
        else if(!user_img_fileName.equals("none")) {
            Glide
                .with(this)
                .load(Static.SERVER_URL_PROFILE_FILE_FOLDER + user_img_fileName)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .bitmapTransform(new CropCircleTransformation(this))
                .into(profile_img);
            Glide
                .with(this)
                .load(Static.SERVER_URL_PROFILE_FILE_FOLDER + user_img_fileName)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .bitmapTransform(new BlurTransformation(this))
                .into(profile_background_img);
        }

        // 파트너 맺기, 끊기 Visibility
        unFollow.setVisibility(View.VISIBLE);
        follow.setVisibility(View.GONE);

        // 회의중 여부 표시하기
        if(on_meeting_orNot.equals("")) {
            on_meeting.setVisibility(View.GONE);
        }
        else if(!on_meeting_orNot.equals("")) {
            on_meeting.setVisibility(View.VISIBLE);
        }
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
     클릭이벤트 ==> 이메일 보내기
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.email)
    public void email(View view) {
        Log.d(TAG, "이메일 보내기 클릭");
        // TODO: redis - 클릭이벤트
        myapp.Redis_log_click_event(getClass().getSimpleName(), view);

    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 채팅
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.go_chat)
    public void go_chat(View view) {
        // TODO: redis - 클릭이벤트
        myapp.Redis_log_click_event(getClass().getSimpleName(), view);

        Log.d(TAG, "채팅 하기 클릭");
        Intent intent = new Intent();
        intent.putExtra("target_user_no", user_no);
        setResult(RESULT_OK, intent);
        finish();
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 파트너 끊기
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.unFollow)
    public void unFollow() {
        // TODO: redis - 화면 이동
        myapp.Redis_log_view_crossOver_from_to(
                getClass().getSimpleName(), Confirm_subtract_partner_D.class.getSimpleName());

        Intent intent = new Intent(this, Confirm_subtract_partner_D.class);
        intent.putExtra(Static.REQUEST_CLASS, getClass().getSimpleName());
        startActivityForResult(intent, REQUEST_SUBTRACT_CONFIRM);
    }


    /**---------------------------------------------------------------------------
     오버라이드 ==> onActivityResult
     ---------------------------------------------------------------------------*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 파트너 끊기로 했다면
        if(requestCode==REQUEST_SUBTRACT_CONFIRM && resultCode==RESULT_OK) {
            unFollow.setClickable(false);
            /** 서버 통신 - 파트너 끊기 */
            RetrofitService rs = ServiceGenerator.createService(RetrofitService.class);
            Call<ResponseBody> call_result = rs.break_partner(
                    Static.BREAK_PARTNER,
                    myapp.getUser_no(), user_no);
            call_result.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        String retrofit_result = response.body().string();
                        Log.d(TAG, "retrofit_result: "+retrofit_result);

                        if(retrofit_result.equals("success")) {
                            unFollow.setClickable(true);
                            unFollow.setVisibility(View.GONE);
                            follow.setVisibility(View.VISIBLE);
                        }
                        else if(retrofit_result.equals("fail")) {
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
    }

    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 파트너 맺기
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.follow)
    public void follow(View view) {
        // TODO: redis - 클릭이벤트
        myapp.Redis_log_click_event(getClass().getSimpleName(), view);

        follow.setClickable(false);
        /** 서버 통신 - 파트너 맺기 */
        RetrofitService rs = ServiceGenerator.createService(RetrofitService.class);
        Call<ResponseBody> call_result = rs.become_a_partner(
                Static.BECOME_A_PARTNER,
                myapp.getUser_no(), user_no);
        call_result.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String retrofit_result = response.body().string();
                    Log.d(TAG, "retrofit_result: "+retrofit_result);

                    if(retrofit_result.equals("success")) {
                        follow.setClickable(true);
                        follow.setVisibility(View.GONE);
                        unFollow.setVisibility(View.VISIBLE);
                    }
                    else if(retrofit_result.equals("fail")) {
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

        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();

        super.onDestroy();
    }
}
