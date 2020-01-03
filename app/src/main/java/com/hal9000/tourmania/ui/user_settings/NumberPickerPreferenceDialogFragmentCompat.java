package com.hal9000.tourmania.ui.user_settings;

import android.content.Context;
import android.view.View;
import android.widget.NumberPicker;

import androidx.preference.DialogPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceDialogFragmentCompat;

public class NumberPickerPreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat implements DialogPreference.TargetFragment
{
    private NumberPicker numberPicker = null;
    private static final int MAX_VAL = 24;
    private static final int MIN_VAL = 1;

    @Override
    protected View onCreateDialogView(Context context)
    {
        numberPicker = new NumberPicker(context);
        numberPicker.setMinValue(MIN_VAL);
        numberPicker.setMaxValue(MAX_VAL);
        return numberPicker;
    }

    @Override
    protected void onBindDialogView(View v)
    {
        super.onBindDialogView(v);
        NumberPickerPreference pref = (NumberPickerPreference) getPreference();
        numberPicker.setValue(pref.hours);
    }

    @Override
    public void onDialogClosed(boolean positiveResult)
    {
        if (positiveResult)
        {
            NumberPickerPreference pref = (NumberPickerPreference) getPreference();
            pref.hours = numberPicker.getValue();

            int value = pref.hours;
            if (pref.callChangeListener(value)) pref.persistIntValue(value);
        }
    }

    @Override
    public Preference findPreference(CharSequence charSequence)
    {
        return getPreference();
    }
}
