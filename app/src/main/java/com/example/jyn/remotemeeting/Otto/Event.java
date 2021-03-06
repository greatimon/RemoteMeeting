package com.example.jyn.remotemeeting.Otto;

import com.example.jyn.remotemeeting.DataClass.Data_for_netty;

/**
 * Created by JYN on 2017-11-17.
 */

public class Event {


    /** 이벤트
     *    Netty_handler --> Chat_F
     *    : Data_for_netty 객체와 함께 채팅방 리사이클러뷰에 대한 변경점 이벤트 메시지를 전달
     * */
    public static class Chat_handler__Chat_F {
        private String message;
        private Data_for_netty data;

        public Chat_handler__Chat_F(String message, Data_for_netty data) {
            this.message = message;
            this.data = data;
        }

        public String getMessage() {
            return message;
        }

        public Data_for_netty getData() {
            return data;
        }
    }


    /** 이벤트
     *    Main_after_login_A --> Partner_F
     *    : 파트너 리스트를 서버로부터 받아 리사이클러뷰를 갱신하라는 이벤트 메시지를 전달
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
     *    Myapp --> Chat_A
     *    : 내가 해당 채팅방에서 읽은 첫번째 메시지와, 마지막 메세지 번호를 업데이트 했으니
     *    : 다시 서버로 부터 채팅 로그를 받아서 읽음 카운트 수를 업데이트 해라
     * */
    public static class Myapp__Chat_A {
        private String message;

        public Myapp__Chat_A(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }


    /** 이벤트
     *    RCV_Chat_log_list_adapter --> Chat_A
     *    : 채팅 로그들을 새로 받아, 리사이클러뷰를 갱신하라는 이벤트 메세지를 전달
     * */
    public static class RCV_Chat_log_list_adapter__Chat_A {
        private String message;

        public RCV_Chat_log_list_adapter__Chat_A(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }


    /** 이벤트
     *    Main_after_login_A --> Chat_F
     *    : 채팅방 리스트를 서버로부터 받아 리사이클러뷰를 갱신하라는 이벤트 메시지를 전달
     * */
    public static class Main_after_login_A__Chat_F {
        private String message;

        public Main_after_login_A__Chat_F(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }


    /** 이벤트
     *    Call_A --> Call_F
     *    : 이 메소드를 통해 여러 기능이 작동하는데,
     *    1) 사용자가 선택하는 포맷의 종류에 따라 로컬 파일들을 가져와서 리스트를 보여주라는 이벤트 메시지를 전달
     *    2) 업로드 확인을 받는 다이얼로그로 부터 '예, 아니오'를 선택받아, 그에 따른 로직을 진행하라는 이벤트 메시지를 전달
     *    3) 이미지 공유모드를 시작하기 직전, Call_F에 있는 video_on_off()를 실행하여, webRTC의 영상전송 모드를 off로 설정
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
     *    Call_F --> Call_A
     *    : 영상회의 중 비디오를 키고, 끌 때, 백업뷰를 켜고 끄라는 메세지 전달
     * */
    public static class Call_F__Call_A {
        private boolean message;

        public Call_F__Call_A(boolean message) {
            this.message = message;
        }

        public boolean getMessage() {
            return message;
        }
    }

    /** 이벤트
     *    Call_F --> Call_A
     *    : 영상회의 중 얼굴인식 모드 on/off 메세지 전달
     * */
    public static class Call_F__Call_A_face_recognition {
        private boolean message;

        public Call_F__Call_A_face_recognition(boolean message) {
            this.message = message;
        }

        public boolean getMessage() {
            return message;
        }
    }


    /** 이벤트
     *    Main_after_login_A --> Profile_F
     *    : 이미지 셋팅하라는 이벤트 메시지를 전달
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
     *    Call_F --> Call_A
     *    : 파일 공유 모드 버튼 클릭되었으니,
     *      '상대방에게 파일공유모드 요청을 보내' 라는 이벤트 메시지를 전달
     * */
    public static class Call_F__Call_A_file_share {
        private String message;

        public Call_F__Call_A_file_share(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }


    /** 이벤트
     *    Main_after_login_A --> Project_F
     *    : 새 프로젝트를 생성하였으니,
     *      '프로젝트 리스트를 서버로부터 다시 받아서 갱신해라' 라는 이벤트 메시지를 전달
     * */
    public static class Main_after_login_A__Project_F {
        private String message;

        public Main_after_login_A__Project_F(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
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
