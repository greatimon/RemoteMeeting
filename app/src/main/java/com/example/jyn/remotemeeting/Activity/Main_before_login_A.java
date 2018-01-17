package com.example.jyn.remotemeeting.Activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.jyn.remotemeeting.DataClass.Users;
import com.example.jyn.remotemeeting.Etc.Static;
import com.example.jyn.remotemeeting.R;
import com.example.jyn.remotemeeting.Util.Get_currentTime;
import com.example.jyn.remotemeeting.Util.Myapp;
import com.example.jyn.remotemeeting.Util.RetrofitService;
import com.example.jyn.remotemeeting.Util.ServiceGenerator;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Main_before_login_A extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener{

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

    // 구글 로그인 관련 변수 ==========================
    // ==============================================
    private final int GOOGLE_LOGIN = 2345;
    private final int ADDITIONAL_INFO_REQUEST = 3000;
    // 로그인 진행 관련, 프로그레스 다이얼로그
    public ProgressDialog google_login_progressDialog;
    private FirebaseAuth firebaseAuth;
    private GoogleApiClient googleApiClient;


    /** 버터나이프*/
    public Unbinder unbinder;
    @BindView(R.id.google_login_layout)         LinearLayout google_login_layout;


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

        // 구글 로그인을 준비하기 위해, 구글API Client와 파이어베이스 인증 객체를 준비하고,
        // 해당 유저가 이미 파이어베이스에 로그인이 되어 있는 상태인지 확인해서
        // 로그인되어 있는 상태면 자동 로그인 진행하는 메소드 호출
        initialize_for_google_login();
    }


    /**
     Main_after_login_A 에서 로그아웃을 진행하기 되면 Main_before_login_A 를 열면서, intent flag로
     FLAG_ACTIVITY_SINGLE_TOP
     FLAG_ACTIVITY_CLEAR_TOP
     를 주게 되는데, 이때 Main_before_login_A 에서 호출되는 생명주기 순서는

     onRestart --> onStart --> onResume 이다
     */

    /**---------------------------------------------------------------------------
     생명주기 ==> onRestart
     ---------------------------------------------------------------------------*/
    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "========== onRestart ==========");

        input_email_ET.setText("");
        input_pw_ET.setText("");

    }


    /**---------------------------------------------------------------------------
     생명주기 ==> onStart
     ---------------------------------------------------------------------------*/
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "========== onStart ==========");
    }

    /**---------------------------------------------------------------------------
     생명주기 ==> onResume
     ---------------------------------------------------------------------------*/
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "========== onResume ==========");
        int random = new Random().nextInt(7);

        Glide
                .with(this)
                .load(back_img[random])
                .into(back_IV);
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 구글 로그인을 준비하기 위해, 구글API Client와 파이어베이스 인증 객체를 준비하고,

                해당 유저가 이미 파이어베이스에 로그인이 되어 있는 상태인지 확인해서
                로그인되어 있는 상태면 자동 로그인 진행하기
     ---------------------------------------------------------------------------*/
    public void initialize_for_google_login() {
        // Configure Google Sign In
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder
                (GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("263032945619-np2u9kklcj4hkqulljm3ervsc9ca69ci.apps.googleusercontent.com")
                .requestEmail()
                .build();
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedlistener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                .build();
        // 파이어베이스 인증 인스턴스 받아놓기
        firebaseAuth = FirebaseAuth.getInstance();

        /** 해당 유저가 이미 파이어베이스에 로그인이 되어 있는 상태인지 확인해서
            로그인되어 있는 상태면, 자동 로그인 진행하기*/
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if(currentUser != null) {
            String email = currentUser.getEmail();
            String pw = currentUser.getUid();
            Log.d(TAG, "initialize_for_google_login()_ email:" + email);
            Log.d(TAG, "initialize_for_google_login()_ pw:" + pw);

            // 자동로그인 정보 쉐어드를 통해 불러오기
            // 'google' 자동 로그인 모드가 'on' 인지 'off' 인지 확인하기
            SharedPreferences Auto_login = getSharedPreferences("Auto_login", MODE_PRIVATE);
            boolean auto_login_mode = Auto_login.getBoolean("google", false);

            // 자동 로그인 모드가 'true'일 때만 로그인 시도
            if(auto_login_mode) {
                // 서버 통신 -- ID, PW를 서버에 전송하여, 로그인 시도하는 내부 메소드 호출
                login(email, pw, "auto");
            }
        }
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 구글 로그인 버튼 클릭
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.google_login_layout)
    public void login_google() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, GOOGLE_LOGIN);
    }


    /**---------------------------------------------------------------------------
     콜백메소드 ==> onActivityResult
     ---------------------------------------------------------------------------*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "requestCode: " + requestCode);
        Log.d(TAG, "resultCode: " + resultCode);

        /** 구글 로그인 다이얼로그 진행창으로부터 돌아왔을 때 */
        if(requestCode == GOOGLE_LOGIN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if(result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();

//                String personName = account.getDisplayName();
//                String personEmail = account.getEmail();
//                String personId = account.getId();
//                String tokenKey = account.getServerAuthCode();
//
//                Log.d(TAG, "===================== onActivityResult =====================");
//                Log.d(TAG, "getDisplayName: " + personName);
//                Log.d(TAG, "getEmail: " + personEmail);
//                Log.d(TAG, "getId: " + personId);
//                Log.d(TAG, "getServerAuthCode: " + tokenKey);
//                Log.d(TAG, "getPhotoUrl: " + account.getPhotoUrl());
//                Log.d(TAG, "account.getAccount().type: " + account.getAccount().type);
//                Log.d(TAG, "account.getAccount().name: " + account.getAccount().name);
//                Log.d(TAG, "getFamilyName: " + account.getFamilyName());
//                Log.d(TAG, "getGivenName: " + account.getGivenName());

                firebaseAuthWithGoogle(account);
            }
        }

        /** 구글 이메일로 회원가입하기 위해 닉네임을 추가 입력 받는 창에서 돌아왔을 때 */
        else if(requestCode == ADDITIONAL_INFO_REQUEST) {
            // 회원 가입 완료
            if(resultCode == RESULT_OK) {
                String email_str = data.getStringExtra("email");
                String pw_str = data.getStringExtra("firebase_UID");

                // 서버 통신 -- ID, PW를 서버에 전송하여, 로그인 시도하는 내부 메소드 호출
                login(email_str, pw_str, "after_join");
            }
            // 회원 가입 취소
            else if(resultCode == RESULT_CANCELED) {
                // 파이어베이스 로그아웃
                firebaseAuth.signOut();
            }
        }
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 구글 로그인 한것, 파이어베이스에 인증 받기
     ---------------------------------------------------------------------------*/
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle: " + acct.getId());
        // [START_EXCLUDE silent]
        showProgressDialog();
        // [END_EXCLUDE]

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential: success");
                            FirebaseUser user = firebaseAuth.getCurrentUser();

                            /** 구글 이메일 정보로, 회원가입 or 로그인 진행하는 내부 메소드 호출*/
                            join_or_login_with_google_email(user.getEmail(), user.getUid());
                        }
                        else {
                            // If sign in fails, display a message to the user.
                            Log.d(TAG, "signInWithCredential: failure", task.getException());
                            myapp.logAndToast("Authentication failed");
                        }
                        // [START_EXCLUDE]
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });

    }


    /**---------------------------------------------------------------------------
     메소드 ==> 파이어베이스에서 인증한 이메일과, 패스워드로 사용할 파이어베이스의 UID값을 받아
                1. 이메일과 UID값 모두 일치하는 DB 정보가 있다면, 해당 이메일과 UID 값으로 로그인 진행
                2. 이메일만 일치하는 DB 정보가 있다면, 다른 사람이 사용중인 이메일이므로
                    해당 이메일을 사용할 수 없음을 알림
                3. DB에 해당 이메일 정보가 없는 경우, 닉네임을 받는 액티비티 띄워 회원가입 진행
     ---------------------------------------------------------------------------*/
    public void join_or_login_with_google_email(final String email, final String firebase_UID) {

        /** User 객체 생성하여 필요한 변수만 채워넣기 */
        Users user = new Users();
        user.setJoin_path("google");
        user.setUser_email(email);
        user.setUser_pw(firebase_UID);
        user.setUser_nickname("");
        user.setAndroid_id("");

        /** user object convert to JsonString using with Gson */
        Gson gson = new Gson();
        String join_info_json = gson.toJson(user);
        Log.d(TAG, "join_info_json: " + join_info_json);

        /** 서버 통신 - 이메일, UID(패스워드) 값 체크 */
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

                    // 일치하는 정보가 없다면 네임을 추가로 받는 액티비티 띄우기
                    if(temp[0].equals("non_overlap")) {
                        Intent intent = new Intent(getBaseContext(), Additional_info_for_google_api_join_A.class);
                        intent.putExtra("email", email);
                        intent.putExtra("firebase_UID", firebase_UID);
                        startActivityForResult(intent, ADDITIONAL_INFO_REQUEST);
                    }
                    // 일치하는 정보가 있으므로, 이미 회원가입이 되어 있는 유저임
                    // 해당 email과 UID 값으로 로그인 진행
                    else if(temp[0].equals("overlap")) {
                        // 서버 통신 -- ID, PW를 서버에 전송하여, 로그인 시도하는 내부 메소드 호출
                        login(email, firebase_UID, "already_exist");
                    }
                    // 이메일만 일치하는 경우,
                    // 다른 사람이 이미 사용중인 이메일이므로 사용이 불가능한 구글 계정임을, toast로 알린다
                    else if(temp[0].equals("non_useable")) {
                        myapp.logAndToast("이미 사용중인 이메일입니다");
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
     메소드 ==> 서버 통신 -- ID, PW를 서버에 전송하여, 로그인 시도
     ---------------------------------------------------------------------------*/
    public void login(String ID, String PW, final String type) {

        /** User 객체 생성하여 필요한 변수만 채워넣기 */
        Users user = new Users();
        user.setUser_email(ID);
        user.setUser_pw(PW);

        /** user object convert to JsonString using with Gson */
        Gson gson = new Gson();
        String login_info_json = gson.toJson(user);
        Log.d(TAG, "login_info_json: " + login_info_json);

        /** 서버 통신 - 자체 이메일 로그인 */
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
//                            Object obj = response.body().string();
//                            Log.d(TAG, "return type=" + obj.getClass().getName());

                    if(retrofit_result.equals("fail")) {
                        if(type.equals("login_with_typing")) {
                            myapp.logAndToast("로그인 정보가 올바르지 않습니다");
                        }
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

                        /** 현재는 구글 계정으로만 로그인했을 때 자동로그인을 활용하기 위해,
                         *  타이핑 로그인을 제외한 모든 로그인일 때 로그인 정보를 쉐어드에 저장하기로 한다 */
                        if(!type.equals("login_with_typing")) {
                            // 구글 로그인 정보, 'true'로 쉐어드에 저장
                            SharedPreferences Auto_login = getSharedPreferences("Auto_login", MODE_PRIVATE);
                            SharedPreferences.Editor Auto_login_edit = Auto_login.edit();
                            Auto_login_edit.putBoolean("google", true).apply();
                        }

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


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 자체 이메일로 로그인
     ---------------------------------------------------------------------------*/
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

        // 서버 통신 -- ID, PW를 서버에 전송하여, 로그인 시도하는 내부 메소드 호출
        login(email_str, pw_str, "login_with_typing");
    }


    /**---------------------------------------------------------------------------
     콜백메소드 ==> 구글 서비스 연결 실패 시, 호출되는 콜백메소드
     ---------------------------------------------------------------------------*/
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign_In)
        // will not be available.
        Log.d(TAG, "onConnectionFailed: " + connectionResult);
        myapp.logAndToast("Google Play Services error.");
    }


    // 쉐어드 초기화_ 테스트용
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


    /**---------------------------------------------------------------------------
     메소드 ==> 구글 로그인 진행 시 보여주는 프로그레스 다이얼로그
     ---------------------------------------------------------------------------*/
    public void showProgressDialog() {
        if(google_login_progressDialog == null) {
            google_login_progressDialog = new ProgressDialog(this);
            google_login_progressDialog.setMessage("loading...");
            google_login_progressDialog.setIndeterminate(true);
        }
        google_login_progressDialog.show();
    }


    /**---------------------------------------------------------------------------
     메소드 ==> '구글 로그인 진행 완료' or 'onStop' 때 프로그레스 다이얼로그를 해제하기 위한 메소드
     ---------------------------------------------------------------------------*/
    public void hideProgressDialog() {
        if(google_login_progressDialog !=null && google_login_progressDialog.isShowing()) {
            google_login_progressDialog.dismiss();
        }
    }


    /**---------------------------------------------------------------------------
     생명주기 ==> onStop
     ---------------------------------------------------------------------------*/
    @Override
    protected void onStop() {
        super.onStop();
        // 혹시 구글 로그인 관련하여 프로그레스바가 돌고 있으면 프로그레스바를 해제 한다
        hideProgressDialog();
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
