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
import com.example.jyn.remotemeeting.DataClass.Preview_selected_file;
import com.example.jyn.remotemeeting.Etc.Static;
import com.example.jyn.remotemeeting.Fragment.Call_F;
import com.example.jyn.remotemeeting.R;
import com.example.jyn.remotemeeting.Recycler_helper.ItemTouchHelperAdapter;
import com.example.jyn.remotemeeting.Recycler_helper.ItemTouchHelperViewHolder;
import com.example.jyn.remotemeeting.Recycler_helper.OnStartDragListener;
import com.example.jyn.remotemeeting.Util.Myapp;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by JYN on 2017-12-28.
 */

public class RCV_selectFile_preview_adapter
            extends RecyclerView.Adapter<RCV_selectFile_preview_adapter.ViewHolder>
            implements ItemTouchHelperAdapter {

    private Context context;
    private int itemLayout;
    private String mode;
    public static String TAG = "all_"+RCV_selectFile_preview_adapter.class.getSimpleName();
    public Myapp myapp;
    private String current_big_preview_fileName;     // 현재 크게 보여주고 있는 이미지의 파일이름
    private int on_moving_index = -1;


    public ArrayList<Preview_selected_file> selected_file_arr;

    // 리사이클러뷰 아이템이 드래그가 발생했을 때, 콜백되는 리스너
    private final OnStartDragListener onStartDragListener;

    /** RecyclerAdapter 생성자 */
    public RCV_selectFile_preview_adapter(
            Context context, int itemLayout, ArrayList<Preview_selected_file> selected_file_arr, String mode
            , OnStartDragListener dragStartListener) {
        Log.d(TAG, "ViewHolder_ RCV_selectFile_preview_adapter: 생성");
        Log.d(TAG, "mode: " + mode);

        this.context = context;
        this.itemLayout = itemLayout;
        this.selected_file_arr = selected_file_arr;
        this.mode = mode;
        this.onStartDragListener = dragStartListener;

        // 어플리케이션 객체 생성
        myapp = Myapp.getInstance();

        notifyDataSetChanged();

        // 미리보기 1번째 이미지 크게 보여주기
        preview_into_big_imageView(context, selected_file_arr.get(0).getFileName(), 1);
        // 미리보기 1번째 아이템, 선택됨을 알려주는 테두리 넣기
        selected_file_border_managing(0);

        // 커스텀 로거 생성
        Logger.clearLogAdapters();
        Logger.addLogAdapter(new AndroidLogAdapter(myapp.custom_log(RCV_selectFile_preview_adapter.class)));
    }


    /** 뷰홀더 */
    class ViewHolder extends RecyclerView.ViewHolder
                     implements ItemTouchHelperViewHolder {

        /** 버터나이프*/
        @BindView(R.id.preview_img)             ImageView preview_img;
        @BindView(R.id.preview_sequence)        TextView preview_sequence;
        @BindView(R.id.container)               RelativeLayout container;
        @BindView(R.id.border_view)             View border_view;

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
                    preview_into_big_imageView(context, selected_file_arr.get(pos).getFileName(), pos+1);

                    // 클릭한 해당 이미지, 리사이클러뷰 아이템 선택됨을 알려주는 테두리 넣기
                    selected_file_border_managing(pos);
                }
            });
        }

        /** 아이템이 롱클릭 및, 스와이프되어 선택됐을 때 콜백되는 메소드 */
        @Override
        public void onItemSelected() {
            // 롱클릭 선택되었을 때, 보여져야 하는 백그라운드 xml 적용
            border_view.setBackgroundResource(R.drawable.shape_file_long_clicked);
            // 롱클릭하여 움직일 아이템의 최초 index 확인
            on_moving_index = getAdapterPosition();
            Logger.d("on_moving_index_at_first " + on_moving_index);
        }

        /** 아이템이 선택해제 됐을 때 콜백되는 메소드*/
        @Override
        public void onItemClear() {
            // 이동아이템 인덱스 초기화
            on_moving_index = -1;

            int pos = getAdapterPosition();
            // 롱클릭 선택해제 되었으므로, 백그라운드 xml도 해제
            border_view.setBackgroundResource(0);
            // selected_file_arr.get(pos).isSelected()에 따른 border_view 백그라운드 xml 다시 적용
            notifyItemChanged(pos);

//            // 롱클릭하여 움직이는 아이템의 이동 index 확인
//            on_moving_index = pos;
//            Logger.d("on_moving_index_onItemClear: " + on_moving_index);

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

            String file_URL = selected_file_arr.get(position).getFileName();
//            Log.d(TAG, "file_URL: " + file_URL);
//            Log.d(TAG, "selected_file_arr.size(): " + selected_file_arr.size());

            for(int i=0; i<getItemCount(); i++) {
//                Log.d(TAG, "selected_file_arr(" + i + "): "+selected_file_arr.get(i).getFileName());
            }

            // 파일 순서 넘버링 set
            holder.preview_sequence.setText(String.valueOf(position+1));
            // 해당 파일 이미지 set
            Glide
                .with(context)
                .load(Static.SERVER_URL_MEETING_UPLOAD_FILE_FOLDER + file_URL)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(holder.preview_img);

            // 아이템이 드래그로 인해서 움직이고 있는 상황이고,
            // 움직인 index가 position과 일치한다면, 롱클릭된 상태를 나타내는 border 적용
            if(on_moving_index == position) {
                holder.border_view.setBackgroundResource(R.drawable.shape_file_long_clicked);
            }
            // 아이템이 드래그로 인해서 움직이는 상황이 아니라면, 일반적인 border 적용 로직 따라가기
            else if(on_moving_index != position) {
                // 크게 미리보기 선택한 파일 테두리(background XML) set
                if(selected_file_arr.get(position).isSelected()) {
                    holder.border_view.setBackgroundResource(R.drawable.shape_selected_file);
                }
                else if(!selected_file_arr.get(position).isSelected()) {
                    holder.border_view.setBackgroundResource(0);
                }
            }

        }
    }

    /** getItemCount => 리사이클러뷰 아이템 arr 사이즈 리턴 */
    @Override
    public int getItemCount() {
        return selected_file_arr.size();
    }


    /**---------------------------------------------------------------------------
     콜백메소드 ==> 아이템을 드래그로 이동했을 때 콜백되는 메소드
     ---------------------------------------------------------------------------*/
    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Log.d(TAG, "fromPosition: " + fromPosition);
        Log.d(TAG, "toPosition: " + toPosition);

        // 롱클릭하여 움직이는 아이템의 이동 index 확인
        on_moving_index = toPosition;
        Logger.d("on_moving_index_toPosition: " + on_moving_index);

        Collections.swap(selected_file_arr, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        // 아이템이 드래그되어 위치가 바뀔때마다, 파일 순서 넘버링을 바꿔주기 위한 notifyItemChanged
        notifyItemChanged(fromPosition);
        notifyItemChanged(toPosition);

        // 드래그 될 때, 현재 선택되어 있는 파일의 움직임에 따라, 미리보기 파일 numeric을 변경해준다
        if(selected_file_arr.get(toPosition).isSelected()) {
            Call_F.file_box_title.setText(Html.fromHtml(
                    "미리보기 - [ <b><font color='#4CAF50'>" + (toPosition+1) + "</font><b/> ]"));
        }
        else if(selected_file_arr.get(fromPosition).isSelected()) {
            Call_F.file_box_title.setText(Html.fromHtml(
                    "미리보기 - [ <b><font color='#4CAF50'>" + (fromPosition+1) + "</font><b/> ]"));
        }
        Log.d(TAG, "to_position.isSelected(): " + selected_file_arr.get(toPosition).isSelected());
        Log.d(TAG, "from_position.isSelected(): " + selected_file_arr.get(fromPosition).isSelected());

        return true;
    }


    /**---------------------------------------------------------------------------
     콜백메소드 ==> 아이템을 스와이프로 제거했을 때 콜백되는 메소드
     ---------------------------------------------------------------------------*/
    @SuppressLint("SetTextI18n")
    @Override
    public void onItemDismiss(int position) {

        // 삭제되는 파일의 이름
        String fileName_to_remove = selected_file_arr.get(position).getFileName();
        // 삭제되는 파일의 arr 인덱스
        int file_index_to_remove = position;

        // 이동아이템 인덱스 초기화
        on_moving_index = -1;

        // 일단, 해당 파일 arr 에서 삭제
        selected_file_arr.remove(position);
        notifyItemRemoved(position);

        //// 현재 크게 미리보기에 set 되어있는 이미지 파일이름과 비교
        // 제거 아이템이 크게 미리보기에 set 되어 있다면,
        if(current_big_preview_fileName.equals(fileName_to_remove)) {
            // 현재 남아있는 아이템의 개수가 2개이상이라면
            if(getItemCount() > 1) {
                // 제거된 아이템의 인덱스가 '0'이라면
                if(file_index_to_remove == 0) {
                    preview_into_big_imageView(context, selected_file_arr.get(0).getFileName(), 1);
                    // 미리보기 1번째 아이템, 선택됨을 알려주는 테두리 넣기
                    selected_file_border_managing(0);
                }
                // 제거된 아이템의 인덱스가 0보다 크다면
                else if(file_index_to_remove > 0) {
                    preview_into_big_imageView(context, selected_file_arr.get(file_index_to_remove-1).getFileName(), file_index_to_remove);
                    // 리사이클러뷰에서 해당 아이템에 '선택됨 테두리' 넣기
                    selected_file_border_managing(file_index_to_remove-1);
                }
            }
            // 현재 남아있는 아이템의 개수가 1개라면
            else if(getItemCount() == 1) {
                preview_into_big_imageView(context, selected_file_arr.get(0).getFileName(), 1);
                // 미리보기 1번째 아이템, 선택됨을 알려주는 테두리 넣기
                selected_file_border_managing(0);
            }
            // 현재 남아있는 아이템이 하나도 없다면
            else if(getItemCount() == 0) {
                // 이미지 set
                Glide
                    .with(context)
                    .load(R.drawable.no_image)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(Call_F.preview_display);
                // title set
                Call_F.file_box_title.setText("미리보기");

                current_big_preview_fileName = "";
            }
        }
        // 제거 아이템이 크게 미리보기에 set 되어 있지 않다면,
        // 해당 미리보기 title 바의 넘버링 변경하기
        else if(!current_big_preview_fileName.equals(fileName_to_remove)) {
            for(int i=0; i<getItemCount(); i++) {
                if(selected_file_arr.get(i).isSelected()) {
                    Call_F.file_box_title.setText(Html.fromHtml(
                            "미리보기 - [ <b><font color='#4CAF50'>" + (i+1) + "</font><b/> ]"));
                }
            }
        }

        // 아이템이 삭제되어 위치가 바뀌었기때문에, 파일 순서 넘버링을 바꿔주기 위한 notifyItemChanged
        for(int i=position; i<getItemCount(); i++) {
            notifyItemChanged(i);
        }
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 미리보기 파일 리스트 변경
     ---------------------------------------------------------------------------*/
    public void refresh_arr(ArrayList<Preview_selected_file> selected_file_arr, String mode) {
        Log.d(TAG, "mode: " + mode);
        this.selected_file_arr = selected_file_arr;
        notifyDataSetChanged();

        // 미리보기 1번째 이미지 크게 보여주기
        preview_into_big_imageView(context, selected_file_arr.get(0).getFileName(), 1);
        // 미리보기 1번째 아이템, 선택됨을 알려주는 테두리 넣기
        selected_file_border_managing(0);
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 파일 미리보기에서, 리사이클러뷰에서 아이템 클릭했을 때, 크게 보여주기
     ---------------------------------------------------------------------------*/
    @SuppressLint("SetTextI18n")
    private void preview_into_big_imageView(Context context, String file_URL, int num) {
        // 이미지 set
        Glide
            .with(context)
            .load(Static.SERVER_URL_MEETING_UPLOAD_FILE_FOLDER + file_URL)
            .fitCenter()
            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
            .into(Call_F.preview_display);
        // title set
        // TODO: 방법 기억 - setText, string 값 부분 색 넣기
        Call_F.file_box_title.setText(Html.fromHtml("미리보기 - [ <b><font color='#4CAF50'>" + num + "</font><b/> ]"));

        // 현재 크게 보여주고 있는 이미지의 파일 이름 변수에 넣어놓기
        // ==> 혹시, 스와이프로 해당 아이템이 삭제되었을 때 크게 보여주고 있는 이미지도 안 보여야하기 때문에
        //     파일이름을 비교하기 위해 파일이름을 변수에 저장함
        current_big_preview_fileName = file_URL;

    }


    /**---------------------------------------------------------------------------
     메소드 ==> 크게 미리보기 파일 선택 했을 때,
                이미 선택되어 있는 아이템은 'false' set
                새로 선택한 아이템을 선택 'true' set
     ---------------------------------------------------------------------------*/
    private void selected_file_border_managing(int position) {
        for(int i=0; i<getItemCount(); i++) {
            if(selected_file_arr.get(i).isSelected()) {
                selected_file_arr.get(i).setSelected(false);
                notifyItemChanged(i);
                Log.d(TAG, "이전에 선택되어 있던 아이템의 파일 인덱스번호: " + (i));
            }
        }
        selected_file_arr.get(position).setSelected(true);
        notifyItemChanged(position);
        Log.d(TAG, "크게 미리보기에 선택된 아이템의 파일 인덱스 번호: " + position);
    }


}
