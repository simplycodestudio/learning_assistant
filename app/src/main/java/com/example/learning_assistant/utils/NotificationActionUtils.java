package com.example.learning_assistant.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;


import com.example.learning_assistant.R;
import com.example.learning_assistant.StartTimerActionReceiver;


import static com.example.learning_assistant.utils.Constants.CZAS_NA_NAUKE;
import static com.example.learning_assistant.utils.Constants.DLUGA_PRZERWA;
import static com.example.learning_assistant.utils.Constants.INTENT_NAME_ACTION;
import static com.example.learning_assistant.utils.Constants.INTENT_VALUE_LONG_BREAK;
import static com.example.learning_assistant.utils.Constants.INTENT_VALUE_SHORT_BREAK;
import static com.example.learning_assistant.utils.Constants.INTENT_VALUE_START;
import static com.example.learning_assistant.utils.Constants.KROTKA_PRZERWA;



public class NotificationActionUtils {
    /**
     * @param currentlyRunningServiceType The next service that shall be run
     * @return Returns Action Buttons and assigns pendingIntents to Actions
     */
    public static NotificationCompat.Action getIntervalAction(int currentlyRunningServiceType,
                                                              Context context) {

        switch (currentlyRunningServiceType) {
            case CZAS_NA_NAUKE:
                return new NotificationCompat.Action(
                        R.drawable.play,
                        context.getString(R.string.start_tametu),
                        getPendingIntent(KROTKA_PRZERWA, INTENT_VALUE_START, context));
            case KROTKA_PRZERWA:
                return new NotificationCompat.Action(
                        R.drawable.short_break,
                        context.getString(R.string.start_short_break),
                        getPendingIntent(KROTKA_PRZERWA, INTENT_VALUE_SHORT_BREAK, context));
            case DLUGA_PRZERWA:
                return new NotificationCompat.Action(
                        R.drawable.long_break,
                        context.getString(R.string.start_long_break),
                        getPendingIntent(DLUGA_PRZERWA, INTENT_VALUE_LONG_BREAK, context));
            default:
                return null;
        }

    }

    private static PendingIntent getPendingIntent(int requestCode, String INTENT_VALUE, Context context) {
        Intent startIntent = new Intent(context, StartTimerActionReceiver.class)
                .putExtra(INTENT_NAME_ACTION, INTENT_VALUE);
        return PendingIntent.getBroadcast(
                context,
                requestCode,
                startIntent,
                PendingIntent.FLAG_ONE_SHOT);
    }
}
