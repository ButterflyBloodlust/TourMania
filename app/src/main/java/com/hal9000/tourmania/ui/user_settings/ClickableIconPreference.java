package com.hal9000.tourmania.ui.user_settings;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

public class ClickableIconPreference extends Preference {

    private View.OnClickListener onIconClickListener;

    public ClickableIconPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public ClickableIconPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ClickableIconPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ClickableIconPreference(Context context) {
        super(context);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        final ImageView imageView = (ImageView) holder.findViewById(android.R.id.icon);
        if (imageView != null && onIconClickListener != null) {
            imageView.setOnClickListener(onIconClickListener);
        }
    }

    public void setOnIconClickListener(View.OnClickListener onIconClickListener) {
        this.onIconClickListener = onIconClickListener;
    }
}
