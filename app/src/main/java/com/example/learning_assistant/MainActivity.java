package com.example.learning_assistant;



import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.learning_assistant.utils.Utils;


import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.learning_assistant.utils.CheckMarkUtils.updateCheckMarkCount;
import static com.example.learning_assistant.utils.Constants.CHANNEL_ID;
import static com.example.learning_assistant.utils.Constants.COMPLETE_ACTION_BROADCAST;
import static com.example.learning_assistant.utils.Constants.COUNTDOWN_BROADCAST;
import static com.example.learning_assistant.utils.Constants.CZAS_NA_NAUKE;
import static com.example.learning_assistant.utils.Constants.DLUGA_PRZERWA;
import static com.example.learning_assistant.utils.Constants.KROTKA_PRZERWA;
import static com.example.learning_assistant.utils.Constants.LONG_BREAK_DURATION_KEY;
import static com.example.learning_assistant.utils.Constants.SHORT_BREAK_DURATION_KEY;
import static com.example.learning_assistant.utils.Constants.START_ACTION_BROADCAST;
import static com.example.learning_assistant.utils.Constants.START_LONG_BREAK_AFTER_KEY;
import static com.example.learning_assistant.utils.Constants.STOP_ACTION_BROADCAST;
import static com.example.learning_assistant.utils.Constants.TASK_INFORMATION_NOTIFICATION_ID;
import static com.example.learning_assistant.utils.Constants.TASK_ON_HAND_COUNT_KEY;
import static com.example.learning_assistant.utils.Constants.WORK_DURATION_KEY;
import static com.example.learning_assistant.utils.NotificationActionUtils.getIntervalAction;
import static com.example.learning_assistant.utils.StartTimerUtils.startTimer;
import static com.example.learning_assistant.utils.StopTimerUtils.sessionCancel;
import static com.example.learning_assistant.utils.StopTimerUtils.sessionComplete;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener {


    public static int currentlyRunningServiceType; // Type of Service can be TAMETU, SHORT_BREAK or LONG_BREAK
    BroadcastReceiver stoppedBroadcastReceiver;
    BroadcastReceiver countDownReceiver;
    BroadcastReceiver completedBroadcastReceiver;
    BroadcastReceiver startBroadcastReceiver;
    @BindView(R.id.settings_btn)
    ImageView settingsImageView;
    @BindView(R.id.timer_button_main)
    ToggleButton timerButton;
    @BindView(R.id.countdown_textview_main)
    TextView countDownTextView;
    @BindView(R.id.tick_btn)
    ImageView finishImageView; // (Complete Button)
    private long workDuration; // Time Period for Pomodoro (Work-Session)
    private String workDurationString; // Time Period for Pomodoro in String
    private long shortBreakDuration; // Time Period for Short-Break
    private String shortBreakDurationString; // Time Period for Short-Break in String
    private long longBreakDuration; // Time Period for Long-Break
    private String longBreakDurationString; // Time Period for Long-Break in String
    private SharedPreferences preferences;
    private AlertDialog alertDialog;
    private boolean isAppVisible = true;
    private String currentCountDown; // Current duration for Work-Session, Short-Break or Long-Break

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isAppVisible = true;
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        ButterKnife.bind(this);
        setOnClickListeners();

        // Set button as checked if the service is already running.
        timerButton.setChecked(isServiceRunning(CountDownTimerService.class));

        // Receives broadcast that the timer has stopped.
        stoppedBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                sessionCompleteAVFeedback(context);
            }
        };

        // Receives broadcast for countDown at every tick.
        countDownReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getExtras() != null) {
                    currentCountDown = intent.getExtras().getString("countDown");
                    setTextCountDownTextView(currentCountDown);
                }
            }
        };

        //Receives broadcast when timer completes its time
        completedBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                sessionCompleteAVFeedback(context);
            }
        };

        //Receives broadcast when timer starts
        startBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                sessionStartAVFeedback();
            }
        };

        retrieveDurationValues(); //Duration values for Session and Short and Long Breaks
        setInitialValuesOnScreen(); //Button Text and Worksession Count

        alertDialog = createTametuCompletionAlertDialog();
        displayTametuCompletionAlertDialog();

        final EditText message = (EditText) findViewById(R.id.current_task_name_textview_main);

        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);

        message.setText(prefs.getString("autoSave", ""));

        if (message.getText().toString().trim().length() == 0)
            message.setText("Task 1", TextView.BufferType.EDITABLE);


        message.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                preferences.edit().putInt(getString(R.string.task_on_hand_count_key), 0).apply();
                prefs.edit().putString("autoSave", s.toString()).apply();

            }
        });

        preferences.registerOnSharedPreferenceChangeListener(this);


    }

    private void sessionStartAVFeedback() {
        ToggleButton toggleButton = findViewById(R.id.timer_button_main);
        toggleButton.setChecked(true);

        try {
            if (alertDialog.isShowing())
                alertDialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setInitialValuesOnScreen() {
        // Changing textOn & textOff according to value of currentlyRunningServiceType.
        currentlyRunningServiceType = Utils.retrieveCurrentlyRunningServiceType(preferences, this);
        changeToggleButtonStateText(currentlyRunningServiceType);

        // Retrieving value of workSessionCount (Current value of workSessionCount) from SharedPreference.
        updateCheckMarkCount(this);
    }

    private void retrieveDurationValues() {
        // Retrieving current value of Duration for POMODORO, SHORT_BREAK and
        // LONG_BREAK from SharedPreferences.
        workDuration = Utils.getCurrentDurationPreferenceOf(preferences, this, CZAS_NA_NAUKE);
        shortBreakDuration = Utils.getCurrentDurationPreferenceOf(preferences, this, KROTKA_PRZERWA);
        longBreakDuration = Utils.getCurrentDurationPreferenceOf(preferences, this, DLUGA_PRZERWA);

        // Retrieving duration in mm:ss format from duration value in milliSeconds.
        workDurationString = Utils.getCurrentDurationPreferenceStringFor(workDuration);
        shortBreakDurationString = Utils.getCurrentDurationPreferenceStringFor(shortBreakDuration);
        longBreakDurationString = Utils.getCurrentDurationPreferenceStringFor(longBreakDuration);
    }

    private void sessionCompleteAVFeedback(Context context) {
        // Retrieving value of currentlyRunningServiceType from SharedPreferences.
        currentlyRunningServiceType = Utils.retrieveCurrentlyRunningServiceType(preferences,
                getApplicationContext());
        changeToggleButtonStateText(currentlyRunningServiceType);
        alertDialog = createTametuCompletionAlertDialog();
        displayTametuCompletionAlertDialog();
        displayTaskInformationNotification();
        //Reset Timer TextView
        String duration = Utils.getCurrentDurationPreferenceStringFor(Utils.
                getCurrentDurationPreferenceOf(preferences, context, currentlyRunningServiceType));
        setTextCountDownTextView(duration);
    }

    private void setOnClickListeners() {
        settingsImageView.setOnClickListener(this);
        timerButton.setOnClickListener(this);
        finishImageView.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        isAppVisible = true;
        currentlyRunningServiceType = Utils.retrieveCurrentlyRunningServiceType(preferences, this);
        super.onStart();
    }

    @Override
    protected void onResume() {
        isAppVisible = true;
        registerLocalBroadcastReceivers();
        // Creates new Alert Dialog.
        alertDialog = createTametuCompletionAlertDialog();
        displayTametuCompletionAlertDialog();
        super.onResume();
    }

    @Override
    protected void onPause() {
        isAppVisible = false;
        super.onPause();
    }

    @Override
    protected void onStop() {
        isAppVisible = false;
        if (!isServiceRunning(CountDownTimerService.class)) {
            unregisterLocalBroadcastReceivers();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        isAppVisible = false;
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        currentCountDown = countDownTextView.getText().toString();
        outState.putString("currentCountDown", currentCountDown);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentCountDown = savedInstanceState.getString("currentCountDown");
        setTextCountDownTextView(currentCountDown);
    }

    @Override
    public void onClick(View v) {
        registerLocalBroadcastReceivers();

        // Retrieving value of currentlyRunningServiceType from SharedPreferences.
        currentlyRunningServiceType = Utils.retrieveCurrentlyRunningServiceType(preferences, this);

        // Switch case to handle different button clicks
        switch (v.getId()) {

            // Settings button is clicked
            case R.id.settings_btn:
                // launch SettingsActivity
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                break;

            case R.id.timer_button_main:
                Date date = new Date(System.currentTimeMillis()); //or simply new Date();
                long millis = date.getTime();
                int resume = (int) millis / 1000;
                int pause = preferences.getInt("pause", 0);
                if ((resume - pause) >= 14400)
                    preferences.edit().putInt(getString(R.string.work_session_count_key), 0).apply();

                if (currentlyRunningServiceType == CZAS_NA_NAUKE) {
                    if (timerButton.isChecked()) {
                        startTimer(workDuration, this);
                    } else {
                        // When "Cancel Pomodoro" is clicked, service is stopped and toggleButton
                        // is reset to "Start Pomodoro".
                        sessionCancel(this, preferences);
                    }
                } else if (currentlyRunningServiceType == KROTKA_PRZERWA) {
                    if (timerButton.isChecked()) {
                        startTimer(shortBreakDuration, this);
                    } else {
                        // When "Skip Short Break" is clicked, service is stopped and toggleButton
                        // is reset to "Start Pomodoro".
                        sessionCancel(this, preferences);
                    }
                } else if (currentlyRunningServiceType == DLUGA_PRZERWA) {
                    if (timerButton.isChecked()) {
                        startTimer(longBreakDuration, this);
                    } else {
                        // When "Skip Long Break" is clicked, service is stopped and toggleButton
                        // is reset to "Start Pomodoro".
                        sessionCancel(this, preferences);
                    }
                }
                break;

            case R.id.tick_btn:
                if (timerButton.isChecked()) {
                    // Finish (Complete Button) stops service and sets currentlyRunningServiceType
                    // to SHORT_BREAK or LONG_BREAK and updates number of completed WorkSessions.
                    sessionComplete(this);
                }
                break;
        }
    }

    /**
     * Changes textOn, textOff for Toggle Button & Resets CountDownTimer to initial value,
     * according to value of currentlyRunningServiceType.
     *
     * @param currentlyRunningServiceType can be POMODORO, SHORT_BREAK or LONG_BREAK.
     */
    private void changeToggleButtonStateText(int currentlyRunningServiceType) {
        timerButton.setChecked(isServiceRunning(CountDownTimerService.class));
        if (currentlyRunningServiceType == CZAS_NA_NAUKE) {
            countDownTextView.setText(workDurationString);
        } else if (currentlyRunningServiceType == KROTKA_PRZERWA) {
            countDownTextView.setText(shortBreakDurationString);
        } else if (currentlyRunningServiceType == DLUGA_PRZERWA) {
            countDownTextView.setText(longBreakDurationString);
        }

        /*
         https://stackoverflow.com/a/3792554/4593315
         While changing textOn, textOff programmatically, button doesn't redraw so I used this hack.
          */
        timerButton.setChecked(timerButton.isChecked());
    }

    /**
     * Registers LocalBroadcastReceivers.
     */
    private void registerLocalBroadcastReceivers() {
        LocalBroadcastManager.getInstance(this).registerReceiver((stoppedBroadcastReceiver),
                new IntentFilter(STOP_ACTION_BROADCAST));
        LocalBroadcastManager.getInstance(this).registerReceiver((countDownReceiver),
                new IntentFilter(COUNTDOWN_BROADCAST));
        LocalBroadcastManager.getInstance(this).registerReceiver(completedBroadcastReceiver,
                new IntentFilter(COMPLETE_ACTION_BROADCAST));
        LocalBroadcastManager.getInstance(this).registerReceiver(startBroadcastReceiver,
                new IntentFilter(START_ACTION_BROADCAST));
    }

    /**
     * Unregisters LocalBroadcastReceivers.
     */
    private void unregisterLocalBroadcastReceivers() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(stoppedBroadcastReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(countDownReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(completedBroadcastReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(startBroadcastReceiver);
    }

    private void setTextCountDownTextView(String duration) {
        countDownTextView.setText(duration);
    }

    /**
     * Checks if a service is running or not.
     *
     * @param serviceClass name of the Service class.
     * @return true if service is running, otherwise false.
     */
    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Creates layout for alert-dialog, which is shown when Pomodoro (Work-Session) is completed.
     *
     * @return alert-dialog
     */


    private AlertDialog createTametuCompletionAlertDialog() {
        if (alertDialog != null)
            alertDialog.cancel();

        View alertDialogLayout = View.inflate(getApplicationContext(), R.layout.layout_alert_dialog, null);
        final Button startBreakLargeButton = alertDialogLayout.findViewById(R.id.start_break);
        final Button startOtherBreakMediumButton = alertDialogLayout.findViewById(R.id.start_other_break);
        Button skipBreakSmallButton = alertDialogLayout.findViewById(R.id.skip_break);

        if (currentlyRunningServiceType == KROTKA_PRZERWA) {
            startBreakLargeButton.setText(R.string.start_short_break);
            startOtherBreakMediumButton.setText(R.string.start_long_break);
        } else if (currentlyRunningServiceType == DLUGA_PRZERWA) {
            startBreakLargeButton.setText(R.string.start_long_break);
            startOtherBreakMediumButton.setText(R.string.start_short_break);
        }

        startBreakLargeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String currentButtonText = startBreakLargeButton.getText().toString();
                startBreakFromAlertDialog(currentButtonText);
            }
        });

        startOtherBreakMediumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String currentButtonText = startOtherBreakMediumButton.getText().toString();
                startBreakFromAlertDialog(currentButtonText);
            }
        });

        skipBreakSmallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sessionCancel(MainActivity.this, preferences);
            }
        });

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(alertDialogLayout);
        alertDialogBuilder.setCancelable(false);
        return alertDialogBuilder.create();
    }

    /**
     * Displays alert dialog when a Pomodoro (Work-Session) is finished.
     */
    private void displayTametuCompletionAlertDialog() {
        if (currentlyRunningServiceType != CZAS_NA_NAUKE && isAppVisible && !alertDialog.isShowing() && !isServiceRunning(CountDownTimerService.class)) {
            alertDialog.show();
        }
    }

    /**
     * Sets appropriate values for medium and large button, and starts service; either SHORT_BREAK or LONG_BREAK.
     *
     * @param currentButtonText button text of either medium button or large button.
     */
    private void startBreakFromAlertDialog(String currentButtonText) {
        long breakDuration = 0;
        if (currentButtonText.equals(getString(R.string.start_long_break))) {
            Utils.updateCurrentlyRunningServiceType(preferences, getApplicationContext(), DLUGA_PRZERWA);
            breakDuration = longBreakDuration;
        } else if (currentButtonText.equals(getString(R.string.start_short_break))) {
            Utils.updateCurrentlyRunningServiceType(preferences, getApplicationContext(), KROTKA_PRZERWA);
            breakDuration = shortBreakDuration;
        }

        currentlyRunningServiceType = Utils.retrieveCurrentlyRunningServiceType(preferences, getApplicationContext());
        if (alertDialog != null)
            alertDialog.cancel();
        registerLocalBroadcastReceivers();
        changeToggleButtonStateText(currentlyRunningServiceType);
        startTimer(breakDuration, this);
        timerButton.setChecked(isServiceRunning(CountDownTimerService.class));
    }

    /**
     * Creates structure for a notification which is shown when a task is Completed.
     * Task can be POMODORO, SHORT_BREAK, LONG_BREAK
     *
     * @return notification.
     */
    private NotificationCompat.Builder createTaskInformationNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.notif_logo)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setColor(getResources().getColor(R.color.colorPrimary))
                .setUsesChronometer(true); //timer that counts-up. Displays time in-between two sessions

        switch (currentlyRunningServiceType) {
            case CZAS_NA_NAUKE:
                notificationBuilder
                        .addAction(getIntervalAction(CZAS_NA_NAUKE, MainActivity.this))
                        .setContentTitle(getString(R.string.break_over_notification_title))
                        .setContentText(getString(R.string.break_over_notification_content_text));
                break;
            case KROTKA_PRZERWA:
                notificationBuilder
                        .addAction(getIntervalAction(KROTKA_PRZERWA, MainActivity.this))
                        .addAction(getIntervalAction(DLUGA_PRZERWA, MainActivity.this))
                        .setContentTitle(getString(R.string.tametu_completion_notification_message))
                        .setContentText(getString(R.string.session_over_notification_content_text));
                break;
            case DLUGA_PRZERWA:
                notificationBuilder
                        .addAction(getIntervalAction(DLUGA_PRZERWA, MainActivity.this))
                        .addAction(getIntervalAction(KROTKA_PRZERWA, MainActivity.this))
                        .setContentTitle(getString(R.string.tametu_completion_alert_message))
                        .setContentText(getString(R.string.session_over_notification_content_text));
                break;
            default:
        }

        return notificationBuilder;
    }

    /**
     * Displays a notification when foreground service is finished.
     */
    private void displayTaskInformationNotification() {
        Notification notification = createTaskInformationNotification().build();
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat
                .from(this);

        // Clearing any previous notifications.
        notificationManagerCompat
                .cancel(TASK_INFORMATION_NOTIFICATION_ID);

        // Displays a notification.
        if (!isServiceRunning(CountDownTimerService.class)) {
            notificationManagerCompat
                    .notify(TASK_INFORMATION_NOTIFICATION_ID, notification);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case WORK_DURATION_KEY:
            case SHORT_BREAK_DURATION_KEY:
            case LONG_BREAK_DURATION_KEY:
            case START_LONG_BREAK_AFTER_KEY:
                retrieveDurationValues();
                setInitialValuesOnScreen();
                break;
            case TASK_ON_HAND_COUNT_KEY:
                updateCheckMarkCount(this);
        }
    }
}

