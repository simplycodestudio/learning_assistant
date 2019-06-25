package com.example.learning_assistant.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;

import com.example.learning_assistant.R;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.example.learning_assistant.utils.Constants.CZAS_NA_NAUKE;
import static com.example.learning_assistant.utils.Constants.DLUGA_PRZERWA;
import static com.example.learning_assistant.utils.Constants.KROTKA_PRZERWA;


public class Utils {
    public static SoundPool soundPool;
    public static int ringID;

    /**
     * Updates value of WorkSessionCount by 1 and Writes the same to SharedPreferences.
     *
     * @param preferences Injected SharedPreferences instance from caller
     * @param context     Injected Context from caller
     * @return updated value of WorkSessionCount
     */
    public static int updateWorkSessionCount(SharedPreferences preferences, Context context) {
        // Retrieving value of workSessionCount (Current value of workSessionCount) from SharedPreference.
        int oldWorkSessionCount = preferences.getInt(context.getString(R.string.work_session_count_key), 0);
        int taskOnHandCount = preferences.getInt(context.getString(R.string.task_on_hand_count_key), 0);
        // Updating oldWorkSessionCount by 1.
        int newWorkSessionCount = ++oldWorkSessionCount;
        int newtaskonhandcount = ++taskOnHandCount;

        // Writing value of workSessionCount after a session is completed (New value of workSessionCount) in SharedPreference.
        preferences
                .edit()
                .putInt(context.getString(R.string.task_on_hand_count_key), newtaskonhandcount)
                .putInt(context.getString(R.string.work_session_count_key), newWorkSessionCount)
                .apply();
        return newWorkSessionCount;
    }

    /**
     * Returns type of break, user should take according to value of workSessionCount
     *
     * @param preferences Injected SharedPreferences instance from caller
     * @param context     Injected Context from caller
     * @return type of break, user should take
     */
    public static int getTypeOfBreak(SharedPreferences preferences, Context context) {
        int currentWorkSessionCount = preferences.getInt(context.getString(R.string.work_session_count_key), 0);
        int session = preferences.getInt(context.getString(R.string.start_long_break_after_key), 2);
        int longbreakintervalsession;
        switch (session) {
            case 0:
                longbreakintervalsession = 2;
                break;
            case 1:
                longbreakintervalsession = 3;
                break;
            case 2:
                longbreakintervalsession = 4;
                break;
            case 3:
                longbreakintervalsession = 5;
                break;
            case 4:
                longbreakintervalsession = 6;
                break;
            default:
                longbreakintervalsession = 4;
        }
        if (currentWorkSessionCount % longbreakintervalsession == 0)
            return DLUGA_PRZERWA;
        return KROTKA_PRZERWA;
    }

    /**
     * Writes new value for currentlyRunningService in SharedPreferences.
     * Value of currentlyRunningService determines values for textOn & textOff of ToggleButton in MainActivity.
     *
     * @param preferences                 Injected SharedPreferences instance from caller
     * @param context                     Injected Context from caller
     * @param currentlyRunningServiceType can be POMODORO, SHORT_BREAK or LONG_BREAK
     */
    public static void updateCurrentlyRunningServiceType(SharedPreferences preferences, Context context, int currentlyRunningServiceType) {
        preferences
                .edit()
                .putInt(context.getString(R.string.currently_running_service_type_key), currentlyRunningServiceType)
                .apply();
    }

    /**
     * @param preferences Injected SharedPreferences instance from caller
     * @param context     Injected Context from caller
     * @return current value of currentlyRunningService from SharedPreferences.
     */
    public static int retrieveCurrentlyRunningServiceType(SharedPreferences preferences, Context context) {
        return preferences.getInt(context.getString(R.string.currently_running_service_type_key), 0);
    }

    /**
     * Retrieving current value of Duration for POMODORO, SHORT_BREAK and LONG_BREAK from SharedPreferences.
     *
     * @param preferences                 Injected SharedPreferences instance from caller
     * @param context                     Injected Context from caller
     * @param currentlyRunningServiceType current value of currentlyRunningService (Can be POMODORO, SHORT_BREAK, or LONG_BREAK).
     * @return duration of CountDown for a service in milliSeconds according to value of currentlyRunningServiceType.
     */
    public static long getCurrentDurationPreferenceOf(SharedPreferences preferences, Context context, int currentlyRunningServiceType) {
        if (currentlyRunningServiceType == CZAS_NA_NAUKE) {
            // Current value of work duration stored in shared-preference
            int currentWorkDurationPreference = preferences.getInt(context.getString(R.string.work_duration_key), 1);
            // Switch case to return appropriate minute value of work duration according value stored in shared-preference.
            switch (currentWorkDurationPreference) {
                case 0:
                    return 10 * 60000;
                case 1:
                    return 15 * 60000;
                case 2:
                    return 20 * 60000;
                case 3:
                    return 25 * 60000;
                case 4:
                    return 30 * 60000;
                case 5:
                    return 35 * 60000;
                case 6:
                    return 40 * 60000;
                case 7:
                    return 45 * 60000;
                case 8:
                    return 50 * 60000;
                case 9:
                    return 55 * 60000;
            }
        } else if (currentlyRunningServiceType == KROTKA_PRZERWA) {
            // Current value of short-break duration stored in shared-preference
            int currentShortBreakDurationPreference = preferences.getInt(context.getString(R.string.short_break_duration_key), 1);

            // Switch case to return appropriate minute value of short-break duration according value stored in shared-preference.
            switch (currentShortBreakDurationPreference) {
                case 0:
                    return 3 * 60000;
                case 1:
                    return 5 * 60000;
                case 2:
                    return 10 * 60000;
                case 3:
                    return 15 * 60000;
            }
        } else if (currentlyRunningServiceType == DLUGA_PRZERWA) {
            // Current value of long-break duration stored in shared-preference
            int currentLongBreakDurationPreference = preferences.getInt(context.getString(R.string.long_break_duration_key), 1);

            // Switch case to return appropriate minute value of long-break duration according value stored in shared-preference.
            switch (currentLongBreakDurationPreference) {
                case 0:
                    return 10 * 60000;
                case 1:
                    return 15 * 60000;
                case 2:
                    return 20 * 60000;
                case 3:
                    return 25 * 60000;
            }
        }
        return 0;
    }

    /**
     * @param duration of currentlyRunningService (CountDown value) in milliSeconds
     * @return duration in mm:ss format from duration value in milliSeconds.
     */
    public static String getCurrentDurationPreferenceStringFor(long duration) {
        // https://stackoverflow.com/a/41589025/8411356
        return String.format(Locale.getDefault(), "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(duration) % 60,
                TimeUnit.MILLISECONDS.toSeconds(duration) % 60);
    }

    /**
     * Prepares SoundPool for ticking and ringing sound playback.
     */
    public static void prepareSoundPool(Context context) {
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        //tickID = soundPool.load(context, R.raw.clockticking, 2);
        ringID = soundPool.load(context, R.raw.iphone_alarm, 2);
    }
}