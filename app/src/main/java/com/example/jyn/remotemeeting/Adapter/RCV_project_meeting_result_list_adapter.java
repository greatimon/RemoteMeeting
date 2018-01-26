package com.example.jyn.remotemeeting.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.jyn.remotemeeting.Activity.Project_meeting_result_list_A;
import com.example.jyn.remotemeeting.DataClass.Chat_room;
import com.example.jyn.remotemeeting.DataClass.Drawing_images_saveFile;
import com.example.jyn.remotemeeting.DataClass.Meeting_room;
import com.example.jyn.remotemeeting.DataClass.Users;
import com.example.jyn.remotemeeting.Dialog.Meeting_result_D;
import com.example.jyn.remotemeeting.Etc.Static;
import com.example.jyn.remotemeeting.R;
import com.example.jyn.remotemeeting.Util.Myapp;
import com.example.jyn.remotemeeting.Util.RetrofitService;
import com.example.jyn.remotemeeting.Util.ServiceGenerator;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.CropCircleTransformation;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by JYN on 2018-01-23.
 */

public class RCV_project_meeting_result_list_adapter extends RecyclerView.Adapter<RCV_project_meeting_result_list_adapter.ViewHolder> {

    private Context context;
    private int itemLayout;
    private ArrayList<Meeting_room> meeting_room_arr;
    private String project_color;
    private int alpha_1_project_color_value_hex;
    private int alpha_2_project_color_value_hex;
    public static String TAG = "all_" + RCV_project_meeting_result_list_adapter.class.getSimpleName();
    Myapp myapp;

    // 리사이클러뷰 최상단에 표시될 날짜변경선의 String 값
    String date_str_pos_0;

    /** RecyclerAdapter 생성자 */
    public RCV_project_meeting_result_list_adapter(
            Context context, int itemLayout, ArrayList<Meeting_room> meeting_room_arr, String project_color) {
        Log.d(TAG, "RCV_project_meeting_result_list_adapter: 생성");
        this.context = context;
        this.itemLayout = itemLayout;
        this.meeting_room_arr = meeting_room_arr;
        this.project_color = project_color;

        // 어플리케이션 객체 생성
        myapp = Myapp.getInstance();

        // 프로젝트 컬러 가져오기
        int project_color_value = myapp.project_color(project_color);

        // 해당 프로젝터 컬러의 13% 투명도 int 값 설정하기
        String alpha_1_project_color_value_str = myapp.set_drawing_tool_seekbar_alpha(project_color_value, 33);
        Log.d(TAG, "alpha_1_project_color_value_str: " + alpha_1_project_color_value_str);

        // 해당 프로젝터 컬러의 18% 투명도 int 값 설정하기
        String alpha_2_project_color_value_str = myapp.set_drawing_tool_seekbar_alpha(project_color_value, 46);
        Log.d(TAG, "alpha_2_project_color_value_str: " + alpha_2_project_color_value_str);

        // HexString convert to int (hex 스트링 값 16진수 인트로 변환)
        alpha_1_project_color_value_hex = Integer.parseInt(alpha_1_project_color_value_str, 16);

        // HexString convert to int (hex 스트링 값 16진수 인트로 변환)
        alpha_2_project_color_value_hex = Integer.parseInt(alpha_2_project_color_value_str, 16);


        Log.d(TAG, "meeting_room_arr.size(): " + meeting_room_arr.size());
    }

    /** 뷰홀더 */
    class ViewHolder extends RecyclerView.ViewHolder {

        /** 버터나이프*/
        @BindView(R.id.container_LIN)                       LinearLayout container_LIN;
        @BindView(R.id.date_line_LIN)                       LinearLayout date_line_LIN;
        @BindView(R.id.profile_imgV)                        ImageView profile_imgV;
        @BindView(R.id.memo_imgV)                           ImageView memo_imgV;
        @BindView(R.id.image_to_document_imgV)              ImageView image_to_document_imgV;
        @BindView(R.id.upload_images_imgV)                  ImageView upload_images_imgV;
        @BindView(R.id.drawing_images_imgV)                 ImageView drawing_images_imgV;
        @BindView(R.id.meeting_title_txt)                   TextView meeting_title_txt;
        @BindView(R.id.meeting_subject_user_nickName_txt)   TextView meeting_subject_user_nickName_txt;
        @BindView(R.id.total_meeting_time_txt)              TextView total_meeting_time_txt;
        @BindView(R.id.date_line_txt)                       TextView date_line_txt;
        @BindView(R.id.date_line_divider_V_1)               View date_line_divider_V_1;
        @BindView(R.id.date_line_divider_V_2)               View date_line_divider_V_2;


