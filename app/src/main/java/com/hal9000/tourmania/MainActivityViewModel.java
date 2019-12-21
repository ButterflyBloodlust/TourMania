package com.hal9000.tourmania;

import android.os.Bundle;
import java.util.HashMap;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;

/**
 * MainActivity-bound ViewModel used for handling communication between fragments when necessary.
 *
 * <p>Should only be used when closer-scoped view models are not possible
 * (e.g. when unable to use multiple subgraphs due to overlapping use of a fragment).
 */
public class MainActivityViewModel extends ViewModel {
    HashMap<String, Bundle> bundleHashMap = new HashMap<>();

    @Nullable
    public Bundle getAndClearBundle(Class aClass) {
        return bundleHashMap.remove(aClass.getName());
    }

    public void putToBundle(Class aClass, Bundle newBundle) {
        String key = aClass.getName();
        Bundle existingBundle = bundleHashMap.get(key);
        if (existingBundle != null)
            existingBundle.putAll(newBundle);
        else
            bundleHashMap.put(key, newBundle);
    }
}
