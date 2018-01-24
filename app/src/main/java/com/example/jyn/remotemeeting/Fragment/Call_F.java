package com.example.jyn.remotemeeting.Fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.jyn.remotemeeting.Activity.Call_A;
import com.example.jyn.remotemeeting.Adapter.RCV_call_adapter;
import com.example.jyn.remotemeeting.Adapter.RCV_selectFile_preview_adapter;
import com.example.jyn.remotemeeting.DataClass.File_info;
import com.example.jyn.remotemeeting.DataClass.Preview_selected_file;
import com.example.jyn.remotemeeting.DataClass.Users;
import com.example.jyn.remotemeeting.Dialog.Confirm_upload_files_D;
import com.example.jyn.remotemeeting.Etc.Static;
import com.example.jyn.remotemeeting.Otto.BusProvider;
import com.example.jyn.remotemeeting.Otto.Event;
import com.example.jyn.remotemeeting.R;
import com.example.jyn.remotemeeting.Recycler_helper.OnStartDragListener;
import com.example.jyn.remotemeeting.Recycler_helper.SimpleItemTouchHelperCallback;
import com.example.jyn.remotemeeting.Util.File_search;
import com.example.jyn.remotemeeting.Util.Myapp;
import com.example.jyn.remotemeeting.Util.SimpleDividerItemDecoration;
import com.example.jyn.remotemeeting.WebRTC.CaptureQualityController;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.pnikosis.materialishprogress.ProgressWheel;
import com.squareup.otto.Subscribe;

import org.webrtc.RendererCommon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.wasabeef.glide.transformations.CropCircleTransformation;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by JYN on 2017-11-10.
 */

public class Call_F extends Fragment  implements PhotoViewAttacher.OnViewTapListener, OnStartDragListener {

    private LinearLayout exit_LIN;
    private RelativeLayout popup_menu_REL;
    private View controlView;
    private TextView contactView;
    private ImageView cameraSwitchButton;
    private ImageView mic_on_show_IV;
    private ImageView mic_off_show_IV;
    private ImageView popup_menu_icon;
    private TextView captureFormatText;
    private TextView cameraSwitchText;
    private SeekBar captureFormatSlider;
    private OnCallEvents callEvents;
    private RendererCommon.ScalingType scalingType;
    private boolean videoCallEnabled = true;
    Myapp myapp;
    private int total_pdf_files_count = -1;
    private int current_sequence = -1;
    private String on_converting_filename;

    private static final String TAG = "all_"+Call_F.class.getSimpleName();

    //// 리사이클러뷰 관련 클래스
    // 회의 파일함 파일리스트 리사이클러뷰
    public RCV_call_adapter rcv_call_adapter_project;
    // 로컬파일, 파일리스트 리사이클러뷰
    public RCV_call_adapter rcv_call_adapter_local;
    // 리사이클러뷰 레이아웃매니저_1
    private RecyclerView.LayoutManager layoutManager;

    // 미리보기, 파일리스트 리사이클러뷰
    public RCV_selectFile_preview_adapter rcv_selectFile_preview_adapter;
    // 리사이클러뷰 레이아웃매니저_2
    private RecyclerView.LayoutManager layoutManager_for_preview;

    // 미리보기 화면으로 넘어갈 때, 미리보기할 파일 arr의 첫번째 파일의 이름을 담을 변수 선언
    String first_fileName = "";

    // 리사이클러뷰 아이템 터치와 관련된 클래스
    private ItemTouchHelper itemTouchHelper;

    /** 버터나이프 뷰 찾기*/
    public Unbinder unbinder;
    @BindView(R.id.popup_file_manager)      public RelativeLayout popup_file_manager_REL;
    @BindView(R.id.preview_REL)             public RelativeLayout preview_REL;
    @BindView(R.id.circularProgressbar_REL) public RelativeLayout circularProgressbar_REL;
    @BindView(R.id.close_popup)             public RelativeLayout close_popup;
    @BindView(R.id.recyclerView)            public RecyclerView recyclerView;
    @BindView(R.id.recyclerView_preview)    public RecyclerView recyclerView_preview;
    @BindView(R.id.back_to_menu)            public ImageView back_to_menu;
    @BindView(R.id.preview)                 public ImageView preview;
    @BindView(R.id.button_call_toggle_video)public ImageView button_call_toggle_video;
    @BindView(R.id.button_call_toggle_mic)  public ImageView button_call_toggle_mic;
    @BindView(R.id.button_call_face_rec)    public ImageView button_call_face_rec;
    @BindView(R.id.video_on_show)           public ImageView video_on_show;
    @BindView(R.id.video_off_show)          public ImageView video_off_show;
    @BindView(R.id.profile_img)             public ImageView subject_profile_img;
    @BindView(R.id.go_share)                public ImageView go_share;
    @BindView(R.id.sequence)                public TextView sequence;
    @BindView(R.id.file_name)               public TextView file_name;
    @BindView(R.id.percent)                 public TextView percent;
    @BindView(R.id.page_status)             public TextView page_status;
    @BindView(R.id.text_call_toggle_video)  public TextView text_call_toggle_video;
    @BindView(R.id.text_call_toggle_mic)    public TextView text_call_toggle_mic;
    @BindView(R.id.text_call_face_rec)      public TextView text_call_face_rec;
    @BindView(R.id.nickName)                public TextView subject_nickName;

    public static CircularProgressBar circularProgressBar;
    @SuppressLint("StaticFieldLeak")
    public static ImageView add_files;
    @SuppressLint("StaticFieldLeak")
    public static TextView comment;
    @SuppressLint("StaticFieldLeak")
//    public static ImageView preview_display;
    public static PhotoView preview_display;
    @SuppressLint("StaticFieldLeak")
    public static TextView file_box_title;

    public ProgressWheel progress_wheel;

    // '미리보기' 담당 리사이클러뷰 어댑터에 넘겨줄 어레이리스트
    ArrayList<Preview_selected_file> preview_file_arr;

    // Call_A 로 부터, 뷰 조절을 하라는 메세지를 전달받을 핸들러
    public static Handler visibility_control_handler;

    // faceTracking 테스트용 변수
    boolean faceTracking_enable = false;


