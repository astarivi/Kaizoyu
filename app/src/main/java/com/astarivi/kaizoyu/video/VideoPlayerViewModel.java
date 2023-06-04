package com.astarivi.kaizoyu.video;

import androidx.lifecycle.ViewModel;

import com.astarivi.kaizolib.irc.IrcManager;
import com.astarivi.kaizolib.irc.client.BaseIrcClient;
import com.astarivi.kaizolib.xdcc.XDCCDownloader;
import com.astarivi.kaizolib.xdcc.base.XDCCDownloadListener;
import com.astarivi.kaizolib.xdcc.model.DCC;
import com.astarivi.kaizoyu.utils.Data;
import com.astarivi.kaizoyu.utils.Threading;

import java.io.File;
import java.util.concurrent.Future;


public class VideoPlayerViewModel extends ViewModel {
    private Future ircFuture;
    private Future dccFuture;
    private BaseIrcClient.IrcOnFailureListener onFailureListener;
    private XDCCDownloadListener dccListener;
    private File cachePath;
    private File downloadFile;
    private IrcManager irc;
    private XDCCDownloader download;
    private boolean hasStarted = false;

    public void initialize(String videoCommand, File cachePath){
        final String username = Data.getProperties(
                Data.CONFIGURATION.APP
        ).getProperty(
                "ircName"
        );

        this.irc = new IrcManager(videoCommand, username, true, false);
        this.cachePath = cachePath;
    }

    public void setIrcOnFailureListener(BaseIrcClient.IrcOnFailureListener onFailureListener){
        this.onFailureListener = onFailureListener;
        if (irc != null){
            irc.setIrcOnFailureListener(onFailureListener);
        }
    }

    public void setDccDownloadListener(XDCCDownloadListener dccListener){
        this.dccListener = dccListener;
        if (download != null){
            download.setXDCCDownloadListener(dccListener);
        }
    }

    public boolean hasStarted(){
        return hasStarted;
    }

    public void start(){
        if (hasStarted) return;
        irc.setIrcOnFailureListener(onFailureListener);
        irc.setIrcOnSuccessListener(this::startDownload);
        hasStarted = true;
        ircFuture = Threading.submitTask(Threading.TASK.INSTANT, irc::execute);
    }

    @Override
    public void onCleared(){
        stop();
        super.onCleared();
    }

    public void stop(){
        onFailureListener = null;
        dccListener = null;

        if (download != null){
            download.stop();
        }

        if (ircFuture != null) {
            ircFuture.cancel(true);
        }

        if (dccFuture != null) {
            dccFuture.cancel(true);
        }

        if (downloadFile != null && downloadFile.isFile()){
            // No need for inspection here, if the file cannot be deleted, it will be deleted later.
            // TODO: Maybe add path to a list to handle later
            //noinspection ResultOfMethodCallIgnored
            downloadFile.delete();
        }
    }

    private void startDownload(DCC dcc){
        downloadFile = new File(cachePath, dcc.getFilename());
        download = new XDCCDownloader(dcc, downloadFile, 20, 10);
        download.setXDCCDownloadListener(dccListener);
        dccFuture = Threading.submitTask(Threading.TASK.INSTANT, download::start);
    }
}
