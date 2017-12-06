package com.example.jyn.remotemeeting.Fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.jyn.remotemeeting.Activity.Call_A;
import com.example.jyn.remotemeeting.Adapter.RCV_call_adapter;
import com.example.jyn.remotemeeting.DataClass.File_info;
import com.example.jyn.remotemeeting.Dialog.Confirm_upload_files_D;
import com.example.jyn.remotemeeting.Etc.Static;
import com.example.jyn.remotemeeting.Otto.BusProvider;
import com.example.jyn.remotemeeting.Otto.Event;
import com.example.jyn.remotemeeting.R;
import com.example.jyn.remotemeeting.Util.File_search;
import com.example.jyn.remotemeeting.Util.Myapp;
import com.example.jyn.remotemeeting.WebRTC.CaptureQualityController;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.squareup.otto.Subscribe;

import org.webrtc.RendererCommon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by JYN on 2017-11-10.
 */

public class Call_F extends Fragment {

    private LinearLayout exit_LIN;
    private RelativeLayout popup_menu_REL;
    private View controlView;
    private TextView contactView;
    private ImageView cameraSwitchButton;
    private ImageView mic_on_show_IV;
    private ImageView mic_off_show_IV;
//    private ImageButton videoScalingButton;
    private ImageView toggleMuteButton;
    private ImageView popup_menu_icon;
    private TextView toggleMuteText;
    private TextView captureFormatText;
    private TextView cameraSwitchText;
    private SeekBar captureFormatSlider;
    private OnCallEvents callEvents;
    private RendererCommon.ScalingType scalingType;
    private boolean videoCallEnabled = true;
    Myapp myapp;

    private static final String TAG = "all_"+Call_F.class.getSimpleName();

    // 리사이클러뷰 관련 클래스
    public RCV_call_adapter rcv_call_adapter_project;
    public RCV_call_adapter rcv_call_adapter_local;
    private RecyclerView.LayoutManager layoutManager;


    /** 버터나이프 적용시킨 이후 뷰 찾기*/
    public Unbinder unbinder;
    @BindView(R.id.popup_file_manager)      public RelativeLayout popup_file_manager_REL;
    @BindView(R.id.recyclerView)            public RecyclerView recyclerView;
    @BindView(R.id.file_box_title)          public TextView file_box_title;
    @BindView(R.id.back_to_menu)            public ImageView back_to_menu;
    @BindView(R.id.go_share)                public ImageView go_share;
    @BindView(R.id.close_popup)             public RelativeLayout close_popup;
    @BindView(R.id.sequence)                public TextView sequence;
    @BindView(R.id.file_name)               public TextView file_name;
    @BindView(R.id.percent)                 public TextView percent;
    @BindView(R.id.circularProgressbar_REL) public RelativeLayout circularProgressbar_REL;

    public static CircularProgressBar circularProgressBar;
    @SuppressLint("StaticFieldLeak")
    public static ImageView add_files;

    /**
     * Call control interface for container activity.
     */
    public interface OnCallEvents {
        void onCallHangUp();
        void onCameraSwitch();
        void onVideoScalingSwitch(RendererCommon.ScalingType scalingType);
        void onCaptureFormatChange(int width, int height, int framerate);
        boolean onToggleMic();
    }

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
        toggleMuteButton = controlView.findViewById(R.id.button_call_toggle_mic);
        toggleMuteText = controlView.findViewById(R.id.text_call_toggle_mic);
        captureFormatText = controlView.findViewById(R.id.capture_format_text_call);
        captureFormatSlider = controlView.findViewById(R.id.capture_format_slider_call);
        popup_menu_icon = controlView.findViewById(R.id.popup_menu_icon);
        popup_menu_REL = controlView.findViewById(R.id.popup_menu);
        mic_on_show_IV = controlView.findViewById(R.id.mic_on_show);
        mic_off_show_IV = controlView.findViewById(R.id.mic_off_show);
        add_files = controlView.findViewById(R.id.add_files);
        circularProgressBar = controlView.findViewById(R.id.circularProgressbar);

        // circularProgressBar 사용 example
        int animationDuration = 2500; // 2500ms = 2,5s
        circularProgressBar.setProgressWithAnimation(65, animationDuration); // Default duration = 1500ms
        // circularProgressBar 사용 example

        // otto 등록
        BusProvider.getBus().register(this);

        // 어플리케이션 객체 생성
        myapp = Myapp.getInstance();

        /** 리사이클러뷰 - 프로젝트 파일 */
        recyclerView.setHasFixedSize(true);
        // 리사이클러뷰 - GridLayoutManager 사용
        layoutManager = new GridLayoutManager(getActivity().getApplicationContext(), 3);
        recyclerView.setLayoutManager(layoutManager);
        // 리사이클러뷰 에니메이션 설정
        recyclerView.setItemAnimator(new DefaultItemAnimator());

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
         클릭이벤트 ==> 마이크 on,off
         ---------------------------------------------------------------------------*/
        toggleMuteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean enabled = callEvents.onToggleMic();
                Log.d(TAG, "toggleMuteButton_enabled: " + enabled);

                // 사용자에게 보여주는 마이크 상태 표시 변경
//                if(enabled) {
//                    mic_on_show_IV.setVisibility(View.VISIBLE);
//                    mic_off_show_IV.setVisibility(View.GONE);
//                } else if(!enabled) {
//                    mic_on_show_IV.setVisibility(View.GONE);
//                    mic_off_show_IV.setVisibility(View.VISIBLE);
//                }


