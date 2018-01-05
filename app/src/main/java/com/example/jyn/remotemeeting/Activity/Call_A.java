package com.example.jyn.remotemeeting.Activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.example.jyn.remotemeeting.Adapter.RCV_selectFile_preview_adapter;
import com.example.jyn.remotemeeting.DataClass.Data_for_netty;
import com.example.jyn.remotemeeting.Dialog.Confirm_img_share_mode_accept_D;
import com.example.jyn.remotemeeting.Dialog.Out_confirm_D;
import com.example.jyn.remotemeeting.Etc.Static;
import com.example.jyn.remotemeeting.Fragment.Call_F;
import com.example.jyn.remotemeeting.Fragment.Hud_F;
import com.example.jyn.remotemeeting.Otto.BusProvider;
import com.example.jyn.remotemeeting.Otto.Event;
import com.example.jyn.remotemeeting.R;
import com.example.jyn.remotemeeting.Realm_and_drawing.DrawPath;
import com.example.jyn.remotemeeting.Realm_and_drawing.DrawPoint;
import com.example.jyn.remotemeeting.Util.Myapp;
import com.example.jyn.remotemeeting.Util.RetrofitService;
import com.example.jyn.remotemeeting.Util.ServiceGenerator;
import com.example.jyn.remotemeeting.Util.UnhandledExceptionHandler;
import com.example.jyn.remotemeeting.WebRTC.AppRTCAudioManager;
import com.example.jyn.remotemeeting.WebRTC.AppRTCAudioManager.AudioDevice;
import com.example.jyn.remotemeeting.WebRTC.AppRTCClient;
import com.example.jyn.remotemeeting.WebRTC.DirectRTCClient;
import com.example.jyn.remotemeeting.WebRTC.PeerConnectionClient;
import com.example.jyn.remotemeeting.WebRTC.WebSocketRTCClient;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.squareup.otto.Subscribe;

import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.EglBase;
import org.webrtc.FileVideoCapturer;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon;
import org.webrtc.ScreenCapturerAndroid;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoFileRenderer;
import org.webrtc.VideoRenderer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.ErrorCode;
import io.realm.ObjectServerError;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.SyncConfiguration;
import io.realm.SyncCredentials;
import io.realm.SyncUser;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by JYN on 2017-11-10.
 */

/**
 * Activity for peer connection call setup, call waiting and call view.
 * 피어 연결 통화 설정, 통화 대기 및 통화보기 활동
 */
public class Call_A extends Activity implements AppRTCClient.SignalingEvents,               // webRTC
                                                PeerConnectionClient.PeerConnectionEvents,  // webRTC
                                                Call_F.OnCallEvents,                        // webRTC
                                                SurfaceHolder.Callback,                     // Drawing
                                                View.OnClickListener {                      // Drawing

    private static final String TAG = "all_" + Call_A.class.getSimpleName();
    private static final int CAPTURE_PERMISSION_REQUEST_CODE = 1;
    public static int REQUEST_CONFIRM_UPLOAD_FILES = 1220;
    public static int REQUEST_CONFIRM_IMAGE_SHARE_MODE = 1899;
    Myapp myapp;

    // Peer connection statistics callback period in ms.
    // 피어 연결 통계 콜백 기간 (밀리 초).
    private static final int STAT_CALLBACK_PERIOD = 1000;

    private class ProxyRenderer implements VideoRenderer.Callbacks {
        private VideoRenderer.Callbacks target;

        synchronized public void renderFrame(VideoRenderer.I420Frame frame) {
            if (target == null) {
                Logging.d(TAG, "Dropping frame in proxy because target is null.");
                // 대상이 null 이기 때문에 프락시에 프레임을 놓습니다.
                VideoRenderer.renderFrameDone(frame);
                return;
            }

            target.renderFrame(frame);
        }

        synchronized public void setTarget(VideoRenderer.Callbacks target) {
            this.target = target;
        }
    }

    private final ProxyRenderer remoteProxyRenderer = new ProxyRenderer();
    private final ProxyRenderer localProxyRenderer = new ProxyRenderer();
    private PeerConnectionClient peerConnectionClient = null;
    private AppRTCClient appRtcClient;
    private AppRTCClient.SignalingParameters signalingParameters;
    private AppRTCAudioManager audioManager = null;
    private EglBase rootEglBase;
    private SurfaceViewRenderer pipRenderer;
    private SurfaceViewRenderer fullscreenRenderer;
    private VideoFileRenderer videoFileRenderer;
    private final List<VideoRenderer.Callbacks> remoteRenderers =
            new ArrayList<VideoRenderer.Callbacks>();
    private Toast logToast;
    private boolean commandLineRun;
    private int runTimeMs;
    private boolean activityRunning;
    private AppRTCClient.RoomConnectionParameters roomConnectionParameters;
    private PeerConnectionClient.PeerConnectionParameters peerConnectionParameters;
    private boolean iceConnected;
    private boolean isError;
    private boolean callControlFragmentVisible = true;
    private long callStartedTimeMs = 0;
    private boolean micEnabled = true;
    private boolean videoEnabled = true;
    private boolean screencaptureEnabled = false;
    private static Intent mediaProjectionPermissionResultData;
    private static int mediaProjectionPermissionResultCode;
    public static ArrayList<String> share_img = new ArrayList<>();

    // True if local view is in the fullscreen renderer.
    // 로컬 뷰가 풀 스크린 렌더러에있는 경우는 true
    private boolean isSwappedFeeds;

    // Controls
    private Call_F call_f;
    private Hud_F hud_f;
//    private CpuMonitor cpuMonitor;

    final int REQUEST_OUT = 1000;
    public final static int REQUEST_GET_LOCAL_FILE = 1001;
    public static Handler hangup_confirm;

    Chronometer timeElapsed;
    Handler time_handler;
    RelativeLayout video_off_backup_REL;
    ImageView back_img;
    CircleImageView profile_img;
    RelativeLayout video_off_backup_REL_full;
    ImageView back_img_full;
    CircleImageView profile_img_full;

    // 서버로 부터 받은 상대방의 비디오 on/off 상태를 Chat_handler로부터 전달받는 핸들러 객체 생성
    public static Handler webrtc_message_handler;

    /** 버터나이프 */
    public Unbinder unbinder;
    @BindView(R.id.image_share_REL)         public RelativeLayout image_share_REL;
    @BindView(R.id.recyclerView_share_image)public RecyclerView recyclerView_share_image;
    @BindView(R.id.surface_view)            public SurfaceView surfaceView;
    @BindView(R.id.enable_drag_btn)         public Button enable_drag_btn;


    // Realm and Drawing 관련 변수 ==============
    // =========================================
    private volatile Realm realm;
    // realm 서버로 부터 받아온 DrawPath
    RealmResults<DrawPath> results_main;
    // realm 서버로 부터 받아온 DrawPath 중 마지막 drawPath 의 인덱스 번호
    int last_realmResult_index = -1;
    // 각 경우에 따라, draw()를 호출할 핸들러
    Handler call_draw_handler;
    // 서피스뷰를 감싸고 있는 뷰의 크기
    int container_width;
    int container_height;
    // 서피스뷰의 크기
    int surfaceView_width;
    int surfaceView_height;
    // 서피스뷰 배경에 깔릴 문서(비트맵) - original
    Bitmap document_bitmap;
    // newWidth, newHight로 생성된 서피스뷰 배경에 깔릴 문서(비트맵) - custom
    Bitmap scaled;
    // 서피스뷰 배경에 깔릴 문서의 새로운 width, height
    int newWidth;
    int newHeight;
    // 비트맵을 서피스뷰에 그릴 때, 가로|세로 중 꽉차는 쪽 저장하기 위한 변수
    boolean surfaceView_fill_width;
    // realm 서버의 DrawPath 테이블이 변경됨을 감지하는 쓰레드
    private DrawThread drawThread;
    // draw 관련 비율 조정하기 위한 상수
    int EDGE_WIDTH = 683;
    // EDGE_WIDTH 에 따른 비율
    private double ratio = -1;
    // 서피스뷰의 좌표 정보를 담은 사각형
    Rect surfaceView_rect;
    // 서피스뷰 배경에 깔리는 문서(비트맵)에서 crop할 좌표 정보를 담은 사각형
    Rect crop_img_rect;
    // 서피스뷰 배경에 깔릴 문서(비트맵)을 드래그 해서 이동 시킬 때 사용 하는 Bitmap, Canvas 객체
    Bitmap bitmap_for_drag;
    Canvas canvas_for_drag;
    // 선을 그릴 때 사용자에게 보여줄 비트맵을 그릴 캔버스
    Canvas canvas_main;
    // drag가 가능한 상태인지 확인하는 변수
    boolean enable_drag;
    // 현재 drag 중인지 확인하는 변수
    boolean on_moving;
    // 현재 서피스뷰가 배경으로 있는 문서(비트맵)의 최상단점 Y값(0)을 기준으로 밑으로 얼마만큼의 Y만큼
    // 위치해 있는지, 그 Int 값을 임시로 저장하기 위한 변수
    int temp_save_top_y;
    // 현재 그리고 있는 점, 선에 대한 정보를 담는 객체, realm 서버로 전달할 객체이기도 함
    private DrawPath currentPath;
    // 선 드로잉 작업 중, action_down 했을 때 x, y 좌표값
    float saveX = 0;
    float saveY = 0;
    // 선 드로잉 작업 중, action_move 할 때 x, y 좌표값
    float moveX, moveY;
    // 선 드로잉 작업 중, action_move 할 때 x, y 좌표값과 saveX, saveY 좌표값의 차이값(움직인 거리)
    float diffX, diffY;
    // 선 드로잉 작업 중, action_up 할 때, top_y(int) 값
    int last_top_y_when_action_up;
    // 현재 서피스뷰가 배경으로 있는 문서(비트맵)의 최상단점 Y값(0)을 기준으로 밑으로 얼마만큼의 Y만큼
    // 위치해 있는지, 그 값을 저장하는 변수
    int top_y;
    // top_y 값에 따라 서피스뷰의 height 만큼 더한 값
    int bottom_y;
    // 디폴트 컬러 이름
    private String currentColor = "Charcoal";
    // 컬러 인트값과, 컬러 스트링 값을 담는 해쉬맵
    private HashMap<String, Integer> nameToColorMap;
    private HashMap<Integer, String> colorIdToName;
    // 그리는 drawing 선의 두께
    int stroke = 6;
    // 그리는 drawing 선의 투명도
    int alpha = 255;



    /**---------------------------------------------------------------------------
     생명주기 ==> onCreate
     ---------------------------------------------------------------------------*/
    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new UnhandledExceptionHandler(this));

        // Set window styles for fullscreen-window size. Needs to be done before adding content.
        // 전체 화면 크기의 창 스타일을 설정합니다. 콘텐츠를 추가하기 전에 완료해야합니다.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(getSystemUiVisibility());
        setContentView(R.layout.a_call);

        // 버터나이프 바인드
        unbinder = ButterKnife.bind(this);

        // 어플리케이션 객체 생성
        myapp = Myapp.getInstance();

        // 커스텀 로거 생성
        Logger.clearLogAdapters();
        Logger.addLogAdapter(new AndroidLogAdapter(myapp.custom_log(RCV_selectFile_preview_adapter.class)));

        iceConnected = false;
        signalingParameters = null;

        // Create UI controls.
        pipRenderer = (SurfaceViewRenderer) findViewById(R.id.pip_video_view);
        fullscreenRenderer = (SurfaceViewRenderer) findViewById(R.id.fullscreen_video_view);
        timeElapsed = (Chronometer) findViewById(R.id.chronometer);
        video_off_backup_REL = findViewById(R.id.video_off_backup_REL);
        back_img = findViewById(R.id.back_img);
        profile_img = findViewById(R.id.profile_img);
        video_off_backup_REL_full = findViewById(R.id.video_off_backup_REL_full);
        back_img_full = findViewById(R.id.back_img_full);
        profile_img_full = findViewById(R.id.profile_img_full);


        call_f = new Call_F();
        hud_f = new Hud_F();

        // Show/hide call control fragment on view click.
        /** 보기 클릭시 통화 제어 fragment 표시 / 숨기기 */
