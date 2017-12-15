package com.example.jyn.remotemeeting.Adapter;

import android.content.Context;
import android.content.Intent;
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
import com.example.jyn.remotemeeting.Activity.Main_after_login_A;
import com.example.jyn.remotemeeting.DataClass.Chat_room;
import com.example.jyn.remotemeeting.Etc.Static;
import com.example.jyn.remotemeeting.R;
import com.example.jyn.remotemeeting.Util.Myapp;
import com.github.kimkevin.cachepot.CachePot;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.CropCircleTransformation;

/**
 * Created by JYN on 2017-12-09.
 */

public class RCV_chatRoom_list_adapter extends RecyclerView.Adapter<RCV_chatRoom_list_adapter.ViewHolder> {

    private Context context;
    private int itemLayout;
    private String request;
    private ArrayList<Chat_room> rooms;
    public static String TAG = "all_"+RCV_chatRoom_list_adapter.class.getSimpleName();
    public Myapp myapp;

    /** RecyclerAdapter 생성자 */
    public RCV_chatRoom_list_adapter(Context context, int itemLayout, ArrayList<Chat_room> rooms, String request) {
        Log.d(TAG, "ViewHolder_ RCV_chatRoom_list_adapter: 생성");
        this.context = context;
        this.itemLayout = itemLayout;
        this.rooms = rooms;
        this.request = request;

        // 어플리케이션 객체 생성
        myapp = Myapp.getInstance();
    }

    /** 뷰홀더 */
    class ViewHolder extends RecyclerView.ViewHolder {

        /** 버터나이프*/
        @BindView(R.id.container)       LinearLayout container;
        @BindView(R.id.profile_img)     ImageView profile_img;
        @BindView(R.id.title)           TextView title;
        @BindView(R.id.counting)        TextView counting;
        @BindView(R.id.content)         TextView content;
        @BindView(R.id.time)            TextView time;
        @BindView(R.id.unread_msg)      TextView unread_msg;

        ViewHolder(View itemView) {
            super(itemView);
            Log.d(TAG, "ViewHolder");
            Log.d(TAG, "request: "+ request);
            ButterKnife.bind(this,itemView);

            container.setClickable(true);
            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    Log.d(TAG, "클릭 아이템 position: " + pos);
                    Log.d(TAG, "채팅방 no: " + rooms.get(pos).getChatroom_no());
                    Log.d(TAG, "채팅방 참여중인 유저 수: " + rooms.get(pos).getUser_nickname_arr().size());
                    Log.d(TAG, "채팅방의 마지막 메세지 no: " + rooms.get(pos).getLast_msg_no());
                    if(rooms.get(pos).getLast_log() != null) {
                        Log.d(TAG, "채팅방의 마지막 메세지 타입: " + rooms.get(pos).getLast_log().getMsg_type());
                        Log.d(TAG, "채팅방의 마지막 메세지 유저번호: " + rooms.get(pos).getLast_log().getUser_no());
                        Log.d(TAG, "채팅방의 마지막 메세지 내용: " + rooms.get(pos).getLast_log().getMsg_content());
                    }
                    for(int i=0; i<rooms.get(pos).getUser_nickname_arr().size(); i++) {
                        Log.d(TAG, "채팅방 유저 닉네임 리스트_"+ i + ": " + rooms.get(pos).getUser_nickname_arr().get(i));
                    }
                    for(int j=0; j<rooms.get(pos).getUser_img_filename_arr().size(); j++) {
                        Log.d(TAG, "채팅방 유저 프로필 URL_"+ j + ": " + rooms.get(pos).getUser_img_filename_arr().get(j));
                    }
                    Log.d(TAG, "채팅방의 마지막 메세지 전송 시각: " + rooms.get(pos).getTransmission_time_for_local());
                    Log.d(TAG, "채팅방의 안읽은 메세지 개수: " + rooms.get(pos).getUnread_msg_count());

                    // TODO: 해당 채팅방 액티비티로 이동
                    // CachePot 이용해서 클릭한 rooms 객체 전달
                    CachePot.getInstance().push("chat_room", rooms.get(pos));

                    // Chat_A 액티비티(채팅방) 열기
                    // 채팅방 리스트로부터 채팅방을 여는 것임을 intent 값으로 알린다
                    Intent intent = new Intent(context, Chat_A.class);
                    intent.putExtra("from", "list");
                    ((Main_after_login_A)context).startActivityForResult(intent, Main_after_login_A.REQUEST_CHAT_ROOM);
                }
            });
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
        Log.d(TAG, "onBindViewHolder");

        String last_log_msg_content = "none exist";
        // TODO: 나중에, 메세지가 오고 간적이 없는 채팅방은 표시하지 않도록 구현하기,
        // TODO: 현재는 테스트를 위해서 그냥 리스트뷰에 표시되도록 함
        if(rooms.get(pos).getLast_log() != null) {
            last_log_msg_content = rooms.get(pos).getLast_log().getMsg_content();
        }
