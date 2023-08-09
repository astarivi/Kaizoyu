package com.astarivi.kaizoyu;

import android.app.Application;
import android.content.Context;

import com.google.android.material.color.DynamicColors;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.config.CoreConfigurationBuilder;
import org.acra.config.HttpSenderConfigurationBuilder;
import org.acra.data.StringFormat;
import org.acra.sender.HttpSender;

import java.io.File;
import java.lang.ref.WeakReference;


public class KaizoyuApplication extends Application {
    public static WeakReference<Application> application;

    public static Application getApplication() {
        return application.get();
    }

    public static Context getContext() {
        return getApplication().getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        application = new WeakReference<>(KaizoyuApplication.this);
        checkDynamicColors();

        ACRA.init(this, new CoreConfigurationBuilder()
                .withBuildConfigClass(BuildConfig.class)
                .withReportFormat(StringFormat.JSON)
                .withReportContent(
                        ReportField.APP_VERSION_CODE,
                        ReportField.APP_VERSION_NAME,
                        ReportField.PHONE_MODEL,
                        ReportField.STACK_TRACE,
                        ReportField.THREAD_DETAILS,
                        ReportField.USER_CRASH_DATE,
                        ReportField.USER_APP_START_DATE,
                        ReportField.REPORT_ID,
                        ReportField.ANDROID_VERSION,
                        ReportField.BRAND
                )
                .withPluginConfigurations(
                        new HttpSenderConfigurationBuilder()
                                .withUri("https://acra.kaizoyu.ovh/report")
                                .withHttpMethod(HttpSender.Method.POST)
                                .withBasicAuthLogin(getString(R.string.acra_user))
                                .withBasicAuthPassword(getString(R.string.acra_pass))
                                .build()
                )
        );
    }

    private void checkDynamicColors() {
        if (new File(getFilesDir(), "config/disabledcolor.bool").exists()) {
            return;
        }

        DynamicColors.applyToActivitiesIfAvailable(this);
    }
}