        ViewHolder(View itemView) {
            super(itemView);
//            Log.d(TAG, "ViewHolder");
            ButterKnife.bind(this,itemView);

            /** 아이템 클릭 이벤트 설정 */
            container_LIN.setClickable(true);
            container_LIN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // TODO: redis - 클릭이벤트
                    myapp.Redis_log_click_event(getClass().getSimpleName(), v);

                    int pos = getAdapterPosition();
                    Log.d(TAG, "클릭 아이템 position: " + pos);

                    Log.d(TAG, "meeting_room_arr.getMeeting_no(): " + meeting_room_arr.get(pos).getMeeting_no());
                    Log.d(TAG, "meeting_room_arr.getReal_meeting_title(): " + meeting_room_arr.get(pos).getReal_meeting_title());
                    Log.d(TAG, "meeting_room_arr.getMeeting_creator_user_no(): " + meeting_room_arr.get(pos).getMeeting_creator_user_no());
                    Log.d(TAG, "meeting_room_arr.getMeeting_subject_user_no(): " + meeting_room_arr.get(pos).getMeeting_subject_user_no());
                    Log.d(TAG, "meeting_room_arr.getMeeting_end_time(): " + meeting_room_arr.get(pos).getMeeting_end_time());
                    Log.d(TAG, "meeting_room_arr.getTotal_meeting_time(): " + meeting_room_arr.get(pos).getTotal_meeting_time());

                    // TODO: redis - 화면 이동
                    myapp.Redis_log_view_crossOver_from_to(
                            getClass().getSimpleName(), Meeting_result_D.class.getSimpleName());

                    // 해당 영상회의 결과 다이얼로그 액티비티 열기
                    Intent intent = new Intent(context, Meeting_result_D.class);
                    intent.putExtra("meeting_no", meeting_room_arr.get(pos).getMeeting_no());
                    intent.putExtra("from", Static.PROJECT_FOLDER);
                    intent.putExtra("subject_user_no", meeting_room_arr.get(pos).getMeeting_subject_user_no());
                    intent.putExtra(Static.REQUEST_CLASS, getClass().getSimpleName());
                    ((Project_meeting_result_list_A)context)
                            .startActivityForResult(intent, Project_meeting_result_list_A.REQUEST_OPEN_THIS_MEETING_RESULT);
                }
            });
        }
    }

    /** onCreateViewHolder => 뷰홀더 생성 - 인플레이팅 */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        Log.d(TAG, "onCreateViewHolder");
        View view = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    /** onBindViewHolder => 리스트뷰의 getView 역할 */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
