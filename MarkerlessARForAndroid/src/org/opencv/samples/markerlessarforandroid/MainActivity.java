package org.opencv.samples.markerlessarforandroid;

import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CountDownLatch;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.markerlessarforandroid.R;
import org.opencv.samples.markerlessarforandroid.calibration.CameraCalibration;
import org.opencv.samples.markerlessarforandroid.calibration.CameraCalibrationActivity;
import org.opencv.samples.markerlessarforandroid.camera.CameraView;
import org.opencv.samples.markerlessarforandroid.graphics.GraphicsView;
import org.opencv.samples.markerlessarforandroid.graphics.JPCTGraphicsRenderer;
import org.opencv.samples.markerlessarforandroid.processor.NativeFrameProcessor;
import org.opencv.samples.markerlessarforandroid.util.DirectoryChooserDialog;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera.Size;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.TextView;

/**
 * The Main Activity for the application.
 * 
 * @author Oliver Wilkie
 * 
 */
public class MainActivity extends Activity implements CvCameraViewListener2 {

	private static final String TAG = "MarkerlessAR::MainActivity";

	public static final String CALIBRATION_SETTINGS_FILE = "CalibrationSettings";

	private NativeFrameProcessor processor;
	private JPCTGraphicsRenderer renderer;
	private CameraCalibration cameraCalibration;

	private Mat frame;

	private CameraView mOpenCvCameraView;
	private GraphicsView mGraphicsView;
	private TextView messageBox;

	private Menu menu;
	private List<Size> mResolutionList;
	private MenuItem[] mResolutionMenuItems;
	private SubMenu mResolutionMenu;

