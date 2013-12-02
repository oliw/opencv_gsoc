package org.opencv.samples.markerlessarforandroid;

import org.opencv.markerlessarforandroid.R;
import org.opencv.samples.markerlessarforandroid.graphics.JPCTGraphicsRenderer;

import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;

public class MainGraphicsActivity extends Activity {

	private GLSurfaceView mGLView;
	private GLSurfaceView.Renderer renderer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mGLView = new GLSurfaceView(getApplication());
		mGLView.setEGLContextClientVersion(2);
		new LoadActivityTask().execute();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mGLView != null)
			mGLView.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mGLView != null)
			mGLView.onResume();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	private class LoadActivityTask extends AsyncTask<Void, Integer, Void> {

		@Override
		protected void onPreExecute() {
			// Show splash screen
			super.onPreExecute();
			setContentView(R.layout.activity_loading);
		}

		@Override
		protected Void doInBackground(Void... args) {
			renderer = new JPCTGraphicsRenderer(MainGraphicsActivity.this);
			return null;
		}

		@Override
        protected void onProgressUpdate(Integer... values) {
            //insult user here
        }
		
		@Override
		protected void onPostExecute(Void result) {
			mGLView.setRenderer(renderer);
			setContentView(mGLView);
		}
	}

}
