#include <jni.h>
#include <opencv2/opencv.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>
#include "CameraCalibration.hpp"
#include "ARPipeline.hpp"

#ifdef ANDROID_NDK_PROFILER_ENABLED
#include <prof.h>
#endif

using namespace std;
using namespace cv;

extern "C" {

JNIEXPORT jlong JNICALL
Java_org_opencv_samples_markerlessarforandroid_NativeFrameProcessor_nativeCreateObject(
		JNIEnv *env, jobject obj, jlongArray images, jint imgCount, jfloat fx,
		jfloat fy, jfloat cx, jfloat cy) {
	CameraCalibration callib(fx, fy, cx, cy);

	jlong imagesBuff[imgCount];
	env->GetLongArrayRegion(images, 0, imgCount, imagesBuff);

	vector<Mat> arr(imgCount);
	for (int i = 0; i < imgCount; i++) {
		Mat *image;
		image = (Mat *) imagesBuff[i];
		arr[i] = *image;
	}

	ARPipeline* p = new ARPipeline(arr, callib);
#ifdef ANDROID_NDK_PROFILER_ENABLED
	setenv("CPUPROFILE_FREQUENCY", "500", 1); /* Change to 500 interrupts per second */
	monstartup("libar-jni.so");
#endif
	return (long) p;
}

JNIEXPORT jlong JNICALL
Java_org_opencv_samples_markerlessarforandroid_NativeFrameProcessor_nativeCreateObject2(
		JNIEnv *env, jobject obj, jobjectArray stringArray, jfloat fx,
		jfloat fy, jfloat cx, jfloat cy) {
	CameraCalibration callib(fx, fy, cx, cy);

	int stringCount = env->GetArrayLength(stringArray);
	vector<string> yamls(stringCount);

	for (int i = 0; i<stringCount; i++) {
		jstring string = (jstring) env->GetObjectArrayElement(stringArray, i);
		const char *rawstring = env->GetStringUTFChars(string,0);
		std::string s(rawstring);
		yamls[i] = s;
		env->ReleaseStringUTFChars(string,rawstring);
	}

	ARPipeline* p = new ARPipeline(yamls, callib);
#ifdef ANDROID_NDK_PROFILER_ENABLED
	setenv("CPUPROFILE_FREQUENCY", "500", 1); /* Change to 500 interrupts per second */
	monstartup("libar-jni.so");
#endif
	return (long) p;
}

JNIEXPORT void JNICALL
Java_org_opencv_samples_markerlessarforandroid_NativeFrameProcessor_nativeDestroyObject(
		JNIEnv *env, jobject obj, jlong object) {
	delete (ARPipeline *) object;
#ifdef ANDROID_NDK_PROFILER_ENABLED
	moncleanup();
#endif
}

JNIEXPORT jboolean JNICALL
Java_org_opencv_samples_markerlessarforandroid_NativeFrameProcessor_nativeProcess(
		JNIEnv *env, jobject obj, jlong object, jlong frame) {
	ARPipeline *pipeline = (ARPipeline *) object;
	Mat *mat = (Mat *) frame;
	return pipeline->processFrame(*mat);
}

JNIEXPORT void JNICALL
Java_org_opencv_samples_markerlessarforandroid_NativeFrameProcessor_nativeGetPose(
		JNIEnv *env, jobject obj, jlong object, jlong pose) {
	ARPipeline *pipeline = (ARPipeline *) object;
	Mat *pose3D = (Mat *) pose;

	Matx44f patternLoc = pipeline->getPatternLocation().getMat44();
	*pose3D = Mat(patternLoc);
}

JNIEXPORT void JNICALL
Java_org_opencv_samples_markerlessarforandroid_NativeFrameProcessor_nativeSavePatterns(JNIEnv *env, jclass obj, jlong object, jstring path) {
	const char *s = env->GetStringUTFChars(path,NULL);
	std::string str(s);
	env->ReleaseStringUTFChars(path,s);
	ARPipeline *pipeline = (ARPipeline *) object;
	pipeline->savePatterns(str);
}
}
