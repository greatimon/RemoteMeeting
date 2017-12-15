package com.example.jyn.remotemeeting.Netty;

import android.util.Log;

import com.example.jyn.remotemeeting.Etc.Static;
import com.example.jyn.remotemeeting.Util.Myapp;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Created by JYN on 2017-12-14.
 */

public class Chat_handler extends ChannelInboundHandlerAdapter {

    private static final String TAG = "all_"+Chat_handler.class.getSimpleName();
    Myapp myapp;

    public Chat_handler() {
        myapp = Myapp.getInstance();
    }

    // 채널이 등록(register)된 이후 호출되는 콜백
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        String sendMessage = Chat_service.user_no + Static.SPLIT + myapp.getChat_room_no();
//        Log.d(TAG, "채팅 서버 접속 시, 전달 메시지: " + sendMessage);
//
//        ctx.writeAndFlush(sendMessage);

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object obj) throws Exception {
        String readMessage = (String)obj;
        Log.d(TAG, "#채팅채팅#" + readMessage);
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
