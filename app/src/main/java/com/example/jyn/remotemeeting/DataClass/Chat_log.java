package com.example.jyn.remotemeeting.DataClass;

/**
 * Created by JYN on 2017-12-09.
 */

// 서버쪽 데이터 클래스 이름은 'Message' 로 네이밍 되어 있음
public class Chat_log {
    private int chat_log_no;
    private int chatroom_no;
    private String type;
    private int user_no;
    private Long transmission_time;
    private String content;
    private String attachment;
    private int target_read_count;
    private int current_read_count;

    public Chat_log() {}

    public int getChat_log_no() {
        return chat_log_no;
    }

    public void setChat_log_no(int chat_log_no) {
        this.chat_log_no = chat_log_no;
    }

    public int getChatroom_no() {
        return chatroom_no;
    }

    public void setChatroom_no(int chatroom_no) {
        this.chatroom_no = chatroom_no;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getUser_no() {
        return user_no;
    }

    public void setUser_no(int user_no) {
        this.user_no = user_no;
    }

    public Long getTransmission_time() {
        return transmission_time;
    }

    public void setTransmission_time(Long transmission_time) {
        this.transmission_time = transmission_time;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    public int getTarget_read_count() {
        return target_read_count;
    }

    public void setTarget_read_count(int target_read_count) {
        this.target_read_count = target_read_count;
    }

    public int getCurrent_read_count() {
        return current_read_count;
    }

    public void setCurrent_read_count(int current_read_count) {
        this.current_read_count = current_read_count;
    }
}
