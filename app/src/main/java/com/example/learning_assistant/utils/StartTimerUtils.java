package com.example.learning_assistant.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

import com.example.learning_assistant.CountDownTimerService;

import static com.example.learning_assistant.utils.Constants.RINGING_VOLUME_LEVEL_KEY;
import static com.example.learning_assistant.utils.Constants.START_ACTION_BROADCAST;
import static com.example.learning_assistant.utils.Constants.TIME_INTERVAL;
import static com.example.learning_assistant.utils.VolumeSeekBarUtils.convertToFloat;
import static com.example.learning_assistant.utils.VolumeSeekBarUtils.maxVolume;
import static com.example.learning_assistant.utils.VolumeSeekBarUtils.floatRingingVolumeLevel;
import static com.example.learning_assistant.utils.Utils.prepareSoundPool;


public class StartTimerUtils {

    /**
     * Starts service and CountDownTimer according to duration value.
     * Duration can be initial value of either POMODORO, SHORT_BREAK or LONG_BREAK.
     *
     * @param duration is Time Period for which timer should tick
     */
    public static void startTimer(long duration, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        prepareSoundPool(context); //Prepare SoundPool
        final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        maxVolume = (audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)); // -1 just because otherwise converttofloat returns infinity
        floatRingingVolumeLevel = convertToFloat(preferences
                .getInt(RINGING_VOLUME_LEVEL_KEY, maxVolume - 1), maxVolume); //also set ringing volume
        Intent serviceIntent = new Intent(context, CountDownTimerService.class);
        serviceIntent.putExtra("time_period", duration);
        serviceIntent.putExtra("time_interval", TIME_INTERVAL);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context.startForegroundService(serviceIntent);
        else
            context.startService(serviceIntent);

        sendBroadcast(context);
    }

    /**
     * Update MainActivity Elements through  broadcast
     */
    private static void sendBroadcast(Context context) {
        LocalBroadcastManager completedBroadcastManager = LocalBroadcastManager.getInstance(context);
        completedBroadcastManager.sendBroadcast(
                new Intent(START_ACTION_BROADCAST));
    }
}
