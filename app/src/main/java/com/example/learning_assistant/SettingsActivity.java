package com.example.learning_assistant;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.learning_assistant.utils.Constants.RINGING_VOLUME_LEVEL_KEY;
import static com.example.learning_assistant.utils.VolumeSeekBarUtils.convertToFloat;
import static com.example.learning_assistant.utils.VolumeSeekBarUtils.initializeSeekBar;
import static com.example.learning_assistant.utils.VolumeSeekBarUtils.maxVolume;
import static com.example.learning_assistant.utils.VolumeSeekBarUtils.floatRingingVolumeLevel;


public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, SeekBar.OnSeekBarChangeListener {

        @BindView(R.id.work_duration_spinner)
        Spinner workDurationSpinner;
        @BindView(R.id.short_break_duration_spinner)
        Spinner shortBreakDurationSpinner;
        @BindView(R.id.long_break_duration_spinner)
        Spinner longBreakDurationSpinner;
        @BindView(R.id.start_long_break_after_spinner)
        Spinner startlongbreakafterSpinner;

        @BindView(R.id.ringing_seek_bar)
        SeekBar ringingSeekBar;
        @BindView(R.id.about_us_text)
        TextView aboutUsTextView;
        private SharedPreferences preferences;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_settings);

            ButterKnife.bind(this);

            preferences = PreferenceManager.getDefaultSharedPreferences(this);

            initSpinner();

            seekBarInitialization();
            aboutUsTextView.setMovementMethod(LinkMovementMethod.getInstance());
        }

        private void seekBarInitialization() {
            ringingSeekBar = initializeSeekBar(this, ringingSeekBar);
            ringingSeekBar.setOnSeekBarChangeListener(this);
        }

        /**
         * Separate method to populate the spinners
         * This will keep the onCreate() method clean
         */
        private void initSpinner() {
            // Create an array adapter for all three spinners using the string array
            ArrayAdapter<CharSequence> workDurationAdapter = ArrayAdapter.createFromResource(this,
                    R.array.work_duration_array, R.layout.spinner_item);
            ArrayAdapter<CharSequence> shortBreakDurationAdapter = ArrayAdapter.createFromResource(this,
                    R.array.short_break_duration_array, R.layout.spinner_item);
            ArrayAdapter<CharSequence> longBreakDurationAdapter = ArrayAdapter.createFromResource(this,
                    R.array.long_break_duration_array, R.layout.spinner_item);
            ArrayAdapter<CharSequence> startlongbreakafterAdapter = ArrayAdapter.createFromResource(this,
                    R.array.start_long_break_after_array, R.layout.spinner_item);

            // Layout to use when list of choices appears
            workDurationAdapter.setDropDownViewResource(R.layout.spinner_item);
            shortBreakDurationAdapter.setDropDownViewResource(R.layout.spinner_item);
            longBreakDurationAdapter.setDropDownViewResource(R.layout.spinner_item);
            startlongbreakafterAdapter.setDropDownViewResource(R.layout.spinner_item);

            // Apply the adapter to the spinner
            workDurationSpinner.setAdapter(workDurationAdapter);
            shortBreakDurationSpinner.setAdapter(shortBreakDurationAdapter);
            longBreakDurationSpinner.setAdapter(longBreakDurationAdapter);
            startlongbreakafterSpinner.setAdapter(startlongbreakafterAdapter);

            // Set the default selection
            workDurationSpinner.setSelection(preferences.getInt(getString(R.string.work_duration_key), 1));
            shortBreakDurationSpinner.setSelection(preferences.getInt(getString(R.string.short_break_duration_key), 1));
            longBreakDurationSpinner.setSelection(preferences.getInt(getString(R.string.long_break_duration_key), 1));
            startlongbreakafterSpinner.setSelection(preferences.getInt(getString((R.string.start_long_break_after_key)), 2));

            workDurationSpinner.setOnItemSelectedListener(this);
            shortBreakDurationSpinner.setOnItemSelectedListener(this);
            longBreakDurationSpinner.setOnItemSelectedListener(this);
            startlongbreakafterSpinner.setOnItemSelectedListener(this);

        }

        /**
         * Save the latest selected item position from the spinners
         * into SharedPreferences
         *
         * @param parent
         * @param view
         * @param position
         * @param id
         */
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            // initialize the editor
            SharedPreferences.Editor editor = preferences.edit();
            // switch case to handle different spinners
            switch (parent.getId()) {
                // item selected in work duration spinner
                case R.id.work_duration_spinner:
                    // save the corresponding item position
                    editor.putInt(getString(R.string.work_duration_key), position);
                    break;
                // item selected in short break duration spinner
                case R.id.short_break_duration_spinner:
                    // save the corresponding item position
                    editor.putInt(getString(R.string.short_break_duration_key), position);
                    break;
                // item selected in long break duration spinner
                case R.id.long_break_duration_spinner:
                    // save the corresponding item position
                    editor.putInt(getString(R.string.long_break_duration_key), position);
                case R.id.start_long_break_after_spinner:
                    // save the corresponding item position
                    editor.putInt(getString(R.string.start_long_break_after_key), position);

            }
            editor.apply();
        }

        /**
         * When nothing is selected from the spinner
         *
         * @param parent
         */
        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }

        @Override
        public void onBackPressed() {
            super.onBackPressed();
            NavUtils.navigateUpFromSameTask(this);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case android.R.id.home:
                    NavUtils.navigateUpFromSameTask(this);
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    return true;
            }
            return super.onOptionsItemSelected(item);
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            switch (seekBar.getId()) {
                case R.id.ringing_seek_bar:
                    preferences.edit().putInt(RINGING_VOLUME_LEVEL_KEY, progress).apply(); //Save value to SharedPreferences
                    floatRingingVolumeLevel = convertToFloat(preferences.getInt(RINGING_VOLUME_LEVEL_KEY, maxVolume), maxVolume);
            }

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }