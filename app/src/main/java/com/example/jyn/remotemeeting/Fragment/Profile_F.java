package com.example.jyn.remotemeeting.Fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.jyn.remotemeeting.Activity.Main_after_login_A;
import com.example.jyn.remotemeeting.DataClass.Users;
import com.example.jyn.remotemeeting.Dialog.Choose_method_for_select_img_D;
import com.example.jyn.remotemeeting.Etc.Static;
import com.example.jyn.remotemeeting.Otto.BusProvider;
import com.example.jyn.remotemeeting.Otto.Event;
import com.example.jyn.remotemeeting.R;
import com.example.jyn.remotemeeting.Util.Myapp;
import com.example.jyn.remotemeeting.Util.RetrofitService;
import com.example.jyn.remotemeeting.Util.ServiceGenerator;
import com.squareup.otto.Subscribe;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.CropCircleTransformation;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by JYN on 2017-11-10.
 */

public class Profile_F extends Fragment {

    private static final String TAG = "all_"+Profile_F.class.getSimpleName();
    LayoutInflater inflater;
    ViewGroup container;
    public View controlView;
    Myapp myapp;
    ProgressDialog progressDialog;

    /** 버터나이프 */
    public Unbinder unbinder;
    @BindView(R.id.profile_background_img)      ImageView profile_background_img;
    @BindView(R.id.profile_img)                 ImageView profile_img;
    @BindView(R.id.init)                        ImageView init;
    @BindView(R.id.choose_method_for_img)       ImageView choose_method_for_img;
    @BindView(R.id.nickName)                    TextView nickName;
    @BindView(R.id.email)                       TextView email;
    @BindView(R.id.nickName_ET)                 EditText nickName_ET;
    @BindView(R.id.complete)                    LinearLayout complete;
    @BindView(R.id.cancel)                      LinearLayout cancel;
    @BindView(R.id.modify)                      LinearLayout modify;


    public Profile_F() {
        // Required empty public constructor
    }

    public static Profile_F newInstance() {
        Bundle args = new Bundle();

        Profile_F fragment = new Profile_F();
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
        controlView = inflater.inflate(R.layout.f_profile, container, false);
        // 버터나이프 바인드
        unbinder = ButterKnife.bind(this, controlView);
        // 어플리케이션 객체 생성
        myapp = Myapp.getInstance();
        // otto 등록
        BusProvider.getBus().register(this);

        // 뷰 Visibility 셋팅
        init.setVisibility(View.GONE);
        choose_method_for_img.setVisibility(View.GONE);
        nickName_ET.setVisibility(View.GONE);
        complete.setVisibility(View.GONE);
        cancel.setVisibility(View.GONE);

        /** 서버로부터 내 정보 받기 */
        Users user = myapp.get_user_info(myapp.getUser_no());

        /** 데이터 셋팅하기 */
        // 이미지 셋팅
        String user_img_fileName = user.getUser_img_filename();
        set_img(user_img_fileName, "server");

        // 닉네임, 이메일 셋팅
        nickName.setText(user.getUser_nickname());
        email.setText(user.getUser_email());

        // 프로필 이미지 클릭 비활성화
        profile_img.setClickable(false);

        // 임시 변수에, 현재 닉네임, 이미지파일 이름 넣어놓기
        myapp.setTemp_nickname(myapp.getUser_nickname());
        myapp.setTemp_img_filename(myapp.getUser_img_filename());
        Log.d(TAG, "임시변수에 넣은 닉네임: " + myapp.getUser_nickname());
        Log.d(TAG, "임시변수에 넣은 이미지 파일이름: " + myapp.getUser_img_filename());

        return controlView;
    }


