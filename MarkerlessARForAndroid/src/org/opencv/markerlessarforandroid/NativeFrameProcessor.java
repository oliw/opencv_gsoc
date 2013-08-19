package org.opencv.markerlessarforandroid;

import java.util.concurrent.Semaphore;
import org.opencv.core.Mat;

import android.os.Handler;

public class NativeFrameProcessor {

	protected FullscreenActivity context;

	private long nativeARPipelineObject = 0;

	private Mat buffer;
	private boolean found;
	private Mat pose3D;

	private final Semaphore producerSemaphore = new Semaphore(1);
	private final Semaphore consumerSempahore = new Semaphore(0);
	private boolean exit = false;

	public NativeFrameProcessor(FullscreenActivity context,
			Mat[] trainingImages, float fx, float fy, float cx, float cy) {
		this.context = context;
		int nImages = trainingImages.length;
		long[] images = new long[nImages];
		for (int i = 0; i < nImages; i++) {
			images[i] = trainingImages[i].getNativeObjAddr();
		}
		pose3D = new Mat();
		nativeARPipelineObject = nativeCreateObject(images, nImages, fx, fy,
				cx, cy);
		thread.start();
	}

	public boolean process() {
		try {
			consumerSempahore.acquire();
			found = nativeProcess(nativeARPipelineObject,
					buffer.getNativeObjAddr());
			if (found) {
				// Compute Pose
				nativeGetPose(nativeARPipelineObject, pose3D.getNativeObjAddr());
			} else {

			}
			producerSemaphore.release();
			return found;
		} catch (InterruptedException e) {
		}
		return false;
	}

	public boolean patternFound() {
		return found;
	}

	public void setFrame(Mat frame) {
		if (producerSemaphore.tryAcquire()) {
			buffer = frame;
			consumerSempahore.release();
		}
	}

	public void release() {
		exit = true;
		nativeDestroyObject(nativeARPipelineObject);
		nativeARPipelineObject = 0;
	}

	public Mat getPose() {
		return pose3D;
	}

	private static native long nativeCreateObject(long[] images, int nImages,
			float fx, float fy, float cx, float cy);

	private static native boolean nativeProcess(long object, long frame);

	private static native void nativeGetPose(long object, long pose);

	private static native void nativeDestroyObject(long object);

	private Thread thread = new Thread() {

		boolean detected = false;

		@Override
		public void run() {
			boolean result = false;
			while (!exit) {
				result = process();
				if (!detected && result) {
					context.showMessage("Pattern found");
				} else if (detected && !result) {
					context.showMessage("Pattern lost");
				}
				detected = result;
			}
		}
	};
}
