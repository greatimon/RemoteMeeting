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
import com.example.jyn.remotemeeting.DataClass.Meeting_room;
import com.example.jyn.remotemeeting.DataClass.Users;
import com.example.jyn.remotemeeting.Etc.Static;
import com.example.jyn.remotemeeting.R;
import com.example.jyn.remotemeeting.Util.Myapp;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.CropCircleTransformation;

/**
 * Created by JYN on 2018-01-23.
 */

public class RCV_project_meeting_result_list_adapter extends RecyclerView.Adapter<RCV_project_meeting_result_list_adapter.ViewHolder> {

    private Context context;
    private int itemLayout;
    private ArrayList<Meeting_room> meeting_room_arr;
    public static String TAG = "all_" + RCV_project_meeting_result_list_adapter.class.getSimpleName();
    Myapp myapp;

    /** RecyclerAdapter 생성자 */
    public RCV_project_meeting_result_list_adapter(
            Context context, int itemLayout, ArrayList<Meeting_room> meeting_room_arr) {
        Log.d(TAG, "RCV_project_meeting_result_list_adapter: 생성");
        this.context = context;
        this.itemLayout = itemLayout;
        this.meeting_room_arr = meeting_room_arr;

        // 어플리케이션 객체 생성
        myapp = Myapp.getInstance();

        Log.d(TAG, "meeting_room_arr.size(): " + meeting_room_arr.size());
    }

    /** 뷰홀더 */
    class ViewHolder extends RecyclerView.ViewHolder {

        /** 버터나이프*/
        @BindView(R.id.container_LIN)                       LinearLayout container_LIN;
        @BindView(R.id.date_line_LIN)                       LinearLayout date_line_LIN;
        @BindView(R.id.profile_imgV)                        ImageView profile_imgV;
//        @BindView(R.id.memo_imgV)                           ImageView memo_imgV;
//        @BindView(R.id.image_to_document_imgV)              ImageView image_to_document_imgV;
//        @BindView(R.id.upload_images_imgV)                  ImageView upload_images_imgV;
//        @BindView(R.id.drawing_images_imgV)                 ImageView drawing_images_imgV;
        @BindView(R.id.meeting_title_txt)                   TextView meeting_title_txt;
        @BindView(R.id.meeting_subject_user_nickName_txt)   TextView meeting_subject_user_nickName_txt;
        @BindView(R.id.total_meeting_time_txt)              TextView total_meeting_time_txt;
        @BindView(R.id.date_line_txt)                       TextView date_line_txt;
        @BindView(R.id.date_line_divider_V_1)               View date_line_divider_V_1;
        @BindView(R.id.date_line_divider_V_2)               View date_line_divider_V_2;


        ViewHolder(View itemView) {
            super(itemView);
            Log.d(TAG, "ViewHolder");
            ButterKnife.bind(this,itemView);

            /** 아이템 클릭 이벤트 설정 */
            container_LIN.setClickable(true);
            container_LIN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    Log.d(TAG, "클릭 아이템 position: " + pos);

                    Log.d(TAG, "meeting_room_arr.getMeeting_no(): " + meeting_room_arr.get(pos).getMeeting_no());
                    Log.d(TAG, "meeting_room_arr.getReal_meeting_title(): " + meeting_room_arr.get(pos).getReal_meeting_title());
                    Log.d(TAG, "meeting_room_arr.getMeeting_creator_user_no(): " + meeting_room_arr.get(pos).getMeeting_creator_user_no());
                    Log.d(TAG, "meeting_room_arr.getMeeting_subject_user_no(): " + meeting_room_arr.get(pos).getMeeting_subject_user_no());
                    Log.d(TAG, "meeting_room_arr.getMeeting_end_time(): " + meeting_room_arr.get(pos).getMeeting_end_time());
                    Log.d(TAG, "meeting_room_arr.getTotal_meeting_time(): " + meeting_room_arr.get(pos).getTotal_meeting_time());

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
    public void onBindViewHolder(ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder");

        if(holder.getAdapterPosition() != RecyclerView.NO_POSITION) {
            // 아이템의 index 값
            int pos = holder.getAdapterPosition();

            // 임시 meeting_room 객체 생성
            Meeting_room temp_meeting_room = meeting_room_arr.get(pos);

            // 상대방 user_no 가져오기
            String this_is_subject_user_no = "";
            if(!temp_meeting_room.getMeeting_creator_user_no().equals(myapp.getUser_no())) {
                this_is_subject_user_no = temp_meeting_room.getMeeting_creator_user_no();
            }
            else if(!temp_meeting_room.getMeeting_subject_user_no().equals(myapp.getUser_no())) {
                this_is_subject_user_no = temp_meeting_room.getMeeting_subject_user_no();
            }
            Log.d(TAG, "this_is_subject_user_no: " + this_is_subject_user_no);

            // 상대방 user_info 서버로 부터 받아오기
            Users target_user = myapp.get_user_info(this_is_subject_user_no);

            // 상대방 프로필 이미지 셋팅
            Glide
                .with(context)
                .load(Static.SERVER_URL_PROFILE_FILE_FOLDER + target_user.getUser_img_filename())
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .bitmapTransform(new CropCircleTransformation(context))
                .into(holder.profile_imgV);

            // 영상회의 제목 셋팅
            holder.meeting_title_txt.setText(temp_meeting_room.getReal_meeting_title());

            // 상대방 닉네임 셋팅
            holder.meeting_subject_user_nickName_txt.setText(target_user.getUser_nickname());

            // 총 회의 시간 셋팅
            holder.total_meeting_time_txt.setText(temp_meeting_room.getTotal_meeting_time());
        }
    }

    /** getItemCount => arr 사이즈 리턴 */
    @Override
    public int getItemCount() {
        return meeting_room_arr.size();
    }
}
