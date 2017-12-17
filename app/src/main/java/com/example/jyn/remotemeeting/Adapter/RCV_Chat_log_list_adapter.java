package com.example.jyn.remotemeeting.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.jyn.remotemeeting.Activity.Chat_A;
import com.example.jyn.remotemeeting.DataClass.Chat_log;
import com.example.jyn.remotemeeting.DataClass.Data_for_netty;
import com.example.jyn.remotemeeting.Otto.BusProvider;
import com.example.jyn.remotemeeting.Otto.Event;
import com.example.jyn.remotemeeting.R;
import com.example.jyn.remotemeeting.Util.Myapp;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by JYN on 2017-12-16.
 */

public class RCV_Chat_log_list_adapter extends RecyclerView.Adapter<RCV_Chat_log_list_adapter.ViewHolder> {

    private Context context;
    private int itemLayout;
    private String request;
    private ArrayList<Chat_log> chat_log;
    public static String TAG = "all_"+RCV_Chat_log_list_adapter.class.getSimpleName();
    public Myapp myapp;

    /** RecyclerAdapter 생성자 */
    public RCV_Chat_log_list_adapter(Context context,
                                     int itemLayout,
                                     ArrayList<Chat_log> chat_log,
                                     String request) {
        Log.d(TAG, "ViewHolder_ RCV_Chat_log_list_adapter: 생성");
        this.context = context;
        this.itemLayout = itemLayout;
        this.chat_log = chat_log;
        this.request = request;

        // 어플리케이션 객체 생성
        myapp = Myapp.getInstance();
    }


    /** 뷰홀더 */
    class ViewHolder extends RecyclerView.ViewHolder {

        /** 버터나이프*/
        @BindView(R.id.not_me_layout)                   LinearLayout not_me_layout;
        @BindView(R.id.me_layout)                       LinearLayout me_layout;
        @BindView(R.id.layout_for_only_msg_content)     LinearLayout layout_for_only_msg_content;
        @BindView(R.id.layout_for_only_msg_content_me)  LinearLayout layout_for_only_msg_content_me;
        @BindView(R.id.sender_profile_img)              ImageView sender_profile_img;
        @BindView(R.id.send_img)                        ImageView send_img;
        @BindView(R.id.send_img_me)                     ImageView send_img_me;
        @BindView(R.id.serial_msg_profile_img)          View serial_msg_profile_img;
        @BindView(R.id.serial_msg_above_content)        View serial_msg_above_content;
        @BindView(R.id.serial_msg_above_content_me)     View serial_msg_above_content_me;
        @BindView(R.id.serial_msg_below_content)        View serial_msg_below_content;
        @BindView(R.id.serial_msg_below_content_me)     View serial_msg_below_content_me;
        @BindView(R.id.nickName)                        TextView nickName;
        @BindView(R.id.msg_content)                     TextView msg_content;
        @BindView(R.id.msg_content_me)                  TextView msg_content_me;
        @BindView(R.id.unread_msg_count)                TextView unread_msg_count;
        @BindView(R.id.unread_msg_count_me)             TextView unread_msg_count_me;
        @BindView(R.id.time)                            TextView time;
        @BindView(R.id.time_me)                         TextView time_me;
        // 일반 findView
        View view;
        public ProgressWheel progress_wheel;

        ViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            Log.d(TAG, "ViewHolder");
            Log.d(TAG, "request: "+ request);
            ButterKnife.bind(this,itemView);
            progress_wheel = view.findViewById(R.id.progress_wheel);

