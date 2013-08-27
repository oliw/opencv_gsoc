package org.opencv.markerlessarforandroid;

/**
 * Each camera lens has unique intrinsic parameters such as focal length, principle point and lens distortion model.
 * The focal length is the distance of the camera film to the lens when focused on subject infinitely away.
 * The principle point is the position of the point where the optical axis meets the image plane.
 * The lens distortion model models how straight lines are warped by the lens.
 * 
 * Whatever we want to draw onto the scene should be warped in the same way the actual scene has been warped by the camera.
 *
 */
public class CameraCalibration {
	
	float fx; // The focal length in the X axis. 
	float fy; 
	float cx; // The x component of the principle point
	float cy; // The y component of the principle point
	
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
