package com.example.jyn.remotemeeting.Otto;

import com.example.jyn.remotemeeting.DataClass.Chat_room;

/**
 * Created by JYN on 2017-11-17.
 */

public class Event {


    /** 이벤트
     *    Main_after_login_A --> Partner_F
     *    : 파트너 리스트를 서버로부터 받아 리사이클러뷰를 갱신
     * */
    public static class Main_after_login_A__Partner_F {
        private String message;

        public Main_after_login_A__Partner_F(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }


    /** 이벤트
     *    Call_A --> Call_F
     *    : 이 메소드를 통해 여러 기능이 작동하는데,
     *    1) 사용자가 선택하는 포맷의 종류에 따라 로컬 파일들을 가져와서 리스트를 보여줌
     *    2) 업로드 확인을 받는 다이얼로그로 부터 '예, 아니오'를 선택받아, 그에 따른 로직을 진행
     *    (1번째 파라미터인 'message'를 통해 위 상황을 구별한다)
     * */
    public static class Call_A__Call_F {
        private String message;
        private String data;

        public Call_A__Call_F(String message, String data) {
            this.message = message;
            this.data = data;
        }

        public String getMessage() {
            return message;
        }

        public String getData() {
            return data;
        }
    }


    /** 이벤트
     *    Main_after_login_A --> Profile_F
     *    : 이미지 셋팅 이벤트를 전달한다
     * */
    public static class Main_after_login_A__Profile_F {
        private String message;
        private String data;

        public Main_after_login_A__Profile_F(String message, String data) {
            this.message = message;
            this.data = data;
        }

        public String getMessage() {
            return message;
        }

        public String getData() {
            return data;
        }
    }


    /** 이벤트
     *    RCV_chat_adapter --> Chat_A
     *    : Chat_room 객체를 전달한다
     * */
    public static class RCV_chat_adapter__Chat_A {
        private String message;
        private Chat_room chat_room;

        public RCV_chat_adapter__Chat_A(String message, Chat_room chat_room) {
            this.message = message;
            this.chat_room = chat_room;
        }

        public String getMessage() {
            return message;
        }

        public Chat_room getChat_room() {
            return chat_room;
        }
    }


    /** 이벤트
     *    Myapp --> Call_F
     *    1) PDF 를 이미지로 컨버팅하는 과정에서, 진행상황을 '다이얼로그'로 보여주는데,
     *       그 다이얼로그의 상태를 컨트롤하는 이벤트를 전달한다
     *    2) 업로드가 완료됐을 때, '파일 공유함'으로 이동하고, 파일 공유함에 공유파일들을
     *       리스팅하는 서버 메소드를 호출하라는 이벤트를 전달한다
     *    (1번째 파라미터인 'message'를 통해 위 상황을 구별한다)
     * */
    public static class Myapp__Call_F {
        private String message;
        private String data;
        private int total_pdf_files_count;
        private int current_sequence;
        private String file_name;
        private int percent;
        private int total_pdf_page_nums;
        private int current_pdf_page;

        public Myapp__Call_F(String message, String data,
                             int total_pdf_files_count, int current_sequence,
                             String file_name, int percent,
                             int total_pdf_page_nums, int current_pdf_page) {
            this.message = message;
            this.data = data;
            this.total_pdf_files_count = total_pdf_files_count;
            this.current_sequence = current_sequence;
            this.file_name = file_name;
            this.percent = percent;
            this.total_pdf_page_nums = total_pdf_page_nums;
            this.current_pdf_page = current_pdf_page;
        }

        public String getMessage() {
            return message;
        }

        public String getData() {
            return data;
        }

        public int getTotal_pdf_files_count() {
            return total_pdf_files_count;
        }

        public int getCurrent_sequence() {
            return current_sequence;
        }

        public String getFile_name() {
            return file_name;
        }

        public int getPercent() {
            return percent;
        }

        public int getTotal_pdf_page_nums() {
            return total_pdf_page_nums;
        }

        public int getCurrent_pdf_page() {
            return current_pdf_page;
        }
    }
}