    /**---------------------------------------------------------------------------
     콜백메소드 ==> 포토뷰 탭, 콜백
     ---------------------------------------------------------------------------*/
    @Override
    public void onViewTap(View view, float x, float y) {
        Log.d(TAG, "포토뷰 탭!");
    }

    /**
     * Call control interface for container activity.
     */
    public interface OnCallEvents {
        void onCallHangUp();
        void onCameraSwitch();
        void onVideoScalingSwitch(RendererCommon.ScalingType scalingType);
        void onCaptureFormatChange(int width, int height, int framerate);
        boolean onToggleMic();
        boolean onToggleVideo();
    }


    /**---------------------------------------------------------------------------
     생명주기 ==> onCreateView
     ---------------------------------------------------------------------------*/
    @SuppressLint("HandlerLeak")
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        controlView = inflater.inflate(R.layout.f_call, container, false);
        // 버터나이프 바인드
        unbinder = ButterKnife.bind(this, controlView);

        // Create UI controls.
//        contactView = controlView.findViewById(R.id.contact_name_call);
        exit_LIN = controlView.findViewById(R.id.button_call_disconnect);
        cameraSwitchButton = controlView.findViewById(R.id.button_call_switch_camera);
        cameraSwitchText = controlView.findViewById(R.id.Textview_call_switch_camera);
//        videoScalingButton = controlView.findViewById(R.id.button_call_scaling_mode);
        captureFormatText = controlView.findViewById(R.id.capture_format_text_call);
        captureFormatSlider = controlView.findViewById(R.id.capture_format_slider_call);
        popup_menu_icon = controlView.findViewById(R.id.popup_menu_icon);
        popup_menu_REL = controlView.findViewById(R.id.popup_menu);
        mic_on_show_IV = controlView.findViewById(R.id.mic_on_show);
        mic_off_show_IV = controlView.findViewById(R.id.mic_off_show);
        add_files = controlView.findViewById(R.id.add_files);
        circularProgressBar = controlView.findViewById(R.id.circularProgressbar);
        progress_wheel = controlView.findViewById(R.id.progress_wheel);
        comment = controlView.findViewById(R.id.comment);
        preview_display = controlView.findViewById(R.id.preview_display);
        file_box_title = controlView.findViewById(R.id.file_box_title);

        // progress_wheel 설정
        progress_wheel.setBarColor(Color.parseColor("#4CAF50"));
        progress_wheel.setSpinSpeed(0.7f);
        progress_wheel.setBarWidth(5);

        // otto 등록
        BusProvider.getBus().register(this);

        // 어플리케이션 객체 생성
        myapp = Myapp.getInstance();

        // 회의 상대 프로필사진, 닉네임 표시 메소드 호출
        set_subject_user_data();

        // 핸들러 생성
        visibility_control_handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                // Call_A 로 부터 전달받는 핸들러 메세지
                // 오픈되어 있는 뷰들을 GONE 처리 해라
                if(msg.what == 0) {
                    popup_file_manager_REL.setVisibility(View.GONE);
                    close_popup.setVisibility(View.GONE);
                    preview_REL.setVisibility(View.GONE);
                    popup_menu_REL.setVisibility(View.GONE);
                    popup_menu_icon.setVisibility(View.GONE);
                }
                // Call_A 로 부터 전달받는 핸들러 메세지
                // 팝업 메뉴만 VISIBLE 처리 해라 - 뷰 초기화
                else if(msg.what == 1) {
                    popup_menu_icon.setVisibility(View.VISIBLE);
                }

                // 문서 공유 모드를 진행하기 전에, 현재 비디오 전송 모드를 확인해서
                // video_on_off() 메소드를 호출해서
                // 만약에 비디오 전송모드가 on이면, off로 하고 / 백업뷰를 VISIBLE 처리한다
                // 만약에 비디오 전송모드가 off이면, 바로 call_A에게 이미지 공유 모드를 진행하라고 알린다
                else if(msg.what == 2) {
                    if(Call_A.videoEnabled) {
                        // 어플리케이션 객체(myapp)에, 비디오 전송모드가 원래 on 이었음을 저장
                        myapp.setVideo_state_was(true);

                        video_on_off(Call_A.image_share_REL);
                    }
                    else if(!Call_A.videoEnabled) {
                        // 어플리케이션 객체(myapp)에, 비디오 전송모드가 원래 off 이었음을 저장
                        myapp.setVideo_state_was(false);

                        Call_A.webrtc_message_handler.sendEmptyMessage(2);
                    }
                }

                // from: Call_A
                // 이미지 공유 모드로 전환하기 직전의 비디오 전송 모드를 확인해서, 거기에 맞게
                // video_on_off() 메소드를 호출해서
                // 만약에 비디오 전송모드가 on이었으면, 다시 on으로 복구하고 / 백업뷰를 GONE 처리한다
                // 만약에 비디오 전송모드가 off었으면, 다시 off로 복구하고 / 백업뷰를 VISIBLE 처리 한다
                else if(msg.what == 3) {
                    if(myapp.isVideo_state_was()) {
                        video_on_off(Call_A.image_share_REL);
                    }
                    else if(!myapp.isVideo_state_was()) {
                        // 비디오 모드를 원래 off였으니, 이미지 공유 모드를 종료하라는 메세지 전달
                        Call_A.webrtc_message_handler.sendEmptyMessage(3);
                    }

                }
            }
        };


        /** 리사이클러뷰 - 프로젝트 파일 */
        recyclerView.setHasFixedSize(true);
        // 리사이클러뷰 - GridLayoutManager 사용
        layoutManager = new GridLayoutManager(getActivity().getApplicationContext(), 3);
        recyclerView.setLayoutManager(layoutManager);
        // 리사이클러뷰 에니메이션 설정
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        /** 미리보기 리사이클러뷰 */
        recyclerView_preview.setHasFixedSize(true);
        // 리사이클러뷰 - LinearLayoutManager 사용
        layoutManager_for_preview = new LinearLayoutManager(getActivity());
        recyclerView_preview.setLayoutManager(layoutManager_for_preview);
        // 리사이클러뷰 구분선 - 가로(클래스 생성)
        recyclerView_preview.addItemDecoration(new SimpleDividerItemDecoration(getActivity(), "Call_F"));
        // 리사이클러뷰 기본 애니메이션 설정
