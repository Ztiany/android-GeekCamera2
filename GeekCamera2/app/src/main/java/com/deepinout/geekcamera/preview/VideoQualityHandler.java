package com.deepinout.geekcamera.preview;

import android.util.Log;

import com.deepinout.geekcamera.cameracontroller.CameraController;
import com.deepinout.geekcamera.MyDebug;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** Handles video quality options.
 *  Note that this class should avoid calls to the Android API, so we can perform local unit testing
 *  on it.
 */
public class VideoQualityHandler {
    private static final String TAG = "VideoQualityHandler";

    public static class Dimension2D {
        final int width;
        final int height;
        int videoFrameRate;

        public Dimension2D(int width, int height) {
            this.width = width;
            this.height = height;
            videoFrameRate = -1;
        }

        public Dimension2D(int width, int height, int frameRate) {
            this.width = width;
            this.height = height;
            videoFrameRate = frameRate;
        }
    }

    // video_quality can either be:
    // - an int, in which case it refers to a CamcorderProfile
    // - of the form [CamcorderProfile]_r[width]x[height] - we use the CamcorderProfile as a base, and override the video resolution - this is needed to support resolutions which don't have corresponding camcorder profiles
    private List<String> mVideoQualities;
    private int current_video_quality = -1; // this is an index into the video_quality array, or -1 if not found (though this shouldn't happen?)
    private List<CameraController.Size> mCameraSupportedVideoSizesNormal;
    private List<CameraController.Size> mCameraSupportedVideoSizesHighSpeed; // may be null if high speed not supported

    void resetCurrentQuality() {
        mVideoQualities = new ArrayList<>();
        current_video_quality = -1;
    }

    /** Initialises the class with the available video profiles and resolutions. The user should first
     *  set the video sizes via setVideoSizes().
     * @param profiles   A list of qualities (see CamcorderProfile.QUALITY_*). Should be supplied in
     *                   order from highest to lowest quality.
     * @param profile_dimensions A corresponding list of the width/height for that quality (as given by
     *                   videoFrameWidth, videoFrameHeight in the profile returned by CamcorderProfile.get()).
     */
    public void initialiseVideoQualityFromProfiles(List<Integer> profiles,
                                                   List<Dimension2D> profile_dimensions,
                                                   boolean is_high_speed,
                                                   boolean isApi2) {
        if( MyDebug.LOG )
            Log.d(TAG, "initialiseVideoQualityFromProfiles is_high_speed:" + is_high_speed);
        List<CameraController.Size> supported_video_sizes = mCameraSupportedVideoSizesNormal;
        if (is_high_speed) {
            supported_video_sizes = mCameraSupportedVideoSizesHighSpeed;
        }
        boolean[] done_video_size = null;
        if( supported_video_sizes != null ) {
            done_video_size = new boolean[supported_video_sizes.size()];
            for(int i = 0; i< supported_video_sizes.size(); i++)
                done_video_size[i] = false;
        }
        if( profiles.size() != profile_dimensions.size() ) {
            Log.e(TAG, "profiles and dimensions have unequal sizes");
            throw new RuntimeException(); // this is a programming error
        }
        for(int i = 0; i < profiles.size(); i++) {
            addVideoResolutions(done_video_size,
                    profiles.get(i),
                    profile_dimensions.get(i),
                    is_high_speed,
                    isApi2);
        }
        if( MyDebug.LOG ) {
            for(int i = 0; i< mVideoQualities.size(); i++) {
                Log.d(TAG, "supported video quality: " + mVideoQualities.get(i));
            }
        }
    }

    // Android docs and FindBugs recommend that Comparators also be Serializable
    private static class SortVideoSizesComparator implements Comparator<CameraController.Size>, Serializable {
        private static final long serialVersionUID = 5802214721033718212L;

        @Override
        public int compare(final CameraController.Size a, final CameraController.Size b) {
            return b.width * b.height - a.width * a.height;
        }
    }

