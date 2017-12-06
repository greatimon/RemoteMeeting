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

import com.example.jyn.remotemeeting.Activity.Call_A;
import com.example.jyn.remotemeeting.Dialog.Register_file_to_project_D;
import com.example.jyn.remotemeeting.DataClass.File_info;
import com.example.jyn.remotemeeting.Fragment.Call_F;
import com.example.jyn.remotemeeting.R;
import com.example.jyn.remotemeeting.Util.Myapp;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by JYN on 2017-11-17.
 */

public class RCV_call_adapter extends RecyclerView.Adapter<RCV_call_adapter.ViewHolder> {

    private Context context;
    private int itemLayout;
    private String request;
    private ArrayList<File_info> files;
    public static String TAG = "all_"+RCV_call_adapter.class.getSimpleName();
//    private static HashMap<String, String> checked_files;
    public Myapp myapp;

    /** RecyclerAdapter 생성자 */
    public RCV_call_adapter(Context context, int itemLayout, ArrayList<File_info> files, String request) {
        Log.d(TAG, "ViewHolder_ RCV_call_adapter: 생성");
        Log.d(TAG, "request: " + request);
        this.context = context;
        this.itemLayout = itemLayout;
        this.files = files;
        this.request = request;

        // 어플리케이션 객체 생성
        myapp = Myapp.getInstance();

        // 체크된 파일들의 canonicalPath 를 담을 Hashmap 생성
//        checked_files = new HashMap<>();
    }

    /** 뷰홀더 */
    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.file_img)                ImageView file_img;
        @BindView(R.id.file_name)               TextView file_name;
        @BindView(R.id.list_item_container)     LinearLayout list_item_container;
        @BindView(R.id.check_mark)              ImageView check_mark;

        public ViewHolder(View itemView, int itemLayout, final String request) {
            super(itemView);
            Log.d(TAG, "ViewHolder");
            Log.d(TAG, "request: "+ request);
            ButterKnife.bind(this,itemView);


            /** 아이템 클릭 이벤트 설정 */
            list_item_container.setClickable(true);
            list_item_container.setOnClickListener(new View.OnClickListener() {
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

                    /** '파일 추가' 버튼인지 확인하기 */
                    String file_format = files.get(pos).getFile_format();
                    // '파일 추가 버튼일 때
                    if(file_format.equals("zero")) {
                        // 추가할 파일의 종류 선택하는 다이얼로그 액티비티 열기
                        Intent intent = new Intent(context, Register_file_to_project_D.class);
                        ((Call_A)context).startActivityForResult(intent, Call_A.REQUEST_GET_LOCAL_FILE);
                    }
                    // 파일 추가 버튼이 아닐 때
                    else if(!file_format.equals("zero")) {
                        // 체크된 파일인지 아닌지 확인
                        String checked_orNot = files.get(pos).getExtra();
                        // 파일 이름 확인 - project / local 인지에 따라 다른 변수값 가져옴
                        String file_name = "";
                        String CanonicalPath = "";

                        // 프로젝트 뷰 일 때 - 파일전환할 파일을 담는다
                        if(request.equals("project")) {
                            file_name = files.get(pos).getFile_name();
                        }
                        // 로컬 뷰 일 때 - 업로드할 파일을 담는다
                        else if(!request.equals("project")) {
                            file_name = files.get(pos).getFile_name();
                            CanonicalPath = files.get(pos).getCanonicalPath();
                        }

                        // 체크되어 있지 않은 파일일 때 -> checked_files 에 put 하고, check 상태 'yes'로 바꾸기
                        if(checked_orNot.equals("no")) {
                            myapp.getChecked_files().put(file_name, CanonicalPath);
                            files.get(pos).setExtra("yes");
                            notifyItemChanged(pos);
                        }
                        // 체크되어 있는 파일 -> checked_files 에서 remove 하고, check 상태 'no로 바꾸기
                        else if(checked_orNot.equals("yes")) {
                            myapp.getChecked_files().remove(file_name);
                            files.get(pos).setExtra("no");
                            notifyItemChanged(pos);
                        }
                    }

                    // 추가한 파일의 개수에 따라 'add' 버튼의 이미지 바꾸기
                    if(myapp.getChecked_files().size() > 0) {
                        Call_F.add_files.setImageResource(R.drawable.add_text_1);
                    }
                    else if(myapp.getChecked_files().size()==0) {
                        Call_F.add_files.setImageResource(R.drawable.add_text_2);
                    }
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

        // 파일 포맷 확인
        String fileFormat = files.get(position).getFile_format();
        Log.d(TAG, "fileFormat: " + fileFormat);

        // 파일 이름 확인 - project / local 인지에 따라 다른 변수값 가져옴
        String file_name = files.get(position).getFile_name();
        Log.d(TAG, "file_name: " + file_name);

        // 확장자 뺀 파일 이름만 뽑아오기
        int Idx = file_name.lastIndexOf(".");
        String file_name_except_format = file_name.substring(0, Idx);
        Log.d(TAG, "fileFormat: " + fileFormat);
        Log.d(TAG, "fileName: " + file_name_except_format);

        // '파일추가' 버튼 일 때 이미지 set
        if(fileFormat.equals("zero")) {
            holder.file_img.setImageResource(R.drawable.add_file_2);
        }

        // 각 파일에 따른 포맷별 이미지 set
        if(fileFormat.equals("pdf")) {
            holder.file_img.setImageResource(R.drawable.pdf);
        }
        else if(fileFormat.equals("jpg")) {
            holder.file_img.setImageResource(R.drawable.jpg);
        }
        else if(fileFormat.equals("png")) {
            holder.file_img.setImageResource(R.drawable.png);
        }

        // 파일 이름 set
        holder.file_name.setText(file_name_except_format);

        // 체크 표시 이미지 set
        String check_mark = files.get(position).getExtra();
        if(check_mark.equals("yes")) {
            holder.check_mark.setVisibility(View.VISIBLE);
        }
        else if(check_mark.equals("no")) {
            holder.check_mark.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        if(itemLayout == R.layout.i_file) {
            return files.size();
        }
        else {
            return 0;
        }
    }

    /**---------------------------------------------------------------------------
     메소드 ==> 현재 어댑터의 타입(request) 값을 리턴 -- file 'add' 버튼의 로직을 구분하기 위함
     ---------------------------------------------------------------------------*/
    @Override
    public String toString() {
        return this.request;
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 서버로부터 받아온 공유 파일 리스트로 arrayList 교체하기
     ---------------------------------------------------------------------------*/
    public void refresh_arr(ArrayList<File_info> files, String request) {
        this.files.clear();
        this.files = files;

        // 체크한 파일을 담는 HashMap도 초기화
        myapp.getChecked_files().clear();

        // local adapter 라면, request(파일 포맷종류) 변경
        if(!request.equals("")) {
            this.request = request;
        }
        notifyDataSetChanged();
    }



    /**---------------------------------------------------------------------------
     메소드 ==> item arrayList 의 체크표시를 모두 해제하기
     ---------------------------------------------------------------------------*/
    public void init_check_mark() {
        for(int i=0; i<files.size(); i++) {
            files.get(i).setExtra("no");
        }
        notifyDataSetChanged();
    }
}
