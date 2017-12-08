package com.example.jyn.remotemeeting.DataClass;

import com.example.jyn.remotemeeting.Util.Myapp;

import java.util.ArrayList;

/**
 * Created by JYN on 2017-12-09.
 */

public class Chat_room {

    private int chatroom_no;
    private int last_msg_no;
    private ArrayList<Integer> user_no_arr;
    private ArrayList<String> user_nickname_arr;
    private ArrayList<String> user_img_filename_arr;
    private Chat_log last_log;
    private String transmission_time_for_local;
    private int read_msg_count;

    public int getRead_msg_count() {
        return read_msg_count;
    }

    public void setRead_msg_count(int read_msg_count) {
        this.read_msg_count = read_msg_count;
    }

    public Myapp myapp;

    public static String TAG = "all_"+Chat_room.class.getSimpleName();

    Chat_room() {
        myapp = Myapp.getInstance();
    }

    public Chat_log getLast_log() {
        return last_log;
    }

    // 마지막 로그 정보를 set 할 때, 마지막 로그 정보에서 long 타입의 메세지 전송 시각을 가져와서,
    // 핸드폰 기기 국가(로컬) 기준으로 원하는 time type String 값으로 변환하여 set 한다
    public void setLast_log(Chat_log last_log) {
        this.last_log = last_log;

        String transmission_time =  myapp.chat_log_transmission_time(this.last_log.getTransmission_time());
        transmission_time_for_local = transmission_time;
    }

    public int getChatroom_no() {
        return chatroom_no;
    }

    public void setChatroom_no(int chatroom_no) {
        this.chatroom_no = chatroom_no;
    }

    public int getLast_msg_no() {
        return last_msg_no;
    }

    public void setLast_msg_no(int last_msg_no) {
        this.last_msg_no = last_msg_no;
    }

    public ArrayList<String> getUser_nickname_arr() {
        return user_nickname_arr;
    }

    public void setUser_nickname_arr(ArrayList<String> user_nickname_arr) {
        this.user_nickname_arr = user_nickname_arr;
    }

    public ArrayList<Integer> getUser_no_arr() {
        return user_no_arr;
    }

    public void setUser_no_arr(ArrayList<Integer> user_no_arr) {
        this.user_no_arr = user_no_arr;
    }

    public String getTransmission_time_for_local() {
        return transmission_time_for_local;
    }

    public void setTransmission_time_for_local(String transmission_time_for_local) {
        this.transmission_time_for_local = transmission_time_for_local;
    }

    public ArrayList<String> getUser_img_filename_arr() {
        return user_img_filename_arr;
    }

    public void setUser_img_filename_arr(ArrayList<String> user_img_filename_arr) {
        this.user_img_filename_arr = user_img_filename_arr;
    }
}
