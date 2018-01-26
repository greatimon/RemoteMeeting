package com.example.jyn.remotemeeting.DataClass;

/**
 * Created by JYN on 2018-01-25.
 *
 * 레디스에 저장하는 로그의 한 종류
 * - 클릭이벤트 로그: 사용자가 클릭한 뷰의 id 값을 담는 클래스
 */

public class Redis_log_click_event {

    // Redis_log 공통 변수
    private String date_now;
    private String user_no;
    private String nickname;
    private String session_id;

    //// 이 로그 타입에만 있는 변수
    // 현재 클래스
    private String curr_class;
    // 클릭한 뷰의 id, int 값
    private int click_id_int;
    // 클릭한 뷰의 id, String 값
    private String click_id_str;

    // 생성자
    public Redis_log_click_event() {}

    //// getter, setter
    public String getDate_now() {
        return date_now;
    }

    public void setDate_now(String date_now) {
        this.date_now = date_now;
    }

    public String getUser_no() {
        return user_no;
    }

    public void setUser_no(String user_no) {
        this.user_no = user_no;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getSession_id() {
        return session_id;
    }

    public void setSession_id(String session_id) {
        this.session_id = session_id;
    }

    public String getCurr_class() {
        return curr_class;
    }

    public void setCurr_class(String curr_class) {
        this.curr_class = curr_class;
    }

    public int getClick_id_int() {
        return click_id_int;
    }

    public void setClick_id_int(int click_id_int) {
        this.click_id_int = click_id_int;
    }

    public String getClick_id_str() {
        return click_id_str;
    }

    public void setClick_id_str(String click_id_str) {
        this.click_id_str = click_id_str;
    }
}
