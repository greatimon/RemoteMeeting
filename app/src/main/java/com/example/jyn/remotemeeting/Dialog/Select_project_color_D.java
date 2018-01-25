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
import android.widget.TextView;

import com.example.jyn.remotemeeting.R;
import com.example.jyn.remotemeeting.Util.Myapp;

import java.util.concurrent.ConcurrentHashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by JYN on 2018-01-25.
 */

public class Select_project_color_D extends Activity {

    private static final String TAG = "all_"+Select_project_color_D.class.getSimpleName();
    Myapp myapp;

    // intent 값을 넘겨받을 때 사용할 intent
    Intent get_intent;

    // 리턴할 때 사용할 intent
    Intent setResult_intent;

    // intent 값으로 넘어온, 이전에 설정했던 프로젝트 컬러
    int already_set_color_resource_index = -1;

    // 이전에 설정했던 프로젝트 컬러의 해당하는 ImageView
    ImageView selected_imgV;

    // 프로젝트 폴더들의 drawable int 배열
    int[] folder_drawable_id = {
            R.id.amber, R.id.blue, R.id.blue_grey, R.id.brown,
            R.id.deep_orange, R.id.deep_purple, R.id.green,
            R.id.indigo, R.id.light_green, R.id.orange,
            R.id.pink, R.id.purple, R.id.red, R.id.teal
    };

    // 폴더 하나를 나타내는 프래그먼트와, 해당 폴더 이미지뷰를 해쉬맵으로 바인딩
    ConcurrentHashMap<Integer, Integer> frag_imgV = new ConcurrentHashMap<>();

    /** 버터나이프*/
    public Unbinder unbinder;
    @BindView(R.id.comment_txt)         TextView comment_txt;
    @BindView(R.id.default_divider)     View default_divider;
    @BindView(R.id.amber)               ImageView amber;
    @BindView(R.id.blue)                ImageView blue;
    @BindView(R.id.blue_grey)           ImageView blue_grey;
    @BindView(R.id.brown)               ImageView brown;
    @BindView(R.id.deep_orange)         ImageView deep_orange;
    @BindView(R.id.deep_purple)         ImageView deep_purple;
    @BindView(R.id.green)               ImageView green;
    @BindView(R.id.indigo)              ImageView indigo;
    @BindView(R.id.orange)              ImageView orange;
    @BindView(R.id.purple)              ImageView purple;
    @BindView(R.id.pink)                ImageView pink;
    @BindView(R.id.red)                 ImageView red;
    @BindView(R.id.teal)                ImageView teal;
    @BindView(R.id.light_green)         ImageView light_green;



