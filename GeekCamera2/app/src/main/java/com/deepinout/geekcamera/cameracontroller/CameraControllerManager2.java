package com.deepinout.geekcamera.cameracontroller;

import com.deepinout.geekcamera.MyDebug;
import com.deepinout.geekcamera.MyUtils;
import com.deepinout.geekcamera.R;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.media.MediaCodec;
import android.media.MediaRecorder;
import android.os.Build;
import android.renderscript.Allocation;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.util.SizeF;
import android.view.SurfaceHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Provides support using Android 5's Camera 2 API
 *  android.hardware.camera2.*.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class CameraControllerManager2 extends CameraControllerManager {
    private static final String TAG = "GC2_CCM2";

    private final Context mContext;
    CameraManager mCameraManager;
    private boolean mPrintedInfo;

    public CameraControllerManager2(Context context) {
        this.mContext = context;
        mCameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        mPrintedInfo = false;
    }

    @Override
    public int getNumberOfCameras() {
        try {
            String[] cameraIdArray = mCameraManager.getCameraIdList();
            if(MyDebug.LOG) {
                Log.d(TAG, "getCameraIdList length:" + cameraIdArray.length);
            }
            if (!mPrintedInfo) {
                for (int i = 0; i < cameraIdArray.length; i++) {
                    isLogicalMultiCamera(mContext, i);
                    if(MyDebug.LOG) {
                        Log.d(TAG, "CameraID:" +i + ", Facing:" + getFacing(i));
                    }
                    getHardwareLevel(mContext, i);
                    printStreamConfigurationMap(mContext, i);
                    printAvailableSessionKeys(mContext, i);
                    printAvailableRequestKeys(mContext, i);
                    printAvailableResultKeys(mContext, i);
                    printAvailableStaticKeys(mContext, i);
                    printAvailableControlModes(mContext, i);
                    printAvailableAEModes(mContext, i);
                }
                mPrintedInfo = true;
            }
            return cameraIdArray.length;
        } catch(Throwable e) {
            if(MyDebug.LOG) {
                Log.e(TAG, "exception trying to get camera ids");
            }
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public CameraController.Facing getFacing(int cameraId) {
        try {
            String cameraIdS = mCameraManager.getCameraIdList()[cameraId];
            CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraIdS);
            switch(characteristics.get(CameraCharacteristics.LENS_FACING)) {
                case CameraMetadata.LENS_FACING_FRONT:
                    return CameraController.Facing.FACING_FRONT;
                case CameraMetadata.LENS_FACING_BACK:
                    return CameraController.Facing.FACING_BACK;
                case CameraMetadata.LENS_FACING_EXTERNAL:
                    return CameraController.Facing.FACING_EXTERNAL;
            }
            Log.e(TAG, "unknown camera_facing: " + characteristics.get(CameraCharacteristics.LENS_FACING));
        } catch(Throwable e) {
            if(MyDebug.LOG)
                Log.e(TAG, "exception trying to get camera characteristics");
            e.printStackTrace();
        }
        return CameraController.Facing.FACING_UNKNOWN;
    }

    @Override
    public String getDescription(Context context, int cameraId) {
        String description = null;
        try {
            String cameraIdS = mCameraManager.getCameraIdList()[cameraId];
            CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraIdS);

            switch( characteristics.get(CameraCharacteristics.LENS_FACING) ) {
                case CameraMetadata.LENS_FACING_FRONT:
                    description = context.getResources().getString(R.string.front_camera);
                    break;
                case CameraMetadata.LENS_FACING_BACK:
                    description = context.getResources().getString(R.string.back_camera);
                    break;
                case CameraMetadata.LENS_FACING_EXTERNAL:
                    description = context.getResources().getString(R.string.external_camera);
                    break;
                default:
                    Log.e(TAG, "unknown camera type");
                    return null;
            }

            SizeF view_angle = CameraControllerManager2.computeViewAngles(characteristics);
            if( view_angle.getWidth() > 90.5f ) {
                // count as ultra-wide
                description += ", " + context.getResources().getString(R.string.ultrawide);
            }
        }
        catch(Throwable e) {
            // see note under isFrontFacing() why we catch anything, not just CameraAccessException
            if( MyDebug.LOG )
                Log.e(TAG, "exception trying to get camera characteristics");
            e.printStackTrace();
        }
        return description;
    }

    @Override
    public String getHardwareLevel(Context context, int cameraId) {
        try {
            String cameraIdS = mCameraManager.getCameraIdList()[cameraId];
            CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraIdS);
            int hardware_level = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
            switch (hardware_level) {
                case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY:
                    Log.d(TAG, "CameraId:" + cameraId + ",Hardware Level: LEGACY");
                    return "LEGACY";
                case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED:
                    Log.d(TAG, "CameraId:" + cameraId + ",Hardware Level: LIMITED");
                    return "LIMITED";
                case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL:
                    Log.d(TAG, "CameraId:" + cameraId + ",Hardware Level: FULL");
                    return "FULL";
                case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3:
                    Log.d(TAG, "CameraId:" + cameraId + ",Hardware Level: Level 3");
                    return "LEVEL_3";
                case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_EXTERNAL:
                    Log.d(TAG, "CameraId:" + cameraId + ",Hardware Level: Level EXTERNAL");
                    return "EXTERNAL";
                default:
                    Log.e(TAG, "Unknown Hardware Level: " + hardware_level);
                    return "ERROR";
            }
        } catch (Throwable e) {
            if( MyDebug.LOG )
                Log.e(TAG, "exception trying to getHardwareLevel.");
            e.printStackTrace();
        }
        return "ERROR";
    }

    @Override
    public boolean isLogicalMultiCamera(Context context, int cameraId) {
        try {
            String cameraIdS = mCameraManager.getCameraIdList()[cameraId];
            CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraIdS);
            Set<String> phySicalCameraIds = characteristics.getPhysicalCameraIds();
            int[] capabilities = characteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES);
            List<String> capabilitiesStringList = MyUtils.convertCapabilityToString(capabilities);
            if (MyDebug.LOG) {
                Log.d(TAG, "LogicalCamera: " + cameraIdS + ", Capabilities:" + capabilitiesStringList.toString());
            }
            List<Integer> capabilitiesList = new ArrayList<>();
            for(Integer capability : capabilities) {
                capabilitiesList.add(capability);
            }
            int logicalMultiCameraId = CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_LOGICAL_MULTI_CAMERA;
            Log.d(TAG, "(LogicalCamera: " + cameraIdS +
                    ",PhysicalCameraIds:" + phySicalCameraIds.toString() +
                    ",has LOGICAL_MULTI_CAMERA capability:" +
                    capabilitiesList.contains(logicalMultiCameraId) + ")");
            return capabilitiesList.contains(logicalMultiCameraId) && (capabilities.length > 0);
        } catch (Throwable e) {
            if(MyDebug.LOG)
                Log.e(TAG, "exception trying to isLogicalMultiCamera.");
            e.printStackTrace();
        }
        return false;
    }

    /** Helper class to compute view angles from the CameraCharacteristics.
     * @return The width and height of the returned size represent the x and y view angles in
     *         degrees.
     */
    static SizeF computeViewAngles(CameraCharacteristics characteristics) {
        // Note this is an approximation (see http://stackoverflow.com/questions/39965408/what-is-the-android-camera2-api-equivalent-of-camera-parameters-gethorizontalvie ).
        // This does not take into account the aspect ratio of the preview or camera, it's up to the caller to do this (e.g., see Preview.getViewAngleX(), getViewAngleY()).
        Rect active_size = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
        SizeF physical_size = characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);
        android.util.Size pixel_size = characteristics.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE);
        float [] focal_lengths = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
        if( active_size == null || physical_size == null || pixel_size == null || focal_lengths == null || focal_lengths.length == 0 ) {
            // in theory this should never happen according to the documentation, but I've had a report of physical_size (SENSOR_INFO_PHYSICAL_SIZE)
            // being null on an EXTERNAL Camera2 device, see https://sourceforge.net/p/opencamera/tickets/754/
            if( MyDebug.LOG ) {
                Log.e(TAG, "can't get camera view angles");
            }
            // fall back to a default
            return new SizeF(55.0f, 43.0f);
        }
        //camera_features.view_angle_x = (float)Math.toDegrees(2.0 * Math.atan2(physical_size.getWidth(), (2.0 * focal_lengths[0])));
        //camera_features.view_angle_y = (float)Math.toDegrees(2.0 * Math.atan2(physical_size.getHeight(), (2.0 * focal_lengths[0])));
        float frac_x = ((float)active_size.width())/(float)pixel_size.getWidth();
        float frac_y = ((float)active_size.height())/(float)pixel_size.getHeight();
        float view_angle_x = (float)Math.toDegrees(2.0 * Math.atan2(physical_size.getWidth() * frac_x, (2.0 * focal_lengths[0])));
        float view_angle_y = (float)Math.toDegrees(2.0 * Math.atan2(physical_size.getHeight() * frac_y, (2.0 * focal_lengths[0])));
        if( MyDebug.LOG ) {
            Log.d(TAG, "frac_x: " + frac_x);
            Log.d(TAG, "frac_y: " + frac_y);
            Log.d(TAG, "view_angle_x: " + view_angle_x);
            Log.d(TAG, "view_angle_y: " + view_angle_y);
        }
        return new SizeF(view_angle_x, view_angle_y);
    }

    /* Returns true if the device supports the required hardware level, or better.
     * See https://developer.android.com/reference/android/hardware/camera2/CameraCharacteristics#INFO_SUPPORTED_HARDWARE_LEVEL .
     * From Android N, higher levels than "FULL" are possible, that will have higher integer values.
     * Also see https://sourceforge.net/p/opencamera/tickets/141/ .
     */
    static boolean isHardwareLevelSupported(CameraCharacteristics c, int requiredLevel) {
        int deviceLevel = c.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
        if( MyDebug.LOG ) {
            switch (deviceLevel) {
                case CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY:
                    Log.d(TAG, "Camera has LEGACY Camera2 support");
                    break;
                case CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_EXTERNAL:
                    Log.d(TAG, "Camera has EXTERNAL Camera2 support");
                    break;
                case CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED:
                    Log.d(TAG, "Camera has LIMITED Camera2 support");
                    break;
                case CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_FULL:
                    Log.d(TAG, "Camera has FULL Camera2 support");
                    break;
                case CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_3:
                    Log.d(TAG, "Camera has Level 3 Camera2 support");
                    break;
                default:
                    Log.d(TAG, "Camera has unknown Camera2 support: " + deviceLevel);
                    break;
            }
        }

        // need to treat legacy and external as special cases; otherwise can then use numerical comparison

        if( deviceLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY ) {
            return requiredLevel == deviceLevel;
        }

        if( deviceLevel == CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_EXTERNAL ) {
            deviceLevel = CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED;
        }
        if( requiredLevel == CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_EXTERNAL ) {
            requiredLevel = CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED;
        }

        return requiredLevel <= deviceLevel;
    }

    /* Rather than allowing Camera2 API on all Android 5+ devices, we restrict it to certain cases.
     * This returns whether the specified camera has at least LIMITED support.
     */
    public boolean allowCamera2Support(int cameraId) {
        try {
            String cameraIdS = mCameraManager.getCameraIdList()[cameraId];
            CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraIdS);
            //return isHardwareLevelSupported(characteristics, CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY);
            return isHardwareLevelSupported(characteristics, CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED);
        }
        catch(Throwable e) {
            // in theory we should only get CameraAccessException, but Google Play shows we can get a variety of exceptions
            // from some devices, e.g., AssertionError, IllegalArgumentException, RuntimeException, so just catch everything!
            // We don't want users to experience a crash just because of buggy camera2 drivers - instead the user can switch
            // back to old camera API.
            if( MyDebug.LOG )
                Log.e(TAG, "exception trying to get camera characteristics");
            e.printStackTrace();
        }
        return false;
    }

    private void printAvailableSessionKeys(Context context, int cameraId) {
        try {
            String cameraIdS = mCameraManager.getCameraIdList()[cameraId];
            CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraIdS);
            List <CaptureRequest.Key<?>> availabeSessionKeys = characteristics.getAvailableSessionKeys();
            for (CaptureRequest.Key sessionKey : availabeSessionKeys) {
                Log.i(TAG, "cameraId;" + cameraIdS + ", sessionKey name:" + sessionKey.getName());
            }
        } catch (Exception e) {

        }
    }

    private void printAvailableControlModes(Context context, int cameraId) {
        try {
            String cameraIdS = mCameraManager.getCameraIdList()[cameraId];
            CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraIdS);
            int[] availableControlMOdes = characteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_MODES);

            for (int controlMode : availableControlMOdes) {
                String controlModeStr = "";
                switch (controlMode) {
                    case CaptureRequest.CONTROL_MODE_OFF:
                        controlModeStr = "OFF";
                        break;
                    case CaptureRequest.CONTROL_MODE_AUTO:
                        controlModeStr = "AUTO";
                        break;
                    case CaptureRequest.CONTROL_MODE_USE_SCENE_MODE:
                        controlModeStr = "USE_SCENE_MODE";
                        break;
                    case CaptureRequest.CONTROL_MODE_OFF_KEEP_STATE:
                        controlModeStr = "OFF_KEEP_STATE";
                        break;
                    default:
                        break;
                }
                Log.i(TAG, "cameraId;" + cameraIdS + ", support control mode:" + controlModeStr);
            }
        } catch (Exception e) {

        }
    }

    private void printAvailableAEModes(Context context, int cameraId) {
        try {
            String cameraIdS = mCameraManager.getCameraIdList()[cameraId];
            CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraIdS);
            int[] availableAeMOdes = characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES);

            for (int aeMode : availableAeMOdes) {
                String controlModeStr = "";
                switch (aeMode) {
                    case CaptureRequest.CONTROL_AE_MODE_OFF:
                        controlModeStr = "OFF";
                        break;
                    case CaptureRequest.CONTROL_AE_MODE_ON:
                        controlModeStr = "ON";
                        break;
                    case CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH:
                        controlModeStr = "ON_AUTO_FLASH";
                        break;
                    case CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH:
                        controlModeStr = "ON_ALWAYS_FLASH";
                        break;
                    case CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH_REDEYE:
                        controlModeStr = "ON_AUTO_FLASH_REDEYE";
                        break;
                    case CaptureRequest.CONTROL_AE_MODE_ON_EXTERNAL_FLASH:
                        controlModeStr = "ON_EXTERNAL_FLASH";
                        break;
                    default:
                        break;
                }
                Log.i(TAG, "cameraId;" + cameraIdS + ", support ae modes:" + controlModeStr);
            }
        } catch (Exception e) {

        }
    }

    private void printAvailableStaticKeys(Context context, int cameraId) {
        try {
            String cameraIdS = mCameraManager.getCameraIdList()[cameraId];
            CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraIdS);
            List <CameraCharacteristics.Key<?>> availabeStaticKeys = characteristics.getKeys();
            for (CameraCharacteristics.Key staticKey : availabeStaticKeys) {
                Log.i(TAG, "cameraId;" + cameraIdS + ", availabeStaticKeys name:" + staticKey.getName());
                if (staticKey.getName().equalsIgnoreCase(
                        CameraController2.mVendorTag_faceLandmark_availableIds.getName())) {
                    Byte[] faceLandmark_availableIds =
                            characteristics.get(CameraController2.mVendorTag_faceLandmark_availableIds);
                    StringBuilder availableIds = new StringBuilder();
                    for (int i = 0; i < faceLandmark_availableIds.length; i++) {
                        availableIds.append((int)faceLandmark_availableIds[i] + " ");
                    }
                    Log.i(TAG, "cameraIdS:" + cameraIdS +
                            " VendorTag_OPS " + CameraController2.mVendorTag_faceLandmark_availableIds.getName() +
                            ", values:" + availableIds.toString());
                }
            }
        } catch (Exception e) {

        }
    }

    private void printAvailableResultKeys(Context context, int cameraId) {
        try {
            String cameraIdS = mCameraManager.getCameraIdList()[cameraId];
            CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraIdS);
            List <CaptureResult.Key<?>> availabeResultKeys = characteristics.getAvailableCaptureResultKeys();
            for (CaptureResult.Key resultKey : availabeResultKeys) {
                Log.i(TAG, "cameraId:" + cameraIdS + ", availabeResultKeys name:" + resultKey.getName());
            }
        } catch (Exception e) {

        }
    }

    private void printAvailableRequestKeys(Context context, int cameraId) {
        try {
            String cameraIdS = mCameraManager.getCameraIdList()[cameraId];
            CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraIdS);
            List <CaptureRequest.Key<?>> availabeRequestKeys = characteristics.getAvailableCaptureRequestKeys();
            for (CaptureRequest.Key requestKey : availabeRequestKeys) {
                Log.i(TAG, "cameraId:" + cameraIdS + ", availabeRequestKeys name:" + requestKey.getName());
            }
            List <CaptureRequest.Key<?>> availabePhysicalRequestKeys =
                    characteristics.getAvailablePhysicalCameraRequestKeys();
            Log.i(TAG, "cameraId:" + cameraIdS + " Physical_Camera availabePhysicalRequestKeys " + availabePhysicalRequestKeys);
            for (CaptureRequest.Key requestKey : availabePhysicalRequestKeys) {
                Log.i(TAG, "cameraId:" + cameraIdS + ", Physical_Camera availabeRequestKeys name:" + requestKey.getName());
            }
        } catch (Exception e) {

        }
    }

    private void printStreamConfigurationMap(Context context, int cameraId) {
        Log.i(TAG, "StreamConfigurationMap++++++++++++++++++++++++++++++++++++++++++++++");
        try {
            String cameraIdS = mCameraManager.getCameraIdList()[cameraId];
            CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraIdS);
            StreamConfigurationMap streamConfigurationMap =
                    characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            Class[] surfaceClasses = new Class[6];
            surfaceClasses[0] = ImageReader.class;
            surfaceClasses[1] = SurfaceTexture.class;
            surfaceClasses[2] = SurfaceHolder.class;
            surfaceClasses[3] = MediaRecorder.class;
            surfaceClasses[4] = MediaCodec.class;
            surfaceClasses[5] = Allocation.class;

            // 打印输出流配置信息: format, outputsize, minframeduration, stallframeduration
            // 打印高分辨率流配置信息
            for (int u = 0; u < surfaceClasses.length; u++) {
                Size[] surfaceSupportedOutputSizes = streamConfigurationMap.getOutputSizes(surfaceClasses[u]);
                if (surfaceSupportedOutputSizes != null) {
                    for (int j = 0; j < surfaceSupportedOutputSizes.length; j++) {
                        long minFrameDuration =
                                streamConfigurationMap.getOutputMinFrameDuration(surfaceClasses[u], surfaceSupportedOutputSizes[j]);
                        long stallFrameDuration =
                                streamConfigurationMap.getOutputStallDuration(surfaceClasses[u], surfaceSupportedOutputSizes[j]);
                        if (MyDebug.LOG) {
                            Log.i(TAG, "StreamConfigurationMap-Output-Surface CameraID:" + cameraIdS +
                                    ", Surface Class:" + surfaceClasses[u] +
                                    ", Size:" + surfaceSupportedOutputSizes[j] +
                                    ", MinFrameDuration:" + minFrameDuration / 1000000 + "ms" +
                                    ", StallFrameDuration:" + stallFrameDuration / 1000000 + "ms");
                        }

                    }
                }
            }

            int[] outputFormats = streamConfigurationMap.getOutputFormats();
            for (int i = 0; i < outputFormats.length; i++) {
                Size[] outputSizeByFormat = streamConfigurationMap.getOutputSizes(outputFormats[i]);
                for (int j = 0; j < outputSizeByFormat.length; j++) {
                    long minFrameDuration =
                            streamConfigurationMap.getOutputMinFrameDuration(outputFormats[i], outputSizeByFormat[j]);
                    long stallFrameDuration =
                            streamConfigurationMap.getOutputStallDuration(outputFormats[i], outputSizeByFormat[j]);
                    if (MyDebug.LOG) {
                        Log.i(TAG, "StreamConfigurationMap-Output CameraID:" + cameraIdS + ", format:" + MyUtils.formatToString(outputFormats[i]) +
                                ", OutputSize:" + outputSizeByFormat[j] +
                                ", MinFrameDuration:" + minFrameDuration / 1000000 + "ms" +
                                ", StallFrameDuration:" + stallFrameDuration / 1000000 + "ms");
                    }

                }
                Size[] highResolutionOutputSizeByFormats =
                        streamConfigurationMap.getHighResolutionOutputSizes(outputFormats[i]);
                if (highResolutionOutputSizeByFormats != null) {
                    if (MyDebug.LOG) {
                        Log.i(TAG, "StreamConfigurationMap-HighResolutionOutput CameraID:" + cameraIdS + ", format:" + MyUtils.formatToString(outputFormats[i]) +
                                ", outputFormats length:" + highResolutionOutputSizeByFormats.length);
                    }
                    for (int j = 0; j < highResolutionOutputSizeByFormats.length; j++) {
                        long minFrameDuration =
                                streamConfigurationMap.getOutputMinFrameDuration(outputFormats[i], highResolutionOutputSizeByFormats[j]);
                        long stallFrameDuration =
                                streamConfigurationMap.getOutputStallDuration(outputFormats[i], highResolutionOutputSizeByFormats[j]);
                        if (MyDebug.LOG) {
                            Log.i(TAG, "StreamConfigurationMap-HighResolutionOutput CameraID:" + cameraIdS + ", format:" + MyUtils.formatToString(outputFormats[i]) +
                                    ", OutputSize:" + highResolutionOutputSizeByFormats[j] +
                                    ", MinFrameDuration:" + minFrameDuration / 1000000 + "ms" +
                                    ", StallFrameDuration:" + stallFrameDuration / 1000000 + "ms");
                        }
                    }
                }
            }

            // 打印高帧率流配置信息
            Size[] highSpeedVideoSizes = streamConfigurationMap.getHighSpeedVideoSizes();
            for (int i = 0; i < highSpeedVideoSizes.length; i++) {
                Range<Integer>[] fpsRangeForSize =
                        streamConfigurationMap.getHighSpeedVideoFpsRangesFor(highSpeedVideoSizes[i]);
                StringBuilder fpsRangeForSizeInfo = new StringBuilder();
                fpsRangeForSizeInfo.append("Supported fps ranges:");
                for(int j = 0; j < fpsRangeForSize.length; j++) {
                    fpsRangeForSizeInfo.append(fpsRangeForSize[j]).append(",");
                }
                if (MyDebug.LOG) {
                    Log.i(TAG, "StreamConfigurationMap-HighSpeed CameraID:" + cameraIdS +
                            ", (size, fpsranges)= " + "(" + highSpeedVideoSizes[i] + "," + fpsRangeForSizeInfo.toString());
                }
            }

            // 打印输入流配置信息
            int[] inputFormats = streamConfigurationMap.getInputFormats();
            if (MyDebug.LOG) {
                Log.i(TAG, "StreamConfigurationMap-Input CameraID:" + cameraIdS + ", format length:" + inputFormats.length);
            }
            for (int i = 0; i < inputFormats.length; i++) {
                StringBuilder inputPrintinfo = new StringBuilder();
                Size[] inputSizesForFormat = streamConfigurationMap.getInputSizes(inputFormats[i]);
                inputPrintinfo.append("supported Input sizes:");
                for (int j = 0; j < inputSizesForFormat.length; j++) {
                    inputPrintinfo.append(inputSizesForFormat[j].toString()).append(",");
                }
                int[] outputFormatsForInput = streamConfigurationMap.getValidOutputFormatsForInput(inputFormats[i]);
                inputPrintinfo.append("supported Output formats:");
                for (int k = 0; k < outputFormatsForInput.length; k++) {
                    inputPrintinfo.append(MyUtils.formatToString(outputFormatsForInput[k])).append(",");
                }
                if (MyDebug.LOG) {
                    Log.i(TAG, "StreamConfigurationMap-Input CameraID:" + cameraIdS +
                            ", input format:" + inputFormats[i] +
                            ", " + inputPrintinfo.toString());
                }
            }
            Log.i(TAG, "StreamConfigurationMap----------------------------------------------");
        } catch (Throwable e) {
            if( MyDebug.LOG )
                Log.e(TAG, "StreamConfigurationMap exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
