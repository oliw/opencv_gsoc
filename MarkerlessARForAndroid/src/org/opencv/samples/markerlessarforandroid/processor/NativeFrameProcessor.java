package org.opencv.samples.markerlessarforandroid.processor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

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
	File patternsDir;
	
	private long nativeARPipelineObject = 0;
	Handler uiFeedback;
	private boolean wasFoundBefore;
	private Mat frameLowQualityBuffer;
	
	public NativeFrameProcessor(Handler uiFeedback, Context context,
			float fx, float fy, float cx, float cy) {
		this.context = context;
		frameLowQualityBuffer = new Mat(480, 640, CvType.CV_8UC4);
		this.uiFeedback = uiFeedback;
		wasFoundBefore = false;
		String[] patternPaths = loadPatternResources();
		nativeARPipelineObject = nativeCreateObject3(patternPaths, fx, fy,
				cx, cy);
	}
	
	// Load patterns from assets
	private String[] loadPatternResources() {
		String[] patternPaths = null;
		AssetManager am = context.getAssets();
		try {
			String[] patterns = am.list("training-patterns");
			patternPaths = new String[patterns.length];
			for (int i = 0; i < patterns.length; i++) {
				String pattern = patterns[i];
				InputStream is = am.open("training-patterns/"+pattern);
				patternsDir = context.getDir("pattern", Context.MODE_PRIVATE);
				File patternFile = new File(patternsDir,pattern);
				FileOutputStream os = new FileOutputStream(patternFile);
				
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                is.close();
                os.close();
                
                patternPaths[i] = patternFile.getAbsolutePath();
			}
		} catch (IOException e) {}
		return patternPaths;
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
		
	private static native long nativeCreateObject3(String[] patternPaths, float fx, float fy, float cx, float cy);

	private static native boolean nativeProcess(long object, long frame);

	private static native void nativeGetPose(long object, long pose);

	private static native void nativeDestroyObject(long object);

	private static native void nativeSavePatterns(long object, String path);
}
