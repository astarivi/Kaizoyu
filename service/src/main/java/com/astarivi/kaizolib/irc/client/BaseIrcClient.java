package com.astarivi.kaizolib.irc.client;

import com.astarivi.kaizolib.irc.exception.BlacklistedIpException;
import com.astarivi.kaizolib.irc.exception.BotNotFoundException;
import com.astarivi.kaizolib.irc.exception.GenericHandshakeException;
import com.astarivi.kaizolib.irc.exception.NoQuickRetryException;
import com.astarivi.kaizolib.xdcc.model.DCC;
import com.astarivi.kaizolib.irc.utils.Utils;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;


public class BaseIrcClient {
    private static final String version = " :KaizoLib 1.7 [" + System.getProperty("os.arch") + "]";
    private final String packCommand;
    private final String nickname;
    protected IrcOnConnectionListener ircConnectionListener;
    protected PrintWriter out;
    protected Scanner in;
    protected int timeoutMs = 15_000;

    public BaseIrcClient(String nickname, String packCommand) {
        this.nickname = nickname;
        this.packCommand = packCommand;
    }

    protected @NotNull DCC start() throws NoQuickRetryException, TimeoutException, BlacklistedIpException, GenericHandshakeException, BotNotFoundException {
        try {
            // Authenticate basic data with server
            write("NICK", nickname);
            write("USER", nickname + " 0 * :" + nickname + "-Kaizo!");

            // Setup vars for while loop
            boolean retryRan = false;
            long startTime = System.currentTimeMillis();

            while (in.hasNext() || (System.currentTimeMillis() - startTime) < timeoutMs) {
                String serverMessage = in.nextLine();
                Logger.debug(serverMessage);

                if (serverMessage.contains("DNSBL Blacklisted IP")) {
                    throw new BlacklistedIpException("Your IP has been blacklisted from Rizon.");
                }

                if (serverMessage.contains(":No such nick/channel") && serverMessage.contains(nickname)) {
                    throw new BotNotFoundException("The bot couldn't be found inside the #NIBL channel.");
                }

                // Automated responses to comply with IRC standard
                if (mandatoryResponses(serverMessage, nickname)) {
                    continue;
                }

                // Messages to the server, which aren't automated (ej: Joining a channel)
                if (serverMessages(serverMessage, packCommand)) {
                    continue;
                }

                // If the received message contains the DCC keyword, start parsing and return it.
                if (serverMessage.split(" ")[1].contains("PRIVMSG")
                        && serverMessage.contains("\u0001DCC")) {

                    return parseResponse(serverMessage);
                }

                if (retryRan) {
                    final String message = "Retry has been attempted, but the bot doesn't support quick-retry";

                    Logger.warn(message);
                    throw new NoQuickRetryException(message);
                }

                // If the pack has already been requested from the bot, cancel and retry.
                if (serverMessage.contains("You already requested that pack")
                        || serverMessage.contains("You have a DCC pending")){
                    Logger.warn("You have requested this resource before. You'll may have to wait from 150 to " +
                            "+300 seconds before retrying.");
                    write(
                        "PRIVMSG",
                        serverMessage.split("!")[0].substring(1) + " :XDCC CANCEL"
                    );
                    write("PRIVMSG", packCommand);
                    retryRan = true;
                }
            }

            final String message = "The IRC connection didn't respond in time, or didn't respond at all to the request.";
            Logger.warn(message);
            throw new TimeoutException(message);
        } catch (NoQuickRetryException | TimeoutException | BlacklistedIpException | BotNotFoundException e) {
            throw e;
        } catch(Exception e) {
            throw new GenericHandshakeException(e);
        } finally {
            close();
        }
    }

    private void write(String command, String message) {
        String fullMessage = command + " " + message;
        out.print(fullMessage + "\r\n");
        out.flush();

        Logger.debug("Said: " + fullMessage);
    }

    protected void quit() {
        out.print("QUIT" + "\r\n");
        out.flush();
    }

    protected void close() {
        try {
            quit();
        } catch (Exception ignored) {
        }
        if (out != null) out.close();
        if (in != null) in.close();
    }

    private boolean mandatoryResponses(String receivedMessage, String nickname) {
        // PING message, should be answered with "PONG" and the number received with the "PING" command from
        // the IRC server.
        if (receivedMessage.startsWith("PING")) {
            write("PONG", receivedMessage.split(" ")[1]);
            return true;
        }

        // VERSION message, should be answered with the client version to the person who asked.
        if (receivedMessage.contains("\u0001VERSION\u0001")) {
            write("PRIVMSG",
                    receivedMessage
                            .split("!")[0]
                            .substring(1) + version);
            return true;
        }

        // If our nickname is already in use for any reason, shuffle it and authenticate again.
        if (receivedMessage.contains("Nickname is already in use")
                && receivedMessage.split(" ")[0].contains(":irc.rizon.io")){

            String shuffledNickname = Utils.shuffle(nickname);

            write("NICK", shuffledNickname);
            write("USER", shuffledNickname + " 0 * :" + shuffledNickname + "-Kaizo!");
            return true;
        }

        return false;
    }

    private boolean serverMessages(String receivedMessage, String packCommand) {
        // Marks the end of the MOTD. Perfect time to join the channel we intend to join.
        if (receivedMessage.contains("End of /MOTD command")) {
            write("JOIN", "#nibl");
            if (ircConnectionListener != null) ircConnectionListener.onConnection();
            return true;
        }

        // Marks the end of the NIBL channel welcome message. Perfect time to message the bot.
        if (receivedMessage.contains("End of /NAMES list")) {
            write("PRIVMSG", packCommand);
            return true;
        }

        return false;
    }

    // IPv6 = DCC SEND "[SubsPlease] Vinland Saga S2 - 01 (720p) [38ED8C16].mkv" 2001:1af8:4e00:a019:9::1337 13580 805873805
    private DCC parseResponse(String message) {
        String[] msg = message.split("\u0001")[1].split(" ");

        String filename = String.join(
                        " ", Arrays.copyOfRange(msg, 2, msg.length-3))
                .replace("\"", "");

        String ipAddress;
        boolean isIPv6 = this.packCommand.toLowerCase().contains("ipv6");

        if (!isIPv6) {
            try {
                long ip = Long.parseLong(msg[msg.length - 3]);
                ipAddress = String.format("%d.%d.%d.%d",
                        (ip >> 24 & 0xff),
                        (ip >> 16 & 0xff),
                        (ip >> 8 & 0xff),
                        (ip & 0xff));
            } catch(NumberFormatException e) {
                ipAddress = msg[msg.length - 3];
            }
        } else {
            ipAddress = msg[msg.length - 3];
        }

        return new DCC(
            filename,
            ipAddress,
            Integer.parseInt(msg[msg.length-2]),
            Long.parseLong(msg[msg.length-1]),
            isIPv6
        );
    }

    @SuppressWarnings("unused")
    public void setIrcOnConnectionListener(IrcOnConnectionListener cListener) {
        ircConnectionListener = cListener;
    }

    public interface IrcOnConnectionListener{
        void onConnection();
    }
}
