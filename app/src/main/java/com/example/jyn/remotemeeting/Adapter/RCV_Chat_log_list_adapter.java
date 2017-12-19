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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.jyn.remotemeeting.Activity.Chat_A;
import com.example.jyn.remotemeeting.DataClass.Chat_log;
import com.example.jyn.remotemeeting.DataClass.Data_for_netty;
import com.example.jyn.remotemeeting.DataClass.Users;
import com.example.jyn.remotemeeting.Etc.Static;
import com.example.jyn.remotemeeting.R;
import com.example.jyn.remotemeeting.Util.Myapp;
import com.google.gson.Gson;
import com.pnikosis.materialishprogress.ProgressWheel;

import org.json.JSONArray;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.CropCircleTransformation;

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
        @BindView(R.id.date_notice_layout)              LinearLayout date_notice_layout;
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
        @BindView(R.id.date_notice)                     TextView date_notice;
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
        Log.d(TAG, "===================================================================");
        Log.d(TAG, "onCreateViewHolder");
        View view = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }


    /** onBindViewHolder => 리스트뷰의 getView 역할 */
    @Override
    public void onBindViewHolder(ViewHolder holder, int pos) {
        Gson gson = new Gson();

        // 라사이클러뷰의 포지션이 존재할때만, 리사이클러뷰 아이템들에 data들을 넣어서 보여줌
        if(holder.getAdapterPosition() != RecyclerView.NO_POSITION) {
            int position = holder.getAdapterPosition();

            // 메세지 내용, get
            String msg_content = chat_log.get(position).getMsg_content();
            // 메세지 서버 전송 millisecond, get
            long transmission_gmt_time = chat_log.get(position).getTransmission_gmt_time();
//            Log.d(TAG, "transmission_gmt_time_ "+ position + ": " + transmission_gmt_time);
            // 안드로이드 기기 local 기준, transmission_gmt_time을 특정 string 값으로 변환
            String transmission_gmt_time_str =
                    myapp.chat_log_transmission_time(transmission_gmt_time, "chat_log");

            // =================================================================
            // 내 채팅 메세지 일때, transmission_gmt_time 확인해보기 (서버로부터 확인 콜백 받기 전 로직)
            // 확인 결과 long type의 default 값인 '0'으로 표시
            if(chat_log.get(position).getUser_no() == Integer.parseInt(myapp.getUser_no())) {
                Log.d(TAG, "transmission_gmt_time:" + transmission_gmt_time);
                Log.d(TAG, "transmission_gmt_time:" + transmission_gmt_time_str);
                Log.d(TAG, "chat_log.get(position).getMember_count():" + chat_log.get(position).getMember_count());
            }

            // 채팅 로그의 user_no 가져오기
            int msg_user_no = chat_log.get(position).getUser_no();

            /** 날짜변경선 item인 경우 */
            if(msg_user_no == -10) {
                // View Visibility 조절
                setView_for_changed_date_item(holder);
                // 변경된 날짜 set
                holder.date_notice.setText(msg_content);
            }

            /** 일반 채팅 item인 경우 */
            else if(msg_user_no != -10) {
                // 이 메세지 당시 member count, get
                int member_count = chat_log.get(position).getMember_count();
                Log.d(TAG, "member_count: " + member_count);
                // 이 메세지를 읽을 수 있는 대상 리스트, get
                String msg_unread_user_no_list_str = chat_log.get(position).getMsg_unread_user_no_list();
                Log.d(TAG, "msg_unread_user_no_list_str: " + msg_unread_user_no_list_str);

                int msg_unread_user_no_list_count = 0;
                if(msg_unread_user_no_list_str != null) {
                    if(msg_unread_user_no_list_str.contains(Static.SPLIT)) {
                        String[] temp = msg_unread_user_no_list_str.split(Static.SPLIT);
                        msg_unread_user_no_list_count = temp.length;
                    }
                    else if(!msg_unread_user_no_list_str.contains(Static.SPLIT)) {
                        msg_unread_user_no_list_count = 1;
                    }
                }
                Log.d(TAG, "msg_unread_user_no_list_count: " + msg_unread_user_no_list_count);

                // =================================================================
                // 읽지 않은 count 수 가져오기
                int unread_msg_count = chat_log.get(position).getMsg_unread_count();
                Log.d(TAG, "최초_ msg_unread_count: " + unread_msg_count);

                // =================================================================
                // 내 채팅 로그인지 아닌지 확인하는 변수 선언 + 확인하기
                boolean me = false;
                if(msg_user_no == Integer.parseInt(myapp.getUser_no())) {
                    me = true;
                }

                // =================================================================
                // 연속된 메세지일때 View를 다르게 하기 위한 구분자 변수
                boolean serial_msg = false;
                if(position != 0) {
                    // 이전 ArrayList의 바로 전 아이템의 user_no와 지금 아이템의 user_no를 비교함
                    int just_right_before_item_user_no = chat_log.get(position-1).getUser_no();
                    // 연속된 메세지인 경우
                    if(just_right_before_item_user_no == msg_user_no) {
                        serial_msg = true;
                    }
                }

                // =================================================================
                // 내 채팅 로그일 때, View Visibility 조절 + 데이터 set
                if(me) {
                    boolean show_unread_msg_count = true;
                    if(unread_msg_count == 0) {
                        show_unread_msg_count = false;
                    }
                    // View Visibility 조절
                    setView_for_me(holder, show_unread_msg_count);

                    // 채팅 내용
                    holder.msg_content_me.setText(msg_content);
                    // 채팅 서버 도착 시간
                    holder.time_me.setText(transmission_gmt_time_str);
                    // 읽지 않은 메시지 count
                    holder.unread_msg_count_me.setText(String.valueOf(unread_msg_count));

                    // HTTP 통신으로 받은 내 채팅 로그, 즉 내가 보냈던 이전 채팅 로그 기록들을 받을때,
                    // 프로그레스 휠을 보여주지 않고, 읽지 않은 메시지 카운팅을 보여준다
                    if(chat_log.get(position).getMsg_no() != 0) {
                        holder.progress_wheel.setVisibility(View.GONE);
                    }

                    // Netty 통신으로 받은 내 채팅 로그, 즉 내가 보낸 실시간 전송된 채팅 로그를 표시할 때는,
                    // 일단 프로그레스 휠을 보여준다 (읽지 않은 메시지 카운팅 보여 주지 않음)
                    // 나중에 Netty 서버로 부터, 내가 보낸 메시지가 제대로 잘 도착했다는 콜백을 받은 이후에
                    // 다른 메소드에서 프로그레스 휠을 GONE 처리 하고 읽지 않은 메시지 카운팅 뷰를 VISIBLE 처리 한다
                    else if(chat_log.get(position).getMsg_no() == 0) {
                        holder.progress_wheel.setVisibility(View.VISIBLE);
                    }
                }
                // =================================================================
                // 내 채팅 로그가 아닐 때, View Visibility 조절 + 데이터 set
                else if(!me) {
                    // 이 메세지의 member_count 가 '2'이고, 이 메세지를 읽을 수 있는 대상 리스트의 개수가 1개이고,
                    // 그게 지금 상대방이라면, 난 지금 상대방의 메세지를 읽게 되는 것이므로
                    // unread_msg_count - 1, 처리를 한다
                    boolean show_unread_msg_count = true;
                    if(msg_unread_user_no_list_str != null) {
//                        Log.d(TAG, "msg_unread_user_no_list_str is NOT NULL");
                        if(member_count == 2 && unread_msg_count == 1
                                && msg_unread_user_no_list_str.equals(myapp.getUser_no())) {
//                            Log.d(TAG, "show_unread_msg_count = false");

                            // 읽었으므로 -1
                            unread_msg_count = unread_msg_count - 1;
                        }
                    }

                    // 읽지 않은 메세지 count가 0이면 count 뷰를 보여주지 않는다
                    if(unread_msg_count == 0) {
                        show_unread_msg_count = false;
                    }
                    Log.d(TAG, "show_unread_msg_count: " + show_unread_msg_count);
                    Log.d(TAG, "체크 후_ msg_unread_count: " + unread_msg_count);

                    // View Visibility 조절
                    setView_for_others(holder, serial_msg, show_unread_msg_count);

                    // 연속된 메세지가 아니라면
                    if(!serial_msg) {
                        // 서버로부터 target_user_no 정보 받기
                        Users user = myapp.get_user_info(String.valueOf(msg_user_no));

                        // target_user_no 프로필 사진 set
                        if(user.getUser_img_filename().equals("none")) {
                            holder.sender_profile_img.setImageResource(R.drawable.default_profile);
                        }
                        else if(!user.getUser_img_filename().equals("none")) {
                            Glide
                                    .with(context)
                                    .load(Static.SERVER_URL_PROFILE_FILE_FOLDER + user.getUser_img_filename())
                                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                                    .bitmapTransform(new CropCircleTransformation(context))
                                    .into(holder.sender_profile_img);
                        }
                        // target_user_no 닉네임 set
                        holder.nickName.setText(user.getUser_nickname());
                    }

                    // 채팅 내용
                    holder.msg_content.setText(msg_content);
                    // 채팅 서버 도착 시간
                    holder.time.setText(transmission_gmt_time_str);
                    // 읽지 않은 메시지 count
                    holder.unread_msg_count.setText(String.valueOf(unread_msg_count));
                }
            }
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
        // 다른 사람의 채팅 메세지일 때
        if(message.equals("new")) {
            Log.d(TAG, "채팅 로그 어레이 리스트 들어옴");
            Chat_log new_chat_log = data.getChat_log();

            // 이 채팅방이, 내가 서버로부터 받은 채팅메세지의 채팅방과 동일한지 확인
            int get_this_chatroom_no = myapp.getChatroom_no();
            int received_chatroon_no = data.getChat_log().getChat_room_no();
            // 동일할때만 arrayList에 추가
            if(get_this_chatroom_no == received_chatroon_no) {
                chat_log.add(new_chat_log);
                notifyItemInserted(chat_log.size()-1);
                Chat_A.recyclerView.getLayoutManager().scrollToPosition(chat_log.size()-1);
            }
        }
        // 내가 보낸 채팅 메세지에 대한 '전송완료' 콜백 메시지일 때
        else if(message.equals("my_chat_msg_call_back")) {
            Log.d(TAG, "내가 서버로 전송한 채팅 메세지에 대한 '전송완료' 콜백 받음");

            Data_for_netty call_back_data = data;
            Chat_log my_chat_log = call_back_data.getChat_log();

            String uuid_this_chat_log = call_back_data.getExtra();
            Log.d(TAG, "uuid_this_chat_log: " + uuid_this_chat_log);
            long local_time_this_chat_log = myapp.getTemp_my_chat_log_hash().get(uuid_this_chat_log);

            // uuid 값과, local_time_this_chat_log 값을 이용해서.
            // 아이템 arrayList 중에서 내가 보낸 채팅 메세지를 찾아서 msg_no를 넣어줌
            for(int i=0; i<getItemCount(); i++) {
                if(chat_log.get(i).getTransmission_gmt_time() == local_time_this_chat_log) {
                    int this_msg_no = my_chat_log.getMsg_no();
                    Log.d(TAG, "서버로 부터 받은 내가 보낸 채팅 메세지의 msg_no 값: " + this_msg_no);
                    chat_log.get(i).setMsg_no(this_msg_no);
                    // 해쉬맵에서 해당 UUID 아이템 제거
                    myapp.getTemp_my_chat_log_hash().remove(uuid_this_chat_log);
                    Log.d(TAG, "myapp.getTemp_my_chat_log_hash().size(): " + myapp.getTemp_my_chat_log_hash().size());
                    // 해당 아이템만 notifyItemChanged
                    notifyItemChanged(i);
                }
            }
        }
    }

    /**---------------------------------------------------------------------------
     메소드 ==> Chat_A로 부터 내가 발송한 채팅 메세지 받아서 어레이리스트에 추가 후, 갱신
                추후에 netty 서버에서 채팅 메시지가 잘 도착했다는 콜백을 받기 전에,
                일단 내 채팅방에 표시하는 것임
     ---------------------------------------------------------------------------*/
    public void update_my_msg_immediately(Chat_log data) {
        chat_log.add(data);
        notifyItemInserted(chat_log.size()-1);
        Chat_A.recyclerView.getLayoutManager().scrollToPosition(chat_log.size()-1);
//        layoutManager.scrollToPosition(chat_log.size()-1);
//        notifyDataSetChanged();
    }


    /**---------------------------------------------------------------------------
     메소드 ==>  날짜 변경선 item일 때, View visibility 셋팅
     ---------------------------------------------------------------------------*/
    private void setView_for_changed_date_item(ViewHolder holder) {
        holder.not_me_layout.setVisibility(View.GONE);
        holder.me_layout.setVisibility(View.GONE);
        holder.date_notice_layout.setVisibility(View.VISIBLE);
    }


    /**---------------------------------------------------------------------------
     메소드 ==>  내 메세지일 때, View visibility 셋팅
     ---------------------------------------------------------------------------*/
    private void setView_for_me(ViewHolder holder, boolean show_unread_msg_count_me) {
        holder.not_me_layout.setVisibility(View.GONE);
        holder.date_notice_layout.setVisibility(View.GONE);
        holder.me_layout.setVisibility(View.VISIBLE);
        if(show_unread_msg_count_me) {
            holder.unread_msg_count_me.setVisibility(View.VISIBLE);
        }
        else if(!show_unread_msg_count_me) {
            holder.unread_msg_count_me.setVisibility(View.GONE);
        }
    }


    /**---------------------------------------------------------------------------
     메소드 ==>  내 메세지가 아닐 때, View visibility 셋팅
     ---------------------------------------------------------------------------*/
    private void setView_for_others(ViewHolder holder, boolean serial, boolean show_unread_msg_count) {
        holder.me_layout.setVisibility(View.GONE);
        holder.date_notice_layout.setVisibility(View.GONE);
        holder.not_me_layout.setVisibility(View.VISIBLE);
        if(show_unread_msg_count) {
            holder.unread_msg_count.setVisibility(View.VISIBLE);
        }
        else if(!show_unread_msg_count) {
            holder.unread_msg_count.setVisibility(View.GONE);
        }

        // 연속된 메세지일 때
        if(serial) {
            holder.sender_profile_img.setVisibility(View.GONE);
            holder.nickName.setVisibility(View.GONE);
            holder.serial_msg_profile_img.setVisibility(View.VISIBLE);
            holder.serial_msg_above_content.setVisibility(View.VISIBLE);
        }
        // 비연속된 메세지일 때
        else if(!serial) {
            holder.serial_msg_profile_img.setVisibility(View.GONE);
            holder.serial_msg_above_content.setVisibility(View.GONE);
            holder.sender_profile_img.setVisibility(View.VISIBLE);
            holder.nickName.setVisibility(View.VISIBLE);
        }
    }
}
