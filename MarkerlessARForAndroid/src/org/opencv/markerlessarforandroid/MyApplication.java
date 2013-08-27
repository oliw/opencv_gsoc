package org.opencv.markerlessarforandroid;

import android.app.Application;
import android.util.Log;

public class MyApplication extends Application {
	
	private static final String  TAG = "MarkerlessAR::MyApplication";

	boolean debugMode;
	
	public MyApplication() {
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
