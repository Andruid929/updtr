package net.druidlabs.updtr.io;

import io.github.andruid929.leutils.time.TimeUnitConversion;
import net.druidlabs.updtr.errorhandling.ErrorLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Time;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class InOut {

    public static void backupMods() throws IOException {
        Path backupTo = Paths.MODS_BACKUP_FOLDER;

        if (Files.notExists(backupTo)) {
            Files.createDirectory(backupTo);
        }

        try (Stream<Path> modsFolderDir = Files.walk(Paths.MINECRAFT_MODS_FOLDER);
             ExecutorService service = Executors.newVirtualThreadPerTaskExecutor()) {

            Set<Path> modFiles = modsFolderDir.filter(path -> path.toString().endsWith(".jar"))
                    .collect(Collectors.toSet());

            if (modFiles.isEmpty()) {
                return;
            }

            Callable<Long> backupTask = () -> {
                long startTimeInMs = System.currentTimeMillis();

                for (Path modFile : modFiles) {
                    Files.copy(modFile, backupTo.resolve(modFile.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                }

                long endTimeInMs = System.currentTimeMillis();

                return endTimeInMs - startTimeInMs;
            };

            Future<Long> backupTaskFuture = service.submit(backupTask);

            double timeTakenInMs = backupTaskFuture.get(20, TimeUnit.MILLISECONDS);

            String outputString = "Backup finished in " + TimeUnitConversion.milliToSecond(timeTakenInMs) + "s";

            System.out.println(outputString);

        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            ErrorLogger.logError(e);
        }
    }

}
