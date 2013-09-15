package org.opencv.samples.markerlessarforandroid;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;
import java.util.ListIterator;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.Utils;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.markerlessarforandroid.R;
import org.opencv.samples.markerlessarforandroid.calibration.CameraCalibration;
import org.opencv.samples.markerlessarforandroid.calibration.CameraCalibrationActivity;
import org.opencv.samples.markerlessarforandroid.graphics.GraphicsRenderer;
import org.opencv.samples.markerlessarforandroid.graphics.GraphicsView;
import org.opencv.samples.markerlessarforandroid.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera.Size;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.provider.VoicemailContract;
import android.util.Log;
import android.util.MonthDisplayHelper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements CvCameraViewListener2 {

	private static final String TAG = "MarkerlessAR::MainScreen::Activity";

	public static final String CALIBRATION_SETTINGS_FILE = "CalibrationSettings";

	private NativeFrameProcessor processor;
	private GraphicsRenderer renderer;
	private CameraCalibration cameraCalibration;

	private Mat frame;

	private static final boolean AUTO_HIDE = true; /* Auto-Hide System UI */
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
	private static final boolean TOGGLE_ON_CLICK = true; /*
														 * Toggle System UI when
														 * pushed
														 */
	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;
	private SystemUiHider mSystemUiHider;

	private List<Size> mResolutionList;
	private MenuItem[] mResolutionMenuItems;
	private SubMenu mResolutionMenu;

	private CameraView mOpenCvCameraView;
	private GraphicsView mGraphicsView;
	private TextView messageBox;

	private boolean openCVLoaded = false;
	private boolean processorReady = false;

	/**
	 * Dynamically load OpenCV library and additional required libraries
	 */
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(TAG, "OpenCV loaded successfully");
				openCVLoaded = true;
				System.loadLibrary("ar-jni"); // Load native library after(!)
												// OpenCV initialization
				mOpenCvCameraView.enableView(); // Enable Camera View
				if (processor == null) {
					new BuildProcessorTask().execute(); // Initialise Frame Processor
				}
				renderer.start(); // Enable Renderer
				break;
			}
			default: {
				super.onManagerConnected(status);
				break;
			}
			}
		}
	};

	private class BuildProcessorTask extends AsyncTask<Void, Integer, Void> {

		@Override
		protected Void doInBackground(Void... unused) {
			// Load training images
			AssetManager assetManager = getAssets();
			String imgFolder = "training-images";
			String[] imgs = new String[0];
			try {
				imgs = assetManager.list(imgFolder);
			} catch (IOException e) {
			}
			Mat[] trainingImages = new Mat[imgs.length];
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
			InputStream istr;
			Bitmap bmp = null;
			for (int i = 0; i < imgs.length; i++) {
				try {
					istr = assetManager.open(imgFolder + File.separator
							+ imgs[i]);
					bmp = BitmapFactory.decodeStream(istr, null, opts);
					// Convert to Mat
					Mat tmp = new Mat(bmp.getWidth(), bmp.getHeight(),
							CvType.CV_8UC1);
					Utils.bitmapToMat(bmp, tmp);
					trainingImages[i] = tmp;
				} catch (IOException e) {
					Log.e(TAG, "Could not load a training image from assets");
				}
			}
			// Get Camera Calibration Settings
			SharedPreferences settings = getSharedPreferences(
					CALIBRATION_SETTINGS_FILE, 0);
			processor = new NativeFrameProcessor(msgBoxHandler, trainingImages,
					settings.getFloat("fx", 0), settings.getFloat("fy", 0),
					settings.getFloat("cx", 0), settings.getFloat("cy", 0));
			processorReady = true;
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			Toast.makeText(MainActivity.this, "Processor Started",
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Ensure Camera Calibration Settings exist
		SharedPreferences settings = getSharedPreferences(
				CALIBRATION_SETTINGS_FILE, 0);
		if (settings.getAll().size() == 0) {
			startActivity(new Intent(this, CameraCalibrationActivity.class));
			finish();
			return;
		} else {
			loadCameraCalibration();
		}

		setContentView(R.layout.activity_fullscreen);

		final View topView = findViewById(R.id.MainViewGroup);

		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
		mSystemUiHider = SystemUiHider.getInstance(this, topView, HIDER_FLAGS);
		mSystemUiHider.setup();
		mSystemUiHider
				.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
					@Override
					@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
					public void onVisibilityChange(boolean visible) {
						if (visible && AUTO_HIDE) {
							// Schedule a hide().
							delayedHide(AUTO_HIDE_DELAY_MILLIS);
						}
					}
				});

		// Set up the user interaction to manually show or hide the system UI.
		topView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (TOGGLE_ON_CLICK) {
					mSystemUiHider.toggle();
				} else {
					mSystemUiHider.show();
				}
			}
		});

		topView.setVisibility(SurfaceView.VISIBLE);

		mOpenCvCameraView = (CameraView) findViewById(R.id.OpenCVCameraView);
		mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
		mOpenCvCameraView.setCvCameraViewListener(this);

		renderer = new GraphicsRenderer(cameraCalibration);
		mGraphicsView = (GraphicsView) findViewById(R.id.OpenGLGraphicsView);
		mGraphicsView.setRenderer(renderer);
		mGraphicsView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
		mGraphicsView.setZOrderMediaOverlay(true);

		messageBox = (TextView) findViewById(R.id.info_message);
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
		if (renderer != null)
			renderer.stop();
		processorReady = false;
		openCVLoaded = false;
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
	public void onResume() {
		super.onResume();
		// Ensure Camera Calibration Settings exist
		SharedPreferences settings = getSharedPreferences(
				CALIBRATION_SETTINGS_FILE, 0);
		if (settings.getAll().size() == 0) {
			startActivity(new Intent(this, CameraCalibrationActivity.class));
			finish();
			return;
		}
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this,
				mLoaderCallback);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
		delayedHide(100);
	}

	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the
	 * system UI. This is to prevent the jarring behaviour of controls going
	 * away while interacting with activity UI.
	 */
	View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (AUTO_HIDE) {
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
			}
			return false;
		}
	};

	Handler mHideHandler = new Handler();
	Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			mSystemUiHider.hide();
		}
	};

	/**
	 * Schedules a call to hide() in [delay] milliseconds, cancelling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
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
		if (processorReady) {
			if (processor.processFrame(frame)) {
				Mat pose = processor.getPose();
				if (openCVLoaded) {
					renderer.setPatternPose(pose);
				}
			} else {
				if (openCVLoaded) {
					renderer.clearPose();
				}
			}
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.app_menu, menu);

		// Programatically add list of available resolutions
		mResolutionMenu = menu.addSubMenu("Resolution");
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
		case R.id.settings:
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		case R.id.about:
			Intent about = new Intent(this, AboutActivity.class);
			startActivity(about);
			return true;
		case R.id.calibrate:
			Intent calibrate = new Intent(this, CameraCalibrationActivity.class);
			startActivity(calibrate);
			return true;
		case R.id.debug:
			MyApplication app = (MyApplication) getApplication();
			item.setChecked(!app.isDebugMode());
			app.setDebugMode(!app.isDebugMode());
		case R.id.exit:
			finish();
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
