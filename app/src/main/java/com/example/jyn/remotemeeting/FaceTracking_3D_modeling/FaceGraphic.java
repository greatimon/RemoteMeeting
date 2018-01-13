/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.jyn.remotemeeting.FaceTracking_3D_modeling;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;

import com.example.jyn.remotemeeting.R;
import com.google.android.gms.vision.face.Face;

/**
 * 사용하지 않는 클래스
 *  - 사용하지 않는 이유: 구글 비전 api용 카메라 프리뷰에서 마스크를 씌워주는 뷰를 보여주지 않기 때문에
 *  애초에 마스크를 씌울 필요도 / 얼굴 인식 위치를 그릴 필요도 없음
 *
 * Graphic instance for rendering face position, orientation, and landmarks within an associated
 * graphic overlay view.
 */
class FaceGraphic extends GraphicOverlay.Graphic {
    // 샘플 코드에서는 사용했던 상수
    private static final float FACE_POSITION_RADIUS = 10.0f;
    private static final float ID_TEXT_SIZE = 40.0f;
    // 샘플 코드에서는 사용했던 상수
    private static final float ID_Y_OFFSET = 50.0f;
    private static final float ID_X_OFFSET = -50.0f;
    // 샘플 코드에는 없었던 변수 - 근데 사용하지 않고 있음
    private static final float GENERIC_POS_OFFSET = 20.0f;
    private static final float GENERIC_NEG_OFFSET = -20.0f;
    private static final float BOX_STROKE_WIDTH = 5.0f;

    private static final int COLOR_CHOICES[] = {
            Color.BLUE,
            Color.CYAN,
            Color.GREEN,
            Color.MAGENTA,
            Color.RED,
            Color.WHITE,
            Color.YELLOW
    };
    private static int mCurrentColorIndex = 0;

    private Paint mFacePositionPaint;
    private Paint mIdPaint;
    private Paint mBoxPaint;

    private volatile Face mFace;
    // 샘플 코드에서는 사용했던 변수
    private int mFaceId;
    private float mFaceHappiness;
    private Bitmap bitmap;
    private Bitmap op;

    FaceGraphic(GraphicOverlay overlay) {
        super(overlay);

        mCurrentColorIndex = (mCurrentColorIndex + 1) % COLOR_CHOICES.length;
        final int selectedColor = COLOR_CHOICES[mCurrentColorIndex];

        mFacePositionPaint = new Paint();
        mFacePositionPaint.setColor(selectedColor);

        mIdPaint = new Paint();
        mIdPaint.setColor(selectedColor);
        mIdPaint.setTextSize(ID_TEXT_SIZE);

        mBoxPaint = new Paint();
        mBoxPaint.setColor(selectedColor);
        mBoxPaint.setStyle(Paint.Style.STROKE);
        mBoxPaint.setStrokeWidth(BOX_STROKE_WIDTH);
        // 샘플 코드에서는 없었던 변수
        // 마스크를 비트맵으로 변환하는 과정
        bitmap = BitmapFactory.decodeResource(getOverlay().getContext().getResources(), R.drawable.op);
        op = bitmap;
    }

    void setId(int id) {
        mFaceId = id;
    }


    /**
     * Updates the face instance from the detection of the most recent frame.  Invalidates the
     * relevant portions of the overlay to trigger a redraw.
     */
    void updateFace(Face face) {
        mFace = face;
        // 샘플 코드에는 없었던 로직
        op = Bitmap.createScaledBitmap(bitmap, (int) scaleX(face.getWidth()),
                (int) scaleY(((bitmap.getHeight() * face.getWidth()) / bitmap.getWidth())), false);
        postInvalidate();
    }

    /**
     * Draws the face annotations for position on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        Face face = mFace;
        if (face == null) {
            return;
        }

        /** 샘플 원래 코드 */
//        // Draws a circle at the position of the detected face, with the face's track id below.
//        float x = translateX(face.getPosition().x + face.getWidth() / 2);
//        float y = translateY(face.getPosition().y + face.getHeight() / 2);
//
//        canvas.drawCircle(x, y, FACE_POSITION_RADIUS, mFacePositionPaint);
//        canvas.drawText("id: " + mFaceId, x + ID_X_OFFSET, y + ID_Y_OFFSET, mIdPaint);
//        canvas.drawText("happiness: " + String.format("%.2f", face.getIsSmilingProbability()), x - ID_X_OFFSET, y - ID_Y_OFFSET, mIdPaint);
//        canvas.drawText("right eye: " + String.format("%.2f", face.getIsRightEyeOpenProbability()), x + ID_X_OFFSET * 2, y + ID_Y_OFFSET * 2, mIdPaint);
//        canvas.drawText("left eye: " + String.format("%.2f", face.getIsLeftEyeOpenProbability()), x - ID_X_OFFSET*2, y - ID_Y_OFFSET*2, mIdPaint);
//
//        // Draws a bounding box around the face.
//        float xOffset = scaleX(face.getWidth() / 2.0f);
//        float yOffset = scaleY(face.getHeight() / 2.0f);
//        float left = x - xOffset;
//        float top = y - yOffset;
//        float right = x + xOffset;
//        float bottom = y + yOffset;
//        canvas.drawRect(left, top, right, bottom, mBoxPaint);


        /** 여기서 변경한 코드 코드 */
        // Draws a circle at the position of the detected face, with the face's track id below.
        // 샘플 코드와 동일
        float x = translateX(face.getPosition().x + face.getWidth() / 2);
        float y = translateY(face.getPosition().y + face.getHeight() / 2);

        // 샘플 코드와 동일
        float xOffset = scaleX(face.getWidth() / 2.0f);
        float yOffset = scaleY(face.getHeight() / 2.0f);
        float left = x - xOffset;
        float top = y - yOffset;
        float right = x + xOffset;
        float bottom = y + yOffset;
        canvas.drawRect(left, top, right, bottom, mBoxPaint);

        // 샘플 코드와 다른 부분
        canvas.drawBitmap(op, left, top, new Paint());
    }

    private float getNoseAndMouthDistance(PointF nose, PointF mouth) {
        return (float) Math.hypot(mouth.x - nose.x, mouth.y - nose.y);
    }
}
