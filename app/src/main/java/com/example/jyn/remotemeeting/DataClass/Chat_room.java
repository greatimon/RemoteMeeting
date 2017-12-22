package com.example.jyn.remotemeeting.DataClass;

import android.graphics.Bitmap;

import com.example.jyn.remotemeeting.Util.Myapp;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by JYN on 2017-12-09.
 */

public class Chat_room implements Serializable {

    private int chatroom_no;
    private int last_msg_no;
//    private ArrayList<Integer> user_no_arr;
    private int authority_user_no;
    private ArrayList<Users> user_arr;
    private ArrayList<String> user_nickname_arr;
    private ArrayList<String> user_img_filename_arr;
    private Chat_log last_log = null;
    private String transmission_time_for_local = "";
    private int unread_msg_count;
    private String chat_room_title;
    private boolean combined_img_ready = false;
    private Bitmap combined_img_bitmap;

    public int getUnread_msg_count() {
        return unread_msg_count;
    }

    public void setUnread_msg_count(int unread_msg_count) {
        this.unread_msg_count = unread_msg_count;
    }

    public Myapp myapp;

    public static String TAG = "all_"+Chat_room.class.getSimpleName();

    public Chat_room() {
        myapp = Myapp.getInstance();
    }

    public Chat_log getLast_log() {
        if(last_log != null) {
            return last_log;
        }
        return null;
    }


    public void setLast_log(Chat_log last_log) {
        this.last_log = last_log;

        // 마지막 로그 정보를 set 할 때, 마지막 로그 정보에서 long 타입의 메세지 전송 시각을 가져와서,
        // 핸드폰 기기 국가(로컬) 기준으로 원하는 time type String 값으로 변환하여 set 한다
        String last_log_transmission_time =
                myapp.chat_log_transmission_time(this.last_log.getTransmission_gmt_time(), "chatroom_list");
        transmission_time_for_local = last_log_transmission_time;
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

//    public ArrayList<Integer> getUser_no_arr() {
//        return user_no_arr;
//    }

//    public void setUser_no_arr(ArrayList<Integer> user_no_arr) {
//        this.user_no_arr = user_no_arr;
//    }

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

    public String getChat_room_title() {
        return chat_room_title;
    }

    public void setChat_room_title(String chat_room_title) {
        this.chat_room_title = chat_room_title;
    }

    public ArrayList<Users> getUser_arr() {
        return user_arr;
    }

    public void setUser_arr(ArrayList<Users> user_arr) {
        this.user_arr = user_arr;
    }

    public int getAuthority_user_no() {
        return authority_user_no;
    }

    public void setAuthority_user_no(int authority_user_no) {
        this.authority_user_no = authority_user_no;
    }

    public boolean isCombined_img_ready() {
        return combined_img_ready;
    }

    public void setCombined_img_ready(boolean combined_img_ready) {
        this.combined_img_ready = combined_img_ready;
    }

    public Bitmap getCombined_img_bitmap() {
        return combined_img_bitmap;
    }

    public void setCombined_img_bitmap(Bitmap combined_img_bitmap) {
        this.combined_img_bitmap = combined_img_bitmap;
    }
}