    public void sortVideoSizes() {
        if( MyDebug.LOG )
            Log.d(TAG, "sortVideoSizes()");
        Collections.sort(this.mCameraSupportedVideoSizesNormal, new SortVideoSizesComparator());
        if( MyDebug.LOG ) {
            for(CameraController.Size size : mCameraSupportedVideoSizesNormal) {
                Log.d(TAG, "    supported video size: " + size.width + ", " + size.height + ", fps:" +
                        ((size.fps_ranges.size() > 0? size.fps_ranges.get(0)[1] + "": "")));
            }
        }
    }

    private void addVideoResolutions(boolean[] done_video_size,
                                     int base_profile,
                                     Dimension2D dimension2D,
                                     boolean is_high_speed,
                                     boolean isApi2) {
        List<CameraController.Size> supported_video_sizes = mCameraSupportedVideoSizesNormal;
        if (is_high_speed) {
            supported_video_sizes = mCameraSupportedVideoSizesHighSpeed;
        }
        if( supported_video_sizes == null ) {
            return;
        }
        if( MyDebug.LOG )
            Log.d(TAG, "addVideoResolutions++++++ profile " + base_profile + " is resolution " +
                    dimension2D.width + " x " + dimension2D.height);
        for(int i = 0; i < supported_video_sizes.size(); i++) {
            if( done_video_size[i] )
                continue;
            CameraController.Size camera_video_size = supported_video_sizes.get(i);
            String camera_fps_range = "null";
            String str = "" + base_profile;
            int camera_fps = 0;
            if (camera_video_size.fps_ranges.size() > 0) {
                camera_fps = camera_video_size.fps_ranges.get(0)[1];
                camera_fps_range = "" + camera_video_size.fps_ranges.get(0)[1];
//                camera_video_size.fps_ranges.get(0)[1] = dimension2D.videoFrameRate;
            }
            if (!isApi2) {
                if(camera_video_size.width == dimension2D.width &&
                        camera_video_size.height == dimension2D.height) {
                    mVideoQualities.add(str);
                    camera_video_size.supported_by_video_quality = true;
                    done_video_size[i] = true;
                    if( MyDebug.LOG ) {
                        Log.d(TAG, "addVideoResolutions added: " + i + ":"+ str + " " +
                                camera_video_size.width + "x" + camera_video_size.height +
                                ",profile fps:" + dimension2D.videoFrameRate +
                                ",camera fps ranges:" + camera_fps_range);
                    }
                }
            } else {
                if(camera_video_size.width == dimension2D.width &&
                        camera_video_size.height == dimension2D.height &&
                        (dimension2D.videoFrameRate == camera_fps)) {
                    mVideoQualities.add(str);
                    camera_video_size.supported_by_video_quality = true;
                    done_video_size[i] = true;
                    if( MyDebug.LOG ) {
                        Log.d(TAG, "addVideoResolutions added: " + i + ":"+ str + " " +
                                camera_video_size.width + "x" + camera_video_size.height +
                                ",profile fps:" + dimension2D.videoFrameRate +
                                ",camera fps ranges:" + camera_fps_range);
                    }
                }
                else {
                    if( MyDebug.LOG ) {
                        Log.e(TAG, "addVideoResolutions not added: " + i + ":"+ str + " " +
                                camera_video_size.width + "x" + camera_video_size.height +
                                ",profile fps:" + dimension2D.videoFrameRate +
                                ",profile width:" + dimension2D.width +
                                ",profile height:" + dimension2D.height +
                                ",camera fps ranges:" + camera_fps);
                    }
                }
//            else if(camera_video_size.width * camera_video_size.height >=
//                    dimension2D.width * dimension2D.height &&
//                    camera_video_size.fps_ranges.size() > 0 &&
//                    dimension2D.videoFrameRate == camera_video_size.fps_ranges.get(0)[1]) {
//                String str = "" + base_profile + "_r" + camera_video_size.width + "x" + camera_video_size.height;
//                video_quality.add(str);
//                done_video_size[i] = true;
//                if( MyDebug.LOG )
//                    Log.d(TAG, "added: " + i + ":" + str + ",fps:" + dimension2D.videoFrameRate);
//            }
            }
        }
        if( MyDebug.LOG )
            Log.d(TAG, "addVideoResolutions-----");
    }

