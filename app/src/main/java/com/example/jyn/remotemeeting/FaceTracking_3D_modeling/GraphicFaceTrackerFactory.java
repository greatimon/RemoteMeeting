package com.example.jyn.remotemeeting.FaceTracking_3D_modeling;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;

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

    public GraphicFaceTrackerFactory(GraphicOverlay graphicOverlay) {
        this.graphicOverlay = graphicOverlay;
    }

    @Override
    public Tracker<Face> create(Face face) {
        return new GraphicFaceTracker(graphicOverlay);
    }



    private class GraphicFaceTracker extends Tracker<Face> {

        private GraphicOverlay graphicOverlay;
//        private FaceGraphic faceGraphic;

        GraphicFaceTracker(GraphicOverlay overlay) {
            graphicOverlay = overlay;
//            faceGraphic = new FaceGraphic(overlay);
        }


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

            // 핸들러로 전달할 Message 객체 생성
            Message msg = Awd_model_handling.rotate_handler.obtainMessage();

            // Message 객체에 넣을 bundle 객체 생성
            Bundle bundle = new Bundle();
            // bundle 객체에 'EulerY, EulerZ' 변수 담기
            bundle.putFloat("EulerY", face.getEulerY());
            bundle.putFloat("EulerZ", face.getEulerZ());
            bundle.putFloat("length_between_eyes_X", (right_eye_coorX - left_eye_coorX));
            bundle.putFloat("face_width", face.getWidth());

            msg.setData(bundle);
            // 핸들러에서 Message 객체 구분을 위한 'what' 값 설정
            msg.what = 0;

            // 핸들러로 Message 객체 전달
            Awd_model_handling.rotate_handler.sendMessage(msg);
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
