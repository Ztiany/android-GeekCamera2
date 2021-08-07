package com.deepinout.geekcamera;

import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.MeteringRectangle;
import android.util.Log;

import com.deepinout.geekcamera.cameracontroller.CameraController;

import java.util.ArrayList;
import java.util.List;

public class MyUtils {
    private static final String TAG = "GC2_MyUtils";

    public static void printCaptureRequest(CaptureRequest captureRequest, int template) {
        List<CaptureRequest.Key<?>> keys = captureRequest.getKeys();
        Log.i(TAG, "template:" + template);
        for (CaptureRequest.Key key : keys) {
            if (captureRequest.get(key) instanceof MeteringRectangle[]) {
                MeteringRectangle[] meteringRectangles = (MeteringRectangle[]) captureRequest.get(key);
                Log.i(TAG, key.getName() + ":" + meteringRectangles[0].toString() + ",");
            } else {
                Log.i(TAG, key.getName() + ":" + captureRequest.get(key).toString() + ",");
            }
        }
    }

    public static List<String> convertCapabilityToString(int[] capabilities) {
        List<String> output_capabilities_string = new ArrayList<>();
        if(capabilities.length == 0) {
            if( MyDebug.LOG )
                Log.i(TAG, "no capabilities.");
            return output_capabilities_string;
        }

        List<Integer> supported_capabilities_int = new ArrayList<>();
        for(Integer supported_capability : capabilities) {
            supported_capabilities_int.add(supported_capability);
        }

        if(supported_capabilities_int.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_BACKWARD_COMPATIBLE) ) {
            output_capabilities_string.add("BACKWARD_COMPATIBLE");
        }

        if(supported_capabilities_int.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_BURST_CAPTURE) ) {
            output_capabilities_string.add("BURST_CAPTURE");
        }

        if(supported_capabilities_int.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_CONSTRAINED_HIGH_SPEED_VIDEO) ) {
            output_capabilities_string.add("CONSTRAINED_HIGH_SPEED_VIDEO");
        }

        if(supported_capabilities_int.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_DEPTH_OUTPUT) ) {
            output_capabilities_string.add("DEPTH_OUTPUT");
        }

        if(supported_capabilities_int.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_LOGICAL_MULTI_CAMERA) ) {
            output_capabilities_string.add("LOGICAL_MULTI_CAMERA");
        }

        if(supported_capabilities_int.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_POST_PROCESSING) ) {
            output_capabilities_string.add("MANUAL_POST_PROCESSING");
        }

        if(supported_capabilities_int.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR) ) {
            output_capabilities_string.add("MANUAL_SENSOR");
        }

        if(supported_capabilities_int.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_MONOCHROME) ) {
            output_capabilities_string.add("MONOCHROME");
        }

        if(supported_capabilities_int.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_MOTION_TRACKING) ) {
            output_capabilities_string.add("MOTION_TRACKING");
        }

        if(supported_capabilities_int.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_PRIVATE_REPROCESSING) ) {
            output_capabilities_string.add("PRIVATE_REPROCESSING");
        }

        if(supported_capabilities_int.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_RAW) ) {
            output_capabilities_string.add("RAW");
        }

        if(supported_capabilities_int.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_READ_SENSOR_SETTINGS) ) {
            output_capabilities_string.add("READ_SENSOR_SETTINGS");
        }

        if(supported_capabilities_int.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_SECURE_IMAGE_DATA) ) {
            output_capabilities_string.add("SECURE_IMAGE_DATA");
        }

        if(supported_capabilities_int.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_YUV_REPROCESSING) ) {
            output_capabilities_string.add("YUV_REPROCESSING");
        }

//         if(supported_capabilities_int.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_OFFLINE_PROCESSING) ) {
//             output_capabilities_string.add("OFFLINE_PROCESSING");
//         }
//
//         if(supported_capabilities_int.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_SYSTEM_CAMERA) ) {
//             output_capabilities_string.add("SYSTEM_CAMERA");
//         }
//
//         if(supported_capabilities_int.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_ULTRA_HIGH_RESOLUTION_SENSOR) ) {
//             output_capabilities_string.add("ULTRA_HIGH_RESOLUTION_SENSOR");
//         }
//
//         if(supported_capabilities_int.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_REMOSAIC_REPROCESSING) ) {
//             output_capabilities_string.add("REMOSAIC_REPROCESSING");
//         }

        return output_capabilities_string;
    }

    public static String formatToString(int format) {
        switch (format) {
            case ImageFormat.YV12:
                return "YV12" + "(" + format + ")";
            case ImageFormat.YUV_420_888:
                return "YUV_420_888" + "(" + format + ")";
            case ImageFormat.NV21:
                return "NV21" + "(" + format + ")";
            case ImageFormat.NV16:
                return "NV16" + "(" + format + ")";
            case PixelFormat.RGB_565:
                return "RGB_565" + "(" + format + ")";
            case PixelFormat.RGBA_8888:
                return "RGBA_8888" + "(" + format + ")";
            case PixelFormat.RGBX_8888:
                return "RGBX_8888" + "(" + format + ")";
            case PixelFormat.RGB_888:
                return "RGB_888" + "(" + format + ")";
            case ImageFormat.JPEG:
                return "JPEG" + "(" + format + ")";
            case ImageFormat.YUY2:
                return "YUY2" + "(" + format + ")";
            case ImageFormat.Y8:
                return "Y8" + "(" + format + ")";
//            case ImageFormat.Y16:
//                return "Y16";
            case ImageFormat.RAW_SENSOR:
                return "RAW_SENSOR" + "(" + format + ")";
            case ImageFormat.RAW_PRIVATE:
                return "RAW_PRIVATE" + "(" + format + ")";
            case ImageFormat.RAW10:
                return "RAW10" + "(" + format + ")";
            case ImageFormat.DEPTH16:
                return "DEPTH16" + "(" + format + ")";
            case ImageFormat.DEPTH_POINT_CLOUD:
                return "DEPTH_POINT_CLOUD" + "(" + format + ")";
            case ImageFormat.DEPTH_JPEG:
                return "DEPTH_JPEG" + "(" + format + ")";
//            case ImageFormat.RAW_DEPTH:
//                return "RAW_DEPTH";
            case ImageFormat.PRIVATE:
                return "PRIVATE" + "(" + format + ")";
            case ImageFormat.HEIC:
                return "HEIC" + "(" + format + ")";
            default:
                return "" + format;
        }
    }
}
