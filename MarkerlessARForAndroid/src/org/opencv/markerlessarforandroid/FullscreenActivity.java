package org.opencv.markerlessarforandroid;

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
import org.opencv.markerlessarforandroid.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class FullscreenActivity extends Activity implements CvCameraViewListener2 {
	
	private static final String  TAG = "MarkerlessAR::MainScreen::Activity";
	
    public static final String CALIBRATION_SETTINGS_FILE = "CalibrationSettings";
	
	private NativeFrameProcessor processor;
	private Mat frame;
	private boolean patternDetected = false;
	
	private static final boolean AUTO_HIDE = true; 			/* Auto-Hide System UI */
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000; 
	private static final boolean TOGGLE_ON_CLICK = true; 	/* Toggle System UI when pushed */
	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;
	private SystemUiHider mSystemUiHider;
	
    private List<Size> mResolutionList;
    private MenuItem[] mResolutionMenuItems;
    private SubMenu mResolutionMenu;
    
	private CameraView mOpenCvCameraView;
	private TextView messageBox;
	
	/**
	 * Dynamically load OpenCV library and additional required libraries
	 */
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    System.loadLibrary("ar-jni");
                    initAR();
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
    
    public native int processFrame(long currFrame, long pipelineAddress);
    public native long initCameraCallibration(float fx, float fy, float cx, float cy);
    public native void releaseCameraCallibration(long pointer);
    public native long initARPipeline(long[] images, int imgCount, long callib);
    public native void releaseARPipeline(long pointer);
    
    private void initAR() {
    	// Load training images 
    	BitmapFactory.Options opts = new BitmapFactory.Options();
    	opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
    	Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.raw.one, opts);
    	// Convert to Mat
    	Mat tmp = new Mat (bmp.getWidth(), bmp.getHeight(), CvType.CV_8UC1);
    	Utils.bitmapToMat(bmp, tmp);
    	// Add to array
    	Mat[] trainingImages = new Mat[1];;
    	trainingImages[0] = tmp;
    	// Get Camera Calibration Settings
    	SharedPreferences settings = getSharedPreferences(CALIBRATION_SETTINGS_FILE, 0);
    	processor = new NativeFrameProcessor(trainingImages, settings.getFloat("fx", 0), settings.getFloat("fy", 0), settings.getFloat("cx", 0), settings.getFloat("cy", 0)); // TODO Update for android
    }
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_fullscreen);

		final View contentView = findViewById(R.id.OpenCVCameraView);

		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
		mSystemUiHider = SystemUiHider.getInstance(this, contentView,
				HIDER_FLAGS);
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
		contentView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (TOGGLE_ON_CLICK) {
					mSystemUiHider.toggle();
				} else {
					mSystemUiHider.show();
				}
			}
		});
	
		mOpenCvCameraView = (CameraView) contentView;
	    mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
	    mOpenCvCameraView.setCvCameraViewListener(this);
	    
	    messageBox = (TextView) findViewById(R.id.info_message);
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
	}
	
	@Override
	public void onPause()
	{
	     super.onPause();
	     if (mOpenCvCameraView != null)
	         mOpenCvCameraView.disableView();
	}
	
	@Override
	public void onDestroy()
	{
	     super.onDestroy();
	     if (mOpenCvCameraView != null)
	         mOpenCvCameraView.disableView();
	     if (processor != null)
	    	 processor.release();
	}
	
    @Override
    public void onResume()
    {
        super.onResume();
    	// Ensure Camera Calibration Settings exist
    	SharedPreferences settings = getSharedPreferences(CALIBRATION_SETTINGS_FILE, 0);
    	if (settings.getAll().size() == 0) {
    		startActivity(new Intent(this, CameraCalibrationActivity.class));
    		return;
    	}
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
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
	 * system UI. This is to prevent the jarring behaviour of controls going away
	 * while interacting with activity UI.
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
		int result = 0;
		if (processor != null) {
			result = processor.process(frame);
		}
		if (!patternDetected && result == 1) {
			msgBoxHandler.post(new MessageBoxUpdater("Found Pattern"));
		} else if (patternDetected && result == 0) {
			msgBoxHandler.post(new MessageBoxUpdater("Cannot find pattern"));
		}
		patternDetected = result == 1;
		return inputFrame.rgba();
//		return inputFrame.rgba();
	}
	
	Handler msgBoxHandler = new Handler();
	
	public void openSettings(View view) {
	    // Do something in response to button
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.app_menu, menu);
	    
	    // Programatically add list of available resolutions
	    mResolutionMenu = menu.addSubMenu("Resolution");
        mResolutionList = mOpenCvCameraView.getResolutionList();
        mResolutionMenuItems = new MenuItem[mResolutionList.size()];

        ListIterator<Size> resolutionItr = mResolutionList.listIterator();
        int idx = 0;
        while(resolutionItr.hasNext()) {
            Size element = resolutionItr.next();
            mResolutionMenuItems[idx] = mResolutionMenu.add(1, idx, Menu.NONE,
                    Integer.valueOf(element.width).toString() + "x" + Integer.valueOf(element.height).toString());
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
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	
	private class MessageBoxUpdater implements Runnable {
		
		String text;
		
		public MessageBoxUpdater(String text) {
			this.text = text;
		}

		@Override
		public void run() {
			messageBox.setText(text);
		}
	}
}
