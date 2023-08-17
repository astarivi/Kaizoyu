package com.astarivi.kaizoyu.core.threading.workers;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.astarivi.kaizoyu.BuildConfig;
import com.astarivi.kaizoyu.R;
import com.astarivi.kaizoyu.core.common.NotificationsHub;
import com.astarivi.kaizoyu.core.models.base.ModelType;
import com.astarivi.kaizoyu.core.models.local.LocalAnime;
import com.astarivi.kaizoyu.core.schedule.AssistedScheduleFetcher;
import com.astarivi.kaizoyu.core.storage.database.data.seen.SeenAnimeWithEpisodes;
import com.astarivi.kaizoyu.core.storage.database.data.seen.SeenEpisode;
import com.astarivi.kaizoyu.details.AnimeDetailsActivity;
import com.astarivi.kaizoyu.gui.library.watching.SharedLibraryActivity;
import com.astarivi.kaizoyu.utils.Data;
import com.astarivi.kaizoyu.utils.Threading;

import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import lombok.AllArgsConstructor;
import lombok.Getter;


public class EpisodePeriodicWorker extends Worker {
    private final static NotificationsHub.Channel channel = NotificationsHub.Channel.EPISODES;

    public EpisodePeriodicWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        if (NotificationsHub.areNotificationDisabled(channel))
            return Result.success();

        Future<List<SeenAnimeWithEpisodes>> future = Threading.forResult.fromTask(Threading.TASK.DATABASE, () ->
                Data.getRepositories()
                        .getSeenAnimeRepository()
                        .getSeenAnimeDao()
                        .getRelationWithRelationType(ModelType.LocalAnime.FAVORITE.getValue())
        );

        AssistedScheduleFetcher.ScheduledAnime[] scheduledAnime = AssistedScheduleFetcher.getScheduledAnime();

        if (scheduledAnime == null) {
            future.cancel(false);
            return Result.success();
        }

        List<SeenAnimeWithEpisodes> watchingRelations;

        try {
            watchingRelations = future.get();
        } catch (ExecutionException | InterruptedException e) {
            Logger.error("Interrupted while waiting for database I/O");
            Logger.error(e);

            return Result.failure();
        }

        if (watchingRelations == null || watchingRelations.isEmpty()) return Result.success();

        HashMap<Integer, SeenAnimeWithEpisodes> watchingHash = new HashMap<>();

        for (SeenAnimeWithEpisodes seenAnimeWithEpisodes : watchingRelations) {
            watchingHash.put(
                    seenAnimeWithEpisodes.anime.anime.kitsuId,
                    seenAnimeWithEpisodes
            );
        }

        ArrayList<NewEpisodeNotification> notifications = new ArrayList<>();

        for (AssistedScheduleFetcher.ScheduledAnime anime : scheduledAnime) {
            if (anime.episode == 0 || anime.episode == -1) continue;

            SeenAnimeWithEpisodes animeUserIsWatching = watchingHash.get(anime.id);

            if (animeUserIsWatching == null) continue;

            int lastSeen = 0;

            for (SeenEpisode seenEpisode : animeUserIsWatching.episodes) {
                if (seenEpisode.episode.episodeNumber > lastSeen) lastSeen = seenEpisode.episode.episodeNumber;
            }

            if (lastSeen == 0 || lastSeen + 1 != anime.episode) continue;

            final LocalAnime localAnime = animeUserIsWatching.anime.toLocalAnime(ModelType.LocalAnime.FAVORITE);

            // TODO: Mark the episode as already notified, or look for another workaround

            notifications.add(
                    new NewEpisodeNotification(
                            localAnime,
                            anime.episode
                    )
            );
        }

        Context context = getApplicationContext();

        if (notifications.isEmpty()) return Result.success();

        // Single notification
        if (notifications.size() == 1) {
            NewEpisodeNotification notification = notifications.get(0);

            String title = String.format(
                    context.getString(R.string.ne_not_title),
                    notification.getEpisodeNumber(),
                    notification.getAnime().getDisplayTitle()
            );

            String description;
            if (title.length() > 65) {
                title = context.getString(R.string.ne_not_title_long);
                description = String.format(
                        context.getString(R.string.ne_not_title_long_desc),
                        notification.getEpisodeNumber(),
                        notification.getAnime().getDisplayTitle()
                )
                        + context.getString(R.string.ne_not_title_long_end_s);
            } else {
                description = context.getString(R.string.ne_not_title_desc);
            }

            Intent intent = new Intent();
            intent.setClassName(BuildConfig.APPLICATION_ID, AnimeDetailsActivity.class.getName());
            intent.putExtra("anime", notification.getAnime());
            intent.putExtra("type", ModelType.Anime.LOCAL.name());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            NotificationsHub.notifyRegardless(
                    context,
                    1,
                    new NotificationCompat.Builder(context, channel.getValue())
                            .setSmallIcon(R.drawable.ic_stat_name)
                            .setContentTitle(title)
                            .setContentText(description)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setContentIntent(
                                    PendingIntent.getActivity(
                                            context,
                                            5,
                                            intent,
                                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                                    )
                            ).build()
            );

            return Result.success();
        }

        // Multiple notifications
        StringBuilder description = new StringBuilder();

        for (NewEpisodeNotification notification : notifications) {
            description.append(
                    String.format(
                            context.getString(R.string.ne_not_title_long_desc),
                            notification.getEpisodeNumber(),
                            notification.getAnime().getDisplayTitle()
                    )
            );
        }

        description.append(context.getString(R.string.ne_not_title_long_end));

        Intent intent = new Intent();
        intent.setClassName(BuildConfig.APPLICATION_ID, SharedLibraryActivity.class.getName());
        intent.putExtra("local_type", ModelType.LocalAnime.FAVORITE.getValue());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context);
        taskStackBuilder.addNextIntentWithParentStack(intent);

        NotificationsHub.notifyRegardless(
                context,
                1,
                new NotificationCompat.Builder(context, channel.getValue())
                        .setSmallIcon(R.drawable.ic_stat_name)
                        .setContentTitle(context.getString(R.string.ne_not_title_mult))
                        .setContentText(String.format(
                                context.getString(R.string.ne_not_title_mult_sub),
                                notifications.size()
                        ))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(description))
                        .setContentIntent(
                                taskStackBuilder.getPendingIntent(
                                        0,
                                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                                )
                        ).build()
        );

        return Result.success();
    }

    @AllArgsConstructor
    @Getter
    public static class NewEpisodeNotification {
        private LocalAnime anime;
        private int episodeNumber;
    }
}
