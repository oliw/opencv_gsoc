package org.opencv.markerlessarforandroid;

import org.opencv.core.Mat;

import android.os.Handler;

public class NativeFrameProcessor {
	
	private long nativeARPipelineObject = 0;
		
	Handler uiFeedback;
	
	boolean wasFoundBefore;
	
	public NativeFrameProcessor(Handler uiFeedback, Mat[] trainingImages, float fx, float fy, float cx, float cy){
		this.uiFeedback = uiFeedback;
		wasFoundBefore = false;
		int nImages = trainingImages.length;
		long[] images = new long[nImages];
		for (int i = 0; i < nImages; i++) {
			images[i] = trainingImages[i].getNativeObjAddr();
		}
		nativeARPipelineObject = nativeCreateObject(images, nImages, fx, fy, cx, cy);
	}
	
	public boolean processFrame(Mat frame) {
		boolean found = nativeProcess(nativeARPipelineObject, frame.getNativeObjAddr());
		if (found && !wasFoundBefore) {
			uiFeedback.sendEmptyMessage(0);
			wasFoundBefore = found;
		} else if (!found && wasFoundBefore) {
			uiFeedback.sendEmptyMessage(1);
			wasFoundBefore = found;
		}
		return found;
	}
		
	public void release() {
		nativeDestroyObject(nativeARPipelineObject);
		nativeARPipelineObject = 0;
	}
	
	private static native long nativeCreateObject(long[] images, int nImages, float fx, float fy, float cx, float cy);
	private static native boolean nativeProcess(long object, long frame); 
	private static native void nativeDestroyObject(long object);
}
