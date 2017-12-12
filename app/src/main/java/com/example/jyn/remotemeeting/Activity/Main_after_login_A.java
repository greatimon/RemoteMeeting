package com.example.jyn.remotemeeting.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.jyn.remotemeeting.Adapter.Main_viewpager_adapter;
import com.example.jyn.remotemeeting.DataClass.Chat_log;
import com.example.jyn.remotemeeting.DataClass.Chat_room;
import com.example.jyn.remotemeeting.DataClass.Users;
import com.example.jyn.remotemeeting.Dialog.Create_room_D;
import com.example.jyn.remotemeeting.Dialog.Enter_room_D;
import com.example.jyn.remotemeeting.Etc.Static;
import com.example.jyn.remotemeeting.Otto.BusProvider;
import com.example.jyn.remotemeeting.Otto.Event;
import com.example.jyn.remotemeeting.R;
import com.example.jyn.remotemeeting.Util.BackPressCloseHandler;
import com.example.jyn.remotemeeting.Util.Hangul;
import com.example.jyn.remotemeeting.Util.IsNetwork;
import com.example.jyn.remotemeeting.Util.Myapp;
import com.example.jyn.remotemeeting.Util.RetrofitService;
import com.example.jyn.remotemeeting.Util.ServiceGenerator;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.github.kimkevin.cachepot.CachePot;
import com.google.gson.Gson;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by JYN on 2017-11-10.
 */

public class Main_after_login_A extends AppCompatActivity implements TabLayout.OnTabSelectedListener {

    private static final String TAG = "all_"+Main_after_login_A.class.getSimpleName();
    String JSON_TAG_CHAT_ROOM_LIST = "chat_room_list";
    private static final int CONNECTION_REQUEST = 1;
    public static int REQUEST_SEARCH_PARTNER = 1318;
    public static int REQUEST_SHOW_PROFILE_DETAIL = 7894;
    public static int REQUEST_CHOOSE_METHOD_FOR_IMG = 2225;
    public static int REQUEST_TAKE_PICTURE = 7325;
    public static int REQUEST_GET_PHOTO_FROM_ALBUM = 7111;
    public static int REQUEST_CHOOSE_OR_NOT = 2367;
    public static int REQUEST_CREATE_ROOM = 1235;
    public static int REQUEST_ENTER_ROOM = 9862;
    public static int REQUEST_CHAT_ROOM = 8548;
    private SharedPreferences sharedPref;
    String JSON_TAG = "am_i_invited";
    Myapp myapp;

    private static boolean commandLineRun = false;

    Toolbar toolbar;
    TabLayout tablayout;
    ViewPager viewpager;
    Main_viewpager_adapter adapter;
//    FloatingActionButton fab;
    FloatingActionsMenu menuMultipleActions;
    View dark_back;
    int current_viewPager_pos = 0;
    boolean onCreate = true;
    File file;
    Handler handler;

    BackPressCloseHandler backPressCloseHandler;
    IsNetwork isNetwork;
    private int[] tabIcons = {
            R.drawable.project_act1,
            R.drawable.partner_act,
            R.drawable.chat_act,
            R.drawable.noti_act,
            R.drawable.profile_act,
    };
    private int[] tabIcons_non = {
            R.drawable.project_non1,
            R.drawable.partner_non,
            R.drawable.chat_non,
            R.drawable.noti_non,
            R.drawable.profile_non
    };

    PermissionListener permissionListener;


    /**---------------------------------------------------------------------------
     생명주기 ==> onCreate
     ---------------------------------------------------------------------------*/
    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "===================");
        Log.d(TAG, "onCreate");
        setContentView(R.layout.a_main_after_login);

        // otto 등록
        BusProvider.getBus().register(this);

        // 네트워크 확인 객체 생성
        isNetwork = new IsNetwork();

        // 어플리케이션 객체 생성
        myapp = Myapp.getInstance();

        // 뷰 찾기
        tablayout = findViewById(R.id.tab);
        viewpager = findViewById(R.id.viewpager);
