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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.jyn.remotemeeting.DataClass.Chat_room;
import com.example.jyn.remotemeeting.Etc.Static;
import com.example.jyn.remotemeeting.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.CropCircleTransformation;

/**
 * Created by JYN on 2017-12-09.
 */

public class RCV_chat_adapter extends RecyclerView.Adapter<RCV_chat_adapter.ViewHolder> {

    private Context context;
    private int itemLayout;
    private String request;
    private ArrayList<Chat_room> rooms;
    public static String TAG = "all_"+RCV_chat_adapter.class.getSimpleName();

    /** RecyclerAdapter 생성자 */
    public RCV_chat_adapter(Context context, int itemLayout, ArrayList<Chat_room> rooms, String request) {
        Log.d(TAG, "ViewHolder_ RCV_chat_adapter: 생성");
        this.context = context;
        this.itemLayout = itemLayout;
        this.rooms = rooms;
        this.request = request;
    }

    /** 뷰홀더 */
    class ViewHolder extends RecyclerView.ViewHolder {

        /** 버터나이프*/
        @BindView(R.id.container)       LinearLayout container;
        @BindView(R.id.profile_img)     ImageView profile_img;
        @BindView(R.id.nickNames)       TextView nickNames;
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
                    Log.d(TAG, "채팅방 no: " + rooms.get(pos).getLast_log().getChatroom_no());
                    Log.d(TAG, "채팅방 참여중인 유저 수: " + rooms.get(pos).getUser_no_arr().size());
                    Log.d(TAG, "채팅방의 마지막 메세지 no: " + rooms.get(pos).getLast_msg_no());
                    Log.d(TAG, "채팅방의 마지막 메세지 타입: " + rooms.get(pos).getLast_log().getType());
                    Log.d(TAG, "채팅방의 마지막 메세지 유저번호: " + rooms.get(pos).getLast_log().getUser_no());
                    for(int i=0; i<rooms.get(pos).getUser_nickname_arr().size(); i++) {
                        Log.d(TAG, "채팅방의 마지막 메세지 유저 닉네임 리스트_"+ i + ": " + rooms.get(pos).getUser_nickname_arr().get(i));
                    }
                    for(int j=0; j<rooms.get(pos).getUser_img_filename_arr().size(); j++) {
                        Log.d(TAG, "채팅방의 마지막 메세지 유저 프로필 URL: " + rooms.get(pos).getUser_img_filename_arr().get(j));
                    }
                    Log.d(TAG, "채팅방의 마지막 메세지 내용: " + rooms.get(pos).getLast_log().getContent());
                    Log.d(TAG, "채팅방의 마지막 메세지 전송 시각: " + rooms.get(pos).getTransmission_time_for_local());
                    int unread_msg_count = rooms.get(pos).getUser_no_arr().size() - rooms.get(pos).getUnread_msg_count();
                    Log.d(TAG, "채팅방의 안읽은 메세지 개수: " + unread_msg_count);

                    // TODO: 클릭 시, 해당 채팅방 액티비티로 이동
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

        String last_log_msg_content = rooms.get(pos).getLast_log().getContent();
        String last_log_transmission_time_for_local = rooms.get(pos).getTransmission_time_for_local();
        int member_count = rooms.get(pos).getUser_no_arr().size();
        int unread_msg_count = member_count - rooms.get(pos).getUnread_msg_count();

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

        Log.d(TAG, "채팅방의 마지막 메세지 유저 프로필 URL 리스트: " + user_img_filename_list);
        Log.d(TAG, "채팅방의 마지막 메세지 유저들 닉네임 리스트: " + user_nickname_list);
        Log.d(TAG, "채팅방 참여중인 유저 수: " + member_count);
        Log.d(TAG, "채팅방의 마지막 메세지 내용: " + last_log_msg_content);
        Log.d(TAG, "채팅방의 마지막 메세지 전송 시각: " + last_log_transmission_time_for_local);
        Log.d(TAG, "채팅방의 안읽은 메세지 개수: " + unread_msg_count);

        // 이미지 제외, 데이터 셋팅
        holder.nickNames.setText(user_nickname_list);
        holder.content.setText(last_log_msg_content);
        holder.counting.setText(String.valueOf(member_count));
        holder.time.setText(last_log_transmission_time_for_local);
        holder.unread_msg.setText(String.valueOf(unread_msg_count));

        // 이미지 셋팅, 지금은 첫번째 이미지만 일단 셋팅
        if(rooms.get(pos).getUser_img_filename_arr().get(0).equals("none")) {
            holder.profile_img.setImageResource(R.drawable.default_profile);
        }
        else if(!rooms.get(pos).getUser_img_filename_arr().get(0).equals("none")) {
            Glide
                .with(context)
                .load(Static.SERVER_URL_PROFILE_FILE_FOLDER + rooms.get(pos).getUser_img_filename_arr().get(0))
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
