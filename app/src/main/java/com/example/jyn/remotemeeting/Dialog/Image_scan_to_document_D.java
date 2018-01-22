package com.example.jyn.remotemeeting.Dialog;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.example.jyn.remotemeeting.R;
import com.example.jyn.remotemeeting.Util.Myapp;
import com.scanlibrary.ScanActivity;
import com.scanlibrary.ScanConstants;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by JYN on 2018-01-20.
 */

public class Image_scan_to_document_D extends Activity {

    int REQUEST_SCAN_CODE = 9999;
    private static final String TAG = "all_"+Image_scan_to_document_D.class.getSimpleName();
    Myapp myapp;

    /** 버터나이프*/
    public Unbinder unbinder;
    @BindView(R.id.scanned_img)         ImageView scanned_img;


    /**---------------------------------------------------------------------------
     생명주기 ==> onCreate
     ---------------------------------------------------------------------------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.v_image_scan_to_document);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        this.setFinishOnTouchOutside(false);

        // 버터나이프 바인드
        unbinder = ButterKnife.bind(this);
        // 어플리케이션 객체 생성
        myapp = Myapp.getInstance();

        // 카메라 열기
        startScan(ScanConstants.OPEN_CAMERA);

    }


    /**---------------------------------------------------------------------------
     메소드 ==> 카메라 열면서, 카메라 스캐너 시작
     ---------------------------------------------------------------------------*/
    protected void startScan(int preference) {
        Intent intent = new Intent(this, ScanActivity.class);
        intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, preference);
        startActivityForResult(intent, REQUEST_SCAN_CODE);
    }


    /**---------------------------------------------------------------------------
     콜백메소드 ==> onActivityResult
     ---------------------------------------------------------------------------*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "=================== onActivityResult ===================");

        if (requestCode == REQUEST_SCAN_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getExtras().getParcelable(ScanConstants.SCANNED_RESULT);
//            Bitmap bitmap = null;
            Log.d(TAG, "onActivityResult_uri: " + uri);
//            try {

//                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
//                getContentResolver().delete(uri, null, null);
//                myapp.setScanned_bitmap(bitmap);

                // 원래코드
//                scanned_img.setImageBitmap(bitmap);

                // 트라이코드
//                /** 비트맵 -> 파일로 저장하기 */
//                String canonicalPath = myapp.saveBitmaptoImage(bitmap,
//                        "remoteMeeting",
//                        "testBitmap_" + myapp.get_time("yyyyMMdd HH_mm_ss"));
//                Log.d(TAG, "canonicalPath: " + canonicalPath);

                // 비트맵 리사이클처리 및 GC 처리
//                bitmap.recycle();
//                System.gc();
//
//                // TODO: 로컬에 파일을 저장하는 것이므로, 쉐어드나 여타 다른 방법을 이용해서
//                // TODO: 해쉬맵 구조로 key는 meeting_no, value는 절대 경로로 하여 저장해서
//                // TODO: 다음에 불러올 수 있도록 처리해야겠다
//                /** 파일로 저장 완료 됐으면 해당 절대 경로를 intent 값으로 반환하면서 액티비티 종료하기 */
                Intent intent = new Intent();

                intent.putExtra(ScanConstants.SCANNED_RESULT, uri);
                setResult(Activity.RESULT_OK, intent);

                setResult(RESULT_OK, intent);
                finish();

//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }
    }


    /**---------------------------------------------------------------------------
     생명주기 ==> onDestroy
     ---------------------------------------------------------------------------*/
    @Override
    protected void onDestroy() {
        // 버터나이프 바인드 해제
        if(unbinder != null) {
            unbinder.unbind();
        }
        // 어플리케이션 객체 null 처리
        myapp = null;

        super.onDestroy();
    }
}
