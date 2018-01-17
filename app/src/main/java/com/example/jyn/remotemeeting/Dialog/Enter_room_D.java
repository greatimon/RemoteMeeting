package com.example.jyn.remotemeeting.Dialog;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.jyn.remotemeeting.Etc.Static;
import com.example.jyn.remotemeeting.R;
import com.example.jyn.remotemeeting.Util.Image_round_helper;
import com.example.jyn.remotemeeting.Util.Myapp;

import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.wasabeef.glide.transformations.CropCircleTransformation;

/**
 * Created by JYN on 2017-12-04.
 */

public class Enter_room_D extends Activity {

    /** 버터나이프*/
    public Unbinder unbinder;
    @BindView(R.id.back_img)        ImageView back_img;
    @BindView(R.id.profile_img)     ImageView profile_img;
    @BindView(R.id.title)           TextView title;
    @BindView(R.id.nickName)        TextView nickName;
    @BindView(R.id.email)           TextView email;
    @BindView(R.id.go_meeting)      TextView go_meeting;

    private static final String TAG = "all_"+Enter_room_D.class.getSimpleName();
    Myapp myapp;
    String creator_user_no;
    String creator_email;
    String creator_nickName;
    String creator_img_fileName;
    String real_meeting_title;
    String transform_meeting_title;

    /**---------------------------------------------------------------------------
     생명주기 ==> onCreate
     ---------------------------------------------------------------------------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.v_enter_room);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        this.setFinishOnTouchOutside(true);

        // 버터나이프 바인드
        unbinder = ButterKnife.bind(this);
        // 어플리케이션 객체 생성
        myapp = Myapp.getInstance();

        // 이미지 모서리 둥글게 만들기
        int random = new Random().nextInt(7);
        BitmapDrawable drawable = (BitmapDrawable) getResources().getDrawable(myapp.round_back_img[random]);
        Bitmap drawable_bitmap = drawable.getBitmap();
        Bitmap bitmap = Image_round_helper.getRoundedCornerBitmap(drawable_bitmap, 50);
        back_img.setImageBitmap(bitmap);

        // 이미지뷰에 어두운 효과 주기
        back_img.setColorFilter(Color.parseColor("#72746b"), PorterDuff.Mode.MULTIPLY);

        // 인텐트값 받기
        Intent intent = getIntent();
        creator_user_no = intent.getStringExtra("creator_user_no");
        creator_email = intent.getStringExtra("creator_email");
        creator_nickName = intent.getStringExtra("creator_nickName");
        creator_img_fileName = intent.getStringExtra("creator_img_fileName");
        real_meeting_title = intent.getStringExtra("real_meeting_title");
        transform_meeting_title = intent.getStringExtra("transform_meeting_title");

        // 회의 제목, 닉네임, 이메일 셋팅
        title.setText(real_meeting_title);
        nickName.setText(creator_nickName);
        email.setText(creator_email);

        // 이미지 셋팅
        if(creator_img_fileName.equals("none")) {
            profile_img.setImageResource(R.drawable.default_profile);
        }
        else if(!creator_img_fileName.equals("none")) {
            Glide
                .with(this)
                .load(Static.SERVER_URL_PROFILE_FILE_FOLDER + creator_img_fileName)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .bitmapTransform(new CropCircleTransformation(this))
                .into(profile_img);
        }

    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 회의 참여
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.go_meeting)
    public void go_meeting() {
        Intent intent = new Intent();
        intent.putExtra("real_meeting_title", real_meeting_title);
        intent.putExtra("transform_meeting_title", transform_meeting_title);
        intent.putExtra("creator_user_no", creator_user_no);
        setResult(RESULT_OK, intent);
        finish();
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
}
