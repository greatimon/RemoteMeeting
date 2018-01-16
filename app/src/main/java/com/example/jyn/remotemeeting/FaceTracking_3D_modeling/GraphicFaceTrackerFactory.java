package com.example.jyn.remotemeeting.FaceTracking_3D_modeling;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.example.jyn.remotemeeting.Activity.Call_A;
import com.example.jyn.remotemeeting.DataClass.Data_for_netty;
import com.example.jyn.remotemeeting.Etc.Static;
import com.example.jyn.remotemeeting.Util.Myapp;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.Landmark;

/**
 * Created by JYN on 2018-01-11.
 */

public class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {

    private String TAG = GraphicFaceTrackerFactory.class.getSimpleName();
    private GraphicOverlay graphicOverlay;
    private Myapp myapp;
    long formal_sending_time = 0;

    // 원래 코드
//    public GraphicFaceTrackerFactory(GraphicOverlay graphicOverlay) {
//        this.graphicOverlay = graphicOverlay;
//    }

    // 시도 코드
    public GraphicFaceTrackerFactory() {
        // 어플리케이션 객체 생성
        myapp = Myapp.getInstance();
    }

    @Override
    public Tracker<Face> create(Face face) {
//        return new GraphicFaceTracker(graphicOverlay);
        return new GraphicFaceTracker();
    }



    private class GraphicFaceTracker extends Tracker<Face> {

        private GraphicOverlay graphicOverlay;
        // 어차피 프리뷰를 보여주지 않기 때문에, 애초에 마스크를 씌울 필요가 없어서 주석처리함
//        private FaceGraphic faceGraphic;

        // 원래 코드
//        GraphicFaceTracker(GraphicOverlay overlay) {
//            graphicOverlay = overlay;
////            faceGraphic = new FaceGraphic(overlay);
//        }
        // 시도 코드
        GraphicFaceTracker() {}

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        @Override
        public void onNewItem(int faceId, Face face) {
//            faceGraphic.setId(faceId);
        }


        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        public void onUpdate(Detector.Detections<Face> detections, Face face) {
//            graphicOverlay.add(faceGraphic);
//            faceGraphic.updateFace(face);

//            Log.d(TAG, "==============================================");
//            Log.d(TAG, "face.getId(): " + face.getId());
//            Log.d(TAG, "face.getEulerY(): " + face.getEulerY());
//            Log.d(TAG, "face.getEulerZ(): " + face.getEulerZ());
//            Log.d(TAG, "face.getWidth(): " + face.getWidth());
//            Log.d(TAG, "face.getHeight(): " + face.getHeight());
//            Log.d(TAG, "face.getIsLeftEyeOpenProbability " + face.getIsLeftEyeOpenProbability());
//            Log.d(TAG, "face.getIsRightEyeOpenProbability(): " + face.getIsRightEyeOpenProbability());
//            Log.d(TAG, "face.getIsSmilingProbability(): " + face.getIsSmilingProbability());
//            Log.d(TAG, "face.getPosition().x: " + face.getPosition().x);
//            Log.d(TAG, "face.getPosition().y: " + face.getPosition().y);

            float right_eye_coorX = 0;
            float left_eye_coorX = 0;
            for (Landmark landmark : face.getLandmarks()) {
                if (landmark.getType() == 10) {
                    right_eye_coorX = landmark.getPosition().x;
                } else if (landmark.getType() == 4) {
                    left_eye_coorX = landmark.getPosition().x;
                }
            }

            // Message 객체에 넣을 bundle 객체 생성
            Bundle bundle = new Bundle();
            // bundle 객체에 'EulerY, EulerZ' 변수 담기
            bundle.putFloat("EulerY", face.getEulerY());
            bundle.putFloat("EulerZ", face.getEulerZ());

            // 핸들러로 전달할 Message 객체 생성
            Message msg = Awd_model_handling.rotate_handler.obtainMessage();
            // bundle 객체 넣기
            msg.setData(bundle);
            // 핸들러에서 Message 객체 구분을 위한 'what' 값 설정
            msg.what = 0;

            // 핸들러로 Message 객체 전달
            Awd_model_handling.rotate_handler.sendMessage(msg);

            /** 만약 상대방과 영상통화 중이라면,
                Netty를 통해 상대방에게 EulerY, EulerZ 값을 전달한다
             */
            if(Call_A.pipRenderer.isClickable()) {
                /** 상대방에게 나의 얼굴인식 on/off 상태를 전달하는 메소드 호출 */
                // 최초는 무조건 전송
                if(formal_sending_time == 0) {
                    myapp.send_my_3d_mode_status_to_subject(Call_A.is_3D_mode, face.getEulerY(), face.getEulerZ());
                }
                // 그다음부터는 이전에 보낸 시간으로부터 0.1초가 지나야 보내도록 하기
                else if(formal_sending_time != 0 &&
                        formal_sending_time - System.currentTimeMillis() > 100) {
                    myapp.send_my_3d_mode_status_to_subject(Call_A.is_3D_mode, face.getEulerY(), face.getEulerZ());
                }
            }
        }


        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        @Override
        public void onMissing(Detector.Detections<Face> detections) {
//            graphicOverlay.remove(faceGraphic);
        }


        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        @Override
        public void onDone() {
//            graphicOverlay.remove(faceGraphic);
        }
    }
}
