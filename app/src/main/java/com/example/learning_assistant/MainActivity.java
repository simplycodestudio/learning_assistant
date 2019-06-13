package com.example.learning_assistant;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    MediaPlayer player;

    private TextView mTextViewCountDown;
    private Button mButtonStartPause;
    private Button mButtonReset;
    private EditText mEditTextInput_Learn;
    private EditText mEditTextInput_Break;
    private Button setTimeBtn;

    private CountDownTimer mCountDownTimer;

    private boolean mTimerRunning;
    private long mStartTimeInMillis;
    private long mTimeLeftInMillis;
    private long mEndTime;
    private boolean czasNaPrzerwe;
    long millisInput_learn;
    long millisInput_break;
    int mnoznikCzasuNauki;
    String input_learn;
    String input_break;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextViewCountDown = findViewById(R.id.text_view_countdown);
        mEditTextInput_Learn = findViewById(R.id.learn_interval);
        mEditTextInput_Break = findViewById(R.id.break_interval);
        setTimeBtn = findViewById(R.id.btn_set_time);
        mButtonStartPause = findViewById(R.id.button_start_pause);
        mButtonReset = findViewById(R.id.button_reset);

        mButtonStartPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTimerRunning) {
                    pauseTimer();
                } else {
                    startTimer();
                }
            }
        });

        mButtonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTimer();
            }
        });

        setTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             input_learn = mEditTextInput_Learn.getText().toString();

             if (input_learn.length() ==0)
             {
                 Toast.makeText(MainActivity.this, "Ale z Ciebie obibok", Toast.LENGTH_LONG).show();
                 return;
             }
             millisInput_learn = Long.parseLong(input_learn) * 60000;
             if (millisInput_learn ==0)
             {
                 Toast.makeText(MainActivity.this, "Wprowadz wlasciwe dane", Toast.LENGTH_LONG).show();
                 return;
             }

             input_break = mEditTextInput_Break.getText().toString();
             if (input_break.length() ==0)
             {
                 Toast.makeText(MainActivity.this, "Nie odpoczywasz?", Toast.LENGTH_LONG).show();
                 return;
             }
             millisInput_break = Long.parseLong(input_break) * 60000;
             if (millisInput_break ==0)
             {
                    Toast.makeText(MainActivity.this, "Wprowadz wlasciwe dane", Toast.LENGTH_LONG).show();
                    return;
             }
           // trzeba to przeniesc w przelaczTimer
             przelaczTimer();
             mEditTextInput_Learn.setText("");
             mEditTextInput_Break.setText("");

            }

        });
    }


    public void przelaczTimer() {


        if (czasNaPrzerwe)
        {
            setTime(millisInput_break);

        }
        else
        {
            setTime(millisInput_learn);
            mnoznikCzasuNauki++;

        }

    }

    private void setTime(long milliseconds) {

        mStartTimeInMillis = milliseconds;
        resetTimer();
        closeKeyboard();

    }


    private void startTimer() {
        mEndTime = System.currentTimeMillis() + mTimeLeftInMillis;

        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                mTimerRunning = false;
                updateWatchInterface();
               // Toast.makeText(MainActivity.this, "koneq", Toast.LENGTH_LONG).show();
                if (!czasNaPrzerwe)
                {
                    dialogPrzerwy();
                }
                else if(czasNaPrzerwe)
                {
                    dialogNauki();
                }
                alarm_start();
            }
        }.start();

        mTimerRunning = true;
        updateWatchInterface();
    }

    private void pauseTimer() {
        mCountDownTimer.cancel();
        mTimerRunning = false;
        updateWatchInterface();
    }

    private void resetTimer() {
        mTimeLeftInMillis = mStartTimeInMillis;
        updateCountDownText();
        updateWatchInterface();
    }

    private void updateCountDownText() {
        int hours = (int) (mTimeLeftInMillis / 1000) / 3600;
        int minutes = (int) ((mTimeLeftInMillis / 1000) % 3600) / 60;
        int seconds = (int) (mTimeLeftInMillis / 1000) % 60;

        String timeLeftFormatted;
        if (hours > 0) {
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%d:%02d:%02d", hours, minutes, seconds);
        } else {
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%02d:%02d", minutes, seconds);
        }

        mTextViewCountDown.setText(timeLeftFormatted);


        if (hours ==0 && minutes ==0 && seconds ==0)
        {
            Toast.makeText(MainActivity.this, "koncoweczka", Toast.LENGTH_LONG).show();
        }

    }

    private void updateWatchInterface() {
        if (mTimerRunning) {
            mEditTextInput_Break.setVisibility(View.INVISIBLE);
            setTimeBtn.setVisibility(View.INVISIBLE);
            mEditTextInput_Learn.setVisibility(View.INVISIBLE);
            mButtonReset.setVisibility(View.INVISIBLE);
            mButtonStartPause.setText("Pause");
        } else {
            mEditTextInput_Break.setVisibility(View.VISIBLE);
            setTimeBtn.setVisibility(View.VISIBLE);
            mEditTextInput_Learn.setVisibility(View.VISIBLE);
            mButtonStartPause.setText("Start");

            if (mTimeLeftInMillis < 1000) {
                mButtonStartPause.setVisibility(View.INVISIBLE);
            } else {
                mButtonStartPause.setVisibility(View.VISIBLE);
            }

            if (mTimeLeftInMillis < mStartTimeInMillis) {
                mButtonReset.setVisibility(View.VISIBLE);
            } else {
                mButtonReset.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putLong("startTimeInMillis", mStartTimeInMillis);
        editor.putLong("millisLeft", mTimeLeftInMillis);
        editor.putBoolean("timerRunning", mTimerRunning);
        editor.putLong("endTime", mEndTime);

        editor.apply();

        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
        alarm_stop();
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);

        mStartTimeInMillis = prefs.getLong("startTimeInMillis", 600000);
        mTimeLeftInMillis = prefs.getLong("millisLeft", mStartTimeInMillis);
        mTimerRunning = prefs.getBoolean("timerRunning", false);

        updateCountDownText();
        updateWatchInterface();

        if (mTimerRunning) {
            mEndTime = prefs.getLong("endTime", 0);
            mTimeLeftInMillis = mEndTime - System.currentTimeMillis();

            if (mTimeLeftInMillis < 0) {
                mTimeLeftInMillis = 0;
                mTimerRunning = false;
                updateCountDownText();
                updateWatchInterface();

            } else {
                startTimer();
            }
        }
    }

    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null)
        {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void dialogPrzerwy() {
        AlertDialog.Builder a_builder = new AlertDialog.Builder(MainActivity.this);
        a_builder.setMessage("Szybka powtórka dotychczasowego materiału? Czas Twojej przerwy zostanie podwojony")
                .setCancelable(false)
                .setPositiveButton("Tak",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alarm_stop();
                        PrzerwaZPowtorka();
                    }
                })
                .setNegativeButton("Odpoczywam",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alarm_stop();
                        PrzerwaBezPowtorki();
                    }
                }) ;
        AlertDialog alert = a_builder.create();
        alert.setTitle("Odpoczynek");
        alert.show();
    }
    private void dialogNauki() {
        AlertDialog.Builder a_builder = new AlertDialog.Builder(MainActivity.this);
        a_builder.setMessage("Dobrze Ci idzie, uczymy się dalej?")
                .setCancelable(false)
                .setPositiveButton("Tak",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TargetNauka();
                        alarm_stop();
                    }
                })
                .setNegativeButton("Sesja poczeka",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alarm_stop();
                        dialogLacznyCzasNauki();
                        return;

                    }
                }) ;
        AlertDialog alert = a_builder.create();
        alert.setTitle("Odpoczynek");
        alert.show();
    }
    private void dialogLacznyCzasNauki() {
        int calkowityczas = mnoznikCzasuNauki*Integer.parseInt(input_learn);
        AlertDialog.Builder a_builder = new AlertDialog.Builder(MainActivity.this);

        a_builder.setMessage("Twój czas nauki wyniósł " + calkowityczas + "minutę")
                .setCancelable(false)
                .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });
        AlertDialog alert = a_builder.create();
        alert.setTitle("Odpoczynek");
        alert.show();
    }


    private void TargetNauka() {
        czasNaPrzerwe = false;
        resetTimer();
        przelaczTimer();
        startTimer();
    }

    private void PrzerwaBezPowtorki() {
        czasNaPrzerwe = true;
        resetTimer();
        przelaczTimer();
        startTimer();

    }

    private void PrzerwaZPowtorka() {
        czasNaPrzerwe = true;
        resetTimer();
        millisInput_break = millisInput_break*2;
        przelaczTimer();
        startTimer();

    }

    public void alarm_start() {
        if(player == null) {
            player = MediaPlayer.create(this, R.raw.iphone_alarm);
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    alarm_stop();
                }
            });
        }
        player.start();
    }

    public void alarm_stop(){
        if (player!=null)
        {
            player.release();
            player = null;
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }
}