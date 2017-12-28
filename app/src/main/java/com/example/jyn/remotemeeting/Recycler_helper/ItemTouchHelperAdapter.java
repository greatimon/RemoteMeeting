package com.example.jyn.remotemeeting.Recycler_helper;

/**
 * Created by JYN on 2017-12-29.
 */


/** 리사이클러뷰의 아이템들이 드래그 되어 움직이거나, 스와이프되어 삭제 될 때 콜백 되는 메소드 정의 */
public interface ItemTouchHelperAdapter {

    boolean onItemMove(int fromPosition, int toPosition);

    void onItemDismiss(int position);

}