    /**---------------------------------------------------------------------------
     생명주기 ==> onCreate
     ---------------------------------------------------------------------------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.v_select_project_color);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        this.setFinishOnTouchOutside(true);

        // 버터나이프 바인드
        unbinder = ButterKnife.bind(this);
        // 어플리케이션 객체 생성
        myapp = Myapp.getInstance();

        // 이미지뷰에 int 값 setTag 하는 내부 메소드 호출
        // Tag 하는 int 값 --> 프로젝트 컬러의 해당하는 color resource index 값
        setTag_into_imageView();

        // 폴더 하나를 나타내는 프래그먼트와, 해당 폴더 이미지뷰를 해쉬맵으로 바인딩
        create_hash();

        // 인텐트 값 전달 받기
        get_intent = getIntent();
        already_set_color_resource_index = get_intent.getIntExtra("color_resource_index", -1);
        Log.d(TAG, "already_set_color_resource_index: " + already_set_color_resource_index);

        // 이전에 설정한 컬러가 있다면
        if(already_set_color_resource_index != -1) {
            // 최상단 뷰 조절 및 텍스트 컬러 변경
            comment_txt.setTextColor(Color.parseColor("#FFFFFF"));
            comment_txt.setBackgroundColor(
                    myapp.folder_color_int_value[already_set_color_resource_index]);
            default_divider.setVisibility(View.GONE);

            // 이전에 설정한 컬러에 해당하는 '브이'체크 ImageView set VISIBLE
            for(int i=0; i<folder_drawable_id.length; i++) {
                if((int)findViewById(folder_drawable_id[i]).getTag() == already_set_color_resource_index) {
                    findViewById(folder_drawable_id[i]).setVisibility(View.VISIBLE);
                    selected_imgV = findViewById(folder_drawable_id[i]);
                }
            }
        }
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 프로젝트 컬러 선택
                 미리 생성해놓은 해쉬맵에서 클릭한 프래그먼트의 view_id를 키값으로,
                 해당 이미지 뷰의 view_id 값을 찾아, 내부에 다른 메소드를 호출
     ---------------------------------------------------------------------------*/
    @OnClick({R.id.amber_f, R.id.blue_f, R.id.blue_grey_f, R.id.brown_f,
            R.id.deep_orange_f, R.id.deep_purple_f, R.id.green_f,
            R.id.indigo_f, R.id.light_green_f, R.id.orange_f,
            R.id.pink_f, R.id.purple_f, R.id.red_f, R.id.teal_f})
    public void select_project_color_frag(View view) {
        select_project_color(frag_imgV.get(view.getId()));
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 이미지 뷰의 view_id 값을 받아, 이전에 설정한 컬러인지 아닌지 확인 후
                setResult 후 finish() 한다
     ---------------------------------------------------------------------------*/
//    @OnClick({R.id.amber, R.id.blue, R.id.blue_grey, R.id.brown,
//            R.id.deep_orange, R.id.deep_purple, R.id.green,
//            R.id.indigo, R.id.light_green, R.id.orange,
//            R.id.pink, R.id.purple, R.id.red, R.id.teal})
    public void select_project_color(int view_id) {

        //// 이전에 설정한 컬러에 해당하는 ImageView가 아닐 때만, 선택이 가능하도록 한다
        if(already_set_color_resource_index != (int)findViewById(view_id).getTag()) {
            int color_resource_index = (int)findViewById(view_id).getTag();
            Log.d(TAG, "color_resource_index: " + color_resource_index);
            setResult_intent = new Intent();
            setResult_intent.putExtra("color_resource_index", color_resource_index);
            setResult(RESULT_OK, setResult_intent);
            finish();
        }
        else if(already_set_color_resource_index == (int)findViewById(view_id).getTag()) {
            myapp.logAndToast("이전에 이미 설정한 컬러입니다.");
        }

    }


    /**---------------------------------------------------------------------------
     메소드 ==> '프로젝트 drawable 아이콘' 에, 해당 color resource index 값을 Tag 값으로 지정
     ---------------------------------------------------------------------------*/
    public void setTag_into_imageView() {
        amber.setTag(0);
        blue.setTag(1);
        blue_grey.setTag(2);
        brown.setTag(3);
        deep_orange.setTag(4);
        deep_purple.setTag(5);
        green.setTag(6);
        indigo.setTag(8);
        light_green.setTag(9);
        orange.setTag(10);
        pink.setTag(11);
        purple.setTag(12);
        red.setTag(13);
        teal.setTag(14);
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 폴더 하나를 나타내는 프래그먼트와, 해당 폴더 이미지뷰를 해쉬맵으로 바인딩
     ---------------------------------------------------------------------------*/
    public void create_hash() {
        frag_imgV.put(R.id.amber_f, R.id.amber);
        frag_imgV.put(R.id.blue_f, R.id.blue);
        frag_imgV.put(R.id.blue_grey_f, R.id.blue_grey);
        frag_imgV.put(R.id.brown_f, R.id.brown);
        frag_imgV.put(R.id.deep_orange_f, R.id.deep_orange);
        frag_imgV.put(R.id.deep_purple_f, R.id.deep_purple);
        frag_imgV.put(R.id.green_f, R.id.green);
        frag_imgV.put(R.id.indigo_f,  R.id.indigo);
        frag_imgV.put(R.id.light_green_f, R.id.light_green);
        frag_imgV.put(R.id.orange_f, R.id.orange);
        frag_imgV.put(R.id.pink_f, R.id.pink);
        frag_imgV.put(R.id.purple_f, R.id.purple);
        frag_imgV.put(R.id.red_f, R.id.red);
        frag_imgV.put(R.id.teal_f, R.id.teal);
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