	// Get Camera Calibration Settings
	SharedPreferences settings;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_fullscreen);

		messageBox = (TextView) findViewById(R.id.info_message);	

		settings = getSharedPreferences(CALIBRATION_SETTINGS_FILE, 0);

		// Load camera view
		mOpenCvCameraView = (CameraView) findViewById(R.id.OpenCVCameraView);
		mOpenCvCameraView.setCvCameraViewListener(MainActivity.this);

		// Load graphics view
		mGraphicsView = (GraphicsView) findViewById(R.id.OpenGLGraphicsView);
		renderer = new JPCTGraphicsRenderer(MainActivity.this);
		mGraphicsView.setRenderer(renderer);
		mGraphicsView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
		mGraphicsView.setZOrderMediaOverlay(true);
	}	

	private class LoadingTask extends AsyncTask<Void, Integer, Void> {

		ProgressDialog dialog = new ProgressDialog(MainActivity.this);
		final CountDownLatch latch = new CountDownLatch(1);

		@Override
		protected void onPreExecute() {
			// Show splash screen
			super.onPreExecute();
			Log.i(TAG, "LoadingTask onPreExecute");
			dialog.setTitle("Starting...");
			dialog.setMessage("Please wait.");
			dialog.setCancelable(false);
			dialog.show();
		}

		@Override
		protected Void doInBackground(Void... args) {
			// Load OpenCV
			OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, MainActivity.this,
					mLoaderCallback);
			Log.i(TAG, "LoadingTask waiting for OpenCV");
			// Wait for OpenCV
			try {
				latch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// Load JNI Code
			System.loadLibrary("ar-jni");
			// Load JNI Frame Processor
			processor = new NativeFrameProcessor(msgBoxHandler,
					MainActivity.this, settings.getFloat("fx", 0),
					settings.getFloat("fy", 0), settings.getFloat("cx", 0),
					settings.getFloat("cy", 0));
			// Initialise OpenGL Renderer
			renderer.init();
			// Update menu
			//updateMenu();
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			dialog.setProgress(progress[0]);
		}		

		/**
		 * Dynamically load OpenCV library and additional required libraries
		 */
		private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(MainActivity.this) {
			@Override
			public void onManagerConnected(int status) {
				super.onManagerConnected(status);
				latch.countDown();
			}
		};

		@Override
		protected void onPostExecute(Void processorStarted) {
			mOpenCvCameraView.enableView();
			dialog.dismiss();
		}
	}

	private void loadCameraCalibration() {
		// Get Camera Calibration Settings
		SharedPreferences settings = getSharedPreferences(
				CALIBRATION_SETTINGS_FILE, 0);
		cameraCalibration = new CameraCalibration(settings.getFloat("fx", 0),
				settings.getFloat("fy", 0), settings.getFloat("cx", 0),
				settings.getFloat("cy", 0));
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
		//		if (renderer != null)
		//			renderer.stop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
		if (processor != null)
			processor.release();
		processor = null;
	}


	@Override
	protected void onResume() {
		super.onResume();
		//Ensure Camera Calibration Settings exist
		SharedPreferences settings = getSharedPreferences(
				CALIBRATION_SETTINGS_FILE, 0);
		if (settings.getAll().size() == 0) {
			startActivity(new Intent(this, CameraCalibrationActivity.class));
			finish();
			return;
		} else {
			loadCameraCalibration();
			// Start heavy duty work on background thread
			new LoadingTask().execute();
		}
		if (mGraphicsView != null)
			mGraphicsView.onResume();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
	}



	@Override
	public void onCameraViewStarted(int width, int height) {
		frame = new Mat();
	}

	@Override
	public void onCameraViewStopped() {
		frame.release();
	}

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		frame = inputFrame.rgba();
		if (processor.processFrame(frame)) {
			//Mat pose = processor.getPose();

		} else {

		}
		return frame;
	}

	Handler msgBoxHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 0) {
				messageBox.setText("Pattern found");
			} else {
				messageBox.setText("Pattern not found");
			}
		}
	};

	private void updateMenu() {
		// Programatically add list of available resolutions
		mResolutionMenu = menu.getItem(2).getSubMenu();
		// Build menu item of camera resolutions
		mResolutionList = mOpenCvCameraView.getResolutionList();
		mResolutionMenuItems = new MenuItem[mResolutionList.size()];

		ListIterator<Size> resolutionItr = mResolutionList.listIterator();
		int idx = 0;
		while (resolutionItr.hasNext()) {
			Size element = resolutionItr.next();
			mResolutionMenuItems[idx] = mResolutionMenu.add(1, idx, Menu.NONE,
					Integer.valueOf(element.width).toString() + "x"
							+ Integer.valueOf(element.height).toString());
			idx++;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.app_menu, menu);
		this.menu = menu;
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle resolutions
		if (item.getGroupId() == 1) {
			int id = item.getItemId();
			Size resolution = mResolutionList.get(id);
			mOpenCvCameraView.setResolution(resolution);
			return true;
		}

		// Handle all other items selection
		switch (item.getItemId()) {
		case R.id.about:
			Intent about = new Intent(this, AboutActivity.class);
			startActivity(about);
			return true;
		case R.id.calibrate:
			Intent calibrate = new Intent(this, CameraCalibrationActivity.class);
			startActivity(calibrate);
			return true;
		case R.id.debug:
			MainApplication app = (MainApplication) getApplication();
			item.setChecked(!app.isDebugMode());
			app.setDebugMode(!app.isDebugMode());
			return true;
		case R.id.export:
			chooseExportDirectory();
			return true;
		case R.id.exit:
			finish();
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void chooseExportDirectory() {
		// Create DirectoryChooserDialog and register a callback
		DirectoryChooserDialog directoryChooserDialog = new DirectoryChooserDialog(
				MainActivity.this,
				new DirectoryChooserDialog.ChosenDirectoryListener() {
					@Override
					public void onChosenDir(String chosenDir) {
						exportPatternsToDirectory(chosenDir);
					}
				});
		directoryChooserDialog.setNewFolderEnabled(true);
		directoryChooserDialog.chooseDirectory(android.os.Environment
				.getExternalStorageDirectory().getAbsolutePath());
	}

	private void exportPatternsToDirectory(String directoryPath) {
		processor.savePatterns(directoryPath);
	}

}
