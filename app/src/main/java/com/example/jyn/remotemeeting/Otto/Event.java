package com.example.jyn.remotemeeting.Otto;

/**
 * Created by JYN on 2017-11-17.
 */

public class Event {

    /** 이벤트 */
    public static class Main_after_login_A__Partner_F {
        private String message;

        public Main_after_login_A__Partner_F(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    /** 이벤트 */
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

    /** 이벤트 */
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
}
