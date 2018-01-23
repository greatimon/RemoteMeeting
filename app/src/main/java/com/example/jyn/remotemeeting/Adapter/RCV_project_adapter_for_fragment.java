package com.example.jyn.remotemeeting.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.jyn.remotemeeting.Activity.Main_after_login_A;
import com.example.jyn.remotemeeting.Activity.Project_meeting_result_list_A;
import com.example.jyn.remotemeeting.Activity.Search_partner_A;
import com.example.jyn.remotemeeting.DataClass.Project;
import com.example.jyn.remotemeeting.R;
import com.example.jyn.remotemeeting.Util.Myapp;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by JYN on 2018-01-19.
 */

public class RCV_project_adapter_for_fragment extends RecyclerView.Adapter<RCV_project_adapter_for_fragment.ViewHolder> {

    private Context context;
    private int itemLayout;
    private String request;
    private ArrayList<Project> project_arr;
    public static String TAG = "all_" + RCV_project_adapter_for_fragment.class.getSimpleName();
    Myapp myapp;

    /** RecyclerAdapter 생성자 */
    public RCV_project_adapter_for_fragment(
            Context context, int itemLayout, ArrayList<Project> project_arr, final String request) {
        Log.d(TAG, "RCV_project_adapter_for_fragment: 생성");
        this.context = context;
        this.itemLayout = itemLayout;
        this.project_arr = project_arr;
        this.request = request;

        // 어플리케이션 객체 생성
        myapp = Myapp.getInstance();

        Log.d(TAG, "RCV_project_adapter_ project_arr.size(): " + project_arr.size());
    }


    /** 뷰홀더 */
    class ViewHolder extends RecyclerView.ViewHolder {

        /** 버터나이프*/
        @BindView(R.id.container_CONS)          ConstraintLayout container_CONS;
        @BindView(R.id.project_folder_img)      ImageView project_folder_img;
        @BindView(R.id.lock_img)                ImageView lock_img;
        @BindView(R.id.created_date_txt)        TextView created_date_txt;
        @BindView(R.id.meeting_nums)            TextView meeting_nums;
        @BindView(R.id.project_name_txt)        TextView project_name_txt;
        @BindView(R.id.project_state_txt)       TextView project_state_txt;
        @BindView(R.id.when_project_not_being_locked)
        View when_project_not_being_locked;

        ViewHolder(View itemView) {
            super(itemView);
            Log.d(TAG, "ViewHolder");
            ButterKnife.bind(this,itemView);

            container_CONS.setClickable(true);
            container_CONS.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    Log.d(TAG, "클릭 아이템 position: " + pos);
                    Log.d(TAG, "project_arr.get(pos).getProject_no(): " + project_arr.get(pos).getProject_no());

                    // 해당 프로젝트에 지정된 영상회의 목록 보는 액티비티 열기
                    Intent intent = new Intent(context, Project_meeting_result_list_A.class);
                    intent.putExtra("project_no", project_arr.get(pos).getProject_no());
                    ((Main_after_login_A)context).startActivityForResult(intent, Main_after_login_A.REQUEST_PROJECT_MEETING_RESULT_LIST);
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
        String project_start_dt = project_arr.get(position).getProject_start_dt();
        int meeting_count = project_arr.get(position).getMeeting_count();
        String project_name = project_arr.get(position).getProject_name();
        String project_status = project_arr.get(position).getProject_status();
        boolean project_lock = project_arr.get(position).isProject_lock();
        int project_pw = project_arr.get(position).getProject_pw();
        Log.d(TAG, "project_color: " + project_color);
        Log.d(TAG, "project_start_dt: " + project_start_dt);
        Log.d(TAG, "meeting_count: " + meeting_count);
        Log.d(TAG, "project_name: " + project_name);
        Log.d(TAG, "project_status: " + project_status);
        Log.d(TAG, "project_lock: " + project_lock);
        Log.d(TAG, "project_pw: " + project_pw);

        // 서버로부터 받아온 '회의 개수'에 특정 String 값 붙이기
        String meeting_count_str = "회의 개수: " + String.valueOf(meeting_count);

        // 서버로부터 받아온 start_dt에서 년도 앞 2자리 제거하기
        if(project_start_dt != null) {
            project_start_dt = project_start_dt.substring(2);
        }
//        Log.d(TAG, "substring_ project_start_dt: " + project_start_dt);

        // 프로젝트 지정이 되지 않은 회의 개수를 담고 있는 가상의 프로젝트인 경우의 데이터값 설정
        if(project_no == 0) {
            project_color = "grey";
            project_start_dt = "";
            project_name = "프로젝트 미지정\n회의목록";
            project_status = "";
            project_lock = false;
        }

        // 폴더 컬러 설정
        Glide
            .with(context)
            .load(myapp.getFolder_color_hash().get(project_color))
            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
            .into(holder.project_folder_img);

        // 프로젝트 시작 날짜 설정
        holder.created_date_txt.setText(project_start_dt);

        // 프로젝트 회의 개수 설정
        holder.meeting_nums.setText(String.valueOf(meeting_count_str));

        // 프로젝트 이름 설정
        holder.project_name_txt.setText(project_name);

        /** '프로젝트 상태' - 사용하지 않기로 함 */
//        // 프로젝트 상태 설정
//        holder.project_state_txt.setText(project_status);
//        // 프로젝트 상태에 따른 백그라운드 drawable 설정
//        if(project_status.equals("진행중")) {
//            holder.project_state_txt.setBackgroundResource(R.drawable.shape_project_in_progress);
//        }
//        else if(project_status.equals("완료")) {
//            holder.project_state_txt.setBackgroundResource(R.drawable.shape_project_end);
//        }
//        else if(project_status.equals("보류")) {
//            holder.project_state_txt.setBackgroundResource(R.drawable.shape_project_hold);
//        }

        // 프로젝트 잠금여부 표시 설정
        if(project_lock) {
            holder.lock_img.setVisibility(View.VISIBLE);
            holder.when_project_not_being_locked.setVisibility(View.GONE);
        }
        else if(!project_lock) {
            holder.lock_img.setVisibility(View.GONE);
            holder.when_project_not_being_locked.setVisibility(View.VISIBLE);
        }

        // 프로젝트 지정이 되지 않은 회의 개수를 담고 있는 가상의 프로젝트인 경우,
        if(project_no == 0) {
            // 프로젝트 진행 상황은 보여주지 않는다
            holder.project_state_txt.setVisibility(View.GONE);
            // 프로젝트 title, 폰트를 크게 한다
            holder.project_name_txt.setTextSize(15);
            // 프로젝트 title, 줄간격을 좀 넓힌다
            holder.project_name_txt.setLineSpacing(0, 1.2f);
        }
    }


    /** getItemCount => arr 사이즈 리턴 */
    @Override
    public int getItemCount() {
        return project_arr.size();
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 서버로부터 받아온 내 프로젝트 리스트로 ArrayList 교체하기
     ---------------------------------------------------------------------------*/
    public void refresh_arr(ArrayList<Project> project_arr) {
        this.project_arr.clear();
        this.project_arr = project_arr;
        notifyDataSetChanged();
    }

}
