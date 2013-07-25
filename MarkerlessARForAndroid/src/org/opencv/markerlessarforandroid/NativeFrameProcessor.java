package org.opencv.markerlessarforandroid;

import org.opencv.core.Mat;

public class NativeFrameProcessor {
		
	private long nativeARPipelineObject = 0;
	
	public NativeFrameProcessor(Mat[] trainingImages, float fx, float fy, float cx, float cy){
		int nImages = trainingImages.length;
		long[] images = new long[nImages];
		for (int i = 0; i < nImages; i++) {
			images[i] = trainingImages[i].getNativeObjAddr();
		}
		nativeARPipelineObject = nativeCreateObject(images, nImages, fx, fy, cx, cy);
	}
	
	public int process(Mat frame) {
		return nativeProcess(nativeARPipelineObject, frame.getNativeObjAddr());
	}
	
	public void release() {
		nativeDestroyObject(nativeARPipelineObject);
		nativeARPipelineObject = 0;
	}
	
	private static native long nativeCreateObject(long[] images, int nImages, float fx, float fy, float cx, float cy);
	private static native int nativeProcess(long object, long frame); 
	private static native void nativeDestroyObject(long object);
}
