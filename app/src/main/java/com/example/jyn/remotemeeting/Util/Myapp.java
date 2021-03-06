package com.example.jyn.remotemeeting.Util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.multidex.MultiDex;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.jyn.remotemeeting.Activity.Main_before_login_A;
import com.example.jyn.remotemeeting.DataClass.Data_for_netty;
import com.example.jyn.remotemeeting.DataClass.File_info;
import com.example.jyn.remotemeeting.DataClass.Meeting_room;
import com.example.jyn.remotemeeting.DataClass.Project;
import com.example.jyn.remotemeeting.DataClass.Redis_log_click_event;
import com.example.jyn.remotemeeting.DataClass.Redis_log_session_info;
import com.example.jyn.remotemeeting.DataClass.Redis_log_view_crossOver_from_to;
import com.example.jyn.remotemeeting.DataClass.Users;
import com.example.jyn.remotemeeting.Etc.Static;
import com.example.jyn.remotemeeting.Fragment.Call_F;
import com.example.jyn.remotemeeting.Netty.GatheringHandler;
import com.example.jyn.remotemeeting.Netty.Netty_handler;
import com.example.jyn.remotemeeting.Netty.Chat_service;
import com.example.jyn.remotemeeting.Otto.BusProvider;
import com.example.jyn.remotemeeting.Otto.Event;
import com.example.jyn.remotemeeting.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.rendering.PDFRenderer;
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.realm.Realm;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by JYN on 2017-12-01.
 *
 * 어플리케이션 객체
 *  - 어플 구동중에 여러 클래스에서 사용해야 하는 변수나, 메소드를 정의함
 */

public class Myapp extends Application {

    private static final String TAG = "all_"+Myapp.class.getSimpleName();
    String JSON_TAG_PARTNER_LIST = "partner_list";
    String JSON_TAG_SEARCH_LIST = "search_list";
    String JSON_TAG_SHARE_FILE_LIST = "share_file_list";
    String JSON_TAG_PROJECT_LIST= "project_list";
    String JSON_TAG_MEETING_ROOM_LIST= "meeting_room_arr";
    private static Myapp appInstance;
    Toast logToast;
    ProgressDialog progressDialog;
    HashMap<String, String> checked_files;
    HashMap<String, String> files_for_upload;
    File root;
    String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/RemoteMeeting";
    Handler handler;
    int PDF_converting_exception_file_count = 0;

    // 로그인 시, 로그인 한 시각을 String 값으로 저장하기 위한 변수
    // --> session_id 값으로 사용하기 위함
    String session_id = "";

    // 로그인 방법
    String login_method = "";

    // 'Main_after_login_A'에서 현재 보고 있는 프래그먼트 클래스 이름을 담는 변수
    // 초기 값은 'Project' --> 로그인하면 보여주는 화면이기 때문
    public String curr_frag_at_main;

    // 로그인 액티비티에서 사용하는 백 이미지들
    public int[] back_img = {
            R.drawable.back_1,
            R.drawable.back_2,
            R.drawable.back_3,
            R.drawable.back_4,
            R.drawable.back_5,
            R.drawable.back_6,
            R.drawable.back_7,
    };

    // 회의 생성 액티비티에서 사용하는 백 이미지들
    public int[] round_back_img = {
            R.drawable.back__1,
            R.drawable.back__2,
            R.drawable.back__3,
            R.drawable.back__4,
            R.drawable.back__5,
            R.drawable.back__6,
            R.drawable.back__7,
    };

    /** 색연필 버튼 id 값들 */
    public int[] color_pen_buttonIds = {
            R.id.black,
            R.id.white,
            R.id.elephant,
            R.id.blue,
            R.id.indigo,
            R.id.grape_jelly,
            R.id.green,
            R.id.teal,
            R.id.orange,
            R.id.yellow,
            R.id.mulberry,
            R.id.sexy_salmon
    };

    /** 내 정보 */
    public String user_no = "";
    public String join_path = "";
    public String join_dt = "";
    public String user_email = "";
    public String user_nickname = "";
    public String present_meeting_in_ornot = "";
    public String user_img_filename = "";

    /** 프로필 변경에서 사용하는 임시 변수 */
    public String temp_nickname="";
    public String temp_img_filename="";
    public String temp_img_absolutePath="";

    /** 참여중인 회의 정보 */
    public String meeting_no = "";
    public String real_meeting_title = "";
    public String transform_meeting_title = "";
    public String meeting_creator_user_no = "";
    public String meeting_subject_user_no = "";
    public String meeting_authority_user_no = "";
    public String project_no = "";
    public String meeting_status = "";

    // 참여중인 회의 번호(위의, 'meeting_no' 처럼 따로 초기화 하지 않음)
    public String target_meeting_no = "";

    /** 참여중인 채팅방 정보 */
    public int chatroom_no = -1;

    /** Netty - Channel : 서비스 구동 후, 서버와 연결된 channel을 어플리케이션 객체에 보관*/
    Channel channel;

    /** 내가 보낸 채팅 메세지에 대한 UUID 값과, 채팅 메세지를 임시적으로 저장하기 위한 hashMap */
    // key - UUID
    // value - 내가 채팅 메세지를 전송할 때의 기기의 로컬 시간
    public ConcurrentHashMap<String, Long> temp_my_chat_log_hash;

    // 요일 배열
    public String[] weekDay = { "일요일", "월요일", "화요일", "수요일", "목요일", "금요일", "토요일" };

    // 영상통화에서 문서 공유를 요청받았을 때, 공유할 문서 리스트를 저장할 변수
    String share_image_file_name_arr_str;

    // 영상통화 중, 이미지 공유 모드를 진행하기 전의 비디오 전송모드가 어떤 모드였는지 저장하기 위한 변수
    boolean video_state_was;

    // 프로젝트 폴더 색깔을 나타내는 String 값에 따른, drawable 리소스_ 해쉬맵
    public ConcurrentHashMap<String, Integer> folder_color_hash;

    // 프로젝트 폴더 색깔 String 배열
    public String[] folder_color_str = {
            "amber", "blue", "blue_grey", "brown", "deep_orange", "deep_purple", "green", "grey",
            "indigo", "light_green", "orange", "pink", "purple", "red", "teal"
    };

    // 프로젝트 폴더 리소스 int 배열
    public int[] folder_color_resource = {
            R.drawable.amber_f,         // 0
            R.drawable.blue_f,          // 1
            R.drawable.blue_grey_f,     // 2
            R.drawable.brown_f,         // 3
            R.drawable.deep_orange_f,   // 4
            R.drawable.deep_purple_f,   // 5
            R.drawable.green_f,         // 6
            R.drawable.grey_f,          // 7
            R.drawable.indigo_f,        // 8
            R.drawable.light_green_f,   // 9
            R.drawable.orange_f,        // 10
            R.drawable.pink_f,          // 11
            R.drawable.purple_f,        // 12
            R.drawable.red_f,           // 13
            R.drawable.teal_f           // 14
    };

    // 프로젝트 폴더 색 int값 배열
    public int[] folder_color_int_value = {
            0xffffc107,
            0xff2196f3,
            0xff607d8b,
            0xff795548,
            0xffff5722,
            0xff673ab7,
            0xff4caf50,
            0xff9e9e9e,
            0xff3f51b5,
            0xff8bc34a,
            0xffff9800,
            0xffe91e63,
            0xff9c27b0,
            0xfff44336,
            0xff009688,
    };

    // 임시 비트맵 저장: 스캔한 문서 비트맵
//    public Bitmap scanned_bitmap;

    /** GET, SET */
    public String getUser_no() {
        return user_no;
    }
    public void setUser_no(String user_no) {
        this.user_no = user_no;
    }
    public String getJoin_path() {
        return join_path;
    }
    public void setJoin_path(String join_path) {
        this.join_path = join_path;
    }
    public String getJoin_dt() {
        return join_dt;
    }
    public void setJoin_dt(String join_dt) {
        this.join_dt = join_dt;
    }
    public String getUser_email() {
        return user_email;
    }
    public void setUser_email(String user_email) {
        this.user_email = user_email;
    }
    public String getUser_nickname() {
        return user_nickname;
    }
    public void setUser_nickname(String user_nickname) {
        this.user_nickname = user_nickname;
    }
    public String getPresent_meeting_in_ornot() {
        return present_meeting_in_ornot;
    }
    public void setPresent_meeting_in_ornot(String present_meeting_in_ornot) {
        this.present_meeting_in_ornot = present_meeting_in_ornot;
    }
    public String getUser_img_filename() {
        return user_img_filename;
    }
    public void setUser_img_filename(String user_img_filename) {
        this.user_img_filename = user_img_filename;
    }
    public String getTemp_nickname() {
        return temp_nickname;
    }
    public void setTemp_nickname(String temp_nickname) {
        this.temp_nickname = temp_nickname;
    }
    public String getTemp_img_filename() {
        return temp_img_filename;
    }
    public void setTemp_img_filename(String temp_img_filename) {
        this.temp_img_filename = temp_img_filename;
    }
    public String getTemp_img_absolutePath() {
        return temp_img_absolutePath;
    }
    public void setTemp_img_absolutePath(String temp_img_absolutePath) {
        this.temp_img_absolutePath = temp_img_absolutePath;
    }
    public String getMeeting_no() {
        return meeting_no;
    }
    public void setMeeting_no(String meeting_no) {
        this.meeting_no = meeting_no;
    }
    public String getReal_meeting_title() {
        return real_meeting_title;
    }
    public void setReal_meeting_title(String real_meeting_title) {
        this.real_meeting_title = real_meeting_title;
    }
    public String getTransform_meeting_title() {
        return transform_meeting_title;
    }
    public void setTransform_meeting_title(String transform_meeting_title) {
        this.transform_meeting_title = transform_meeting_title;
    }

    public String getMeeting_creator_user_no() {
        return meeting_creator_user_no;
    }
    public void setMeeting_creator_user_no(String meeting_creator_user_no) {
        this.meeting_creator_user_no = meeting_creator_user_no;
    }
    public String getMeeting_subject_user_no() {
        return meeting_subject_user_no;
    }
    public void setMeeting_subject_user_no(String meeting_subject_user_no) {
        this.meeting_subject_user_no = meeting_subject_user_no;
    }
    public String getMeeting_authority_user_no() {
        return meeting_authority_user_no;
    }
    public void setMeeting_authority_user_no(String meeting_authority_user_no) {
        this.meeting_authority_user_no = meeting_authority_user_no;
    }
    public String getProject_no() {
        return project_no;
    }
    public void setProject_no(String project_no) {
        this.project_no = project_no;
    }
    public String getMeeting_status() {
        return meeting_status;
    }
    public void setMeeting_status(String meeting_status) {
        this.meeting_status = meeting_status;
    }
    public HashMap<String, String> getChecked_files() {
        return checked_files;
    }
    public void setChecked_files(HashMap<String, String> checked_files) {
        this.checked_files = checked_files;
    }

