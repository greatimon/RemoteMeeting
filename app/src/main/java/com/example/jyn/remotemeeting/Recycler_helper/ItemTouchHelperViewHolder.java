package com.example.jyn.remotemeeting.Recycler_helper;

/**
 * Created by JYN on 2017-12-29.
 */

import android.support.v7.widget.helper.ItemTouchHelper;

/** 리사이클러뷰 아이템의 '선택, 삭제(스와이프)'를 알리는 인터페이스 관련 콜백 */
public interface ItemTouchHelperViewHolder {

    /**
     * Called when the {@link ItemTouchHelper} first registers an item as being moved or swiped.
     * Implementations should update the item view to indicate it's active state.
     */
    void onItemSelected();

    /**
     * Called when the {@link ItemTouchHelper} has completed the move or swipe, and the active item
     * state should be cleared.
     */
    void onItemClear();
}
