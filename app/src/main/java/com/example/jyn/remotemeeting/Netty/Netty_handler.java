package com.example.jyn.remotemeeting.Netty;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.example.jyn.remotemeeting.Activity.Call_A;
import com.example.jyn.remotemeeting.Activity.Chat_A;
import com.example.jyn.remotemeeting.Activity.Main_after_login_A;
import com.example.jyn.remotemeeting.Adapter.RCV_selectFile_preview_adapter;
import com.example.jyn.remotemeeting.DataClass.Data_for_netty;
import com.example.jyn.remotemeeting.Util.Myapp;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import java.io.StringReader;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Created by JYN on 2017-12-14.
 */

public class Netty_handler extends ChannelInboundHandlerAdapter {

    private static final String TAG = "all_"+Netty_handler.class.getSimpleName();
    private Myapp myapp;
    public String user_no;

    /**---------------------------------------------------------------------------
     생성자
     ---------------------------------------------------------------------------
     * @param user_no*/
    @SuppressLint("HandlerLeak")
    public Netty_handler(String user_no) {
        myapp = Myapp.getInstance();
        this.user_no = user_no;

        // 커스텀 로거 생성
        Logger.clearLogAdapters();
        Logger.addLogAdapter(new AndroidLogAdapter(myapp.custom_log(RCV_selectFile_preview_adapter.class)));

    }


