package com.astarivi.kaizoyu.core.threading.workers;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.astarivi.kaizoyu.BuildConfig;
import com.astarivi.kaizoyu.R;
import com.astarivi.kaizoyu.core.common.NotificationsHub;
import com.astarivi.kaizoyu.core.updater.UpdateManager;
import com.astarivi.kaizoyu.updater.UpdaterActivity;
import com.astarivi.kaizoyu.utils.Data;

import java.io.IOException;


public class UpdatePeriodicWorker extends Worker {
    private final static NotificationsHub.Channel channel = NotificationsHub.Channel.APP_UPDATES;

    public UpdatePeriodicWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        if (NotificationsHub.areNotificationDisabled(channel))
            return Result.success();

        if (!Data.getProperties(Data.CONFIGURATION.APP)
                .getBooleanProperty("autoupdate", true))
            return Result.success();

        Context context = getApplicationContext();

        UpdateManager.AppUpdate latestUpdate;
        try {
            latestUpdate = UpdateManager.getAppUpdate();
        } catch (IOException e) {
            return Result.failure();
        }

        String versionToSkip = Data.getProperties(Data.CONFIGURATION.APP)
                .getProperty("skip_version", "false");

        if (latestUpdate == null || versionToSkip.equals(latestUpdate.getVersion()))
            return Result.success();

        Intent intent = new Intent();
        intent.setClassName(BuildConfig.APPLICATION_ID, UpdaterActivity.class.getName());
        intent.putExtra("latestUpdate", latestUpdate);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, channel.getValue())
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle(context.getString(R.string.updates_not_title))
                .setContentText(
                        String.format(context.getString(R.string.updates_not_desc), latestUpdate.getVersion())
                )
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setAutoCancel(true)
                .setContentIntent(
                        PendingIntent.getActivity(
                                context,
                                4,
                                intent,
                                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                        )
                );

        NotificationsHub.notifyRegardless(context, 0, notificationBuilder.build());

        return Result.success();
    }
}
