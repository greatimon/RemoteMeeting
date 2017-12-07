package com.example.jyn.remotemeeting.Util;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jyn.remotemeeting.DataClass.File_info;
import com.example.jyn.remotemeeting.DataClass.Users;
import com.example.jyn.remotemeeting.Etc.Static;
import com.example.jyn.remotemeeting.Fragment.Call_F;
import com.example.jyn.remotemeeting.Otto.BusProvider;
import com.example.jyn.remotemeeting.Otto.Event;
import com.example.jyn.remotemeeting.R;
import com.google.gson.Gson;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.rendering.PDFRenderer;
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by JYN on 2017-12-01.
 */

public class Myapp extends Application {

    private static final String TAG = "all_"+Myapp.class.getSimpleName();
    String JSON_TAG_PARTNER_LIST = "partner_list";
    String JSON_TAG_SEARCH_LIST = "search_list";
    String JSON_TAG_SHARE_FILE_LIST = "share_file_list";
    private static Myapp appInstance;
    Toast logToast;
    ProgressDialog progressDialog;
    HashMap<String, String> checked_files;
    HashMap<String, String> files_for_upload;
    File root;
    String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/RemoteMeeting";
    Handler handler;
    int PDF_converting_exception_file_count = 0;

    public int[] back_img = {
            R.drawable.back_1,
            R.drawable.back_2,
            R.drawable.back_3,
            R.drawable.back_4,
            R.drawable.back_5,
            R.drawable.back_6,
            R.drawable.back_7,
    };

    public int[] round_back_img = {
            R.drawable.back__1,
            R.drawable.back__2,
            R.drawable.back__3,
            R.drawable.back__4,
            R.drawable.back__5,
            R.drawable.back__6,
            R.drawable.back__7,
    };

    /** 내 정보 */
    public String user_no = "";
    public String join_path = "";
    public String join_dt = "";
    public String user_email = "";
    public String user_nickname = "";
    public String present_meeting_in_ornot = "";
    public String user_img_filename = "";

    public String temp_nickname="";
    public String temp_img_filename="";
    public String temp_img_absolutePath="";

    /** 참여 회의 정보 */
    public String meeting_no = "";
    public String real_meeting_title = "";
    public String meeting_creator_user_no = "";
    public String meeting_subject_user_no = "";
    public String meeting_authority_user_no = "";
    public String project_no = "";
    public String meeting_status = "";


    /** GET, SET */
    public String getUser_no() {
        return user_no;
    }
    public void setUser_no(String user_no) {
        this.user_no = user_no;
    }
    public String getJoin_path() {
        return join_path;
    }
    public void setJoin_path(String join_path) {
        this.join_path = join_path;
    }
    public String getJoin_dt() {
        return join_dt;
    }
    public void setJoin_dt(String join_dt) {
        this.join_dt = join_dt;
    }
    public String getUser_email() {
        return user_email;
    }
    public void setUser_email(String user_email) {
        this.user_email = user_email;
    }
    public String getUser_nickname() {
        return user_nickname;
    }
    public void setUser_nickname(String user_nickname) {
        this.user_nickname = user_nickname;
    }
    public String getPresent_meeting_in_ornot() {
        return present_meeting_in_ornot;
    }
    public void setPresent_meeting_in_ornot(String present_meeting_in_ornot) {
        this.present_meeting_in_ornot = present_meeting_in_ornot;
    }
    public String getUser_img_filename() {
        return user_img_filename;
    }
    public void setUser_img_filename(String user_img_filename) {
        this.user_img_filename = user_img_filename;
    }
    public String getTemp_nickname() {
        return temp_nickname;
    }
    public void setTemp_nickname(String temp_nickname) {
        this.temp_nickname = temp_nickname;
    }
    public String getTemp_img_filename() {
        return temp_img_filename;
    }
    public void setTemp_img_filename(String temp_img_filename) {
        this.temp_img_filename = temp_img_filename;
    }
    public String getTemp_img_absolutePath() {
        return temp_img_absolutePath;
    }
    public void setTemp_img_absolutePath(String temp_img_absolutePath) {
        this.temp_img_absolutePath = temp_img_absolutePath;
    }
    public String getMeeting_no() {
        return meeting_no;
    }
    public void setMeeting_no(String meeting_no) {
        this.meeting_no = meeting_no;
    }
    public String getReal_meeting_title() {
        return real_meeting_title;
    }
    public void setReal_meeting_title(String real_meeting_title) {
        this.real_meeting_title = real_meeting_title;
    }
    public String getMeeting_creator_user_no() {
        return meeting_creator_user_no;
    }
    public void setMeeting_creator_user_no(String meeting_creator_user_no) {
        this.meeting_creator_user_no = meeting_creator_user_no;
    }
    public String getMeeting_subject_user_no() {
        return meeting_subject_user_no;
    }
    public void setMeeting_subject_user_no(String meeting_subject_user_no) {
        this.meeting_subject_user_no = meeting_subject_user_no;
    }
    public String getMeeting_authority_user_no() {
        return meeting_authority_user_no;
    }
    public void setMeeting_authority_user_no(String meeting_authority_user_no) {
        this.meeting_authority_user_no = meeting_authority_user_no;
    }
    public String getProject_no() {
        return project_no;
    }
    public void setProject_no(String project_no) {
        this.project_no = project_no;
    }
    public String getMeeting_status() {
        return meeting_status;
    }
    public void setMeeting_status(String meeting_status) {
        this.meeting_status = meeting_status;
    }
    public HashMap<String, String> getChecked_files() {
        return checked_files;
    }
    public void setChecked_files(HashMap<String, String> checked_files) {
        this.checked_files = checked_files;
    }