    public int getPDF_converting_exception_file_count() {
        return PDF_converting_exception_file_count;
    }

    public void setPDF_converting_exception_file_count(int PDF_converting_exception_file_count) {
        this.PDF_converting_exception_file_count = PDF_converting_exception_file_count;
    }

    public HashMap<String, String> getFiles_for_upload() {
        return files_for_upload;
    }

    public int getChatroom_no() {
        return chatroom_no;
    }

    public void setChatroom_no(int chatroom_no) {
        this.chatroom_no = chatroom_no;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public ConcurrentHashMap<String, Long> getTemp_my_chat_log_hash() {
        return temp_my_chat_log_hash;
    }

    public void setTemp_my_chat_log_hash(ConcurrentHashMap<String, Long> temp_my_chat_log_hash) {
        this.temp_my_chat_log_hash = temp_my_chat_log_hash;
    }

    public String getShare_image_file_name_arr_str() {
        return share_image_file_name_arr_str;
    }

    public void setShare_image_file_name_arr_str(String share_image_file_name_arr_str) {
        this.share_image_file_name_arr_str = share_image_file_name_arr_str;
    }

    public boolean isVideo_state_was() {
        return video_state_was;
    }

    public void setVideo_state_was(boolean video_state_was) {
        this.video_state_was = video_state_was;
    }

    public ConcurrentHashMap<String, Integer> getFolder_color_hash() {
        return folder_color_hash;
    }

    public void setFolder_color_hash(ConcurrentHashMap<String, Integer> folder_color_hash) {
        this.folder_color_hash = folder_color_hash;
    }

    public String getThis_meeting_no() {
        return target_meeting_no;
    }

    public void setThis_meeting_no(String target_meeting_no) {
        this.target_meeting_no = target_meeting_no;
    }

    public String getSession_id() {
        return session_id;
    }

    public void setSession_id(String session_id) {
        this.session_id = session_id;
    }

    public String getLogin_method() {
        return login_method;
    }

    public void setLogin_method(String login_method) {
        this.login_method = login_method;
    }

    public String getCurr_frag_at_main() {
        return curr_frag_at_main;
    }

    public void setCurr_frag_at_main(String curr_frag_at_main) {
        this.curr_frag_at_main = curr_frag_at_main;
    }

    //    public Bitmap getScanned_bitmap() {
//        return scanned_bitmap;
//    }
//
//    public void setScanned_bitmap(Bitmap scanned_bitmap) {
//        this.scanned_bitmap = scanned_bitmap;
//    }

    /** 생명주기 - onCreate */
    @Override
    public void onCreate() {
        super.onCreate();
        appInstance = this;
        checked_files = new HashMap<>();
        Collections.synchronizedMap(checked_files);
        files_for_upload = new HashMap<>();
        temp_my_chat_log_hash = new ConcurrentHashMap<>();
        // 폴더 컬러 색 String 값에 따른 맞는 리소스 int id 값을 설정하기 위한 해쉬맵 선언
        folder_color_hash = new ConcurrentHashMap<>();

        // 폴더 컬러 관련 해쉬맵 put 하기
        for(int i=0; i<folder_color_str.length; i++) {
            folder_color_hash.put(folder_color_str[i], folder_color_resource[i]);
        }

        Realm.init(this);

        curr_frag_at_main = "Project_F";
    }

    /** 싱글톤 */
    public static Myapp getInstance() {
        return appInstance;
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 이메일 정규식 -- 영문, 숫자 조합 + '@' + 뒤에 '.' 포함
     ---------------------------------------------------------------------------*/
    public boolean email_check(String email) {
        String EMAIL_REGEX = "^[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$";
        Boolean b = email.matches(EMAIL_REGEX);
        return b;
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 비밀번호 정규식 -- 영문, 숫자, 특수문자 조합 / 8자리이상 16자이하
     ---------------------------------------------------------------------------*/
    public boolean pw_check(String pw) {
        String PASSWORD_REGEX = "^(?=.*[a-zA-Z]+)(?=.*[0-9]+)(?=.*[!@#$%^&*?_~]+).{8,16}$";
        Boolean b = pw.matches(PASSWORD_REGEX);
        return b;
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 닉네임 정규식 -- 모든 문자열 / 2자리이상 20자이하
     ---------------------------------------------------------------------------*/
    public boolean nickName_check(String nickName) {
        String NICKNAME_REGEX = "^[\\w\\Wㄱ-ㅎㅏ-ㅣ가-힣]{2,20}$";
        Boolean b = nickName.matches(NICKNAME_REGEX);
        return b;
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 로그 + 토스트
     ---------------------------------------------------------------------------*/
    public void logAndToast(String msg) {
        Log.d(TAG, "logAndToast- " + msg);
        if (logToast != null) {
            logToast.cancel();
        }
        logToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        logToast.show();
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 커스텀 로그(라이브러리: Logger-pretty Logger)
     ---------------------------------------------------------------------------*/
    public FormatStrategy custom_log(Class target_class) {
        FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
            // (Optional) Whether to show thread info or not. Default true
            .showThreadInfo(false)
            // (Optional) How many method line to show. Default 2
            .methodCount(0)
            // (Optional) Skips some method invokes in stack trace. Default 5
            .methodOffset(5)
            // (Optional) Custom tag for each log. Default PRETTY_LOGGER
            .tag(target_class.getSimpleName())
            .build();

        return formatStrategy;

        // 로그 출력 on/off 조절 방법: 메모임
//        Logger.addLogAdapter(new AndroidLogAdapter(myapp.custom_log(RCV_selectFile_preview_adapter.class)) {
//            @Override public boolean isLoggable(int priority, String tag) {
//                // true - Logger 활성화
//                // false - Logger 비활성화
//                return true;
//            }
//        });
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 내 user 객체 정보 저장 -- 정상적으로 로그인이 완료 됐을 때,
                서버로부터 내 정보를 받아와서 어플리케이션 객체에 정보 저장
                + netty 서버 접속 서비스 구동
     ---------------------------------------------------------------------------*/
    public void set_myInfo(JSONObject user_object) {
        try {
            this.user_no = user_object.getString("user_no");
            this.join_path = user_object.getString("join_path");
            this.join_dt = user_object.getString("join_dt");
            this.user_email = user_object.getString("user_email");
            this.user_nickname = user_object.getString("user_nickname");
            this.present_meeting_in_ornot = user_object.getString("present_meeting_in_ornot");
            this.user_img_filename = user_object.getString("user_img_filename");
            Log.d(TAG, "user_no: " + user_no);
            Log.d(TAG, "join_path: " + join_path);
            Log.d(TAG, "join_dt: " + join_dt);
            Log.d(TAG, "user_email: " + user_email);
            Log.d(TAG, "user_nickname: " + user_nickname);
            Log.d(TAG, "present_meeting_in_ornot: " + present_meeting_in_ornot);
            Log.d(TAG, "user_img_filename: " + user_img_filename);

            /** 채팅 서버 접속 서비스 구동 메소드 호출 - 테스트(개발) 중에는 일단 끄기: 구현은 완료*/
            // 현재 서비스가 구동중인지 확인하는 메소드 호출 - 자세한 설명은 해당 메소드에 주석처리
//            if(!isServiceRunningCheck()) {
//                chat_server_conn_service_run();
//            }

            // TODO: 테스트 코드 - 테스트 중, 로그인할 때 채팅 서버에 접속하는 것으로 하였음
            new Thread(new Runnable() {
                @Override
                public void run() {
                    EventLoopGroup group = new NioEventLoopGroup();

                    try {
                        Bootstrap bootStrap = new Bootstrap();
                        bootStrap.group(group)
                                // 논블럭 방식 적용
                                .channel(NioSocketChannel.class)
                                .handler(new ChannelInitializer<SocketChannel>() {
                                    @Override
                                    public void initChannel(SocketChannel ch) throws Exception {
                                        ChannelPipeline pipeline = ch.pipeline();
                                        // String 인/디코더 (default인 UTF-8)
                                        pipeline.addLast(new StringEncoder(), new StringDecoder());
                                        pipeline.addLast(new GatheringHandler());
                                        // IO 이벤트 핸들러
                                        pipeline.addLast(new Netty_handler(getUser_no()));
                                    }
                                });

                        Channel channel = bootStrap.connect("52.78.88.227", 8888).sync().channel();

                        // 어플리케이션 객체에 Channel 객체 저장하기
                        setChannel(channel);

                        channel.closeFuture().sync();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        group.shutdownGracefully();
                    }
                }
            }).start();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 채팅 서버 접속 서비스 구동
     ---------------------------------------------------------------------------*/
    public void chat_server_conn_service_run() {
        Intent serviceIntent = new Intent(this, Chat_service.class);
        serviceIntent.putExtra("user_no", user_no);
        startService(serviceIntent);
        Log.d(TAG, "#채팅채팅#" + "채팅 서버 접속 서비스 구동");
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 서버 통신 -- 해당 회의번호로 업로드 된 파일 리스트 가져와서 리턴하기
     ---------------------------------------------------------------------------*/
    @SuppressLint("StaticFieldLeak")
    public ArrayList<File_info> get_uploaded_file_list(Context context, final String this_meeting_no) {
        ArrayList<File_info> files = new ArrayList<>();
        final RetrofitService rs = ServiceGenerator.createService(RetrofitService.class);

        // 프로그래스 다이얼로그 호출
        show_progress(context, "파일 리스트업 중입니다");

        // 동기 호출
        try {
            final ArrayList<File_info> final_files = files;

            return new AsyncTask<Void, Void, ArrayList<File_info>>() {

                // 통신 끝나고 리턴한 뒤
                @Override
                protected void onPostExecute(ArrayList<File_info> file_infos) {
                    // 프로그레스 다이얼로그 dismiss
                    dismiss_progress();
                }

                // 통신 로직
                @Override
                protected ArrayList<File_info> doInBackground(Void... voids) {
                    try {
                        Call<ResponseBody> call_result = rs.get_share_file_list(
                                Static.GET_SHARE_FILE_LIST,
                                this_meeting_no);
                        Response<ResponseBody> list = call_result.execute();
                        String result = list.body().string();

                        try {
                            if(result.equals("fail")) {
                                logAndToast("예외발생: " + result);
                                final_files.clear();
                            }
                            else if(result.equals("no_result")) {
                                final_files.clear();
                            }
                            else {
                                // 길이가 긴 JSONString 출력하기
//                                print_long_Json_logcat(result, TAG);
                                // jsonString --> jsonObject
                                JSONObject jsonObject = new JSONObject(result);
                                // jsonObject --> jsonArray
                                JSONArray jsonArray = jsonObject.getJSONArray(JSON_TAG_SHARE_FILE_LIST);
                                Log.d(TAG, "jsonArray 개수: " + jsonArray.length());

                                // jsonArray 에서 jsonObject를 하나씩 가져와서,
                                // gson과 File_info 데이터 클래스를 이용해서 리턴할 ArrayList에 add 하기
                                Gson gson = new Gson();
                                for(int i=0; i<jsonArray.length(); i++) {
                                    String jsonString = jsonArray.getJSONObject(i).toString();
                                     File_info file = gson.fromJson(jsonString, File_info.class);
                                     final_files.add(file);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        return final_files;

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }.execute().get();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 서버 통신 -- 내 프로젝트 리스트 가져와서 리턴하기
     ---------------------------------------------------------------------------*/
    @SuppressLint("StaticFieldLeak")
    public ArrayList<Project> get_project_list() {

        ArrayList<Project> project_arr = new ArrayList<>();
        final RetrofitService rs = ServiceGenerator.createService(RetrofitService.class);

        // 동기 호출
        try {
            final ArrayList<Project> finalProject_arr = project_arr;

            return new AsyncTask<Void, Void, ArrayList<Project>>() {
                @Override
                protected ArrayList<Project> doInBackground(Void... voids) {
                    try {
                        Call<ResponseBody> call_result = rs.get_project_list(
                            Static.GET_PROJECT_LIST,
                            user_no);
                        Response<ResponseBody> list = call_result.execute();
                        String result = list.body().string();
                        Log.d(TAG, "프로젝트 리스트 서버 리턴 결과: " + result);

                        try {
                            if(result.equals("fail")) {
                                logAndToast("예외발생: " + result);
                                finalProject_arr.clear();
                            }
                            else if(result.equals("no_result")) {
                                finalProject_arr.clear();
                            }
                            else {
                                // 길이가 긴 JSONString 출력하기
                                print_long_Json_logcat(result, TAG);
                                // jsonString --> jsonObject
                                JSONObject jsonObject = new JSONObject(result);
                                // jsonObject -->jsonArray
                                JSONArray jsonArray = jsonObject.getJSONArray(JSON_TAG_PROJECT_LIST);
                                Log.d(TAG, "jsonArray 개수: " + jsonArray.length());

                                // 데이터 클래스로 파싱하기 위한 GSON 객체 생성
                                Gson gson = new Gson();

                                // jsonArray에서 jsonObject를 가지고 와서, parsing 하기
                                if(jsonArray.length() == 1) {
                                    // 프로젝트 지정이 안되어 있는 회의 개수
                                    int unspecified_project_count = jsonArray.getJSONObject(0).getInt("unspecified_project_count");

                                    //// 프로젝트 정보 parsing 해서 어레이에 넣기
                                    // 'specified_project_data' JSONString을 JSONObect로 파싱
                                    JSONArray jsonArray_for_project = new JSONArray(jsonArray.getJSONObject(0).getString("specified_project_data"));

                                    // gson 이용해서 project 객체로 변환해서, 그 project 객체 안에서, project_no 값을 가져와서,
                                    for(int k=0; k<jsonArray_for_project.length(); k++) {

                                        Project project = gson.fromJson(jsonArray_for_project.get(k).toString(), Project.class);

                                        // TODO: 더미데이터는 표시 안하기 위해 add 하지 않음
                                        if(project.getProject_status().equals("완료")) {
                                            continue;
                                        }

                                        finalProject_arr.add(project);

                                        // 서버로부터 받은 데이터가 잘 파싱되어 있는지 확인하기 위한 Log
                                        Log.d(TAG, "project.getProject_no(): " + project.getProject_no());
//                                        Log.d(TAG, "project.getProject_name(): " + project.getProject_name());
//                                        Log.d(TAG, "project.getProject_color(): " + project.getProject_color());
//                                        Log.d(TAG, "project.getProject_director_user_no(): " + project.getProject_director_user_no());
//                                        Log.d(TAG, "project.getProject_status(): " + project.getProject_status());
//                                        Log.d(TAG, "project.getProject_start_dt(): " + project.getProject_start_dt());
//                                        Log.d(TAG, "project.getProject_end_dt(): " + project.getProject_end_dt());
//                                        Log.d(TAG, "project.getProject_pw(): " + project.getProject_pw());
//                                        Log.d(TAG, "project.getMeeting_count(): " + project.getMeeting_count());
                                    }

                                    // 프로젝트 미지정된 회의가 있을때만
                                    if(unspecified_project_count > 0) {
                                        // 프로젝트 지정이 안되어 있는 회의 개수를 담는 가상의 '프로젝트' 폴더가 있다고 가정하고
                                        // 해당 '프로젝트' 폴더를 만들어서 어레이에 add 한다
                                        // 프로젝트 지정이 없는 회의개수를 담는 가상의 '프로젝트'의 project_no = 0;
                                        Project virtual_project = new Project();
                                        virtual_project.setMeeting_count(unspecified_project_count);
                                        virtual_project.setProject_no(0);
                                        virtual_project.setProject_color("grey");
                                        virtual_project.setProject_director_user_no(Integer.parseInt(getUser_no()));
                                        virtual_project.setProject_director_user_no(Integer.parseInt(getUser_no()));
                                        virtual_project.setProject_name("프로젝트 미지정 회의목록");
                                        virtual_project.setProject_start_dt("0000-00-00");
                                        virtual_project.setProject_end_dt("0000-00-00");

                                        finalProject_arr.add(0, virtual_project);
                                    }
                                }
                                Log.d(TAG, "finalProject_arr.size(): " + finalProject_arr.size());
                            }
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                        return finalProject_arr;

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }.execute().get();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 서버 통신 -- 내 파트너 리스트 가져와서 리턴하기
     ---------------------------------------------------------------------------*/
    @SuppressLint("StaticFieldLeak")
    public ArrayList<Users> get_partner_list() {

        ArrayList<Users> user_arr = new ArrayList<>();
        final RetrofitService rs = ServiceGenerator.createService(RetrofitService.class);

        // 동기 호출
        try {
            final ArrayList<Users> finalUser_arr = user_arr;

            return new AsyncTask<Void, Void, ArrayList<Users>>() {
                @Override
                protected ArrayList<Users> doInBackground(Void... voids) {
                    try {
                        Call<ResponseBody> call_result = rs.get_partner_list(
                                Static.GET_PARTNER_LIST,
                                user_no);
                        Response<ResponseBody> list = call_result.execute();
                        String result = list.body().string();

                        try {
                            if(result.equals("fail")) {
                                logAndToast("예외발생: " + result);
                                finalUser_arr.clear();
                            }
                            else if(result.equals("no_result")) {
                                finalUser_arr.clear();
                            }
                            else {
                                // 길이가 긴 JSONString 출력하기
                                print_long_Json_logcat(result, TAG);
                                // jsonString --> jsonObject
                                JSONObject jsonObject = new JSONObject(result);
                                // jsonObject --> jsonArray
                                JSONArray jsonArray = jsonObject.getJSONArray(JSON_TAG_PARTNER_LIST);
                                Log.d(TAG, "jsonArray 개수: " + jsonArray.length());

                                // jsonArray에서 jsonObject를 하나씩 가지고 와서,
                                // gson과 user 데이터클래스를 이용하여 user_arr에 add 하기
                                for(int i=0; i<jsonArray.length(); i++) {
                                    String jsonString = jsonArray.getJSONObject(i).toString();
                                    Gson gson = new Gson();
                                    Users user = gson.fromJson(jsonString, Users.class);
                                    finalUser_arr.add(user);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        return finalUser_arr;

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }.execute().get();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 서버 통신 -- 파트너 검색하기
     ---------------------------------------------------------------------------*/
    @SuppressLint("StaticFieldLeak")
    public ArrayList<Users> search_partners(final String with_domain, final String without_domain) {
        ArrayList<Users> user_arr = new ArrayList<>();
        final RetrofitService rs = ServiceGenerator.createService(RetrofitService.class);

        // 동기 호출
        try {
            final ArrayList<Users> finalUser_arr = user_arr;
            return new AsyncTask<Void, Void, ArrayList<Users>>() {

                @Override
                protected ArrayList<Users> doInBackground(Void... voids) {
                    try {
                        Call<ResponseBody> call = rs.search_partners(
                                Static.SEARCH_PARTNERS,
                                user_no, with_domain, without_domain);
                        Response<ResponseBody> call_result = call.execute();
                        String result = call_result.body().string();

                        try {
                            if(result.equals("fail")) {
                                logAndToast("예외발생: " + result);
                                finalUser_arr.clear();
                            }
                            else if(result.equals("no_result")) {
                                finalUser_arr.clear();
                            }
                            else {
                                // 길이가 긴 JSONString 출력하기
                                print_long_Json_logcat(result, TAG);
                                // jsonString --> jsonObject
                                JSONObject jsonObject = new JSONObject(result);
                                // jsonObject --> jsonArray
                                JSONArray jsonArray = jsonObject.getJSONArray(JSON_TAG_SEARCH_LIST);
                                Log.d(TAG, "jsonArray 개수: " + jsonArray.length());

                                // jsonArray에서 jsonObject를 하나씩 가지고 와서,
                                // gson과 user 데이터클래스를 이용하여 user_arr에 add 하기
                                for(int i=0; i<jsonArray.length(); i++) {
                                    String jsonString = jsonArray.getJSONObject(i).toString();
                                    Gson gson = new Gson();
                                    Users user = gson.fromJson(jsonString, Users.class);
                                    finalUser_arr.add(user);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        return finalUser_arr;

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }.execute().get();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 서버 통신 -- 종료된 영상회의의 정보를 서버로부터 받아온다
        사용 클래스 1) Meeting_result_D
     ---------------------------------------------------------------------------*/
    @SuppressLint("StaticFieldLeak")
    public String get_ended_meeting_result(
            final String ended_meeting_no, final String ended_subject_user_no) {
        final RetrofitService rs = ServiceGenerator.createService(RetrofitService.class);

        // 동기 호출
        try {
            return new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... voids) {
                    try {
                        Call<ResponseBody> call_result = rs.get_ended_meeting_result(
                                Static.GET_ENDED_MEETING_RESULT,
                                getUser_no(),
                                ended_subject_user_no,
                                ended_meeting_no);
                        Response<ResponseBody> list = call_result.execute();
                        String result = list.body().string();

                        if(result.equals("fail")) {
                            logAndToast("예외발생: " + result);
                        }
                        else {
                            // 길이가 긴 JSONString 출력하기
                            print_long_Json_logcat(result, TAG);
                            return result;
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }.execute().get();
        }catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 서버 통신 -- 해당 프로젝트에 지정된 회의 정보들을 받아온다
     사용 클래스) Project_meeting_result_list_A
     ---------------------------------------------------------------------------*/
    @SuppressLint("StaticFieldLeak")
    public ArrayList<Meeting_room> get_meeting_room_list(final int project_no) {

        ArrayList<Meeting_room> meeting_room_arr = new ArrayList<>();
        final RetrofitService rs = ServiceGenerator.createService(RetrofitService.class);

        // 동기 호출
        try {
            final ArrayList<Meeting_room> final_meeting_room_arr = meeting_room_arr;

            return new AsyncTask<Void, Void, ArrayList<Meeting_room>>() {
                @Override
                protected ArrayList<Meeting_room> doInBackground(Void... voids) {
                    try {
                        Call<ResponseBody> call_result = rs.get_meeting_room_list(
                                Static.GET_MEETING_ROOM_LIST,
                                getUser_no(),
                                project_no);
                        Response<ResponseBody> list = call_result.execute();
                        String result = list.body().string();

                        try {
                            if(result.equals("fail")) {
                                logAndToast("예외발생: " + result);
                                final_meeting_room_arr.clear();
                            }
                            else if(result.equals("no_result")) {
                                final_meeting_room_arr.clear();
                            }
                            else {
                                // 길이가 긴 JSONString 출력하기
                                print_long_Json_logcat(result, TAG);
                                // jsonString --> jsonObject
                                JSONObject jsonObject = new JSONObject(result);
                                // jsonObject -->jsonArray
                                JSONArray jsonArray = jsonObject.getJSONArray(JSON_TAG_MEETING_ROOM_LIST);
                                Log.d(TAG, "jsonArray 개수: " + jsonArray.length());

                                // 데이터 클래스로 파싱하기 위한 GSON 객체 생성
                                Gson gson = new Gson();

                                for(int i=0; i<jsonArray.length(); i++) {
                                    Meeting_room meeting_room = gson.fromJson(jsonArray.get(i).toString(), Meeting_room.class);
                                    final_meeting_room_arr.add(meeting_room);
                                    // 서버로부터 받은 데이터가 잘 파싱되어 있는지 확인하기 위한 Log
                                    Log.d(TAG, "meeting_room.getMeeting_no(): " + meeting_room.getMeeting_no());
                                }
                                Log.d(TAG, "final_meeting_room_arr.size(): " + final_meeting_room_arr.size());
                            }
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                        return final_meeting_room_arr;

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }.execute().get();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**---------------------------------------------------------------------------
     메소드 ==> color 를 나타내는 String 값으로, 실제 color int 값을 찾아, 반환하는 메소드
        사용 클래스 1) Meeting_result_D
     ---------------------------------------------------------------------------*/
    public int project_color(String selected_project_color) {
        // 해당 프로젝트 컬러에 따른 색 int value 값을 가져온다
        int color_int_value_index = -1;
        for(int i=0; i<folder_color_int_value.length; i++) {
            if(folder_color_str[i].equals(selected_project_color)) {
                color_int_value_index = i;
            }
        }
        Log.d(TAG, "color_int_value_index: " + color_int_value_index);

        if(color_int_value_index != -1) {
            int color_int_value = folder_color_int_value[color_int_value_index];
            return color_int_value;
        }
        else {
            return -1;
        }
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 서버 통신 -- 지정한 프로젝트 no를 DB에 저장한다
        매개변수 1) 지정한 프로젝트 번호
        매개변수 2) 지정할 회의 번호

        사용 클래스 1) Meeting_result_D
     ---------------------------------------------------------------------------*/
    @SuppressLint("StaticFieldLeak")
    public boolean assign_project(final int selected_project_no, final String meeting_no) {
        final RetrofitService rs = ServiceGenerator.createService(RetrofitService.class);

        Log.d(TAG, "selected_project_no: " + selected_project_no);
        Log.d(TAG, "meeting_no: " + meeting_no);

        // 동기 호출
        try {
            return new AsyncTask<Void, Void, Boolean>() {
                @Override
                protected Boolean doInBackground(Void... voids) {
                    try {
                        Call<ResponseBody> call_result = rs.assign_project(
                                Static.ASSIGN_PROJECT,
                                getUser_no(),
                                selected_project_no,
                                meeting_no);
                        Response<ResponseBody> list = call_result.execute();
                        String result = list.body().string();
                        Log.d(TAG, "assign_project_result: " + result);

                        if(result.equals("fail")) {
                            logAndToast("예외발생: " + result);
                            return false;
                        }
                        else if(result.equals("success")){
                            return true;
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return false;
                }
            }.execute().get();
        }catch(Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 채팅방 인원이 3명이상일 때, 채팅방 리스트에 보여주는 대표 이미지를 만들 때 사용.
                이미지뷰 자체가 4분할 구성이기 때문에, 남은 1/4 공간을
                아무것도 없는 회색 으로 채우기 위해, 회색 비트맵 반환
     ---------------------------------------------------------------------------*/
    public Bitmap get_blank_gray_view() {
            BitmapDrawable drawable = (BitmapDrawable) getResources().getDrawable(R.drawable.none_user);
            Bitmap bitmap = drawable.getBitmap();

        return bitmap;
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 긴 JsonString Log 출력하기
     ---------------------------------------------------------------------------*/
    public void print_long_Json_logcat(String json_log, String TAG) {

        String temp_json = json_log;
        int log_index = 1;
        try {
            while (temp_json.length() > 0) {
                if (temp_json.length() > 3000) {
                    Log.d(TAG, "json - " + log_index + " : "
                            + temp_json.substring(0, 3000));
                    temp_json = temp_json.substring(3000);
                    log_index++;
                } else {
                    Log.d(TAG, "json - " + log_index + " :" + temp_json);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 서버 통신 -- 유저 한명의 정보 가져오기
     ---------------------------------------------------------------------------*/
    @SuppressLint("StaticFieldLeak")
    public Users get_user_info(final String target_user_no) {

        final Users[] users = {new Users()};
        final RetrofitService rs = ServiceGenerator.createService(RetrofitService.class);

        // 동기 호출
        try {
            return new AsyncTask<Void, Void, Users>() {
                @Override
                protected Users doInBackground(Void... voids) {
                    try {
                        Call<ResponseBody> call = rs.get_user_info(
                                Static.GET_USER_INFO,
                                target_user_no);
                        Response<ResponseBody> call_result = call.execute();
                        String result = call_result.body().string();
                        Log.d(TAG, "result: " + result);

                        if(result.equals("fail")) {
                            logAndToast("예외발생: " + result);
                        }
                        else {
                            Gson gson = new Gson();
                            users[0] = gson.fromJson(result, Users.class);

                            // 내 정보라면, 그냥 혹시 모르니 데이터 저장
                            if(users[0].getUser_no().equals(user_no)) {
                                join_path = users[0].getJoin_path();
                                join_dt = users[0].getJoin_dt();
                                user_email = users[0].getUser_email();
                                user_nickname = users[0].getUser_nickname();
                                present_meeting_in_ornot = users[0].getPresent_meeting_in_ornot();
                                user_img_filename = users[0].getUser_img_filename();
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return users[0];
                }
            }.execute().get();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 카메라 촬영 이미지 파일 이름 네이밍을 위한 메소드
     ---------------------------------------------------------------------------*/
    public String now() {
        SimpleDateFormat format_for_save = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.KOREA);
        long time_mil = System.currentTimeMillis();
        Date date = new Date(time_mil);
        String result = format_for_save.format(date);
        return result;
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 원하는 포맷으로 현재 시간, 현재 date를 가져오기 위한 메소드
     ---------------------------------------------------------------------------*/
    public String get_time(String format) {
        SimpleDateFormat received_fotmat = new SimpleDateFormat(format, Locale.KOREA);
        long time_mil = System.currentTimeMillis();
        Date date = new Date(time_mil);
        String result = received_fotmat.format(date);
        return result;
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 채팅 로그 전송 시각을 서버로부터 받아서, 기기 로컬(국가)에 맞게 변환
                조건에 맞게 다른 result 값을 반환

                1. request - chatroom list
                  - 채팅방 리스트에 표시되는 가장 최근 메세지의 시간을 나타내기 위한 String 값 리턴
                2. request - chat_log list
                  - 채팅방 안에 표시되는 메세지의 시간을 나타내기 위한 String 값 리턴
     ---------------------------------------------------------------------------*/
    public String chat_log_transmission_time(long server_trans_time, String request) {
        // 기기 Locale 타입 가져오기
        Locale systemLocale = getApplicationContext().getResources().getConfiguration().locale;
        long time_mil = server_trans_time;
        Date date = new Date(time_mil);

        SimpleDateFormat format_until_day = new SimpleDateFormat("yyyy-MM-dd", systemLocale);
        SimpleDateFormat format_until_month = new SimpleDateFormat("yyyy-MM", systemLocale);
        SimpleDateFormat format_day = new SimpleDateFormat("dd", systemLocale);
        SimpleDateFormat format_time = new SimpleDateFormat("HH:mm", systemLocale);

        if(request.equals("chatroom_list")) {
            // 채팅 로그 서버 전송시간이 오늘이라면
            if(get_time("yyyy-MM-dd").equals(format_until_day.format(date))) {
                return format_time.format(date);
            }
            // 채팅 로그 서버 전송시간이 어제라면
            else if(get_time("yyyy-MM").equals(format_until_month.format(date)) &&
                    (Integer.parseInt(get_time("dd"))-1) == Integer.parseInt(format_day.format(date))) {
                return "어제";
            }
            // 위 둘다 아니라면
            else {
                return format_until_day.format(date);
            }
        }
        else if(request.equals("chat_log")) {
            return format_time.format(date);
        }
        return null;
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 채팅방 안에서 채팅을 주고 받는 동안에 변경되는 날짜를 변경을 감지하고,
                변경된 경우 해당 날짜를 리턴하는 메소드
     ---------------------------------------------------------------------------*/
    public String check_change_date(long previous_msg_trans_time,
                                    long target_msg_trans_time,
                                    boolean check_skip) {
        // 캘린더 객체
        Calendar cal = Calendar.getInstance();
        // 기기 Locale 타입 가져오기
        Locale systemLocale = getApplicationContext().getResources().getConfiguration().locale;
        // 이전 메세지와, 현재 메세지를 date 형태로 변환
        Date previous_msg_date = new Date(previous_msg_trans_time);
        Date target_msg_date = new Date(target_msg_trans_time);

        // 리턴할 때 만들 String값을 만들기 위한 dateFormat
        SimpleDateFormat format_year = new SimpleDateFormat("yyyy", systemLocale);
        SimpleDateFormat format_month = new SimpleDateFormat("M", systemLocale);
        SimpleDateFormat format_day = new SimpleDateFormat("dd", systemLocale);

        // 체크 필요 없이 그냥 날짜를 리턴하면 되는 경우
        // ==> 해당 채팅방의 첫번째 메세지일 때
        if(check_skip) {
            String year = format_year.format(target_msg_date);
            String month = format_month.format(target_msg_date);
            String day = format_day.format(target_msg_date);
            int num = cal.get(Calendar.DAY_OF_WEEK)-1;
            String today = weekDay[num];

            return year + "년 " + month + "월 " + day + "일 " + today;
        }

        // 해당 채팅방의 첫번째 메시지가 아닌 경우, 즉 비교 대상이 있는 경우 ==> 체크
        else if(!check_skip) {
            // 비교할 dateFormat
            SimpleDateFormat format_until_day = new SimpleDateFormat("yyyy-MM-dd", systemLocale);

            // 두 메세지의 "yyyy-MM-dd" String 비교값이 다르다면, 즉 날짜가 변경되었다면
            if(!format_until_day.format(previous_msg_date).equals(format_until_day.format(target_msg_date))) {
                String year = format_year.format(target_msg_date);
                String month = format_month.format(target_msg_date);
                String day = format_day.format(target_msg_date);
                int num = cal.get(Calendar.DAY_OF_WEEK)-1;
                String today = weekDay[num];

                return "changed" + Static.SPLIT + year + "년 " + month + "월 " + day + "일 " + today;
            }
            // 변경되지 않았다면
            return "not_changed" + Static.SPLIT + "";
        }

        return null;
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 프로그레스 다이얼로그 show
     ---------------------------------------------------------------------------*/
    public void show_progress(Context context, String msg) {
        progressDialog = new ProgressDialog(context);
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


    /**---------------------------------------------------------------------------
     메소드 ==> 해쉬맵 checked_files clear(초기화)
     ---------------------------------------------------------------------------*/
    public void init_checked_files() {
        if(checked_files != null) {
            checked_files.clear();
        }
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 해쉬맵 files_for_upload clear(초기화)
     ---------------------------------------------------------------------------*/
    public void init_files_for_upload() {
        if(files_for_upload != null) {
            files_for_upload.clear();
        }
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 멀티 파일 업로드 -- 동기
     ---------------------------------------------------------------------------*/
    @SuppressLint("StaticFieldLeak")
    public void upload_multi_files_1(final Context context) {

        final int total_file_nums = files_for_upload.size();
        final Long[] total_file_size = {0L};

        final RetrofitService rs = ServiceGenerator.createService(RetrofitService.class);

        // 동기 호출
        try {
            new AsyncTask<String, Void, Long>() {
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    show_progress(context, "이미지 업로드 중입니다");
                }

                @Override
                protected Long doInBackground(String... params) {

                    Iterator<String> iterator = files_for_upload.keySet().iterator();
                    // 루프 - 파일 사이즈를 구하기 위한
                    Long temp_total_file_size = 0L;
                    while(iterator.hasNext()) {
                        String value = files_for_upload.get(iterator.next());

                        File file = new File(value);
                        temp_total_file_size = temp_total_file_size + file.length();
                    }
                    total_file_size[0] = temp_total_file_size;
                    Log.d(TAG, "업로드할 파일들의 총 개수: " + files_for_upload.size());
                    Log.d(TAG, "업로드할 파일들의 총 크기: " + total_file_size[0] + " bytes");

                    // 테스트 코드
//                    files_for_upload.clear();

                    Iterator<String> iterator_2 = files_for_upload.keySet().iterator();
                    Long uploaded_file_size = 0L;

                    // 루프 - 파일 업로드를 위한
                    while(iterator_2.hasNext()) {
                        String value = files_for_upload.get(iterator_2.next());

                        // 파일 객체 생성
                        final File file = new File(value);

                        // 확장자만 분류
                        int Idx = file.getName().lastIndexOf(".");
                        String format = file.getName().substring(Idx+1);
                        Log.d(TAG, "format: " + format);

                        // RequestBody 생성 from file
                        RequestBody requestFile = RequestBody.create(MediaType.parse("image/" + format), file);
                        MultipartBody.Part body =
                                MultipartBody.Part.createFormData("image", file.getName(), requestFile);

                        // user_no
                        RequestBody user_no = RequestBody.create(MediaType.parse("text/plain"), getUser_no());

                        // meeting_no
                        RequestBody meeting_no = RequestBody.create(MediaType.parse("text/plain"), getMeeting_no());

                        Call<ResponseBody> call = rs.upload_multi_files(
                                Static.UPLOAD_MULTI_FILES,
                                user_no, meeting_no, body);
                        try {
                            Response<ResponseBody> call_result = call.execute();
                            String result = call_result.body().string();
                            Log.d(TAG, "result: " + result);

                            if(result.equals("fail")) {
                                Log.d(TAG, "업로드 실패: " + result);
                            }
                            else if(result.equals("success")) {
                                Log.d(TAG, file.getName() + "업로드 성공!!!!");
                                uploaded_file_size = uploaded_file_size + file.length();
                            }
                            else {
                                logAndToast("파일 업로드 오류" + result);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            return 0L;
                        }
                    }
                    return uploaded_file_size;
                }

                @Override
                protected void onPostExecute(Long size) {
                    super.onPostExecute(size);
                    Log.d(TAG, "uploaded_file_size: " + size);
                    Log.d(TAG, "total_file_size: " + total_file_size[0]);

                    dismiss_progress();
                    logAndToast(String.valueOf(total_file_nums) + "개 파일, 업로드 완료");

                    BusProvider.getBus().register(this);        // otto 등록
                    // otto 를 통해, 프래그먼트로 이벤트 전달하기
                    Event.Myapp__Call_F myapp__call_f = new Event.Myapp__Call_F("upload", "end",
                            0, 0, "", 0, 0, 0);
                    BusProvider.getBus().post(myapp__call_f);
                    BusProvider.getBus().unregister(this);      // otto 등록 해제
                }
            }.execute();

        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**---------------------------------------------------------------------------
     메소드 ==> PDF 파일 있으면 이미지로 변환한 뒤, 멀티 파일 업로드 메소드 호출
     ---------------------------------------------------------------------------*/
    public void check_pdf_files(String contain_padf_file_orNot, Context context) {

        // RemoteMeeting 디렉토리가 존재하지 않으면 디렉토리 생성
        File folder = new File(sdPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        // PDF 파일이 있다면, PDF 파일 걸러서 ArrayList 에 담기
        if(contain_padf_file_orNot.equals("true")) {

            // Pdfbox 사용을 위한 init 메소드 호출
            setup();

            // checked_files Iterator
            Iterator<String> iterator = checked_files.keySet().iterator();

            // Pdf 파일들 리스트만 따로 담을 temp arr 생성
            ArrayList<String> temp_pdf_files_arr = new ArrayList<>();

            // 루프 돌면서 pdf 파일 찾기
            while(iterator.hasNext()) {
                String key = iterator.next();
                Log.d(TAG, "key: " + key);
                String value = checked_files.get(key);
                Log.d(TAG, "value: " + value);

                // 확장자만 분류
                int Idx = key.lastIndexOf(".");
                String format = key.substring(Idx+1);
                Log.d(TAG, "format: " + format);

                // ArrayList Add
                if(format.equals("pdf")) {
                    temp_pdf_files_arr.add(value);
                }
                // PDF가 아닌 파일들은 files_for_upload 해쉬맵에 추가
                else {
                    files_for_upload.put(key, value);
                    Log.d(TAG, "files_for_upload 해쉬맵에 추가");
                    Log.d(TAG, "file_name_with_format: " + key);
                    Log.d(TAG, "canonicalPath: " + value);
                }
            }

            // pdf 파일(canonicalPath) 리스트, 변환 로직으로 넘기기
            renderFile(temp_pdf_files_arr, context);
        }

        // PDF 파일이 없다면
        else if(contain_padf_file_orNot.equals("false")) {
            // 루프 돌면서 files_for_upload 해쉬맵으로 데이터 복사
            // checked_files Iterator
            Iterator<String> iterator = checked_files.keySet().iterator();
            // 루프 돌면서 pdf 파일 찾기
            while(iterator.hasNext()) {
                String key = iterator.next();
                String value = checked_files.get(key);

                files_for_upload.put(key, value);
                Log.d(TAG, "files_for_upload 해쉬맵에 추가");
                Log.d(TAG, "file_name_with_format: " + key);
                Log.d(TAG, "canonicalPath: " + value);
            }

            /** 이미지 파일 업로드 메소드 호출 */
            Call_F.comment.setText("Images, on uploading");
            upload_multi_files_1(context);
        }
    }


    /**---------------------------------------------------------------------------
     메소드 ==> pdfbox 라이브러리 사용을 위한 Initializes
     ---------------------------------------------------------------------------*/
    private void setup() {
        // Enable Android-style asset loading (highly recommended)
        PDFBoxResourceLoader.init(getApplicationContext());
        // Find the root of the external storage.
        root = android.os.Environment.getExternalStorageDirectory();
    }


    /**---------------------------------------------------------------------------
     메소드 ==> Pdf file TO image file 1.
     ---------------------------------------------------------------------------*/
    @SuppressLint({"SetTextI18n", "HandlerLeak"})
    public void renderFile(final ArrayList<String> arr, Context context) {

        // 1개의 PDF 파일의 변환이 완료됐을 때 콜백 받을 핸들러 생성
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                /** 파일 변환 - 시작, 호출 콜백*/
                if(msg.what == 5) {
                    // 넘어온 PDF 파일의 개수 구하기
                    int pdf_files_count = arr.size();
                    Log.d(TAG, "pdf_files_count: " + pdf_files_count);

                    if(pdf_files_count > 0) {
                        // 파일 이름 추출
                        String[] temp = arr.get(0).split("[/]");
                        String fileName = temp[temp.length-1];

                        // otto 를 통해, 프래그먼트로 이벤트 전달하기
                        Event.Myapp__Call_F myapp__call_f = new Event.Myapp__Call_F("progress", "start",
                                pdf_files_count, 1, fileName, 0, 0, 0);
                        BusProvider.getBus().post(myapp__call_f);
                    }
                }

                /** 페이지 전환율 - 한 페이지 변환 시작, 호출 콜백 */
                else if(msg.what == 4) {
                    int pdf_files_count = arr.size();
                    Log.d(TAG, "pdf_files_count: " + pdf_files_count);

                    if(pdf_files_count > 0) {
                        int total = msg.getData().getInt("total", 0);
                        int current_pdf_page = msg.getData().getInt("current_pdf_page", 0);

                        // otto 를 통해, 프래그먼트로 이벤트 전달하기
                        Event.Myapp__Call_F myapp__call_f = new Event.Myapp__Call_F("progress", "progress",
                                -1, 1, "", 0, total, current_pdf_page);
                        BusProvider.getBus().post(myapp__call_f);
                    }
                }

                /** 페이지 전환율 - 한 페이지 변환 완료, 호출 콜백 */
                else if(msg.what == 2) {
                    int total = msg.getData().getInt("total", 0);
                    int current_pdf_page = msg.getData().getInt("current_pdf_page", 0);
                    int percent = msg.getData().getInt("percent", 0);

                    // otto 를 통해, 프래그먼트로 이벤트 전달하기
                    Event.Myapp__Call_F myapp__call_f = new Event.Myapp__Call_F("progress", "ing",
                            -1, -1, "", percent, total, current_pdf_page);
                    BusProvider.getBus().post(myapp__call_f);
                }

                /** 페이지 전환율 - 100%, 호출 콜백 */
                else if(msg.what == 3) {
                    int total = msg.getData().getInt("total", 0);

                    // otto 를 통해, 프래그먼트로 이벤트 전달하기
                    Event.Myapp__Call_F myapp__call_f = new Event.Myapp__Call_F("progress", "ing",
                            -1, -1, "", 100, total, total);
                    BusProvider.getBus().post(myapp__call_f);
                }

                /** 파일 변환 - 파일 한개 변환 완료 했을때 호출하는 콜백 +
                 *  파일 변환 - 파일 모두 변환 완료 했을때 호출하는 콜백 */
                else if(msg.what == 0) {
                    Log.d(TAG, "제거 전 arr.size(): " + arr.size());
                    arr.remove(0);
                    Log.d(TAG, "제거 후 arr.size(): " + arr.size());

                    // arr 에 pdf파일 리스트가 모두 없어질때까지 무한루프
                    if(arr.size() > 0) {
                        over_and_over_convert(arr.get(0));
                    }
                    else if(arr.size() == 0) {
//                        logAndToast("PDF 파일 변환이 완료되었습니다");
                        // otto 를 통해, 프래그먼트로 이벤트 전달하기
                        Event.Myapp__Call_F myapp__call_f = new Event.Myapp__Call_F("progress", "end",
                                PDF_converting_exception_file_count, -1, "", -1, 0, 0);
                        BusProvider.getBus().post(myapp__call_f);
                        // otto 등록 해제
                        BusProvider.getBus().unregister(this);
                        // exception 파일 개수 초기화
                        setPDF_converting_exception_file_count(0);
                    }
                }
                /** 파일 변환 - 파일 변환중 Exception 에러가 발생했을 때 호출하는 콜백 +
                 *  파일 변환 - 파일 모두 변환 완료 했을때 호출하는 콜백 */
                else if(msg.what == 1) {
                    // exception 파일 개수 추가
                    PDF_converting_exception_file_count++;

                    String[] temp = arr.get(0).split("[/]");
                    String fileName = temp[temp.length-1];
                    int Idx = fileName.lastIndexOf(".");
                    final String only_fileName = fileName.substring(0, Idx);
                    logAndToast(only_fileName + " 파일이 변환중 에러로 업로드에서 제외됩니다.");

                    Log.d(TAG, "제거 전 arr.size(): " + arr.size());
                    arr.remove(0);
                    Log.d(TAG, "제거 후 arr.size(): " + arr.size());

                    // 토스트 뜰 시간 벌기 위해 0.5초 뒤에 실행
                    new Handler().postDelayed(new Runnable() {
                        @Override public void run() {
                            // arr 에 pdf파일 리스트가 모두 없어질때까지 무한루프
                            if(arr.size() > 0) {
                                over_and_over_convert(arr.get(0));
                            }
                            else if(arr.size() == 0) {
                                // exception 파일 개수 초기화
//                                logAndToast("PDF 파일 변환이 완료되었습니다");
                                // otto 를 통해, 프래그먼트로 이벤트 전달하기
                                Event.Myapp__Call_F myapp__call_f = new Event.Myapp__Call_F("progress", "end",
                                        PDF_converting_exception_file_count, -1, "", -1, 0, 0);
                                BusProvider.getBus().post(myapp__call_f);
                                // otto 등록 해제
                                BusProvider.getBus().unregister(this);
                                // exception 파일 개수 초기화
                                setPDF_converting_exception_file_count(0);
                            }
                        }
                    }, 500);
                }
            }
        };

        // otto 등록
        BusProvider.getBus().register(this);

        /** renderFile 최초 호출될 때 pdf 첫번째 파일 변환 실행 */
        over_and_over_convert(arr.get(0));
    }


    /**---------------------------------------------------------------------------
     메소드 ==> Pdf file TO image file 2.
     ---------------------------------------------------------------------------*/
    public void over_and_over_convert(final String canonicalPath) {
        try {
            File target_file = new File(canonicalPath);
            Log.d(TAG, "target_file_canonicalPath: " + canonicalPath);

            // Load in an already created PDF
            final PDDocument document = PDDocument.load(target_file);
            // Create a renderer for the document
            final PDFRenderer renderer = new PDFRenderer(document);

            // 확장자를 제외한 파일 이름
            String pdf_fileName = target_file.getName();
            int Idx = pdf_fileName.lastIndexOf(".");
            final String only_fileName = pdf_fileName.substring(0, Idx);
            Log.d(TAG, "only_fileName:" + only_fileName);

            // PDF 파일 총 페이지 수
            int total_pages_num = document.getNumberOfPages();
            Log.d(TAG, "PDF 파일 총 페이지 수: " + total_pages_num);

            new Thread() {
                @Override
                public void run() {
                    super.run();

                    try {
                        for(int i=1; i<=document.getNumberOfPages(); i++) {

                            /** 파일 변환 - 시작 시에, 핸들러를 통해 변환 완료 알림 */
                            if(i == 1) {
                                Log.d(TAG, "========================================");
                                Log.d(TAG, only_fileName + ".pdf: 이미지 변환 시작");
                                handler.sendEmptyMessage(5);
                            }

                            /** 페이지 전환율 - 진척도, 핸들러를 통해 알림 */
                            int value_1 = i;
                            int total_1 = document.getNumberOfPages();

                            Message msg_1 = new Message();
                            Bundle data_1 = new Bundle();
                            data_1.putInt("current_pdf_page", value_1);
                            data_1.putInt("total", total_1);
                            msg_1.what = 4;

                            msg_1.setData(data_1);
                            handler.sendMessage(msg_1);

                            // =================================================================
                            // ================== 한개의 페이지 이미지 변환 시작 ==================
                            // Render the image to an RGB Bitmap
                            Bitmap pageImage = renderer.renderImage(i-1, 1, Bitmap.Config.RGB_565);
                            // Save the render result to an image
                            String created_fileName = String.valueOf(i) + "_" + only_fileName + ".png";
                            String path = sdPath + "/" + created_fileName;
                            File renderFile = new File(path);
                            FileOutputStream fileOut = new FileOutputStream(renderFile);
                            pageImage.compress(Bitmap.CompressFormat.PNG, 100, fileOut);
                            fileOut.close();
                            Log.d(TAG, created_fileName + " 변환");
                            // ================== 한개의 페이지 이미지 변환 종료 ==================
                            // =================================================================
                            /** files_for_upload 해쉬맵에 추가 */
                            files_for_upload.put(created_fileName, path);
                            Log.d(TAG, "files_for_upload 해쉬맵에 추가");
                            Log.d(TAG, "file_name_with_format: " + created_fileName);
                            Log.d(TAG, "canonicalPath: " + path);

                            /** 페이지 전환율 - 하나 변환 완료됐을 때, 핸들러를 통해 알림 */
                            if(i != document.getNumberOfPages()) {

                                // 파일 변환 percent 구하기
                                int value_2 = i;
                                int total_2 = document.getNumberOfPages();
                                int rate = (int)((double)value_2/(double)total_2 * 100);
                                Log.d(TAG, "PDF 파일 변환 진척도: " + rate + "%");

                                Message msg_2 = new Message();
                                Bundle data_2 = new Bundle();
                                data_2.putInt("current_pdf_page", value_2);
                                data_2.putInt("total", total_2);
                                data_2.putInt("percent", rate);
                                msg_2.what = 2;

                                msg_2.setData(data_2);
                                handler.sendMessage(msg_2);
                            }

                            /** 페이지 전환율 - 100% 된 것도 핸들러롤 통해 변환 완료 알림 */
                            if(i == document.getNumberOfPages()) {
                                int value_3 = i;
                                Message msg_3 = new Message();
                                Bundle data_3 = new Bundle();
                                data_3.putInt("total", value_3);
                                msg_3.what = 3;
                                msg_3.setData(data_3);
                                handler.sendMessage(msg_3);
                            }

                            /** 파일 변환 - 완료 시에, 핸들러를 통해 변환 완료 알림 */
                            if(i == document.getNumberOfPages()) {
                                handler.sendEmptyMessage(0);
                                Log.d(TAG, only_fileName + ".pdf: 이미지로 변환 완료");
                                document.close();
                            }
                        }
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                        Log.d(TAG, "Pdf 변환 에러!!!!!!!!!!! - " + e.getMessage());
                        handler.sendEmptyMessage(1);
                    }
                }
            }.start();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 현재 서비스가 구동중인지 확인하는 메소드 -- 로그인 user_no와 구동중인 서비스의 user_no 비교
     ---------------------------------------------------------------------------*/
    public boolean isServiceRunningCheck() {
        // 액티비티 서비스에 대한 액티비티 매니저를 가져온다
        ActivityManager manager = (ActivityManager) this.getSystemService(Activity.ACTIVITY_SERVICE);
        // 가져온 서비스 목록을 돌면서 내 앱에서 구동하는 서비스 목록(Chat_service)와 일치하는지 확인한다
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            // Chat_service 가 구동중이라면
            if (Chat_service.class.getName().equals(service.service.getClassName())) {
                // 구동중인 Chat_service 의 user_no 와 로그인한 user_no가 일치하는지 확인한다, 일치할때만 true 반환
                // 일치하지 않는다면, 다른 아이디로 로그인한 경우이므로 채팅 서버에 재접속하여, channel 정보를 변경/갱신한다
                if(Chat_service.user_no.equals(user_no)) {
                    Log.d(TAG, "#채팅채팅#" + "구동중인 서비스의 user_no와 로그인한 user_no가 일치함 --> 동일인 로그인");
                    return true;
                }
                else {
                    Log.d(TAG, "#채팅채팅#" + "구동중인 서비스의 user_no와 로그인한 user_no가 일치하지 않음 --> 다른 아이디로 로그인");
                    // 현재 구동중인 서비스 중지시키기
                    Chat_service.getInstance().stopSelf();
                    // 현재 netty Channel 닫고, null 처리
                    channel.close();
                    channel = null;
                    return false;
                }
            }
        }
        Log.d(TAG, "#채팅채팅#" + "Chat_service 미구동");
        return false;
    }

    /**---------------------------------------------------------------------------
     메소드 ==> Netty 를 통해 연결된 서버로 통신메세지 보내기
     ---------------------------------------------------------------------------*/
    public void send_to_server(final Data_for_netty data) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                /** try 1.*/
                Gson gson = new GsonBuilder().setLenient().create();
                String data_string = gson.toJson(data);
                Log.d(TAG, "send_to_server_data_string: " + data_string);

                getChannel().writeAndFlush(data_string);
            }
        }).start();
    }

    /** MultiDex 문제 해결*/
    // 에러 메세지
    // Caused by: java.lang.ClassNotFoundException: Didn't find class "com.example.jyn.remotemeeting.Activity.Main_before_login_A" on path:
    // DexPathList[[zip file "/data/app/com.example.jyn.remotemeeting-2.apk"],nativeLibraryDirectories=[/data/app-lib/com.example.jyn.remotemeeting-2, /vendor/lib, /system/lib]]
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 현재 기기에 최상단에 올라와 있는 액티비티 클래스 확인하기
     ---------------------------------------------------------------------------*/
    public String getTop_activity() {
        ActivityManager activityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> info;
        info = activityManager.getRunningTasks(1);

        String top_activity_name = "";
        for (ActivityManager.RunningTaskInfo runningTaskInfo : info) {
            top_activity_name = runningTaskInfo.topActivity.getClassName();
            Log.d(TAG, top_activity_name);
        }
        Log.d(TAG, "current_activity_with_package_name: " + top_activity_name);
        String[] temp = top_activity_name.split("[.]");
        String curr_activity_name = temp[temp.length-1];
        Log.d(TAG, "curr_activity_name: " + curr_activity_name);
        return curr_activity_name;
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 해당 채팅방에서 내가 서버로부터 받은 'first / last' msg_no를 서버 DB에 업데이트 하기
        마지막으로 받은 msg_no를 서버DB 'my_chat_room_info'테이블의 'last_read_msg_no'로 업데이트 한다
        처음으로 받은 msg_no를 서버DB 'my_chat_room_info'테이블의 'first_read_msg_no'로 업데이트 한다
        -- 단, 서버DB 'my_chat_room_info'테이블의 'first_read_msg_no'가 '0'이 아닌 경우에만!
        -- 즉, 최초 1회에 한해서만 업데이트 한다
     ---------------------------------------------------------------------------*/
    public void update_first_last_msg_no(
            final int chat_room_no, final int first_read_msg_no, final int last_read_msg_no, final String request) {

        Log.d(TAG, "first_read_msg_no: " + first_read_msg_no);
        Log.d(TAG, "last_read_msg_no: " + last_read_msg_no);
        RetrofitService rs = ServiceGenerator.createService(RetrofitService.class);
        Call<ResponseBody> call = rs.update_first_last_msg_no(
                Static.UPDATE_FIRST_LAST_MSG_NO,
                getUser_no(), chat_room_no, first_read_msg_no, last_read_msg_no);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String update_first_last_msg_no_result = response.body().string();
                    Log.d(TAG, "update_first_last_msg_no_result: "+update_first_last_msg_no_result);

                    boolean success = false;
                    int first_update_msg_no = -1;

                    if(update_first_last_msg_no_result.contains(Static.SPLIT)) {
                        String[] temp = update_first_last_msg_no_result.split(Static.SPLIT);
                        if(temp[0].equals("success")) {
                            success = true;
                            first_update_msg_no = Integer.parseInt(temp[1]);
                        }
                    }

                    // 업데이트 성공, 그리고 first_update_msg_no를 잘 가져왔다면
                    if(success && first_update_msg_no!=-1) {

                        /**
                         * 서버에 내가 읽은 'first_read_msg_no, last_read_msg_no'가 업데이트 되고 나면,
                         * netty를 통해서 해당 채팅방에 들어와 있는 사람에게
                         * 'first_read_msg_no / last_read_msg_no' 를 전달해서
                         *  해당 범위에 있는 msg_no 정보를 업데이트 하라는 내용을 전달한다
                         */
                        Data_for_netty data = new Data_for_netty();
                        data.setNetty_type("request");
                        data.setSubType("update_chat_log");
                        data.setSender_user_no(getUser_no());
                        // 현재 채팅방 번호를 Data_for_netty의 Extra 변수에 넣기
                        data.setExtra(String.valueOf(chat_room_no));
                        data.setFirst_read_msg_no(String.valueOf(first_update_msg_no));
                        data.setLast_read_msg_no(String.valueOf(last_read_msg_no));
                        Log.d(TAG, "myapp_first_update_msg_no: " + first_update_msg_no);
                        Log.d(TAG, "myapp_last_read_msg_no: " + last_read_msg_no);
                        // 통신 전송 메소드 호출
                        send_to_server(data);


                        new Handler().postDelayed(new Runnable() {
                            // 1 초 후에 실행
                            @Override public void run() {
                                BusProvider.getBus().register(this);        // otto 등록
                                // otto 를 통해, 프래그먼트로 이벤트 전달하기
                                Event.Myapp__Chat_A myapp__chat_a = new Event.Myapp__Chat_A("re_receive_chat_log");
                                BusProvider.getBus().post(myapp__chat_a);
                                BusProvider.getBus().unregister(this);      // otto 등록 해제
                            }
                        }, 500);
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                logAndToast("onFailure_result" + t.getMessage());
            }
        });
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 맵에서 value 를 가지고 sorting
     ---------------------------------------------------------------------------*/
    public List sort_map_by_value(final HashMap map){
        List<String> list = new ArrayList();
        list.addAll(map.keySet());

        Collections.sort(list, new Comparator(){

            public int compare(Object o1,Object o2){
                Object v1 = map.get(o1);
                Object v2 = map.get(o2);

                return ((Comparable) v1).compareTo(v2);
            }

        });
//        Collections.reverse(list); // 주석시 오름차순
        return list;
    }


    /**---------------------------------------------------------------------------
     생명주기 ==> onTerminate()
     ---------------------------------------------------------------------------*/
    @Override
    public void onTerminate() {
        // todo: redis - 세션
        Redis_log_session_info("out");
        // TODO: 서비스 돌리기 이전, 테스트 코드 - 나중에 삭제
//        channel.close();
        super.onTerminate();
    }


    /**
     * 비트맵 -> 파일로 저장하기
     * Image SDCard Save (input Bitmap -> saved Image File)
     * @param bitmap : input bitmap file
     * @param folder : input folder name
     * @param name   : output file name
     */
    public String saveBitmaptoImage(Bitmap bitmap, String folder, String name){
        String ex_storage = Environment.getExternalStorageDirectory().getAbsolutePath();
        // Get Absolute Path in External Sdcard

        String folder_name = "/"+folder+"/";
        String string_path = ex_storage+folder_name;
        String file_name = name+".png";
        Logger.d("string_path + file_name: " + string_path + file_name);

        File file_path;
        try{
            file_path = new File(string_path);
            if(!file_path.isDirectory()){
                file_path.mkdirs();
            }
            FileOutputStream out = new FileOutputStream(string_path+file_name);

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();
            return string_path + file_name;

        }catch(FileNotFoundException exception){
            Log.e("FileNotFoundException", exception.getMessage());
        }catch(IOException exception){
            Log.e("IOException", exception.getMessage());
        }
        return null;
    }


    /**---------------------------------------------------------------------------
     메소드 ==> netty를 통해, 회의하고 있는 상대방에게 나의 얼굴인식+3d 모드 on/off 상태를 알림
     ---------------------------------------------------------------------------*/
    public void send_my_3d_mode_status_to_subject(boolean is_3d_object_mode_on,
                                                  float eulerY,
                                                  float eulerZ) {
        Data_for_netty data = new Data_for_netty();
        data.setNetty_type("webrtc");
        data.setSubType("sending_my_3d_mode_status");
        data.setSender_user_no(getUser_no());
        data.setTarget_user_no(getMeeting_subject_user_no());
        // Data_for_netty 객체의 'extra' 변수에
        // index 0. '얼굴인식 + 3D모드' 상태
        // index 1. 구글 비전 API의 getEulerY 값
        // index 2. 구글 비전 API의 getEulerZ 값
        // ~ 를 담아 보낸다
        String send_this_string =
                String.valueOf(is_3d_object_mode_on) + Static.SPLIT +
                        String.valueOf(eulerY) + Static.SPLIT +
                        String.valueOf(eulerZ);

        Log.d(TAG, "eulerY: " + eulerY);
        Log.d(TAG, "eulerZ: " + eulerZ);

        data.setExtra(send_this_string);
        send_to_server(data);
    }

    /**---------------------------------------------------------------------------
     메소드 ==> 시크바 투명도 alpha 값 반환 메소드
     매개변수 1. 컬러
     매개변수 2. 현재 alpha int 값 (10~255 사이)
     ---------------------------------------------------------------------------*/
    public String set_drawing_tool_seekbar_alpha(int changed_color, int changed_alpha) {
        // 알파값에 따른 hex 값 구하기
        String hex = Integer.toHexString(changed_alpha).toUpperCase();
        if(hex.length() == 1) {
            hex = "0" + hex;
        }
        Log.d(TAG, "hex: " + hex);

        // 알파값을 제외한 현재 컬러 값 구하기
        String except_hex = Integer.toHexString(changed_color);
        except_hex = except_hex.substring(2, 8);
        String IndicatorColor = hex + except_hex;
        Log.d(TAG, "except_hex: " + except_hex);
        Log.d(TAG, "IndicatorColor: " + IndicatorColor);

        return IndicatorColor;
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 해당 회의결과에 지정된 프로젝트가 있는지 확인하고, 있다면 해당 프로젝트 객체를 리턴하는 메소드
     ---------------------------------------------------------------------------*/
    @SuppressLint("StaticFieldLeak")
    public Project get_project_info(final int target_project_no) {

        final Project project = new Project();
        final RetrofitService rs = ServiceGenerator.createService(RetrofitService.class);

        // 동기 호출
        try {
            final Project[] final_project = {project};

            return new AsyncTask<Void, Void, Project>() {
                @Override
                protected Project doInBackground(Void... voids) {
                    try {
                        Call<ResponseBody> call_result = rs.get_project_info(
                                Static.GET_PROJECT_INFO,
                                getUser_no(),
                                target_project_no);
                        Response<ResponseBody> list = call_result.execute();
                        String result = list.body().string();
                        Log.d(TAG, "get_project_info_result: " + result);

                        // 에러
                        if(result.equals("fail")) {}

                        // 정상 결과 리턴
                        else  {
                            Gson gson = new Gson();
                            final_project[0] = gson.fromJson(result, Project.class);
                            Log.d(TAG, "final_project[0].getProject_no(): " + final_project[0].getProject_no());
                            Log.d(TAG, "final_project[0].getProject_color(): " + final_project[0].getProject_color());
                            Log.d(TAG, "final_project[0].getProject_name(): " + final_project[0].getProject_name());
                            Log.d(TAG, "final_project[0].getMeeting_count(): " + final_project[0].getMeeting_count());
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return final_project[0];
                }
            }.execute().get();
        }catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 해당 회의결과에 지정된 프로젝트가 있는지 확인하고, 있다면 해당 프로젝트 객체를 리턴하는 메소드
     ---------------------------------------------------------------------------*/
    @SuppressLint("StaticFieldLeak")
    public Project assigned_project(final String meeting_no) {

        final Project project = new Project();
        final RetrofitService rs = ServiceGenerator.createService(RetrofitService.class);

        // 동기 호출
        try {
            final Project[] final_project = {project};

            return new AsyncTask<Void, Void, Project>() {
                @Override
                protected Project doInBackground(Void... voids) {
                    try {
                        Call<ResponseBody> call_result = rs.assigned_project(
                                Static.ASSIGNED_PROJECT,
                                getUser_no(),
                                meeting_no);
                        Response<ResponseBody> list = call_result.execute();
                        String result = list.body().string();
                        Log.d(TAG, "assigned_project_result: " + result);

                        // 에러
                        if(result.equals("fail")) {
//                            logAndToast("예외발생: " + result);
                            final_project[0].setProject_no(-1);

                        }
                        // 지정된 프로젝트가 없는 경우
                        else if(result.equals("unassigned")) {
                            final_project[0].setProject_no(0);
                        }
                        // 지정된 프로젝트가 있는 경우
                        else  {
                            Gson gson = new Gson();
                            final_project[0] = gson.fromJson(result, Project.class);
                            Log.d(TAG, "final_project[0].getProject_no(): " + final_project[0].getProject_no());
                            Log.d(TAG, "final_project[0].getProject_color(): " + final_project[0].getProject_color());
                            Log.d(TAG, "final_project[0].getProject_name(): " + final_project[0].getProject_name());
                            Log.d(TAG, "final_project[0].getMeeting_count(): " + final_project[0].getMeeting_count());
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return final_project[0];
                }
            }.execute().get();
        }catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 오늘의 해당하는 '년, 월, 일'을 int 배열로 리턴하는 메소드
                리턴 형식: [2016, 0, 31] / month 는 '-1' 된 값
     ---------------------------------------------------------------------------*/
    public int[] year_month_day() {
        int[] return_int_arr = new int[3];

        Calendar cal = Calendar.getInstance();

        return_int_arr[0] = cal.get(Calendar.YEAR);
        return_int_arr[1] = cal.get(Calendar.MONTH);
        return_int_arr[2] = cal.get(Calendar.DAY_OF_MONTH);

        return return_int_arr;
    }


    /**---------------------------------------------------------------------------
     메소드 ==> dp --> pixel(float)
     ---------------------------------------------------------------------------*/
    public float convertDpToPixel(float dp, Context context){

        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);

        return px;
    }


    /**---------------------------------------------------------------------------
     메소드 ==> pixel(float) --> dp
     ---------------------------------------------------------------------------*/
    public float convertPixelToDp(float px, Context context){

        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);

        return dp;
    }

    /**---------------------------------------------------------------------------
     메소드 ==> Redis 로그 전송 jsonString 빌드, case 1.
                - 액티비티 이동정보 로그 (from|to 클래스 이름)
     ---------------------------------------------------------------------------*/
    public void Redis_log_view_crossOver_from_to(String from_class, String to_class) {

        Redis_log_view_crossOver_from_to redis_log_view_crossOver_from_to
                = new Redis_log_view_crossOver_from_to();

        // 로그 공통 변수 set
        redis_log_view_crossOver_from_to.setDate_now(Get_currentTime.get_full());
        redis_log_view_crossOver_from_to.setUser_no(getUser_no());
        redis_log_view_crossOver_from_to.setNickname(getUser_nickname());
        redis_log_view_crossOver_from_to.setSession_id(getSession_id());

        // 이 로그 타입에만 있는 변수 set
        redis_log_view_crossOver_from_to.setFrom_class(from_class);
        redis_log_view_crossOver_from_to.setTo_class(to_class);

        // jsonString 으로 변환
        Gson gson = new Gson();
        String jsonString = gson.toJson(redis_log_view_crossOver_from_to);
        Log.d(TAG, "view_crossOver_from_to: " + jsonString);

        // jsonString, 서버 전송하는 내부 메소드 호출
        Redis_save_log("view_crossOver_from_to",jsonString);
    }


    /**---------------------------------------------------------------------------
     메소드 ==> Redis 로그 전송 jsonString 빌드, case 2.
                - session 정보 로그 ('enter' or 'out')
     ---------------------------------------------------------------------------*/
    public void Redis_log_session_info(String session_type) {

        Redis_log_session_info redis_log_session_info = new Redis_log_session_info();

        // 로그 공통 변수 set
        redis_log_session_info.setDate_now(Get_currentTime.get_full());
        redis_log_session_info.setUser_no(getUser_no());
        redis_log_session_info.setNickname(getUser_nickname());
        redis_log_session_info.setSession_id(getSession_id());

        // 이 로그 타입에만 있는 변수 set
        redis_log_session_info.setType(session_type);
        redis_log_session_info.setMethod(getLogin_method());

        // jsonString 으로 변환
        Gson gson = new Gson();
        String jsonString = gson.toJson(redis_log_session_info);
        Log.d(TAG, "session_info: " + jsonString);

        // jsonString, 서버 전송하는 내부 메소드 호출
        Redis_save_log("session_info",jsonString);
    }


    /**---------------------------------------------------------------------------
     메소드 ==> Redis 로그 전송 jsonString 빌드, case 3.
                - 클릭이벤트 정보 로그 (클릭한 뷰의 id, int|String 값)
     ---------------------------------------------------------------------------*/
    public void Redis_log_click_event(String curr_class, View view) {

        Redis_log_click_event redis_log_click_event = null;

//        // onBackPressed, 뒤로가기 버튼 눌렀때
//        if(view == null) {
//            redis_log_click_event = new Redis_log_click_event();
//
//            // 로그 공통 변수 set
//            redis_log_click_event.setDate_now(Get_currentTime.get_full());
//            redis_log_click_event.setUser_no(getUser_no());
//            redis_log_click_event.setNickname(getUser_nickname());
//            redis_log_click_event.setSession_id(getSession_id());
//            // 이 로그 타입에만 있는 변수 set
//            redis_log_click_event.setCurr_class(curr_class);
//            redis_log_click_event.setClick_id_int(0);
//            redis_log_click_event.setClick_id_str("onBackPressed");
//        }
        if(view != null) {
            String[] temp = view.getResources().getResourceName(view.getId()).split("[/]");

            redis_log_click_event = new Redis_log_click_event();

            // 로그 공통 변수 set
            redis_log_click_event.setDate_now(Get_currentTime.get_full());
            redis_log_click_event.setUser_no(getUser_no());
            redis_log_click_event.setNickname(getUser_nickname());
            redis_log_click_event.setSession_id(getSession_id());
            // 이 로그 타입에만 있는 변수 set
            redis_log_click_event.setCurr_class(curr_class);
            redis_log_click_event.setClick_id_int(view.getId());
            redis_log_click_event.setClick_id_str(temp[temp.length-1]);
        }

        // jsonString 으로 변환
        Gson gson = new Gson();
        String jsonString = gson.toJson(redis_log_click_event);
        Log.d(TAG, "click_event: " + jsonString);

        // jsonString, 서버 전송하는 내부 메소드 호출
        Redis_save_log("click_event",jsonString);
    }


    /**---------------------------------------------------------------------------
     메소드 ==> Redis 로그 전송 jsonString, 서버 전송 -- 스레드로 구동
     ---------------------------------------------------------------------------*/
    public void Redis_save_log(final String log_type, final String jsonString) {

        new Thread() {
            @Override
            public void run() {
                super.run();

                String log_key = getUser_no() + ":" + log_type;
//                String log_key = log_type;
                String log_value = jsonString;

                RetrofitService rs = ServiceGenerator.createService(RetrofitService.class);
                Call<ResponseBody> call = rs.redis_save_log(
                        Static.REDIS_SAVE_LOG,
                        log_key,
                        log_value);

                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            String retrofit_result = response.body().string();
                            Log.d(TAG, "Redis_retrofit_result: "+retrofit_result);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        logAndToast("onFailure_result" + t.getMessage());
                    }
                });
            }
        }.start();


    }
}



















