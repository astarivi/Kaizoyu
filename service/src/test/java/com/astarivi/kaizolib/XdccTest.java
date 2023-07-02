package com.astarivi.kaizolib;

import com.astarivi.kaizolib.irc.IrcClient;
import com.astarivi.kaizolib.irc.utils.Utils;
import com.astarivi.kaizolib.xdcc.XDCCDownloader;
import com.astarivi.kaizolib.xdcc.base.XDCCDownloadListener;
import com.astarivi.kaizolib.xdcc.base.XDCCFailure;
import com.astarivi.kaizolib.xdcc.model.DCC;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;


public class XdccTest {
    @Test
    @DisplayName("T-XDCC IPv4 Download")
    void testIpv4Download() throws Exception {
        final IrcClient ircManager = new IrcClient(
                "Ginpachi-Sensei :xdcc send #3199",
                Utils.shuffle("KaizoLibTesting"), // Shuffle to support multiple people testing package
                true,
                false
        );

        final DCC dcc = ircManager.execute();
        assertNotNull(dcc);

        XDCCDownloadListener listener = new XDCCDownloadListener() {
            @Override
            public void onDownloadReadyToPlay(int progress, File downloadFile) {

            }

            @Override
            public void onProgress(int progress, String speed) {

            }

            @Override
            public void onFinished(File downloadFile) {

            }

            @Override
            public void onError(XDCCFailure failureCode) {
                fail(failureCode.name());
            }
        };

        File tempFile = Files.createTempFile("kaizo", null).toFile();

        XDCCDownloader downloader = new XDCCDownloader(
                dcc,
                Files.createTempFile("kaizo", null).toFile()
        );

        downloader.setXDCCDownloadListener(listener);
        downloader.start();

        deleteFile(tempFile);
    }

    @Test
    @DisplayName("T-XDCC IPv4 Ginpachi-Sensei Bot Download Non-TLS")
    void testGinpachiDownload() throws Exception {
        final IrcClient ircManager = new IrcClient(
                "Ginpachi-Sensei :xdcc send #3199",
                Utils.shuffle("KaizoLibTesting"), // Shuffle to support multiple people testing package
                false,
                false
        );

        final DCC dcc = ircManager.execute();
        assertNotNull(dcc);

        XDCCDownloadListener listener = new XDCCDownloadListener() {
            @Override
            public void onDownloadReadyToPlay(int progress, File downloadFile) {

            }

            @Override
            public void onProgress(int progress, String speed) {

            }

            @Override
            public void onFinished(File downloadFile) {

            }

            @Override
            public void onError(XDCCFailure failureCode) {
                fail(failureCode.name());
            }
        };

        File tempFile = Files.createTempFile("kaizo", null).toFile();

        XDCCDownloader downloader = new XDCCDownloader(
                dcc,
                Files.createTempFile("kaizo", null).toFile()
        );

        downloader.setXDCCDownloadListener(listener);
        downloader.start();

        deleteFile(tempFile);
    }

    @Test
    @DisplayName("T-XDCC IPv6 Download")
    void testIpv6Download() throws Exception {
        final IrcClient ircManager = new IrcClient(
                "CR-HOLLAND-IPv6|NEW :xdcc send #9411",
                Utils.shuffle("KaizoLibTesting"), // Shuffle to support multiple people testing package
                true,
                true
        );

        final DCC dcc = ircManager.execute();
        assertNotNull(dcc);

        XDCCDownloadListener listener = new XDCCDownloadListener() {
            @Override
            public void onDownloadReadyToPlay(int progress, File downloadFile) {

            }

            @Override
            public void onProgress(int progress, String speed) {

            }

            @Override
            public void onFinished(File downloadFile) {

            }

            @Override
            public void onError(XDCCFailure failureCode) {
                fail(failureCode.name());
            }
        };

        File tempFile = Files.createTempFile("kaizo", null).toFile();

        XDCCDownloader downloader = new XDCCDownloader(
                dcc,
                Files.createTempFile("kaizo", null).toFile()
        );

        downloader.setXDCCDownloadListener(listener);
        downloader.start();

        deleteFile(tempFile);
    }

    private static void deleteFile(File file) {
        try {
            if (!file.delete()) file.deleteOnExit();
        } catch (Exception ignored) {
        }
    }
}
