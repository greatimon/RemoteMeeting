package com.example.jyn.remotemeeting.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.jyn.remotemeeting.DataClass.File_info;
import com.example.jyn.remotemeeting.Dialog.Meeting_result_D;
import com.example.jyn.remotemeeting.Dialog.Show_one_image_D;
import com.example.jyn.remotemeeting.Etc.Static;
import com.example.jyn.remotemeeting.R;
import com.example.jyn.remotemeeting.Util.Myapp;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by JYN on 2017-11-17.
 */

public class RCV_show_drawing_images_adapter extends RecyclerView.Adapter<RCV_show_drawing_images_adapter.ViewHolder> {

    private Context context;
    private int itemLayout;
    private ArrayList<String> drawing_images_fileName_arr;
    public static String TAG = "all_"+RCV_show_drawing_images_adapter.class.getSimpleName();
    public Myapp myapp;

    /** RecyclerAdapter 생성자 1. */
    public RCV_show_drawing_images_adapter(
            Context context, int itemLayout, ArrayList<String> drawing_images_fileName_arr) {
        Log.d(TAG, "ViewHolder_ RCV_show_drawing_images_adapter: 생성");
        this.context = context;
        this.itemLayout = itemLayout;
        this.drawing_images_fileName_arr = drawing_images_fileName_arr;
        // 어플리케이션 객체 생성
        myapp = Myapp.getInstance();
    }

    /** 뷰홀더 */
    public class ViewHolder extends RecyclerView.ViewHolder {

        /**
            인플레이팅 되는 레아아웃을 RCV_show_uploaded_images_adapter.class 와 같이 쓰다보니
            이미지뷰의 id 이름이 'uploaded_img' 임.
            실제로 여기서 다루는 이미지는, 로컬에 저장되어 있는 이미지임
         */
        @BindView(R.id.uploaded_img)                ImageView drawing_img;

        public ViewHolder(View itemView) {
            super(itemView);
            Log.d(TAG, "ViewHolder");
            ButterKnife.bind(this,itemView);

            /** 아이템 클릭 이벤트 설정 */
            drawing_img.setClickable(true);
            drawing_img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // TODO: redis - 클릭이벤트
                    myapp.Redis_log_click_event(getClass().getSimpleName(), v);

                    int pos = getAdapterPosition();

                    Log.d(TAG, "클릭 아이템 position: " + pos);
                    Log.d(TAG, "getFile_no: " + drawing_images_fileName_arr.get(pos));

                    // TODO: redis - 화면 이동
                    myapp.Redis_log_view_crossOver_from_to(
                            getClass().getSimpleName(), Show_one_image_D.class.getSimpleName());

                    Intent intent = new Intent(context, Show_one_image_D.class);
                    intent.putExtra("image_source", drawing_images_fileName_arr.get(pos));
                    intent.putExtra("fileName", drawing_images_fileName_arr.get(pos));
                    intent.putExtra(Static.REQUEST_CLASS, getClass().getSimpleName());
                    ((Meeting_result_D)context).startActivityForResult(intent, Meeting_result_D.REQUEST_SHOW_THIS_IMAGE);
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

        // 해당 파일이 실제로 존재하는지 확인
        File f = new File(drawing_images_fileName_arr.get(position));
        if(f.isFile()) {
            Glide
                    .with(context)
                    .load(drawing_images_fileName_arr.get(position))
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(holder.drawing_img);
        }
    }

    @Override
    public int getItemCount() {
        return drawing_images_fileName_arr.size();
    }
}
