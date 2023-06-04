package com.astarivi.kaizoyu;

import android.app.Application;

import com.astarivi.kaizoyu.core.updater.UpdateManager;
import com.flurry.android.FlurryAgent;
import com.google.android.material.color.DynamicColors;

import java.io.File;


public class KaizoyuApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        checkDynamicColors();

        if (BuildConfig.DEBUG) {
            FlurryAgent.setVersionName(UpdateManager.VERSION_NAME + "_debug");
        } else {
            FlurryAgent.setVersionName(UpdateManager.VERSION_NAME);
        }

        new FlurryAgent.Builder()
                .withReportLocation(false)
                .withLogEnabled(true)
                .build(this, getString(R.string.flurry_key));

        FlurryAgent.addSessionProperty("version", UpdateManager.VERSION);

        if (BuildConfig.DEBUG) {
            FlurryAgent.addSessionProperty("development", "true");
        }
    }

    private void checkDynamicColors() {
        if (new File(getFilesDir(), "config/disabledcolor.bool").exists()) {
            return;
        }

        DynamicColors.applyToActivitiesIfAvailable(this);
    }
}
