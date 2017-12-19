package com.example.jyn.remotemeeting.Util;

import javax.xml.transform.Result;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

/**
 * Created by JYN on 2017-12-01.
 */

public interface RetrofitService {

    /** 이메일 회원가입 */
    @FormUrlEncoded
    @POST("/{url}")
    Call<ResponseBody> email_join (
            @Path("url") String url,
            @Field(value = "join_info_json", encoded=true) String join_info_json
    );


    /** 이메일 로그인 */
    @FormUrlEncoded
    @POST("/{url}")
    Call<ResponseBody> email_login (
            @Path("url") String url,
            @Field(value = "login_info_json", encoded=true) String login_info_json
    );


    /** 내 파트너 리스트 가져오기 */
    @FormUrlEncoded
    @POST("/{url}")
    Call<ResponseBody> get_partner_list (
            @Path("url") String url,
            @Field(value = "user_no", encoded=true) String user_no
    );


    /** 1:1 채팅방 개설하기 */
    @FormUrlEncoded
    @POST("/{url}")
    Call<ResponseBody> create_chat_room_for_one (
            @Path("url") String url,
            @Field(value = "user_no", encoded=true) String user_no,
            @Field(value = "target_user_no", encoded=true) String target_user_no
    );


    /** 내 채팅방 리스트 가져오기 */
    @FormUrlEncoded
    @POST("/{url}")
    Call<ResponseBody> get_chat_room_list (
            @Path("url") String url,
            @Field(value = "user_no", encoded=true) String user_no
    );


    /** 특정 한개의 채팅방 정보 가져오기 */
    @FormUrlEncoded
    @POST("/{url}")
    Call<ResponseBody> get_new_chatroom_info (
            @Path("url") String url,
            @Field(value = "user_no", encoded=true) String user_no,
            @Field(value = "target_chatroom_no", encoded=true) int target_chatroom_no
    );


    /** 해당 채팅방에서 내가 읽지 않은 메세지 개수 가져오기 */
    @FormUrlEncoded
    @POST("/{url}")
    Call<ResponseBody> get_unread_msg_count (
            @Path("url") String url,
            @Field(value = "user_no", encoded=true) String user_no,
            @Field(value = "chat_room_no", encoded=true) int chat_room_no
    );


    /** 채팅방 로그 가져오기 */
    @FormUrlEncoded
    @POST("/{url}")
    Call<ResponseBody> get_chatting_logs (
            @Path("url") String url,
            @Field(value = "user_no", encoded=true) String user_no,
            @Field(value = "chat_room_no", encoded=true) String chat_room_no
    );


    /** 해당 채팅방에서 처음으로 받은 msg_no + 마지막으로 받은 msg_no 업데이트 */
    @FormUrlEncoded
    @POST("/{url}")
    Call<ResponseBody> update_first_last_msg_no (
            @Path("url") String url,
            @Field(value = "user_no", encoded=true) String user_no,
            @Field(value = "chat_room_no", encoded=true) int chat_room_no,
            @Field(value = "first_read_msg_no", encoded=true) int first_read_msg_no,
            @Field(value = "last_read_msg_no", encoded=true) int last_read_msg_no
    );


    /** 파트너 검색결과 가져오기 */
    @FormUrlEncoded
    @POST("/{url}")
    Call<ResponseBody> search_partners (
            @Path("url") String url,
            @Field(value = "user_no", encoded=true) String user_no,
            @Field(value = "search_word_with_domain", encoded=true) String search_word_with_domain,
            @Field(value = "search_word_without_domain", encoded=true) String search_word_without_domain
    );


    /** 파트너 끊기 */
    @FormUrlEncoded
    @POST("/{url}")
    Call<ResponseBody> break_partner (
            @Path("url") String url,
            @Field(value = "user_no", encoded=true) String user_no,
            @Field(value = "target_user_no", encoded=true) String target_user_no
    );


    /** 파트너 맺기 */
    @FormUrlEncoded
    @POST("/{url}")
    Call<ResponseBody> become_a_partner (
            @Path("url") String url,
            @Field(value = "user_no", encoded=true) String user_no,
            @Field(value = "target_user_no", encoded=true) String target_user_no
    );


    /** 유저 한명의 정보 가져오기 */
    @FormUrlEncoded
    @POST("/{url}")
    Call<ResponseBody> get_user_info (
            @Path("url") String url,
            @Field(value = "target_user_no", encoded=true) String target_user_no
    );


    /** 이미지 파일 업로드 */
    @Multipart
    @POST("/{url}")
    Call<ResponseBody> upload_profile_img(
            @Path("url") String url,
            @Part("user_no") RequestBody user_no,
            @Part MultipartBody.Part file
    );


    /** 회의, 멀티 파일 업로드 */
    @Multipart
    @POST("/{url}")
    Call<ResponseBody> upload_multi_files(
            @Path("url") String url,
            @Part("user_no") RequestBody user_no,
            @Part("meeting_no") RequestBody meeting_no,
            @Part MultipartBody.Part file
    );


    /** 닉네임 변경 */
    @FormUrlEncoded
    @POST("/{url}")
    Call<ResponseBody> update_nickname (
            @Path("url") String url,
            @Field(value = "user_no", encoded=true) String user_no,
            @Field(value = "user_nickname", encoded=true) String user_nickname
    );


    /** 프로필 이미지 디폴트 이미지로 변경 */
    @FormUrlEncoded
    @POST("/{url}")
    Call<ResponseBody> set_default_img (
            @Path("url") String url,
            @Field(value = "user_no", encoded=true) String user_no
    );


    /** 방 생성 하기 */
    @FormUrlEncoded
    @POST("/{url}")
    Call<ResponseBody> create_meeting_room (
            @Path("url") String url,
            @Field(value = "real_meeting_title", encoded=true) String real_meeting_title,
            @Field(value = "transform_meeting_title", encoded=true) String transform_meeting_title,
            @Field(value = "user_no", encoded=true) String user_no,
            @Field(value = "subject_user_no", encoded=true) String subject_user_no
    );


    /** 회의 종료했을 때, 내 회의 상태 + 미팅상태 변경하기 */
    @FormUrlEncoded
    @POST("/{url}")
    Call<ResponseBody> got_out_from_meeting (
            @Path("url") String url,
            @Field(value = "user_no", encoded=true) String user_no,
            @Field(value = "closed_meeting_room_no", encoded=true) String closed_meeting_room_no
    );


    /** 내가 초대된 회의가 있는지 없는지 확인 */
    @FormUrlEncoded
    @POST("/{url}")
    Call<ResponseBody> am_i_invited (
            @Path("url") String url,
            @Field(value = "user_no", encoded=true) String user_no
    );


    /** 해당 회의 번호에 업로드된 파일 리스트 가져오기 */
    @FormUrlEncoded
    @POST("/{url}")
    Call<ResponseBody> get_share_file_list (
            @Path("url") String url,
            @Field(value = "meeting_no", encoded=true) String meeting_no
    );

}
