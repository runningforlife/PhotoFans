package com.github.runningforlife.photosniffer.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.ui.fragment.TimePickerFragment;
import com.github.runningforlife.photosniffer.utils.SharedPrefUtil;
import com.github.runningforlife.photosniffer.utils.ToastUtil;
import com.github.runningforlife.photosniffer.utils.WallpaperUtils;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * activity to pick night time
 */

public class TimePickerActivity extends AppCompatActivity
            implements TimePickerFragment.TimeSetCallback {

    private static final String TAG = TimePickerActivity.class.getSimpleName();

    private static final int TIME_START = 0;
    private static final int TIME_END = 1;

    private static final int DAY_CURRENT = 0x01;
    private static final int DAY_SECOND = 0x02;

    @BindView(R.id.tv_start_time) TextView mTvStart;
    @BindView(R.id.tv_end_time) TextView mTvEnd;
    @BindView(R.id.spinner_start) Spinner mSpinStart;
    @BindView(R.id.spinner_end) Spinner mSpinEnd;
    /** current or the second day */
    private int[] mDayType;
    private long[] mNightTime;
    private int[] mHour;
    private int[] mMinute;

    private Calendar mCalendar;
    private DateFormat mTimeFormat;

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        setContentView(R.layout.activity_time_picker);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_white_24dp);
        }

        ButterKnife.bind(this);

        mDayType = new int[2];
        mDayType[TIME_START] = DAY_CURRENT;
        mDayType[TIME_END] = DAY_SECOND;

        mNightTime = new long[2];
        mNightTime[TIME_START] = mNightTime[TIME_END] = System.currentTimeMillis();

        mHour = new int[2];
        mMinute = new int[2];

        mHour[TIME_START] = 22;
        mHour[TIME_END] = 6;

        initView();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mNightTime[TIME_START] < mNightTime[TIME_END]) {
            String prefNightTime = getString(R.string.pref_night_time_interval);
            String prefNightTimeStart = getString(R.string.pref_night_time_starting);
            SharedPrefUtil.putLong(prefNightTime, mNightTime[TIME_END] - mNightTime[TIME_START]);
            SharedPrefUtil.putLong(prefNightTimeStart, mNightTime[TIME_START]);

            long currentTime = System.currentTimeMillis();
            if (mNightTime[TIME_START] <= currentTime) {
                WallpaperUtils.restartAutoWallpaperForNightTime(this);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Intent intent = new Intent(this, SettingsActivity.class);
            NavUtils.navigateUpTo(this,intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTimeSet(int hour, int minute, int type) {
        Log.v(TAG,"onTimeSet()");
        mCalendar.set(Calendar.HOUR_OF_DAY, hour);
        mCalendar.set(Calendar.MINUTE, minute);

        if (type == TIME_START) {
            mTvStart.setText(mTimeFormat.format(mCalendar.getTime()));
            mNightTime[TIME_START] = mCalendar.getTimeInMillis();
            if (mDayType[TIME_START] == DAY_SECOND) {
                mNightTime[TIME_START] += TimeUnit.HOURS.toMillis(24);
            }
        } else {
            mTvEnd.setText(mTimeFormat.format(mCalendar.getTime()));
            mNightTime[TIME_END] = mCalendar.getTimeInMillis();
            if (mDayType[TIME_END] == DAY_SECOND) {
                mNightTime[TIME_START] += TimeUnit.HOURS.toMillis(24);
            }
        }

        if (mDayType[TIME_START] >= mDayType[TIME_END] && mNightTime[TIME_START] > mNightTime[TIME_END]) {
            showTimeErrorToast();
        }

        mHour[type] = hour;
        mMinute[type] = minute;
    }

    private void initView() {
        mTvStart.setText(R.string.init_start_night_time);
        mTvEnd.setText(R.string.init_end_night_time);

        mTimeFormat = new SimpleDateFormat("HH:mm", Locale.US);

        mTvStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePicker(TIME_START, mHour[TIME_START], mMinute[TIME_START]);
            }
        });

        mTvEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePicker(TIME_END, mHour[TIME_END], mMinute[TIME_END]);
            }
        });

        mCalendar = Calendar.getInstance();

        ArrayAdapter<CharSequence> startAdapter = ArrayAdapter.createFromResource(this,
                R.array.night_time_start, android.R.layout.simple_spinner_dropdown_item);
        mSpinStart.setAdapter(startAdapter);
        //mSpinStart.setDropDownVerticalOffset(3);
        mSpinStart.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mDayType[0] = position == 0 ? DAY_CURRENT : DAY_SECOND;
                if (mDayType[TIME_START] > mDayType[TIME_END]) {
                    parent.setSelection(0);
                    showTimeErrorToast();
                }
            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        ArrayAdapter<CharSequence> endAdapter = ArrayAdapter.createFromResource(this,
                R.array.night_time_end, android.R.layout.simple_spinner_dropdown_item);
        mSpinEnd.setAdapter(endAdapter);
        mSpinEnd.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mDayType[TIME_END] = position == 1 ? DAY_CURRENT : DAY_SECOND;
                if (mDayType[TIME_START] > mDayType[TIME_END]) {
                    parent.setSelection(0);
                    showTimeErrorToast();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void showTimePicker(int type, int hour, int minute) {
        TimePickerFragment fragment = TimePickerFragment.newInstance(type, hour, minute);
        fragment.show(getSupportFragmentManager(), "TimePicker");
    }

    private void showTimeErrorToast() {
        String timeError = getString(R.string.interval_time_setting_error);
        ToastUtil.showToast(this, timeError);
    }
}
