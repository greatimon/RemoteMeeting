package com.example.jyn.remotemeeting.DataClass;

/**
 * Created by JYN on 2018-01-19.
 *
 * 종료된 영상회의 목록들을 리스트로 갖는, 프로젝트 데이터클래스
 */

public class Project {

    private int project_no;
    private String project_name;
    private String project_color;
    private int project_director_user_no;
    private String project_status;
    private String project_start_dt;
    private String project_end_dt;
    private boolean project_lock;
    private int project_pw;
    private int meeting_count;

    public Project() {}

    public int getProject_no() {
        return project_no;
    }

    public void setProject_no(int project_no) {
        this.project_no = project_no;
    }

    public String getProject_name() {
        return project_name;
    }

    public void setProject_name(String project_name) {
        this.project_name = project_name;
    }

    public String getProject_color() {
        return project_color;
    }

    public void setProject_color(String project_color) {
        this.project_color = project_color;
    }

    public int getProject_director_user_no() {
        return project_director_user_no;
    }

    public void setProject_director_user_no(int project_director_user_no) {
        this.project_director_user_no = project_director_user_no;
    }

    public String getProject_status() {
        return project_status;
    }

    public void setProject_status(String project_status) {
        this.project_status = project_status;
    }

    public String getProject_start_dt() {
        return project_start_dt;
    }

    public void setProject_start_dt(String project_start_dt) {
        this.project_start_dt = project_start_dt;
    }

    public String getProject_end_dt() {
        return project_end_dt;
    }

    public void setProject_end_dt(String project_end_dt) {
        this.project_end_dt = project_end_dt;
    }

    public boolean isProject_lock() {
        return project_lock;
    }

    public void setProject_lock(boolean project_lock) {
        this.project_lock = project_lock;
    }

    public int getProject_pw() {
        return project_pw;
    }

    public void setProject_pw(int project_pw) {
        this.project_pw = project_pw;
    }

    public int getMeeting_count() {
        return meeting_count;
    }

    public void setMeeting_count(int meeting_count) {
        this.meeting_count = meeting_count;
    }
}
