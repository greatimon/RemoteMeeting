package com.example.jyn.remotemeeting.FaceTracking_3D_modeling;

import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.example.jyn.remotemeeting.R;

import org.rajawali3d.renderer.ISurfaceRenderer;
import org.rajawali3d.renderer.Renderer;
import org.rajawali3d.view.IDisplay;
import org.rajawali3d.view.ISurface;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public abstract class Awd_model_fragment extends Fragment implements IDisplay, OnClickListener {

	public String TAG = Awd_model_fragment.class.getSimpleName();

	protected FrameLayout      mLayout;
	protected ISurface mRajawaliSurface;
	protected ISurfaceRenderer mRenderer;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		// Inflate the view
		mLayout = (FrameLayout) inflater.inflate(getLayoutID(), container, false);

		// Find the TextureView
		mRajawaliSurface = (ISurface) mLayout.findViewById(R.id.rajwali_surface);

		// Create the renderer
		mRenderer = createRenderer();
		onBeforeApplyRenderer();
		applyRenderer();
		return mLayout;
	}

	protected void onBeforeApplyRenderer() {

	}

	protected void applyRenderer() {
		mRajawaliSurface.setSurfaceRenderer(mRenderer);
	}

	@Override
	public void onClick(View v) {
//		switch (v.getId()) {
//			case R.id.image_view_example_link:
//				if (mImageViewExampleLink == null) throw new IllegalStateException("Example link is null!");
//
//				final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mExampleUrl));
//				startActivity(intent);
//				break;
//		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();

		if (mLayout != null)
			mLayout.removeView((View) mRajawaliSurface);
	}

	public int getLayoutID() {
		return R.layout.rajawali_textureview_fragment;
	}



	protected abstract class base_Renderer extends Renderer {

		public base_Renderer(Context context) {
			super(context);
		}

		@Override
		public void onOffsetsChanged(float v, float v2, float v3, float v4, int i, int i2) {

		}

		@Override
		public void onTouchEvent(MotionEvent event) {

		}

		@Override
		public void onRenderSurfaceCreated(EGLConfig config, GL10 gl, int width, int height) {
			super.onRenderSurfaceCreated(config, gl, width, height);
		}

		@Override
		protected void onRender(long ellapsedRealtime, double deltaTime) {
			super.onRender(ellapsedRealtime, deltaTime);
		}
	}
}