//        fab = findViewById(R.id.fab);
        toolbar = findViewById(R.id.toolbar);
        dark_back = findViewById(R.id.dark_back);

        /** 플로팅 버튼 설정 */
        menuMultipleActions = findViewById(R.id.multiple_actions);
        // 플로팅 버튼 액션 리스너
        menuMultipleActions.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
            @Override
            public void onMenuExpanded() {
                dark_back.setVisibility(View.VISIBLE);
            }

            @Override
            public void onMenuCollapsed() {
                dark_back.setVisibility(View.GONE);
            }
        });

        // 핸들러 객체 메세지 리스너
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == 0) {

                }
            }
        };

        // 회의룸 생성 버튼
        final FloatingActionButton create_room = findViewById(R.id.create_room);
        create_room.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "회의룸 생성 버튼 클릭");
                handler.sendEmptyMessage(0);
                Intent intent = new Intent(view.getContext(), Create_room_D.class);
                startActivityForResult(intent, REQUEST_CREATE_ROOM);
            }
        });
        // 회의룸 입장 버튼
        final FloatingActionButton enter_room = findViewById(R.id.enter_room);
        enter_room.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "회의룸 입장 버튼 클릭");
                // 내가 초대된 회의가 있는지 없는지 확인하기
                RetrofitService rs = ServiceGenerator.createService(RetrofitService.class);
                Call<ResponseBody> call_result = rs.am_i_invited(
                        Static.AM_I_INVITED,
                        myapp.getUser_no());
                call_result.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            String retrofit_result = response.body().string();
                            Log.d(TAG, "retrofit_result: "+retrofit_result);

                            if(retrofit_result.equals("fail")) {
                                myapp.logAndToast("onResponse_fail" + response.errorBody().string());
                            }
                            else if(retrofit_result.equals("no_result")) {
                                myapp.logAndToast("초대된 회의가 없습니다");
                            }
                            else {
                                try {
                                    // jsonString --> jsonObject
                                    JSONObject jsonObject = new JSONObject(retrofit_result);
                                    // jsonObject --> jsonArray
                                    JSONArray jsonArray = jsonObject.getJSONArray(JSON_TAG);
                                    Log.d(TAG, "jsonArray 개수: " + jsonArray.length());

                                    // 일단 초대된 방은 1개로 (나중에 리스트뷰로 나타낼수도?)
                                    if(jsonArray.length() == 1) {
                                        JSONObject jsonObject_1 = jsonArray.getJSONObject(0);

                                        String meeting_no = jsonObject_1.getString("meeting_no");
                                        String real_meeting_title = jsonObject_1.getString("real_meeting_title");
                                        String meeting_creator_user_no = jsonObject_1.getString("meeting_creator_user_no");
                                        String creator_user_no = jsonObject_1.getString("user_no");
                                        String creator_email = jsonObject_1.getString("user_email");
                                        String creator_nickName = jsonObject_1.getString("user_nickName");
                                        String creator_img_fileName = jsonObject_1.getString("user_img_fileName");

                                        // 어플리케이션 객체에 회의 정보 저장해놓기
                                        myapp.setMeeting_no(meeting_no);
                                        myapp.setReal_meeting_title(real_meeting_title);
                                        myapp.setMeeting_creator_user_no(meeting_creator_user_no);
                                        myapp.setMeeting_subject_user_no(myapp.getUser_no());
                                        myapp.setMeeting_authority_user_no(meeting_creator_user_no);
                                        myapp.setProject_no("0");
                                        myapp.setMeeting_status("on");

                                        // Enter_room 다이얼로그 액티비티 열기
                                        Intent intent = new Intent(getBaseContext(), Enter_room_D.class);
                                        intent.putExtra("creator_user_no", creator_user_no);
                                        intent.putExtra("creator_email", creator_email);
                                        intent.putExtra("creator_nickName", creator_nickName);
                                        intent.putExtra("creator_img_fileName", creator_img_fileName);
                                        intent.putExtra("real_meeting_title", real_meeting_title);
                                        startActivityForResult(intent, REQUEST_ENTER_ROOM);
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

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
        });

        // 툴바 설정
        toolbar.setTitleTextColor(Color.parseColor("#f5f4f4")); //제목의 칼라
//        toolbar.setSubtitle(R.string.subtitle); //부제목 넣기
//        toolbar.setNavigationIcon(R.mipmap.ic_launcher); //제목앞에 아이콘 넣기
        setSupportActionBar(toolbar); //툴바를 액션바와 같게 만들어 준다.

        // 탭레이아웃, 뷰페이져
        adapter = new Main_viewpager_adapter(getSupportFragmentManager(), this);
        viewpager.setAdapter(adapter);
        tablayout.setupWithViewPager(viewpager);
        setupTabIcons();

        /**---------------------------------------------------------------------------
         리스너 ==> 탭 레이아웃, 클릭리스너
         ---------------------------------------------------------------------------*/
        tablayout.post(new Runnable() {
            @Override
            public void run() {
//                tablayout.setupWithViewPager(viewpager);
//                tablayout.setTabsFromPagerAdapter(adapter);
                tablayout.setOnTabSelectedListener(Main_after_login_A.this);
            }
        });

        // 뒤로 두번 누르면 종료되게
        backPressCloseHandler = new BackPressCloseHandler(this);

        // 퍼미션 리스너(테드_ 라이브러리)
        permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
//                Toast.makeText(a_profile.this, "권한 허가", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
//                Toast.makeText(a_profile.this, "권한 거부", Toast.LENGTH_SHORT).show();
            }
        };

        // 퍼미션 체크
        permission_check();

        // 검정 배경 GONE 처리
        dark_back.setVisibility(View.GONE);

