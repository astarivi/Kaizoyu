package com.astarivi.kaizolib.irc;

import com.astarivi.kaizolib.irc.client.BaseIrcClient;
import com.astarivi.kaizolib.irc.exception.*;
import com.astarivi.kaizolib.xdcc.model.DCC;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;


public class IrcClient extends BaseIrcClient {
    private boolean useTls;
    private final boolean alwaysTls;

    public IrcClient(String packCommand, String nickname, boolean useTls, boolean alwaysTls) {
        super(nickname, packCommand);
        this.useTls = useTls;
        this.alwaysTls = useTls && alwaysTls;
    }

    public DCC execute() throws IOException, NoQuickRetryException, TimeoutException, StrictModeException, BlacklistedIpException, GenericHandshakeException, BotNotFoundException {
        if (useTls) {
            try {
                return executeTls();
            } catch(IOException | TimeoutException e) {
                if (alwaysTls) {
                    throw new StrictModeException(e);
                }

                useTls = false;
                super.close();
            }
        }

        return executeUnencrypted();
    }

    private @NotNull DCC executeTls() throws IOException, NoQuickRetryException, TimeoutException, BlacklistedIpException, GenericHandshakeException, BotNotFoundException {
        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();

        try (SSLSocket socket = (SSLSocket) factory.createSocket("irc.rizon.net", 6697)) {
            socket.startHandshake();

            out = new PrintWriter(socket.getOutputStream(), true);
            in = new Scanner(socket.getInputStream());

            // If client prefers privacy
            if (alwaysTls) {
                timeoutMs = 20_000;
            } else {
                timeoutMs = 10_000;
            }

            return super.start();
        }
    }

    private @NotNull DCC executeUnencrypted() throws IOException, NoQuickRetryException, TimeoutException, BlacklistedIpException, GenericHandshakeException, BotNotFoundException {
        try (Socket socket = new Socket("irc.rizon.net", 6667)) {
            // Initialize sockets
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new Scanner(socket.getInputStream());
            timeoutMs = 20_000;
            return super.start();
        }
    }
}
