package com.astarivi.kaizoyu;

import android.app.Application;
import android.content.Context;

import com.astarivi.kaizoyu.core.common.NotificationsHub;
import com.astarivi.kaizoyu.core.storage.properties.ExtendedProperties;
import com.astarivi.kaizoyu.core.threading.workers.WorkerInitializers;
import com.astarivi.kaizoyu.utils.Data;
import com.google.android.material.color.DynamicColors;
import com.startapp.sdk.adsbase.StartAppSDK;

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

        //noinspection ConstantValue
        StartAppSDK.setTestAdsEnabled(com.astarivi.kaizoyu.BuildConfig.VERSION_NAME.contains("-DEBUG"));
        ExtendedProperties appSettings = Data.getProperties(Data.CONFIGURATION.APP);

        StartAppSDK.setUserConsent(
                this,
                "pas",
                System.currentTimeMillis(),
                appSettings.getBooleanProperty("gdpr_consent", false)
        );

        NotificationsHub.initialize();
        WorkerInitializers.queueWorkers(this);
    }

    private void checkDynamicColors() {
        if (new File(getFilesDir(), "config/disabledcolor.bool").exists()) {
            return;
        }

        DynamicColors.applyToActivitiesIfAvailable(this);
    }
}
