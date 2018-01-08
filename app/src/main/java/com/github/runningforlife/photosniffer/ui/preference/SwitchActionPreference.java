package com.github.runningforlife.photosniffer.ui.preference;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.ui.activity.TimePickerActivity;
import com.github.runningforlife.photosniffer.utils.SharedPrefUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * when switch is checked, it start an activity
 */

public class SwitchActionPreference extends SwitchPreference {
    private static final String TAG = "SwitchActionPreference";

    private int[] mHour;
    private int[] mMinute;

    @TargetApi(21)
    public SwitchActionPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public SwitchActionPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    public SwitchActionPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SwitchActionPreference(Context context) {
        super(context);
    }

    @Override
    public void onClick() {
        super.onClick();

        if (isChecked()) {
            startNightTimeSettingActivity();
        }
    }

    @Override
    public void onBindView(View view) {
        super.onBindView(view);
        Log.v(TAG, "onBindView()");

        init();
    }

    private void startNightTimeSettingActivity() {
        Intent intent = new Intent(getContext(), TimePickerActivity.class);
        ArrayList<Integer> time = new ArrayList<>(4);
        time.add(mHour[0]);
        time.add(mHour[1]);
        time.add(mMinute[0]);
        time.add(mMinute[1]);
        intent.putIntegerArrayListExtra("init_time", time);
        getContext().startActivity(intent);
    }

    private void init() {
        mHour = new int[2];
        mMinute = new int[2];

        mHour[0] = 22;
        mHour[1] = 6;

        showSleepTimeToSummery();
    }

    private void showSleepTimeToSummery() {
        String prefNightTimeInterval = getContext().getString(R.string.pref_night_time_interval);
        String prefNightTimeStart = getContext().getString(R.string.pref_night_time_starting);

        long interval = SharedPrefUtil.getLong(prefNightTimeInterval, 0);
        long start = SharedPrefUtil.getLong(prefNightTimeStart, 0);

        if (interval != 0) {
            DateFormat df = new SimpleDateFormat("HH:mm", Locale.US);
            String summeryOnText = getContext().getString(R.string.sleeping_time);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(start);
            mHour[0] = calendar.get(Calendar.HOUR_OF_DAY);
            mMinute[0] = calendar.get(Calendar.MINUTE);
            summeryOnText += "(" + df.format(calendar.getTime()) + "-";

            calendar.setTimeInMillis(start + interval);
            mHour[1] = calendar.get(Calendar.HOUR_OF_DAY);
            mMinute[1] = calendar.get(Calendar.MINUTE);
            summeryOnText += df.format(calendar.getTime()) + ")";
            if (isChecked()) {
                setSummaryOn(summeryOnText);
            }
        }
    }
}
