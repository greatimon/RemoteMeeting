package com.example.jyn.remotemeeting.DataClass;

/**
 * Created by JYN on 2018-01-20.
 */

public class Meeting_room {

    private String meeting_no;
    private String real_meeting_title;
    private String transform_meeting_title;
    private String meeting_creator_user_no;
    private String meeting_subject_user_no;
    private String meeting_authority_user_no;
    private String meeting_start_time;
    private String meeting_end_time;
    private String total_meeting_time;
    private String project_no;
    private String meeting_status;
//    private String extra_column_2;
//    private String extra_column_3;
    private String memo = "";
    private String scanned_img_path = "";
    private String uplopaded_img_fileNames = "";
    private String drawing_img_fileNames = "";

    //// 회의 결과 어레이리스트를 서버로부터 받아와서, 회의결과와 관련된 데이터들이
    //// 있는지 없는지 확인하고, 있다면 해당 데이터를 객체에 저장한다
    // 최초(확인전): 0
    // 확인결과 데이터가 있음: 1
    // 확인결과 데이터가 없음: -1
    private int memo_state = 0;
    private int scanned_img_state = 0;
    private int uplopaded_img_state = 0;
    private int drawing_img_state = 0;

    //// 회의 결과 어레이리스트에서, 바로 이전 인덱스의 회의결과와 비교했을 때,
    //// 회의 날짜가 변경됐는지 확인하고, 변경되었다면 변경된 날짜를 객체에 저장한다
    private boolean show_date = false;
    private String changed_date = "";

    //// 날짜변경선 백그라운드에 적용할 컬러 int 값
    private int date_back_color_int_1;
    private int date_back_color_int_2;


    Meeting_room() {}

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

    public String getMeeting_start_time() {
        return meeting_start_time;
    }

    public void setMeeting_start_time(String meeting_start_time) {
        this.meeting_start_time = meeting_start_time;
    }

    public String getMeeting_end_time() {
        return meeting_end_time;
    }

    public void setMeeting_end_time(String meeting_end_time) {
        this.meeting_end_time = meeting_end_time;
    }

    public String getTotal_meeting_time() {
        return total_meeting_time;
    }

    public void setTotal_meeting_time(String total_meeting_time) {
        this.total_meeting_time = total_meeting_time;
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

    public boolean isShow_date() {
        return show_date;
    }

    public void setShow_date(boolean show_date) {
        this.show_date = show_date;
    }

    public String getChanged_date() {
        return changed_date;
    }

    public void setChanged_date(String changed_date) {
        this.changed_date = changed_date;
    }

    //    public String getExtra_column_2() {
//        return extra_column_2;
//    }
//
//    public void setExtra_column_2(String extra_column_2) {
//        this.extra_column_2 = extra_column_2;
//    }
//
//    public String getExtra_column_3() {
//        return extra_column_3;
//    }
//
//    public void setExtra_column_3(String extra_column_3) {
//        this.extra_column_3 = extra_column_3;
//    }


    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getScanned_img_path() {
        return scanned_img_path;
    }

    public void setScanned_img_path(String scanned_img_path) {
        this.scanned_img_path = scanned_img_path;
    }

    public String getUplopaded_img_fileNames() {
        return uplopaded_img_fileNames;
    }

    public void setUplopaded_img_fileNames(String uplopaded_img_fileNames) {
        this.uplopaded_img_fileNames = uplopaded_img_fileNames;
    }

    public String getDrawing_img_fileNames() {
        return drawing_img_fileNames;
    }

    public void setDrawing_img_fileNames(String drawing_img_fileNames) {
        this.drawing_img_fileNames = drawing_img_fileNames;
    }

    public int getMemo_state() {
        return memo_state;
    }

    public void setMemo_state(int memo_state) {
        this.memo_state = memo_state;
    }

    public int getScanned_img_state() {
        return scanned_img_state;
    }

    public void setScanned_img_state(int scanned_img_state) {
        this.scanned_img_state = scanned_img_state;
    }

    public int getUplopaded_img_state() {
        return uplopaded_img_state;
    }

    public void setUplopaded_img_state(int uplopaded_img_state) {
        this.uplopaded_img_state = uplopaded_img_state;
    }

    public int getDrawing_img_state() {
        return drawing_img_state;
    }

    public void setDrawing_img_state(int drawing_img_state) {
        this.drawing_img_state = drawing_img_state;
    }

    public int getDate_back_color_int_1() {
        return date_back_color_int_1;
    }

    public void setDate_back_color_int_1(int date_back_color_int_1) {
        this.date_back_color_int_1 = date_back_color_int_1;
    }

    public int getDate_back_color_int_2() {
        return date_back_color_int_2;
    }

    public void setDate_back_color_int_2(int date_back_color_int_2) {
        this.date_back_color_int_2 = date_back_color_int_2;
    }

    public boolean is_first_time_to_listed() {
        if(memo_state == 0 && scanned_img_state == 0 && uplopaded_img_state == 0 && drawing_img_state == 0) {
            return true;
        }
        else {
            return false;
        }
    }
}
