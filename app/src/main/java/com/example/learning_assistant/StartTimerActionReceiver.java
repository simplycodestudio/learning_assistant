package com.example.learning_assistant;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


import com.example.learning_assistant.utils.Utils;


import static com.example.learning_assistant.utils.Constants.CZAS_NA_NAUKE;
import static com.example.learning_assistant.utils.Constants.DLUGA_PRZERWA;
import static com.example.learning_assistant.utils.Constants.INTENT_NAME_ACTION;
import static com.example.learning_assistant.utils.Constants.INTENT_VALUE_LONG_BREAK;
import static com.example.learning_assistant.utils.Constants.INTENT_VALUE_SHORT_BREAK;
import static com.example.learning_assistant.utils.Constants.INTENT_VALUE_START;
import static com.example.learning_assistant.utils.Constants.KROTKA_PRZERWA;

import static com.example.learning_assistant.utils.StartTimerUtils.startTimer;


public class StartTimerActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String receivedIntent = intent.getStringExtra(INTENT_NAME_ACTION);
        SharedPreferences prefences = PreferenceManager.getDefaultSharedPreferences(context);
        switch (receivedIntent) {
            case INTENT_VALUE_START:
                long workDuration = Utils.getCurrentDurationPreferenceOf(prefences, context,
                        CZAS_NA_NAUKE);
                startTimer(workDuration, context);
                break;
            case INTENT_VALUE_SHORT_BREAK:
                long shortBreakDuration = Utils.getCurrentDurationPreferenceOf(prefences, context,
                        KROTKA_PRZERWA);
                startTimer(shortBreakDuration, context);
                break;
            case INTENT_VALUE_LONG_BREAK:
                long longBreakDuration = Utils.getCurrentDurationPreferenceOf(prefences, context,
                        DLUGA_PRZERWA);
                startTimer(longBreakDuration, context);
        }
    }
}
