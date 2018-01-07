package com.github.runningforlife.photosniffer.ui.preference;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.preference.SwitchPreference;
import android.util.AttributeSet;

import com.github.runningforlife.photosniffer.ui.activity.TimePickerActivity;

/**
 * when switch is checked, it start an activity
 */

public class SwitchActionPreference extends SwitchPreference {

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

    private void startNightTimeSettingActivity() {
        Intent intent = new Intent(getContext(), TimePickerActivity.class);
        getContext().startActivity(intent);
    }
}
