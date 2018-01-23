package com.example.jyn.remotemeeting.Adapter;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
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
import com.example.jyn.remotemeeting.DataClass.Project;
import com.example.jyn.remotemeeting.Dialog.Assign_to_existing_project_D;
import com.example.jyn.remotemeeting.R;
import com.example.jyn.remotemeeting.Util.Myapp;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by JYN on 2018-01-23.
 */

public class RCV_project_adapter_for_assign_project extends RecyclerView.Adapter<RCV_project_adapter_for_assign_project.ViewHolder> {

    private Context context;
    private int itemLayout;
    private ArrayList<Project> project_arr;
    public static String TAG = "all_" + RCV_project_adapter_for_assign_project.class.getSimpleName();
    Myapp myapp;


    /** RecyclerAdapter 생성자 */
    public RCV_project_adapter_for_assign_project(
            Context context, int itemLayout, ArrayList<Project> project_arr) {
        Log.d(TAG, "RCV_project_adapter_for_assign_project: 생성");
        this.context = context;
        this.itemLayout = itemLayout;

        /** project_arr.remove(0) */
        // 'index=0' ==> 'project_no' 가 '0'인 경우
        // 'project_no==0' 인 경우는 어떤 경우냐면,
        // 'Project_F.class' 에서 이 ArrayList<Project>를 사용할 때,
        // 프로젝트가 지정되지 않은 회의결과들을 임의의 가상 폴더로 지정하여,
        // 지정되지 않은 영상회의 결과의 개수를 사용자에게 보여주기 위해서 임의로 add 한 아이템이므로,
        // 이 리사이클러뷰에서는 필요 없는 아이템이므로, 제거하도록 한다
        project_arr.remove(0);
        this.project_arr = project_arr;

        // 어플리케이션 객체 생성
        myapp = Myapp.getInstance();

        Log.d(TAG, "RCV_project_adapter_ project_arr.size(): " + project_arr.size());
    }

    /** 뷰홀더 */
    class ViewHolder extends RecyclerView.ViewHolder {

        /** 버터나이프*/
        @BindView(R.id.container)               LinearLayout container;
        @BindView(R.id.project_folder_img)      ImageView project_folder_img;
        @BindView(R.id.project_name_txt)        TextView project_name_txt;

        ViewHolder(View itemView) {
            super(itemView);
            Log.d(TAG, "ViewHolder");
            ButterKnife.bind(this,itemView);

            /** 아이템 클릭 이벤트 설정 */
            container.setClickable(true);
            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    Log.d(TAG, "클릭 아이템 position: " + pos);
                    Log.d(TAG, "project_arr.get(pos).getProject_no(): " + project_arr.get(pos).getProject_no());
                    Log.d(TAG, "project_arr.get(pos).getProject_no(): " + project_arr.get(pos).getProject_color());
                    Log.d(TAG, "project_arr.get(pos).getProject_no(): " + project_arr.get(pos).getProject_name());

                    // Message 객체에 넣을 bundle 객체 생성
                    Bundle bundle = new Bundle();
                    // bundle 객체에 'selected_project_no' 변수 담기
                    // bundle 객체에 'selected_project_color' 변수 담기
                    // bundle 객체에 'selected_project_name' 변수 담기
                    bundle.putInt("selected_project_no", project_arr.get(pos).getProject_no());
                    bundle.putString("selected_project_color", project_arr.get(pos).getProject_color());
                    bundle.putString("selected_project_name", project_arr.get(pos).getProject_name());

                    // 핸들러로 전달할 Message 객체 생성
                    Message msg = Assign_to_existing_project_D.receive_project_no_from_adapter_handler.obtainMessage();
                    // bundle 객체 넣기
                    msg.setData(bundle);
                    // 핸들러에서 Message 객체 구분을 위한 'what' 값 설정
                    msg.what = 0;

                    // 핸들러로 Message 객체 전달
                    Assign_to_existing_project_D.receive_project_no_from_adapter_handler.sendMessage(msg);
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

        int project_no = project_arr.get(position).getProject_no();
        String project_color = project_arr.get(position).getProject_color();
        String project_name = project_arr.get(position).getProject_name();
        Log.d(TAG, "project_no: " + project_no);
        Log.d(TAG, "project_color: " + project_color);
        Log.d(TAG, "project_name: " + project_name);

        // 폴더 컬러 설정
        Glide
                .with(context)
                .load(myapp.getFolder_color_hash().get(project_color))
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(holder.project_folder_img);

        // 프로젝트 이름 설정
        holder.project_name_txt.setText(project_name);

    }

    /** getItemCount => arr 사이즈 리턴 */
    @Override
    public int getItemCount() {
        return project_arr.size();
    }
}