//        Log.d(TAG, "onBindViewHolder");

        if(holder.getAdapterPosition() != RecyclerView.NO_POSITION) {
            // 아이템의 index 값
            int pos = holder.getAdapterPosition();

            // 임시 meeting_room 객체 생성
            Meeting_room temp_meeting_room = meeting_room_arr.get(pos);

//            //// 메모, 스캔 이미지 파일, 업로드 파일, 드로잉 파일이 존재여부 확인
//            // 최초로 리스트가 보여질 때, 각 데이터가 있는지 없는지 확인해서 있으면 내용 가져오기
//            if(temp_meeting_room.getMemo_state() == 0) {
//                String check_memo = isMemo_exist(context, temp_meeting_room.getMeeting_no());
//                // 메모가 있다면
//                if(check_memo != null) {
//                    meeting_room_arr.get(pos).setMemo(check_memo);
//                    meeting_room_arr.get(pos).setMemo_state(1);
//                }
//                // 메모가 있다면
//                else if(check_memo == null) {
//                    meeting_room_arr.get(pos).setMemo_state(-1);
//                }
//            }
//            if(temp_meeting_room.getScanned_img_state() == 0) {
//                String get_scanned_image_absolutePath = isScanned_image(context, temp_meeting_room.getMeeting_no());
//                // 스캔한 이미지 파일이 있다면
//                if(get_scanned_image_absolutePath != null) {
//                    meeting_room_arr.get(pos).setScanned_img_path(get_scanned_image_absolutePath);
//                    meeting_room_arr.get(pos).setScanned_img_state(1);
//                }
//                // 스캔한 이미지 파일이 있다면
//                else if(get_scanned_image_absolutePath == null) {
//                    meeting_room_arr.get(pos).setScanned_img_state(-1);
//                }
//            }
//            if(temp_meeting_room.getUplopaded_img_state() == 0) {
//                String jsonString_uploaded_fileName = isUploaded_file(temp_meeting_room.getMeeting_no());
//                // 업로드한 이미지 파일이 있다면
//                if(jsonString_uploaded_fileName != null) {
//                    meeting_room_arr.get(pos).setUplopaded_img_fileNames(jsonString_uploaded_fileName);
//                    meeting_room_arr.get(pos).setUplopaded_img_state(1);
//                }
//                // 업로드한 이미지 파일이 없다면
//                else if(jsonString_uploaded_fileName == null) {
//                    meeting_room_arr.get(pos).setUplopaded_img_state(-1);
//                }
//            }
//            if(temp_meeting_room.getDrawing_img_state() == 0) {
//                String isDrawing_image_fileName = isDrawing_image(context, temp_meeting_room.getMeeting_no());
//                // 드로잉한 이미지 파일이 있다면
//                if(isDrawing_image_fileName != null) {
//                    meeting_room_arr.get(pos).setDrawing_img_fileNames(isDrawing_image_fileName);
//                    meeting_room_arr.get(pos).setDrawing_img_state(1);
//                }
//                // 드로잉한 이미지 파일이 없다면
//                else if(isDrawing_image_fileName == null) {
//                    meeting_room_arr.get(pos).setDrawing_img_state(-1);
//                }
//            }

            /** 날짜변경선 표시 하기 */
            // 어레이리스트 첫번째 아이템일 때,
            if(pos == 0) {
                // 뷰 visibility 조절
                holder.date_line_LIN.setVisibility(View.VISIBLE);

                // 표시할 날짜 String 값
                date_str_pos_0 = extract_date_from_meeting_room_obj(temp_meeting_room.getMeeting_end_time());
                holder.date_line_txt.setText(date_str_pos_0);

                //// 표시할 뷰의 색 넣기
                // 뷰 border 컬러
                holder.date_line_divider_V_1.setBackgroundColor(alpha_2_project_color_value_hex);
                holder.date_line_divider_V_2.setBackgroundColor(alpha_2_project_color_value_hex);
                // 뷰 컬러
                holder.date_line_txt.setBackgroundColor(alpha_1_project_color_value_hex);
            }
            // 어레이 첫번째 아이템이 아닐 때,
            else if(pos > 0) {
                // 이전 어레이아이템의 'meeting_end_time' 과 지금 어레이의 아이템의 'meeting_end_time'을 비교,
                // 날짜가 변경되었을 때만 날짜 변경선을 표시하도록 함
                String check_date_has_been_changed =
                        check_date_has_been_changed(
                                temp_meeting_room.getMeeting_end_time(), meeting_room_arr.get(pos-1).getMeeting_end_time());
                // 값이 'null' 이 아니라면, 날짜 변경선에 표시할 리턴값이 들어 있는 경우이므로, 데이터를 셋팅한다
                // 단, 최상단에 표시했던 날짜 변경선과 동일하다면 제외
                if(check_date_has_been_changed != null && !date_str_pos_0.equals(check_date_has_been_changed)) {
                    // 뷰 visibility 조절
                    holder.date_line_LIN.setVisibility(View.VISIBLE);

                    // 표시할 날짜 String 값
                    holder.date_line_txt.setText(check_date_has_been_changed);

                    //// 표시할 뷰의 색 넣기
                    // 뷰 border 컬러
                    holder.date_line_divider_V_1.setBackgroundColor(alpha_2_project_color_value_hex);
                    holder.date_line_divider_V_2.setBackgroundColor(alpha_2_project_color_value_hex);
                    // 뷰 컬러
                    holder.date_line_txt.setBackgroundColor(alpha_1_project_color_value_hex);
                }
            }


            // 상대방 user_no 가져오기
            String this_is_subject_user_no = "";
            if(!temp_meeting_room.getMeeting_creator_user_no().equals(myapp.getUser_no())) {
                this_is_subject_user_no = temp_meeting_room.getMeeting_creator_user_no();
            }
            else if(!temp_meeting_room.getMeeting_subject_user_no().equals(myapp.getUser_no())) {
                this_is_subject_user_no = temp_meeting_room.getMeeting_subject_user_no();
            }
//            Log.d(TAG, "this_is_subject_user_no: " + this_is_subject_user_no);
//            Log.d(TAG, "getMeeting_no(): " + meeting_room_arr.get(pos).getMeeting_no());
//            Log.d(TAG, "getMeeting_end_time(): " + meeting_room_arr.get(pos).getMeeting_end_time());
//            Log.d(TAG, "getReal_meeting_title(): " + meeting_room_arr.get(pos).getReal_meeting_title());

            // 상대방 user_info 서버로 부터 받아오기
            Users target_user = myapp.get_user_info(this_is_subject_user_no);

            // 상대방 프로필 이미지 셋팅
            Glide
                .with(context)
                .load(Static.SERVER_URL_PROFILE_FILE_FOLDER + target_user.getUser_img_filename())
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .bitmapTransform(new CropCircleTransformation(context))
                .into(holder.profile_imgV);

            // 영상회의 제목 셋팅
            holder.meeting_title_txt.setText(temp_meeting_room.getReal_meeting_title());

            // 상대방 닉네임 셋팅
            holder.meeting_subject_user_nickName_txt.setText(target_user.getUser_nickname());

            // 총 회의 시간 셋팅
            holder.total_meeting_time_txt.setText(temp_meeting_room.getTotal_meeting_time());

            //// 해당 회의결과에 각 데이터가 있는지 확인해서, 메모 아이콘 alpha 값 조절하기
            // 메모내용
            if(meeting_room_arr.get(pos).getMemo_state() == 1 &&
                    !meeting_room_arr.get(pos).getMemo().equals("")) {
                holder.memo_imgV.setAlpha(1.0f);
            }
            else if(meeting_room_arr.get(pos).getMemo_state() == -1) {
                holder.memo_imgV.setAlpha(0.15f);
            }

            // 스캔 이미지 파일
            if(meeting_room_arr.get(pos).getScanned_img_state() == 1 &&
                    !meeting_room_arr.get(pos).getScanned_img_path().equals("")) {
                holder.image_to_document_imgV.setAlpha(1.0f);
            }
            else if(meeting_room_arr.get(pos).getScanned_img_state() == -1) {
                holder.image_to_document_imgV.setAlpha(0.15f);
            }

            // 업로드 이미지 파일
            if(meeting_room_arr.get(pos).getUplopaded_img_state() == 1 &&
                    !meeting_room_arr.get(pos).getUplopaded_img_fileNames().equals("")) {
                holder.upload_images_imgV.setAlpha(1.0f);
            }
            else if(meeting_room_arr.get(pos).getUplopaded_img_state() == -1) {
                holder.upload_images_imgV.setAlpha(0.15f);
            }

            // 드로잉 이미지 파일
            if(meeting_room_arr.get(pos).getDrawing_img_state() == 1 &&
                    !meeting_room_arr.get(pos).getDrawing_img_fileNames().equals("")) {
                holder.drawing_images_imgV.setAlpha(1.0f);
            }
            else if(meeting_room_arr.get(pos).getDrawing_img_state() == -1) {
                holder.drawing_images_imgV.setAlpha(0.15f);
            }
        }
    }

    /** getItemCount => arr 사이즈 리턴 */
    @Override
    public int getItemCount() {
        return meeting_room_arr.size();
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 넘겨받은 두개의 'meeting_end_time'를 가지고 날짜가 변경되었는지 확인하여,
                변경되었다면, 현재 'position' 기준의 날짜를 리턴한다
     ---------------------------------------------------------------------------*/
    private String check_date_has_been_changed(String meeting_end_time_1, String meeting_end_time_2) {
        // 'position-1' 의 날짜 확인
        String[] temp_1 = meeting_end_time_1.split("[ ]");
//        Log.d(TAG, "'position-1' 의 날짜: " + temp_1[0]);

        // 현재 'position'의 날짜 확인
        String[] temp_2 = meeting_end_time_2.split("[ ]");
//        Log.d(TAG, "현재 'position'의 날짜: " + temp_2[0]);

        if(!temp_1[0].equals(temp_2[0])) {
            // 날짜 변경선에 표시할 String 값을 리턴하는, 내부 메소드 호출
            return extract_date_from_meeting_room_obj(meeting_end_time_1);
        }
        else {
            return null;
        }
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 'meeting_end_time'의 '년-월-일' 값으로, 날짜변경선에 표시할 String 값 생성하여 리턴
     ---------------------------------------------------------------------------*/
    private String extract_date_from_meeting_room_obj(String meeting_end_time) {
        // 스플릿 첫번째. '공백'으로 스플릿
        String[] temp_1 = meeting_end_time.split("[ ]");
//        Log.d(TAG, "temp_1[0]: " + temp_1[0]);

        // 스플릿 두번째. '-'으로 스플릿
        String[] temp_2 = temp_1[0].split("[-]");

        // 'meeting_end_time'에 해당하는 '요일' 구하기
        Calendar meeting_cal= Calendar.getInstance();
        meeting_cal.set(Calendar.YEAR, Integer.parseInt(temp_2[0]));
        meeting_cal.set(Calendar.MONTH, Integer.parseInt(temp_2[1])-1);
        meeting_cal.set(Calendar.DATE, Integer.parseInt(temp_2[2]));

        int meeting_today = meeting_cal.get(Calendar.DAY_OF_WEEK)-1;
        String today = myapp.weekDay[meeting_today].substring(0,1);
//        Log.d(TAG, "meeting_today: " + today);

        // 한자리수 '월'인 경우, 앞에 붙어있는 '0' 제거하기
        if(Integer.parseInt(temp_2[1]) < 10) {
            temp_2[1] = temp_2[1].substring(1);
        }
        // 한자리수 '일'인 경우, 앞에 붙어있는 '0' 제거 하기
        if(Integer.parseInt(temp_2[2]) < 10) {
            temp_2[2] = temp_2[2].substring(1);
        }

        // 오늘 날짜 확인
        // 캘린더 객체
        Calendar cal= Calendar.getInstance();
//        int num = cal.get(Calendar.DAY_OF_WEEK)-1;
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH)+1;
        String month_str = String.valueOf(month);
        if(month < 10) {
            month_str = "0"+String.valueOf(month);
        }
        int day = cal.get(Calendar.DAY_OF_MONTH);

        // meeting_end_time의 날짜가 오늘 날짜와 일치하는지 확인
        String temp_today = String.valueOf(year) + "-" + String.valueOf(month_str) + "-" + String.valueOf(day);
//        Log.d(TAG, "temp_today: " + temp_today);
        if(temp_1[0].equals(temp_today)) {
            return "오늘";
        }

        // 오늘 날짜가 아니라면,
        else {
            // 리턴할 String 만들기 - '2018년 1월 21일 (일)' 형태
            String date = temp_2[0] + "년 " + temp_2[1] + "월 " + temp_2[2] + "일 (" + today + ")";
//            Log.d(TAG, "date: " + today);

            return date;
        }
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 서버로부터 받아온 meeting_room_arr 리스트로 arrayList 교체하기
     ---------------------------------------------------------------------------*/
    public void refresh_arr(ArrayList<Meeting_room> meeting_room_arr) {
        this.meeting_room_arr.clear();
        this.meeting_room_arr = meeting_room_arr;
        notifyDataSetChanged();
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 영상회의 결과를 담고 있는 arr를 초기화 하고, 아무것도 없는 화면을 출력하기 위해
            notifyDataSetChanged()를 호출한다
     ---------------------------------------------------------------------------*/
    public void clear_item_arr() {
        this.meeting_room_arr.clear();
        notifyDataSetChanged();
    }
}
