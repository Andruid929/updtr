package net.druidlabs.updtr.errorhandling;

import io.github.andruid929.leutils.errorhandling.ErrorMessageHandler;
import net.druidlabs.updtr.Constants;
import org.jetbrains.annotations.NotNull;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public final class ErrorLogger {

    private static final BlockingQueue<String> errorQueue = new LinkedBlockingQueue<>();

    private static final String ERROR_LOG_FILE = "ModupdErr.txt";

    private static boolean simpleLogs = false;

    static {
        Thread errorLoggerThread = new Thread(() -> {

            try(FileWriter writer = new FileWriter(ERROR_LOG_FILE, true);
                PrintWriter logger = new PrintWriter(writer)) {

                while (true) {
                    String error = errorQueue.take();

                    String errorOutput = timestampErrorMessage(error);

                    logger.println(errorOutput);
                    logger.flush();
                }

            } catch (IOException | InterruptedException e) {
                ErrorMessageHandler.printSimpleErrorMessage(e);
            }
        });

        errorLoggerThread.setName("UpdaterErrorLog");
        errorLoggerThread.setDaemon(true);
        errorLoggerThread.start();
    }

    private ErrorLogger() {
    }

    public static void logError(Exception e) {
        String toLog;

        if (simpleLogs) {
            toLog = ErrorMessageHandler.simpleErrorMessage(e);

        } else {
            toLog = Arrays.stream(e.getStackTrace())
                    .map(StackTraceElement::toString)
                    .collect(Collectors.joining(System.lineSeparator()));
        }

        errorQueue.offer(toLog);
    }

    private static void setSimpleLogs(boolean simpleLogsOn) {
        if (simpleLogsOn != simpleLogs) {
            simpleLogs = !simpleLogs;
        }
    }

    public static void initiate() {
        System.out.println("Error logger started");
    }

    private static @NotNull String timestampErrorMessage(String errMessage) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());

        String time = DateTimeFormatter.ofPattern(Constants.ERROR_LOG_TIMESTAMP_FORMAT).format(now);

        return "[" + time + "] " + errMessage;
    }
}
