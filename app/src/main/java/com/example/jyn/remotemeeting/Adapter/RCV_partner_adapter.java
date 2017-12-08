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
import com.example.jyn.remotemeeting.Activity.Main_after_login_A;
import com.example.jyn.remotemeeting.DataClass.Users;
import com.example.jyn.remotemeeting.Dialog.Profile_detail_D;
import com.example.jyn.remotemeeting.Etc.Static;
import com.example.jyn.remotemeeting.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.CropCircleTransformation;

/**
 * Created by JYN on 2017-12-01.
 */

public class RCV_partner_adapter extends RecyclerView.Adapter<RCV_partner_adapter.ViewHolder> {

    private Context context;
    private int itemLayout;
    private String request;
    private ArrayList<Users> users;
    public static String TAG = "all_"+RCV_partner_adapter.class.getSimpleName();


    /** RecyclerAdapter 생성자 */
    public RCV_partner_adapter(Context context, int itemLayout, ArrayList<Users> users, final String request) {
        Log.d(TAG, "ViewHolder_ RCV_partner_adapter: 생성");
        this.context = context;
        this.itemLayout = itemLayout;
        this.users = users;
        this.request = request;
    }

    /** 뷰홀더 */
    class ViewHolder extends RecyclerView.ViewHolder {

        /** 버터나이프*/
        @BindView(R.id.container)       LinearLayout container;
        @BindView(R.id.profile_img)     ImageView profile_img;
        @BindView(R.id.on_meeting)      ImageView on_meeting;
        @BindView(R.id.nickName)        TextView nickName;
        @BindView(R.id.email)           TextView email;

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
                    Log.d(TAG, "getUser_no_arr: " + users.get(pos).getUser_no());
                    Log.d(TAG, "getUser_email: " + users.get(pos).getUser_email());
                    Log.d(TAG, "getUser_nickName: " + users.get(pos).getUser_nickname());
                    Log.d(TAG, "getUser_img_fileName: " + users.get(pos).getUser_img_filename());

                    // 프로필 상세보기 다이얼로그 액티비티 열기
                    Intent intent = new Intent(context, Profile_detail_D.class);
                    intent.putExtra("user_no", users.get(pos).getUser_no());
                    intent.putExtra("nickname", users.get(pos).getUser_nickname());
                    intent.putExtra("email", users.get(pos).getUser_email());
                    intent.putExtra("img_fileName", users.get(pos).getUser_img_filename());
                    intent.putExtra("on_meeting", users.get(pos).getPresent_meeting_in_ornot());
                    ((Main_after_login_A)context).startActivityForResult(intent, Main_after_login_A.REQUEST_SHOW_PROFILE_DETAIL);
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

        String nickName = users.get(position).getUser_nickname();
        String email = users.get(position).getUser_email();
        String img_fileName = users.get(position).getUser_img_filename();
        String on_air = users.get(position).getPresent_meeting_in_ornot();
//        Log.d(TAG, "nickName: " + nickName);
//        Log.d(TAG, "email: " + email);
//        Log.d(TAG, "img_fileName: " + img_fileName);
//        Log.d(TAG, "on_air: " + on_air);

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
        }
        else if(!on_air.equals("")) {
            holder.on_meeting.setVisibility(View.VISIBLE);
        }
    }

    /** getItemCount => arr 사이즈 리턴 */
    @Override
    public int getItemCount() {
        return users.size();
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 서버로부터 받아온 내 파트너 리스트로 arrayList 교체하기
     ---------------------------------------------------------------------------*/
    public void refresh_arr(ArrayList<Users> users) {
        this.users.clear();
        this.users = users;
        notifyDataSetChanged();
    }
}
