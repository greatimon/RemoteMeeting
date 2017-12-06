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
import com.example.jyn.remotemeeting.Activity.Search_partner;
import com.example.jyn.remotemeeting.DataClass.Users;
import com.example.jyn.remotemeeting.Dialog.Confirm_subtract_partner_D;
import com.example.jyn.remotemeeting.Etc.Static;
import com.example.jyn.remotemeeting.R;
import com.example.jyn.remotemeeting.Util.Myapp;
import com.example.jyn.remotemeeting.Util.RetrofitService;
import com.example.jyn.remotemeeting.Util.ServiceGenerator;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.CropCircleTransformation;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by JYN on 2017-12-02.
 */

public class RCV_search_partner_adapter extends RecyclerView.Adapter<RCV_search_partner_adapter.ViewHolder> {

    private Context context;
    private int itemLayout;
    private String request;
    private ArrayList<Users> users;
    public static String TAG = "all_"+RCV_search_partner_adapter.class.getSimpleName();
    private Myapp myapp;

    /** RecyclerAdapter 생성자 */
    public RCV_search_partner_adapter(Context context, int itemLayout, ArrayList<Users> users, final String request) {
        Log.d(TAG, "ViewHolder_ RCV_search_partner_adapter: 생성");
        this.context = context;
        this.itemLayout = itemLayout;
        this.users = users;
        this.request = request;

        // 어플리케이션 객체 생성
        myapp = Myapp.getInstance();
    }

    /** 뷰홀더 */
    class ViewHolder extends RecyclerView.ViewHolder {

        /** 버터나이프*/
        @BindView(R.id.profile_img)         ImageView profile_img;
        @BindView(R.id.nickName)            TextView nickName;
        @BindView(R.id.email)               TextView email;
        @BindView(R.id.follow)              TextView follow;
        @BindView(R.id.unFollow)            LinearLayout unFollow;

        ViewHolder(View itemView) {
            super(itemView);
            Log.d(TAG, "ViewHolder");
            Log.d(TAG, "request: "+ request);
            ButterKnife.bind(this,itemView);

            /** 파트너 맺기 */
            follow.setClickable(true);
            follow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int pos = getAdapterPosition();

                    Log.d(TAG, "클릭 아이템 position: " + pos);
                    Log.d(TAG, "getUser_no: " + users.get(pos).getUser_no());
                    Log.d(TAG, "getUser_email: " + users.get(pos).getUser_email());
                    Log.d(TAG, "getUser_nickName: " + users.get(pos).getUser_nickname());
                    Log.d(TAG, "getUser_img_fileName: " + users.get(pos).getUser_img_filename());
                    Log.d(TAG, "partner_orNot: " + users.get(pos).getExtra());

                    follow.setClickable(false);
                    /** 서버 통신 - 파트너 맺기 */
                    RetrofitService rs = ServiceGenerator.createService(RetrofitService.class);
                    Call<ResponseBody> follow_result = rs.become_a_partner(
                            Static.BECOME_A_PARTNER,
                            myapp.getUser_no(), users.get(pos).getUser_no());
                    follow_result.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            try {
                                String retrofit_result = response.body().string();
                                Log.d(TAG, "retrofit_result: "+retrofit_result);

                                if(retrofit_result.equals("success")) {
                                    follow.setClickable(true);
                                    users.get(pos).setExtra("partner");
                                    notifyItemChanged(pos);
                                }
                                else if(retrofit_result.equals("fail")) {
                                    myapp.logAndToast("예외발생: " + retrofit_result);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            myapp.logAndToast("onFailure_result" + t.getMessage());
                        }
                    });

                }
            });

            /** 파트너 끊기 */
            unFollow.setClickable(true);
            unFollow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();

                    Log.d(TAG, "클릭 아이템 position: " + pos);
                    Log.d(TAG, "getUser_no: " + users.get(pos).getUser_no());
                    Log.d(TAG, "getUser_email: " + users.get(pos).getUser_email());
                    Log.d(TAG, "getUser_nickName: " + users.get(pos).getUser_nickname());
                    Log.d(TAG, "getUser_img_fileName: " + users.get(pos).getUser_img_filename());
                    Log.d(TAG, "partner_orNot: " + users.get(pos).getExtra());

                    // 프로필 상세보기 다이얼로그 액티비티 열기
                    Intent intent = new Intent(context, Confirm_subtract_partner_D.class);
                    intent.putExtra("position", pos);
                    intent.putExtra("target_user_no", users.get(pos).getUser_no());
                    ((Search_partner)context).startActivityForResult(intent, Search_partner.REQUEST_SUBTRACT_CONFIRM);
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
        String partner_orNot = users.get(position).getExtra();
        Log.d(TAG, "nickName: " + nickName);
        Log.d(TAG, "email: " + email);
        Log.d(TAG, "img_fileName: " + img_fileName);
        Log.d(TAG, "partner_orNot: " + partner_orNot);

        // 닉네임, 이메일 셋팅
        holder.nickName.setText(nickName);
        holder.email.setText(email);

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

        // 내 파트너인지 아닌지 표시
        if(partner_orNot.equals("partner")) {
            holder.follow.setVisibility(View.GONE);
            holder.unFollow.setVisibility(View.VISIBLE);
        }
        else {
            holder.unFollow.setVisibility(View.GONE);
            holder.follow.setVisibility(View.VISIBLE);
        }
    }

    /** getItemCount => arr 사이즈 리턴 */
    @Override
    public int getItemCount() {
        return users.size();
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 서버 통신 -- 서버로부터 받아온 검색결과로 arrayList 교체하기
     ---------------------------------------------------------------------------*/
    public void refresh_arr(ArrayList<Users> users) {
        this.users.clear();
        this.users = users;
        notifyDataSetChanged();
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 파트너 끊기로 한 대상의 position
     ---------------------------------------------------------------------------*/
    public void break_partner_reflect(final int position, String target_user_no) {
        /** 서버 통신 - 파트너 끊기 */
        RetrofitService rs = ServiceGenerator.createService(RetrofitService.class);
        Call<ResponseBody> unFollow_result = rs.break_partner(
                Static.BREAK_PARTNER,
                myapp.getUser_no(), target_user_no);
        unFollow_result.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String retrofit_result = response.body().string();
                    Log.d(TAG, "retrofit_result: "+retrofit_result);

                    if(retrofit_result.equals("success")) {
                        users.get(position).setExtra("");
                        notifyItemChanged(position);
                    }
                    else if(retrofit_result.equals("fail")) {
                        myapp.logAndToast("예외발생: " + retrofit_result);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                myapp.logAndToast("onFailure_result" + t.getMessage());
            }
        });
    }

}