//        else if(rooms.get(pos).getLast_log() ) {
//
//        }

        String last_log_transmission_time_for_local = rooms.get(pos).getTransmission_time_for_local();
        int member_count = rooms.get(pos).getUser_nickname_arr().size();
        int unread_msg_count = rooms.get(pos).getUnread_msg_count();
        String chat_room_title = rooms.get(pos).getChat_room_title();

        StringBuilder user_nickname_list = new StringBuilder();
        for(int i=0; i<rooms.get(pos).getUser_nickname_arr().size(); i++) {
            if(i==0) {
                user_nickname_list.append(rooms.get(pos).getUser_nickname_arr().get(i));
            }
            else {
                user_nickname_list.append(", ").append(rooms.get(pos).getUser_nickname_arr().get(i));
            }
        }

        StringBuilder user_img_filename_list = new StringBuilder();
        for(int j=0; j<rooms.get(pos).getUser_img_filename_arr().size(); j++) {
            if(j==0) {
                user_img_filename_list.append(rooms.get(pos).getUser_img_filename_arr().get(j));
            }
            else {
                user_img_filename_list.append(", ").append(rooms.get(pos).getUser_img_filename_arr().get(j));
            }
        }

        Log.d(TAG, "채팅방 유저 프로필 URL 리스트: " + user_img_filename_list);
        Log.d(TAG, "채팅방 유저 닉네임 리스트: " + user_nickname_list);
        Log.d(TAG, "채팅방 제목: " + chat_room_title);
        Log.d(TAG, "채팅방 참여중인 유저 수: " + member_count);
        Log.d(TAG, "채팅방의 마지막 메세지 내용: " + last_log_msg_content);
        Log.d(TAG, "채팅방의 마지막 메세지 전송 시각: " + last_log_transmission_time_for_local);
        Log.d(TAG, "채팅방의 안읽은 메세지 개수: " + unread_msg_count);

        // 만약 들어 있는 이미지 URL 개수가 2개라면, 즉 1:1 채팅방이라면
        // 이미지 URL - 채팅방 표시에 들어갈 이미지 URL은 상대방이 되어야 한다
        // 닉네임 - 채팅방 표시에 들어갈 닉네임은 상대방만 있으면 된다
        String img_URL_for_setting = "";
        String nickName_for_setting = "";
        if(rooms.get(pos).getUser_img_filename_arr().size() == 2) {
            // 이미지 URL
            if(rooms.get(pos).getUser_img_filename_arr().get(0).equals(myapp.getUser_img_filename())) {
                img_URL_for_setting = rooms.get(pos).getUser_img_filename_arr().get(1);
            }
            else if(rooms.get(pos).getUser_img_filename_arr().get(1).equals(myapp.getUser_img_filename())) {
                img_URL_for_setting = rooms.get(pos).getUser_img_filename_arr().get(0);
            }
            // 닉네임
            if(rooms.get(pos).getUser_nickname_arr().get(0).equals(myapp.getUser_nickname())) {
                nickName_for_setting = rooms.get(pos).getUser_nickname_arr().get(1);
            }
            else if(rooms.get(pos).getUser_nickname_arr().get(1).equals(myapp.getUser_nickname())) {
                nickName_for_setting = rooms.get(pos).getUser_nickname_arr().get(0);
            }
        }

        // 이미지 URL을 제외한, 데이터 셋팅
//        holder.nickNames.setText(nickName_for_setting);
        if(chat_room_title.equals("none")) {
            holder.title.setText(nickName_for_setting);
        }
        else if(!chat_room_title.equals("none")) {
            holder.title.setText(chat_room_title);
        }
        holder.content.setText(last_log_msg_content);
        holder.counting.setText(String.valueOf(member_count));
        holder.time.setText(last_log_transmission_time_for_local);
        holder.unread_msg.setText(String.valueOf(unread_msg_count));

        // 이미지 셋팅, 일단 지금은 1:1 기준으로만 셋팅
        if(img_URL_for_setting.equals("none")) {
            holder.profile_img.setImageResource(R.drawable.default_profile);
        }
        else if(!img_URL_for_setting.equals("none")) {
            Glide
                .with(context)
                .load(Static.SERVER_URL_PROFILE_FILE_FOLDER + img_URL_for_setting)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .bitmapTransform(new CropCircleTransformation(context))
                .into(holder.profile_img);
        }
    }

    /** getItemCount => arr 사이즈 리턴 */
    @Override
    public int getItemCount() {
        return rooms.size();
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 서버로부터 받아온 내 파트너 리스트로 arrayList 교체하기
     ---------------------------------------------------------------------------*/
    public void refresh_arr(ArrayList<Chat_room> rooms) {
        this.rooms.clear();
        this.rooms = rooms;
        notifyDataSetChanged();
    }

}
