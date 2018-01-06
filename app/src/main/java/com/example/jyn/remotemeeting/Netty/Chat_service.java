package com.example.jyn.remotemeeting.Netty;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.jyn.remotemeeting.Util.Myapp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * Created by JYN on 2017-12-14.
 */

public class Chat_service extends Service implements Runnable  {

    private static final String TAG = "all_" + Chat_service.class.getSimpleName() + "#채팅채팅#";
    Thread thread;
    Myapp myapp;
    public static String user_no;

    /** 싱글턴 객체 반환 */
    private static Chat_service instance;
    public static Chat_service getInstance() {
        return instance;
    }


    /**---------------------------------------------------------------------------
     생명주기 ==> onCreate -- Service 에서 가장 먼저 호출되며, startService() 호출 때 최초 1회만 호출된다
     ---------------------------------------------------------------------------*/
    @Override
    public void onCreate() {
        Log.d(TAG, "===onCreate()===");
        super.onCreate();

        // 어플리케이션 인스턴스 받아오기
        myapp = Myapp.getInstance();

        // 채팅 서버 연결, 스레드 구동
        thread = new Thread(this);
        thread.start();
    }


    /**---------------------------------------------------------------------------
     생명주기 ==> onStartCommand
     ---------------------------------------------------------------------------*/
    // process가 죽게되면 onStartCommand 리턴 값이 sticky 일 경우, service는 자체적으로 다시 돌기 시작하는데
    // 그때마다 onCreate가 아닌 onStartCommand 가 불려진다
    // 그래서 중요한 작업은 주로 여기서 정의되고, onCreate()는 보통 초기 셋팅값이나 코드들이 들어간다
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "===onStartCommand()===");
        instance = this;

        user_no = intent.getStringExtra("user_no");
        Log.d(TAG, "가동하는 서비스의 user_no: " + user_no);

        return START_REDELIVER_INTENT;    // process가 죽을 때, service를 재시작하기 위한 리턴값 설정
//        return START_NOT_STICKY;
//        return super.onStartCommand(intent, flags, startId);
    }

    /** Runnable 에 따른 콜백 메소드 - 구현할 코드 들어가는 곳 */
    @Override
    public void run() {
        Log.d(TAG, "===run()===");

        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootStrap = new Bootstrap();
            bootStrap.group(group)
                    // 논블럭 방식 적용
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            // String 인/디코더 (default인 UTF-8)
                            pipeline.addLast(new StringEncoder(), new StringDecoder());
                            pipeline.addLast(new GatheringHandler());
                            // IO 이벤트 핸들러
                            pipeline.addLast(new Netty_handler(user_no));
                        }
                    });

            Channel channel = bootStrap.connect("52.78.88.227", 8888).sync().channel();

            // 어플리케이션 객체에 Channel 객체 저장하기
            myapp.setChannel(channel);

            channel.closeFuture().sync();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }


    /**---------------------------------------------------------------------------
     생명주기 ==> onDestroy
     ---------------------------------------------------------------------------*/
    @Override
    public void onDestroy() {
        Log.d(TAG, "===onDestroy()===");
        super.onDestroy();
    }


    /**---------------------------------------------------------------------------
     생명주기 ==> onBind -- bindStart() 로 서비스를 시작할 때, 콜백되는 메소드
     ---------------------------------------------------------------------------*/
    // 보통 액티비티나 여타 다른 컴포넌트들간 data를 주고 받을 때 사용된다
    // 디폴트 값: null
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "===onBind()===");
        return null;
    }


    /**---------------------------------------------------------------------------
     getter ==> user_no -- 서비스가 실행될 때 intent로 받는 값으로,
        추후에 로그인 할 때, 구동중인 서비스의 user_no 값과
        로그인한 user_no 값을 비교한다.
        만약에 user_no이 다를 경우(즉, 다른 아이디로 로그인 했을 때),
        로그인한 아이디로 서비스를 재구동(즉, 채팅 서버에 재접속)한다
     ---------------------------------------------------------------------------*/
    public String getUser_no() {
        return user_no;
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 서비스 중지시키기
     ---------------------------------------------------------------------------*/
    public void stop_this_service() {
        Log.d(TAG, "현재 서비스 구동 중지");
        Log.d(TAG, "중지하는 서비스의 user_no: " + user_no);
        stopSelf();
    }
}
