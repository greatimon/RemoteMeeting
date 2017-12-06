package com.example.jyn.remotemeeting.Util;

import android.app.Activity;
import android.widget.Toast;

/**
 * Created by JYN on 2017-11-10.
 */

public class BackPressCloseHandler {

    private long backkeyPressedTime = 0;
    private Toast toast;
    private Activity activity;

    public BackPressCloseHandler(Activity context) {
        this.activity = context;
    }

    public void onBackPressed() {
        if(System.currentTimeMillis() > backkeyPressedTime + 2000) {
            backkeyPressedTime = System.currentTimeMillis();
            toast = Toast.makeText(activity, "'뒤로'버튼 한번 더 누르시면 종료됩니다", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        if(System.currentTimeMillis() <= backkeyPressedTime + 2000) {
            toast.cancel();
            activity.finishAffinity();

        }
    }
}
