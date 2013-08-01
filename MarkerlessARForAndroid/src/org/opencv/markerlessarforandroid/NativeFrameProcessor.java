package org.opencv.markerlessarforandroid;

import java.util.concurrent.Semaphore;
import org.opencv.core.Mat;

import android.os.Handler;

public class NativeFrameProcessor {
	
	private long nativeARPipelineObject = 0;
	
	private Mat buffer;
	private final Semaphore producerSemaphore = new Semaphore(1);
	private final Semaphore consumerSempahore = new Semaphore(0);
	private boolean exit = false;
	
	Handler uiFeedback;
	
	public NativeFrameProcessor(Handler uiFeedback, Mat[] trainingImages, float fx, float fy, float cx, float cy){
		this.uiFeedback = uiFeedback;
		int nImages = trainingImages.length;
		long[] images = new long[nImages];
		for (int i = 0; i < nImages; i++) {
			images[i] = trainingImages[i].getNativeObjAddr();
		}
		nativeARPipelineObject = nativeCreateObject(images, nImages, fx, fy, cx, cy);
		thread.start();
	}
	
	public int process() {
		try {
			consumerSempahore.acquire();
			int result = nativeProcess(nativeARPipelineObject, buffer.getNativeObjAddr());
			buffer = null;
			producerSemaphore.release();
			return result;
		} catch (InterruptedException e) {
		}
		return -1;
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
	
	private static native long nativeCreateObject(long[] images, int nImages, float fx, float fy, float cx, float cy);
	private static native int nativeProcess(long object, long frame); 
	private static native void nativeDestroyObject(long object);
	
	private Thread thread = new Thread() {
		
		boolean detected = false;
		
		@Override
		public void run() {
			int result = 0;
			while (!exit) {
				 result = process();
				 if (!detected && result == 1) {
					 	uiFeedback.sendEmptyMessage(0);
					} else if (detected && result == 0) {
						uiFeedback.sendEmptyMessage(1);
					}
					detected = result == 1;
			}
		}
	};
}
