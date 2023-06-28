package com.astarivi.kaizoyu.video;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.astarivi.kaizolib.irc.IrcManager;
import com.astarivi.kaizolib.irc.client.BaseIrcClient;
import com.astarivi.kaizolib.xdcc.XDCCDownloader;
import com.astarivi.kaizolib.xdcc.base.XDCCDownloadListener;
import com.astarivi.kaizolib.xdcc.base.XDCCFailure;
import com.astarivi.kaizolib.xdcc.model.DCC;
import com.astarivi.kaizoyu.core.analytics.AnalyticsClient;
import com.astarivi.kaizoyu.core.models.Result;
import com.astarivi.kaizoyu.core.storage.properties.ExtendedProperties;
import com.astarivi.kaizoyu.utils.Data;
import com.astarivi.kaizoyu.utils.Threading;

import java.io.File;
import java.util.concurrent.Future;


public class VideoPlayerViewModel extends ViewModel {
    private final MutableLiveData<File> downloadFile = new MutableLiveData<>();
    private final MutableLiveData<Pair<Integer, String>> progress = new MutableLiveData<>();
    private final MutableLiveData<BaseIrcClient.FailureCode> ircFailure = new MutableLiveData<>();
    private final MutableLiveData<XDCCFailure> xdccFailure = new MutableLiveData<>();
    private Future<?> downloadFuture;
    private boolean hasStartedDownload = false;

    public MutableLiveData<File> getDownloadFile() {
        return downloadFile;
    }

    public MutableLiveData<Pair<Integer, String>> getProgress() {
        return progress;
    }

    public MutableLiveData<BaseIrcClient.FailureCode> getIrcFailure() {
        return ircFailure;
    }

    public MutableLiveData<XDCCFailure> getXdccFailure() {
        return xdccFailure;
    }

    public void startDownload(Context context, Result result) {
        if (hasStartedDownload) return;

        downloadFuture = Threading.submitTask(Threading.TASK.INSTANT, () -> {
            ExtendedProperties appProperties = Data.getProperties(Data.CONFIGURATION.APP);

            final boolean alwaysTls = appProperties.getBooleanProperty(
                    "strict_mode",
                    false
            );

            final String username = appProperties.getProperty(
                    "ircName"
            );

            IrcManager irc = new IrcManager(result.getXDCCCommand(), username, true, alwaysTls);

            irc.setIrcOnFailureListener(ircFailure::postValue);
            hasStartedDownload = true;
            DCC dcc = irc.execute();

            if (context instanceof Activity) {
                Activity activity = (Activity) context;
                if (activity.isFinishing() || activity.isDestroyed()) {
                    return;
                }
            }

            XDCCDownloader xdccDownloader = new XDCCDownloader(
                    dcc,
                    new File(context.getCacheDir(), dcc.getFilename()),
                    20,
                    10
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

            xdccDownloader.start();
        });
    }

    @Override
    public void onCleared(){
        destroy();
        super.onCleared();
    }

    public void destroy(){
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
