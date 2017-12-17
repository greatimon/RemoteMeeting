package com.example.jyn.remotemeeting.DataClass;

/**
 * Created by JYN on 2017-12-15.
 */


import java.util.concurrent.ConcurrentHashMap;

/**
 * 이 클래스는 netty를 이용해서 서버와 통신할 때, 주고 받는 데이터를 정형화 한 것으로
 * Builder 타입을 이용해서, 필요한 데이터만 이 객체에 넣어서 전달하기 위해 만들었다
 * (특정 상황에서 서버와 통신할 때, 필요하지 않은 변수는 넣지 않고 객체를 만들 수 있음)
 * */
public class Data_for_netty {

    // builder 타입
    private final String type;            // 서버 통신 분류
    private final String subType;         // 소분류
    private final String user_no;         // 내 user_no
    private final String target_user_no;  // 타켓 user_no
    private final Chat_log chat_log;      // 채팅 메세지라면, 그 객체
    private final String attachment;      // 첨부파일 정보
    // 여기서 부터는 getter, setter
    private String extra;           // 스페어용 변수
    // 키 - user jsonString
    // 밸류 - message jsonString
    private ConcurrentHashMap<String, String> chatting_logs;
//    private ConcurrentHashMap<Users, Chat_log> chatting_logs;

    public static class Builder {
        private final String type;
        private final String subType;
        private final String user_no;
        private String target_user_no;
        private Chat_log chat_log;
        private String attachment;

        public Builder(String type, String subType, String user_no){
            this.type = type;
            this.subType = subType;
            this.user_no = user_no;
        }

        public Builder target_user_no(String target_user_no){
            this.target_user_no = target_user_no;
            return this;
        }
        public Builder chat_log(Chat_log chat_log){
            this.chat_log = chat_log;
            return this;
        }
        public Builder attachment(String attachment){
            this.attachment = attachment;
            return this;
        }

        public Data_for_netty build() {
            return new Data_for_netty(this);
        }
    }

    public Data_for_netty(Builder builder){
        type = builder.type;
        subType = builder.subType;
        user_no = builder.user_no;
        target_user_no = builder.target_user_no;
        chat_log = builder.chat_log;
        attachment = builder.attachment;
    }

    public String getType() {
        return type;
    }

    public String getSubType() {
        return subType;
    }

    public String getUser_no() {
        return user_no;
    }

    public String getTarget_user_no() {
        return target_user_no;
    }

    public Chat_log getChat_log() {
        return chat_log;
    }

    public String getAttachment() {
        return attachment;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public ConcurrentHashMap<String, String> getChatting_logs() {
        return chatting_logs;
    }

    public void setChatting_logs(ConcurrentHashMap<String, String> chatting_logs) {
        this.chatting_logs = chatting_logs;
    }


//    public ConcurrentHashMap<Users, Chat_log> getChatting_logs() {
//        return chatting_logs;
//    }
//
//    public void setChatting_logs(ConcurrentHashMap<Users, Chat_log> chatting_logs) {
//        this.chatting_logs = chatting_logs;
//    }
}
