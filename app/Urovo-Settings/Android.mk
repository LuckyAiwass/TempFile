LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_PACKAGE_NAME := Urovo-Settings
LOCAL_PRIVATE_PLATFORM_APIS :=true
LOCAL_STATIC_JAVA_LIBRARIES := \
    UZxing \
    android-support-v4 \
    SaxParseXml \
    settingslib
#LOCAL_JAVA_LIBRARIES := android.urovo.device
LOCAL_CERTIFICATE := platform
include $(BUILD_PACKAGE)


include $(CLEAR_VARS)


LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := \
   UZxing:libs/Uzxing.jar \
   settingslib:libs/settingslib.jar
 #  SaxParseXml:libs/SaxParseXml.jar \
#   android-support-v4:libs/android-support-v4.jar

include $(BUILD_MULTI_PREBUILT)