    /**---------------------------------------------------------------------------
     생명주기 ==> onResume
     ---------------------------------------------------------------------------*/
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 프로필 수정모드로
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.modify)
    public void modify() {
        modify.setVisibility(View.GONE);
        cancel.setVisibility(View.VISIBLE);
        complete.setVisibility(View.VISIBLE);
        choose_method_for_img.setVisibility(View.VISIBLE);
        nickName.setVisibility(View.GONE);
        profile_img.setClickable(true);

        nickName_ET.setText(myapp.getUser_nickname());
        nickName_ET.setVisibility(View.VISIBLE);
        nickName_ET.setSelection(nickName_ET.length()); // EditText의 커서를 맨 끝으로

        if(myapp.getUser_img_filename().equals("none")) {
            init.setVisibility(View.GONE);
        }
        else {
            init.setVisibility(View.VISIBLE);
        }
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 이미지 가져오기
     ---------------------------------------------------------------------------*/
    @OnClick({R.id.choose_method_for_img, R.id.profile_img})
    public void choose_method_for_img() {
        Intent intent = new Intent(getActivity(), Choose_method_for_select_img_D.class);
        getActivity().startActivityForResult(intent, Main_after_login_A.REQUEST_CHOOSE_METHOD_FOR_IMG);
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 수정 완료
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.complete)
    public void complete() {
        // 닉네임 temp 변수에 넣기
        String edit_nickname = nickName_ET.getText().toString();
        myapp.setTemp_nickname(edit_nickname);

        Log.d(TAG, "edit_nickname: " + edit_nickname);
        Log.d(TAG, "myapp.getTemp_nickname(): " + myapp.getTemp_nickname());
        Log.d(TAG, "myapp.getUser_nickname(): " + myapp.getUser_nickname());

        // 닉네임, 이미지 둘다 변경되지 않은 경우
        if(myapp.getTemp_nickname().equals(myapp.getUser_nickname()) &&
                myapp.getTemp_img_filename().equals(myapp.getUser_img_filename())) {
            myapp.logAndToast("변경된 정보가 없습니다");
            return;
        }

        // 닉네임이 변경된 경우
        if(!myapp.getTemp_nickname().equals(myapp.getUser_nickname())) {
            Log.d(TAG, "서버 비동기 닉네임 변경 로직 구현");
            update_nickname(myapp.getTemp_nickname());
        }

        // 이미지가 변경된 경우
        if(!myapp.getTemp_img_filename().equals(myapp.getUser_img_filename())) {
            Log.d(TAG, "서버 비동기 이미지 업로드 로직 구현");
            upload_profile_img(myapp.getTemp_img_absolutePath());

            // 닉네임은 원래대로
            nickName.setText(myapp.getUser_nickname());
            nickName.setVisibility(View.VISIBLE);
        }

        // 뷰 Visibility 조절
        modify.setVisibility(View.VISIBLE);
        cancel.setVisibility(View.GONE);
        complete.setVisibility(View.GONE);
        init.setVisibility(View.GONE);
        choose_method_for_img.setVisibility(View.GONE);
        nickName_ET.setVisibility(View.GONE);
        profile_img.setClickable(false);

        // 닉네임, 이미지 셋팅은 각 업로드 완료 콜백 메소드 부분에 있음
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 수정 취소
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.cancel)
    public void cancel() {
        modify.setVisibility(View.VISIBLE);
        cancel.setVisibility(View.GONE);
        complete.setVisibility(View.GONE);
        init.setVisibility(View.GONE);
        choose_method_for_img.setVisibility(View.GONE);
        nickName_ET.setVisibility(View.GONE);
        profile_img.setClickable(false);

        // 닉네임 원래대로
        nickName.setText(myapp.getUser_nickname());
        nickName.setVisibility(View.VISIBLE);

        // 이미지 원래대로
        set_img(myapp.getUser_img_filename() ,"server");

        // Myapp 애플리케이션 객체의 temp 변수 초기화
        myapp.setTemp_nickname(myapp.getUser_nickname());
        myapp.setTemp_img_filename(myapp.getUser_img_filename());
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 사진 초기화 -- 디폴트 이미지로 넣기
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.init)
    public void init_img() {
        // 디폴트 이미지 셋팅
        set_img("", "init");

        // 임시 변수값 변경
        myapp.setTemp_img_filename("none");
        myapp.setTemp_img_absolutePath("");

        // 뷰 GONE 처리
        init.setVisibility(View.GONE);
    }


    /**---------------------------------------------------------------------------
     otto ==> Main_after_login_A로 부터 message 수신 -- 이미지 셋팅
     ---------------------------------------------------------------------------*/
    @Subscribe
    public void getMessage(Event.Main_after_login_A__Profile_F event) {
        Log.d(TAG, "otto 받음_ " + event.getMessage());
        Log.d(TAG, "otto 받음_ " + event.getData());

        if(event.getMessage().equals("image")) {
            // 이미지 뷰에 셋팅
            set_img(event.getData(), "local");

            // Myapp 애플리케이션 객체의 temp 변수에 데이터 임시 저장
            // - absolutePath 저장
            myapp.setTemp_img_absolutePath(event.getData());

            // - 파일 이름 저장
            String[] split_str = event.getData().split("[/]");
            String for_save = split_str[split_str.length-1];
            myapp.setTemp_img_filename(for_save);
            Log.d(TAG, "임시저장 이미지 파일 이름: " + for_save);

            // init 뷰 VISIBLE 처리
            init.setVisibility(View.VISIBLE);
        }
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 이미지 셋팅
     ---------------------------------------------------------------------------*/
    public void set_img(String absolutePath, String type) {
        if(type.equals("server")) {
            if(absolutePath.equals("none")) {
                profile_img.setImageResource(R.drawable.default_profile);
                int random = new Random().nextInt(7);
                Glide
                    .with(this)
                    .load(myapp.back_img[random])
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .bitmapTransform(new BlurTransformation(getContext()))
                    .into(profile_background_img);
            }
            else if(!absolutePath.equals("none")) {
                Glide
                    .with(this)
                    .load(Static.SERVER_URL_PROFILE_FILE_FOLDER + absolutePath)
//                    .placeholder(R.drawable.on_loading)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .bitmapTransform(new CropCircleTransformation(getContext()))
                    .into(profile_img);
                Glide
                    .with(this)
                    .load(Static.SERVER_URL_PROFILE_FILE_FOLDER + absolutePath)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .bitmapTransform(new BlurTransformation(getContext()))
                    .into(profile_background_img);
            }
        }
        else if(type.equals("local")) {
            Glide
                .with(this)
                .load(absolutePath)
//                .placeholder(R.drawable.on_loading)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .bitmapTransform(new CropCircleTransformation(getContext()))
                .into(profile_img);
            Glide
                .with(this)
                .load(absolutePath)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .bitmapTransform(new BlurTransformation(getContext()))
                .into(profile_background_img);
        }
        else if(type.equals("init")) {
            int random = new Random().nextInt(7);
            Glide
                .with(this)
                .load(R.drawable.default_profile)
//                .placeholder(R.drawable.on_loading)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .bitmapTransform(new CropCircleTransformation(getContext()))
                .into(profile_img);
            Glide
                .with(this)
                .load(myapp.back_img[random])
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .bitmapTransform(new BlurTransformation(getContext()))
                .into(profile_background_img);
        }
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 서버 통신 -- 닉네임 변경
     ---------------------------------------------------------------------------*/
    public void update_nickname(final String nickname) {
        RetrofitService rs = ServiceGenerator.createService(RetrofitService.class);
        Call<ResponseBody> call_result = rs.update_nickname(
                Static.UPDATE_NICKNAME,
                myapp.getUser_no(), nickname);
        call_result.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String retrofit_result = response.body().string();
                    Log.d(TAG, "retrofit_result: "+retrofit_result);

                    if(retrofit_result.equals("success")) {
                        Log.d(TAG, "변경된 닉네임: " + nickname);
                        // 닉네임 저장
                        myapp.setUser_nickname(nickname);
                        // 임시 변수에, 이미지파일 이름 넣어놓기
                        myapp.setTemp_nickname(nickname);
                        // 닉네임 셋팅
                        nickName.setText(nickname);
                        nickName.setVisibility(View.VISIBLE);
                    }
                    else if(retrofit_result.equals("fail")) {
                        myapp.logAndToast("onResponse_fail" + response.errorBody().string());
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
     메소드 ==> 서버 통신 -- 이미지 업로드
     ---------------------------------------------------------------------------*/
    public void upload_profile_img(String absolutePath) {

        // 디폴트 이미지로 변경 했을 경우
        if(absolutePath.equals("") && myapp.getTemp_img_filename().equals("none")) {
            RetrofitService rs = ServiceGenerator.createService(RetrofitService.class);
            Call<ResponseBody> call = rs.set_default_img(
                    Static.SET_DEFAULT_IMG,
                    myapp.getUser_no());
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        String retrofit_result = response.body().string();
                        Log.d(TAG, "default_img_result: "+retrofit_result);

                        if(retrofit_result.equals("success")) {
                            // 이미지 파일 이름 저장
                            myapp.setUser_img_filename("none");
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
            return;
        }

        // 디폴트 이미지가 아닐 경우
        // 프로그레스 다이얼로그 띄우기
        show_progress("이미지 업로드 중입니다");

        // 파일 객체 생성
        File file = new File(absolutePath);

        // 확장자만 분류
        int Idx = file.getName().lastIndexOf(".");
        String format = file.getName().substring(Idx+1);
        Log.d(TAG, "format: " + format);

        // RequestBody 생성 from file
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/" + format), file);
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("image", file.getName(), requestFile);

        // user_no
        RequestBody user_no = RequestBody.create(MediaType.parse("text/plain"), myapp.getUser_no());

        RetrofitService rs = ServiceGenerator.createService(RetrofitService.class);
        Call<ResponseBody> call = rs.upload_profile_img(
                Static.UPLOAD_PROFILE_IMG,
                user_no, body);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String retrofit_result = response.body().string();
                    Log.d(TAG, "retrofit_result: "+retrofit_result);

                    String[] temp = retrofit_result.split(Static.SPLIT);

                    if(temp[0].equals("success")) {
                        Log.d(TAG, "file_name: " + temp[1]);

                        // 이미지 파일 이름 저장
                        myapp.setUser_img_filename(temp[1]);
                        // 임시 변수에, 이미지파일 이름 넣어놓기
                        myapp.setTemp_img_filename(temp[1]);
                        // 절대경로 임시 변수 값 초기화
                        myapp.setTemp_img_absolutePath("");
                        // 이미지 셋팅
                        set_img(myapp.getUser_img_filename(), "server");
                    }
                    else if(temp[0].equals("fail")) {
                        Log.d(TAG, "업로드 실패: " + temp[1]);
                        myapp.logAndToast("업로드 실패" + temp[1]);
                    }

                    // 프로그레스 다이얼로그 dismiss
                    dismiss_progress();

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
     메소드 ==> 프로그레스 다이얼로그 show
     ---------------------------------------------------------------------------*/
    public void show_progress(String msg) {
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(msg);
        progressDialog.show();
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 프로그레스 다이얼로그 dismiss
     ---------------------------------------------------------------------------*/
    public void dismiss_progress() {
        if(progressDialog != null) {
            progressDialog.dismiss();
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
