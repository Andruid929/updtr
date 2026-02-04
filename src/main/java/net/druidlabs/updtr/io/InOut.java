package net.druidlabs.updtr.io;

import com.google.gson.Gson;
import io.github.andruid929.leutils.time.TimeUnitConversion;
import net.druidlabs.updtr.errorhandling.ErrorLogger;
import net.druidlabs.updtr.mods.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
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

            double timeTakenInMs = backupTaskFuture.get();

            String outputString = "Backup finished in " + TimeUnitConversion.milliToSecond(timeTakenInMs) + "s";

            System.out.println(outputString);

        } catch (ExecutionException | InterruptedException e) {
            System.err.println("Unable to backup mods:");

            ErrorLogger.logError(e);
        }
    }

    public static @NotNull Set<Mod> loadLocalMods(@Nullable String modsFolderName) throws IOException {
        Path pathToRead;

        if (modsFolderName == null) {
            pathToRead = Paths.MINECRAFT_MODS_FOLDER;

        } else {
            pathToRead = Paths.MINECRAFT_FOLDER.resolve(modsFolderName);
        }

        Set<Mod> localMods = new HashSet<>();

        try (Stream<Path> modDir = Files.walk(pathToRead)
                .filter(path -> path.toString().endsWith(".jar"))) {

            Set<Path> modFilePaths = modDir.collect(Collectors.toSet());

            if (modFilePaths.isEmpty()) {
                return localMods;
            }

            modFilePaths.forEach(path -> {
                Mod mod = Mod.getInfo(path);

                localMods.add(mod);
            });
        }

        return localMods;
    }

    public static void persistDataAsJson(Object data, Type dataType, @NotNull Path absolutePath) throws IOException {
        if (Files.notExists(absolutePath.getParent())) {
            Files.createDirectories(absolutePath.getParent());
        }

        String jsonString = new Gson().toJson(data, dataType);

        try (BufferedWriter writer = Files.newBufferedWriter(absolutePath, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

            writer.write(jsonString);
        }

    }

    public static void persistDataAsJson(Object data, Type dataType, String absolutePath) throws IOException {
        persistDataAsJson(data, dataType, Path.of(absolutePath));
    }

    public static void persistConfigData(Object data, Type dataType, String fileName) throws IOException {
        Path saveFile = Paths.APP_CONFIG_FOLDER.resolve(fileName);

        persistDataAsJson(data, dataType, saveFile);
    }

    public static @NotNull String readJsonData(Path absolutePath) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(absolutePath, StandardCharsets.UTF_8)) {

            String line;
            StringBuilder jsonStringBuilder = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                jsonStringBuilder.append(line);
            }

            return jsonStringBuilder.toString();
        }

    }

    public static @NotNull String readJsonData(String absolutePath) throws IOException {
        return readJsonData(Path.of(absolutePath));
    }

    public static @NotNull String readConfigData(String fileName) throws IOException {
        Path saveFile = Paths.APP_CONFIG_FOLDER.resolve(fileName);

        return readJsonData(saveFile);
    }

}