//        View.OnClickListener listener = new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Log.d(TAG, "화면 클릭");
////                toggleCallControlFragmentVisibility();
//            }
//        };

        // Swap feeds on pip view click.
        /** 클릭 시, 뷰 스왑 */
        Log.d(TAG, "1_pipRenderer.isClickable(): " + pipRenderer.isClickable());
//        pipRenderer.setEnabled(false);
        pipRenderer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "pip 화면 클릭");
                if(pipRenderer.isEnabled()) {
                    Log.d(TAG, "pip is Enabled");
//                    setSwappedFeeds(!isSwappedFeeds);
                }
                else if(!pipRenderer.isEnabled()) {
                    Log.d(TAG, "pip is not Enabled");
                }
            }
        });

//        fullscreenRenderer.setOnClickListener(listener);
        remoteRenderers.add(remoteProxyRenderer);

        final Intent intent = getIntent();

        // Create video renderers.
        /** PIP 뷰 비율 조정 */
        /** SCALE_ASPECT_FIT, SCALE_ASPECT_BALANCED, SCALE_ASPECT_FILL */
        rootEglBase = EglBase.create();
        pipRenderer.init(rootEglBase.getEglBaseContext(), null);
        Log.d(TAG, "2_pipRenderer.isClickable(): " + pipRenderer.isClickable());
        pipRenderer.setClickable(false);
        pipRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        Log.d(TAG, "3_pipRenderer.isClickable(): " + pipRenderer.isClickable());


        // TODO: PIP 뷰 비율 조정_ 파일 저장? => 일단은 안됨
        // 원래 코드
        String saveRemoteVideoToFile = intent.getStringExtra(Static.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE);
        // try 코드
        // String saveRemoteVideoToFile = "NOT_NULL";
        Log.d(TAG, "saveRemoteVideoToFile: " + saveRemoteVideoToFile);

        // When saveRemoteVideoToFile is set we save the video from the remote to a file.
        // saveRemoteVideoToFile이 설정되면 원격에서 파일로 비디오를 저장합니다.
        if (saveRemoteVideoToFile != null) {
            int videoOutWidth = intent.getIntExtra(Static.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH, 0);
            int videoOutHeight = intent.getIntExtra(Static.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT, 0);
            try {
                videoFileRenderer = new VideoFileRenderer(
                        saveRemoteVideoToFile, videoOutWidth, videoOutHeight, rootEglBase.getEglBaseContext());
                remoteRenderers.add(videoFileRenderer);
            } catch (IOException e) {
                throw new RuntimeException(
                        "Failed to open video file for output: " + saveRemoteVideoToFile, e);
            }
        }

        fullscreenRenderer.init(rootEglBase.getEglBaseContext(), null);
        fullscreenRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);


        pipRenderer.setVisibility(View.INVISIBLE);
        pipRenderer.setZOrderMediaOverlay(true);
        Log.d(TAG, "4_pipRenderer.isClickable(): " + pipRenderer.isClickable());
        pipRenderer.setEnableHardwareScaler(true /* enabled */);
        Log.d(TAG, "5_pipRenderer.isClickable(): " + pipRenderer.isClickable());
        fullscreenRenderer.setEnableHardwareScaler(true /* enabled */);

        // Start with local feed in fullscreen and swap it to the pip when the call is connected.
        // 전체 화면에서 로컬 피드로 시작하고 전화가 연결되면 핍으로 바꾸십시오.
        setSwappedFeeds(true /* isSwappedFeeds */);

        Uri roomUri = intent.getData();
        if (roomUri == null) {
//            logAndToast("FATAL ERROR: Missing URL to connect to.");
            Log.e(TAG, "Didn't get any URL in intent!");
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        // Get Intent parameters.
        String roomId = intent.getStringExtra(Static.EXTRA_ROOMID);
        Log.d(TAG, "Room ID: " + roomId);
        if (roomId == null || roomId.length() == 0) {
//            logAndToast("FATAL ERROR: Missing URL to connect to.");
            Log.e(TAG, "Incorrect room ID in intent!");
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        boolean loopback = intent.getBooleanExtra(Static.EXTRA_LOOPBACK, false);
        boolean tracing = intent.getBooleanExtra(Static.EXTRA_TRACING, false);

        int videoWidth = intent.getIntExtra(Static.EXTRA_VIDEO_WIDTH, 0);
        int videoHeight = intent.getIntExtra(Static.EXTRA_VIDEO_HEIGHT, 0);

        screencaptureEnabled = intent.getBooleanExtra(Static.EXTRA_SCREENCAPTURE, false);

        // If capturing format is not specified for screenCapture, use screen resolution.
        // screenCapture에 캡처 형식이 지정되어 있지 않으면 화면 해상도를 사용함
        if (screencaptureEnabled && videoWidth == 0 && videoHeight == 0) {
            DisplayMetrics displayMetrics = getDisplayMetrics();
            videoWidth = displayMetrics.widthPixels;
            videoHeight = displayMetrics.heightPixels;
        }

        PeerConnectionClient.DataChannelParameters dataChannelParameters = null;
        if (intent.getBooleanExtra(Static.EXTRA_DATA_CHANNEL_ENABLED, false)) {
            dataChannelParameters = new PeerConnectionClient.DataChannelParameters(intent.getBooleanExtra(Static.EXTRA_ORDERED, true),
                    intent.getIntExtra(Static.EXTRA_MAX_RETRANSMITS_MS, -1),
                    intent.getIntExtra(Static.EXTRA_MAX_RETRANSMITS, -1), intent.getStringExtra(Static.EXTRA_PROTOCOL),
                    intent.getBooleanExtra(Static.EXTRA_NEGOTIATED, false), intent.getIntExtra(Static.EXTRA_ID, -1));
        }

        peerConnectionParameters =
                new PeerConnectionClient.PeerConnectionParameters(intent.getBooleanExtra(Static.EXTRA_VIDEO_CALL, true), loopback,
                        tracing, videoWidth, videoHeight, intent.getIntExtra(Static.EXTRA_VIDEO_FPS, 0),
                        intent.getIntExtra(Static.EXTRA_VIDEO_BITRATE, 0), intent.getStringExtra(Static.EXTRA_VIDEOCODEC),
                        intent.getBooleanExtra(Static.EXTRA_HWCODEC_ENABLED, true),
                        intent.getBooleanExtra(Static.EXTRA_FLEXFEC_ENABLED, false),
                        intent.getIntExtra(Static.EXTRA_AUDIO_BITRATE, 0), intent.getStringExtra(Static.EXTRA_AUDIOCODEC),
                        intent.getBooleanExtra(Static.EXTRA_NOAUDIOPROCESSING_ENABLED, false),
                        intent.getBooleanExtra(Static.EXTRA_AECDUMP_ENABLED, false),
                        intent.getBooleanExtra(Static.EXTRA_OPENSLES_ENABLED, false),
                        intent.getBooleanExtra(Static.EXTRA_DISABLE_BUILT_IN_AEC, false),
                        intent.getBooleanExtra(Static.EXTRA_DISABLE_BUILT_IN_AGC, false),
                        intent.getBooleanExtra(Static.EXTRA_DISABLE_BUILT_IN_NS, false),
                        intent.getBooleanExtra(Static.EXTRA_ENABLE_LEVEL_CONTROL, false),
                        intent.getBooleanExtra(Static.EXTRA_DISABLE_WEBRTC_AGC_AND_HPF, false), dataChannelParameters);

        commandLineRun = intent.getBooleanExtra(Static.EXTRA_CMDLINE, false);
        runTimeMs = intent.getIntExtra(Static.EXTRA_RUNTIME, 0);

        Log.d(TAG, "VIDEO_FILE: '" + intent.getStringExtra(Static.EXTRA_VIDEO_FILE_AS_CAMERA) + "'");

        // Create connection client. Use DirectRTCClient if room name is an IP otherwise use the standard WebSocketRTCClient.
        //
        // 연결 클라이언트를 만듭니다. 방 이름이 IP 인 경우 DirectRTCClient를 사용하고,
        // 그렇지 않으면 표준 WebSocketRTCClient를 사용하십시오.
        // TODO: 보통 웹소켓을 통해 연결
        if (loopback || !DirectRTCClient.IP_PATTERN.matcher(roomId).matches()) {
            appRtcClient = new WebSocketRTCClient(this);
            Log.i(TAG, "Using WebSocketRTCClient");
        } else {
            Log.i(TAG, "Using DirectRTCClient because room name looks like an IP.");
            appRtcClient = new DirectRTCClient(this);
        }

        // Create connection parameters.
        String urlParameters = intent.getStringExtra(Static.EXTRA_URLPARAMETERS);
        roomConnectionParameters =
                new AppRTCClient.RoomConnectionParameters(roomUri.toString(), roomId, loopback, urlParameters);

        // Create CPU monitor
//        cpuMonitor = new CpuMonitor(this);
//        hudFragment.setCpuMonitor(cpuMonitor);

        // Send intent arguments to fragments.
        call_f.setArguments(intent.getExtras());
        hud_f.setArguments(intent.getExtras());
        // Activate call and HUD fragments and start the call.
        // call_f 및 hud_f를 활성화하고, 통화를 시작
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.call_fragment_container, call_f);
        ft.add(R.id.hud_fragment_container, hud_f);
        ft.commit();

        // For command line execution run connection for <runTimeMs> and exit.
        if (commandLineRun && runTimeMs > 0) {
            (new Handler()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    disconnect();
                }
            }, runTimeMs);
        }

        peerConnectionClient = PeerConnectionClient.getInstance();
        if (loopback) {
            PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
            options.networkIgnoreMask = 0;
            peerConnectionClient.setPeerConnectionFactoryOptions(options);
        }

        peerConnectionClient.createPeerConnectionFactory(
                getApplicationContext(), peerConnectionParameters, Call_A.this);

        // TODO: == webRTC 구동을 끄기위한 주석 ==
        // TODO: 개발을 위해 임시적으로 주석처리
        // TODO: 나중에 반드시 주석 해제 할 것!!!!!!!!!!!
