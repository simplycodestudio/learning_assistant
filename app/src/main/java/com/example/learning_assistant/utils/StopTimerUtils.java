package com.example.learning_assistant.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

import com.example.learning_assistant.CountDownTimerService;

import java.util.Date;



import static com.example.learning_assistant.MainActivity.currentlyRunningServiceType;
import static com.example.learning_assistant.utils.Constants.COMPLETE_ACTION_BROADCAST;
import static com.example.learning_assistant.utils.Constants.CZAS_NA_NAUKE;
import static com.example.learning_assistant.utils.Utils.ringID;
import static com.example.learning_assistant.utils.Utils.soundPool;
import static com.example.learning_assistant.utils.Utils.updateCurrentlyRunningServiceType;
import static com.example.learning_assistant.utils.VolumeSeekBarUtils.floatRingingVolumeLevel;



public class StopTimerUtils {

    /**
     * Tasks executed when the timer Completes Ticking or is prematurely completed
     */
    public static void sessionComplete(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        if (currentlyRunningServiceType == CZAS_NA_NAUKE) {

            // Updates newWorkSessionCount in SharedPreferences and displays it on TextView.
            Utils.updateWorkSessionCount(preferences, context);

            // Retrieves type of break user should take, either SHORT_BREAK or
            // LONG_BREAK, and updates value of currentlyRunningService in SharedPreferences.
            currentlyRunningServiceType = Utils.getTypeOfBreak(preferences, context);
            updateCurrentlyRunningServiceType(preferences, context,
                    currentlyRunningServiceType);

            stopTimer(context);
            soundPool.play(ringID, floatRingingVolumeLevel, floatRingingVolumeLevel, 2, 1,
                    1f);
            sendBroadcast(context);
        }
        Date date = new Date(System.currentTimeMillis()); //or simply new Date();
        long millis = date.getTime();
        preferences.edit().putInt("pause", (int) millis / 1000).apply();
    }

    /**
     * Tasks executed when the session is cancelled prematurely
     */
    public static void sessionCancel(Context context, SharedPreferences preferences) {
        updateCurrentlyRunningServiceType(preferences, context, CZAS_NA_NAUKE);
        stopTimer(context);
        sendBroadcast(context);
    }

    /**
     * Update MainActivity Elements through  broadcast
     */
    private static void sendBroadcast(Context context) {
        LocalBroadcastManager completedBroadcastManager = LocalBroadcastManager.getInstance(context);
        completedBroadcastManager.sendBroadcast(
                new Intent(COMPLETE_ACTION_BROADCAST));
    }

    /**
     * Stops service and resets CountDownTimer to initial value.
     * Duration can be initial value of either POMODORO, SHORT_BREAK or LONG_BREAK.
     */
    private static void stopTimer(Context context) {
        Intent serviceIntent = new Intent(context, CountDownTimerService.class);
        context.stopService(serviceIntent);
    }
}
