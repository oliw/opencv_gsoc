package org.opencv.markerlessarforandroid;

public class CameraCalibration {
	
	float fx;
	float fy;
	float cx;
	float cy;
	
	public CameraCalibration(float fx, float fy, float cx, float cy) {
		this.fx = fx;
		this.fy = fy;
		this.cx = cx;
		this.cy = cy;
	}

	public float getFx() {
		return fx;
	}

	public float getFy() {
		return fy;
	}

	public float getCx() {
		return cx;
	}

	public float getCy() {
		return cy;
	}

}
