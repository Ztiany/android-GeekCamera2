package com.deepinout.geekcamera.preview;

import android.media.CamcorderProfile;
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
    private List<String> video_quality;
    private int current_video_quality = -1; // this is an index into the video_quality array, or -1 if not found (though this shouldn't happen?)
    private List<CameraController.Size> mCameraSupportedVideoSizesNormal;
    private List<CameraController.Size> mCameraSupportedVideoSizesHighSpeed; // may be null if high speed not supported

    void resetCurrentQuality() {
        video_quality = null;
        current_video_quality = -1;
    }

    /** Initialises the class with the available video profiles and resolutions. The user should first
     *  set the video sizes via setVideoSizes().
     * @param profiles   A list of qualities (see CamcorderProfile.QUALITY_*). Should be supplied in
     *                   order from highest to lowest quality.
     * @param profile_dimensions A corresponding list of the width/height for that quality (as given by
     *                   videoFrameWidth, videoFrameHeight in the profile returned by CamcorderProfile.get()).
     */
    public void initialiseVideoQualityFromProfiles(List<Integer> profiles, List<Dimension2D> profile_dimensions) {
        if( MyDebug.LOG )
            Log.d(TAG, "initialiseVideoQualityFromProfiles()");
        video_quality = new ArrayList<>();
        boolean[] done_video_size = null;
        if( mCameraSupportedVideoSizesNormal != null ) {
            done_video_size = new boolean[mCameraSupportedVideoSizesNormal.size()];
            for(int i = 0; i< mCameraSupportedVideoSizesNormal.size(); i++)
                done_video_size[i] = false;
        }
        if( profiles.size() != profile_dimensions.size() ) {
            Log.e(TAG, "profiles and dimensions have unequal sizes");
            throw new RuntimeException(); // this is a programming error
        }
        for(int i = 0; i < profiles.size(); i++) {
            addVideoResolutions(done_video_size, profiles.get(i), profile_dimensions.get(i));
        }
        if( MyDebug.LOG ) {
            for(int i=0;i<video_quality.size();i++) {
                Log.d(TAG, "supported video quality: " + video_quality.get(i));
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
                Log.d(TAG, "    supported video size: " + size.width + ", " + size.height);
            }
        }
    }

    private void addVideoResolutions(boolean[] done_video_size,
                                     int base_profile,
                                     Dimension2D dimension2D) {
        if( mCameraSupportedVideoSizesNormal == null ) {
            return;
        }
        if( MyDebug.LOG )
            Log.d(TAG, "profile " + base_profile + " is resolution " +
                    dimension2D.width + " x " + dimension2D.height);
        for(int i = 0; i < mCameraSupportedVideoSizesNormal.size(); i++) {
            if( done_video_size[i] )
                continue;
            CameraController.Size camera_video_size = mCameraSupportedVideoSizesNormal.get(i);
            if(camera_video_size.width == dimension2D.width &&
               camera_video_size.height == dimension2D.height ) {
                String str = "" + base_profile;
                video_quality.add(str);
                done_video_size[i] = true;
                if( MyDebug.LOG )
                    Log.d(TAG, "added: " + i + ":"+ str + " " +
                            camera_video_size.width + "x" + camera_video_size.height +
                            ",fps:" + dimension2D.videoFrameRate);
            }
            else if(camera_video_size.width * camera_video_size.height >=
                    dimension2D.width * dimension2D.height &&
                    camera_video_size.fps_ranges.size() > 0 &&
                    dimension2D.videoFrameRate == camera_video_size.fps_ranges.get(0)[1]) {
                String str = "" + base_profile + "_r" + camera_video_size.width + "x" + camera_video_size.height;
                video_quality.add(str);
                done_video_size[i] = true;
                if( MyDebug.LOG )
                    Log.d(TAG, "added: " + i + ":" + str + ",fps:" + dimension2D.videoFrameRate);
            }
        }
    }

    public List<String> getSupportedVideoQuality() {
        if( MyDebug.LOG )
            Log.d(TAG, "getSupportedVideoQuality");
        return this.video_quality;
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
        return video_quality.get(current_video_quality);
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
            Log.d(TAG, "findVideoSizeForFrameRate");
            Log.d(TAG, "width: " + width);
            Log.d(TAG, "height: " + height);
            Log.d(TAG, "fps: " + fps);
        }
        CameraController.Size requested_size = new CameraController.Size(width, height);
        CameraController.Size best_video_size = CameraController.CameraFeatures.findSize(this.getSupportedVideoSizes(), requested_size, fps, false);
        if( best_video_size == null && this.getSupportedVideoSizesHighSpeed() != null ) {
            if( MyDebug.LOG )
                Log.d(TAG, "need to check high speed sizes");
            // check high speed
            best_video_size = CameraController.CameraFeatures.findSize(this.getSupportedVideoSizesHighSpeed(), requested_size, fps, false);
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
