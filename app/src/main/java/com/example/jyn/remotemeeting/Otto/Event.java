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

    /** 이벤트 */
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
