package com.example.jyn.remotemeeting.Util;

import android.util.Log;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Created by JYN on 2017-12-21.
 */

public class GatheringHandler extends ChannelInboundHandlerAdapter {

    private String temp = "";
    private int count = 0;
    private static final String TAG = "all_"+GatheringHandler.class.getSimpleName();

    /**---------------------------------------------------------------------------
     콜백메소드 ==> channelRead -- 서버로부터 메세지가 왔을 때 호출되는 콜백
     ---------------------------------------------------------------------------*/
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object obj) throws InterruptedException {
        count++;
        // 서버가 보낸 통신메세지 확인하기
        String temp_2 = (String) obj;
        String message = temp_2.replace(" ", "");
        Log.d(TAG, "==================== GatheringHandler_ message[" + count + "] -" + message + " ====================");

        temp = temp + message;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelRead(temp);
        temp = "";
        count = 0;
    }

}
