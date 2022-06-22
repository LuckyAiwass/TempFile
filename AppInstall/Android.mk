LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)


LOCAL_SRC_FILES := $(call all-subdir-java-files)
LOCAL_PACKAGE_NAME := AppInstall
LOCAL_MODULE_TAGS := optional
LOCAL_CERTIFICATE := platform


LOCAL_AAPT_FLAGS := \
    --auto-add-overlay \
    --extra-packages android.support.v7.appcompat \
    --extra-packages android.support.v7.recyclerview \
    --extra-packages android.support.v7.preference 

LOCAL_STATIC_JAVA_LIBRARIES := \
    android-support-v7-appcompat \
    android-support-v4 \
    android-support-v7-recyclerview \
    android-support-v7-preference \
    zxing

LOCAL_PROGUARD_ENABLED := disabled	

LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/res \
    frameworks/support/v7/preference/res \
    frameworks/support/v14/preference/res \
    frameworks/support/v7/appcompat/res \
    frameworks/support/v7/recyclerview/res

LOCAL_PREBUILT_STATIC_JAVA_LIBARIES := \
    zxing:libs/zxing.jar
LOCAL_DEX_PREOPT := false

include $(BUILD_PACKAGE)
include $(CLEAR_VARS)
LOCAL_MODULE_CLASS := JAVA_LIBRARIES
LOCAL_MODULE := zxing
LOCAL_SRC_FILES := libs/zxing.jar
LOCAL_UNINSTALLABLE_MODULE := true
include $(BUILD_PREBUILT)

include $(call all-makefiles-under,$(LOCAL_PATH))
