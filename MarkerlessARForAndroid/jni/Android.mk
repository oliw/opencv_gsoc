# There is one Android.mk per module
ANDROID_LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
include ~/Code/GSOC/MarkerlessAR/Android.mk
LOCAL_PATH := $(ANDROID_LOCAL_PATH)

include $(CLEAR_VARS)
include /home/oli/NVPACK/OpenCV-2.4.5-Tegra-sdk/sdk/native/jni/OpenCV-tegra3.mk 
LOCAL_C_INCLUDES += $(LOCAL_PATH)
LOCAL_MODULE := ar-jni
LOCAL_SRC_FILES := NativeFrameProcessor_jni.cpp
LOCAL_SHARED_LIBRARIES := ar
include $(BUILD_SHARED_LIBRARY) 