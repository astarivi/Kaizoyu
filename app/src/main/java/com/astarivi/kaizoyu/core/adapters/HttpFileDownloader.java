package com.astarivi.kaizoyu.core.adapters;

import com.astarivi.kaizolib.kitsu.exception.NetworkConnectionException;
import com.astarivi.kaizoyu.core.common.ThreadedOnly;
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

import lombok.Setter;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class HttpFileDownloader {
    private final String url;
    private final File downloadFile;
    @Setter
    private ProgressListener listener;

    public HttpFileDownloader(String url, File destination) {
        this.url = url;
        downloadFile = destination;
    }

    @ThreadedOnly
    public File download() throws IOException, NetworkConnectionException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = Data.getUserHttpClient().executeRequest(request)) {
            if (!response.isSuccessful()) {
                throw new NetworkConnectionException("Network error");
            }

            ResponseBody responseBody = response.body();

            if (responseBody == null) {
                Logger.error("Empty body for request {}", url);
                throw new IllegalStateException("Body was empty");
            }

            return writeFile(response);
        } catch (IOException e) {
            Logger.error("Failed to download file from {}", url);
            Logger.error(e);
            throw new IOException(e);
        }
    }

    private File writeFile(@NotNull Response response) throws IOException {
        long totalLength;
        try {
            totalLength = Long.parseLong(
                    Objects.requireNonNull(
                            response.header("content-length", "1")
                    )
            );
        } catch(NullPointerException e) {
            Logger.error("Unknown download size for URL {}", url);
            throw new IllegalStateException("Download size is unknown");
        }

        // No need for this inspection. Older android versions don't support the inspection
        // suggested method.
        //noinspection IOStreamConstructor
        try (
                BufferedInputStream input = new BufferedInputStream(
                        Objects.requireNonNull(response.body()).byteStream()
                );
                OutputStream outputStream = new BufferedOutputStream(
                        new FileOutputStream(downloadFile)
                )
        ) {
            byte[] buffer = new byte[1024];
            int readBytes;
            long totalBytes = 0;
            int repetitions = 0;

            while ((readBytes = input.read(buffer)) != -1) {
                repetitions++;
                totalBytes += readBytes;
                outputStream.write(buffer, 0, readBytes);

                if (repetitions >= 200 && listener != null) {
                    repetitions = 0;
                    listener.onProgress((int) (totalBytes * 100 / totalLength));
                }
            }

            return downloadFile;
        }
    }

    public interface ProgressListener {
        void onProgress(int progressPercentage);
    }
}
