package net.druidlabs.updtr.errorhandling;

import io.github.andruid929.leutils.errorhandling.ErrorMessageHandler;
import net.druidlabs.updtr.Constants;
import net.druidlabs.updtr.io.Paths;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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

    private static volatile boolean useSimpleLogs = false;

    private static volatile boolean isLoggerActive = false;

    static {
        if (Files.notExists(Paths.APP_DIRECTORY)) {
            try {
                Files.createDirectory(Paths.APP_DIRECTORY);
            } catch (IOException e) {
                System.out.println("Unable to create app folder");
            }
        }
    }

    private ErrorLogger() {
    }

    public static void logError(Exception e) {
        String toLog = ErrorMessageHandler.simpleErrorMessage(e);

        ErrorMessageHandler.printSimpleErrorMessage(e);

        if (useSimpleLogs) {
            errorQueue.offer(toLog);

        } else {
            String stackTrace = Arrays.stream(e.getStackTrace())
                    .map(StackTraceElement::toString)
                    .collect(Collectors.joining(System.lineSeparator()));

            String extendedLog = toLog.concat(System.lineSeparator())
                    .concat(stackTrace);

            errorQueue.offer(extendedLog);
        }

    }

    private static synchronized void setSimpleLogs(boolean simpleLogs) {
        if (useSimpleLogs != simpleLogs) {
            useSimpleLogs = !useSimpleLogs;
        }
    }

    public static void initiate() {
        if (isLoggerActive) return;

        Thread errorLoggerThread = new Thread(() -> {

            File pathToLogFile = Paths.createPathInAppDirectory(ERROR_LOG_FILE).toFile();

            try (FileWriter writer = new FileWriter(pathToLogFile, StandardCharsets.UTF_8, true);
                 PrintWriter logger = new PrintWriter(writer)) {

                while (true) {
                    String error = errorQueue.take();

                    String errorOutput = timestampErrorMessage(error);

                    logger.println(errorOutput);
                    logger.flush();
                }

            } catch (IOException | InterruptedException e) {
                ErrorMessageHandler.printSimpleErrorMessage(e);

                Thread.currentThread().interrupt();
            }
        });

        errorLoggerThread.setName("UpdaterErrorLog");
        errorLoggerThread.setDaemon(true);
        errorLoggerThread.start();

        isLoggerActive = true;

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {

            File pathToLogFile = Paths.createPathInAppDirectory(ERROR_LOG_FILE).toFile();

            try (FileWriter writer = new FileWriter(pathToLogFile, StandardCharsets.UTF_8, true);
                 PrintWriter logger = new PrintWriter(writer)) {

                String msg;
                while ((msg = errorQueue.poll()) != null) {
                    logger.println(timestampErrorMessage(msg));
                }

                logger.flush();

            } catch (IOException ignored) {
            }
        }, "UpdaterErrorLog-ShutdownHook"));
    }

    private static @NotNull String timestampErrorMessage(String errMessage) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());

        String time = DateTimeFormatter.ofPattern(Constants.ERROR_LOG_TIMESTAMP_FORMAT).format(now);

        return "[" + time + "] " + errMessage;
    }
}
