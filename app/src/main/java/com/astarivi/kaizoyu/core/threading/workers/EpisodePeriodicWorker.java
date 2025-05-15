package com.astarivi.kaizoyu.core.threading.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.astarivi.kaizoyu.core.common.NotificationsHub;
import com.astarivi.kaizoyu.core.models.anime.LocalAnime;

import lombok.AllArgsConstructor;
import lombok.Getter;


public class EpisodePeriodicWorker extends Worker {
    private final static NotificationsHub.Channel channel = NotificationsHub.Channel.EPISODES;

    public EpisodePeriodicWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    // FIXME: Enable this
    @NonNull
    @Override
    public Result doWork() {
//        if (NotificationsHub.areNotificationDisabled(channel))
//            return Result.success();
//
//        Future<List<SeenAnimeWithEpisodes>> future = Threading.forResult.fromTask(Threading.TASK.DATABASE, () ->
//                Data.getRepositories()
//                        .getSeenAnimeRepository()
//                        .getSeenAnimeDao()
//                        .getRelationWithRelationType(ModelType.LocalAnime.FAVORITE.getValue())
//        );
//
//        ArrayList<AiringSchedule.Episode> airingEpisodes;
//
//        try {
//            airingEpisodes = AniListSchedule.airingSchedule().episodes;
//            Objects.requireNonNull(airingEpisodes);
//        } catch (AniListException | IOException | NullPointerException e) {
//            future.cancel(false);
//            return Result.success();
//        }
//
//        List<SeenAnimeWithEpisodes> watchingRelations;
//
//        try {
//            watchingRelations = future.get();
//        } catch (ExecutionException | InterruptedException e) {
//            Logger.error("Interrupted while waiting for database I/O");
//            Logger.error(e);
//
//            return Result.failure();
//        }
//
//        if (watchingRelations == null || watchingRelations.isEmpty()) return Result.success();
//
//        HashMap<Integer, SeenAnimeWithEpisodes> watchingHash = new HashMap<>();
//
//        for (SeenAnimeWithEpisodes seenAnimeWithEpisodes : watchingRelations) {
//            watchingHash.put(
//                    seenAnimeWithEpisodes.anime.anime.kitsuId,
//                    seenAnimeWithEpisodes
//            );
//        }
//
//        ArrayList<NewEpisodeNotification> notifications = new ArrayList<>();
//        ArrayList<Integer> episodeIds = new ArrayList<>();
//
//        for (AiringSchedule.Episode airingEpisode : airingEpisodes) {
//            if (airingEpisode.episode == 0 || airingEpisode.episode == -1) continue;
//
//            SeenAnimeWithEpisodes animeUserIsWatching = watchingHash.get(
//                    Math.toIntExact(airingEpisode.media.id)
//            );
//
//            if (animeUserIsWatching == null) continue;
//
//            int lastSeen = 0;
//            SeenEpisode lastEpisode = null;
//
//            for (SeenEpisode seenEpisode : animeUserIsWatching.episodes) {
//                if (seenEpisode.episode.episodeNumber > lastSeen) {
//                    lastSeen = seenEpisode.episode.episodeNumber;
//                    lastEpisode = seenEpisode;
//                }
//            }
//
//            if (lastSeen == 0 || lastEpisode.notified || lastSeen + 1 != airingEpisode.episode) continue;
//
//            episodeIds.add(lastEpisode.id);
//
//            final LocalAnime localAnime = animeUserIsWatching.anime.toLocalAnime(ModelType.LocalAnime.FAVORITE);
//
//            notifications.add(
//                    new NewEpisodeNotification(
//                            localAnime,
//                            airingEpisode.episode
//                    )
//            );
//        }
//        Context context = getApplicationContext();
//
//        if (notifications.isEmpty()) return Result.success();
//
//        try {
//            Threading.submitTask(Threading.TASK.DATABASE, () ->
//                Data.getRepositories()
//                        .getSeenAnimeRepository()
//                        .getSeenEpisodeDao()
//                        .setNotified(true, episodeIds)
//            ).get();
//        } catch (ExecutionException | InterruptedException e) {
//            Logger.error("Interrupted while updating database");
//            Logger.error(e);
//
//            return Result.failure();
//        }
//
//        // Single notification
//        if (notifications.size() == 1) {
//            NewEpisodeNotification notification = notifications.get(0);
//
//            String title = String.format(
//                    context.getString(R.string.ne_not_title),
//                    notification.getEpisodeNumber(),
//                    notification.getAnime().getDisplayTitle()
//            );
//
//            String description;
//            if (title.length() > 65) {
//                title = context.getString(R.string.ne_not_title_long);
//                description = String.format(
//                        context.getString(R.string.ne_not_title_long_desc),
//                        notification.getEpisodeNumber(),
//                        notification.getAnime().getDisplayTitle()
//                )
//                        + context.getString(R.string.ne_not_title_long_end_s);
//            } else {
//                description = context.getString(R.string.ne_not_title_desc);
//            }
//
//            Intent intent = new Intent();
//            intent.setClassName(BuildConfig.APPLICATION_ID, AnimeDetailsActivity.class.getName());
//            intent.putExtra("anime", notification.getAnime());
//            intent.putExtra("type", ModelType.Anime.LOCAL.name());
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//
//            NotificationsHub.notifyRegardless(
//                    context,
//                    1,
//                    new NotificationCompat.Builder(context, channel.getValue())
//                            .setSmallIcon(R.drawable.ic_stat_name)
//                            .setContentTitle(title)
//                            .setContentText(description)
//                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//                            .setAutoCancel(true)
//                            .setContentIntent(
//                                    PendingIntent.getActivity(
//                                            context,
//                                            5,
//                                            intent,
//                                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
//                                    )
//                            ).build()
//            );
//
//            return Result.success();
//        }
//
//        // Multiple notifications
//        StringBuilder description = new StringBuilder();
//
//        for (NewEpisodeNotification notification : notifications) {
//            description.append(
//                    String.format(
//                            context.getString(R.string.ne_not_title_long_desc),
//                            notification.getEpisodeNumber(),
//                            notification.getAnime().getDisplayTitle()
//                    )
//            );
//        }
//
//        description.append(context.getString(R.string.ne_not_title_long_end));
//
//        Intent intent = new Intent();
//        intent.setClassName(BuildConfig.APPLICATION_ID, SharedLibraryActivity.class.getName());
//        intent.putExtra("local_type", ModelType.LocalAnime.FAVORITE.name());
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//
//        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context);
//        taskStackBuilder.addNextIntentWithParentStack(intent);
//
//        NotificationsHub.notifyRegardless(
//                context,
//                1,
//                new NotificationCompat.Builder(context, channel.getValue())
//                        .setSmallIcon(R.drawable.ic_stat_name)
//                        .setContentTitle(context.getString(R.string.ne_not_title_mult))
//                        .setContentText(String.format(
//                                context.getString(R.string.ne_not_title_mult_sub),
//                                notifications.size()
//                        ))
//                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//                        .setAutoCancel(true)
//                        .setStyle(new NotificationCompat.BigTextStyle()
//                                .bigText(description))
//                        .setContentIntent(
//                                taskStackBuilder.getPendingIntent(
//                                        10,
//                                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
//                                )
//                        ).build()
//        );

        return Result.success();
    }

    @AllArgsConstructor
    @Getter
    public static class NewEpisodeNotification {
        private LocalAnime anime;
        private int episodeNumber;
    }
}
