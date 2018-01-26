package com.example.jyn.remotemeeting.DataClass;

/**
 * Created by JYN on 2018-01-25.
 *
 * 레디스에 저장하는 로그의 한 종류
 * - 사용자가 이쪽 뷰 클래스에서 저쪽 뷰 클래스로 이동할 때,
 *   그 이동 정보를 담는 클래스
 */

public class Redis_log_view_crossOver_from_to {

    // Redis_log 공통 변수
    private String date_now;
    private String user_no;
    private String nickname;
    private String session_id;

    //// 이 로그 타입에만 있는 변수
    // 현재 클래스
    private String from_class;
    // 이동하려는 타켓 클래스
    private String to_class;

    // 생성자
    public Redis_log_view_crossOver_from_to() {}

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

    public String getSession_id() {
        return session_id;
    }

    public void setSession_id(String session_id) {
        this.session_id = session_id;
    }

    public String getFrom_class() {
        return from_class;
    }

    public void setFrom_class(String from_class) {
        this.from_class = from_class;
    }

    public String getTo_class() {
        return to_class;
    }

    public void setTo_class(String to_class) {
        this.to_class = to_class;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
