package com.hal9000.tourmania.ui.user_settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

public class NumberPickerPreference extends DialogPreference
{
    int hours = 0;

    public NumberPickerPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index)
    {
        return a.getInt(index, 1);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue)
    {
        int value;
        if (restoreValue)
        {
            if (defaultValue == null) value = getPersistedInt(1);
            else value = getPersistedInt(Integer.parseInt(defaultValue.toString()));
        }
        else
        {
            value = Integer.parseInt(defaultValue.toString());
        }

        hours = value;
    }

    public void persistIntValue(int value)
    {
        persistInt(value);
    }
}
