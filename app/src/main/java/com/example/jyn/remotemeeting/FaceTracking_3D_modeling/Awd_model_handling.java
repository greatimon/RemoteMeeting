package com.example.jyn.remotemeeting.FaceTracking_3D_modeling;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.example.jyn.remotemeeting.R;

import org.rajawali3d.Object3D;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.loader.LoaderAWD;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.CubeMapTexture;
import org.rajawali3d.math.vector.Vector3;

import java.util.ArrayList;

public class Awd_model_handling extends Awd_model_fragment {

    String TAG = Awd_model_handling.class.getSimpleName();


    private float mGravity[];

    static public Handler rotate_handler;

    float length_between_eyes_X;
    float face_width;
    public static double scale = 1.0d;

    @SuppressLint("HandlerLeak")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGravity = new float[3];

        rotate_handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(msg.what == 0) {
                    // Message 객체로부터 'EulerY, EulerZ' 변수 담기
                    float EulerY = msg.getData().getFloat("EulerY");
                    float EulerZ = msg.getData().getFloat("EulerZ");
                    length_between_eyes_X = msg.getData().getFloat("length_between_eyes_X");
                    face_width = msg.getData().getFloat("face_width");

//                    Log.d(TAG, "====================== AccelerometerFragment_ get handleMessage ======================");
//                    Log.d(TAG, "EulerY: " + EulerY);
//                    Log.d(TAG, "EulerZ: " + EulerZ);

//                    ((AccelerometerRenderer) mRenderer).rotate(EulerY, EulerZ);
                    ((On_face_tracking_Renderer) mRenderer).set_on_faceTrackingValues(
                            EulerZ,
                            (EulerY*1.35f),
                            0);
                }
            }
        };
    }

    @Override
    public base_Renderer createRenderer() {
        return new On_face_tracking_Renderer(getActivity());
    }



    private final class On_face_tracking_Renderer extends base_Renderer {
        private DirectionalLight mLight;
        private Object3D mMonkey;
        private Vector3 face_trackingValues;

        double formal_x;
        double formal_y;
        double formal_z;

        public On_face_tracking_Renderer(Context context) {
            super(context);
            face_trackingValues = new Vector3();
        }

        @Override
        protected void initScene() {

            try {
//                mLight = new DirectionalLight(0.1f, 0.2f, -1.0f);
//                mLight.setColor(1.0f, 1.0f, 1.0f);
//                mLight.setPower(1.5f);
//                getCurrentScene().addLight(mLight);

                DirectionalLight light = new DirectionalLight();
                light.setLookAt(1, -45, 1);
                light.enableLookAt();
                light.setPower(1.5f);
                getCurrentScene().addLight(light);

                light = new DirectionalLight();
                light.setLookAt(-45, 1, -45);
                light.enableLookAt();
                light.setPower(1.5f);
                getCurrentScene().addLight(light);

                final LoaderAWD parser = new LoaderAWD(mContext.getResources(), mTextureManager, R.raw.awd_suzanne);
                parser.parse();

                mMonkey = parser.getParsedObject();
                getCurrentScene().addChild(mMonkey);

//                getCurrentCamera().setZ(4.5d); /** 줌 레벨 */

                int[] resourceIds = new int[]{R.drawable.posx, R.drawable.negx,
                    R.drawable.posy, R.drawable.negy, R.drawable.posz,
                    R.drawable.negz};

                final int[] resourceIds_1 = new int[] {
                        R.drawable.posx2, R.drawable.negx2,
                        R.drawable.posy2, R.drawable.negy2, R.drawable.posz2, R.drawable.negz2
                };

                final Bitmap[] resourceIds_1_bitmap = new Bitmap[6];

                for(int i = 0; i< resourceIds_1.length; i++) {
                    final int finalI = i;

                    // OOM을 막기 위해서 리소스를 글라이드로 적절한 비트맵으로 변환
                    Glide
                        .with(getActivity())
                        .load(resourceIds[i])
                        .asBitmap()
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                resourceIds_1_bitmap[finalI] = resource;

                                // 마지막일 때
                                if(finalI == resourceIds_1.length - 1) {
                                    Material material = new Material();
                                    material.enableLighting(true);
                                    material.setDiffuseMethod(new DiffuseMethod.Lambert());

                                    try {
                                        CubeMapTexture envMap = new CubeMapTexture("environmentMap",
                                                resourceIds_1_bitmap);
                                        envMap.isEnvironmentTexture(true);
                                        material.addTexture(envMap);
                                        material.setColorInfluence(0);
                                        mMonkey.setMaterial(material);
                                    } catch (ATexture.TextureException e) {
                                        e.printStackTrace();
                                    }

                                }
                            }
                        });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onRender(long ellapsedRealtime, double deltaTime) {
            super.onRender(ellapsedRealtime, deltaTime);
//            mMonkey.setRotation(face_trackingValues.x, face_trackingValues.y + 180, face_trackingValues.z);
            // y, z움직임 조정하는 로직
            mMonkey.setRotation(face_trackingValues.x, face_trackingValues.y, face_trackingValues.z);
            Log.d(TAG, "onRender_ x: " + face_trackingValues.x + ", y: " + face_trackingValues.y + ", z:" + face_trackingValues.z);

            // 스케일 조정하는 로직
//            double this_Z = face_width/125.0d;
//            if(this_Z > 4.0d) {
//                this_Z = 4.0d;
//            }
//            else if(this_Z < 1.1d) {
//                this_Z = 1.1d;
//            }
//            Log.d(TAG, "this_Z: " + this_Z);
//
//            mMonkey.setScale(this_Z/10.0d);
//            Log.d(TAG, "face_width: " + face_width);

        }

        // todo: 여기가 키
        public void set_on_faceTrackingValues(float x, float y, float z) {
            face_trackingValues.setAll(x, y, z);
            Log.d(TAG, "setAccelerometerValues_ -x: " + x + ", -y: " + y + ", -z:" + z);
        }
    }


}
