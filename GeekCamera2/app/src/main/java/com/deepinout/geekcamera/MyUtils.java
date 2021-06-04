package com.deepinout.geekcamera;

import android.hardware.camera2.CameraCharacteristics;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class MyUtils {
    private static final String TAG = "GC2_MyUtils";

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
}