            // progress_wheel 설정
            progress_wheel.setBarColor(Color.parseColor("#4CAF50"));
            progress_wheel.setSpinSpeed(0.8f);
            progress_wheel.setBarWidth(3);
        }
    }

    /** onCreateViewHolder => 뷰홀더 생성 - 인플레이팅 */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder");
        View view = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }


    /** onBindViewHolder => 리스트뷰의 getView 역할 */
    @Override
    public void onBindViewHolder(ViewHolder holder, int pos) {

        // 라사이클러뷰의 포지션이 존재할때만, 리사이클러뷰 아이템들에 data들을 넣어서 보여줌
        if(holder.getAdapterPosition() != RecyclerView.NO_POSITION) {
            int position = holder.getAdapterPosition();
            String msg_content = chat_log.get(position).getMsg_content();
            long transmission_gmt_time = chat_log.get(position).getTransmission_gmt_time();
            String transmission_gmt_time_str = myapp.chat_log_transmission_time(transmission_gmt_time);

            // 내 채팅 메세지 일때, transmission_gmt_time 확인해보기
            // 확인 결과 long type의 default 값인 '0'으로 표시
            if(chat_log.get(position).getUser_no() == Integer.parseInt(myapp.getUser_no())) {
                Log.d(TAG, "transmission_gmt_time:" + transmission_gmt_time);
                Log.d(TAG, "transmission_gmt_time:" + transmission_gmt_time_str);
            }

            // 채팅 로그의 user_no 가져오기
            int msg_user_no = chat_log.get(position).getUser_no();
            // 내 채팅 로그인지 아닌지 확인하는 변수 선언 + 확인하기
            boolean me = false;
            if(msg_user_no == Integer.parseInt(myapp.getUser_no())) {
                me = true;
            }

            // 내 채팅 로그일 때, View Visibility 조절 + 데이터 set
            if(me) {
                holder.not_me_layout.setVisibility(View.GONE);
                holder.me_layout.setVisibility(View.VISIBLE);

                holder.msg_content_me.setText(msg_content);
                holder.time_me.setText(transmission_gmt_time_str);
                // 채팅 로그의 메세지 전송 시간이 '0L' 일 때,
                // 즉, 아직 서버로 부터 메세지 전송 완료 콜백을 받지 못했을 때임
                if(transmission_gmt_time == 0L) {
                    holder.progress_wheel.setVisibility(View.VISIBLE);
                }
            }
            // 내 채팅 로그가 아닐 때, View Visibility 조절 + 데이터 set
            else if(!me) {
                holder.me_layout.setVisibility(View.GONE);
                holder.not_me_layout.setVisibility(View.VISIBLE);

                holder.msg_content.setText(msg_content);
                holder.time.setText(transmission_gmt_time_str);
            }

            // 마지막 채팅 로그까지 뷰에 다 표시 했다면, 리사이클러뷰의 포커스를 제일 마지막 아이템으로 준다
//            if(position == chat_log.size()-1) {
//                layoutManager.scrollToPosition(chat_log.size()-1);
//            }
        }

    }

    /** getItemCount => arr 사이즈 리턴 */
    @Override
    public int getItemCount() {
        return chat_log.size();
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 서버로부터 받아온 내 파트너 리스트로 arrayList 교체하기
     ---------------------------------------------------------------------------*/
    public void refresh_arr(ArrayList<Chat_log> chat_log_arr) {
        this.chat_log.clear();
        this.chat_log = chat_log_arr;
        notifyDataSetChanged();
    }


    /**---------------------------------------------------------------------------
     메소드 ==> Chat_A로 부터 채팅방 메세지 받아서 어레이리스트에 추가 후, 갱신
     ---------------------------------------------------------------------------*/
    public void update_last_msg(String message, Data_for_netty data) {
        // 채팅방 리스트를 업데이트 하라는 지시일 때
        if(message.equals("new")) {
            Log.d(TAG, "채팅 로그 어레이 리스트 들어옴");
            Chat_log new_chat_log = data.getChat_log();
            chat_log.add(new_chat_log);
            notifyItemInserted(chat_log.size()-1);
            Chat_A.recyclerView.getLayoutManager().scrollToPosition(chat_log.size()-1);
        }
    }

    /**---------------------------------------------------------------------------
     메소드 ==> Chat_A로 부터 내가 발송한 채팅 메세지 받아서 어레이리스트에 추가 후, 갱신
     ---------------------------------------------------------------------------*/
    public void update_my_msg(Chat_log data) {
        chat_log.add(data);
        notifyItemInserted(chat_log.size()-1);
        Chat_A.recyclerView.getLayoutManager().scrollToPosition(chat_log.size()-1);
//        layoutManager.scrollToPosition(chat_log.size()-1);
//        notifyDataSetChanged();
    }

    public void setView_for_me() {

    }


    public void setView_for_others() {

    }
}
