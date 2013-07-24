#include <jni.h>
#include <opencv2/opencv.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>
#include "CameraCalibration.hpp"
#include "ARPipeline.hpp"
#include "ARDrawingContext.hpp"

using namespace std;
using namespace cv;

extern "C" {


	JNIEXPORT jlong JNICALL
	Java_org_opencv_markerlessarforandroid_FullscreenActivity_initCameraCallibration
	(JNIEnv *env, jobject obj, jfloat fx, jfloat fy, jfloat cx, jfloat cy)
	{
		CameraCalibration *callib = new CameraCalibration(fx, fy, cx, cy);
		long pointer = (long) callib;
		return pointer;
	}

	JNIEXPORT void JNICALL
	Java_org_opencv_markerlessarforandroid_FullscreenActivity_releaseCameraCallibration
	(JNIEnv *env, jobject obj, jlong pointer)
	{
		CameraCalibration *callib;
		callib = (CameraCalibration *)pointer;
		delete callib;
	}

	JNIEXPORT jlong JNICALL
	Java_org_opencv_markerlessarforandroid_FullscreenActivity_initARPipeline
	(JNIEnv *env, jobject obj, jlongArray images, jint imgCount, jlong callibPoint)
	{
		CameraCalibration *callib;
		callib = (CameraCalibration *)callibPoint;

		jlong imagesBuff[imgCount];
		env->GetLongArrayRegion(images, 0, imgCount, imagesBuff);

		vector<Mat> arr(imgCount);
		for (int i = 0; i < imgCount; i++) {
			Mat *image;
			image = (Mat *)imagesBuff[i];
			arr[i] = *image;
		}

		ARPipeline *pipeline = new ARPipeline(arr, *callib);
		long pointer = (long) pipeline;
		return pointer;
	}

	JNIEXPORT void JNICALL
	Java_org_opencv_markerlessarforandroid_FullscreenActivity_releaseARPipeline
	(JNIEnv *env, jobject obj, jlong pointer)
	{
		ARPipeline *pipeline;
		pipeline = (ARPipeline *)pointer;
		delete(pipeline);
	}


	JNIEXPORT jint JNICALL
	Java_org_opencv_markerlessarforandroid_FullscreenActivity_processFrame
	(JNIEnv *env, jobject obj, jlong addrFrame, jlong addrPipeline)
	{
		Mat* mat = (Mat*) addrFrame;
		Mat frame = mat->clone();

		ARPipeline *pipeline = (ARPipeline *)addrPipeline;

		bool isFound = pipeline->processFrame(frame);
		return isFound ? 1 : 0;
	}

}
