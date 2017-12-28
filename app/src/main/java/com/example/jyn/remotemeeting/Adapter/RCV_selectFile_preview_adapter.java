package com.example.jyn.remotemeeting.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.jyn.remotemeeting.Etc.Static;
import com.example.jyn.remotemeeting.Fragment.Call_F;
import com.example.jyn.remotemeeting.R;
import com.example.jyn.remotemeeting.Util.Myapp;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by JYN on 2017-12-28.
 */

public class RCV_selectFile_preview_adapter
        extends RecyclerView.Adapter<RCV_selectFile_preview_adapter.ViewHolder> {

    private Context context;
    private int itemLayout;
    private String mode;
    public static String TAG = "all_"+RCV_selectFile_preview_adapter.class.getSimpleName();
    public Myapp myapp;

    public ArrayList<String> selected_file_arr;

    /** RecyclerAdapter 생성자 */
    public RCV_selectFile_preview_adapter(
            Context context, int itemLayout, ArrayList<String> selected_file_arr, String mode) {
        Log.d(TAG, "ViewHolder_ RCV_selectFile_preview_adapter: 생성");
        Log.d(TAG, "mode: " + mode);

        this.context = context;
        this.itemLayout = itemLayout;
        this.selected_file_arr = selected_file_arr;
        this.mode = mode;

        // 어플리케이션 객체 생성
        myapp = Myapp.getInstance();

        notifyDataSetChanged();

        // 넘겨받은 selectedFile_name_arr_jsonString 을, selected_file_arr 로 변환하는 메소드 호출
//        into_selected_file_arr();
    }


    /** 뷰홀더 */
    class ViewHolder extends RecyclerView.ViewHolder {

        /** 버터나이프*/
        @BindView(R.id.preview_img)         ImageView preview_img;
        @BindView(R.id.preview_sequence)    TextView preview_sequence;
        @BindView(R.id.container)           RelativeLayout container;

        ViewHolder(View itemView, int itemLayout, final String request) {
            super(itemView);
            Log.d(TAG, "ViewHolder");
            Log.d(TAG, "request: "+ request);
            ButterKnife.bind(this, itemView);

            container.setClickable(true);
            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    Log.d(TAG, "클릭 아이템 position: " + pos);
                    Log.d(TAG, "클릭 아이템 파일이름: " + selected_file_arr.get(pos));

                    // 클릭한 해당 이미지, 리사이클러뷰 왼쪽에 프리뷰 화면에 크게 보여주게 하는 메소드 호출
                    preview_into_big_imageView(context, selected_file_arr.get(pos), pos+1);
                }
            });
        }
    }


    /** onCreateViewHolder => 뷰홀더 생성 - 인플레이팅 */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder");
        View view = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
        ViewHolder viewHolder = new ViewHolder(view, itemLayout, mode);

        return viewHolder;
    }

    /** onBindViewHolder => 리스트뷰의 getView 역할 */
    @Override
    public void onBindViewHolder(ViewHolder holder, int pos) {
        Log.d(TAG, "onBindViewHolder");

        if(holder.getAdapterPosition() != RecyclerView.NO_POSITION) {
            // 아이템의 index 값
            int position = holder.getAdapterPosition();

            String file_URL = selected_file_arr.get(position);
            Log.d(TAG, "file_URL: " + file_URL);
            Log.d(TAG, "selected_file_arr.size(): " + selected_file_arr.size());

            for(int i=0; i<getItemCount(); i++) {
                Log.d(TAG, "selected_file_arr(" + i + "): "+selected_file_arr.get(i));
            }

            // 파일 순서 넘버링 set
            holder.preview_sequence.setText(String.valueOf(position+1));
            // 해당 파일 이미지 set
            Glide
                .with(context)
                .load(Static.SERVER_URL_MEETING_UPLOAD_FILE_FOLDER + file_URL)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(holder.preview_img);
        }
    }

    /** getItemCount => arr 사이즈 리턴 */
    @Override
    public int getItemCount() {
        return selected_file_arr.size();
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 미리보기 파일 리스트 변경
     ---------------------------------------------------------------------------*/
    public void refresh_arr(ArrayList<String>selected_file_arr, String mode) {
        Log.d(TAG, "mode: " + mode);
        this.selected_file_arr = selected_file_arr;
        notifyDataSetChanged();
//        into_selected_file_arr();
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 파일 미리보기에서, 리사이클러뷰에서 아이템 클릭했을 때, 크게 보여주는 것 하기
     ---------------------------------------------------------------------------*/
    @SuppressLint("SetTextI18n")
    public static void preview_into_big_imageView(Context context, String file_URL, int num) {
        // 이미지 set
        Glide
            .with(context)
            .load(Static.SERVER_URL_MEETING_UPLOAD_FILE_FOLDER + file_URL)
                .fitCenter()
            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
            .into(Call_F.preview_display);
        // title set
        Call_F.file_box_title.setText(Html.fromHtml("미리보기 - [<b><font color='#4CAF50'>" + num + "</font><b/>]"));
//        Call_F.file_box_title.setText("미리보기 - [" + num +"]");
    }

}