//        /**---------------------------------------------------------------------------
//         액티비티 이동 ==> 영상 통화 걸기
//         ---------------------------------------------------------------------------*/
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d(TAG, "플로팅 버튼 클릭");
//
//                /**
//                 * 통화 연결
//                 * */
//                connectToRoom("", false, false, false, 0);
//
//            }
//        });


        /**---------------------------------------------------------------------------
         리스너 ==> 뷰페이져, 페이징 관련
         ---------------------------------------------------------------------------*/
        viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Log.d(TAG, "onPageScrolled position: " + position);
            }

            @Override
            public void onPageSelected(int position) {
                Log.d(TAG, "onPageSelected: " + position);
                setTitle(position);
                setupTabIcons_moving(position);
                current_viewPager_pos = position;

                // 인플레이팅되는 메뉴 재확인
                invalidateOptionsMenu();

                // 프로필 화면에 가면 플로팅 버튼 숨기기
                if(position == 4) {
                    if(menuMultipleActions.isExpanded()){
                        menuMultipleActions.collapseImmediately();
                    }
                    menuMultipleActions.setVisibility(View.GONE);
                }
                else {
                    menuMultipleActions.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

                if (state == ViewPager.SCROLL_STATE_DRAGGING) {
//                    Log.d(TAG, "onPageScrollStateChanged: SCROLL_STATE_DRAGGING");
                }
                if (state == ViewPager.SCROLL_STATE_SETTLING) {
//                    Log.d(TAG, "onPageScrollStateChanged: SCROLL_STATE_SETTLING");
                }
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    Log.d(TAG, "onPageScrollStateChanged: SCROLL_STATE_IDLE");
                }
            }
        });
    }


    /**---------------------------------------------------------------------------
     생명주기 ==> onResume
     ---------------------------------------------------------------------------*/
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "===================");
        Log.d(TAG, "onResume");

        if(!IsNetwork.available(this)) {
            myapp.logAndToast("인터넷이 연결되어 있지 않습니다");
        }
        else if(IsNetwork.available(this)) {
            if(onCreate) {
                setTitle(0);
                onCreate = false;
            }
        }

        if(menuMultipleActions.isExpanded()) {
            menuMultipleActions.collapseImmediately();
        }
    }


    /**---------------------------------------------------------------------------
     생명주기 ==> onDestroy
     ---------------------------------------------------------------------------*/
    @Override
    protected void onDestroy() {
        // otto 해제
        BusProvider.getBus().unregister(this);
        super.onDestroy();
        // 회의가 비정상적으로 종료됐을 때를 대비
//        if(!myapp.getMeeting_no().equals("")) {
//            // 메소드 호출
//            got_out_from_meeting();
//        }
    }


    /**---------------------------------------------------------------------------
     네비게이션 드로어 ==> 메뉴 아이템 인플레이트 - 메뉴생성 최초에만 호출되는 콜백메소드
    invalidateOptionsMenu() 호출시 콜백되는 메소드
     ---------------------------------------------------------------------------*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "=== onCreateOptionsMenu");
        if(current_viewPager_pos == 1) {
            Log.d(TAG, "search_menu_ inflated");
            getMenuInflater().inflate(R.menu.search_menu, menu);
            return true;
        }
        else {
            return true;
        }
    }


    /**---------------------------------------------------------------------------
     네비게이션 드로어 ==> 메뉴 아이템 인플레이트 -
     invalidateOptionsMenu() 호출시 콜백되는 메소드
     ---------------------------------------------------------------------------*/
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.d(TAG, "=== onPrepareOptionsMenu");
        return true;
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 툴바 아이콘들 클릭이벤트
     ---------------------------------------------------------------------------*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "=== onOptionsItemSelected");
        int id = item.getItemId();

        // 검색 아이콘 클릭했을 때
        if(id == R.id.action_search) {
             Intent intent = new Intent(this, Search_partner.class);
             startActivityForResult(intent, REQUEST_SEARCH_PARTNER);
        }

        return super.onOptionsItemSelected(item);
    }


    /**---------------------------------------------------------------------------
     메소드 ==> OnCreate 시, 뷰페이저 하단 탭, 아이콘 넣는 로직
     ---------------------------------------------------------------------------*/
    private void setupTabIcons() {
        tablayout.getTabAt(0).setIcon(tabIcons[0]);
        tablayout.getTabAt(1).setIcon(tabIcons_non[1]);
        tablayout.getTabAt(2).setIcon(tabIcons_non[2]);
        tablayout.getTabAt(3).setIcon(tabIcons_non[3]);
        tablayout.getTabAt(4).setIcon(tabIcons_non[4]);
    }

    /**---------------------------------------------------------------------------
     메소드 ==> 페이징 변경 시, 뷰페이저 하단 탭, 아이콘 넣는 로직
     ---------------------------------------------------------------------------*/
    private void setupTabIcons_moving(int position) {
        Log.d(TAG, "setupTabIcons_moving: " + position);

        tablayout.getTabAt(position).setIcon(tabIcons[position]);

        for(int i=0; i<Main_viewpager_adapter.PAGE_NUMBER; i++) {
//            Log.d(TAG, "setupTabIcons_moving_i 값: " + i);
            if(i != position) {
//                Log.d(TAG, "setupTabIcons_moving_for문: " + i);
                tablayout.getTabAt(i).setIcon(tabIcons_non[i]);
            }
        }
    }


    /**---------------------------------------------------------------------------
     클래스 ==> connectToRoom -- 영상통화 연결
     ---------------------------------------------------------------------------*/
    private void connectToRoom(String roomId, boolean commandLineRun, boolean loopback, boolean useValuesFromIntent, int runTimeMs) {
        Main_after_login_A.commandLineRun = commandLineRun;
        Log.d(TAG, roomId);

        /**
         * 인자값 intent 셋팅
         * */
        Intent intent = new Intent(this, Call_A.class);

        boolean videoCallEnabled = Static.VIDEO_CALL_ENABLED;
        boolean useScreencapture = Static.USE_SCREEN_CAPTURE;
        boolean useCamera2 = Static.USE_CAMERA_2;

        // Get video resolution from settings.
        int videoWidth = Static.DEFAULT_VIDEO_WIDTH;
        int videoHeight = Static.DEFAULT_VIDEO_HEIGHT;
//        if (useValuesFromIntent) {
//            videoWidth = getIntent().getIntExtra(Static.EXTRA_VIDEO_WIDTH, 0);
//            videoHeight = getIntent().getIntExtra(Static.EXTRA_VIDEO_HEIGHT, 0);
//        }
//        if (videoWidth == 0 && videoHeight == 0) {
//            String resolution =
//                    sharedPref.getString(keyprefResolution, getString(R.string.pref_resolution_default));
//            String[] dimensions = resolution.split("[ x]+");
//            if (dimensions.length == 2) {
//                try {
//                    videoWidth = Integer.parseInt(dimensions[0]);
//                    videoHeight = Integer.parseInt(dimensions[1]);
//                } catch (NumberFormatException e) {
//                    videoWidth = 0;
//                    videoHeight = 0;
//                    Log.e(TAG, "Wrong video resolution setting: " + resolution);
//                }
//            }
//        }

        // Get camera fps from settings.
        int cameraFps = Static.DEFAULT_CAMERA_FPS;
//        if (useValuesFromIntent) {
//            cameraFps = getIntent().getIntExtra(CallActivity.EXTRA_VIDEO_FPS, 0);
//        }
//        if (cameraFps == 0) {
//            String fps = sharedPref.getString(keyprefFps, getString(R.string.pref_fps_default));
//            String[] fpsValues = fps.split("[ x]+");
//            if (fpsValues.length == 2) {
//                try {
//                    cameraFps = Integer.parseInt(fpsValues[0]);
//                } catch (NumberFormatException e) {
//                    cameraFps = 0;
//                    Log.e(TAG, "Wrong camera fps setting: " + fps);
//                }
//            }
//        }

        // Get video and audio start bitrate.
        int videoStartBitrate = Static.VIDEO_START_BITRATE;
//        if (useValuesFromIntent) {
//            videoStartBitrate = getIntent().getIntExtra(CallActivity.EXTRA_VIDEO_BITRATE, 0);
//        }
//        if (videoStartBitrate == 0) {
//            String bitrateTypeDefault = getString(R.string.pref_maxvideobitrate_default);
//            String bitrateType = sharedPref.getString(keyprefVideoBitrateType, bitrateTypeDefault);
//            if (!bitrateType.equals(bitrateTypeDefault)) {
//                String bitrateValue = sharedPref.getString(
//                        keyprefVideoBitrateValue, getString(R.string.pref_maxvideobitratevalue_default));
//                videoStartBitrate = Integer.parseInt(bitrateValue);
//            }
//        }

        boolean captureQualitySlider = Static.CAPTURE_QUALITY_SLIDER;
        String videoCodec = Static.VIDEO_CODEC;
        String audioCodec = Static.AUDIO_CODEC;
        boolean hwCodec = Static.HW_CODEC;
        boolean captureToTexture = Static.CAPTURE_TO_TEXTURE;
        boolean flexfecEnabled = Static.FLEXFEC_ENABLED;
        boolean noAudioProcessing = Static.NO_AUDIO_PROCESSING;
        boolean aecDump = Static.AEC_DUMP;
        boolean useOpenSLES = Static.USE_OPENSLES;
        boolean disableBuiltInAEC = Static.DISABLE_BUILT_IN_AEC;
        boolean disableBuiltInAGC = Static.DISABLE_BUILT_IN_AGC;
        boolean disableBuiltInNS = Static.DISABLE_BUILT_IN_NS;
        boolean enableLevelControl = Static.ENABLE_LEVEL_CONTROL;
        boolean disableWebRtcAGCAndHPF = Static.DISABLE_WEBRTC_AGC_AND_HPE;

        int audioStartBitrate = Static.AUDIO_START_BITRATE;
//        if (useValuesFromIntent) {
//            audioStartBitrate = getIntent().getIntExtra(Static.EXTRA_AUDIO_BITRATE, 0);
//        }
//        if (audioStartBitrate == 0) {
//            String bitrateTypeDefault = getString(R.string.pref_startaudiobitrate_default);
//            String bitrateType = sharedPref.getString(keyprefAudioBitrateType, bitrateTypeDefault);
//            if (!bitrateType.equals(bitrateTypeDefault)) {
//                String bitrateValue = sharedPref.getString(
//                        keyprefAudioBitrateValue, getString(R.string.pref_startaudiobitratevalue_default));
//                audioStartBitrate = Integer.parseInt(bitrateValue);
//            }
//        }

        boolean displayHud = Static.DISPLAY_HUD;
        boolean tracing = Static.TRACING;
        boolean dataChannelEnabled = Static.DATA_CHANNEL_ENABLED;
        boolean ordered = Static.ORDERED;
        int maxRetrMs = Static.MAX_RETR_MS;
        int maxRetr = Static.MAX_RETR;
        String protocol = Static.PROTOCOL;
        boolean negotiated = Static.NEGOTIATED;
        int id = Static.ID;


        
        /**
         * 인자값 intent 넣기
         * */
        String roomUrl = Static.WEBRTC_URL;
        Log.d(TAG, roomUrl);
        Uri uri = Uri.parse(roomUrl);

        intent.setData(uri);
        intent.putExtra(Static.EXTRA_ROOMID, roomId);
        intent.putExtra(Static.EXTRA_LOOPBACK, loopback);
        intent.putExtra(Static.EXTRA_VIDEO_CALL, videoCallEnabled);
        intent.putExtra(Static.EXTRA_SCREENCAPTURE, useScreencapture);
        intent.putExtra(Static.EXTRA_CAMERA2, useCamera2);
        intent.putExtra(Static.EXTRA_VIDEO_WIDTH, videoWidth);
        intent.putExtra(Static.EXTRA_VIDEO_HEIGHT, videoHeight);
        intent.putExtra(Static.EXTRA_VIDEO_FPS, cameraFps);
        intent.putExtra(Static.EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED, captureQualitySlider);
        intent.putExtra(Static.EXTRA_VIDEO_BITRATE, videoStartBitrate);
        intent.putExtra(Static.EXTRA_VIDEOCODEC, videoCodec);
        intent.putExtra(Static.EXTRA_HWCODEC_ENABLED, hwCodec);
        intent.putExtra(Static.EXTRA_CAPTURETOTEXTURE_ENABLED, captureToTexture);
        intent.putExtra(Static.EXTRA_FLEXFEC_ENABLED, flexfecEnabled);
        intent.putExtra(Static.EXTRA_NOAUDIOPROCESSING_ENABLED, noAudioProcessing);
        intent.putExtra(Static.EXTRA_AECDUMP_ENABLED, aecDump);
        intent.putExtra(Static.EXTRA_OPENSLES_ENABLED, useOpenSLES);
        intent.putExtra(Static.EXTRA_DISABLE_BUILT_IN_AEC, disableBuiltInAEC);
        intent.putExtra(Static.EXTRA_DISABLE_BUILT_IN_AGC, disableBuiltInAGC);
        intent.putExtra(Static.EXTRA_DISABLE_BUILT_IN_NS, disableBuiltInNS);
        intent.putExtra(Static.EXTRA_ENABLE_LEVEL_CONTROL, enableLevelControl);
        intent.putExtra(Static.EXTRA_DISABLE_WEBRTC_AGC_AND_HPF, disableWebRtcAGCAndHPF);
        intent.putExtra(Static.EXTRA_AUDIO_BITRATE, audioStartBitrate);
        intent.putExtra(Static.EXTRA_AUDIOCODEC, audioCodec);
        intent.putExtra(Static.EXTRA_DISPLAY_HUD, displayHud);
        intent.putExtra(Static.EXTRA_TRACING, tracing);
        intent.putExtra(Static.EXTRA_CMDLINE, commandLineRun);
        intent.putExtra(Static.EXTRA_RUNTIME, runTimeMs);

        intent.putExtra(Static.EXTRA_DATA_CHANNEL_ENABLED, dataChannelEnabled);


        if (dataChannelEnabled) {
            intent.putExtra(Static.EXTRA_ORDERED, ordered);
            intent.putExtra(Static.EXTRA_MAX_RETRANSMITS_MS, maxRetrMs);
            intent.putExtra(Static.EXTRA_MAX_RETRANSMITS, maxRetr);
            intent.putExtra(Static.EXTRA_PROTOCOL, protocol);
            intent.putExtra(Static.EXTRA_NEGOTIATED, negotiated);
            intent.putExtra(Static.EXTRA_ID, id);
        }

        if (useValuesFromIntent) {
            if (getIntent().hasExtra(Static.EXTRA_VIDEO_FILE_AS_CAMERA)) {
                String videoFileAsCamera =
                        getIntent().getStringExtra(Static.EXTRA_VIDEO_FILE_AS_CAMERA);
                intent.putExtra(Static.EXTRA_VIDEO_FILE_AS_CAMERA, videoFileAsCamera);
            }

            if (getIntent().hasExtra(Static.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE)) {
                String saveRemoteVideoToFile =
                        getIntent().getStringExtra(Static.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE);
                intent.putExtra(Static.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE, saveRemoteVideoToFile);
            }

            if (getIntent().hasExtra(Static.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH)) {
                int videoOutWidth =
                        getIntent().getIntExtra(Static.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH, 0);
                intent.putExtra(Static.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH, videoOutWidth);
            }

            if (getIntent().hasExtra(Static.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT)) {
                int videoOutHeight =
                        getIntent().getIntExtra(Static.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT, 0);
                intent.putExtra(Static.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT, videoOutHeight);
            }
        }
        
        startActivityForResult(intent, CONNECTION_REQUEST);
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 퍼미션 체크
     ---------------------------------------------------------------------------*/
    public void permission_check() {
        // 퍼미션 확인(테드_ 라이브러리)
        new TedPermission(this)
                .setPermissionListener(permissionListener)
//                .setRationaleMessage("다음 작업을 허용하시겠습니까? 기기 사진, 미디어, 파일 액세스")
                .setDeniedMessage("[설정] > [권한] 에서 권한을 허용할 수 있습니다")
                .setGotoSettingButton(true)
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.CAMERA,
                        Manifest.permission.MODIFY_AUDIO_SETTINGS)
                .check();
    }


    /**---------------------------------------------------------------------------
     콜백메소드 ==> 탭레이아웃의 탭 클릭관련 콜백
     ---------------------------------------------------------------------------*/
    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        Log.d(TAG, "TabSelected: " + tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        Log.d(TAG, "TabUnselected: " + tab.getPosition());
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        Log.d(TAG, "TabReselected: " + tab.getPosition());
        if(tab.getPosition() == 0) {
            setTitle(tab.getPosition());
        }
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 툴바 텍스트 변경
     ---------------------------------------------------------------------------*/
    public void setTitle(int positon) {
        switch(positon) {
            case 0:
                toolbar.setTitle("프로젝트");
                break;
            case 1:
                toolbar.setTitle("비지니스 파트너");
                break;
            case 2:
                toolbar.setTitle("채팅");
                break;
            case 3:
                toolbar.setTitle("알림");
                break;
            case 4:
                toolbar.setTitle("프로필");
                break;
        }
    }


    /**---------------------------------------------------------------------------
     오버라이드 ==> 소프트 키보드 백버튼 -- 두번 누르면 앱닫기
     ---------------------------------------------------------------------------*/
    @Override
    public void onBackPressed() {
        if(menuMultipleActions.isExpanded()) {
            menuMultipleActions.collapse();
        }
        else {
            backPressCloseHandler.onBackPressed();
        }
    }

    /**---------------------------------------------------------------------------
     메소드 ==> 프로필 상세보기에서 1:1 채팅 버튼을 눌렀을 때,
               서버로부터 방 정보에 대한 jsonString을 받아서 Chat_room 객체 형식에 맞게 변환하고
               CachePot 이용 해서 Chat_room 객체를 전달한 뒤, 채팅방 액티비티로 이동
     ---------------------------------------------------------------------------*/
    public void form_to_chat_room_ob(String result_jsonString) {
        try {
            // jsonString --> jsonObject
            JSONObject jsonObject = new JSONObject(result_jsonString);
            // jsonObject --> jsonArray
            JSONArray jsonArray = jsonObject.getJSONArray(JSON_TAG_CHAT_ROOM_LIST);
            Log.d(TAG, "jsonArray 개수: " + jsonArray.length());

            String temp = jsonArray.getJSONObject(0).toString();
            Log.d(TAG, "jsonString: " + temp);

            // Chat_room 객체안의 세부 ArrayList 객체들 생성
            ArrayList<String> user_nickname_arr = new ArrayList<>();
            ArrayList<String> user_img_filename_arr = new ArrayList<>();

            // 1. 채팅방 번호
            int chatroom_no = jsonArray.getJSONObject(0).getInt("chatroom_no");
            // 채팅방 방장 번호
            int chat_room_authority_user_no = jsonArray.getJSONObject(0).getInt("chat_room_authority_user_no");
            // 2. 채팅방 제목
            String chat_room_title = jsonArray.getJSONObject(0).getString("chat_room_title");

            Log.d(TAG, "chatroom_no: " + chatroom_no);
            Log.d(TAG, "chat_room_authority_user_no: " + chat_room_authority_user_no);
            Log.d(TAG, "chat_room_title: " + chat_room_title);

            // 데이터 클래스로 파싱하기 위한 GSON 객체 생성
            Gson gson = new Gson();

            // 3. user 정보를 가지고 있는 JsonString을 가져와서 gson을 이용해서 user 객체로 변환
            String temp1 =  jsonArray.getJSONObject(0).getString("user_ob");
            Users user = gson.fromJson(temp1, Users.class);
            Log.d(TAG, "user.getUser_nickname(): " + user.getUser_nickname());
            Log.d(TAG, "user.getUser_img_filename(): " + user.getUser_img_filename());

            // 변환한 user 객체에서 닉네임과 이미지 URL 값을 가져와서 해당 ArrayList 에 add.
            user_nickname_arr.add(user.getUser_nickname());
            user_img_filename_arr.add(user.getUser_img_filename());

            /** Chat_room 객체에 데이터 넣기 */
            Chat_room room = new Chat_room();
            room.setChatroom_no(chatroom_no);
            room.setUser_nickname_arr(user_nickname_arr);
            room.setUser_img_filename_arr(user_img_filename_arr);
            room.setChat_room_title(chat_room_title);

            // TODO: 채팅방 액티비티로 이동
            // CachePot 이용해서 클릭한 rooms 객체 전달
            CachePot.getInstance().push("chat_room", room);

            // Chat_A 액티비티(채팅방) 열기
            // 상대방 프로필로부터 채팅방을 여는 것임을 intent 값으로 알린다
            Intent intent = new Intent(getBaseContext(), Chat_A.class);
            intent.putExtra("from", "profile");
            startActivityForResult(intent, REQUEST_CHAT_ROOM);

        } catch (JSONException e) {
            e.printStackTrace();
        }



    }


    /**---------------------------------------------------------------------------
     오버라이드 ==> onActivityResult
     ---------------------------------------------------------------------------*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 채팅방 액티비티에서 돌아왔을 때
        if(requestCode==REQUEST_CHAT_ROOM) {
            viewpager.setCurrentItem(2);
            /** otto 를 통해, Chat_F로 서버로부터 데이터를 다시 받아 채팅방 리스트를 갱신하라는 이벤트 전달하기 */
            Event.Main_after_login_A__Chat_F event = new Event.Main_after_login_A__Chat_F("activate_RCV");
            BusProvider.getBus().post(event);
            Log.d(TAG, "otto 전달_ onActivityResult");
        }

        // 파트너 검색 액티비티에서 돌아왔을 때
        if(requestCode==REQUEST_SEARCH_PARTNER) {
            viewpager.setCurrentItem(1);
            /** otto 를 통해, 프래그먼트로 이벤트 전달하기 */
            Event.Main_after_login_A__Partner_F event = new Event.Main_after_login_A__Partner_F("activate_RCV");
            BusProvider.getBus().post(event);
            Log.d(TAG, "otto 전달_ onActivityResult");
        }

        // 프로필 상세보기 액티비티에서 돌아왔을 때
        else if(requestCode==REQUEST_SHOW_PROFILE_DETAIL && requestCode==RESULT_CANCELED) {
            /** otto 를 통해, 프래그먼트로 이벤트 전달하기 */
            Event.Main_after_login_A__Partner_F event = new Event.Main_after_login_A__Partner_F("activate_RCV");
            BusProvider.getBus().post(event);

        }
        // 프로필 상세보기에서, 1:1 채팅을 눌렀을 때
        else if(requestCode==REQUEST_SHOW_PROFILE_DETAIL && resultCode==RESULT_OK) {
            String target_user_no = data.getStringExtra("target_user_no");
            if(target_user_no != null) {
                /** 서버 통신 - 1:1 채팅방 생성 */
                RetrofitService rs = ServiceGenerator.createService(RetrofitService.class);
                Call<ResponseBody> call_result = rs.create_chat_room_for_one(
                        Static.CREATE_CHAT_ROOM_FOR_ONE,
                        myapp.getUser_no(), target_user_no);
                call_result.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            String retrofit_result = response.body().string();
                            Log.d(TAG, "1:1 채팅방 생성 관련 retrofit_result: "+retrofit_result);

                            // 오류
                            if(retrofit_result.equals("fail")) {
                                Log.d(TAG, "retrofit_result_ 1:1 채팅방 생성 fail" + retrofit_result);
                            }

                            // 레트로핏 결과가 'fail'이 아니라면,
                            else if(!retrofit_result.equals("fail")) {
                                // SPLIT 상수를 포함하고 있다면, 채팅방이 이미 있는 것임
                                if(retrofit_result.contains(Static.SPLIT)) {
                                    String[] temp = retrofit_result.split(Static.SPLIT);
                                    // 이미 채팅방이 존재할 때
                                    if(temp[0].equals("overlap")) {
                                        Log.d(TAG, "retrofit_result_ 이미 이 사람과의 채팅방 존재함!!");
                                        Log.d(TAG, "temp[1]: " + temp[1]);

                                        // jsonString을 Chat_room 객체 형식으로 바꾸는 메소드 호출
                                        // 그리고, 채팅방 액티비티로 이동함
                                        form_to_chat_room_ob(temp[1]);
                                    }

                                }
                                // SPLIT 상수를 포함하고 있지 않다면, 채팅방을 생성한 것임
                                else {
                                    Log.d(TAG, "retrofit_result: " + retrofit_result);
                                    // jsonString을 Chat_room 객체 형식으로 바꾸는 메소드 호출
                                    // 그리고, 채팅방 액티비티로 이동함
                                    form_to_chat_room_ob(retrofit_result);
                                }
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

        // 이미지 선택 방법 다이얼로그에서 돌아왔을 때
        else if(requestCode==REQUEST_CHOOSE_METHOD_FOR_IMG && resultCode==RESULT_OK) {
            String method = data.getStringExtra("method");
            Log.d(TAG ,"method: " + method);

            // 파일 객체 생성
            file = createFile();

            // 사진촬영 선택
            if(method.equals("camera")) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file)); //어떤 파일에 저장할 것인지를 설정
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, REQUEST_TAKE_PICTURE);
                }
            }
            // 앨범 선택
            else if(method.equals("album")) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, REQUEST_GET_PHOTO_FROM_ALBUM);
                }
            }
        }
        // 카메라 촬영하고 돌아왔을 때
        else if(requestCode==REQUEST_TAKE_PICTURE && resultCode==RESULT_OK) {
            Log.d(TAG, "getAbsolutePath: " + file.getAbsolutePath());

            // BroadCast 이용하여 미디어 스캐닝
            Intent media_scan_intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            media_scan_intent.setData(Uri.fromFile(file));
            sendBroadcast(media_scan_intent);

            // 0.2초 뒤에 실행 - onResume 실행될 시간 벌어주기
            new Handler().postDelayed(new Runnable() {
                @Override public void run() {
                    Intent intent = new Intent(getBaseContext(), Preview_img_A.class);
                    intent.putExtra("absolutePath",file.getAbsolutePath());
                    startActivityForResult(intent, REQUEST_CHOOSE_OR_NOT);
                }
            }, 200);

        }
        // 앨범에서 사진을 선택하고 돌아왔을 때
        else if(requestCode==REQUEST_GET_PHOTO_FROM_ALBUM && resultCode== RESULT_OK) {

            // 이미지의 절대경로 취득
            final String absolutePath = getPath(data.getData());
            Log.d(TAG, "absolutePath: " + absolutePath);

            // 0.2초 뒤에 실행 - onResume 실행될 시간 벌어주기
            new Handler().postDelayed(new Runnable() {
                @Override public void run() {
                    Intent intent = new Intent(getBaseContext(), Preview_img_A.class);
                    intent.putExtra("absolutePath", absolutePath);
                    startActivityForResult(intent, REQUEST_CHOOSE_OR_NOT);
                }
            }, 200);
        }
        // 최종적으로 이 사진을 선택한다고 했을 때
        else if(requestCode==REQUEST_CHOOSE_OR_NOT && resultCode==RESULT_OK) {
            String absolutePath = data.getStringExtra("absolutePath");
            Log.d(TAG, "absolutePath: " + absolutePath);

            /** otto 를 통해, Profile_F로 이미지 셋팅 이벤트 전달하기 */
            Event.Main_after_login_A__Profile_F event = new Event.Main_after_login_A__Profile_F("image", absolutePath);
            BusProvider.getBus().post(event);
        }

        // 방 생성한다고 했을 때
        else if(requestCode==REQUEST_CREATE_ROOM && resultCode==RESULT_OK) {
            final String subject_user_no = data.getStringExtra("subject_user_no");
            final String input_title = data.getStringExtra("input_title");
            final String convert_str = data.getStringExtra("convert_str");
            Log.d(TAG, "subject_user_no: " + subject_user_no);
            Log.d(TAG, "input_title: " + input_title);
            Log.d(TAG, "convert_str: " + convert_str);

            // 0.2초 뒤에 실행 - onResume 실행될 시간 벌어주기
            /** 서버 통신 -- 방 생성 정보 전달 + 영상통화 시작 */
            new Handler().postDelayed(new Runnable() {
                @Override public void run() {
                    RetrofitService rs = ServiceGenerator.createService(RetrofitService.class);
                    Call<ResponseBody> call_result = rs.create_meeting_room(
                            Static.CREATE_MEETING_ROOM,
                            input_title, convert_str, myapp.getUser_no(), subject_user_no);
                    call_result.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            try {
                                String retrofit_result = response.body().string();
                                Log.d(TAG, "create_room_result: "+retrofit_result);

                                if(retrofit_result.equals("fail")) {
                                    myapp.logAndToast("onResponse_fail" + response.errorBody().string());
                                }

                                else {
                                    String[] temp = retrofit_result.split(Static.SPLIT);

                                    // 어플리케이션 객체에 회의 정보 저장해놓기
                                    myapp.setMeeting_no(temp[1]);
                                    myapp.setReal_meeting_title(input_title);
                                    myapp.setMeeting_creator_user_no(myapp.getUser_no());
                                    myapp.setMeeting_subject_user_no(subject_user_no);
                                    myapp.setMeeting_authority_user_no(myapp.getUser_no());
                                    myapp.setProject_no("0");
                                    myapp.setMeeting_status("on");

                                    Log.d(TAG, "setMeeting_no: " + temp[1]);
                                    Log.d(TAG, "setReal_meeting_title: " + input_title);
                                    Log.d(TAG, "setMeeting_creator_user_no: " + myapp.getUser_no());
                                    Log.d(TAG, "setMeeting_subject_user_no: " + subject_user_no);
                                    Log.d(TAG, "setMeeting_authority_user_no: " + myapp.getUser_no());

                                    /**
                                     * 통화 연결
                                     * */
                                    connectToRoom(convert_str, false, false, false, 0);
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
            }, 200);
        }

        // 회의 입장한다고 했을 때
        else if(requestCode==REQUEST_ENTER_ROOM && resultCode==RESULT_OK) {
            String real_meeting_title = data.getStringExtra("real_meeting_title");
            Log.d(TAG, "real_meeting_title: " + real_meeting_title);

            String convert = Hangul.convert(real_meeting_title);

            /**
             * 통화 연결
             * */
            connectToRoom(convert, false, false, false, 0);
        }

        // 회의하고 돌아왔을 때
        else if(requestCode==CONNECTION_REQUEST) {
//            // 메소드 호출
//            got_out_from_meeting();
        }

    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 이미지 파일 생성 -- 카메라 사진촬영을 위한
     ---------------------------------------------------------------------------*/
    public File createFile() {
        // 디렉토리가 존재하지 않으면 디렉토리 생성
        String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/RemoteMeeting";
        File folder = new File(sdPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        // 현재 시간 기준으로 파일명 생성
        String now = myapp.now();
        String imageFileName = now + ".jpg";
        Log.d(TAG, "imageFileName: " + imageFileName);

        // 파일 객체 생성
        File curFile = new File(sdPath, imageFileName);
        // 파일 객체 리턴
        return curFile;
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 선택된 Uri의 사진 `절대경로`를 가져온다
     ---------------------------------------------------------------------------*/
    public String getPath(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};

        CursorLoader cursorLoader = new CursorLoader(this, contentUri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();

        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }






















}
