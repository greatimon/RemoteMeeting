package com.example.jyn.remotemeeting.WebRTC;

/**
 * Created by JYN on 2017-11-10.
 */

import android.widget.SeekBar;
import android.widget.TextView;

import com.example.jyn.remotemeeting.Fragment.Call_F;
import com.example.jyn.remotemeeting.R;

import org.webrtc.CameraEnumerationAndroid;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Control capture format based on a seekbar listener.
 * seekbar listener를 기반으로 캡처 형식을 제어합니다.
 */
public class CaptureQualityController implements SeekBar.OnSeekBarChangeListener {


    private final List<CameraEnumerationAndroid.CaptureFormat> formats =
            Arrays.asList(new CameraEnumerationAndroid.CaptureFormat(1280, 720, 0, 30000), new CameraEnumerationAndroid.CaptureFormat(960, 540, 0, 30000),
                    new CameraEnumerationAndroid.CaptureFormat(640, 480, 0, 30000), new CameraEnumerationAndroid.CaptureFormat(480, 360, 0, 30000),
                    new CameraEnumerationAndroid.CaptureFormat(320, 240, 0, 30000), new CameraEnumerationAndroid.CaptureFormat(256, 144, 0, 30000));

    // Prioritize framerate below this threshold and resolution above the threshold.
    // 이 임계 값 및 임계 값보다 높은 해상도보다 낮은 프레임 우선 순위를 지정하십시오.
    private static final int FRAMERATE_THRESHOLD = 15;
    private TextView captureFormatText;
    private Call_F.OnCallEvents callEvents;
    private int width = 0;
    private int height = 0;
    private int framerate = 0;
    private double targetBandwidth = 0;

    public CaptureQualityController(TextView captureFormatText, Call_F.OnCallEvents callEvents) {
        this.captureFormatText = captureFormatText;
        this.callEvents = callEvents;
    }

    private final Comparator<CameraEnumerationAndroid.CaptureFormat> compareFormats = new Comparator<CameraEnumerationAndroid.CaptureFormat>() {
        @Override
        public int compare(CameraEnumerationAndroid.CaptureFormat first, CameraEnumerationAndroid.CaptureFormat second) {
            int firstFps = calculateFramerate(targetBandwidth, first);
            int secondFps = calculateFramerate(targetBandwidth, second);

            if (firstFps >= FRAMERATE_THRESHOLD && secondFps >= FRAMERATE_THRESHOLD
                    || firstFps == secondFps) {
                // Compare resolution.
                return first.width * first.height - second.width * second.height;
            } else {
                // Compare fps.
                return firstFps - secondFps;
            }
        }
    };

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (progress == 0) {
            width = 0;
            height = 0;
            framerate = 0;
            captureFormatText.setText("Muted");
            return;
        }

        // Extract max bandwidth (in millipixels / second).
        long maxCaptureBandwidth = java.lang.Long.MIN_VALUE;
        for (CameraEnumerationAndroid.CaptureFormat format : formats) {
            maxCaptureBandwidth =
                    Math.max(maxCaptureBandwidth, (long) format.width * format.height * format.framerate.max);
        }

        // Fraction between 0 and 1.
        double bandwidthFraction = (double) progress / 100.0;
        // Make a log-scale transformation, still between 0 and 1.
        final double kExpConstant = 3.0;
        bandwidthFraction =
                (Math.exp(kExpConstant * bandwidthFraction) - 1) / (Math.exp(kExpConstant) - 1);
        targetBandwidth = bandwidthFraction * maxCaptureBandwidth;

        // Choose the best format given a target bandwidth.
        final CameraEnumerationAndroid.CaptureFormat bestFormat = Collections.max(formats, compareFormats);
        width = bestFormat.width;
        height = bestFormat.height;
        framerate = calculateFramerate(targetBandwidth, bestFormat);
        captureFormatText.setText(
                String.format(captureFormatText.getContext().getString(R.string.format_description), width,
                        height, framerate));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        callEvents.onCaptureFormatChange(width, height, framerate);
    }

    // Return the highest frame rate possible based on bandwidth and format.
    // 대역폭 및 형식에 따라 가능한 최고 프레임 속도를 반환합니다.
    private int calculateFramerate(double bandwidth, CameraEnumerationAndroid.CaptureFormat format) {
        return (int) Math.round(
                Math.min(format.framerate.max, (int) Math.round(bandwidth / (format.width * format.height))) / 1000.0);
    }
}
