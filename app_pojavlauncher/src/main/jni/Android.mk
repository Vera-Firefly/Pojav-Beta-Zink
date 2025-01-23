LOCAL_PATH := $(call my-dir)
HERE_PATH := $(LOCAL_PATH)

# include $(HERE_PATH)/crash_dump/libbase/Android.mk
# include $(HERE_PATH)/crash_dump/libbacktrace/Android.mk
# include $(HERE_PATH)/crash_dump/debuggerd/Android.mk


LOCAL_PATH := $(HERE_PATH)

$(call import-module,prefab/bytehook)
LOCAL_PATH := $(HERE_PATH)


include $(CLEAR_VARS)
LOCAL_MODULE := angle_gles2
LOCAL_SRC_FILES := angle/angle-gles/$(TARGET_ARCH_ABI)/libGLESv2_angle.so
include $(PREBUILT_SHARED_LIBRARY)


include $(CLEAR_VARS)
LOCAL_MODULE := Angle
LOCAL_SHARED_LIBRARIES := angle_gles2
LOCAL_SRC_FILES := angle/main.c angle/string_utils.c
LOCAL_C_INCLUDES := $(LOCAL_PATH)/angle
LOCAL_CFLAGS += -g -rdynamic
include $(BUILD_SHARED_LIBRARY)


include $(CLEAR_VARS)
LOCAL_LDLIBS := -ldl -llog -landroid
LOCAL_MODULE := pojavexec
LOCAL_SHARED_LIBRARIES := driver_helper bytehook
LOCAL_CFLAGS += -g -rdynamic

LOCAL_SRC_FILES := \
    bigcoreaffinity.c \
    egl_bridge.c \
    ctxbridges/gl_bridge.c \
    ctxbridges/osm_bridge.c \
    ctxbridges/osm_bridge_xxx1.c \
    ctxbridges/osm_bridge_xxx2.c \
    ctxbridges/osm_bridge_xxx3.c \
    ctxbridges/egl_loader.c \
    ctxbridges/osmesa_loader.c \
    ctxbridges/swap_interval_no_egl.c \
    ctxbridges/virgl_bridge.c \
    environ/environ.c \
    input_bridge_v3.c \
    jre_launcher.c \
    utils.c \
    stdio_is.c

ifeq ($(TARGET_ARCH_ABI),arm64-v8a)
LOCAL_CFLAGS += -DADRENO_POSSIBLE
LOCAL_LDLIBS += -lEGL -lGLESv2
endif
include $(BUILD_SHARED_LIBRARY)


include $(CLEAR_VARS)
LOCAL_LDLIBS := -ldl -llog -landroid
LOCAL_MODULE := driver_helper
LOCAL_SRC_FILES := \
    driver_helper/driver_helper.c \
    driver_helper/nsbypass.c
LOCAL_CFLAGS += -g -rdynamic

ifeq ($(TARGET_ARCH_ABI),arm64-v8a)
LOCAL_CFLAGS += -DADRENO_POSSIBLE
LOCAL_LDLIBS += -lEGL -lGLESv2
endif
include $(BUILD_SHARED_LIBRARY)


include $(CLEAR_VARS)
LOCAL_MODULE := linkerhook
LOCAL_SRC_FILES := \
    linkerhook/linkerhook.cpp \
    linkerhook/linkerns.c
LOCAL_LDFLAGS := -z global
include $(BUILD_SHARED_LIBRARY)


include $(CLEAR_VARS)
LOCAL_MODULE := pojavexec_awt
LOCAL_SRC_FILES := \
    awt_bridge.c
include $(BUILD_SHARED_LIBRARY)


include $(CLEAR_VARS)
LOCAL_MODULE := awt_headless
include $(BUILD_SHARED_LIBRARY)


LOCAL_PATH := $(HERE_PATH)/awt_xawt
include $(CLEAR_VARS)
LOCAL_MODULE := awt_xawt
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)
LOCAL_SHARED_LIBRARIES := awt_headless
LOCAL_SRC_FILES := xawt_fake.c
include $(BUILD_SHARED_LIBRARY)

$(info $(shell (rm $(HERE_PATH)/../jniLibs/*/libawt_headless.so)))

