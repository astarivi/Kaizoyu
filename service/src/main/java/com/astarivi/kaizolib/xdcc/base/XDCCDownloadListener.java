package com.astarivi.kaizolib.xdcc.base;

import java.io.File;


public interface XDCCDownloadListener {
    void onDownloadReadyToPlay(int progress, File downloadFile);

    void onProgress(int progress, String speed);

    void onFinished(File downloadFile);

    void onError(XDCCFailure failureCode);
}
