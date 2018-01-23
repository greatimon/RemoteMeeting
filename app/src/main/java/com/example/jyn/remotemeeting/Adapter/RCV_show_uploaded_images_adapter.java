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
import com.example.jyn.remotemeeting.Activity.Call_A;
import com.example.jyn.remotemeeting.DataClass.File_info;
import com.example.jyn.remotemeeting.Dialog.Meeting_result_D;
import com.example.jyn.remotemeeting.Dialog.Show_one_image_D;
import com.example.jyn.remotemeeting.Etc.Static;
import com.example.jyn.remotemeeting.R;
import com.example.jyn.remotemeeting.Util.Myapp;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by JYN on 2017-11-17.
 */

public class RCV_show_uploaded_images_adapter extends RecyclerView.Adapter<RCV_show_uploaded_images_adapter.ViewHolder> {

    private Context context;
    private int itemLayout;
    private String request;
    private ArrayList<File_info> files;
    public static String TAG = "all_"+RCV_show_uploaded_images_adapter.class.getSimpleName();
    public Myapp myapp;

    /** RecyclerAdapter 생성자 1. */
    public RCV_show_uploaded_images_adapter(
            Context context, int itemLayout, ArrayList<File_info> files, String request) {
        Log.d(TAG, "ViewHolder_ RCV_show_uploaded_images_adapter: 생성");
        Log.d(TAG, "request: " + request);
        this.context = context;
        this.itemLayout = itemLayout;
        this.files = files;
        this.request = request;
        // 어플리케이션 객체 생성
        myapp = Myapp.getInstance();
    }

    /** 뷰홀더 */
    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.uploaded_img)                ImageView uploaded_img;

        public ViewHolder(View itemView, int itemLayout, final String request) {
            super(itemView);
            Log.d(TAG, "ViewHolder");
            Log.d(TAG, "request: "+ request);
            ButterKnife.bind(this,itemView);

            /** 아이템 클릭 이벤트 설정 */
            uploaded_img.setClickable(true);
            uploaded_img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();

                    Log.d(TAG, "클릭 아이템 position: " + pos);
                    Log.d(TAG, "getFile_no: " + files.get(pos).getFile_no());
                    Log.d(TAG, "getFile_name: " + files.get(pos).getFile_name());
                    Log.d(TAG, "getCanonicalPath: " + files.get(pos).getCanonicalPath());
                    Log.d(TAG, "getFile_format: " + files.get(pos).getFile_format());
                    Log.d(TAG, "checked_orNot: " + files.get(pos).getExtra());
                    Log.d(TAG, "getMeeting_no: " + files.get(pos).getMeeting_no());

                    Intent intent = new Intent(context, Show_one_image_D.class);
                    intent.putExtra("image_source", Static.SERVER_URL_MEETING_UPLOAD_FILE_FOLDER
                            + files.get(pos).getFile_name());
                    intent.putExtra("fileName", files.get(pos).getFile_name());
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
        ViewHolder viewHolder = new ViewHolder(view, itemLayout, request);

        return viewHolder;
    }

    /** onBindViewHolder => 리스트뷰의 getView 역할 */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder");

        Glide
            .with(context)
            .load(Static.SERVER_URL_MEETING_UPLOAD_FILE_FOLDER
                    + files.get(position).getFile_name())
            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
            .into(holder.uploaded_img);
    }

    @Override
    public int getItemCount() {
        return files.size();
    }
}
