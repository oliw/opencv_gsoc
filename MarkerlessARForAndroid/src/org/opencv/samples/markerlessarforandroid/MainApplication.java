package org.opencv.samples.markerlessarforandroid;

import android.app.Application;
import android.util.Log;

public class MainApplication extends Application {
	
	private static final String  TAG = "MarkerlessAR::MainApplication";

	boolean debugMode;
	
	public MainApplication() {
		super();
		debugMode = false;
	}
	
	public boolean isDebugMode() {
		return debugMode;
	}

	public void setDebugMode(boolean debugMode) {
		this.debugMode = debugMode;
		Log.i(TAG, "Debug mode set to:"+Boolean.toString(debugMode));
	}
}
