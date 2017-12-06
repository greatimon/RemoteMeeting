package com.example.jyn.remotemeeting.DataClass;

import android.app.Application;
import android.util.Log;

import com.example.jyn.remotemeeting.Util.File_search;

/**
 * Created by JYN on 2017-11-17.
 */

public class File_info extends Application {

    private int file_no = -1;
    private String file_upload_user_no = "";
    private String file_format = "";
    private String file_name = "";
    private String meeting_no = "";
    private int project_no = 0;
    private String file_upload_dt = "";
    private int upload_pack_sequence = -1;
    private String extra = "no";
    private boolean visual_share = false;
    private String canonicalPath = "";

    public static String TAG = "all_"+File_info.class.getSimpleName();

    public File_info() {}


    public int getFile_no() {
        return file_no;
    }

    public void setFile_no(int file_no) {
        this.file_no = file_no;
    }

    public String getFile_upload_user_no() {
        return file_upload_user_no;
    }

    public void setFile_upload_user_no(String file_upload_user_no) {
        this.file_upload_user_no = file_upload_user_no;
    }

    public String getFile_format() {
        return file_format;
    }

    public void setFile_format(String file_format) {
        this.file_format = file_format;
    }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {

        // 로컬 파일일 때
        if(file_name.contains("Download") ||
                file_name.contains("DCIM") ||
                file_name.contains("KakaoTalkDownload")) {

            // 절대 경로와 파일이름을 담은 String 값을 canonicalPath 에 담고
            canonicalPath = file_name;

            // 파일이름만 따로 빼서, file_name 에 담는다
            String[] temp_content = canonicalPath.split("[/]");
            String real_file_name = temp_content[temp_content.length-1];
            Log.d(TAG, "real_file_name: " + real_file_name);
            this.file_name = real_file_name;

            // 포맷을 따로 빼서 file_format 에 담는다
            String[] temp = real_file_name.split("[.]");
            this.file_format = temp[temp.length-1];
        }
        // 서버에서 받은 파일 일 때,
        else {
            this.file_name = file_name;
        }
    }

    public String getMeeting_no() {
        return meeting_no;
    }

    public void setMeeting_no(String meeting_no) {
        this.meeting_no = meeting_no;
    }

    public int getProject_no() {
        return project_no;
    }

    public void setProject_no(int project_no) {
        this.project_no = project_no;
    }

    public String getFile_upload_dt() {
        return file_upload_dt;
    }

    public void setFile_upload_dt(String file_upload_dt) {
        this.file_upload_dt = file_upload_dt;
    }

    public int getUpload_pack_sequence() {
        return upload_pack_sequence;
    }

    public void setUpload_pack_sequence(int upload_pack_sequence) {
        this.upload_pack_sequence = upload_pack_sequence;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public boolean isVisual_share() {
        return visual_share;
    }

    public void setVisual_share(boolean visual_share) {
        this.visual_share = visual_share;
    }

    public String getCanonicalPath() {
        return canonicalPath;
    }
}
