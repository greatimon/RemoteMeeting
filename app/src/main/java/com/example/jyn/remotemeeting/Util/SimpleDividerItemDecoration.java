package com.example.jyn.remotemeeting.Util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.example.jyn.remotemeeting.R;

/**
 * Created by JYN on 2017-11-17.
 */

public class SimpleDividerItemDecoration extends RecyclerView.ItemDecoration {
    private Drawable mDivider;

    @SuppressLint("LongLogTag")
    public SimpleDividerItemDecoration(Context context, String requestFrom) {

        // 일반적으로 이 드로어블을 디바이더로 사용하지만
        int drawable_id = R.drawable.shape_line_divider;

        // Call_A의 share_image 리사이클러뷰의 디바이더는 다른 드로어블을 사용한다
        if(requestFrom.equals("Call_A")) {
            drawable_id = R.drawable.shape_line_divider_for_share_img;
        }
        mDivider = context.getResources().getDrawable(drawable_id);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + mDivider.getIntrinsicHeight();

            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }
}