package net.druidlabs.updtr.errorhandling;

import io.github.andruid929.leutils.errorhandling.ErrorMessageHandler;
import io.github.andruid929.leutils.time.TimeUnitConversion;
import io.github.andruid929.leutils.time.TimeUtil;
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
        ErrorMessageHandler.printSimpleErrorMessage(e);

        String errorCause = ErrorMessageHandler.simpleErrorMessage(e);

        String stackTrace = Arrays.stream(e.getStackTrace())
                .map(StackTraceElement::toString)
                .collect(Collectors.joining(System.lineSeparator()));

        String toLog = errorCause.concat(System.lineSeparator())
                .concat(stackTrace);

        boolean ignored = errorQueue.offer(toLog);
    }

    public static void initiate() {
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
    }

    private static @NotNull String timestampErrorMessage(String errMessage) {
        String time = TimeUtil.captureInstant().getTime(Constants.ERROR_LOG_TIMESTAMP_FORMAT);

        return "[" + time + "] " + errMessage;
    }
}
