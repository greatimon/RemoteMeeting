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
import android.graphics.Color;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.example.jyn.remotemeeting.DataClass.Data_for_netty;
import com.example.jyn.remotemeeting.Dialog.Out_confirm_D;
import com.example.jyn.remotemeeting.Etc.Static;
import com.example.jyn.remotemeeting.Fragment.Call_F;
import com.example.jyn.remotemeeting.Fragment.Hud_F;
import com.example.jyn.remotemeeting.Otto.BusProvider;
import com.example.jyn.remotemeeting.Otto.Event;
import com.example.jyn.remotemeeting.R;
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
import java.util.List;
import java.util.Random;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.glide.transformations.CropCircleTransformation;
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
public class Call_A extends Activity implements AppRTCClient.SignalingEvents,
                                                PeerConnectionClient.PeerConnectionEvents,
                                                Call_F.OnCallEvents {

    private static final String TAG = "all_" + Call_A.class.getSimpleName();
    private static final int CAPTURE_PERMISSION_REQUEST_CODE = 1;
    public static int REQUEST_CONFIRM_UPLOAD_FILES = 1220;
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

        // 어플리케이션 객체 생성
        myapp = Myapp.getInstance();

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
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "화면 클릭");
//                toggleCallControlFragmentVisibility();
            }
        };

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

        fullscreenRenderer.setOnClickListener(listener);
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

                    String[] temp = received_data.getExtra().split(Static.SPLIT);

                    // 뷰 조절 메소드 호출
                    when_video_status_change(
                            Boolean.valueOf(temp[0]), !pipRenderer.isClickable(), temp[1]);
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

        // 퍼미션?
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
}