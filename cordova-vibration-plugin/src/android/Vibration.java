package com.sachadvr.vibration;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.os.Vibrator;
import android.os.Build;

public class Vibration extends CordovaPlugin {

    private Vibrator vibrator;

    @Override
    protected void pluginInitialize() {
        vibrator = (Vibrator) cordova.getActivity().getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        if (vibrator == null || !vibrator.hasVibrator()) {
            callbackContext.error("Device does not support vibration");
            return false;
        }

        if (action.equals("vibrate")) {
            long duration = args.getLong(0);
            this.vibrate(duration);
            callbackContext.success();
            return true;
        } else if (action.equals("vibrateWithPattern")) {
            JSONArray pattern = args.getJSONArray(0);
            long[] patternArray = new long[pattern.length()];

            for (int i = 0; i < pattern.length(); i++) {
                patternArray[i] = pattern.getLong(i);
            }

            this.vibrateWithPattern(patternArray);
            callbackContext.success();
            return true;
        } else if (action.equals("cancelVibration")) {
            this.cancelVibration();
            callbackContext.success();
            return true;
        }

        return false;
    }

    private void vibrate(long duration) {
        if (duration > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(android.os.VibrationEffect.createOneShot(duration, android.os.VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(duration);
            }
        }
    }

    private void vibrateWithPattern(long[] pattern) {
        if (pattern.length > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(android.os.VibrationEffect.createWaveform(pattern, -1));
            } else {
                vibrator.vibrate(pattern, -1);
            }
        }
    }

    private void cancelVibration() {
        vibrator.cancel();
    }
}
