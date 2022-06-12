package com.deepinout.geekcamera;

import android.os.Build;
import android.os.Trace;

public class GeekCamera2Trace {
    public static final String GET_CAMERA_MANAGER = "GC2_App_create_CameraManager";
    public static final String OPEN_CAMERA = "GC2_App_openCamera";
    public static final String CREATE_CAPTURE_SESSION = "GC2_App_createCaptureSession";
    public static final String OPEN_CAMERA_AYSNC_TASK = "GC2_App_AysncTask_Switch";
    public static final String OPEN_CAMERA_CORE = "GC2_App_openCameraCore";
    public static final String SETUP_CAMERA = "GC2_App_setupCamera";
    public static final String SET_REPEATING_REQUEST = "GC2_App_setRepeatingRequest";
    public static final String FIRST_PREVIEW_BUFFER = "GC2_App_firstPreviewBuffer";
    public static final String FRAME_NUMBER = "GC2_App_FrameNumber";
    //Add for close camera
    public static final String CC2_RELEASE = "GC2_CC2_Release";
    public static final String CAMERA_DEVICE_RELEASE = "GC2_CameraDevice_Release";
    public static boolean isFirstPreviewBuffer = false;
    public static void beginAsyncSection(String methodName, int cookie) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && Trace.isEnabled()) {
            Trace.beginAsyncSection(methodName, cookie);
        }
    }

    public static void endAsyncSection(String methodName, int cookie) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && Trace.isEnabled()) {
            Trace.endAsyncSection(methodName, cookie);
        }
    }

    public static void beginSection(String sectionName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&Trace.isEnabled()) {
            Trace.beginSection(sectionName);
        }
    }

    public static void endSection() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&Trace.isEnabled()) {
            Trace.endSection();
        }
    }

    public static void setCounter(String counterName, long counterValue) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&Trace.isEnabled()) {
            Trace.setCounter(counterName, counterValue);
        }
    }
}
