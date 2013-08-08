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
	Java_org_opencv_markerlessarforandroid_NativeFrameProcessor_nativeCreateObject
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
	Java_org_opencv_markerlessarforandroid_NativeFrameProcessor_nativeDestroyObject
	(JNIEnv *env, jobject obj, jlong object) {
		delete (ARPipeline *) object;
	}

	JNIEXPORT jint JNICALL
	Java_org_opencv_markerlessarforandroid_NativeFrameProcessor_nativeProcess
	(JNIEnv *env, jobject obj, jlong object, jlong frame)
	{
		ARPipeline *pipeline = (ARPipeline *)object;
		Mat *mat = (Mat *)frame;

		bool isFound = pipeline->processFrame(*mat);
		return isFound ? 1 : 0;
	}

	JNIEXPORT jlong JNICALL
	Java_org_opencv_markerlessarforandroid_NativeFrameProcessor_nativeGetPose
	(JNIEnv *env, jobject obj, jlong object)
	{
		ARPipeline *pipeline = (ARPipeline *)object;
		const Transformation& transformation = pipeline->getPatternLocation();
		Matrix44 mat = transformation.getMat44();

		jclass jcClass = env->GetObjectClass(obj);
		jfieldID poseArrayId = env->GetFieldID(jcClass, "pose", "[F");
		jobject mvdata = env->GetObjectField(obj, poseArrayId);
		jfloatArray * poseArray = reinterpret_cast<jfloatArray*>(&mvdata);
		float * data = env->GetFloatArrayElements(*poseArray, NULL);
		for (int i = 0; i < 16 ; i++) {
			data[i] = mat.data[i];
		}
		env->ReleaseFloatArrayElements(*poseArray, data, 0);
	}
}
