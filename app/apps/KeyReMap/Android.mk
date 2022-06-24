LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-subdir-java-files)
LOCAL_PACKAGE_NAME := KeyReMap
LOCAL_CERTIFICATE := platform
LOCAL_PRIVATE_PLATFORM_APIS := true
LOCAL_PROGUARD_FLAG_FILES := proguard.cfg
LOCAL_JAVA_LIBRARIES := com.ubx.platform
LOCAL_STATIC_JAVA_LIBRARIES := \
    FlowLayout

include $(BUILD_PACKAGE)

include $(CLEAR_VARS)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := \
   FlowLayout:libs/custom.views.jar 
include $(BUILD_MULTI_PREBUILT)

# ====  scanner default property   ========================
include $(CLEAR_VARS)
LOCAL_MODULE := default_keymap.xml

LOCAL_SRC_FILES := $(LOCAL_MODULE)

LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_CLASS := ETC
LOCAL_MODULE_PATH := $(TARGET_OUT_ETC)
include $(BUILD_PREBUILT)