    public int getPDF_converting_exception_file_count() {
        return PDF_converting_exception_file_count;
    }

    public void setPDF_converting_exception_file_count(int PDF_converting_exception_file_count) {
        this.PDF_converting_exception_file_count = PDF_converting_exception_file_count;
    }

    public HashMap<String, String> getFiles_for_upload() {
        return files_for_upload;
    }

    /** 생명주기 - onCreate */
    @Override
    public void onCreate() {
        super.onCreate();
        appInstance = this;
        checked_files = new HashMap<>();
        files_for_upload = new HashMap<>();
    }

    /** 싱글톤 */
    public static Myapp getInstance() {
        return appInstance;
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 이메일 정규식 -- 영문, 숫자 조합 + '@' + 뒤에 '.' 포함
     ---------------------------------------------------------------------------*/
    public boolean email_check(String email) {
        String EMAIL_REGEX = "^[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$";
        Boolean b = email.matches(EMAIL_REGEX);
        return b;
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 비밀번호 정규식 -- 영문, 숫자, 특수문자 조합 / 8자리이상 16자이하
     ---------------------------------------------------------------------------*/
    public boolean pw_check(String pw) {
        String PASSWORD_REGEX = "^(?=.*[a-zA-Z]+)(?=.*[0-9]+)(?=.*[!@#$%^&*?_~]+).{8,16}$";
        Boolean b = pw.matches(PASSWORD_REGEX);
        return b;
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 닉네임 정규식 -- 모든 문자열 / 2자리이상 20자이하
     ---------------------------------------------------------------------------*/
    public boolean nickName_check(String nickName) {
        String NICKNAME_REGEX = "^[\\w\\Wㄱ-ㅎㅏ-ㅣ가-힣]{2,20}$";
        Boolean b = nickName.matches(NICKNAME_REGEX);
        return b;
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 로그 + 토스트
     ---------------------------------------------------------------------------*/
    public void logAndToast(String msg) {
        Log.d(TAG, "logAndToast- " + msg);
        if (logToast != null) {
            logToast.cancel();
        }
        logToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        logToast.show();
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 내 user 객체 정보 저장
     ---------------------------------------------------------------------------*/
    public void set_myInfo(JSONObject user_object) {
        try {
            this.user_no = user_object.getString("user_no");
            this.join_path = user_object.getString("join_path");
            this.join_dt = user_object.getString("join_dt");
            this.user_email = user_object.getString("user_email");
            this.user_nickname = user_object.getString("user_nickname");
            this.present_meeting_in_ornot = user_object.getString("present_meeting_in_ornot");
            this.user_img_filename = user_object.getString("user_img_filename");
            Log.d(TAG, "user_no: " + user_no);
            Log.d(TAG, "join_path: " + join_path);
            Log.d(TAG, "join_dt: " + join_dt);
            Log.d(TAG, "user_email: " + user_email);
            Log.d(TAG, "user_nickname: " + user_nickname);
            Log.d(TAG, "present_meeting_in_ornot: " + present_meeting_in_ornot);
            Log.d(TAG, "user_img_filename: " + user_img_filename);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 서버 통신 -- 해당 회의번호로 업로드 된 파일 리스트 가져와서 리턴하기
     ---------------------------------------------------------------------------*/
    @SuppressLint("StaticFieldLeak")
    public ArrayList<File_info> get_uploaded_file_list(Context context) {
        ArrayList<File_info> files = new ArrayList<>();
        final RetrofitService rs = ServiceGenerator.createService(RetrofitService.class);

        // 프로그래스 다이얼로그 호출
        show_progress(context, "파일 리스트업 중입니다");

        // 동기 호출
        try {
            final ArrayList<File_info> final_files = files;

            return new AsyncTask<Void, Void, ArrayList<File_info>>() {

                // 통신 끝나고 리턴한 뒤
                @Override
                protected void onPostExecute(ArrayList<File_info> file_infos) {
                    // 프로그레스 다이얼로그 dismiss
                    dismiss_progress();
                }

                // 통신 로직
                @Override
                protected ArrayList<File_info> doInBackground(Void... voids) {
                    try {
                        Call<ResponseBody> call_result = rs.get_share_file_list(
                                Static.GET_SHARE_FILE_LIST,
                                meeting_no);
                        Response<ResponseBody> list = call_result.execute();
                        String result = list.body().string();

                        try {
                            if(result.equals("fail")) {
                                logAndToast("예외발생: " + result);
                                final_files.clear();
                            }
                            else if(result.equals("no_result")) {
                                final_files.clear();
                            }
                            else {
                                // 길이가 긴 JSONString 출력하기
//                                print_long_Json_logcat(result, TAG);
                                // jsonString --> jsonObject
                                JSONObject jsonObject = new JSONObject(result);
                                // jsonObject --> jsonArray
                                JSONArray jsonArray = jsonObject.getJSONArray(JSON_TAG_SHARE_FILE_LIST);
                                Log.d(TAG, "jsonArray 개수: " + jsonArray.length());

                                // jsonArray 에서 jsonObject를 하나씩 가져와서,
                                // gson과 File_info 데이터 클래스를 이용해서 리턴할 ArrayList에 add 하기
                                Gson gson = new Gson();
                                for(int i=0; i<jsonArray.length(); i++) {
                                    String jsonString = jsonArray.getJSONObject(i).toString();
                                     File_info file = gson.fromJson(jsonString, File_info.class);
                                     final_files.add(file);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        return final_files;

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }.execute().get();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 서버 통신 -- 내 파트너 리스트 가져와서 리턴하기
     ---------------------------------------------------------------------------*/
    @SuppressLint("StaticFieldLeak")
    public ArrayList<Users> get_partner_list() {

        ArrayList<Users> user_arr = new ArrayList<>();
        final RetrofitService rs = ServiceGenerator.createService(RetrofitService.class);

        // 동기 호출
        try {
            final ArrayList<Users> finalUser_arr = user_arr;

            return new AsyncTask<Void, Void, ArrayList<Users>>() {
                @Override
                protected ArrayList<Users> doInBackground(Void... voids) {
                    try {
                        Call<ResponseBody> call_result = rs.get_partner_list(
                                Static.GET_PARTNER_LIST,
                                user_no);
                        Response<ResponseBody> list = call_result.execute();
                        String result = list.body().string();

                        try {
                            if(result.equals("fail")) {
                                logAndToast("예외발생: " + result);
                                finalUser_arr.clear();
                            }
                            else if(result.equals("no_result")) {
                                finalUser_arr.clear();
                            }
                            else {
                                // 길이가 긴 JSONString 출력하기
                                print_long_Json_logcat(result, TAG);
                                // jsonString --> jsonObject
                                JSONObject jsonObject = new JSONObject(result);
                                // jsonObject --> jsonArray
                                JSONArray jsonArray = jsonObject.getJSONArray(JSON_TAG_PARTNER_LIST);
                                Log.d(TAG, "jsonArray 개수: " + jsonArray.length());

                                // jsonArray에서 jsonObject를 하나씩 가지고 와서,
                                // gson과 user 데이터클래스를 이용하여 user_arr에 add 하기
                                for(int i=0; i<jsonArray.length(); i++) {
                                    String jsonString = jsonArray.getJSONObject(i).toString();
                                    Gson gson = new Gson();
                                    Users user = gson.fromJson(jsonString, Users.class);
                                    finalUser_arr.add(user);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        return finalUser_arr;

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }.execute().get();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 서버 통신 -- 파트너 검색하기
     ---------------------------------------------------------------------------*/
    @SuppressLint("StaticFieldLeak")
    public ArrayList<Users> search_partners(final String with_domain, final String without_domain) {
        ArrayList<Users> user_arr = new ArrayList<>();
        final RetrofitService rs = ServiceGenerator.createService(RetrofitService.class);

        // 동기 호출
        try {
            final ArrayList<Users> finalUser_arr = user_arr;
            return new AsyncTask<Void, Void, ArrayList<Users>>() {

                @Override
                protected ArrayList<Users> doInBackground(Void... voids) {
                    try {
                        Call<ResponseBody> call = rs.search_partners(
                                Static.SEARCH_PARTNERS,
                                user_no, with_domain, without_domain);
                        Response<ResponseBody> call_result = call.execute();
                        String result = call_result.body().string();

                        try {
                            if(result.equals("fail")) {
                                logAndToast("예외발생: " + result);
                                finalUser_arr.clear();
                            }
                            else if(result.equals("no_result")) {
                                finalUser_arr.clear();
                            }
                            else {
                                // 길이가 긴 JSONString 출력하기
                                print_long_Json_logcat(result, TAG);
                                // jsonString --> jsonObject
                                JSONObject jsonObject = new JSONObject(result);
                                // jsonObject --> jsonArray
                                JSONArray jsonArray = jsonObject.getJSONArray(JSON_TAG_SEARCH_LIST);
                                Log.d(TAG, "jsonArray 개수: " + jsonArray.length());

                                // jsonArray에서 jsonObject를 하나씩 가지고 와서,
                                // gson과 user 데이터클래스를 이용하여 user_arr에 add 하기
                                for(int i=0; i<jsonArray.length(); i++) {
                                    String jsonString = jsonArray.getJSONObject(i).toString();
                                    Gson gson = new Gson();
                                    Users user = gson.fromJson(jsonString, Users.class);
                                    finalUser_arr.add(user);
                                }

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        return finalUser_arr;

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }.execute().get();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 긴 JsonString Log 출력하기
     ---------------------------------------------------------------------------*/
    public void print_long_Json_logcat(String json_log, String TAG) {

        String temp_json = json_log;
        int log_index = 1;
        try {
            while (temp_json.length() > 0) {
                if (temp_json.length() > 3000) {
                    Log.d(TAG, "json - " + log_index + " : "
                            + temp_json.substring(0, 3000));
                    temp_json = temp_json.substring(3000);
                    log_index++;
                } else {
                    Log.d(TAG, "json - " + log_index + " :" + temp_json);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 서버 통신 -- 유저 한명의 정보 가져오기
     ---------------------------------------------------------------------------*/
    @SuppressLint("StaticFieldLeak")
    public Users get_user_info(final String target_user_no) {

        final Users[] users = {new Users()};
        final RetrofitService rs = ServiceGenerator.createService(RetrofitService.class);

        // 동기 호출
        try {
            return new AsyncTask<Void, Void, Users>() {
                @Override
                protected Users doInBackground(Void... voids) {
                    try {
                        Call<ResponseBody> call = rs.get_user_info(
                                Static.GET_USER_INFO,
                                target_user_no);
                        Response<ResponseBody> call_result = call.execute();
                        String result = call_result.body().string();
                        Log.d(TAG, "result: " + result);

                        if(result.equals("fail")) {
                            logAndToast("예외발생: " + result);
                        }
                        else {
                            Gson gson = new Gson();
                            users[0] = gson.fromJson(result, Users.class);

                            // 내 정보라면, 그냥 혹시 모르니 데이터 저장
                            if(users[0].getUser_no().equals(user_no)) {
                                join_path = users[0].getJoin_path();
                                join_dt = users[0].getJoin_dt();
                                user_email = users[0].getUser_email();
                                user_nickname = users[0].getUser_nickname();
                                present_meeting_in_ornot = users[0].getPresent_meeting_in_ornot();
                                user_img_filename = users[0].getUser_img_filename();
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return users[0];
                }
            }.execute().get();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 카메라 촬영 이미지 파일 이름 네이밍을 위한 메소드
     ---------------------------------------------------------------------------*/
    public String now() {
        SimpleDateFormat format_for_save = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.KOREA);
        long time_mil = System.currentTimeMillis();
        Date date_start_broadCast = new Date(time_mil);
        String result = format_for_save.format(date_start_broadCast);
        return result;
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 프로그레스 다이얼로그 show
     ---------------------------------------------------------------------------*/
    public void show_progress(Context context, String msg) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(msg);
        progressDialog.show();
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 프로그레스 다이얼로그 dismiss
     ---------------------------------------------------------------------------*/
    public void dismiss_progress() {
        if(progressDialog != null) {
            progressDialog.dismiss();
        }
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 해쉬맵 checked_files clear(초기화)
     ---------------------------------------------------------------------------*/
    public void init_checked_files() {
        if(checked_files != null) {
            checked_files.clear();
        }
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 해쉬맵 files_for_upload clear(초기화)
     ---------------------------------------------------------------------------*/
    public void init_files_for_upload() {
        if(files_for_upload != null) {
            files_for_upload.clear();
        }
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 멀티 파일 업로드 -- 동기
     ---------------------------------------------------------------------------*/
    @SuppressLint("StaticFieldLeak")
    public void upload_multi_files_1(final Context context) {

        final int total_file_nums = files_for_upload.size();
        final Long[] total_file_size = {0L};

        final RetrofitService rs = ServiceGenerator.createService(RetrofitService.class);

        // 동기 호출
        try {
            new AsyncTask<String, Void, Long>() {
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    show_progress(context, "이미지 업로드 중입니다");
                }

                @Override
                protected Long doInBackground(String... params) {

                    Iterator<String> iterator = files_for_upload.keySet().iterator();
                    // 루프 - 파일 사이즈를 구하기 위한
                    Long temp_total_file_size = 0L;
                    while(iterator.hasNext()) {
                        String value = files_for_upload.get(iterator.next());

                        File file = new File(value);
                        temp_total_file_size = temp_total_file_size + file.length();
                    }
                    total_file_size[0] = temp_total_file_size;
                    Log.d(TAG, "업로드할 파일들의 총 개수: " + files_for_upload.size());
                    Log.d(TAG, "업로드할 파일들의 총 크기: " + total_file_size[0] + " bytes");

                    // 테스트 코드
//                    files_for_upload.clear();

                    Iterator<String> iterator_2 = files_for_upload.keySet().iterator();
                    Long uploaded_file_size = 0L;

                    // 루프 - 파일 업로드를 위한
                    while(iterator_2.hasNext()) {
                        String value = files_for_upload.get(iterator_2.next());

                        // 파일 객체 생성
                        final File file = new File(value);

                        // 확장자만 분류
                        int Idx = file.getName().lastIndexOf(".");
                        String format = file.getName().substring(Idx+1);
                        Log.d(TAG, "format: " + format);

                        // RequestBody 생성 from file
                        RequestBody requestFile = RequestBody.create(MediaType.parse("image/" + format), file);
                        MultipartBody.Part body =
                                MultipartBody.Part.createFormData("image", file.getName(), requestFile);

                        // user_no
                        RequestBody user_no = RequestBody.create(MediaType.parse("text/plain"), getUser_no());

                        // meeting_no
                        RequestBody meeting_no = RequestBody.create(MediaType.parse("text/plain"), getMeeting_no());

                        Call<ResponseBody> call = rs.upload_multi_files(
                                Static.UPLOAD_MULTI_FILES,
                                user_no, meeting_no, body);
                        try {
                            Response<ResponseBody> call_result = call.execute();
                            String result = call_result.body().string();
                            Log.d(TAG, "result: " + result);

                            if(result.equals("fail")) {
                                Log.d(TAG, "업로드 실패: " + result);
                            }
                            else if(result.equals("success")) {
                                Log.d(TAG, file.getName() + "업로드 성공!!!!");
                                uploaded_file_size = uploaded_file_size + file.length();
                            }
                            else {
                                logAndToast("파일 업로드 오류" + result);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            return 0L;
                        }
                    }
                    return uploaded_file_size;
                }

                @Override
                protected void onPostExecute(Long size) {
                    super.onPostExecute(size);
                    Log.d(TAG, "uploaded_file_size: " + size);
                    Log.d(TAG, "total_file_size: " + total_file_size[0]);

                    dismiss_progress();
                    logAndToast(String.valueOf(total_file_nums) + "개 파일, 업로드 완료");

                    BusProvider.getBus().register(this);        // otto 등록
                    // otto 를 통해, 프래그먼트로 이벤트 전달하기
                    Event.Myapp__Call_F myapp__call_f = new Event.Myapp__Call_F("upload", "end",
                            0, 0, "", 0, 0, 0);
                    BusProvider.getBus().post(myapp__call_f);
                    BusProvider.getBus().unregister(this);      // otto 등록 해제
                }
            }.execute();

        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**---------------------------------------------------------------------------
     메소드 ==> PDF 파일 있으면 이미지로 변환한 뒤, 멀티 파일 업로드 메소드 호출
     ---------------------------------------------------------------------------*/
    public void checed_pdf_files(String contain_padf_file_orNot, Context context) {

        // RemoteMeeting 디렉토리가 존재하지 않으면 디렉토리 생성
        File folder = new File(sdPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        // PDF 파일이 있다면, PDF 파일 걸러서 ArrayList 에 담기
        if(contain_padf_file_orNot.equals("true")) {

            // Pdfbox 사용을 위한 init 메소드 호출
            setup();

            // checked_files Iterator
            Iterator<String> iterator = checked_files.keySet().iterator();

            // Pdf 파일들 리스트만 따로 담을 temp arr 생성
            ArrayList<String> temp_pdf_files_arr = new ArrayList<>();

            // 루프 돌면서 pdf 파일 찾기
            while(iterator.hasNext()) {
                String key = iterator.next();
                Log.d(TAG, "key: " + key);
                String value = checked_files.get(key);
                Log.d(TAG, "value: " + value);

                // 확장자만 분류
                int Idx = key.lastIndexOf(".");
                String format = key.substring(Idx+1);
                Log.d(TAG, "format: " + format);

                // ArrayList Add
                if(format.equals("pdf")) {
                    temp_pdf_files_arr.add(value);
                }
                // PDF가 아닌 파일들은 files_for_upload 해쉬맵에 추가
                else {
                    files_for_upload.put(key, value);
                    Log.d(TAG, "files_for_upload 해쉬맵에 추가");
                    Log.d(TAG, "file_name_with_format: " + key);
                    Log.d(TAG, "canonicalPath: " + value);
                }
            }

            // pdf 파일(canonicalPath) 리스트, 변환 로직으로 넘기기
            renderFile(temp_pdf_files_arr, context);
        }

        // PDF 파일이 없다면
        else if(contain_padf_file_orNot.equals("false")) {
            // 루프 돌면서 files_for_upload 해쉬맵으로 데이터 복사
            // checked_files Iterator
            Iterator<String> iterator = checked_files.keySet().iterator();
            // 루프 돌면서 pdf 파일 찾기
            while(iterator.hasNext()) {
                String key = iterator.next();
                String value = checked_files.get(key);

                files_for_upload.put(key, value);
                Log.d(TAG, "files_for_upload 해쉬맵에 추가");
                Log.d(TAG, "file_name_with_format: " + key);
                Log.d(TAG, "canonicalPath: " + value);
            }
            // todo: 이미지 업로드 로직을 호출해야할 곳 - 레트로핏
            Call_F.comment.setText("Images, on uploading");
            upload_multi_files_1(context);
        }
    }


    /**---------------------------------------------------------------------------
     메소드 ==> pdfbox 라이브러리 사용을 위한 Initializes
     ---------------------------------------------------------------------------*/
    private void setup() {
        // Enable Android-style asset loading (highly recommended)
        PDFBoxResourceLoader.init(getApplicationContext());
        // Find the root of the external storage.
        root = android.os.Environment.getExternalStorageDirectory();
    }


    /**---------------------------------------------------------------------------
     메소드 ==> Pdf file TO image file 1.
     ---------------------------------------------------------------------------*/
    @SuppressLint({"SetTextI18n", "HandlerLeak"})
    public void renderFile(final ArrayList<String> arr, Context context) {

        // 1개의 PDF 파일의 변환이 완료됐을 때 콜백 받을 핸들러 생성
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                /** 파일 변환 - 시작, 호출 콜백*/
                if(msg.what == 5) {
                    // 넘어온 PDF 파일의 개수 구하기
                    int pdf_files_count = arr.size();
                    Log.d(TAG, "pdf_files_count: " + pdf_files_count);

                    if(pdf_files_count > 0) {
                        // 파일 이름 추출
                        String[] temp = arr.get(0).split("[/]");
                        String fileName = temp[temp.length-1];

                        // otto 를 통해, 프래그먼트로 이벤트 전달하기
                        Event.Myapp__Call_F myapp__call_f = new Event.Myapp__Call_F("progress", "start",
                                pdf_files_count, 1, fileName, 0, 0, 0);
                        BusProvider.getBus().post(myapp__call_f);
                    }
                }

                /** 페이지 전환율 - 한 페이지 변환 시작, 호출 콜백 */
                else if(msg.what == 4) {
                    int pdf_files_count = arr.size();
                    Log.d(TAG, "pdf_files_count: " + pdf_files_count);

                    if(pdf_files_count > 0) {
                        int total = msg.getData().getInt("total", 0);
                        int current_pdf_page = msg.getData().getInt("current_pdf_page", 0);

                        // otto 를 통해, 프래그먼트로 이벤트 전달하기
                        Event.Myapp__Call_F myapp__call_f = new Event.Myapp__Call_F("progress", "progress",
                                -1, 1, "", 0, total, current_pdf_page);
                        BusProvider.getBus().post(myapp__call_f);
                    }
                }

                /** 페이지 전환율 - 한 페이지 변환 완료, 호출 콜백 */
                else if(msg.what == 2) {
                    int total = msg.getData().getInt("total", 0);
                    int current_pdf_page = msg.getData().getInt("current_pdf_page", 0);
                    int percent = msg.getData().getInt("percent", 0);

                    // otto 를 통해, 프래그먼트로 이벤트 전달하기
                    Event.Myapp__Call_F myapp__call_f = new Event.Myapp__Call_F("progress", "ing",
                            -1, -1, "", percent, total, current_pdf_page);
                    BusProvider.getBus().post(myapp__call_f);
                }

                /** 페이지 전환율 - 100%, 호출 콜백 */
                else if(msg.what == 3) {
                    int total = msg.getData().getInt("total", 0);

                    // otto 를 통해, 프래그먼트로 이벤트 전달하기
                    Event.Myapp__Call_F myapp__call_f = new Event.Myapp__Call_F("progress", "ing",
                            -1, -1, "", 100, total, total);
                    BusProvider.getBus().post(myapp__call_f);
                }

                /** 파일 변환 - 파일 한개 변환 완료 했을때 호출하는 콜백 +
                 *  파일 변환 - 파일 모두 변환 완료 했을때 호출하는 콜백 */
                else if(msg.what == 0) {
                    Log.d(TAG, "제거 전 arr.size(): " + arr.size());
                    arr.remove(0);
                    Log.d(TAG, "제거 후 arr.size(): " + arr.size());

                    // arr 에 pdf파일 리스트가 모두 없어질때까지 무한루프
                    if(arr.size() > 0) {
                        over_and_over_convert(arr.get(0));
                    }
                    else if(arr.size() == 0) {
//                        logAndToast("PDF 파일 변환이 완료되었습니다");
                        // otto 를 통해, 프래그먼트로 이벤트 전달하기
                        Event.Myapp__Call_F myapp__call_f = new Event.Myapp__Call_F("progress", "end",
                                PDF_converting_exception_file_count, -1, "", -1, 0, 0);
                        BusProvider.getBus().post(myapp__call_f);
                        // otto 등록 해제
                        BusProvider.getBus().unregister(this);
                        // exception 파일 개수 초기화
                        setPDF_converting_exception_file_count(0);
                    }
                }
                /** 파일 변환 - 파일 변환중 Exception 에러가 발생했을 때 호출하는 콜백 +
                 *  파일 변환 - 파일 모두 변환 완료 했을때 호출하는 콜백 */
                else if(msg.what == 1) {
                    // exception 파일 개수 추가
                    PDF_converting_exception_file_count++;

                    String[] temp = arr.get(0).split("[/]");
                    String fileName = temp[temp.length-1];
                    int Idx = fileName.lastIndexOf(".");
                    final String only_fileName = fileName.substring(0, Idx);
                    logAndToast(only_fileName + " 파일이 변환중 에러로 업로드에서 제외됩니다.");

                    Log.d(TAG, "제거 전 arr.size(): " + arr.size());
                    arr.remove(0);
                    Log.d(TAG, "제거 후 arr.size(): " + arr.size());

                    // 토스트 뜰 시간 벌기 위해 0.5초 뒤에 실행
                    new Handler().postDelayed(new Runnable() {
                        @Override public void run() {
                            // arr 에 pdf파일 리스트가 모두 없어질때까지 무한루프
                            if(arr.size() > 0) {
                                over_and_over_convert(arr.get(0));
                            }
                            else if(arr.size() == 0) {
                                // exception 파일 개수 초기화
//                                logAndToast("PDF 파일 변환이 완료되었습니다");
                                // otto 를 통해, 프래그먼트로 이벤트 전달하기
                                Event.Myapp__Call_F myapp__call_f = new Event.Myapp__Call_F("progress", "end",
                                        PDF_converting_exception_file_count, -1, "", -1, 0, 0);
                                BusProvider.getBus().post(myapp__call_f);
                                // otto 등록 해제
                                BusProvider.getBus().unregister(this);
                                // exception 파일 개수 초기화
                                setPDF_converting_exception_file_count(0);
                            }
                        }
                    }, 500);
                }
            }
        };

        // otto 등록
        BusProvider.getBus().register(this);

        /** renderFile 최초 호출될 때 pdf 첫번째 파일 변환 실행 */
        over_and_over_convert(arr.get(0));
    }


    /**---------------------------------------------------------------------------
     메소드 ==> Pdf file TO image file 2.
     ---------------------------------------------------------------------------*/
    public void over_and_over_convert(final String canonicalPath) {
        try {
            File target_file = new File(canonicalPath);
            Log.d(TAG, "target_file_canonicalPath: " + canonicalPath);

            // Load in an already created PDF
            final PDDocument document = PDDocument.load(target_file);
            // Create a renderer for the document
            final PDFRenderer renderer = new PDFRenderer(document);

            // 확장자를 제외한 파일 이름
            String pdf_fileName = target_file.getName();
            int Idx = pdf_fileName.lastIndexOf(".");
            final String only_fileName = pdf_fileName.substring(0, Idx);
            Log.d(TAG, "only_fileName:" + only_fileName);

            // PDF 파일 총 페이지 수
            int total_pages_num = document.getNumberOfPages();
            Log.d(TAG, "PDF 파일 총 페이지 수: " + total_pages_num);

            new Thread() {
                @Override
                public void run() {
                    super.run();

                    try {
                        for(int i=1; i<=document.getNumberOfPages(); i++) {

                            /** 파일 변환 - 시작 시에, 핸들러를 통해 변환 완료 알림 */
                            if(i == 1) {
                                Log.d(TAG, "========================================");
                                Log.d(TAG, only_fileName + ".pdf: 이미지 변환 시작");
                                handler.sendEmptyMessage(5);
                            }

                            /** 페이지 전환율 - 진척도, 핸들러를 통해 알림 */
                            int value_1 = i;
                            int total_1 = document.getNumberOfPages();

                            Message msg_1 = new Message();
                            Bundle data_1 = new Bundle();
                            data_1.putInt("current_pdf_page", value_1);
                            data_1.putInt("total", total_1);
                            msg_1.what = 4;

                            msg_1.setData(data_1);
                            handler.sendMessage(msg_1);

                            // =================================================================
                            // ================== 한개의 페이지 이미지 변환 시작 ==================
                            // Render the image to an RGB Bitmap
                            Bitmap pageImage = renderer.renderImage(i-1, 1, Bitmap.Config.RGB_565);
                            // Save the render result to an image
                            String created_fileName = String.valueOf(i) + "_" + only_fileName + ".png";
                            String path = sdPath + "/" + created_fileName;
                            File renderFile = new File(path);
                            FileOutputStream fileOut = new FileOutputStream(renderFile);
                            pageImage.compress(Bitmap.CompressFormat.PNG, 100, fileOut);
                            fileOut.close();
                            Log.d(TAG, created_fileName + " 변환");
                            // ================== 한개의 페이지 이미지 변환 종료 ==================
                            // =================================================================
                            /** files_for_upload 해쉬맵에 추가 */
                            files_for_upload.put(created_fileName, path);
                            Log.d(TAG, "files_for_upload 해쉬맵에 추가");
                            Log.d(TAG, "file_name_with_format: " + created_fileName);
                            Log.d(TAG, "canonicalPath: " + path);

                            /** 페이지 전환율 - 하나 변환 완료됐을 때, 핸들러를 통해 알림 */
                            if(i != document.getNumberOfPages()) {

                                // 파일 변환 percent 구하기
                                int value_2 = i;
                                int total_2 = document.getNumberOfPages();
                                int rate = (int)((double)((double)value_2/(double)total_2) * 100);
                                Log.d(TAG, "PDF 파일 변환 진척도: " + rate + "%");

                                Message msg_2 = new Message();
                                Bundle data_2 = new Bundle();
                                data_2.putInt("current_pdf_page", value_2);
                                data_2.putInt("total", total_2);
                                data_2.putInt("percent", rate);
                                msg_2.what = 2;

                                msg_2.setData(data_2);
                                handler.sendMessage(msg_2);
                            }

                            /** 페이지 전환율 - 100% 된 것도 핸들러롤 통해 변환 완료 알림 */
                            if(i == document.getNumberOfPages()) {
                                int value_3 = i;
                                Message msg_3 = new Message();
                                Bundle data_3 = new Bundle();
                                data_3.putInt("total", value_3);
                                msg_3.what = 3;
                                msg_3.setData(data_3);
                                handler.sendMessage(msg_3);
                            }

                            /** 파일 변환 - 완료 시에, 핸들러를 통해 변환 완료 알림 */
                            if(i == document.getNumberOfPages()) {
                                handler.sendEmptyMessage(0);
                                Log.d(TAG, only_fileName + ".pdf: 이미지로 변환 완료");
                                document.close();
                            }
                        }
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                        Log.d(TAG, "Pdf 변환 에러!!!!!!!!!!! - " + e.getMessage());
                        handler.sendEmptyMessage(1);
                    }
                }
            }.start();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
