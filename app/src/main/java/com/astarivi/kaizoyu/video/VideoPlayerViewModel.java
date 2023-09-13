package com.astarivi.kaizoyu.video;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.astarivi.kaizolib.irc.IrcClient;
import com.astarivi.kaizolib.irc.exception.IrcExceptionManager;
import com.astarivi.kaizolib.xdcc.XDCCDownloader;
import com.astarivi.kaizolib.xdcc.base.XDCCDownloadListener;
import com.astarivi.kaizolib.xdcc.base.XDCCFailure;
import com.astarivi.kaizolib.xdcc.model.DCC;
import com.astarivi.kaizoyu.core.common.AnalyticsClient;
import com.astarivi.kaizoyu.core.models.Result;
import com.astarivi.kaizoyu.core.storage.properties.ExtendedProperties;
import com.astarivi.kaizoyu.utils.Data;
import com.astarivi.kaizoyu.utils.Threading;

import org.tinylog.Logger;

import java.io.File;
import java.util.concurrent.Future;

import lombok.AccessLevel;
import lombok.Getter;


@Getter
public class VideoPlayerViewModel extends ViewModel {
    private final MutableLiveData<File> downloadFile = new MutableLiveData<>();
    private final MutableLiveData<Pair<Integer, String>> progress = new MutableLiveData<>();
    private final MutableLiveData<IrcExceptionManager.FailureCode> ircFailure = new MutableLiveData<>();
    private final MutableLiveData<XDCCFailure> xdccFailure = new MutableLiveData<>();
    @Getter(AccessLevel.NONE)
    private Future<?> downloadFuture;
    @Getter(AccessLevel.NONE)
    private boolean hasStartedDownload = false;

    public void startDownload(Context context, Result result) {
        if (hasStartedDownload) return;
        Logger.info("About to start download of following file:");
        Logger.info(result);
        downloadFuture = Threading.submitTask(Threading.TASK.INSTANT, () -> {
            ExtendedProperties appProperties = Data.getProperties(Data.CONFIGURATION.APP);

            final boolean alwaysTls = appProperties.getBooleanProperty(
                    "strict_mode",
                    false
            );

            final String username = appProperties.getProperty(
                    "ircName"
            );

            Logger.info("Download properties set.");

            IrcClient irc = new IrcClient(result.getXDCCCommand(), username, true, alwaysTls);

            Logger.info("IrcClient created.");
            hasStartedDownload = true;

            DCC dcc;
            try {
                Logger.info("Executing DCC.");
                dcc = irc.execute();
            } catch (Exception e) {
                IrcExceptionManager.FailureCode failureCode = IrcExceptionManager.getFailureCode(e);
                AnalyticsClient.onError("handshake_error", failureCode.name(), e);
                ircFailure.postValue(failureCode);

                Logger.error("IRC Failure.");
                Logger.error(e);
                return;
            }

            Logger.info("Executed DCC.");

            if (context instanceof Activity) {
                Activity activity = (Activity) context;
                if (activity.isFinishing() || activity.isDestroyed()) {
                    Logger.error("Parent activity has died, falling back...");
                    return;
                }
            }

            Logger.info("Starting XDCC download.");

            XDCCDownloader xdccDownloader = new XDCCDownloader(
                    dcc,
                    new File(context.getCacheDir(), dcc.getFilename()),
                    20,
                    10
            );

            boolean useXdcc = appProperties.getBooleanProperty("use_xdcc", false);

            Logger.info("User has set 'use XDCC' to {}", useXdcc);

            xdccDownloader.setXDCC(
                    useXdcc
            );

            xdccDownloader.setXDCCDownloadListener(new XDCCDownloadListener() {
                @Override
                public void onDownloadReadyToPlay(int i, File file) {
                    downloadFile.postValue(file);
                }

                @Override
                public void onProgress(int currentProgress, String speed) {
                    progress.postValue(new Pair<>(currentProgress, speed));
                }

                @Override
                public void onFinished(File file) {
                    progress.postValue(new Pair<>(100, ""));
                }

                @Override
                public void onError(XDCCFailure failure) {
                    xdccFailure.postValue(failure);
                }
            });

            Logger.info("Starting download.");
            xdccDownloader.start();
        });
    }

    @Override
    public void onCleared(){
        destroy();
        super.onCleared();
    }

    public void destroy(){
        Logger.info("Destroying ViewModel from player.");
        if (downloadFuture != null) {
            downloadFuture.cancel(true);
        }

        if (progress.getValue() != null) {
            progress.setValue(null);
        }

        if (ircFailure.getValue() != null) {
            ircFailure.setValue(null);
        }

        if (xdccFailure.getValue() != null) {
            xdccFailure.setValue(null);
        }

        if (downloadFile.getValue() != null && downloadFile.getValue().isFile()){
            File file = downloadFile.getValue();
            downloadFile.setValue(null);

            if (!file.delete()) {
                final Handler handler = new Handler(Looper.getMainLooper());
                // No need for inspection here, if the file cannot be deleted, it will be deleted later, or even later.
                handler.postDelayed(() -> {
                    try {
                        if (file.exists()) //noinspection ResultOfMethodCallIgnored
                            file.delete();
                    } catch(Exception error) {
                        AnalyticsClient.onError(
                                "failed_to_delete",
                                "Failed to delete file after playback ended, even after 5s delay",
                                error
                        );
                    }

                }, 5000);
            }
        }
    }
}
