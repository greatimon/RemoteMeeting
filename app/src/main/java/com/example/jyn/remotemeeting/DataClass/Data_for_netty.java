package com.example.jyn.remotemeeting.DataClass;

/**
 * Created by JYN on 2017-12-15.
 */


/**
 * 이 클래스는 netty를 이용해서 서버와 통신할 때, 주고 받는 데이터를 정형화 한 것으로
 * Builder 타입을 이용해서, 필요한 데이터만 이 객체에 넣어서 전달하기 위해 만들었다
 * (특정 상황에서 서버와 통신할 때, 필요하지 않은 변수는 넣지 않고 객체를 만들 수 있음)
 * */
public class Data_for_netty {

    private final String type;            // 서버 통신 분류
    private final String subType;         // 소분류
    private final String user_no;         // 내 user_no
    private final String target_user_no;  // 타켓 user_no
    private final String msg;             // 채팅 메세지라면, 그 내용
    private final String attachment;      // 첨부파일 정보
    private final String extra;           // 스페어용 변수


    public static class Builder {
        private final String type;
        private final String subType;
        private final String user_no;
        private String target_user_no;
        private String msg;
        private String attachment;
        private String extra;

        public Builder(String type, String subType, String user_no){
            this.type = type;
            this.subType = subType;
            this.user_no = user_no;
        }

        public Builder target_user_no(String target_user_no){
            this.target_user_no = target_user_no;
            return this;
        }
        public Builder msg(String msg){
            this.msg = msg;
            return this;
        }
        public Builder attachment(String attachment){
            this.attachment = attachment;
            return this;
        }
        public Builder extra(String extra){
            this.extra = extra;
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
        msg = builder.msg;
        attachment = builder.attachment;
        extra = builder.extra;
    }
}
