LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := message
LOCAL_SRC_FILES := message.cpp

include $(BUILD_SHARED_LIBRARY)
