package com.example.jyn.remotemeeting.Adapter;

import android.content.Context;
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
import com.example.jyn.remotemeeting.R;
import com.example.jyn.remotemeeting.Util.Myapp;

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
    public RCV_Chat_log_list_adapter(Context context, int itemLayout, ArrayList<Chat_log> chat_log, String request) {
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

        ViewHolder(View itemView) {
            super(itemView);
            Log.d(TAG, "ViewHolder");
            Log.d(TAG, "request: "+ request);
            ButterKnife.bind(this,itemView);
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
        String msg_content = chat_log.get(pos).getMsg_content();
        long transmission_gmt_time = chat_log.get(pos).getTransmission_gmt_time();
        String transmission_gmt_time_str = myapp.chat_log_transmission_time(transmission_gmt_time);

        int msg_user_no = chat_log.get(pos).getUser_no();
        boolean me = false;
        if(msg_user_no == Integer.parseInt(myapp.getUser_no())) {
            me = true;
        }

        if(me) {
            holder.not_me_layout.setVisibility(View.GONE);
            holder.me_layout.setVisibility(View.VISIBLE);

            holder.msg_content_me.setText(msg_content);
            holder.time_me.setText(transmission_gmt_time_str);
        }
        else if(!me) {
            holder.me_layout.setVisibility(View.GONE);
            holder.not_me_layout.setVisibility(View.VISIBLE);

            holder.msg_content.setText(msg_content);
            holder.time.setText(transmission_gmt_time_str);
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
     메소드 ==> 채팅방 메세지 받아서 출력 (어레이리스트에 추가)
     ---------------------------------------------------------------------------*/
    public void update_last_msg(String message, Data_for_netty data) {
        // 채팅방 리스트를 업데이트 하라는 지시일 때
        if(message.equals("new")) {
            Log.d(TAG, "채팅 로그 어레이 리스트 들어옴");
            Chat_log new_chat_log = data.getChat_log();
            chat_log.add(new_chat_log);
            notifyItemInserted(chat_log.size()-1);
//            notifyDataSetChanged();
        }
    }

    public void update_my_msg(Chat_log data) {
        chat_log.add(data);
        notifyItemInserted(chat_log.size()-1);
//        notifyDataSetChanged();
    }

    public void setView_for_me() {

    }


    public void setView_for_others() {

    }
}
