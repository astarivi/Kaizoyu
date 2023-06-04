package com.astarivi.kaizoyu.core.updater;

import com.astarivi.kaizolib.common.network.UserHttpClient;
import com.astarivi.kaizoyu.core.annotations.ThreadedOnly;
import com.astarivi.kaizoyu.utils.Data;

import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class UpdateDownloader {
    private final UserHttpClient httpClient = Data.getUserHttpClient();
    private final UpdateManager.LatestUpdate update;
    private final DownloadStatusListener listener;
    private final File downloadFile;

    public UpdateDownloader(UpdateManager.LatestUpdate u,
                            File destination,
                            DownloadStatusListener listener) {
        update = u;
        this.listener = listener;
        downloadFile = destination;
    }

    @ThreadedOnly
    public void download() {
        Request request = new Request.Builder()
                .url(update.downloadUrl)
                .build();

        try (Response response = httpClient.executeRequest(request)) {
            if (!response.isSuccessful()) {
                listener.onError(DownloadErrorCodes.BadResponse);
                return;
            }

            ResponseBody responseBody = response.body();

            if (responseBody == null) {
                listener.onError(DownloadErrorCodes.EmptyBody);
                return;
            }

            writeFile(response);
        } catch (IOException e) {
            Logger.error("Failed to download update from %s", update.downloadUrl);
            listener.onError(DownloadErrorCodes.IOException);
        } catch (NullPointerException e) {
            Logger.error("Download size unknown");
            listener.onError(DownloadErrorCodes.SizeUnknown);
        }
    }

    private void writeFile(@NotNull Response response) throws IOException, NullPointerException {
        long totalLength = Long.parseLong(
                Objects.requireNonNull(
                        response.header("content-length", "1")
                )
        );

        // No need for this inspection. Older android versions don't support the inspection
        // suggested method.
        //noinspection IOStreamConstructor
        try (
                BufferedInputStream input = new BufferedInputStream(Objects.requireNonNull(response.body()).byteStream());
                OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(downloadFile))
        ) {
            byte[] buffer = new byte[1024];
            int readBytes;
            long totalBytes = 0;
            int repetitions = 0;

            while ((readBytes = input.read(buffer)) != -1) {
                repetitions++;
                totalBytes += readBytes;
                outputStream.write(buffer, 0, readBytes);

                if (repetitions >= 200) {
                    repetitions = 0;
                    listener.onProgress((int) (totalBytes * 100 / totalLength));
                }
            }

            outputStream.close();
            listener.onFinish(downloadFile);
        }
    }

    public enum DownloadErrorCodes {
        IOException,
        BadResponse,
        EmptyBody,
        SizeUnknown
    }

    public interface DownloadStatusListener {
        void onProgress(int percentage);
        void onError(DownloadErrorCodes code);
        void onFinish(File file);
    }
}
