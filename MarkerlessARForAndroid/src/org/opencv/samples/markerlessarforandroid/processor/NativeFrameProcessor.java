package org.opencv.samples.markerlessarforandroid.processor;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Handler;

/**
 * The NativeFrameProcessor acts as the interface between the Android
 * application and the Native C++ code performing the Computer Vision tasks.
 * 
 * @author Oliver Wilkie
 * 
 */
public class NativeFrameProcessor {
	
	Context context;

	private long nativeARPipelineObject = 0;

	Handler uiFeedback;

	private boolean wasFoundBefore;

	private Mat frameLowQualityBuffer;

//	public NativeFrameProcessor(Handler uiFeedback, Mat[] trainingImages,
//			float fx, float fy, float cx, float cy) {
//		frameLowQualityBuffer = new Mat(480, 640, CvType.CV_8UC4);
//		this.uiFeedback = uiFeedback;
//		wasFoundBefore = false;
//		int nImages = trainingImages.length;
//		long[] images = new long[nImages];
//		for (int i = 0; i < nImages; i++) {
//			images[i] = trainingImages[i].getNativeObjAddr();
//		}
//		nativeARPipelineObject = nativeCreateObject(images, nImages, fx, fy,
//				cx, cy);
//	}
	
	public NativeFrameProcessor(Handler uiFeedback, Context context,
			float fx, float fy, float cx, float cy) {
		frameLowQualityBuffer = new Mat(480, 640, CvType.CV_8UC4);
		this.uiFeedback = uiFeedback;
		wasFoundBefore = false;
		nativeARPipelineObject = nativeCreateObject3(context.getAssets(), fx, fy,
				cx, cy);
	}

	public boolean processFrame(Mat frame) {
		// Create lower quality version of frame
		Imgproc.resize(frame, frameLowQualityBuffer,
				frameLowQualityBuffer.size());
		// Process lower quality version of frame
		boolean found = nativeProcess(nativeARPipelineObject,
				frameLowQualityBuffer.getNativeObjAddr());
		if (found && !wasFoundBefore) {
			uiFeedback.sendEmptyMessage(0);
			wasFoundBefore = found;
		} else if (!found && wasFoundBefore) {
			uiFeedback.sendEmptyMessage(1);
			wasFoundBefore = found;
		}
		return found;
	}

	public Mat getPose() {
		Mat pose = new Mat(4, 4, CvType.CV_32F);
		nativeGetPose(nativeARPipelineObject, pose.getNativeObjAddr());
		return pose;
	}

	public void release() {
		nativeDestroyObject(nativeARPipelineObject);
		nativeARPipelineObject = 0;
	}
	
	public void savePatterns(String directory) {
		nativeSavePatterns(nativeARPipelineObject,directory);
	}

	//private static native long nativeCreateObject(long[] images, int nImages, float fx, float fy, float cx, float cy);
		
	private static native long nativeCreateObject3(AssetManager manager, float fx, float fy, float cx, float cy);

	private static native boolean nativeProcess(long object, long frame);

	private static native void nativeGetPose(long object, long pose);

	private static native void nativeDestroyObject(long object);

	private static native void nativeSavePatterns(long object, String path);
}