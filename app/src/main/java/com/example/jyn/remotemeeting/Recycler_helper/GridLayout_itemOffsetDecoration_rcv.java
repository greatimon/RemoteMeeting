package com.example.jyn.remotemeeting.Recycler_helper;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by JYN on 2018-01-22.
 *
 * Meeting_result_D 클래스에서 '업로드한 이미지, 드로잉한 이미지'를 리사이클러뷰로 나타낼 때
 * 그리드 레이아웃으로 나타내는데, 그 그리드 레이아웃 아이템간 padding을 일정하게 주기 위한 클래스
 */

public class GridLayout_itemOffsetDecoration_rcv extends RecyclerView.ItemDecoration {
    private int mItemOffset;

    public GridLayout_itemOffsetDecoration_rcv(int itemOffset) {
        mItemOffset = itemOffset;
    }

    public GridLayout_itemOffsetDecoration_rcv(@NonNull Context context, @DimenRes int itemOffsetId) {
        this(context.getResources().getDimensionPixelSize(itemOffsetId));
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.set(mItemOffset, mItemOffset, mItemOffset, mItemOffset);
    }
}