//        if (screencaptureEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            startScreenCapture();
//        } else {
//            startCall();
//        }

        /**---------------------------------------------------------------------------
         핸들러 ==> 회의 종료 다이얼로그(액티비티) 띄우기
         ---------------------------------------------------------------------------*/
        hangup_confirm = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    onBackPressed();
                }
            }
        };


        /**---------------------------------------------------------------------------
         핸들러 ==> 크로노미터 -- 회의시간
         ---------------------------------------------------------------------------*/
        time_handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0) {
                    timeElapsed.setBase(SystemClock.elapsedRealtime());
                    timeElapsed.start();

                    timeElapsed.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
                        @Override
                        public void onChronometerTick(Chronometer cArg) {
                            timeElapsed.setVisibility(View.VISIBLE);
//                long time = SystemClock.elapsedRealtime() - cArg.getBase() + onAir_Elapsed_time_mil;
                            long time = SystemClock.elapsedRealtime() - cArg.getBase();
//                        Log.d("TCP", "onAir_Elapsed_time_mil: " + String.valueOf(onAir_Elapsed_time_mil));
//                        Log.d("TCP", "cArg.getBase(): " + String.valueOf(cArg.getBase()));
//                        Log.d("TCP", "time: " + String.valueOf(time));

                            int h = (int) (time / 3600000);
                            int m = (int) (time - h * 3600000) / 60000;
                            int s = (int) (time - h * 3600000 - m * 60000) / 1000;
                            String hh = h < 10 ? "0" + h : h + "";
                            String mm = m < 10 ? "0" + m : m + "";
                            String ss = s < 10 ? "0" + s : s + "";
                            cArg.setText(hh + ":" + mm + ":" + ss);
//                        Log.d("TCP", "hh+\":\"+mm+\":\"+ss_ " + hh+":"+mm+":"+ss);
                        }
                    });
                }
            }
        };

        /** 서버로 부터 받은 상대방의 비디오 on/off 상태를 Chat_handler로부터 전달받음 */
        webrtc_message_handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                // 핸들러 메세지를 보낸 주체가 Chat_handler에 to_Call_A() 메소드일 때
                if(msg.what == 1) {

                    // Message 객체로 부터 전달된 값들 가져오기
                    String order = msg.getData().getString("order");
                    Data_for_netty received_data = (Data_for_netty) msg.obj;

                    Log.d(TAG, "order: " + order);
                    Log.d(TAG, "getNetty_type(): " + received_data.getNetty_type());
                    Log.d(TAG, "getSubType(): " + received_data.getSubType());
                    Log.d(TAG, "getSender_user_no(): " + received_data.getSender_user_no());
                    Log.d(TAG, "getTarget_user_no(): " + received_data.getTarget_user_no());
                    Log.d(TAG, "getExtra(): " + received_data.getExtra());

                    // 서버에서 중계한, 다른 유저가 보낸 비디오 on/off 상태를 전달일 때
                    if(order.equals("relay_video_status")) {
                        String[] temp = received_data.getExtra().split(Static.SPLIT);

                        // 뷰 조절 메소드 호출
                        when_video_status_change(
                                Boolean.valueOf(temp[0]), !pipRenderer.isClickable(), temp[1]);
                    }

                    // 서버에서 중계한, 파일 공유 요청을 전달일 때
                    else if(order.equals("relay_request_image_file_share")) {
                        Intent intent = new Intent(Call_A.this, Confirm_img_share_mode_accept_D.class);
                        startActivityForResult(intent, REQUEST_CONFIRM_IMAGE_SHARE_MODE);
                    }

                    // 서버에서 중계한, 파일 공유 요청에 대한 답변 전달일 때
                    else if(order.equals("relay_answer_image_file_share")) {

                        String answer = received_data.getExtra();
                        Logger.d("answer: " + answer);
                        // todo: answer 의 'yes', 'no'에 따라서 맞춰 코딩하기
                    }
                }
            }
        };
    }


    /**---------------------------------------------------------------------------
     생명주기 ==> onResume
     ---------------------------------------------------------------------------*/
    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * 기기 해상도 가져오기
     */
    @TargetApi(17)
    private DisplayMetrics getDisplayMetrics() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager =
                (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getRealMetrics(displayMetrics);
        return displayMetrics;
    }

    @TargetApi(19)
    private static int getSystemUiVisibility() {
        int flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            flags |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }
        return flags;
    }

    @TargetApi(21)
    private void startScreenCapture() {
        MediaProjectionManager mediaProjectionManager =
                (MediaProjectionManager) getApplication().getSystemService(
                        Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(
                mediaProjectionManager.createScreenCaptureIntent(), CAPTURE_PERMISSION_REQUEST_CODE);
    }

    private void startCall() {
        if (appRtcClient == null) {
            Log.e(TAG, "AppRTC client is not allocated for a call.");
            return;
        }
        callStartedTimeMs = System.currentTimeMillis();

        // Start room connection.
//        logAndToast(getString(R.string.connecting_to, roomConnectionParameters.roomUrl));
        appRtcClient.connectToRoom(roomConnectionParameters);

        // Create and audio manager that will take care of audio routing, audio modes, audio device enumeration etc.
        // 오디오 라우팅, 오디오 모드, 오디오 장치 열거 등을 처리하는 오디오 관리자를 만듭니다.
        audioManager = AppRTCAudioManager.create(getApplicationContext());
        // Store existing audio settings and change audio mode to MODE_IN_COMMUNICATION for best possible VoIP performance.
        // 가능한 최상의 VoIP 성능을 위해 기존 오디오 설정을 저장하고 오디오 모드를 MODE_IN_COMMUNICATION으로 변경하십시오.
        Log.d(TAG, "Starting the audio manager...");
        audioManager.start(new AppRTCAudioManager.AudioManagerEvents() {
            // This method will be called each time the number of available audio devices has changed.
            // 이 메서드는 사용 가능한 오디오 장치 수가 변경 될 때마다 호출됩니다.
            @Override
            public void onAudioDeviceChanged(AppRTCAudioManager.AudioDevice audioDevice, Set<AppRTCAudioManager.AudioDevice> availableAudioDevices) {
                onAudioManagerDevicesChanged(audioDevice, availableAudioDevices);
            }
        });

        /** 전송 화질 수정 시도 */
        // TODO: 전송 화질 수정 시도, 중요!!
        peerConnectionClient.changeCaptureFormat(640, 480, 24);
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 이미지 쉐어 모드 -- 그리기 선 모두 지우기
     ---------------------------------------------------------------------------*/
    @OnClick({R.id.remove_all})
    public void remove_all() {
        Log.d(TAG, "그리기 선 모두 지우기 클릭");
        wipeCanvas();
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 이미지 쉐어 모드 -- 이동/그리기 모드 전환 버튼
     ---------------------------------------------------------------------------*/
    @OnClick({R.id.enable_drag_btn})
    public void enable_drag() {
        Log.d(TAG, "이동/그리기 모드 전환 버튼 클릭");
        if(!enable_drag) {
//            Logger.d("enable_drag_ON");
            on_moving = true;
            enable_drag = true;
            enable_drag_btn.setText("이동 on");
            call_draw_handler.sendEmptyMessage(4);
            // 현재 realm Result 의 마지막 인덱스를 변수에 저장한다
            if(results_main.size() > 0) {
                last_realmResult_index = results_main.size()-1;
                Logger.d("현재 realm Result 의 마지막 인덱스: " + last_realmResult_index);
            }
        }
        else if(enable_drag) {
//            Logger.d("enable_drag_OFF");
            on_moving = false;
            enable_drag = false;
            enable_drag_btn.setText("이동 off");
        }
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 이미지 쉐어 모드 -- 뷰에 보여지는 상태 그대로, 이미지 파일 저장하기
     ---------------------------------------------------------------------------*/
    @OnClick({R.id.save_Image})
    public void save_Image() {
        Log.d(TAG, "뷰에 보여지는 상태 그대로, 이미지 파일 저장 버튼 클릭");
        call_draw_handler.sendEmptyMessage(3);
    }


    /**
     * ---------------------------------------------------------------------------
     * 메소드 ==> onActivityResult -- 통화 종료 | 로컬 파일 가져오기
     * ---------------------------------------------------------------------------
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        /** 통화종료 */
        if (requestCode == REQUEST_OUT && resultCode == RESULT_OK) {
            Thread.setDefaultUncaughtExceptionHandler(null);
            disconnect();
            if (logToast != null) {
                logToast.cancel();
            }
            activityRunning = false;
        }

        /** 로컬 파일 가져오기 */
        else if (requestCode == REQUEST_GET_LOCAL_FILE && resultCode == RESULT_OK) {
            String target_format = data.getStringExtra("FORMAT");
            Log.d(TAG, "target_format: " + target_format);

            // otto 를 통해, 프래그먼트로 이벤트 전달하기
            Event.Call_A__Call_F call_a__call_f = new Event.Call_A__Call_F("local", target_format);
            BusProvider.getBus().post(call_a__call_f);
        }

        /** 파일들 업로드 선택 */
        else if (requestCode == REQUEST_CONFIRM_UPLOAD_FILES && resultCode == RESULT_OK) {
            boolean contain_pdf_file_orNot = data.getBooleanExtra("contain_pdf_file_orNot", false);
            Log.d(TAG, "contain_pdf_file_orNot: " + contain_pdf_file_orNot);

            // otto 를 통해, 프래그먼트로 이벤트 전달하기
            Event.Call_A__Call_F call_a__call_f = new Event.Call_A__Call_F("file_upload", String.valueOf(contain_pdf_file_orNot));
            BusProvider.getBus().post(call_a__call_f);
        }

        /** 파일들 업로드 취소선택 */
        else if (requestCode == REQUEST_CONFIRM_UPLOAD_FILES && resultCode == RESULT_CANCELED) {

            // otto 를 통해, 프래그먼트로 이벤트 전달하기
            Event.Call_A__Call_F call_a__call_f = new Event.Call_A__Call_F("file_upload_cancel", "");
            BusProvider.getBus().post(call_a__call_f);
        }

        /** 문서 공유 모드 요청 수락|거절 */
        else if(requestCode == REQUEST_CONFIRM_IMAGE_SHARE_MODE) {

            // netty를 통해서 상대방에게 파일 공유모드 요청에 대한 답변 보내기
            Data_for_netty data_for_answer = new Data_for_netty();
            data_for_answer.setNetty_type("webrtc");
            data_for_answer.setSubType("answer_image_file_share");
            data_for_answer.setSender_user_no(myapp.getUser_no());
            data_for_answer.setTarget_user_no(myapp.getMeeting_subject_user_no());

            // todo: answer 의 'yes', 'no'에 따라서 맞춰 코딩하기
            //// Data_for_netty 객체의 'extra' 변수에
            //// 문서 공유모드 요청 답변을 넣어 보낸다
            // 수락
            if(resultCode == RESULT_OK) {
                data_for_answer.setExtra("yes");

                /**
                 * 문서 공유모드 진행을 위한 내부 메소드 호출
                    ==> realm 서버 접속 및 드로잉 준비
                 */
                initializing_for_image_share_mode();
            }
            // 거절
            else if(resultCode == RESULT_CANCELED) {
                data_for_answer.setExtra("no");

                /** 테스트용 코드 */
                // 이미지 공유 레이아웃(서피스뷰 포함) GONE.
                image_share_REL.setVisibility(View.GONE);

            }

            myapp.send_to_server(data_for_answer);
        }

        // webRTC 스크린 캡쳐 퍼미션 확인
        if (requestCode != CAPTURE_PERMISSION_REQUEST_CODE) {
            Log.d(TAG, "CAPTURE_PERMISSION_REQUEST_CODE");
            return;
        }

        mediaProjectionPermissionResultCode = resultCode;
        mediaProjectionPermissionResultData = data;
        startCall();
    }

    private boolean useCamera2() {
        return Camera2Enumerator.isSupported(this) && getIntent().getBooleanExtra(Static.EXTRA_CAMERA2, true);
    }

    private boolean captureToTexture() {
        return getIntent().getBooleanExtra(Static.EXTRA_CAPTURETOTEXTURE_ENABLED, false);
    }

    private VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();

        // First, try to find front facing camera
        Logging.d(TAG, "Looking for front facing cameras.");
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating front facing camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        // Front facing camera not found, try something else
        Logging.d(TAG, "Looking for other cameras.");
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating other camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }

    @TargetApi(21)
    private VideoCapturer createScreenCapturer() {
        if (mediaProjectionPermissionResultCode != Activity.RESULT_OK) {
            reportError("User didn't give permission to capture the screen.");
            return null;
        }
        return new ScreenCapturerAndroid(mediaProjectionPermissionResultData, new MediaProjection.Callback() {
            @Override
            public void onStop() {
                reportError("User revoked permission to capture the screen.");
            }
        });
    }

    // Activity interfaces
    @Override
    public void onStop() {
        super.onStop();
        activityRunning = false;
        // Don't stop the video when using screencapture to allow user to show other apps to the remote end.
        // screencapture를 사용할 때 동영상을 멈추지 말고 다른 앱을 원격쪽에 보여줄 수 있습니다.
        if (peerConnectionClient != null && !screencaptureEnabled) {
            peerConnectionClient.stopVideoSource();
        }
//        cpuMonitor.pause();
    }

    @Override
    public void onStart() {
        super.onStart();

        // otto 등록
        BusProvider.getBus().register(this);

        activityRunning = true;
        // Video is not paused for screencapture. See onPause.
        if (peerConnectionClient != null && !screencaptureEnabled) {
            peerConnectionClient.startVideoSource();
        }
//        cpuMonitor.resume();
    }

    @Override
    protected void onDestroy() {
        Thread.setDefaultUncaughtExceptionHandler(null);
        disconnect();
        if (logToast != null) {
            logToast.cancel();
        }
        activityRunning = false;
        if (rootEglBase != null) {
            rootEglBase.release();
        }
        timeElapsed.stop();

        // otto 등록 해제
        BusProvider.getBus().unregister(this);
        // 메소드 호출
        got_out_from_meeting();

        // static handler 객체 null 처리
        if(webrtc_message_handler != null) {
            webrtc_message_handler = null;
        }
        // 버터나이프 바인드 해제
        if(unbinder != null) {
            unbinder.unbind();
        }
        // realm 객체 닫고 null 처리하기
        if (realm != null) {
            realm.close();
            realm = null;
        }
        super.onDestroy();
    }

    public void got_out_from_meeting() {
        RetrofitService rs = ServiceGenerator.createService(RetrofitService.class);
        Call<ResponseBody> call_result = rs.got_out_from_meeting(
                Static.GOT_OUT_FROM_MEETING,
                myapp.getUser_no(), myapp.getMeeting_no());
        call_result.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String retrofit_result = response.body().string();
                    Log.d(TAG, "GOT_OUT_FROM_MEETING_result: " + retrofit_result);

                    if (retrofit_result.equals("success")) {
                        // 내 회의 정보 정보 변경
                        myapp.setPresent_meeting_in_ornot("");
                        myapp.setMeeting_no("");
                        myapp.setReal_meeting_title("");
                        myapp.setMeeting_creator_user_no("");
                        myapp.setMeeting_subject_user_no("");
                        myapp.setMeeting_authority_user_no("");
                        myapp.setProject_no("");
                        myapp.setMeeting_status("");
                    } else if (retrofit_result.equals("fail")) {
                        myapp.logAndToast("onResponse_fail");
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


    @Override
    public void onConnectedToRoom(final AppRTCClient.SignalingParameters params) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onConnectedToRoomInternal(params);
            }
        });
    }

    @Override
    public void onRemoteDescription(final SessionDescription sdp) {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (peerConnectionClient == null) {
                    Log.e(TAG, "Received remote SDP for non-initilized peer connection.");
                    return;
                }
//                logAndToast("Received remote " + sdp.type + ", delay=" + delta + "ms");
                peerConnectionClient.setRemoteDescription(sdp);
                if (!signalingParameters.initiator) {
//                    logAndToast("Creating ANSWER...");
                    // Create answer. Answer SDP will be sent to offering client in
                    // PeerConnectionEvents.onLocalDescription event.
                    peerConnectionClient.createAnswer();
                }
            }
        });
    }

    @Override
    public void onRemoteIceCandidate(final IceCandidate candidate) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (peerConnectionClient == null) {
                    Log.e(TAG, "Received ICE candidate for a non-initialized peer connection.");
                    return;
                }
                peerConnectionClient.addRemoteIceCandidate(candidate);
            }
        });
    }

    @Override
    public void onRemoteIceCandidatesRemoved(final IceCandidate[] candidates) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (peerConnectionClient == null) {
                    Log.e(TAG, "Received ICE candidate removals for a non-initialized peer connection.");
                    return;
                }
                peerConnectionClient.removeRemoteIceCandidates(candidates);
            }
        });
    }

    @Override
    public void onChannelClose() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                logAndToast("Remote end hung up; dropping PeerConnection");
                disconnect();
            }
        });
    }

    @Override
    public void onChannelError(final String description) {
        reportError(description);
    }

    // CallFragment.OnCallEvents interface implementation.
    @Override
    public void onCallHangUp() {
        disconnect();
    }

    @Override
    public void onCameraSwitch() {
        if (peerConnectionClient != null) {
            peerConnectionClient.switchCamera();
        }
    }

    @Override
    public void onVideoScalingSwitch(RendererCommon.ScalingType scalingType) {
        fullscreenRenderer.setScalingType(scalingType);
    }

    @Override
    public void onCaptureFormatChange(int width, int height, int framerate) {
        if (peerConnectionClient != null) {
            // TODO: 화질 수정, 중요!!
            peerConnectionClient.changeCaptureFormat(width, height, framerate);
        }
    }

    @Override
    public boolean onToggleMic() {
        if (peerConnectionClient != null) {
            micEnabled = !micEnabled;
            peerConnectionClient.setAudioEnabled(micEnabled);
        }
        return micEnabled;
    }

    @Override
    public boolean onToggleVideo() {
        if (peerConnectionClient != null) {
            videoEnabled = !videoEnabled;
            peerConnectionClient.setVideoEnabled(videoEnabled);
        }
        return videoEnabled;
    }

    // Helper functions.
    private void toggleCallControlFragmentVisibility() {
        if (!iceConnected || !call_f.isAdded()) {
            return;
        }
        // Show/hide call control fragment
        callControlFragmentVisible = !callControlFragmentVisible;
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (callControlFragmentVisible) {
            ft.show(call_f);
            ft.show(hud_f);
        } else {
            ft.hide(call_f);
            ft.hide(hud_f);
        }
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();
    }

    // Should be called from UI thread
    // UI 스레드에서 호출해야합니다.
    private void callConnected() {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        Log.i(TAG, "Call connected: delay=" + delta + "ms");
        if (peerConnectionClient == null || isError) {
            Log.w(TAG, "Call is connected in closed or error state");
            return;
        }
        // Enable statistics callback.
        peerConnectionClient.enableStatsEvents(true, STAT_CALLBACK_PERIOD);
        /** pip view set VISIBLE */
        pipRenderer.setClickable(true);
        Log.d(TAG, "6_pipRenderer.isClickable(): " + pipRenderer.isClickable());
        // remote 뷰를 풀스크린으로, local뷰는 pip로
        setSwappedFeeds(false /* isSwappedFeeds */);
    }

    // This method is called when the audio manager reports audio device change, e.g. from wired headset to speakerphone.
    // 이 메소드는 오디오 관리자가 오디오 장치 변경을보고 할 때 호출됩니다. 유선 헤드셋에서 스피커폰으로.
    private void onAudioManagerDevicesChanged(
            final AudioDevice device, final Set<AudioDevice> availableDevices) {
        Log.d(TAG, "onAudioManagerDevicesChanged: " + availableDevices + ", "
                + "selected: " + device);
        // TODO(henrika): add callback handler.
    }

    // Disconnect from remote resources, dispose of local resources, and exit.
    // 원격 자원의 연결을 끊고, 로컬 자원을 처분하고 종료하십시오.
    private void disconnect() {
        activityRunning = false;
        remoteProxyRenderer.setTarget(null);
        localProxyRenderer.setTarget(null);
        if (appRtcClient != null) {
            appRtcClient.disconnectFromRoom();
            appRtcClient = null;
        }
        if (peerConnectionClient != null) {
            peerConnectionClient.close();
            peerConnectionClient = null;
        }
        if (pipRenderer != null) {
            pipRenderer.release();
            pipRenderer = null;
        }
        if (videoFileRenderer != null) {
            videoFileRenderer.release();
            videoFileRenderer = null;
        }
        if (fullscreenRenderer != null) {
            fullscreenRenderer.release();
            fullscreenRenderer = null;
        }
        if (audioManager != null) {
            audioManager.stop();
            audioManager = null;
        }
        if (iceConnected && !isError) {
            setResult(RESULT_OK);
        } else {
            setResult(RESULT_CANCELED);
        }
        finish();
    }

    private void disconnectWithErrorMessage(final String errorMessage) {
        if (commandLineRun || !activityRunning) {
            Log.e(TAG, "Critical error: " + errorMessage);
            disconnect();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Connection error")
                    .setMessage(errorMessage)
                    .setCancelable(false)
                    .setNeutralButton("OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    disconnect();
                                }
                            })
                    .create()
                    .show();
        }
    }

    // Log |msg| and Toast about it.
    private void logAndToast(String msg) {
        Log.d(TAG, msg);
        if (logToast != null) {
            logToast.cancel();
        }
        logToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        logToast.show();
    }

    private void reportError(final String description) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isError) {
                    isError = true;
                    disconnectWithErrorMessage(description);
                }
            }
        });
    }

    private VideoCapturer createVideoCapturer() {
        VideoCapturer videoCapturer = null;
        String videoFileAsCamera = getIntent().getStringExtra(Static.EXTRA_VIDEO_FILE_AS_CAMERA);
        if (videoFileAsCamera != null) {
            try {
                videoCapturer = new FileVideoCapturer(videoFileAsCamera);
            } catch (IOException e) {
                reportError("Failed to open video file for emulated camera");
                return null;
            }
        } else if (screencaptureEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return createScreenCapturer();
        } else if (useCamera2()) {
            if (!captureToTexture()) {
                reportError("Camera2 only supports capturing to texture. Either disable Camera2 or enable capturing to texture in the options.");
                return null;
            }

            Logging.d(TAG, "Creating capturer using camera2 API.");
            videoCapturer = createCameraCapturer(new Camera2Enumerator(this));
        } else {
            Logging.d(TAG, "Creating capturer using camera1 API.");
            videoCapturer = createCameraCapturer(new Camera1Enumerator(captureToTexture()));
        }
        if (videoCapturer == null) {
            reportError("Failed to open camera");
            return null;
        }
        return videoCapturer;
    }

    private void setSwappedFeeds(boolean isSwappedFeeds) {
        Logging.d(TAG, "setSwappedFeeds: " + isSwappedFeeds);
        this.isSwappedFeeds = isSwappedFeeds;
        localProxyRenderer.setTarget(isSwappedFeeds ? fullscreenRenderer : pipRenderer);
        remoteProxyRenderer.setTarget(isSwappedFeeds ? pipRenderer : fullscreenRenderer);
        fullscreenRenderer.setMirror(isSwappedFeeds);
        pipRenderer.setMirror(!isSwappedFeeds);
    }

    // -----Implementation of AppRTCClient.AppRTCSignalingEvents ---------------
    // All callbacks are invoked from websocket signaling looper thread and are routed to UI thread.

    // AppRTCClient.AppRTCSignalingEvents 구현
    // 모든 콜백은 websocket 신호 루퍼 스레드에서 호출되며 UI 스레드로 라우팅됩니다.
    private void onConnectedToRoomInternal(final AppRTCClient.SignalingParameters params) {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;

        signalingParameters = params;
//        logAndToast("Creating peer connection, delay=" + delta + "ms");
        VideoCapturer videoCapturer = null;
        if (peerConnectionParameters.videoCallEnabled) {
            videoCapturer = createVideoCapturer();
        }
        peerConnectionClient.createPeerConnection(rootEglBase.getEglBaseContext(), localProxyRenderer,
                remoteRenderers, videoCapturer, signalingParameters);

        if (signalingParameters.initiator) {
//            logAndToast("Creating OFFER...");
            // Create offer. Offer SDP will be sent to answering client in
            // PeerConnectionEvents.onLocalDescription event.
            // offer를 만듭니다. 오퍼 SDP는 PeerConnectionEvents.onLocalDescription 이벤트에서 응답 클라이언트로 전송됩니다.
            peerConnectionClient.createOffer();
        } else {
            if (params.offerSdp != null) {
                peerConnectionClient.setRemoteDescription(params.offerSdp);
//                logAndToast("Creating ANSWER...");
                // Create answer. Answer SDP will be sent to offering client in
                // PeerConnectionEvents.onLocalDescription event.
                // 대답을 만듭니다. 답변 SDP는 PeerConnectionEvents.onLocalDescription 이벤트에서 클라이언트 제공 업체로 전송됩니다.
                peerConnectionClient.createAnswer();
            }
            if (params.iceCandidates != null) {
                // Add remote ICE candidates from room.
                // room에서 원격 ICE candidates를 추가
                for (IceCandidate iceCandidate : params.iceCandidates) {
                    peerConnectionClient.addRemoteIceCandidate(iceCandidate);
                }
            }
        }
    }


    // -----Implementation of PeerConnectionClient.PeerConnectionEvents.---------
    // Send local peer connection SDP and ICE candidates to remote party.
    // All callbacks are invoked from peer connection client looper thread and
    // are routed to UI thread.

    // PeerConnectionClient.PeerConnectionEvents 구현.
    // 로컬 피어 연결 SDP 및 ICE 후보를 원격 상대방에게 보냅니다.
    // 모든 콜백은 피어 연결 클라이언트 루퍼 스레드에서 호출되며 UI 스레드로 라우팅됩니다.
    @Override
    public void onLocalDescription(final SessionDescription sdp) {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (appRtcClient != null) {
//                    logAndToast("Sending " + sdp.type + ", delay=" + delta + "ms");
                    if (signalingParameters.initiator) {
                        appRtcClient.sendOfferSdp(sdp);
                    } else {
                        appRtcClient.sendAnswerSdp(sdp);
                    }
                }
                if (peerConnectionParameters.videoMaxBitrate > 0) {
                    Log.d(TAG, "Set video maximum bitrate: " + peerConnectionParameters.videoMaxBitrate);
                    peerConnectionClient.setVideoMaxBitrate(peerConnectionParameters.videoMaxBitrate);
                }
            }
        });
    }

    @Override
    public void onIceCandidate(final IceCandidate candidate) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (appRtcClient != null) {
                    appRtcClient.sendLocalIceCandidate(candidate);
                }
            }
        });
    }

    @Override
    public void onIceCandidatesRemoved(final IceCandidate[] candidates) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (appRtcClient != null) {
                    appRtcClient.sendLocalIceCandidateRemovals(candidates);
                }
            }
        });
    }

    @Override
    public void onIceConnected() {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                logAndToast("ICE connected, delay=" + delta + "ms");
                iceConnected = true;
                /** pip 서피스뷰 set Visible */
                pipRenderer.setVisibility(View.VISIBLE);
                callConnected();
            }
        });

        time_handler.sendEmptyMessage(0);
    }

    @Override
    public void onIceDisconnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                logAndToast("ICE disconnected");
                iceConnected = false;
                disconnect();
            }
        });
    }

    @Override
    public void onPeerConnectionClosed() {
    }

    @Override
    public void onPeerConnectionStatsReady(final StatsReport[] reports) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isError && iceConnected) {
                    hud_f.updateEncoderStatistics(reports);
                }
            }
        });
    }

    @Override
    public void onPeerConnectionError(final String description) {
        reportError(description);
    }


    /**
     * ---------------------------------------------------------------------------
     * otto ==> Call_F로 부터 message 수신
     *          : 비디오모드가 on / off일 떄, 맞춰서 백업뷰를 키거나 끄라는 메세지
     *          : 뷰 조절 메소드로 비디오 모드 on/off 상태 바로 전달
     * ---------------------------------------------------------------------------
     */
    @Subscribe
    public void getMessage(Event.Call_F__Call_A event) {
        Log.d(TAG, "otto 받음_ " + event.getMessage());

        Log.d(TAG, "8_pipRenderer.isClickable(): " + pipRenderer.isClickable());
        when_video_status_change(event.getMessage(), !pipRenderer.isClickable(), "me");
    }


    /**
     * ---------------------------------------------------------------------------
     * otto ==> Call_F로 부터 message 수신
     *          : 파일 공유 모드 버튼 클릭되었으니,
     *           '상대방에게 파일공유모드 요청을 보내' 라는 이벤트 메시지를 전달받음
     * ---------------------------------------------------------------------------
     */
    @Subscribe
    public void getMessage(Event.Call_F__Call_A_file_share event) {
        Log.d(TAG, "otto 받음_ " + event.getMessage());
        if(event.getMessage().equals("go_share")) {

            // netty를 통해서 상대방에게 파일 공유모드 요청하기
            Data_for_netty data = new Data_for_netty();
            data.setNetty_type("webrtc");
            data.setSubType("request_image_file_share");
            data.setSender_user_no(myapp.getUser_no());
            data.setTarget_user_no(myapp.getMeeting_subject_user_no());
            // Data_for_netty 객체의 'extra' 변수에
            // 내 닉네임을 담아 보낸다
            String requester_nickName = myapp.getUser_nickname();
            data.setExtra(requester_nickName);
            // Data_for_netty 객체의 'attachment' 변수에
            // 공유할 파일들의 이름을 담아 보낸다

            myapp.send_to_server(data);
        }
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 비디오모드가 on/off 일 떄, 백업뷰를 조절하는 메소드
     ---------------------------------------------------------------------------*/
    public void when_video_status_change(
            boolean video_status, boolean loopback_mode, String from) {

        Log.d(TAG, "video_status: " + video_status);
        Log.d(TAG, "loopback_mode: " + loopback_mode);
        Log.d(TAG, "9_pipRenderer.isClickable(): " + pipRenderer.isClickable());
        Log.d(TAG, "from: " + from);

        // 일단 백업뷰 Visibility GONE으로 초기화
//        video_off_backup_REL_full.setVisibility(View.GONE);
//        video_off_backup_REL.setVisibility(View.GONE);

        /** 비디오 모드 변동 주체가 '나' 일때 */
        if(from.equals("me")) {
            // 루프백 모드일 때 - 나 혼자 대기중일 때
            if(loopback_mode) {
                // 비디오 모드가 off 일때, 백업뷰 VISIBLE
                if(!video_status) {
                    video_off_backup_REL_full.setVisibility(View.VISIBLE);
                    // 프로필 이미지
//                    Glide
//                        .with(this)
//                        .load(Static.SERVER_URL_PROFILE_FILE_FOLDER + myapp.getUser_img_filename())
//                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
//                        .bitmapTransform(new CropCircleTransformation(this))
//                        .into(profile_img_full);
                    Glide
                        .with(this)
                        .load(Static.SERVER_URL_PROFILE_FILE_FOLDER + myapp.getUser_img_filename())
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .dontAnimate()
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                profile_img_full.setImageBitmap(resource);
                                profile_img_full.setBorderColor(Color.parseColor("#fffff5"));
                                profile_img_full.setBorderWidth(18);
                            }
                        });

                    // 백 이미지
                    int random = new Random().nextInt(2);
                    Glide
                        .with(this)
//                        .load(myapp.video_off_back_img[random])
                        .load(R.drawable.video_off_back_5)
                        .into(back_img_full);
                }
                // 비디오 모드가 on 일때, 백업뷰 GONE
                else if(video_status) {
                    video_off_backup_REL_full.setVisibility(View.GONE);
                }
            }
            // 루프백 모드가 아닐 때 - 상대방이 영상회의에 들어와 있을 때
            else if(!loopback_mode) {
                // 비디오 모드가 off 일때, 백업뷰 VISIBLE
                if(!video_status) {
                    video_off_backup_REL.setVisibility(View.VISIBLE);
                    // 프로필 이미지
//                    Glide
//                        .with(Call_A.this)
//                        .load(Static.SERVER_URL_PROFILE_FILE_FOLDER + myapp.getUser_img_filename())
//                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
//                        .bitmapTransform(new CropCircleTransformation(Call_A.this))
//                        .into(profile_img);
                    Glide
                        .with(this)
                        .load(Static.SERVER_URL_PROFILE_FILE_FOLDER + myapp.getUser_img_filename())
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .dontAnimate()
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                profile_img.setImageBitmap(resource);
                                profile_img.setBorderColor(Color.parseColor("#fffff5"));
                                profile_img.setBorderWidth(8);
                            }
                        });

                    // 백 이미지
                    int random = new Random().nextInt(2);
                    Glide
                        .with(Call_A.this)
                        .load(R.drawable.video_off_back_5)
                        .into(back_img);
                }
                // 비디오 모드가 on 일때, 백업뷰 GONE
                else if(video_status) {
                    video_off_backup_REL.setVisibility(View.INVISIBLE);
                }

                /** 상대방에게 나의 비디오 on/off 상태를 전달하는 메소드 호출 */
                send_to_subject_my_video_status(video_status);
            }
        }
        /** 비디오 모드 변동 주체가 '상대' 일때 */
        // 매개변수 'from' 에 상대방의 프로필 이미지 파일 이름이 담겨 온다
        else if(!from.equals("me")) {
            Log.d(TAG, "상대방 프로필 이미지 파일 이름: " + from);
            // 비디오 모드가 off 일때, 백업뷰 VISIBLE
            if(!video_status) {
                video_off_backup_REL_full.setVisibility(View.VISIBLE);
                // 프로필 이미지
//                Glide
//                    .with(Call_A.this)
//                    .load(Static.SERVER_URL_PROFILE_FILE_FOLDER + from)
//                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
//                    .bitmapTransform(new CropCircleTransformation(Call_A.this))
//                    .into(profile_img_full);
                Glide
                    .with(this)
                    .load(Static.SERVER_URL_PROFILE_FILE_FOLDER + from)
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .dontAnimate()
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            profile_img_full.setImageBitmap(resource);
                            profile_img_full.setBorderColor(Color.parseColor("#fffff5"));
                            profile_img_full.setBorderWidth(18);
                        }
                    });

                // 백 이미지
                int random = new Random().nextInt(2);
                Glide
                    .with(Call_A.this)
                    .load(R.drawable.video_off_back_5)
                    .into(back_img_full);
            }
            // 비디오 모드가 on 일때, 백업뷰 GONE
            else if(video_status) {
                video_off_backup_REL_full.setVisibility(View.INVISIBLE);
            }
        }
    }


    /**---------------------------------------------------------------------------
     메소드 ==> netty를 통해, 회의하고 있는 상대방에게 나의 비디오 on/off 상태를 알림
     ---------------------------------------------------------------------------*/
    public void send_to_subject_my_video_status(boolean video_status) {
        Data_for_netty data = new Data_for_netty();
        data.setNetty_type("webrtc");
        data.setSubType("sending_my_video_status");
        data.setSender_user_no(myapp.getUser_no());
        data.setTarget_user_no(myapp.getMeeting_subject_user_no());
        // Data_for_netty 객체의 'extra' 변수에
        // index 0. 내 프로필 이미지 파일 이름
        // index 1. 현재 비디오의 상태
        // 이 둘을 Split 구분자로 합하여 담아 보낸다
        String send_this_string = String.valueOf(video_status) + Static.SPLIT + myapp.getUser_img_filename();
        data.setExtra(send_this_string);

        myapp.send_to_server(data);
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 소프트 키보드 백버튼 오버라이드 -- 방송 나가기 컨펌 -- 레이아웃 다이얼로그로 띄우기
     ---------------------------------------------------------------------------*/
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Call_A.this, Out_confirm_D.class);
        startActivityForResult(intent, REQUEST_OUT);
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 파일 공유 모드를 위한, initializing
     ---------------------------------------------------------------------------*/
    public void initializing_for_image_share_mode() {
        // 이미지 공유 레이아웃(서피스뷰 포함) VISIBLE.
        image_share_REL.setVisibility(View.VISIBLE);

        // 임시 - 컬러 이름과 인트값을 담을 해쉬맵 선언
        nameToColorMap = new HashMap<>();
//        colorIdToName= new HashMap<>();

        // realm 서버 로그인
        createUserIfNeededAndAndLogin();

        // 서피스뷰에 콜백 붙이기
        surfaceView.getHolder().addCallback(Call_A.this);

        // draw 관련 핸들러 생성
        draw_handler_create();

        /** 임의 값임, 나중에 다른 방법으로 변경 */
        // 컬러맵 생성
        generateColorMap();

    }


    /**---------------------------------------------------------------------------
     메소드 ==> realm 서버 접속
     ---------------------------------------------------------------------------*/
    private void createUserIfNeededAndAndLogin() {

        // Realm 서버 접속 관련 상수
        final String REALM_URL = "realm://" + "52.78.88.227" + ":9080/~/Draw";
        final String AUTH_URL = "http://" + "52.78.88.227" + ":9080/auth";
        final String ID = "remoteMeeting";
        final String PASSWORD = "remoteMeeting";
//        final String ID = "timon11";
//        final String PASSWORD = "dydska11";

        final SyncCredentials syncCredentials = SyncCredentials.usernamePassword(ID, PASSWORD, false);

        // Assume user exist already first time. If that fails, create it.
        SyncUser.loginAsync(syncCredentials, AUTH_URL, new SyncUser.Callback<SyncUser>() {
            @Override
            public void onSuccess(SyncUser user) {
                final SyncConfiguration syncConfiguration = new SyncConfiguration.Builder(user, REALM_URL).build();
                Realm.setDefaultConfiguration(syncConfiguration);
                realm = Realm.getDefaultInstance();
                results_main = realm.where(DrawPath.class).findAll();
                // 현재 realm Result 의 마지막 인덱스를 변수에 저장한다
                if(results_main.size() > 0) {
                    last_realmResult_index = results_main.size()-1;
                    Logger.d("Login_onSuccess_ 현재 realm Result 의 마지막 인덱스: " + last_realmResult_index);
                }
            }

            @Override
            public void onError(ObjectServerError error) {
                if (error.getErrorCode() == ErrorCode.INVALID_CREDENTIALS) {
                    // User did not exist, create it
                    SyncUser.loginAsync(SyncCredentials.usernamePassword(ID, PASSWORD, true), AUTH_URL, this);
                } else {
                    String errorMsg = String.format("(%s) %s", error.getErrorCode(), error.getErrorMessage());
                    Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    /**---------------------------------------------------------------------------
     콜백메소드 ==> SurfaceHolder.Callback -- surfaceCreated
     ---------------------------------------------------------------------------*/
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

        // 서피스 뷰를 감싸고 있는 뷰의 크기 구하기
        container_width = image_share_REL.getWidth();
        container_height = image_share_REL.getHeight();
//        Logger.d("container_width/2: " + container_width/2);
//        Logger.d("container_height/2: " + container_height/2);

        /** surfaceView 백그라운드에 이미지 넣기, 방법 1
         - 이미지 가로, 세로 길이 체크해서 1080 이상이면 사이즈 줄이기*/
//        // 메모리에 비트맵을 올리지 않고 이미지의 width, height 가져오기
//        try {
//            BitmapFactory.Options options = new BitmapFactory.Options();
//            options.inJustDecodeBounds = true;
//            BitmapFactory.decodeResource(getResources(), R.drawable.lion);
//
//            // 해당 이미지의 비트맵의 가로나 세로의 길이가 1080을 넘는다면, 1/2만큼 리사이즈
//            if(options.outWidth > 1080 || options.outHeight > 1080) {
//                BitmapFactory.Options options_reduction = new BitmapFactory.Options();
//                options_reduction.inSampleSize = 4;
//                document_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.lion);
//            }
//
//            else {
//                document_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.lion);
//            }
//        } catch(Exception e) {
//            Logger.d("e.getMessage(): " + e.getMessage());
//        }

//        double background_scale = (double)document_bitmap.getHeight()/(double)document_bitmap.getWidth();
//        double surfaceView_scale = (double)surfaceView.getHeight()/(double)surfaceView.getWidth();
////        Logger.d("background_scale: " + (float)background_scale);
////        Logger.d("surfaceView_scale: " + (float)surfaceView_scale);
//        Logger.d("background_scale: " + String.valueOf(background_scale) + "\n" +
//                "surfaceView_scale: " + String.valueOf(surfaceView_scale));
//
//        int document_bitmap_width = document_bitmap.getWidth();
//        int document_bitmap_height = document_bitmap.getHeight();
//
//        surfaceView_width = surfaceView.getWidth();
//        surfaceView_height = surfaceView.getHeight();
//        Logger.d("document_bitmap_width: " + document_bitmap_width + "\n" +
//                "document_bitmap_height: " + document_bitmap_height + "\n" +
//                "surfaceView_width: " + surfaceView_width + "\n" +
//                "surfaceView_height: " + surfaceView_height + "\n"
//        );
//
//        /** 가로 길이를 디바이스 가로 크기에 맞춰서,
//         * 가로는 Match_Parent 처럼, 세로는 드래그가 가능하도록 밑으로 길게 빼서 보여주기 위한 길이 계산 - 코드시작 */
//        // 가로는 서피스뷰 가로길이로 고정
//        newWidth = surfaceView_width;
//        //// 세로는, 이미지가 서피스뷰 가로길이에 변경된 비율에 맞게 계산하여 조정하기
//        // 이미지의 가로 길이가 서피스뷰의 가로길이보다 같거나 클 경우
//        double width_transform_scale = 0;
//        if(document_bitmap_width >= surfaceView_width) {
//            width_transform_scale = ((double)document_bitmap_width/(double) surfaceView_width);
//            newHeight = (int)((double)document_bitmap_height/width_transform_scale);
//        }
//        // 이미지의 가로 길이가 서피스뷰의 가로길이보다 작을 경우
//        else if(document_bitmap_width < surfaceView_width) {
//            width_transform_scale = ((double) surfaceView_width /(double)document_bitmap_width);
//            newHeight = (int)((double)document_bitmap_height*width_transform_scale);
//        }
//        Logger.d("width_transform_scale: " + width_transform_scale + "\n" +
//                "newWidth: " + newWidth + "\n" +
//                "newHeight: " + newHeight + "\n" +
//                "background_new_scale: " + (double)newHeight/(double)newWidth + "\n"
//        );
//        surfaceView_fill_width = true;
//
//        scaled = Bitmap.createScaledBitmap(document_bitmap, newWidth, newHeight, true);
//        /** 가로 길이를 디바이스 가로 크기에 맞춰서,
//         * 가로는 Match_Parent 처럼, 세로는 드래그가 가능하도록 밑으로 길게 빼서 보여주기 위한 길이 계산 - 코드끝 */
//
//        /** 가로 or 세로 길이를 디바이스 화면 크기 한쪽에 맞춰서, 최대한 크게 보여주기 위한 길이 계산 - 코드시작 */
////        // 이미지가 가로가 더 길거나 같을 때
////        if(background_width >= background_height) {
////            // 이미지의 가로세로 비율이 서피스뷰의 가로세로 비율보다 높거나 같을 때
////            // 세로 꽉차게 하기
////            if(background_scale >= surfaceView_scale) {
////                newWidth = (background_width*surfaceView_height)/background_height;
////                newHeight = surfaceView_height;
////                surfaceView_fill_width = false;
////            }
////            // 이미지의 가로세로 비율이 서피스뷰의 가로세로 비율보다 낮을 때
////            // 가로 꽉차게 하기
////            else if(background_scale < surfaceView_scale) {
////                newWidth = surfaceview_width;
////                newHeight = (background_height*surfaceview_width)/background_width;
////                surfaceView_fill_width = true;
////            }
////        }
////        // 이미지가 세로가 더 길 때
////        // 세로 꽉차게 하기
////        if(background_width < background_height) {
////            newWidth = (background_width*surfaceView_height)/background_height;
////            newHeight = surfaceView_height;
////            surfaceView_fill_width = false;
////        }
////        Logger.d("newWidth: " + newWidth);
////        Logger.d("newHeight: " + newHeight);
////
////        scaled = Bitmap.createScaledBitmap(background, newWidth, newHeight, true);
//        /** 가로 or 세로 길이를 디바이스 화면 크기 한쪽에 맞춰서, 최대한 크게 보여주기 위한 길이 계산 - 코드끝 */
//
//        // 최초 서피스뷰 그리기
//        call_draw_handler.sendEmptyMessage(2);
//
//        if (drawThread == null) {
//            drawThread = new DrawThread();
//            drawThread.start();
//        }

        /** surfaceView 백그라운드에 이미지 넣기, 방법 2
         - 글라이드로 비트맵 변환해서 줄이기 */
        Glide
            .with(this)
            .load(R.drawable.lion)
            .asBitmap()
            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
            .dontAnimate()
            .into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                    document_bitmap = resource;

                    double background_scale = (double)document_bitmap.getHeight()/(double)document_bitmap.getWidth();
                    double surfaceView_scale = (double)surfaceView.getHeight()/(double)surfaceView.getWidth();
//                    Logger.d("background_scale: " + (float)background_scale);
//                    Logger.d("surfaceView_scale: " + (float)surfaceView_scale);
                    Logger.d("background_scale: " + String.valueOf(background_scale) + "\n" +
                            "surfaceView_scale: " + String.valueOf(surfaceView_scale));

                    int document_bitmap_width = document_bitmap.getWidth();
                    int document_bitmap_height = document_bitmap.getHeight();

                    surfaceView_width = surfaceView.getWidth();
                    surfaceView_height = surfaceView.getHeight();
                    Logger.d("document_bitmap_width: " + document_bitmap_width + "\n" +
                            "document_bitmap_height: " + document_bitmap_height + "\n" +
                            "surfaceView_width: " + surfaceView_width + "\n" +
                            "surfaceView_height: " + surfaceView_height + "\n"
                    );

                    /** 가로 길이를 디바이스 가로 크기에 맞춰서,
                     * 가로는 Match_Parent 처럼, 세로는 드래그가 가능하도록 밑으로 길게 빼서 보여주기 위한 길이 계산 */
                    // 가로는 서피스뷰 가로길이로 고정
                    newWidth = surfaceView_width;
                    //// 세로는, 이미지가 서피스뷰 가로길이에 변경된 비율에 맞게 계산하여 조정하기
                    // 이미지의 가로 길이가 서피스뷰의 가로길이보다 같거나 클 경우
                    double width_transform_scale = 0;
                    if(document_bitmap_width >= surfaceView_width) {
                        width_transform_scale = ((double)document_bitmap_width/(double) surfaceView_width);
                        newHeight = (int)((double)document_bitmap_height/width_transform_scale);
                    }
                    // 이미지의 가로 길이가 서피스뷰의 가로길이보다 작을 경우
                    else if(document_bitmap_width < surfaceView_width) {
                        width_transform_scale = ((double) surfaceView_width /(double)document_bitmap_width);
                        newHeight = (int)((double)document_bitmap_height*width_transform_scale);
                    }
                    Logger.d("width_transform_scale: " + width_transform_scale + "\n" +
                            "newWidth: " + newWidth + "\n" +
                            "newHeight: " + newHeight + "\n" +
                            "background_new_scale: " + (double)newHeight/(double)newWidth + "\n"
                    );
                    surfaceView_fill_width = true;

                    scaled = Bitmap.createScaledBitmap(document_bitmap, newWidth, newHeight, true);

                    // 최초 서피스뷰 그리기
                    call_draw_handler.sendEmptyMessage(2);

                    if (drawThread == null) {
                        drawThread = new DrawThread();
                        drawThread.start();
                    }
                }
            });
    }


    /**---------------------------------------------------------------------------
     콜백메소드 ==> SurfaceHolder.Callback -- surfaceChanged
     ---------------------------------------------------------------------------*/
    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        boolean isPortrait = width < height;
        if (isPortrait) {
            ratio = (double) EDGE_WIDTH / height;
        } else {
            ratio = (double) EDGE_WIDTH / width;
        }

        // 서피스 뷰 rect 생성
        surfaceView_rect = new Rect();
        surfaceView_rect.set(0, 0, surfaceView.getWidth(), surfaceView.getHeight());
//        Logger.d("surfaceView_rect.left: " + surfaceView_rect.left);
//        Logger.d("surfaceView_rect.top: " + surfaceView_rect.top);
//        Logger.d("surfaceView_rect.right: " + surfaceView_rect.right);
//        Logger.d("surfaceView_rect.bottom: " + surfaceView_rect.bottom);

        // crop 할 rect 생성
        crop_img_rect = new Rect();
        crop_img_rect.set(0, 0, surfaceView.getWidth(), surfaceView.getHeight());
//        Logger.d("crop_img_rect.left: " + crop_img_rect.left);
//        Logger.d("crop_img_rect.top: " + crop_img_rect.top);
//        Logger.d("crop_img_rect.right: " + crop_img_rect.right);
//        Logger.d("crop_img_rect.bottom: " + crop_img_rect.bottom);

    }


    /**---------------------------------------------------------------------------
     콜백메소드 ==> SurfaceHolder.Callback -- surfaceDestroyed
     ---------------------------------------------------------------------------*/
    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (drawThread != null) {
            drawThread.shutdown();
            drawThread = null;
        }
        ratio = -1;
    }


    /**---------------------------------------------------------------------------
     콜백메소드 ==> onClick
     ---------------------------------------------------------------------------*/
    @Override
    public void onClick(View v) {

    }


    /**---------------------------------------------------------------------------
     메소드 ==> 캔버스에 비트맵을 그리는 'draw()' 를 호출하는 핸들러, 생성
     ---------------------------------------------------------------------------*/
    @SuppressLint("HandlerLeak")
    public void draw_handler_create() {
        call_draw_handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                // 최초 서피스뷰를 그릴 때, 전달되는 메세지
                if(msg.what == 2) {
                    Logger.d("최초 서피스뷰를 그릴 때, 전달되는 메세지");
                    draw("surfaceCreated");
                }
                // realm 서버에 변동이 있음을 감지했을 때(waitForChange()), 전달되는 메세지
                else if(msg.what == 0) {
//                    Logger.d("realm 서버에 변동이 있음을 감지했을 때, ");
                    // 현재 realm Result 의 마지막 인덱스를 변수에 저장한다
                    if(results_main.size() > 0) {
                        last_realmResult_index = results_main.size()-1;
//                        Logger.d("현재 realm Result 의 마지막 인덱스: " + last_realmResult_index);
                    }
                    draw("realm");
                }
                // 줌인했을 때, 전달되는 메세지 - 사용 안함
                else if(msg.what == 1) {
                    Logger.d("줌인했을 때, 전달되는 메세지");
                    draw("zoom_in");
                }
                // 비트맵 저장 버튼 클릭됐을 때, 전달되는 메세지
                else if(msg.what == 3) {
                    Logger.d("비트맵 저장 버튼 클릭됐을 때, 전달되는 메세지");
                    draw("save");
                }
                // 이미지 드래그 버튼을 클릭했을 때, 전달되는 메세지
                else if(msg.what == 4) {
                    Logger.d("이미지 드래그 버튼을 클릭했을 때, 전달되는 메세지");
                    draw("drag");
                }
                // 그리기 전체 삭제 했을 때, 전달되는 메세지
                else if(msg.what == 5) {
                    Logger.d("그리기 전체 삭제 했을 때, 전달되는 메세지");
                    draw("wipeCanvas");
                }

            }
        };
    }


    /**---------------------------------------------------------------------------
     메소드 ==> request에 따라 맞는 비트맵 그려서 서피스뷰에 post 하는 메소드
     ---------------------------------------------------------------------------*/
    public void draw(String requestFrom) {
        // 'save' 시 사용할 변수들
        Bitmap bitmap_for_save = null;
        Canvas canvas_for_save = null;

        // 파일로 비트맵을 저장하라는 요청일 때, 비트맵, 캔버스 생성
        if(requestFrom.equals("save")) {
            bitmap_for_save = Bitmap.createBitmap(scaled.getWidth(), scaled.getHeight(), Bitmap.Config.ARGB_8888);
            canvas_for_save = new Canvas(bitmap_for_save);
        }
        // 문서(비트맵)을 이동시키는 요청일 때, 비트맵, 캔버스 생성
        else if(requestFrom.equals("drag") && bitmap_for_drag == null) {
            bitmap_for_drag = Bitmap.createBitmap(scaled.getWidth(), scaled.getHeight(), Bitmap.Config.ARGB_8888);
            canvas_for_drag = new Canvas(bitmap_for_drag);
        }
        // 모든 DrawPath, DrawPoint를 지우는 요청일 때, 드래그 할때 보여주는 비트맵 초기화(원본 복사해서 set)
        else if(requestFrom.equals("wipeCanvas")) {
            bitmap_for_drag = scaled.copy(Bitmap.Config.ARGB_8888, true);
            canvas_for_drag.setBitmap(bitmap_for_drag);
        }

        // 선을 그리 위해서, 서피스뷰의 Holder를 가져와서 lockCanvas 처리
        SurfaceHolder holder = surfaceView.getHolder();
        canvas_main = holder.lockCanvas();

        // 비트맵(배경) 그리기 - 원본에서 기기의 Width에 맞게 scaled 처리한 원본 비트맵임(draw 없음)
        canvas_main.drawBitmap(scaled, crop_img_rect, surfaceView_rect, null);

        // 비트맵 파일 저장요청이라면, 저장용 비트맵을 그릴 캔버스에 비트맵을 그린다
        if(requestFrom.equals("save")) {
            if (canvas_for_save != null) {
                canvas_for_save.drawBitmap(scaled, 0, 0, null);
            }
        }
        // 드래그 요청이라면, 드래그일 때 보여줄 캔버스에 비트맵을 그린다
        else if(requestFrom.equals("drag")) {
            canvas_for_drag.drawBitmap(scaled, 0, 0, null);
        }


        if(results_main != null) {
            /** realm 서버에 DrawPath가 하나라도 있다면 */
            if(results_main.size() > 0) {

                synchronized (holder) {
                    final Paint paint = new Paint();

                    for(int i=0; i<results_main.size(); i++) {

                        final RealmList<DrawPoint> points = results_main.get(i).getPoints();
                        final Integer color = nameToColorMap.get(results_main.get(i).getColor());
                        final int strokeWidth = results_main.get(i).getStrokeWidth();
                        final int strokeAlpha = results_main.get(i).getStrokeAlpha();

                        // 색상 정보 찾아와서 적용
                        if (color != null) {
                            paint.setColor(color);
                        }
                        // 색상 정보 못찾을 시에 디폴트색 적용
                        else {
                            paint.setColor(nameToColorMap.get(currentColor));
                        }
                        // 선 종류
                        paint.setStyle(Paint.Style.STROKE);
                        // 선 두께
                        paint.setStrokeWidth((float) (strokeWidth / ratio));
                        // 선 투명도
                        paint.setAlpha(strokeAlpha);

                        final Iterator<DrawPoint> iterator = points.iterator();
                        final DrawPoint firstPoint = iterator.next();

                        float firstX = (float) ((firstPoint.getX() / ratio));
                        float firstY = (float) ((firstPoint.getY() / ratio));
//                        Logger.d("firstX: " + firstX + "\nfirstY:" + firstY);

                        // 화면에 그릴 Path
                        Path path = null;
                        // 비트맵으로 저장할 Path
                        Path path_for_save = null;

                        // 현재 화면이 드래그되어, 서피스 뷰의 top_y 값이 0이 아닐 때,
                        // drawPath의 y값에서 빼줘야할 top_height값 변수 선언
                        float subtract_Top_y = 0;

                        // DrawPath 가 변경이 있을때 or 서피스뷰를 처음 그릴 때
                        if(requestFrom.equals("realm") || requestFrom.equals("surfaceCreated")) {
                            path = new Path();

                            if(last_realmResult_index > -1 && i <= last_realmResult_index) {
                                subtract_Top_y = subtract_Top_y + ((float)last_top_y_when_action_up - subtract_Top_y);
                            }
                            else if(last_realmResult_index > -1 && i > last_realmResult_index) {
                                subtract_Top_y = (float)firstPoint.getTop_y();
                            }
                            /** 현재 화면이 crop 하고 있는 뷰의 Y값에 맞게 화면에 그려져야 하기 때문에
                             getTop_y 값을 빼줌 */
                            path.moveTo(firstX, firstY - subtract_Top_y);
                        }

                        // 저장요청일 때 or 배경을 움직일 때
                        else if((requestFrom.equals("save") || (requestFrom.equals("drag")))
                                && surfaceView_fill_width) {
                            path_for_save = new Path();
                            path_for_save.moveTo(firstX, firstY);
                        }

                        // DrawPoint 의 개수대로 path를 그린다
                        while(iterator.hasNext()) {
                            DrawPoint point = iterator.next();
                            float x = (float) ((point.getX() / ratio));
                            float y = (float) ((point.getY() / ratio));

                            if(requestFrom.equals("realm") || requestFrom.equals("surfaceCreated")) {
                                /** 현재 화면이 crop 하고 있는 뷰의 Y값에 맞게 화면에 그려져야 하기 때문에
                                 getTop_y 값을 빼줌 */
                                path.lineTo(x, y - subtract_Top_y);
                            }
                            // 가로가 꽉찬 상태라면 ==> - point_Y
                            else if((requestFrom.equals("save") || (requestFrom.equals("drag")))
                                    && surfaceView_fill_width) {
                                path_for_save.lineTo(x, y);
                            }
                        }

                        // DrawPath 가 변경이 있을때 or 서피스뷰를 처음 그릴 때 (즉, 배경 이동중이 아닐 때)
                        // 메인 켄버스에 Path를 그린다
                        if(requestFrom.equals("realm") || requestFrom.equals("surfaceCreated")) {
                            canvas_main.drawPath(path, paint);
                        }

                        // 만약 비트맵 저장요청이라면, 저장용 캔버스에 Path를 그린다
                        if(requestFrom.equals("save")) {
                            if (canvas_for_save != null) {
                                canvas_for_save.drawPath(path_for_save, paint);
                            }
                        }
                        // 드래그 요청이라면, 드래그할 때 보여주는 캔버스에 Path를 그린다
                        else if(requestFrom.equals("drag")) {
                            canvas_for_drag.drawPath(path_for_save, paint);
                        }
                    }
                    if(requestFrom.equals("save")) {
                        // 비트맵으로 저장
                        String saveBitmaptoImage_result =
                                myapp.saveBitmaptoImage(bitmap_for_save,
                                                        "remoteMeeting",
                                                        "testBitmap_" + myapp.get_time("yyyyMMdd HH_mm_ss"));
                        Logger.d("saveBitmaptoImage_result: " + saveBitmaptoImage_result);
                        if (bitmap_for_save != null) {
                            bitmap_for_save.recycle();
                        }
                    }
                    // 드래그를 위한 비트맵을 메인 켄버스에 셋팅
                    else if(requestFrom.equals("drag")) {
                        // 다 그려진 비트맵으로 백그라운드 이미지 다시 셋팅
//                        canvas_main.drawBitmap(bitmap_for_drag, point_X, point_Y, null);
                        canvas_main.drawBitmap(bitmap_for_drag, crop_img_rect, surfaceView_rect, null);
                        Toast toast = Toast.makeText(this, "다 그려진 비트맵으로 백그라운드 이미지 다시 셋팅", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
            }
            /** realm 서버에 DrawPath가 하나도 없다면 */
            else if(results_main.size() == 0) {
                // 메인켄버스에 scaled 된 배경 원본 비트맵을 셋팅한다
                if(requestFrom.equals("drag")) {
                    canvas_main.drawBitmap(scaled, crop_img_rect, surfaceView_rect, null);
                }
            }
        }
        // 서피스뷰에 캔버스 unlock 하고 post
        surfaceView.getHolder().unlockCanvasAndPost(canvas_main);
    }


    /**---------------------------------------------------------------------------
     클래스 ==> realm object server 와 연결 뒤, 서버의 'DrawPath' 테이블이 변경 될때마다
                콜백을 주기 위한 쓰레드 클래스
     ---------------------------------------------------------------------------*/
    class DrawThread extends Thread {

        private Realm bgRealm;

        public void shutdown() {
            synchronized(this) {
                if (bgRealm != null) {
                    bgRealm.stopWaitForChange();
                }
            }
            interrupt();
        }

        @Override
        public void run() {
            while (ratio < 0 && !isInterrupted()) {
                Logger.d("ratio < 0 && !isInterrupted()_1");
            }

            if (isInterrupted()) {
                return;
            }

            while (realm == null && !isInterrupted()) {
//                Logger.d("ratio < 0 && !isInterrupted()_2");
            }

            if (isInterrupted()) {
                return;
            }

            bgRealm = Realm.getDefaultInstance();

            while (!isInterrupted()) {

                call_draw_handler.sendEmptyMessage(0);
                bgRealm.waitForChange();

            }

            synchronized(this) {
                bgRealm.close();
            }
        }
    }


    /**---------------------------------------------------------------------------
     메소드 ==> realm 서버의 drawPath, drawPoint를 모두 지우는 메소드
     ---------------------------------------------------------------------------*/
    public void wipeCanvas() {
        if(realm != null) {
            realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm r) {
                    r.deleteAll();

                    // realmResult 마지막 인덱스 값 초기화
                    last_realmResult_index = -1;

                    // draw("wipeCanvas") 호출
                    call_draw_handler.sendEmptyMessage(5);
                }
            });
        }
    }


    /**---------------------------------------------------------------------------
     콜백메소드 ==> onTouchEvent
      1) 화면을 터치하는 x,y 좌표 및 top_Y를 구해서 realm 서버에 insert 하는 로직
      2) 배경 문서 이미지를 드래그하는 로직
     ---------------------------------------------------------------------------*/
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if(realm == null) {
            return false; // if we are in the middle of a rotation, realm may be null.
        }

        int action = event.getAction();

        // 좌표의 정확한 위치를 위해,
        // 서피스뷰 사각형의 왼쪽 상단점을 0,0으로 두기 위한 좌표 계산에 쓰이는 값
        int[] viewLocation = new int[2];
        surfaceView.getLocationInWindow(viewLocation);

        if (action == MotionEvent.ACTION_DOWN
                || action == MotionEvent.ACTION_MOVE
                || action == MotionEvent.ACTION_UP
                || action == MotionEvent.ACTION_CANCEL) {
            float x = event.getX();
            float y = event.getY();

            // X 좌표
            double pointX = (x - viewLocation[0]) * ratio;
            // Y 좌표
            /** ##중요##
             현재 보고 있는 이미지의 부분에 맞는 Y 값을 더해줌 (temp_save_top_y) */
            double pointY = (y - viewLocation[1] + temp_save_top_y) * ratio;

            // 화면 이동중 모드가 아니고, 손가락 하나도 터치 했을 때
            if(!on_moving && event.getPointerCount() == 1) {

                /** 화면에 최초 터치 했을 때 */
                if (action == MotionEvent.ACTION_DOWN) {
                    // realm 트랜잭션 준비
                    realm.beginTransaction();

                    // DrawPath를 realm 객체로 만들어서 변수값 넣기
                    currentPath = realm.createObject(DrawPath.class);
                    currentPath.setColor(currentColor);
                    currentPath.setUser_id(myapp.getUser_no());
                    currentPath.setStrokeWidth(stroke);
                    currentPath.setStrokeAlpha(alpha);

                    // DrawPoint를 realm 객체로 만들어서 변수값 넣기
                    DrawPoint point = realm.createObject(DrawPoint.class);
                    point.setX(pointX);
                    point.setY(pointY);
                    point.setTop_y(temp_save_top_y);

                    // DrawPoint를 DrawPoint의 RealmList에 add 하기
                    currentPath.getPoints().add(point);

                    // realm 트랜잭션 실행
                    realm.commitTransaction();
                }

                /** 화면에서 터치한 상태로 Move할 때 */
                else if (action == MotionEvent.ACTION_MOVE) {
                    // realm 트랜잭션 준비
                    realm.beginTransaction();

                    // DrawPoint를 realm 객체로 만들어서 변수값 넣기
                    DrawPoint point = realm.createObject(DrawPoint.class);
                    point.setX(pointX);
                    point.setY(pointY);
                    point.setTop_y(temp_save_top_y);

                    // DrawPoint를 DrawPoint의 RealmList에 add 하기
                    currentPath.getPoints().add(point);

                    // realm 트랜잭션 실행
                    realm.commitTransaction();
                }

                /** 터치 중이다가, 화면에서 손가락을 땠을 때 */
                else if (action == MotionEvent.ACTION_UP) {
                    // realm 트랜잭션 준비
                    realm.beginTransaction();

                    // completed true로 설정하여 해당 DrawPath는 드로잉이 끝났음을 표시
                    currentPath.setCompleted(true);

                    // DrawPoint를 realm 객체로 만들어서 변수값 넣기
                    DrawPoint point = realm.createObject(DrawPoint.class);
                    point.setX(pointX);
                    point.setY(pointY);
                    point.setTop_y(temp_save_top_y);

                    // DrawPoint를 DrawPoint의 RealmList에 add 하기
                    currentPath.getPoints().add(point);

                    // realm 트랜잭션 실행
                    realm.commitTransaction();

                    // currentPath null 처리
                    currentPath = null;
                }

                // 예외사항 - completed 여부는 무조건 true 되게끔 처리
                else {
                    realm.beginTransaction();
                    currentPath.setCompleted(true);
                    realm.commitTransaction();
                    currentPath = null;
                }
                return true;
            }

            // 드래그 모드일 때
            if(on_moving) {
                // 처음 클릭했을 때 좌표 구하기
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    saveX = event.getX();
                    saveY = event.getY();
                }
                // 이동중일 때, 이동한 좌표와 처음 클릭 좌표간의 거리를 구해서 이동한 x, y 거리 구하기
                else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    moveX = event.getX();
                    moveY = event.getY();
                    diffX = saveX - moveX;
                    diffY = saveY - moveY;

                    // 배경으로 있는 문서 이미지를 움직이기 위해,
                    // 이동한 y 값을 거리를 매개변수로 하는내부 메소드 호출
                    drag_back_img(diffY);
                }
                else if (action == MotionEvent.ACTION_UP) {
                    // 마지막 손가락을 땠을 때의 top_y 값으로,
                    // 배경으로 있는 문서 이미지를 움직이기 위한 내부 메소드 호출
                    save_last_top_y();
                }
                return true;
            }
        }
        return false;
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 드래그 모드에서, 손가락으로 화면을 터치하여 이동한
               y 값을 거리를 가지고 서피스뷰를 움직여서 드래그한 방향으로
               문서 이미지(비트맵)을 세로로 스크롤효과를 내는 메소드
     ---------------------------------------------------------------------------*/
    public void drag_back_img(float move_Y) {
        // 이동한 거리값에 따라 top_y, bottom_y 값을 계산
        top_y = (int)((float)last_top_y_when_action_up + move_Y);
        bottom_y = top_y + surfaceView_height;

        // top_y 값이 최소값(0)보다 크거나 같고,
        // 최대값(이미지 height에서 서피스뷰 height를 뺀 값) 보다 작거나 같을 때
        // == > 이 때만 뷰를 다시 그려 드래그 효과를 낸다
        // * 참고: 가로가 꽉찬 화면이기 때문에 Y 값만 변한다
        if(top_y >= 0 && top_y <= (newHeight-surfaceView_height)) {
            SurfaceHolder holder = surfaceView.getHolder();
            Canvas canvas = holder.lockCanvas();

            // crop 할 범위를 정한다
            crop_img_rect.set(0, top_y, scaled.getWidth(), bottom_y);

            // crop 할 rect를 적용하여 비트맵을 캔버스에 그린다
            canvas.drawBitmap(bitmap_for_drag, crop_img_rect, surfaceView_rect, null);

            surfaceView.getHolder().unlockCanvasAndPost(canvas);
        }
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 마지막 드래그 하는 손가락을 땠을 때 top_y를 기준으로
               문서 이미지(비트맵)을 세로로 스크롤효과를 내는 메소드

     # action_up일 때 이 메소드를 구현하는 이유
      : 드래그 하는 손가락에 대응하는 dragBitmap을 충분히 빨리 생성하지 못해서,
        이동한 y값에 따라 드래그가 완전하게 이루어지지 않는 경우가 발생했음
     ---------------------------------------------------------------------------*/
    private void save_last_top_y() {
        // top_y이 0보다 작을 경우, 시작점인 0으로 설정
        if(top_y < 0) {
            temp_save_top_y = 0;
        }
        // top_y이 최대값(서피스뷰 height를 뺀 값)보다 클 경우, 최대값으로 설정
        else if(top_y > (newHeight-surfaceView_height)) {
            temp_save_top_y = newHeight-surfaceView_height;
        }
        // 위 경우를 제외하면 top_y 값 그대로를 temp_save_top_y 값에 넣는다
        else {
            temp_save_top_y = top_y;
        }
        // temp_save_top_y 값에 따른 bottom_y 값을 계산
        bottom_y = temp_save_top_y + surfaceView_height;

        // 락 캔버스
        SurfaceHolder holder = surfaceView.getHolder();
        Canvas canvas = holder.lockCanvas();

        // crop 할 범위를 정한다
        crop_img_rect.set(0, temp_save_top_y, scaled.getWidth(), bottom_y);

        // crop 할 rect를 적용하여 비트맵을 캔버스에 그린다
        canvas.drawBitmap(bitmap_for_drag, crop_img_rect, surfaceView_rect, null);

        // 언락캔버스 & 포스트
        surfaceView.getHolder().unlockCanvasAndPost(canvas);

        // temp_save_top_y 값을 action_up 했을 때 마지막 top_y 값에 넣는다
        last_top_y_when_action_up = temp_save_top_y;
        Logger.d("---last_top_y_when_action_up: " + last_top_y_when_action_up);
    }


    /**---------------------------------------------------------------------------
     메소드 ==> drawing 할 때 고를 수 있는 색을 String 값과 매칭 시키는 메소드
     ---------------------------------------------------------------------------*/
    private void generateColorMap() {
        nameToColorMap.put("Charcoal", 0xff1c283f);
//        nameToColorMap.put("Elephant", 0xff9a9ba5);
//        nameToColorMap.put("Dove", 0xffebebf2);
//        nameToColorMap.put("Ultramarine", 0xff39477f);
//        nameToColorMap.put("Indigo", 0xff59569e);
//        nameToColorMap.put("GrapeJelly", 0xff9a50a5);
//        nameToColorMap.put("Mulberry", 0xffd34ca3);
//        nameToColorMap.put("Flamingo", 0xfffe5192);
//        nameToColorMap.put("SexySalmon", 0xfff77c88);
//        nameToColorMap.put("Peach", 0xfffc9f95);
//        nameToColorMap.put("Melon", 0xfffcc397);

//        colorIdToName.put(R.id.charcoal, "Charcoal");
//        colorIdToName.put(R.id.elephant, "Elephant");
//        colorIdToName.put(R.id.dove, "Dove");
//        colorIdToName.put(R.id.ultramarine, "Ultramarine");
//        colorIdToName.put(R.id.indigo, "Indigo");
//        colorIdToName.put(R.id.grape_jelly, "GrapeJelly");
//        colorIdToName.put(R.id.mulberry, "Mulberry");
//        colorIdToName.put(R.id.flamingo, "Flamingo");
//        colorIdToName.put(R.id.sexy_salmon, "SexySalmon");
//        colorIdToName.put(R.id.peach, "Peach");
//        colorIdToName.put(R.id.melon, "Melon");
    }
}