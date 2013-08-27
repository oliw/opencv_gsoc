package org.opencv.markerlessarforandroid;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class NativeFrameProcessor {
	
	private static final String TAG = "MarkerlessAR::NativeFrameProcessor";

	protected MainActivity context;

	private long nativeARPipelineObject = 0;

//	private Mat buffer;
//	private boolean found;
//	private Mat pose3D;
//
//	private final Semaphore producerSemaphore = new Semaphore(1);
//	private final Semaphore consumerSempahore = new Semaphore(0);
//	private boolean exit = false;

	public NativeFrameProcessor(MainActivity context,
			Mat[] trainingImages, float fx, float fy, float cx, float cy) {
		this.context = context;
		int nImages = trainingImages.length;
		long[] images = new long[nImages];
		for (int i = 0; i < nImages; i++) {
			images[i] = trainingImages[i].getNativeObjAddr();
		}
		nativeARPipelineObject = nativeCreateObject(images, nImages, fx, fy,
				cx, cy);
		//thread.start();
	}
	
	/**
	 * Returns the 4x4 pose of the pattern in the scene in an OpenCV Mat format which is Row-Major
	 * @param frame a frame taken from the camera
	 * @return the pose of the pattern in the scene (null if not detected)
	 */
	public Mat processFrame(Mat frame) {
		boolean found = nativeProcess(nativeARPipelineObject,
				frame.getNativeObjAddr());
		if (found) {
			Mat pose = new Mat(4,4,CvType.CV_32F);
			nativeGetPose(nativeARPipelineObject, pose.getNativeObjAddr());
			return pose;
		} else {
			return null;
		}
	}

//	public boolean process() {
//		try {
//			consumerSempahore.acquire();
//			found = nativeProcess(nativeARPipelineObject,
//					buffer.getNativeObjAddr());
//			if (found) {
//				// Compute Pose
//				nativeGetPose(nativeARPipelineObject, pose3D.getNativeObjAddr());
//				context.getRenderer().setPatternPose(pose3D);
//			} else {
//				context.getRenderer().setPatternPose(null);
//			}
//			producerSemaphore.release();
//			return found;
//		} catch (InterruptedException e) {
//		}
//		return false;
//	}
//
//	public boolean patternFound() {
//		return found;
//	}
//
//	public void setFrame(Mat frame) {
//		if (producerSemaphore.tryAcquire()) {
//			buffer = frame;
//			consumerSempahore.release();
//		}
//	}

	public void release() {
//		exit = true;
		nativeDestroyObject(nativeARPipelineObject);
		nativeARPipelineObject = 0;
	}

	private static native long nativeCreateObject(long[] images, int nImages,
			float fx, float fy, float cx, float cy);

	private static native boolean nativeProcess(long object, long frame);

	private static native void nativeGetPose(long object, long pose);

	private static native void nativeDestroyObject(long object);

//	private Thread thread = new Thread() {
//
//		boolean previouslyFound = false;
//
//		@Override
//		public void run() {
//			boolean exit = false;
//			while (!exit) {
//				process();
//				if (!previouslyFound && found) {
//					context.showMessage("Pattern found");
//				} else if (previouslyFound && !found) {
//					context.showMessage("Pattern lost");
//				}
//				previouslyFound = found;
//			}
//		}
//	};
}
