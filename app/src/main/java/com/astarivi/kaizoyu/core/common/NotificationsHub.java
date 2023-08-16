package com.astarivi.kaizoyu.core.common;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;

import com.astarivi.kaizoyu.KaizoyuApplication;
import com.astarivi.kaizoyu.R;

import org.tinylog.Logger;

import lombok.Getter;


public class NotificationsHub {
    public static void initialize() {
        Context context = KaizoyuApplication.getContext();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

            // App group

            notificationManager.createNotificationChannelGroup(
                    new NotificationChannelGroup(
                            Group.PLATFORM.getValue(),
                            context.getString(R.string.app_not_group)
                    )
            );

            // Updates
            NotificationChannel updatesChannel = new NotificationChannel(
                    Channel.APP_UPDATES.getValue(),
                    context.getString(R.string.updates_channel_title),
                    NotificationManager.IMPORTANCE_LOW
            );

            updatesChannel.setDescription(
                    context.getString(R.string.updates_channel_description)
            );

            updatesChannel.setGroup(Group.PLATFORM.getValue());

            notificationManager.createNotificationChannel(updatesChannel);

            // Reports
            NotificationChannel reportsChannel = new NotificationChannel(
                    Channel.APP_REPORTS.getValue(),
                    context.getString(R.string.updates_reports_title),
                    NotificationManager.IMPORTANCE_LOW
            );

            reportsChannel.setDescription(
                    context.getString(R.string.updates_reports_description)
            );

            reportsChannel.setGroup(Group.PLATFORM.getValue());

            notificationManager.createNotificationChannel(reportsChannel);

            // Content group
            notificationManager.createNotificationChannelGroup(
                    new NotificationChannelGroup(
                            Group.CONTENT.getValue(),
                            context.getString(R.string.shows_not_group)
                    )
            );

            // Episodes
            NotificationChannel episodesChannel = new NotificationChannel(
                    Channel.EPISODES.getValue(),
                    context.getString(R.string.updates_schedule_title),
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            episodesChannel.setDescription(
                    context.getString(R.string.updates_schedule_description)
            );

            episodesChannel.setGroup(Group.CONTENT.getValue());

            notificationManager.createNotificationChannel(episodesChannel);
        }
    }

    public static boolean canSendNotification(Channel channel) {
        NotificationManager notificationManager = KaizoyuApplication.getContext().getSystemService(NotificationManager.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = notificationManager.getNotificationChannel(channel.getValue());

            if (notificationChannel == null) return false;

            return notificationManager.areNotificationsEnabled() && notificationChannel.getImportance() != NotificationManager.IMPORTANCE_NONE;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return notificationManager.areNotificationsEnabled();
        }

        return true;
    }

    @SuppressLint("MissingPermission")
    public static void notifyRegardless(Context context, int id, Notification notification) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        try {
            notificationManager.notify(id, notification);
        } catch(Exception e) {
            Logger.error("Error sending notification id {}", id);
            Logger.error(e);

            AnalyticsClient.onError(
                    "notification_error",
                    "Couldn't send notification due to an error at NotificationsHub",
                    e
            );
        }
    }

    public enum Channel {
        APP_UPDATES("upts"),
        APP_REPORTS("rpts"),
        EPISODES("schl");

        @Getter
        private final String value;

        Channel(String id) {
            value = id;
        }

        @NonNull
        @Override
        public String toString() {
            return value;
        }
    }

    public enum Group {
        PLATFORM("g_app"),
        CONTENT("g_cnt");

        @Getter
        private final String value;

        Group(String id) {
            value = id;
        }

        @NonNull
        @Override
        public String toString() {
            return value;
        }
    }
}