//        recyclerView_preview.setItemAnimator(new DefaultItemAnimator());
        // 애니메이션 설정 - 애니메이션 설정 끔
        ((SimpleItemAnimator)recyclerView_preview.getItemAnimator()).setSupportsChangeAnimations(false);


        /**---------------------------------------------------------------------------
         클릭이벤트 ==> 통화 종료확인 요청 -- static Handler 이용
         ---------------------------------------------------------------------------*/
        exit_LIN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                callEvents.onCallHangUp();
                Call_A.hangup_confirm.sendEmptyMessage(1);
            }
        });


        /**---------------------------------------------------------------------------
         클릭이벤트 ==> 카메라 플립
         ---------------------------------------------------------------------------*/
        cameraSwitchText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callEvents.onCameraSwitch();
            }
        });
        cameraSwitchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callEvents.onCameraSwitch();
            }
        });

//        videoScalingButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (scalingType == RendererCommon.ScalingType.SCALE_ASPECT_FILL) {
//                    videoScalingButton.setBackgroundResource(R.drawable.ic_action_full_screen);
//                    scalingType = RendererCommon.ScalingType.SCALE_ASPECT_FIT;
//                } else {
//                    videoScalingButton.setBackgroundResource(R.drawable.ic_action_return_from_full_screen);
//                    scalingType = RendererCommon.ScalingType.SCALE_ASPECT_FILL;
//                }
//                callEvents.onVideoScalingSwitch(scalingType);
//            }
//        });
        scalingType = RendererCommon.ScalingType.SCALE_ASPECT_FILL;


        /**---------------------------------------------------------------------------
         클릭이벤트 ==> 팝업 메뉴 VISIBLE, GONE
         ---------------------------------------------------------------------------*/
        popup_menu_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (popup_menu_REL.getVisibility()) {
                    case View.GONE:
                        popup_menu_REL.setVisibility(View.VISIBLE);
                        popup_file_manager_REL.setVisibility(View.GONE);
                        break;
                    case View.VISIBLE:
                        popup_menu_REL.setVisibility(View.GONE);
                        popup_file_manager_REL.setVisibility(View.GONE);
                        break;
                }
            }
        });


        /**---------------------------------------------------------------------------
         클릭리스너 ==> PhotoView 탭(클릭) 리스너, 스케일체인지 리너스 등록
         ---------------------------------------------------------------------------*/
        final PhotoViewAttacher mAttacher = new PhotoViewAttacher(preview_display);
        mAttacher.setScale(1.0f);
        mAttacher.setOnViewTapListener(this);
        mAttacher.setOnScaleChangeListener(new PhotoViewAttacher.OnScaleChangeListener() {
            @Override
            public void onScaleChange(float scaleFactor, float focusX, float focusY) {
                if(scaleFactor >= 1.0f) {
                    mAttacher.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                }
            }
        });

        return controlView;
    }


    /**---------------------------------------------------------------------------
     생명주기 ==> onStart
     ---------------------------------------------------------------------------*/
    @Override
    public void onStart() {
        super.onStart();

        boolean captureSliderEnabled = false;
        Bundle args = getArguments();

        if (args != null) {
            String contactName = args.getString(Static.EXTRA_ROOMID);
//            contactView.setText(contactName);fileFormat:
            videoCallEnabled = args.getBoolean(Static.EXTRA_VIDEO_CALL, true);
            captureSliderEnabled = videoCallEnabled
                    && args.getBoolean(Static.EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED, false);
        }

        if (!videoCallEnabled) {
            cameraSwitchButton.setVisibility(View.INVISIBLE);
        }

        if (captureSliderEnabled) {
            captureFormatSlider.setOnSeekBarChangeListener(
                    new CaptureQualityController(captureFormatText, callEvents));
        }

        else {
            captureFormatText.setVisibility(View.GONE);
            captureFormatSlider.setVisibility(View.GONE);
        }
    }


    /**---------------------------------------------------------------------------
     생명주기 ==> onAttach
     ---------------------------------------------------------------------------*/
    // TODO(sakal): Replace with onAttach(Context) once we only support API level 23+.
    // API 레벨 23 이상 만 지원하면 onAttach(Context)로 바꿉니다.
    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        callEvents = (OnCallEvents) activity;
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 회의 상대 프로필사진, 닉네임 표시
     ---------------------------------------------------------------------------*/
    public void set_subject_user_data() {
        Users subject_user = myapp.get_user_info(myapp.getMeeting_subject_user_no());

        Glide
            .with(getActivity())
            .load(Static.SERVER_URL_PROFILE_FILE_FOLDER + subject_user.getUser_img_filename())
            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
            .bitmapTransform(new CropCircleTransformation(getActivity()))
            .into(subject_profile_img);
        subject_nickName.setText(subject_user.getUser_nickname());

    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 회의 파일함 아이콘 클릭
     ---------------------------------------------------------------------------*/
    @OnClick({R.id.file_box_LIN})
    public void go_file_box() {
        // 회의 파일함 oepn 메소드 호출
        file_box();
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 얼굴인식 + 3d object 모드 On/off
     ---------------------------------------------------------------------------*/
    @OnClick({R.id.button_call_face_rec, R.id.text_call_face_rec})
    public void face_recognition_on_off(View v) {
        // 얼굴인식 + 3d object 모드를 on/off 할때 비디오 모드도 같이 on/off 하기 위해서 해당 메소드 호출
        video_on_off(button_call_face_rec);

        faceTracking_enable = !faceTracking_enable;
        Log.d(TAG, "face_recognition_enabled: " + faceTracking_enable);

        if(faceTracking_enable == false) {
            button_call_face_rec.setAlpha(0.3f);
        }
        else if(faceTracking_enable == true) {
            button_call_face_rec.setAlpha(1.0f);
        }
        // 얼굴인식 모드 on/off 메세지 전달
        Event.Call_F__Call_A_face_recognition call_f__call_a_face_recognition
                = new Event.Call_F__Call_A_face_recognition(faceTracking_enable);
        BusProvider.getBus().post(call_f__call_a_face_recognition);
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 파일 공유하기 아이콘 클릭
     ---------------------------------------------------------------------------*/
    @OnClick({R.id.go_share})
    public void go_share() {
        Log.d(TAG, "파일 공유하기 아이콘 클릭");

        if(RCV_selectFile_preview_adapter.selected_file_arr.size() == 0) {
            myapp.logAndToast("공유할 파일이 없습니다.");
        }

        else if(RCV_selectFile_preview_adapter.selected_file_arr.size() > 0) {
            // 파일 공유 모드 버튼 클릭되었으니,
            // 상대방에게 파일공유모드 요청을 보내' 라는 이벤트 메시지를 전달
            Event.Call_F__Call_A_file_share call_f__call_a_file_share
                    = new Event.Call_F__Call_A_file_share("go_share");

            BusProvider.getBus().post(call_f__call_a_file_share);
        }
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 오디오 on/off
     ---------------------------------------------------------------------------*/
    @OnClick({R.id.button_call_toggle_mic, R.id.text_call_toggle_mic})
    public void audio_on_off() {
        boolean enabled = callEvents.onToggleMic();
        Log.d(TAG, "toggleMuteButton_enabled: " + enabled);

        button_call_toggle_mic.setAlpha(enabled ? 1.0f : 0.3f);
        text_call_toggle_mic.setAlpha(enabled ? 1.0f : 0.3f);
        String test = String.valueOf(enabled);
        switch (test) {
            case "true":
                mic_off_show_IV.setVisibility(View.GONE);
                mic_on_show_IV.setVisibility(View.VISIBLE);
                break;
            case "false":
                mic_on_show_IV.setVisibility(View.GONE);
                mic_off_show_IV.setVisibility(View.VISIBLE);
                break;
        }
    }


    /**---------------------------------------------------------------------------
     // todo: 기능 테스트 버튼 - xml 문서에서 해당 버튼 주석처리하면, 얘도 반드시 주석처리하기
     클릭이벤트 ==> 테스트 버튼!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     ---------------------------------------------------------------------------*/
//    @OnClick(R.id.test_btn)
//    public void test_btn() {
//        video_on_off(popup_menu_icon);
//    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 비디오 on/off
     ---------------------------------------------------------------------------*/
    @OnClick({R.id.button_call_toggle_video, R.id.text_call_toggle_video})
    public void video_on_off(View view) {

//        if(view.getId() == R.id.button_call_toggle_video || view.getId() == R.id.text_call_toggle_video) {
//            Log.d(TAG, "원래 버튼 클릭을 통한 비디오 on/off 기능 실행");
//        }
//        else if(view.getId() == R.id.popup_menu_icon) {
//            Log.d(TAG, "테스트 버튼 클릭을 통한 비디오 on/off 기능 실행");
//            Log.d(TAG, "view.getId(): " + view.getId());
//        }

        /**
         버튼 클릭이 아닌,
         이미지 공유 모드 on,off 관련하여 이 메소드가 호출되었을 때
         */
        if(view.getId() == R.id.image_share_REL) {

            boolean enabled = callEvents.onToggleVideo();
            Log.d(TAG, "toggleVideoButton_enabled: " + enabled);

            button_call_toggle_video.setAlpha(enabled ? 1.0f : 0.3f);
            text_call_toggle_video.setAlpha(enabled ? 1.0f : 0.3f);
            String video_state = String.valueOf(enabled);
            switch (video_state) {
                case "true":
                    video_off_show.setVisibility(View.GONE);
                    video_on_show.setVisibility(View.VISIBLE);
                    // 비디오 전송 모드가 On 이니, 백업뷰를 끄라는 메세지 전달하기
                    Event.Call_F__Call_A call_f__call_a_on = new Event.Call_F__Call_A(enabled);
                    BusProvider.getBus().post(call_f__call_a_on);
                    break;
                case "false":
                    video_on_show.setVisibility(View.GONE);
                    video_off_show.setVisibility(View.VISIBLE);
                    // 비디오 전송 모드가 Off 이니, 백업뷰를 키라는 메세지 전달하기
                    Event.Call_F__Call_A call_f__call_a_off = new Event.Call_F__Call_A(enabled);
                    BusProvider.getBus().post(call_f__call_a_off);
                    break;
            }

            Log.d(TAG, "view.getVisibility(): " + String.valueOf(view.getVisibility()));
            // 이미지 공유 모드 off --> on
            if(view.getVisibility() == View.GONE) {
                // 비디오 모드를 off로 바꾸었으니, 이제 이미지 공유 모드를 진행하라는 메세지 전달
                Call_A.webrtc_message_handler.sendEmptyMessage(2);
            }
            else if(view.getVisibility() == View.VISIBLE) {
                // 비디오 모드를 on 으로 복구했으니, 이제 이미지 공유 모드를 종료하라는 메세지 전달
                Call_A.webrtc_message_handler.sendEmptyMessage(3);
            }
        }

        /** 비디오 on/off 토글 버튼으로 이 메소드가 호출되었을 때 */
        else  {
            boolean enabled = callEvents.onToggleVideo();
            Log.d(TAG, "toggleVideoButton_enabled: " + enabled);

            button_call_toggle_video.setAlpha(enabled ? 1.0f : 0.3f);
            text_call_toggle_video.setAlpha(enabled ? 1.0f : 0.3f);
            String video_state = String.valueOf(enabled);
            switch (video_state) {
                case "true":
                    video_off_show.setVisibility(View.GONE);
                    video_on_show.setVisibility(View.VISIBLE);
                    // 비디오 전송 모드가 On 이니, 백업뷰를 끄라는 메세지 전달하기
                    Event.Call_F__Call_A call_f__call_a_on = new Event.Call_F__Call_A(enabled);
                    BusProvider.getBus().post(call_f__call_a_on);
                    break;
                case "false":
                    video_on_show.setVisibility(View.GONE);
                    video_off_show.setVisibility(View.VISIBLE);
                    // 비디오 전송 모드가 Off 이니, 백업뷰를 키라는 메세지 전달하기
                    Event.Call_F__Call_A call_f__call_a_off = new Event.Call_F__Call_A(enabled);
                    BusProvider.getBus().post(call_f__call_a_off);
                    break;
            }
        }
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 회의 파일함 Open
     ---------------------------------------------------------------------------*/
    public void file_box() {
        // 체크된 파일 리스트를 담는 checked_files 해쉬맵 초기화
        // 업로드할 파일 리스트를 담는 init_files_for_upload 해쉬맵도 초기화
        myapp.init_checked_files();
        myapp.init_files_for_upload();

        // 리사이클러뷰 동작 메소드 호출
        activate_File_RCV("project", "");

        popup_menu_REL.setVisibility(View.GONE);
        popup_menu_icon.setVisibility(View.GONE);
        popup_file_manager_REL.setVisibility(View.VISIBLE);
        back_to_menu.setVisibility(View.VISIBLE);
        add_files.setVisibility(View.GONE);
        close_popup.setVisibility(View.VISIBLE);
        preview.setVisibility(View.VISIBLE);
        // 미리보기 로직 관련 뷰들 visibility 처리
        recyclerView.setVisibility(View.VISIBLE);
        preview_display.setVisibility(View.GONE);
        preview_REL.setVisibility(View.GONE);
        go_share.setVisibility(View.GONE);
        preview.setVisibility(View.VISIBLE);

        file_box_title.setText("회의 파일함");
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> back_img 클릭 -- 로컬 파일리스트 => 회의 파일함 or 회의 파일함 => 메뉴
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.back_to_menu)
    public void back_to_menu() {
        // 현재보고 있는 뷰가 project가 아니라면, 즉 로컬 파일리스트를 보고 있다면
        // 로컬뷰 가리고, 프로젝트뷰 보여주기
        String get_format = recyclerView.getAdapter().toString();
        if(!get_format.equals("project")) {
            // 회의 파일함 oepn 메소드 호출
            file_box();
        }
        // 현재있는 뷰가 project 라면, 메뉴뷰로 돌아가기
        else if(get_format.equals("project")) {
            // 체크된 파일 리스트를 담는 checked_files 해쉬맵 초기화
            // 업로드할 파일 리스트를 담는 init_files_for_upload 해쉬맵도 초기화
            myapp.init_checked_files();
            myapp.init_files_for_upload();

            popup_menu_REL.setVisibility(View.VISIBLE);
            popup_menu_icon.setVisibility(View.VISIBLE);
            popup_file_manager_REL.setVisibility(View.GONE);
            close_popup.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            preview_display.setVisibility(View.GONE);
            preview_REL.setVisibility(View.GONE);
            go_share.setVisibility(View.GONE);
            preview.setVisibility(View.VISIBLE);
        }
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 회의 파일함에서 선택한 파일들의 미리보기 모드
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.preview)
    public void selectFile_preview_mode() {

        if(myapp.getChecked_files().size() == 0) {
            myapp.logAndToast("선택한 파일이 없습니다.");
            return;
        }

        for(String key: myapp.getChecked_files().keySet()) {
            Log.d(TAG, "key: " + key + ", value: " + myapp.getChecked_files().get(key));
        }

        // 선택된 파일들, '미리보기' 담당 리사이클러뷰 어댑터로 넘기는, 클래스 내부 메소드 호출
        activate_preview_RCV("preview");

    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 서버 통신 -- 파일 업로드: 로컬파일함 => 회의 파일함으로 이동
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.add_files)
    public void upload_files(View view) {
        // todo: 클릭할 때 로직 구분 해야함 - 서버 파일 업로드 / 파일 화면 공유

        // 체크된 파일 중에서 pdf 파일이 있는지 확인
        // 해쉬맵의 키, 밸류 값 모두 file_name이기 때문에 key값만 가지고 판별
        HashMap<String,String> checked_files = myapp.getChecked_files();
        Iterator<String> iterator = checked_files.keySet().iterator();
        boolean have_pdf_ofNot = false;

        while(iterator.hasNext()) {
            String key = iterator.next();
            Log.d(TAG, "key: " + key);

            // 확장자만 분류
            int Idx = key.lastIndexOf(".");
            String format = key.substring(Idx+1);
            Log.d(TAG, "format: " + format);

            // 확장자 중에 PDF가 있다면, while 중지
            if(format.equals("pdf")) {
                have_pdf_ofNot = true;
                break;
            }
        }

        // 활성화되어 있는 어댑터의 request(파일 포맷 종류)을 가져와서 비교
        // -- 어댑터 클래스에서 toString() 메소드를 오버라이드함
        Log.d(TAG, recyclerView.getAdapter().toString());
        String get_format = recyclerView.getAdapter().toString();

        // 만약 local 파일들을 보는 View에서 add 버튼을 클릭했다면,
        // checked_files 의 개수가 1개 이상일 때(즉, 체크한 파일이 있을 때)
        // 파일 업로드를 묻는 다이얼로그 띄우기
        if((get_format.equals("pdf") || get_format.equals("img") || get_format.equals("all"))
                && checked_files.size() > 0) {
            Intent intent = new Intent(getActivity(), Confirm_upload_files_D.class);
            intent.putExtra("upload_files_count", checked_files.size());
            intent.putExtra("contain_pdf_file_orNot", have_pdf_ofNot);
            getActivity().startActivityForResult(intent, Call_A.REQUEST_CONFIRM_UPLOAD_FILES);
        }
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 팝업 닫기
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.close_popup)
    public void close_popup() {
        // 체크된 파일 리스트를 담는 checked_files 해쉬맵 초기화
        // 업로드할 파일 리스트를 담는 init_files_for_upload 해쉬맵도 초기화
        myapp.init_checked_files();
        myapp.init_files_for_upload();

        popup_menu_REL.setVisibility(View.GONE);
        popup_menu_icon.setVisibility(View.VISIBLE);
        popup_file_manager_REL.setVisibility(View.GONE);
        close_popup.setVisibility(View.GONE);
        // 미리보기 로직 관련 뷰들 visibility 처리
        recyclerView.setVisibility(View.VISIBLE);
        preview_display.setVisibility(View.GONE);
        preview_REL.setVisibility(View.GONE);
        go_share.setVisibility(View.GONE);
        preview.setVisibility(View.VISIBLE);
    }


    /**---------------------------------------------------------------------------
     생명주기 ==> onDestroyView
     ---------------------------------------------------------------------------*/
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 버터나이프 바인드 해제
        if(unbinder != null) {
            unbinder.unbind();
        }
        // otto 해제
        BusProvider.getBus().unregister(this);
        // 체크된 파일 리스트를 담는 checked_files 해쉬맵 초기화
        // 업로드할 파일 리스트를 담는 init_files_for_upload 해쉬맵도 초기화
        myapp.init_checked_files();
        myapp.init_files_for_upload();
    }


    /**---------------------------------------------------------------------------
     otto ==> Call_A로 부터 message 수신
     ---------------------------------------------------------------------------*/
    @Subscribe
    public void getMessage(Event.Call_A__Call_F event) {
        Log.d(TAG, "otto 받음_ " + event.getMessage());
        Log.d(TAG, "otto 받음_ " + event.getData());

        /** 로컬 파일을 보여주기 위한 로직 */
        if(event.getMessage().equals("local")) {
            // 리사이클러뷰 동작 메소드 호출
            activate_File_RCV("local", event.getData());

            //// 회의 파일함 -> 로컬파일함으로 이동
            // 뷰 Visibility 조절
            back_to_menu.setVisibility(View.VISIBLE);
            add_files.setVisibility(View.VISIBLE);
            preview.setVisibility(View.GONE);

            // 뷰 comment 셋팅
            String title = "";
            if(event.getData().equals("pdf")) {
                title = "내장 PDF 파일";
            }
            else if(event.getData().equals("img")) {
                title = "내장 Img 파일";
            }
            else if(event.getData().equals("all")) {
                title = "내장 PDF/Img 파일";
            }
            file_box_title.setText(title);
        }

        /** 파일 업로드를 하기 위한 로직 */
        if(event.getMessage().equals("file_upload")) {
            String contain_pdf_file_orNot = event.getData();
            Log.d(TAG, "contain_pdf_file_orNot: " + contain_pdf_file_orNot);

            // 파일 업로드 시작점 메소드 호출
            myapp.check_pdf_files(contain_pdf_file_orNot, getActivity());
        }

        /** 파일 업로드 취소 선택 시 */
        if(event.getMessage().equals("file_upload_cancel")) {
            // Checked_file 해쉬맵 초기화
            myapp.getChecked_files().clear();
            // 어댑터의 arrayList의 Extra 값을 'no'로 변경하여 체크마크 해제시키기
            rcv_call_adapter_local.init_check_mark();
        }
    }


    /**---------------------------------------------------------------------------
     otto ==> Myapp로 부터 message 수신 -- PDF 컨버팅 +  업로드 관련
     ---------------------------------------------------------------------------*/
    @SuppressLint("SetTextI18n")
    @Subscribe
    public void getMessage(final Event.Myapp__Call_F event) {
        String message = event.getMessage();
        String data = event.getData();
        Log.d(TAG, "otto 받음_ getMessage: " + event.getMessage());
        Log.d(TAG, "otto 받음_ getData: " + event.getData());

//        int total_pdf_files_count     - PDF 전체 파일 개수 ('end'일 때는 exception 파일 개수를 담음)
//        int current_sequence          - 현재 변환중인 PDF 파일의 시퀀스(변환 순번)
//        String file_name              - PDF 파일이름
//        int percent                   - PDF 파일 변환중 페이지 변환율
//        int total_pdf_page_nums       - PDF 파일 총 페이지 수
//        int current_pdf_page          - 현재 변환중인 PDF 파일 page 넘버

        final int animationDuration = 1000; // 1000ms = 1.0s

        /** PDF 컨버팅 관련 메세지 */
        if(message.equals("progress")) {
            // 파일 변환 - 시작
            if(data.equals("start")) {
                // 프로그레스 View VISIBLE
                comment.setText("PDF convert to images");
                circularProgressbar_REL.setVisibility(View.VISIBLE);
                circularProgressBar.setVisibility(View.VISIBLE);
                // 프로그래스 바 초기화
                circularProgressBar.setProgressWithAnimation(0, 0);
                // close 버튼 GONE
                close_popup.setVisibility(View.GONE);
                progress_wheel.setVisibility(View.VISIBLE); // 조그만 프로그레스 바
                circularProgressBar.setColor(Color.parseColor("#388E3C"));
                circularProgressBar.setBackgroundColor(Color.parseColor("#A5D6A7"));

                on_converting_filename = event.getFile_name();
                //// PDF 변환 관련 변수 담기 - PDF 총 파일 개수와, 변환 파일 시퀀스는 최초 1번만 받는다
                // 첫번째 Start 처리
                if(total_pdf_files_count == -1 && current_sequence == -1) {
                    total_pdf_files_count = event.getTotal_pdf_files_count();
                    current_sequence = event.getCurrent_sequence();
                    percent.setText("0%");
                    sequence.setText(current_sequence + "번째 파일 변환 중 (총 " + String.valueOf(total_pdf_files_count) + "개 파일)");
                    file_name.setText(on_converting_filename);
                }

                // 두번째 Start 처리
                else  {
                    current_sequence++;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            percent.setText("0%");
                            sequence.setText(current_sequence + "번째 파일 변환 중 (총 " + String.valueOf(total_pdf_files_count) + "개 파일)");
                            file_name.setText(on_converting_filename);
                            // 프로그레스 초기화
                            circularProgressBar.setProgressWithAnimation(0, 0);
                        }
                    }, 1300);
                }
                Log.d(TAG, "start_total_pdf_files_count: " + total_pdf_files_count);
                Log.d(TAG, "start_current_sequence: " + current_sequence);
                Log.d(TAG, "start_on_converting_filename: " + on_converting_filename);
            }

            // 페이지 전환율 - 한 페이지 변환 시작
            else if(data.equals("progress")) {
                final int total = event.getTotal_pdf_page_nums();
                final int current_pdf_page = event.getCurrent_pdf_page();
                Log.d(TAG, "progress_total_pdf_page_nums: " + total);
                Log.d(TAG, "progress_current_pdf_page: " + current_pdf_page);

                // 연속된 PDF 파일 변환 시작 시, 딜레이 주기
                if(current_sequence > 1 && current_pdf_page==1) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            page_status.setText(String.valueOf(current_pdf_page-1) + "/" + String.valueOf(total));
                        }
                    }, 1300);
                }
                else {
                    page_status.setText(String.valueOf(current_pdf_page-1) + "/" + String.valueOf(total));
                }


            }
            // 페이지 전환율 - 한 페이지 변환 완료
            else if(data.equals("ing")) {
                int total = event.getTotal_pdf_page_nums();
                int current_pdf_page = event.getCurrent_pdf_page();
                int rate = event.getPercent();
                Log.d(TAG, "ing_percent: " + rate);

                page_status.setText(String.valueOf(current_pdf_page) + "/" + String.valueOf(total));
                percent.setText(String.valueOf(rate) + "%");
                circularProgressBar.setProgressWithAnimation(rate, animationDuration);
            }

            // 파일 변환 - 파일 모두 변환 완료
            else if(data.equals("end")) {
                Log.d(TAG, "end");
                // 1초 뒤에 실행
                new Handler().postDelayed(new Runnable() {
                    @Override public void run() {
                        progress_wheel.setVisibility(View.GONE); // 조그만 프로그레스 바
                        // 정상 변환 처리된 PDF 파일 개수 계산
                        int exceptioned_pdf_file_count = event.getTotal_pdf_files_count();
                        int converted_pdf_file_count = total_pdf_files_count - exceptioned_pdf_file_count;
                        Log.d(TAG, "total_pdf_files_count: " + total_pdf_files_count);
                        Log.d(TAG, "exceptioned_pdf_file_count: " + exceptioned_pdf_file_count);
                        Log.d(TAG, "converted_pdf_file_count: " + converted_pdf_file_count);

                        int delay = 0;
                        String result = "";
                        String percent_str = "";
                        int rate = 0;
                        int ani_duration = animationDuration;

                        if(exceptioned_pdf_file_count == 0) {
                            sequence.setText("");
                            delay = 1500;
                            result = "총 " + String.valueOf(converted_pdf_file_count) + "개 파일 변환 완료";
                            percent_str = "완료";
                            rate = 100;
                        }
                        else if(exceptioned_pdf_file_count > 0 && total_pdf_files_count!=exceptioned_pdf_file_count) {
                            sequence.setText(event.getTotal_pdf_files_count() + "개의 파일, 변환중 에러로 제외");
                            delay = 3000;
                            result = "총 " + String.valueOf(converted_pdf_file_count) + "개 파일 변환 완료";
                            percent_str = "완료";
                            rate = 100;
                        }
                        // 컨버팅한 PDF 파일이 모두 오류가 발생했다면
                        else if(total_pdf_files_count==exceptioned_pdf_file_count) {
                            circularProgressBar.setVisibility(View.INVISIBLE);
                            sequence.setText("");
                            delay = 2000;
                            if(exceptioned_pdf_file_count == 1) {
                                result = "파일 변환에 실패하였습니다";
                            }
                            else if(exceptioned_pdf_file_count > 1) {
                                result = String.valueOf(exceptioned_pdf_file_count) + "개 파일, 모두 변환에 실패하였습니다";
                            }
                            percent_str = "변환 실패";
                            rate = 0;
                            ani_duration = 0;
                        }

                        file_name.setText(result);
                        percent.setText(percent_str);
                        page_status.setText("");
                        circularProgressBar.setProgressWithAnimation(rate, ani_duration);

                        // 1.5초 뒤에 프로그레스 GONE 처리
                        new Handler().postDelayed(new Runnable() {
                            @Override public void run() {
                                // 프로그레스 View GONE
                                circularProgressBar.setVisibility(View.GONE);
                                circularProgressbar_REL.setVisibility(View.GONE);
                                // close 버튼 VISIBLE
                                close_popup.setVisibility(View.VISIBLE);

                                // PDF 파일 개수 관련 변수 초기화
                                total_pdf_files_count = -1;
                                current_sequence = -1;

                                /** 이미지 파일 업로드 메소드 호출 */
                                myapp.upload_multi_files_1(getActivity());
                            }
                        }, delay);
                    }
                }, 1000);
            }
        }
        /** 업로드 완료 메시지 -- 회의 파일함으로 돌아가기 */
        if(message.equals("upload") && data.equals("end")) {
            Log.d(TAG, "파일 업로드 콜백 -- 회의 파일함으로 돌아가기");
            // 회의 파일함 oepn 메소드 호출
            file_box();
        }
    }


    /**---------------------------------------------------------------------------
     메소드 ==> circularProgressbar 컬러 조정
     ---------------------------------------------------------------------------*/
    private int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 선택된 파일들, '미리보기' 담당 리사이클러뷰 어댑터로 넘기기
     ---------------------------------------------------------------------------*/
    @SuppressLint("SetTextI18n")
    public void activate_preview_RCV(String mode) {

        // '미리보기' 담당 리사이클러뷰 어댑터에 넘겨줄 어레이리스트
        preview_file_arr = new ArrayList<>();
        // 어플리케이션 객체에 있는 해쉬맵을 담을 옮겨담을 변수
        HashMap<String, String> checked_files_hash = myapp.getChecked_files();
        Log.d(TAG, "myapp.getChecked_files().size(): " + checked_files_hash.size());

        // value 값으로 sorting(사용자가 파일 추가 클릭한 순서대로 sorting)
        Iterator it = myapp.sort_map_by_value(checked_files_hash).iterator();

        // 미리보기 첫번째 파일의 이름을 담을 변수, 초기화
        first_fileName = "";

        // sorting 한 결과대로, 어댑터에 넘겨줄 어레이리스트에 add
        while(it.hasNext()) {
            String temp = (String)it.next();
            Log.d(TAG, temp + " = " + myapp.getChecked_files().get(temp));

            Preview_selected_file preview_selected_file = new Preview_selected_file();
            preview_selected_file.setFileName(temp);
            preview_selected_file.setSelected(false);

            preview_file_arr.add(preview_selected_file);

            if(first_fileName.equals("")) {
                first_fileName = temp;
            }
        }
        Log.d(TAG, "preview_file_arr.size(): " + preview_file_arr.size());

        // 어플리케이션 객체, 체크파일 해쉬맵 초기화
        myapp.init_checked_files();

        // 어댑터가 생성되지 않았을 때 -> 어댑터를 생성
        if(rcv_selectFile_preview_adapter == null) {
            // 생성자 인수
            // 1. 액티비티(context 객체 넘기기)
            // 2. 인플레이팅 되는 레이아웃
            // 3. 선택한 파일들 파일 이름이 담긴 arrayList
            // 4. 모드 변별 변수
            rcv_selectFile_preview_adapter = new RCV_selectFile_preview_adapter(
                    getActivity(), R.layout.i_selected_file, preview_file_arr, mode, this);

            recyclerView_preview.setAdapter(rcv_selectFile_preview_adapter);
            rcv_selectFile_preview_adapter.notifyDataSetChanged();

            // 리사이클러뷰 드래그 and 스와이프 기능 관련
            ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(rcv_selectFile_preview_adapter);
            itemTouchHelper = new ItemTouchHelper(callback);
            itemTouchHelper.attachToRecyclerView(recyclerView_preview);

        }
        // 어댑터가 생성되어 있을 때, 셋팅되는 arrayList 만 교체
        else {
            rcv_selectFile_preview_adapter.refresh_arr(preview_file_arr, "");
            recyclerView_preview.setAdapter(rcv_selectFile_preview_adapter);
        }

        // 뷰 Visibility 조절
        recyclerView.setVisibility(View.GONE);
        preview_display.setVisibility(View.VISIBLE);
        preview_REL.setVisibility(View.VISIBLE);
        preview.setVisibility(View.GONE);
        go_share.setVisibility(View.VISIBLE);
    }


    /**---------------------------------------------------------------------------
     콜백메소드 ==> 리사이클러뷰 아이템 드래그가 발생할 때, 콜백되는 메소드
     ---------------------------------------------------------------------------*/
    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {

    }


    /**---------------------------------------------------------------------------
     메소드 ==> 파일 종류에 따른 리스트, 어댑터로 넘기기
     ---------------------------------------------------------------------------*/
    public void activate_File_RCV(String target, String format) {
        // 체크된 파일 리스트를 담는 checked_files 해쉬맵 초기화
        // 업로드할 파일 리스트를 담는 files_for_upload 해쉬맵도 초기화
        myapp.init_checked_files();
        myapp.init_files_for_upload();

        /** 로컬 파일 어댑터가 필요할 때 */
        if(target.equals("local")) {
            // File_info 객체를 담는 ArrayLlist 생성
            ArrayList<File_info> files = new ArrayList<>();
            // 해당 포맷들의 파일들의 CanonicalPath가 담긴 ArrayList 받아오기
            ArrayList<String> mFileNames = File_search.file_search(format);
            Log.d(TAG, "local_files 개수: " + mFileNames.size());
            Log.d(TAG, "local_files.isEmpty(): " + mFileNames.isEmpty());

            // 가져온 파일들을 어댑터에 전달할 arrayList 에 add 하는 과정
            String canonicalPath;
            for(int i=0; i<mFileNames.size(); i++) {
                canonicalPath = mFileNames.get(i);
                Log.d(TAG, "canonicalPath_ " + i + ": " + canonicalPath);

                // File_info 객체 생성
                File_info file_for_adding = new File_info();
                // 생성된 File_info 에 정보 담기
                file_for_adding.setFile_name(canonicalPath);
                // 어댑터에 넘길 arrayList 에 add 하기
                files.add(i, file_for_adding);
            }
            Log.d(TAG, "어댑터에 넘길 local_files 개수: " + files.size());
            Log.d(TAG, "어댑터에 넘길 local_files.isEmpty(): " + files.isEmpty());

            // 어댑터가 생성되지 않았을 때 -> 어댑터를 생성
            if(rcv_call_adapter_local == null) {
                // 생성자 인수
                // 1. 액티비티
                // 2. 인플레이팅 되는 레이아웃
                // 3. arrayList 데이터
                // 4. 변별 변수
                rcv_call_adapter_local = new RCV_call_adapter(getActivity(), R.layout.i_file, files, format);
                recyclerView.setAdapter(rcv_call_adapter_local);
                rcv_call_adapter_local.notifyDataSetChanged();
            }
            // 어댑터가 생성되어 있을 때, 셋팅되는 arrayList 만 교체
            else {
                rcv_call_adapter_local.refresh_arr(files, format);
                recyclerView.setAdapter(rcv_call_adapter_local);
            }
        }

        /** 공유 파일 어댑터가 필요할 때 */
        else if(target.equals("project")) {
            // 서버로부터 공유 파일리스트 받기
            ArrayList<File_info> files = myapp.get_uploaded_file_list(getActivity(), myapp.getMeeting_no());
            Log.d(TAG, "어댑터에 넘길 project_files.isEmpty(): " + files.isEmpty());
            if(!files.isEmpty()) {
                Log.d(TAG, "어댑터에 넘길 project_files 개수: " + files.size());
            }

            // '파일 추가' 아이콘 item 추가하기
            File_info file_add_btn = new File_info();
            file_add_btn.setFile_format("zero");
            file_add_btn.setFile_name("파일추가.zero");
            files.add(0, file_add_btn);

            // 어댑터가 생성되지 않았을 때 -> 어댑터를 생성
            if(rcv_call_adapter_project == null) {
                // 생성자 인수
                // 1. 액티비티
                // 2. 인플레이팅 되는 레이아웃
                // 3. arrayList 데이터
                // 4. 변별 변수
                rcv_call_adapter_project = new RCV_call_adapter(getActivity(), R.layout.i_file, files, "project");
                recyclerView.setAdapter(rcv_call_adapter_project);
                rcv_call_adapter_project.notifyDataSetChanged();
            }
            // 어댑터가 생성되어 있을 때, 셋팅되는 arrayList 만 교체
            else {
                rcv_call_adapter_project.refresh_arr(files, format);
                recyclerView.setAdapter(rcv_call_adapter_project);
            }
        }
    }
}