                toggleMuteButton.setAlpha(enabled ? 1.0f : 0.3f);
                String test = String.valueOf(enabled);
                switch (test) {
                    case "true":
                    mic_on_show_IV.setVisibility(View.VISIBLE);
                    mic_off_show_IV.setVisibility(View.GONE);
                        break;
                    case "false":
                        mic_on_show_IV.setVisibility(View.GONE);
                        mic_off_show_IV.setVisibility(View.VISIBLE);
                        break;
                  }

            }
        });
        toggleMuteText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean enabled = callEvents.onToggleMic();
                Log.d(TAG, "toggleMuteText_enabled: " + enabled);

                // 사용자에게 보여주는 마이크 상태 표시 변경
//                if(enabled) {
//                    mic_on_show_IV.setVisibility(View.VISIBLE);
//                    mic_off_show_IV.setVisibility(View.GONE);
//                } else if(!enabled) {
//                    mic_on_show_IV.setVisibility(View.GONE);
//                    mic_off_show_IV.setVisibility(View.VISIBLE);
//                }
                toggleMuteButton.setAlpha(enabled ? 1.0f : 0.3f);
                String test = String.valueOf(enabled);
                switch (test) {
                    case "true":
                        mic_on_show_IV.setVisibility(View.VISIBLE);
                        mic_off_show_IV.setVisibility(View.GONE);
                        break;
                    case "false":
                        mic_on_show_IV.setVisibility(View.GONE);
                        mic_off_show_IV.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });


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
     메소드 ==> circularProgressBar show
     ---------------------------------------------------------------------------*/
    public void show_circularProgressBar(int Total_pdf_files_count, int current_sequence,
                                         String file_name, int percent) {
        circularProgressbar_REL.setVisibility(View.VISIBLE);

    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 회의 파일함 Open
     ---------------------------------------------------------------------------*/
    @OnClick({R.id.file_box_IV})
    public void go_file_box(View view) {
        // 체크된 파일 리스트를 담는 checked_files 해쉬맵 초기화
        myapp.init_checked_files();

        // 리사이클러뷰 동작 메소드 호출
        activate_RCV("project", "");

        popup_menu_REL.setVisibility(View.GONE);
        popup_menu_icon.setVisibility(View.GONE);
        popup_file_manager_REL.setVisibility(View.VISIBLE);
        back_to_menu.setVisibility(View.VISIBLE);
        add_files.setVisibility(View.GONE);
        close_popup.setVisibility(View.VISIBLE);
        go_share.setVisibility(View.VISIBLE);

        file_box_title.setText("회의 파일함");
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> back_img 클릭 -- 회의 파일함 => 메뉴로 이동
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.back_to_menu)
    public void back_to_menu() {
        // 체크된 파일 리스트를 담는 checked_files 해쉬맵 초기화
        myapp.init_checked_files();

        popup_menu_REL.setVisibility(View.VISIBLE);
        popup_menu_icon.setVisibility(View.VISIBLE);
        popup_file_manager_REL.setVisibility(View.GONE);
        close_popup.setVisibility(View.GONE);
    }


    /**---------------------------------------------------------------------------
     클릭이벤트 ==> 서버 통신 -- 파일 업로드: 로컬파일함 => 회의 파일함으로 이동
     ---------------------------------------------------------------------------*/
    @OnClick(R.id.add_files)
    public void upload_files(View view) {
        // todo: 업로드를 묻는 다이얼로그 띄우기
        // todo: 업로드 완료 후, 뷰 처리하고, 회의 파일함으로 이동하기
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
        myapp.init_checked_files();

        popup_menu_REL.setVisibility(View.GONE);
        popup_menu_icon.setVisibility(View.VISIBLE);
        popup_file_manager_REL.setVisibility(View.GONE);
        close_popup.setVisibility(View.GONE);
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
        myapp.init_checked_files();
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
            activate_RCV("local", event.getData());

            //// 회의 파일함 -> 로컬파일함으로 이동
            // 뷰 Visibility 조절
            back_to_menu.setVisibility(View.VISIBLE);
            add_files.setVisibility(View.VISIBLE);
            go_share.setVisibility(View.GONE);

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

            // pdf 파일이 하나라도 포함되어 있다면
            if(contain_pdf_file_orNot.equals("true")) {

            }
            // pdf 파일이 없다면
            else if(contain_pdf_file_orNot.equals("false")) {

            }

//        //// 로컬 파일함 -> 회의파일함으로 이동
//        // 뷰 Visibility 조절
//        popup_menu_REL.setVisibility(View.GONE);
//        popup_menu_icon.setVisibility(View.GONE);
//        popup_file_manager_REL.setVisibility(View.VISIBLE);
//        back_to_menu.setVisibility(View.VISIBLE);
//        add_files.setVisibility(View.GONE);
//        go_share.setVisibility(View.VISIBLE);
//
//        // 뷰 comment 셋팅
//        file_box_title.setText("회의 파일함");
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
     메소드 ==> 서버로부터 공유 파일 리스트 받아와서, 어댑터로 넘기기
     ---------------------------------------------------------------------------*/
    public void activate_RCV(String target, String format) {
        // 체크된 파일 리스트를 담는 checked_files 해쉬맵 초기화
        myapp.init_checked_files();

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
            ArrayList<File_info> files = myapp.get_uploaded_file_list(getActivity());
            Log.d(TAG, "어댑터에 넘길 project_files 개수: " + files.size());
            Log.d(TAG, "어댑터에 넘길 project_files.isEmpty(): " + files.isEmpty());

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
