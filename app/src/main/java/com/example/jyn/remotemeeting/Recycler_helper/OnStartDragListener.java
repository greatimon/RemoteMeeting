package com.example.jyn.remotemeeting.Recycler_helper;

/**
 * Created by JYN on 2017-12-29.
 */

import android.support.v7.widget.RecyclerView;

/** 특정 지정뷰를 클릭해서 드래그가 발생할 때, 콜백되는 리스너 */
public interface OnStartDragListener {
    void onStartDrag(RecyclerView.ViewHolder viewHolder);
}
