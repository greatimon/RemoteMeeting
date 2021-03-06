package com.example.jyn.remotemeeting.Dialog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.jyn.remotemeeting.R;
import com.example.jyn.remotemeeting.Util.Myapp;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by JYN on 2017-12-12.
 */

public class Chat_draw_menu_D extends Activity {


    /** 버터나이프*/
    public Unbinder unbinder;
    @BindView(R.id.menu)LinearLayout menu;

    private static final String TAG = "all_"+Chat_draw_menu_D.class.getSimpleName();
    String request_class;

    Myapp myapp;


    /**---------------------------------------------------------------------------
     생명주기 ==> onCreate
     ---------------------------------------------------------------------------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        overridePendingTransition(R.anim.chat_menu_emerge, R.anim.hold);
        setContentView(R.layout.v_chat_draw_menu);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
//                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        this.setFinishOnTouchOutside(true);

        // 어플리케이션 객체 생성
        myapp = Myapp.getInstance();

        // 이 클래스를 호출한 클래스 인텐트 값으로 받기
        Intent get_intent = getIntent();
        request_class = get_intent.getStringExtra("request_class");

        // 버터나이프 바인드
        unbinder = ButterKnife.bind(this);

        Display display = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point point = new Point();

        display.getSize(point);
        int height = point.y;
        int width = point.x;
        int apply_width = (width/5)*4;

        Log.d(TAG, "Height : " + height + ", Width : " + width);
        Log.d(TAG, "apply_width : " + apply_width);

        menu.getLayoutParams().width = apply_width;

        WindowManager.LayoutParams wmlp = getWindow().getAttributes();
        wmlp.gravity = Gravity.RIGHT;

    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.hold, R.anim.chat_menu_disappear);
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
