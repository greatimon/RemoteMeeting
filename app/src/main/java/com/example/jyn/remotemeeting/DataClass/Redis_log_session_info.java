package com.example.jyn.remotemeeting.DataClass;

/**
 * Created by JYN on 2018-01-25.
 *
 * 레디스에 저장하는 로그의 한 종류
 * - 사용자의 세션 enter/out date 정보를 담는 클래스
 */

public class Redis_log_session_info {

    // Redis_log 공통 변수
    private String date_now;
    private String user_no;
    private String nickname;
    private String session_id;

    //// 이 로그 타입에만 있는 변수
    // session 종류
    private String type;
    private String method;

    // 생성자
    public Redis_log_session_info() {}

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
