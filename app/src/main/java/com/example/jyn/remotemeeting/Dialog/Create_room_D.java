package com.example.jyn.remotemeeting.Dialog;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.jyn.remotemeeting.Etc.Static;
import com.example.jyn.remotemeeting.R;
import com.example.jyn.remotemeeting.Util.Hangul;
import com.example.jyn.remotemeeting.Util.Image_round_helper;
import com.example.jyn.remotemeeting.Util.Myapp;

import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.wasabeef.glide.transformations.CropCircleTransformation;

/**
 * Created by JYN on 2017-12-03.
 */

public class Create_room_D extends Activity {

    /** 버터나이프*/
    public Unbinder unbinder;
    @BindView(R.id.back_img)            ImageView back_img;
    @BindView(R.id.add_subject_img)     ImageView add_subject_img;
    @BindView(R.id.profile_img)         ImageView profile_img;
    @BindView(R.id.title)               EditText title;
    @BindView(R.id.add_subject_text)    TextView add_subject_text;
    @BindView(R.id.go_meeting)          TextView go_meeting;
    @BindView(R.id.nickName)            TextView nickName;
    @BindView(R.id.email)               TextView email;
    @BindView(R.id.comment)             TextView comment;
    @BindView(R.id.before_add_subject)  LinearLayout before_add_subject;
    @BindView(R.id.after_add_subject)   RelativeLayout after_add_subject;

    private static final String TAG = "all_"+Create_room_D.class.getSimpleName();
    Myapp myapp;
    String request_class;
    static int REQUEST_ADD_SUBJECT = 1616;
    String subject_user_no = "";

    /**---------------------------------------------------------------------------
     생명주기 ==> onDestroy
     ---------------------------------------------------------------------------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.v_create_room);
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
        request_class = get_intent.getStringExtra("request_class");

        // 이미지 모서리 둥글게 만들기
        int random = new Random().nextInt(7);
        BitmapDrawable drawable = (BitmapDrawable)getResources().getDrawable(myapp.round_back_img[random]);
        Bitmap drawable_bitmap = drawable.getBitmap();
        Bitmap bitmap = Image_round_helper.getRoundedCornerBitmap(drawable_bitmap, 50);
        back_img.setImageBitmap(bitmap);

        // 이미지뷰에 어두운 효과 주기
        back_img.setColorFilter(Color.parseColor("#72746b"), PorterDuff.Mode.MULTIPLY);

        // 초기 View visibility 셋팅
        before_add_subject.setVisibility(View.VISIBLE);
        after_add_subject.setVisibility(View.GONE);
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 회의 대상 지정
     ---------------------------------------------------------------------------*/
    @OnClick({R.id.add_subject_img, R.id.add_subject_text, R.id.profile_img, R.id.nickName, R.id.email, R.id.comment})
    public void add_subject_user(View view) {
        // TODO: redis - 클릭이벤트
        myapp.Redis_log_click_event(getClass().getSimpleName(), view);

        // TODO: redis - 화면 이동
        myapp.Redis_log_view_crossOver_from_to(
                getClass().getSimpleName(), Add_subject_user_D.class.getSimpleName());

        Intent intent = new Intent(this, Add_subject_user_D.class);
        intent.putExtra(Static.REQUEST_CLASS, getClass().getSimpleName());
        startActivityForResult(intent, REQUEST_ADD_SUBJECT);
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 회의 시작
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.go_meeting)
    public void go_meeting(View view) {
        // TODO: redis - 클릭이벤트
        myapp.Redis_log_click_event(getClass().getSimpleName(), view);

        // TODO: 영문으로 변환 전의 회의 제목 공백 제거 안하기로 함
//        String input_title = (title.getText().toString()).replace(" ", ""); // 공백제거
//        String convert_str = Hangul.convert(input_title);   // 영문으로 변환(이모티콘, 특수문자 안됨)
        String input_title = title.getText().toString();
        // 영문으로 변환(이모티콘, 특수문자 안됨) + 공백 제거
        String convert_str = Hangul.convert(input_title.replace(" ", ""));
        Log.d(TAG, "convert: " + convert_str);

        // 회의 제목 길이 검사를 myapp/어플리케이션 객체에 있는 nickName 길이 검사 메소드로 진행함
        if(!myapp.nickName_check(convert_str)) {
            myapp.logAndToast("회의 제목은 2자 이상\n20자 이하로 작성해주세요");
            return;
        }

        else if(subject_user_no.equals("")) {
            myapp.logAndToast("회의 대상이 지정되지 않았습니다");
            return;
        }

        else if(!subject_user_no.equals("") && myapp.nickName_check(convert_str)) {
            Intent intent = new Intent();
            intent.putExtra("input_title", input_title);
            intent.putExtra("subject_user_no", subject_user_no);
            intent.putExtra("convert_str", convert_str);
            setResult(RESULT_OK, intent);
            finish();
        }
    }


    /**---------------------------------------------------------------------------
     오버라이드 ==> onActivityResult
     ---------------------------------------------------------------------------*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==REQUEST_ADD_SUBJECT && resultCode==RESULT_OK) {
            subject_user_no = data.getStringExtra("user_no");
            String subject_nickname = data.getStringExtra("nickname");
            String subject_email = data.getStringExtra("email");
            String subject_filename = data.getStringExtra("filename");
            Log.d(TAG, "subject_user_no: " + subject_user_no);
            Log.d(TAG, "subject_nickname: " + subject_nickname);
            Log.d(TAG, "subject_email: " + subject_email);
            Log.d(TAG, "subject_filename: " + subject_filename);

            // View 셋팅
            // 닉네임, 이메일 셋팅
            nickName.setText(subject_nickname);
            email.setText(subject_email);

            // 이미지 셋팅
            if(subject_filename.equals("none")) {
                profile_img.setImageResource(R.drawable.default_profile);
            }
            else if(!subject_filename.equals("none")) {
                Glide
                    .with(this)
                    .load(Static.SERVER_URL_PROFILE_FILE_FOLDER + subject_filename)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .bitmapTransform(new CropCircleTransformation(this))
                    .into(profile_img);
            }

            before_add_subject.setVisibility(View.GONE);
            after_add_subject.setVisibility(View.VISIBLE);
        }
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