    public List<String> getSupportedVideoQuality() {
        if( MyDebug.LOG )
            Log.d(TAG, "getSupportedVideoQuality");
        return this.mVideoQualities;
    }

    int getCurrentVideoQualityIndex() {
        if( MyDebug.LOG )
            Log.d(TAG, "getCurrentVideoQualityIndex");
        return this.current_video_quality;
    }

    void setCurrentVideoQualityIndex(int current_video_quality) {
        if( MyDebug.LOG )
            Log.d(TAG, "setCurrentVideoQualityIndex: " + current_video_quality);
        this.current_video_quality = current_video_quality;
    }

    public String getCurrentVideoQuality() {
        if( current_video_quality == -1 )
            return null;
        return mVideoQualities.get(current_video_quality);
    }

    public List<CameraController.Size> getSupportedVideoSizes() {
        if( MyDebug.LOG )
            Log.d(TAG, "getSupportedVideoSizes");
        return this.mCameraSupportedVideoSizesNormal;
    }

    public List<CameraController.Size> getSupportedVideoSizesHighSpeed() {
        if( MyDebug.LOG )
            Log.d(TAG, "getSupportedVideoSizesHighSpeed");
        return this.mCameraSupportedVideoSizesHighSpeed;
    }

    /** Whether the requested fps is supported, without relying on high-speed mode.
     *  Typically caller should first check videoSupportsFrameRateHighSpeed().
     */
    public boolean videoSupportsFrameRate(int fps) {
        return CameraController.CameraFeatures.supportsFrameRate(this.mCameraSupportedVideoSizesNormal, fps);
    }

    /** Whether the requested fps is supported as a high-speed mode.
     */
    public boolean videoSupportsFrameRateHighSpeed(int fps) {
        return CameraController.CameraFeatures.supportsFrameRate(this.mCameraSupportedVideoSizesHighSpeed, fps);
    }

    CameraController.Size findVideoSizeForFrameRate(int width, int height, int fps) {
        if( MyDebug.LOG ) {
            Log.d(TAG, "findVideoSizeForFrameRate++++++");
            Log.d(TAG, "findVideoSizeForFrameRate width: " + width);
            Log.d(TAG, "findVideoSizeForFrameRate height: " + height);
            Log.d(TAG, "findVideoSizeForFrameRate fps: " + fps);
        }
        CameraController.Size requested_size = new CameraController.Size(width, height);
        CameraController.Size best_video_size = CameraController.CameraFeatures.findSize(
                this.getSupportedVideoSizes(),
                requested_size,
                fps,
                false);
        if( best_video_size == null && this.getSupportedVideoSizesHighSpeed() != null ) {
            if( MyDebug.LOG )
                Log.d(TAG, "need to check high speed sizes fps:" + fps);
            // check high speed
            best_video_size = CameraController.CameraFeatures.findSize(
                    this.getSupportedVideoSizesHighSpeed(),
                    requested_size,
                    fps,
                    false);
        }
        return best_video_size;
    }

    private static CameraController.Size getMaxVideoSize(List<CameraController.Size> sizes) {
        int max_width = -1, max_height = -1;
        for(CameraController.Size size : sizes) {
            if( max_width == -1 || size.width*size.height > max_width*max_height ) {
                max_width = size.width;
                max_height = size.height;
            }
        }
        return new CameraController.Size(max_width, max_height);
    }

    /** Returns the maximum supported (non-high-speed) video size.
     */
    CameraController.Size getMaxSupportedVideoSize() {
        return getMaxVideoSize(mCameraSupportedVideoSizesNormal);
    }

    /** Returns the maximum supported high speed video size.
     */
    CameraController.Size getMaxSupportedVideoSizeHighSpeed() {
        return getMaxVideoSize(mCameraSupportedVideoSizesHighSpeed);
    }

    public void setVideoSizes(List<CameraController.Size> video_sizes) {
        this.mCameraSupportedVideoSizesNormal = video_sizes;
        this.sortVideoSizes();
    }

    public void setVideoSizesHighSpeed(List<CameraController.Size> video_sizes_high_speed) {
        this.mCameraSupportedVideoSizesHighSpeed = video_sizes_high_speed;
    }

}
