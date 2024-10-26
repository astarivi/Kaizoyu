package com.astarivi.kaizolib.xdcc;

import com.astarivi.kaizolib.xdcc.base.TimeoutException;
import com.astarivi.kaizolib.xdcc.base.XDCCDownloadListener;
import com.astarivi.kaizolib.xdcc.base.XDCCFailure;
import com.astarivi.kaizolib.xdcc.model.DCC;

import org.tinylog.Logger;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;


public class XDCCDownloader {
    private final DCC dcc;
    private final File downloadFile;
    private final int timeout;
    private final int startThreshold;
    private boolean stop = false;
    private int progress = 0;
    private int speedKBps = 0;
    private long lastTimeReminderRan = 0;
    private XDCCDownloadListener downloadEventListener;
    private boolean isXDCC = false;

    // Uses default timeout of 20 seconds
    public XDCCDownloader(DCC dcc, File downloadFile) {
        this.dcc = dcc;
        this.downloadFile = downloadFile;
        this.timeout = 20;
        this.startThreshold = 10;
    }

    public XDCCDownloader(DCC dcc, File downloadFile, int timeoutSeconds, int startThreshold) {
        this.dcc = dcc;
        this.downloadFile = downloadFile;
        this.timeout = timeoutSeconds;
        this.startThreshold = Math.max(1, Math.min(100, startThreshold));
    }

    public void setXDCCDownloadListener(XDCCDownloadListener listener){
        downloadEventListener = listener;
    }

    public void setXDCC(boolean value) {
        isXDCC = value;
    }

    @SuppressWarnings("unused")
    public void stop() {
        this.stop = true;
    }

    @SuppressWarnings("unused")
    public int getProgress() {
        return progress;
    }

    public String getSpeed() {
        if (speedKBps > 1024) {
            return String.format(java.util.Locale.US,"%.2f", speedKBps / 1024.0) + " Mbps";
        } else {
            return speedKBps + " Kbps";
        }
    }

    public void start() {
        Logger.debug("Starting XDCC download of:");
        Logger.info(dcc.toString());

        InetAddress address;
        try {
            address = InetAddress.getByName(dcc.ip());
        } catch (UnknownHostException e) {
            if (downloadEventListener != null) downloadEventListener.onError(
                    XDCCFailure.UnknownHost
            );
            return;
        }

        //noinspection IOStreamConstructor
        try(
                Socket socket = new Socket(address, dcc.port());
                DataInputStream inputStream = new DataInputStream(
                        socket.getInputStream()
                );
                OutputStream output = socket.getOutputStream();
                DataOutputStream fileOutput = new DataOutputStream(
                        new BufferedOutputStream(
                                new FileOutputStream(downloadFile)
                        )
                )
        ){
            Logger.info("Automated Buffer size: " + socket.getReceiveBufferSize());
            Logger.info("Actual Buffer size: 8192 (Hardcoded)");
            socket.setTcpNoDelay(true);

            byte[] buffer = new byte[8192];

            boolean hasFiredPlay = false;

            int repetitions = 0;
            long downloadedLength = 0;
            long fileLength = dcc.sizeBits();
            long downloadStartTime = System.currentTimeMillis();

            Logger.debug("About to start download loop. Downloading from: " + dcc.ip());

            // Signal start
            write(output, 0);

            while (downloadedLength < fileLength && !stop) {
                if (Thread.interrupted()) {
                    Logger.info("XDCC download thread interrupted");
                    return;
                }

                // Has the download not even started?
                if (downloadedLength == 0 && inputStream.available() == 0) {
                    // Tell the server that we're waiting for data.
                    checkTimeout(downloadStartTime);
                    contactServer(output);
                    continue;
                }

                int read = inputStream.read(buffer);

                // Why is it telling us that we already downloaded the whole file?
                if (downloadedLength == 0 && read == -1) {
                    // Tell the server we don't have shit yet
                    checkTimeout(downloadStartTime);
                    contactServer(output);
                    continue;
                }

                if (read == -1) {
                    break;
                }

                if (read == 0) {
                    continue;
                }

                downloadedLength += read;

                fileOutput.write(buffer, 0, read);
                // For old DCC compatibility
                write(output, (int) downloadedLength);

                //Update progress every 400 repetitions to avoid calculating the progress so often.
                if (repetitions >= 400) {
                    long downloadElapsedTime = TimeUnit.MILLISECONDS.toSeconds(
                            (System.currentTimeMillis() - downloadStartTime)
                    );

                    // Come back here if this dumb patch causes download to hang at 0% lmao
                    if (downloadElapsedTime == 0) {
                        repetitions += 1;
                        continue;
                    }

                    progress = (int) (downloadedLength * 100 / fileLength);
                    speedKBps = (int) ((downloadedLength / downloadElapsedTime) / 1024);

                    if (downloadEventListener != null) downloadEventListener.onProgress(progress, getSpeed());

                    if (!hasFiredPlay && progress > startThreshold  && downloadEventListener != null){
                        hasFiredPlay = true;
                        downloadEventListener.onDownloadReadyToPlay(progress, downloadFile);
                    }

                    Logger.debug("File Received " + progress + "% at " + speedKBps);
                    repetitions = 0;
                }

                repetitions += 1;
            }

            progress = 100;
            Logger.debug("Receive completed, file saved as " + downloadFile.getPath());

            if (!stop && downloadEventListener != null) downloadEventListener.onFinished(downloadFile);
        } catch (SocketException e) {
            // Temporary fix for dumb old DCC behavior
            if (progress == 99) {
                progress = 100;
                if (!stop && downloadEventListener != null) downloadEventListener.onFinished(downloadFile);
                return;
            }

            if (downloadEventListener != null) downloadEventListener.onError(
                    XDCCFailure.ConnectionLost
            );
        } catch (TimeoutException e){
            if (downloadEventListener != null) downloadEventListener.onError(
                    XDCCFailure.TimedOut
            );
        } catch (IOException e) {
            if (downloadEventListener != null) downloadEventListener.onError(
                    XDCCFailure.IOError
            );
        }
    }

    private void checkTimeout(long downloadStartTime) throws TimeoutException {
        long downloadElapsedTime = TimeUnit.MILLISECONDS.toSeconds(
                (System.currentTimeMillis() - downloadStartTime)
        );

        if (downloadElapsedTime >= timeout) {
            Logger.debug("Timed out waiting for XDCC download.");
            throw new TimeoutException();
        }
    }

    // Dumb code
    private void contactServer(OutputStream output) throws IOException {
        long timeNow = System.currentTimeMillis();

        // First time we run
        if (lastTimeReminderRan == 0) {
            lastTimeReminderRan = timeNow;
            write(output, 0);
            Logger.debug("Telling server that we're waiting.");
            return;
        }

        if ((timeNow - lastTimeReminderRan) < 200) {
            return;
        }

        lastTimeReminderRan = timeNow;
        write(output, 0);
        Logger.debug("Telling server that we're waiting.");
    }

    private void write(OutputStream stream, int value) throws IOException {
        if (isXDCC) return;

        stream.write(value);
    }
}