    /**---------------------------------------------------------------------------
     콜백메소드 ==> channelActive -- 채널이 등록(register)된 이후 호출되는 콜백
     ---------------------------------------------------------------------------*/
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // Data_for_netty 객체 만들어서, 서버로 통신메세지 보내기
        // 서버 접속 메시지 보내기
        Data_for_netty data = new Data_for_netty();
        data.setNetty_type("conn");
        data.setSubType("none");
        data.setSender_user_no(user_no);
        // 통신 전송 메소드 호출
        myapp.send_to_server(data);

    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }


    /**---------------------------------------------------------------------------
     콜백메소드 ==> channelRead -- 서버로부터 오는 메세지가 올 때 호출되는 콜백
     ---------------------------------------------------------------------------*/
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object obj) throws Exception {

        // 서버가 보낸 통신메세지 확인하기
        String temp_1 = (String)obj;
//        String message = temp_1.replace(" ", "");
//        Log.d(TAG, "[서버]: " + message);

        // 받은 통신 메세지 Data_for_netty 객체화
        Gson gson = new GsonBuilder().setLenient().create();

        JsonReader reader = new JsonReader(new StringReader(temp_1));
        reader.setLenient(true);

        final Data_for_netty data = gson.fromJson(reader, Data_for_netty.class);
        Log.d(TAG, "data.getType(): " + data.getNetty_type());
        Log.d(TAG, "data.getSubType(): " + data.getSubType());
        Log.d(TAG, "data.getAttachment(): " + data.getAttachment());
        Log.d(TAG, "reader: " + reader.toString());

        // 현재 액티비티 확인해보기
//        String current_activity_with_package_name = myapp.getTop_activity();
//        Log.d(TAG, "current_activity_with_package_name: " + current_activity_with_package_name);
//        String[] temp = current_activity_with_package_name.split("[.]");
//        String curr_activity_name = temp[temp.length-1];
        String curr_activity_name = myapp.getTop_activity();

        Log.d(TAG, "curr_activity_name: " + curr_activity_name);
        Log.d(TAG, "Main_after_login_A.chat_F_onResume: " + Main_after_login_A.chat_F_onResume);

        /** 통신 메세지 구분 */
        switch (data.getSubType()) {
            // 서버에서 중계한 다른 유저가 보낸 채팅 메시지를 받았을 때
            case "relay_msg":
                // 현재의 액티비티가 'Chat_A'(채팅 화면)라면,
                if(curr_activity_name.equals("Chat_A")) {
                    Log.d(TAG, "Chat_A_ 화면에서 relay_msg 받음!!!");
                    new Thread() {
                        @Override
                        public void run() {
                            to_Chat_A("new", data);

                            // 현재 있는 채팅방이, 서버로부터 받은 relay_msg의 채팅방과 일치한다면,
                            if(myapp.getChatroom_no() == data.getChat_log().getChat_room_no()) {
                                // 해당 채팅방에서 내가 서버로부터 받은 'first / last' msg_no를 서버 DB에 업데이트 하는, 메소드 호출
                                // 인자 1. 채팅방 번호
                                // 인자 2. 서버로 부터 받은 채팅 로그 중 첫번째 메세지 번호
                                // 인자 3. 서버로 부터 받은 채팅 로그 중 마지막 메세지 번호
                                // 인자 4. 요청구분자
                                myapp.update_first_last_msg_no(
                                        data.getChat_log().getChat_room_no(),
                                        data.getChat_log().getMsg_no(),
                                        data.getChat_log().getMsg_no(),
                                        "netty");
                            }
                        }
                    }.start();
                }
                // 현재의 액티비티가 'Main_after_login_A' 이고, onResume 상태라면
                else if(curr_activity_name.equals("Main_after_login_A") &&
                        Main_after_login_A.chat_F_onResume) {
                    Log.d(TAG, "Main_after_login_A_ 화면에서 relay_msg 받음!!!");
                    new Thread() {
                        @Override
                        public void run() {
                            to_Char_F("update", data);
                        }
                    }.start();
                }

                break;

            // netty 접속 내가 보낸 채팅 메시지가 서버에 잘 도착했다는 콜백(응답) 메시지를 받았을 때
            case "call_back":
                // 내가 보낸 채팅 메세지에 대한 콜백 이라면,
                if(data.getNetty_type().equals("msg") && data.getSender_user_no().equals(myapp.getUser_no())) {
                    Log.d(TAG, "call_back 받음!!!!!!!!!!!!!");
                    new Thread() {
                        @Override
                        public void run() {
                            to_Chat_A("my_chat_msg_call_back", data);

                            // 해당 채팅방에서 내가 서버로부터 받은 'first / last' msg_no를 서버 DB에 업데이트 하는, 메소드 호출
                            // 인자 1. 채팅방 번호
                            // 인자 2. 서버로 부터 받은 채팅 로그 중 첫번째 메세지 번호
                            // 인자 3. 서버로 부터 받은 채팅 로그 중 마지막 메세지 번호
                            // 인자 4. 요청구분자
                            myapp.update_first_last_msg_no(
                                    data.getChat_log().getChat_room_no(),
                                    data.getChat_log().getMsg_no(),
                                    data.getChat_log().getMsg_no(),
                                    "netty");
                        }
                    }.start();
                }

                // 채팅 로그를 하라는 나의 요청에, 상대방에 채팅 로그를 업데이트 하라는 통신 메세지를 받았다는 콜백
                if(data.getNetty_type().equals("update_chat_log_complete")) {
                    Log.d(TAG, "Netty 서버로 부터 `update_chat_log_complete` 통신 메세지 전달 받음!~");
                    // 채팅 번호 확인
                    Log.d(TAG, "chatRoom_no: " + data.getExtra());
                    // 업데이트한 first 메세지 번호 확인
                    Log.d(TAG, "getFirst_read_msg_no: " + data.getFirst_read_msg_no());
                    // 업데이트한 last 메세지 번호 확인
                    Log.d(TAG, "getLast_read_msg_no: " + data.getLast_read_msg_no());

                    // 현재의 액티비티가 'Chat_A'(채팅 화면)라면,
                    if(curr_activity_name.equals("Chat_A")) {
                        // 내가 들어와 있는 채팅방이, 상대방이 업데이트한 로그의 채팅방 번호와 일치 한다면
                        if(myapp.getChatroom_no() == Integer.parseInt(data.getExtra())) {
                            to_Chat_A("update_chat_log_complete_callBack", data);
                        }
                    }


                }
                break;

            // 채팅 로그를 업데이트하라는 요청일 때,
            case "update_chat_log":
                if(data.getNetty_type().equals("request")) {
                    Log.d(TAG, "update_chat_log 받음!!!!!!!!!!!!!");
                    // 내가 지금 현재 채팅방에 들어와 있다면,
                    // (이 메세지를 보내기 전에 netty 서버에서 내가 이 채팅방에 들어와있는지 안들어와 있는지 확인하지만, 재확인)
                    // 해당 msg_no의 msg_unread_count 받아와서, 해당 채팅 리사이클러뷰 item을 갱신한다
                    if(data.getExtra().equals(String.valueOf(myapp.getChatroom_no()))) {
//                        String unread_msg_count_info_jsonString = data.getUnread_msg_count_info_jsonString();
//                        Log.d(TAG, "unread_msg_count_info_jsonString: " + unread_msg_count_info_jsonString);
                        new Thread() {
                            @Override
                            public void run() {
                                to_Chat_A("update_chat_log", data);

                                // 업데이트를 하라는 요청 잘 받고 난뒤, 다시 서버에 잘 받았다고 콜백 보내기
                                // 발신자에게 잘 받았다고 알려줘서, 발신자의 안 읽음 메세지 카운트를
                                // 다시 업데이트 하기 위함
                                data.setNetty_type("update_chat_log_complete");
                                myapp.send_to_server(data);
                            }
                        }.start();
                    }
                }
                break;
            // 서버에서 중계한, 다른 유저가 보낸 비디오 on/off 상태를 전달 받았을 때,
            case "relay_video_status":
                if(data.getNetty_type().equals("webrtc")) {
                    Log.d(TAG, "relay_video_status 받음~!~!");

                    // Call_A 로 전달받은 데이터 그대로 전달
                    to_Call_A("relay_video_status", data);
                }
                break;
            // 서버에서 중계한, 파일 공유 요청을 전달 받았을 때,
            case "relay_request_image_file_share":
                if(data.getNetty_type().equals("webrtc")) {
                    Logger.d("relay_request_image_file_share, 받음~~~");

                    // Call_A 로 전달받은 데이터 그대로 전달
                    to_Call_A("relay_request_image_file_share", data);
                }
                break;
            // 서버에서 중계한, 파일 공유 요청에 대한 답변을 전달 받았을 때,
            case "relay_answer_image_file_share":
                if(data.getNetty_type().equals("webrtc")) {
                    Logger.d("relay_answer_image_file_share, 받음~~~");

                    // Call_A 로 전달받은 데이터 그대로 전달
                    to_Call_A("relay_answer_image_file_share", data);
                }
                break;
            // 상대방이 이미지 공유 모드를 종료했음을 전달 받았을 때,
            case "relay_end_image_file_share":
                if(data.getNetty_type().equals("webrtc")) {
                    Logger.d("relay_end_image_file_share, 받음~~~");

                    // Call_A 로 전달받은 데이터 그대로 전달
                    to_Call_A("relay_end_image_file_share", data);
                }
                break;
        }
    }


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
//        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        //ctx.close();
    }


    /**---------------------------------------------------------------------------
     메소드 ==> otto -- to_Char_F
     : Data_for_netty 객체와 함께 채팅방 리사이클러뷰에 대한 변경점 이벤트 메시지를 전달
     ---------------------------------------------------------------------------*/
    public void to_Char_F(final String order, final Data_for_netty data) {
        new Thread() {
            @Override
            public void run() {

                // 핸들러로 전달할 Message 객체 생성
                Message msg = Main_after_login_A.chat_room_handler.obtainMessage();

                // Message 객체에 넣을 bundle 객체 생성
                Bundle bundle = new Bundle();
                // bundle 객체에 'order' 변수 담기
                bundle.putString("order", order);
                // Message 객체에 bundle, 'data' 변수 담기
                msg.setData(bundle);
                msg.obj = data;
                // 핸들러에서 Message 객체 구분을 위한 'what' 값 설정
                msg.what = 0;
                // 핸들러로 Message 객체 전달
                Main_after_login_A.chat_room_handler.sendMessage(msg);

            }
        }.start();
    }


    /**---------------------------------------------------------------------------
     메소드 ==> otto -- to_Chat_A
     : 서버로 부터 받은 Data_for_netty 객체(채팅 메세지)를 Chat_A 로 전달
     ---------------------------------------------------------------------------*/
    public void to_Chat_A(final String order, final Data_for_netty data) {
        new Thread() {
            public void run() {

                // 핸들러로 전달할 Message 객체 생성
                Message msg = Chat_A.chat_message_handler.obtainMessage();

                // Message 객체에 넣을 bundle 객체 생성
                Bundle bundle = new Bundle();
                // bundle 객체에 'order' 변수 담기
                bundle.putString("order", order);
                // Message 객체에 bundle, 'data' 변수 담기
                msg.setData(bundle);
                msg.obj = data;
                // 핸들러에서 Message 객체 구분을 위한 'what' 값 설정
                msg.what = 1;
                // 핸들러로 Message 객체 전달
                Chat_A.chat_message_handler.sendMessage(msg);

            }
        }.start();
    }


    /**---------------------------------------------------------------------------
     메소드 ==> otto -- to_Call_A
        1. 서버로 부터 받은 Data_for_netty 객체(비디오 on/off 상태 메세지) 전달
        2. 서버로 부터 받은 Data_for_netty 객체(파일 공유 요청) 전달
     ---------------------------------------------------------------------------*/
    public void to_Call_A(final String order, final Data_for_netty data) {
        new Thread() {
            public void run() {

                // 핸들러로 전달할 Message 객체 생성
                Message msg = Call_A.webrtc_message_handler.obtainMessage();

                // Message 객체에 넣을 bundle 객체 생성
                Bundle bundle = new Bundle();
                // bundle 객체에 'order' 변수 담기
                bundle.putString("order", order);
                // Message 객체에 bundle, 'data' 변수 담기
                msg.setData(bundle);
                msg.obj = data;
                // 핸들러에서 Message 객체 구분을 위한 'what' 값 설정
                msg.what = 1;
                // 핸들러로 Message 객체 전달
                Call_A.webrtc_message_handler.sendMessage(msg);

            }
        }.start();
    }
}