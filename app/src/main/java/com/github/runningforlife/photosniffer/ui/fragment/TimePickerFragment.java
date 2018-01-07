package com.github.runningforlife.photosniffer.ui.fragment;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.TimePicker;

import java.util.Calendar;

import javax.annotation.Nonnull;

import butterknife.OnClick;

/**
 *  a time picker dialog
 */

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
    private static final String TAG = TimePickerFragment.class.getSimpleName();
    private static final String ARG_TYPE = "type";
    private static final String ARG_HOUR = "hour";
    private static final String ARG_MINUTE = "minute";

    private TimeSetCallback mCallback;

    public interface TimeSetCallback {
        void onTimeSet(int hour, int minute, int type);
    }

    public static TimePickerFragment newInstance(int type, int hour, int minute) {
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_TYPE, type);
        bundle.putInt(ARG_HOUR, hour);
        bundle.putInt(ARG_MINUTE, minute);

        TimePickerFragment fragment = new TimePickerFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    @Nonnull
    public Dialog onCreateDialog(Bundle savedState) {
        super.onCreate(savedState);

        int hour = getArguments().getInt(ARG_HOUR);
        int minute = getArguments().getInt(ARG_MINUTE);

        return new TimePickerDialog(getContext(), this, hour, minute, true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (!(context instanceof TimeSetCallback)) {
            throw new IllegalArgumentException("parent should implement callback");
        }

        mCallback = (TimeSetCallback)context;
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Log.v(TAG,"onTimeSet()");
        mCallback.onTimeSet(hourOfDay, minute, getArguments().getInt(ARG_TYPE));
    }
}
