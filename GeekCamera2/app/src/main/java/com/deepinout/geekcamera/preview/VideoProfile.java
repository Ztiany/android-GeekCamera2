package com.deepinout.geekcamera.preview;

import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import androidx.annotation.NonNull;

import com.deepinout.geekcamera.MyDebug;

/** This is essentially similar to CamcorderProfile in that it encapsulates a set of video settings
     *  to be passed to MediaRecorder, but allows us to store additional fields.
     */
public class VideoProfile {
    private static final String TAG = "GC2_VideoProfile";

    public boolean record_audio;
    public boolean no_audio_permission; // set to true if record_audio==false, but where the user had requested audio and we don't have microphone permission
    public int audioSource;
    public int audioCodec;
    public int audioChannels;
    @SuppressWarnings("WeakerAccess")
    public int audioBitRate;
    @SuppressWarnings("WeakerAccess")
    public int audioSampleRate;
    public int fileFormat;
    public String fileExtension = "mp4";
    public int videoSource;
    public int videoCodec;
    public int videoFrameRate;
    public int videoCaptureRate;
    public int videoBitRate;
    public int videoFrameHeight;
    public int videoFrameWidth;

    private static final int BIT_RATE_720P = 8000000;
    private static final int BIT_RATE_MIN = 64000;
    private static final int BIT_RATE_MAX = BIT_RATE_720P;

    /** Returns a dummy video profile, used if video isn't supported.
     */
    VideoProfile() {
    }

    private int getVideoBitRate(Size sz) {
        int rate = BIT_RATE_720P;
        float scaleFactor = sz.getHeight() * sz.getWidth() / (float)(1280 * 720);
        rate = (int)(rate * scaleFactor);

        // Clamp to the MIN, MAX range.
        return Math.max(BIT_RATE_MIN, Math.min(BIT_RATE_MAX, rate));
    }

    VideoProfile(CamcorderProfile camcorderProfile) {
        this.record_audio = true;
        this.no_audio_permission = false;
        this.audioSource = MediaRecorder.AudioSource.CAMCORDER;
        this.audioCodec = camcorderProfile.audioCodec;
        this.audioChannels = camcorderProfile.audioChannels;
        this.audioBitRate = camcorderProfile.audioBitRate;
        this.audioSampleRate = camcorderProfile.audioSampleRate;
        this.fileFormat = camcorderProfile.fileFormat;
        this.videoSource = MediaRecorder.VideoSource.CAMERA;
        this.videoCodec = camcorderProfile.videoCodec;
        this.videoFrameRate = camcorderProfile.videoFrameRate;
        this.videoCaptureRate = camcorderProfile.videoFrameRate;
        this.videoBitRate = camcorderProfile.videoBitRate;
        this.videoFrameHeight = camcorderProfile.videoFrameHeight;
        this.videoFrameWidth = camcorderProfile.videoFrameWidth;
    }

    @NonNull
    public String toString() {
        return ("\nAudioSource:        " + this.audioSource +
                "\nVideoSource:        " + this.videoSource +
                "\nFileFormat:         " + this.fileFormat +
                "\nFileExtension:         " + this.fileExtension +
                "\nAudioCodec:         " + this.audioCodec +
                "\nAudioChannels:      " + this.audioChannels +
                "\nAudioBitrate:       " + this.audioBitRate +
                "\nAudioSampleRate:    " + this.audioSampleRate +
                "\nVideoCodec:         " + this.videoCodec +
                "\nVideoFrameRate:     " + this.videoFrameRate +
                "\nVideoCaptureRate:   " + this.videoCaptureRate +
                "\nVideoBitRate:       " + this.videoBitRate +
                "\nVideoWidth:         " + this.videoFrameWidth +
                "\nVideoHeight:        " + this.videoFrameHeight
        );
    }

    public void copyToMediaRecorderSlowMotion(MediaRecorder mediaRecorder) {
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setVideoEncodingBitRate(getVideoBitRate(new Size(this.videoFrameWidth, this.videoFrameHeight)));
        mediaRecorder.setVideoFrameRate(30);
        mediaRecorder.setCaptureRate(this.videoCaptureRate);
        mediaRecorder.setVideoSize(this.videoFrameWidth, this.videoFrameHeight);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
    }

    /**
     * Copies the fields of this profile to a MediaRecorder instance.
     */
    public void copyToMediaRecorder(MediaRecorder media_recorder, boolean slow_motion, Surface persistSurface) {
        if( MyDebug.LOG )
            Log.d(TAG, "copyToMediaRecorder: " + media_recorder + toString());
        if( record_audio && !slow_motion) {
            if( MyDebug.LOG )
                Log.d(TAG, "record audio");
            media_recorder.setAudioSource(this.audioSource);
        }
        media_recorder.setVideoSource(this.videoSource);
        // n.b., order may be important - output format should be first, at least
        // also match order of MediaRecorder.setProfile() just to be safe, see https://stackoverflow.com/questions/5524672/is-it-possible-to-use-camcorderprofile-without-audio-source
        media_recorder.setOutputFormat(this.fileFormat);
        if (slow_motion) {
            media_recorder.setVideoFrameRate(30);
        } else {
            media_recorder.setVideoFrameRate(this.videoFrameRate);
        }
        media_recorder.setCaptureRate(this.videoCaptureRate);
        media_recorder.setVideoSize(this.videoFrameWidth, this.videoFrameHeight);
        media_recorder.setVideoEncodingBitRate(this.videoBitRate);
        media_recorder.setVideoEncoder(this.videoCodec);
        if( record_audio && !slow_motion) {
            media_recorder.setAudioEncodingBitRate(this.audioBitRate);
            media_recorder.setAudioChannels(this.audioChannels);
            media_recorder.setAudioSamplingRate(this.audioSampleRate);
            media_recorder.setAudioEncoder(this.audioCodec);
        }
        if( MyDebug.LOG )
            Log.d(TAG, "done: " + media_recorder);

        if (persistSurface != null) {
            media_recorder.setInputSurface(persistSurface);
        }
    }
}
