package com.example.jyn.remotemeeting.Netty;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.example.jyn.remotemeeting.Activity.Chat_A;
import com.example.jyn.remotemeeting.DataClass.Data_for_netty;
import com.example.jyn.remotemeeting.Otto.BusProvider;
import com.example.jyn.remotemeeting.Otto.Event;
import com.example.jyn.remotemeeting.Util.Myapp;
import com.google.gson.Gson;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Created by JYN on 2017-12-14.
 */

public class Chat_handler extends ChannelInboundHandlerAdapter {

    private static final String TAG = "all_"+Chat_handler.class.getSimpleName();
    private Myapp myapp;


    /**---------------------------------------------------------------------------
     생성자
     ---------------------------------------------------------------------------*/
    @SuppressLint("HandlerLeak")
    public Chat_handler() {
        myapp = Myapp.getInstance();

    }


    /**---------------------------------------------------------------------------
     콜백메소드 ==> channelActive -- 채널이 등록(register)된 이후 호출되는 콜백
     ---------------------------------------------------------------------------*/
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // Data_for_netty 객체 만들어서, 서버로 통신메세지 보내기
        // 서버 접속 메시지 보내기
        Data_for_netty data = new Data_for_netty
                .Builder("conn", "none", myapp.getUser_no())
                .build();
        // 통신 전송 메소드 호출
        myapp.send_to_server(data);

    }


    /**---------------------------------------------------------------------------
     메소드 ==> otto -- to_Char_F
                    : Data_for_netty 객체와 함께 채팅방 리사이클러뷰에 대한 변경점 이벤트 메시지를 전달
     ---------------------------------------------------------------------------*/
    public void to_Char_F(final String order, final Data_for_netty data) {

        // otto 등록
        BusProvider.getBus().register(this);

        Event.Chat_handler__Chat_F event
                = new Event.Chat_handler__Chat_F(order, data);
        BusProvider.getBus().post(event);
        Log.d(TAG, "otto 전달_ to_Char_F");

        // otto 해제
        BusProvider.getBus().unregister(this);
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
                // 핸들러로 Message 객체 전달6
                Chat_A.chat_message_handler.sendMessage(msg);

            }
        }.start();
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
        String message = (String)obj;
        Log.d(TAG, "[서버]: " + message);

        // 받은 통신 메세지 Data_for_netty 객체화
        Gson gson = new Gson();
        Data_for_netty data = gson.fromJson(message, Data_for_netty.class);
        Log.d(TAG, "data.getType(): " + data.getType());
        Log.d(TAG, "data.getSubType(): " + data.getSubType());

        // 현재 액티비티 확인해보기
        String current_activity_with_package_name = myapp.getTop_activity();
        Log.d(TAG, "current_activity_with_package_name: " + current_activity_with_package_name);
        String[] temp = current_activity_with_package_name.split("[.]");
        String curr_activity_name = temp[temp.length-1];
        Log.d(TAG, "curr_activity_name: " + curr_activity_name);

        /** 통신 메세지 구분 */
        switch (data.getSubType()) {
            // 서버에서 중계한 다른 유저가 보낸 채팅 메시지를 받았을 때
            case "relay_msg":
                // 현재의 액티비티가 'Chat_A'(채팅 화면)라면,
                if(curr_activity_name.equals("Chat_A")) {
                    to_Chat_A("new", data);
                }
                // 현재의 액티비티가 'Chat_A'(채팅 화면)가 아닌 다른 액티비티라면,
                else if(!curr_activity_name.equals("Chat_A")) {
                    to_Char_F("update", data);
                }

                break;
            // 내가 보낸 채팅 메시지가 서버에 잘 도착했다는 콜백(응답) 메시지를 받았을 때
            case "call_back":
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
}
