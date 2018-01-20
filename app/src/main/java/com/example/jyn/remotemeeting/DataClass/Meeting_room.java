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
    private String extra_column_2;
    private String extra_column_3;

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

    public String getExtra_column_2() {
        return extra_column_2;
    }

    public void setExtra_column_2(String extra_column_2) {
        this.extra_column_2 = extra_column_2;
    }

    public String getExtra_column_3() {
        return extra_column_3;
    }

    public void setExtra_column_3(String extra_column_3) {
        this.extra_column_3 = extra_column_3;
    }
}
