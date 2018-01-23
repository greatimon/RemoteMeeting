package com.example.jyn.remotemeeting.DataClass;

import java.util.ArrayList;

/**
 * Created by JYN on 2018-01-23.
 *
 * 영상회의 중, 파일 공유모드에서 드로잉한 파일을 로컬에 저장할 때,
 * 해당 영상회의 번호와, 저장한 파일이름을 어레이리스트에 넣어
 * 로컬인 sharedPreference 에 저장하기 위한 데이터 클래스
 * (gson을 이용해서 쉽게 파싱하기 위해서 데이터 클래스를 생성함)
 */

public class Drawing_images_saveFile {

    // 회의 번호
    private String meeting_no;
    // 드로잉 이미지 파일이름을 요소로 하는 어레이리스트
    private ArrayList<String> drawing_images_fileName_arr;


    public Drawing_images_saveFile() {
        drawing_images_fileName_arr = new ArrayList<>();
    }


    public String getMeeting_no() {
        return meeting_no;
    }

    public void setMeeting_no(String meeting_no) {
        this.meeting_no = meeting_no;
    }

    public ArrayList<String> getDrawing_images_fileName_arr() {
        return drawing_images_fileName_arr;
    }

    public void setDrawing_images_fileName_arr(ArrayList<String> drawing_images_fileName_arr) {
        this.drawing_images_fileName_arr = drawing_images_fileName_arr;
    }

    public void add_item(String drawing_image_fileName) {
        drawing_images_fileName_arr.add(drawing_image_fileName);
    }

    public int file_nums() {
        return drawing_images_fileName_arr.size();
    }
}
