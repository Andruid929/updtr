package net.druidlabs.updtr.io;

import net.druidlabs.updtr.Constants;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public final class Paths {


    public static final Path APP_DIRECTORY = createPath(System.getProperty("user.home"),
            "Documents").resolve(Constants.APP_NAME);

    public static final Path MINECRAFT_FOLDER = createPath(System.getenv("APPDATA"), ".minecraft");

    public static final Path MINECRAFT_MODS_FOLDER = MINECRAFT_FOLDER.resolve("mods");

    public static final Path MODS_BACKUP_FOLDER = MINECRAFT_FOLDER.resolve(Constants.MODS_BACKUP_FOLDER_NAME);

    public static @NotNull String pathAsString(@NotNull Path path, String... extra) {
        String pathAsString = path.toString();

        if (extra == null) {
            return pathAsString;
        }

        return createPath(pathAsString, extra).toString();
    }

    public static @NotNull Path createPathInAppDirectory(String... extra) {

        String appDir = pathAsString(APP_DIRECTORY);

        return createPath(appDir, extra);
    }

    @Contract(pure = true)
    private static @NotNull Path createPath(String first, String... more) {
        return Path.of(first, more);
    }
}
