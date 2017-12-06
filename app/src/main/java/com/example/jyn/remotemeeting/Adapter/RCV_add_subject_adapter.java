package com.example.jyn.remotemeeting.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
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
import com.example.jyn.remotemeeting.DataClass.Users;
import com.example.jyn.remotemeeting.Etc.Static;
import com.example.jyn.remotemeeting.R;
import com.example.jyn.remotemeeting.Util.Myapp;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.CropCircleTransformation;

/**
 * Created by JYN on 2017-12-03.
 */

public class RCV_add_subject_adapter extends RecyclerView.Adapter<RCV_add_subject_adapter.ViewHolder> {

    private Context context;
    private int itemLayout;
    private String request;
    private ArrayList<Users> users;
    public static String TAG = "all_"+RCV_add_subject_adapter.class.getSimpleName();
    public String added_subject_user_no = "";
    private int added_subject_user_no_pos = -1;
    Myapp myapp;

    /** RecyclerAdapter 생성자 */
    public RCV_add_subject_adapter(Context context, int itemLayout, ArrayList<Users> users, final String request) {
        Log.d(TAG, "ViewHolder_ RCV_add_subject_adapter: 생성");
        this.context = context;
        this.itemLayout = itemLayout;
        this.users = users;
        this.request = request;

        // 어플리케이션 객체 생성
        myapp = Myapp.getInstance();

    }

    /** 뷰홀더 */
    public class ViewHolder extends RecyclerView.ViewHolder {

        /** 버터나이프*/
        @BindView(R.id.profile_img)     ImageView profile_img;
        @BindView(R.id.on_meeting)      ImageView on_meeting;
        @BindView(R.id.check_mark)      ImageView check_mark;
        @BindView(R.id.nickName)        TextView nickName;
        @BindView(R.id.email)           TextView email;
        @BindView(R.id.container)       LinearLayout container;

        public ViewHolder(View itemView, int itemLayout, final String request) {
            super(itemView);
            Log.d(TAG, "ViewHolder");
            Log.d(TAG, "request: "+ request);
            ButterKnife.bind(this,itemView);

            /** 아이템 클릭 이벤트 설정 */
            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();

                    String checked_orNot = users.get(pos).getExtra();
                    String target_user_no = users.get(pos).getUser_no();

                    // 체크되어 있는 유저가 없을 경우
                    if(added_subject_user_no.equals("")) {
                        // 더블 체크 - 해당 유저가 체크되어있는지 아닌지 확인
                        if(checked_orNot.equals("check_no")) {
                            users.get(pos).setExtra("check_yes");
                            added_subject_user_no = target_user_no;
                            added_subject_user_no_pos = pos;
                            notifyItemChanged(pos);
                        }

                    }
                    // 체크되어 있는 유저가 있을 경우
                    else if(!added_subject_user_no.equals("")) {
                        // 해당 유저가, 체크되어 있는 유저인 경우 --> 체크해제
                        if(added_subject_user_no.equals(target_user_no)) {
                            users.get(pos).setExtra("check_no");
                            added_subject_user_no = "";
                            added_subject_user_no_pos = -1;
                            notifyItemChanged(pos);
                        }
                        // 해당 유저가, 체크되어 있는 유저가 아닌 경우 --> 토스트 알림
                        else if(!added_subject_user_no.equals(target_user_no)) {
                            myapp.logAndToast("이미 지정한 대상이 존재합니다");
                        }
                    }

                    Log.d(TAG, "클릭 아이템 position: " + pos);
                    Log.d(TAG, "getUser_no: " + users.get(pos).getUser_no());
                    Log.d(TAG, "getUser_email: " + users.get(pos).getUser_email());
                    Log.d(TAG, "getUser_nickName: " + users.get(pos).getUser_nickname());
                    Log.d(TAG, "getUser_img_fileName: " + users.get(pos).getUser_img_filename());
                    Log.d(TAG, "checked_orNot: " + users.get(pos).getExtra());
                }
            });
        }
    }

    /** onCreateViewHolder => 뷰홀더 생성 - 인플레이팅 */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder");
        View view = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
        ViewHolder viewHolder = new ViewHolder(view, itemLayout, request);

        return viewHolder;
    }

    /** onBindViewHolder => 리스트뷰의 getView 역할 */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder");

        String nickName = users.get(position).getUser_nickname();
        String email = users.get(position).getUser_email();
        String img_fileName = users.get(position).getUser_img_filename();
        String on_air = users.get(position).getPresent_meeting_in_ornot();
        String checked_orNot = users.get(position).getExtra();
        Log.d(TAG, "nickName: " + nickName);
        Log.d(TAG, "email: " + email);
        Log.d(TAG, "img_fileName: " + img_fileName);
        Log.d(TAG, "on_air: " + on_air);
        Log.d(TAG, "checked_orNot: " + checked_orNot);

        // 닉네임, 이메일 셋팅
        holder.nickName.setText(nickName);
        String[] temp = email.split("@");
        holder.email.setText(temp[0] + "@");

        // 이미지 셋팅
        if(img_fileName.equals("none")) {
            holder.profile_img.setImageResource(R.drawable.default_profile);
        }
        else if(!img_fileName.equals("none")) {
            Glide
                .with(context)
                .load(Static.SERVER_URL_PROFILE_FOLDER + img_fileName)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .bitmapTransform(new CropCircleTransformation(context))
                .into(holder.profile_img);
        }

        // 미팅중인지 아닌지 표시
        if(on_air.equals("")) {
            holder.on_meeting.setVisibility(View.GONE);
//            holder.container.setClickable(true);    // 미팅중 아닌 사람만 클릭 가능하게
        }
        else if(!on_air.equals("")) {
            holder.on_meeting.setVisibility(View.VISIBLE);
//            holder.container.setClickable(false);   // 미팅중 아닌 사람만 클릭 가능하게
            // 이미지뷰에 어두운 효과 주기
            holder.profile_img.setColorFilter(Color.parseColor("#72746b"), PorterDuff.Mode.MULTIPLY);
        }

        // 체크되었는지 아닌지 체크하기
        if(checked_orNot.equals("check_no")) {
            holder.check_mark.setVisibility(View.GONE);
        }
        else if(checked_orNot.equals("check_yes")) {
            holder.check_mark.setVisibility(View.VISIBLE);
            // 이미지뷰에 어두운 효과 주기
            holder.profile_img.setColorFilter(Color.parseColor("#72746b"), PorterDuff.Mode.MULTIPLY);
        }
    }

    /** getItemCount => arr 사이즈 리턴 */
    @Override
    public int getItemCount() {
        return users.size();
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 서버 통신 -- 서버로부터 받아온 내 파트너 리스트로 arrayList 교체하기
     ---------------------------------------------------------------------------*/
    public void refresh_arr(ArrayList<Users> users) {
        this.users.clear();
        this.users = users;
        notifyDataSetChanged();
        added_subject_user_no = "";
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 지정 회의 대상 객체 전달
     ---------------------------------------------------------------------------*/
    public Users here_is_the_target_user_info() {
        if(added_subject_user_no_pos != -1) {
            return users.get(added_subject_user_no_pos);
        }
        return null;
    }


}

