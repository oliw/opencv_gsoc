#include <jni.h>
#include <opencv2/opencv.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>
#include "CameraCalibration.hpp"
#include "ARPipeline.hpp"

using namespace std;
using namespace cv;

extern "C" {

	JNIEXPORT jlong JNICALL
	Java_org_opencv_samples_markerlessarforandroid_NativeFrameProcessor_nativeCreateObject
	(JNIEnv *env, jobject obj, jlongArray images, jint imgCount, jfloat fx, jfloat fy, jfloat cx, jfloat cy)
	{
		CameraCalibration callib(fx, fy, cx, cy);

		jlong imagesBuff[imgCount];
		env->GetLongArrayRegion(images, 0, imgCount, imagesBuff);

		vector<Mat> arr(imgCount);
		for (int i = 0; i < imgCount; i++) {
			Mat *image;
			image = (Mat *)imagesBuff[i];
			arr[i] = *image;
		}

		return (long) new ARPipeline(arr, callib);
	}

	JNIEXPORT void JNICALL
	Java_org_opencv_samples_markerlessarforandroid_NativeFrameProcessor_nativeDestroyObject
	(JNIEnv *env, jobject obj, jlong object) {
		delete (ARPipeline *) object;
	}

	JNIEXPORT jboolean JNICALL
	Java_org_opencv_samples_markerlessarforandroid_NativeFrameProcessor_nativeProcess
	(JNIEnv *env, jobject obj, jlong object, jlong frame)
	{
		ARPipeline *pipeline = (ARPipeline *)object;
		Mat *mat = (Mat *)frame;

		return pipeline->processFrame(*mat);
	}

	JNIEXPORT void JNICALL
	Java_org_opencv_markerlessarforandroid_NativeFrameProcessor_nativeGetPose
	(JNIEnv *env, jobject obj, jlong object, jlong pose)
	{
//		ARPipeline *pipeline = (ARPipeline *)object;
//		Mat *pose3D = (Mat *)pose;
//
//		Mat patternLoc = pipeline->getPatternLocation();
//		memcpy(pose3D->data, patternLoc.data, patternLoc.step * patternLoc.rows);
	}
}
