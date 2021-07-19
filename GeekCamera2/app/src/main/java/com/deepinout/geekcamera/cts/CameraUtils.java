package com.deepinout.geekcamera.cts;

import android.hardware.Camera;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;


import java.util.Comparator;

/**
 * Utility class containing helper functions for the Camera CTS tests.
 */
public class CameraUtils {

    private static final String CAMERA_ID_INSTR_ARG_KEY = "camera-id";

    /**
     * Returns {@code true} if this device only supports {@code LEGACY} mode operation in the
     * Camera2 API for the given camera ID.
     *
     * @param manager The {@link CameraManager} used to retrieve camera characteristics.
     * @return {@code true} if this device only supports {@code LEGACY} mode.
     */
    public static boolean isLegacyHAL(CameraManager manager, String cameraIdStr) throws Exception {
        CameraCharacteristics characteristics =
                manager.getCameraCharacteristics(cameraIdStr);

        return characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL) ==
                CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY;
    }

    /**
     * Shared size comparison method used by size comparators.
     *
     * <p>Compares the number of pixels it covers.If two the areas of two sizes are same, compare
     * the widths.</p>
     */
    public static int compareSizes(int widthA, int heightA, int widthB, int heightB) {
        long left = widthA * (long) heightA;
        long right = widthB * (long) heightB;
        if (left == right) {
            left = widthA;
            right = widthB;
        }
        return (left < right) ? -1 : (left > right ? 1 : 0);
    }

    /**
     * Size comparator that compares the number of pixels it covers.
     *
     * <p>If two the areas of two sizes are same, compare the widths.</p>
     */
    public static class LegacySizeComparator implements Comparator<Camera.Size> {
        @Override
        public int compare(Camera.Size lhs, Camera.Size rhs) {
            return compareSizes(lhs.width, lhs.height, rhs.width, rhs.height);
        }
    }
}