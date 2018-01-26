package com.example.jyn.remotemeeting.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.jyn.remotemeeting.Activity.Call_A;
import com.example.jyn.remotemeeting.DataClass.Preview_share_img_file;
import com.example.jyn.remotemeeting.Etc.Static;
import com.example.jyn.remotemeeting.R;
import com.example.jyn.remotemeeting.Util.Myapp;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by JYN on 2017-12-28.
 */

public class RCV_share_image_adapter
            extends RecyclerView.Adapter<RCV_share_image_adapter.ViewHolder> {

    private Context context;
    private int itemLayout;
    public static String TAG = "all_" + RCV_share_image_adapter.class.getSimpleName();
    public Myapp myapp;
    public static String current_big_share_img_fileName;     // 현재 크게 보여주고 있는 이미지의 파일이름

    public static ArrayList<Preview_share_img_file> share_img_file_arr;

    /** RecyclerAdapter 생성자 */
    public RCV_share_image_adapter(Context context,
                                   int itemLayout,
                                   ArrayList<Preview_share_img_file> share_img_file_arr) {
        Log.d(TAG, "ViewHolder_ RCV_share_image_adapter: 생성");

        this.context = context;
        this.itemLayout = itemLayout;
        this.share_img_file_arr = share_img_file_arr;

        // 어플리케이션 객체 생성
        myapp = Myapp.getInstance();

        notifyDataSetChanged();

        // 미리보기 1번째 이미지 크게 보여주기
        preview_into_big_imageView(context, share_img_file_arr.get(0).getFileName(), 1);
        // 미리보기 1번째 아이템, 선택됨을 알려주는 테두리 넣기
        selected_file_border_managing(0);
    }


    /** 뷰홀더 */
    class ViewHolder extends RecyclerView.ViewHolder {

        /** 버터나이프*/
        @BindView(R.id.preview_img)             ImageView preview_img;
        @BindView(R.id.preview_sequence)        TextView preview_sequence;
        @BindView(R.id.container)               RelativeLayout container;
        @BindView(R.id.border_view)             View border_view;

        ViewHolder(View itemView) {
            super(itemView);
            Log.d(TAG, "ViewHolder");
            ButterKnife.bind(this, itemView);

            container.setClickable(true);
            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // TODO: redis - 클릭이벤트
                    myapp.Redis_log_click_event(getClass().getSimpleName(), v);

                    int pos = getAdapterPosition();
                    Log.d(TAG, "클릭 아이템 position: " + pos);
                    Log.d(TAG, "클릭 아이템 파일이름: " + share_img_file_arr.get(pos).getFileName());

                    Call_A.got_reset_order_from_rcv_adapter = true;

                    // 클릭한 해당 이미지, 리사이클러뷰 왼쪽에 프리뷰 화면에 크게 보여주게 하는 메소드 호출
                    preview_into_big_imageView(context, share_img_file_arr.get(pos).getFileName(), pos+1);

                    // 클릭한 해당 이미지, 리사이클러뷰 아이템 선택됨을 알려주는 테두리 넣기
                    selected_file_border_managing(pos);
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

        if(holder.getAdapterPosition() != RecyclerView.NO_POSITION) {
            // 아이템의 index 값
            int position = holder.getAdapterPosition();

            String file_URL = share_img_file_arr.get(position).getFileName();
//            Log.d(TAG, "file_URL: " + file_URL);
//            Log.d(TAG, "selected_file_arr.size(): " + selected_file_arr.size());

            // 파일 순서 넘버링 set
            holder.preview_sequence.setText(String.valueOf(position+1));
            // 해당 파일 이미지 set
            Glide
                .with(context)
                .load(Static.SERVER_URL_MEETING_UPLOAD_FILE_FOLDER + file_URL)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(holder.preview_img);

            // 크게 미리보기 선택한 파일 테두리(background XML) set
            if(share_img_file_arr.get(position).isSelected()) {
                holder.border_view.setBackgroundResource(R.drawable.shape_selected_file);
            }
            else if(!share_img_file_arr.get(position).isSelected()) {
                holder.border_view.setBackgroundResource(0);
            }

        }
    }

    /** getItemCount => 리사이클러뷰 아이템 arr 사이즈 리턴 */
    @Override
    public int getItemCount() {
        return share_img_file_arr.size();
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 미리보기 파일 리스트 변경
     ---------------------------------------------------------------------------*/
    public void refresh_arr(ArrayList<Preview_share_img_file> share_img_file_arr) {
        this.share_img_file_arr = share_img_file_arr;
        notifyDataSetChanged();

        Call_A.got_reset_order_from_rcv_adapter = true;

        // 미리보기 1번째 이미지 크게 보여주기
        preview_into_big_imageView(context, share_img_file_arr.get(0).getFileName(), 1);
        // 미리보기 1번째 아이템, 선택됨을 알려주는 테두리 넣기
        selected_file_border_managing(0);
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 파일 미리보기에서, 리사이클러뷰에서 아이템 클릭했을 때, 크게 보여주기
     ---------------------------------------------------------------------------*/
    @SuppressLint("SetTextI18n")
    private void preview_into_big_imageView(Context context, String file_URL, int num) {
        // 이미지 set
//        Glide
//            .with(context)
//            .load(Static.SERVER_URL_MEETING_UPLOAD_FILE_FOLDER + file_URL)
//            .fitCenter()
//            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
//            .into(Call_F.preview_display);

        // 현재 크게 보여주고 있는 이미지의 파일 이름 변수에 넣어놓기
        current_big_share_img_fileName = file_URL;

        // 핸들러로 전달할 Message 객체 생성
        Message msg = Call_A.call_draw_handler.obtainMessage();

        // Message 객체에 넣을 bundle 객체 생성
        Bundle bundle = new Bundle();
        // bundle 객체에 'fileName' 변수 담기
        bundle.putString("fileName", current_big_share_img_fileName);
        // Message 객체에 bundle, 'data' 변수 담기
        msg.setData(bundle);
        // 핸들러에서 Message 객체 구분을 위한 'what' 값 설정
        msg.what = 6;
        // 핸들러로 Message 객체 전달
        Call_A.call_draw_handler.sendMessage(msg);

    }


    /**---------------------------------------------------------------------------
     메소드 ==> 크게 미리보기 파일 선택 했을 때,
                이미 선택되어 있는 아이템은 'false' set
                새로 선택한 아이템을 선택 'true' set
     ---------------------------------------------------------------------------*/
    private void selected_file_border_managing(int position) {
        for(int i=0; i<getItemCount(); i++) {
            if(share_img_file_arr.get(i).isSelected()) {
                share_img_file_arr.get(i).setSelected(false);
                notifyItemChanged(i);
                Log.d(TAG, "이전에 선택되어 있던 아이템의 파일 인덱스번호: " + (i));
            }
        }
        share_img_file_arr.get(position).setSelected(true);
        notifyItemChanged(position);
        Log.d(TAG, "크게 미리보기에 선택된 아이템의 파일 인덱스 번호: " + position);
    }


}
