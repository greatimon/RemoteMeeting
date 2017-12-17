package com.example.jyn.remotemeeting.Netty;

import android.util.Log;

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
    Myapp myapp;


    /**---------------------------------------------------------------------------
     생성자
     ---------------------------------------------------------------------------*/
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
    public void to_Char_F(String message, Data_for_netty data) {
        // otto 등록
        BusProvider.getBus().register(this);

        Event.Chat_handler__Char_F event
                = new Event.Chat_handler__Char_F(message, data);
        BusProvider.getBus().post(event);
        Log.d(TAG, "otto 전달_ to_Char_F");

        // otto 해제
        BusProvider.getBus().unregister(this);
    }

    public void to_Chat_A(String message, Data_for_netty data) {
        // otto 등록
        BusProvider.getBus().register(this);

        Event.Chat_handler__Chat_A event
                = new Event.Chat_handler__Chat_A(message, data);
        BusProvider.getBus().post(event);
        Log.d(TAG, "otto 전달_ to_Chat_A");

        // otto 해제
        BusProvider.getBus().unregister(this);
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

        // 받은 통신 메세지 data 객체화
        Gson gson = new Gson();
        Data_for_netty data = gson.fromJson(message, Data_for_netty.class);
        Log.d(TAG, "data.getType(): " + data.getType());
        Log.d(TAG, "data.getSubType(): " + data.getSubType());

        /** 통신 메세지 구분 */
        switch (data.getType()) {
            // 채팅 메세지일 때
            case "msg":
                String chat_room_no = String.valueOf(data.getChat_log().getChat_room_no());

                // 텍스트라면
                if(data.getChat_log().getMsg_type().equals("text")) {
                    Log.d(TAG, "[유저_" + data.getUser_no() + "]: " + data.getChat_log().getMsg_content());
                }

                to_Char_F("update", data);
                to_Chat_A("new", data);

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
