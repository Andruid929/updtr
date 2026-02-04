package net.druidlabs.updtr.session;

import net.druidlabs.updtr.io.Paths;
import net.druidlabs.updtr.mods.ModLoaders;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Session {

    private final String gameVersion;

    private final ModLoaders modLoader;

    private Session(String gameVersion, int modLoader) {
        this.gameVersion = gameVersion;

        this.modLoader = switch (modLoader) {
            case 3 -> ModLoaders.FORGE;
            case 4 -> ModLoaders.FABRIC;
            case 5 -> ModLoaders.QUILT;
            case 6 -> ModLoaders.NEO_FORGE;
            default -> throw new IllegalArgumentException("Unknown mod loader");
        };
    }

    public ModLoaders getModLoader() {
        return modLoader;
    }

    public String getGameVersion() {
        return gameVersion;
    }

    private static @NotNull String getFabricApiFileName() throws IOException {
        try(Stream<Path> dirStream = Files.walk(Paths.MINECRAFT_MODS_FOLDER)) {

            Set<String> modFileNames = dirStream.map(path -> path.getFileName().toString())
                    .collect(Collectors.toSet());

            for (String modFileName : modFileNames) {

                if (modFileName.startsWith("fabric-api-")) {
                    return modFileName;
                }
            }
        }

        return "";
    }

    @Contract(pure = true)
    public static @NotNull Session createSession() throws IOException {
        String fabricApiFilename = getFabricApiFileName().replace(".jar","");

        if (fabricApiFilename.isBlank()) {
            throw new IllegalStateException("Unable to create session, unknown game version");
        }

        String version;

        {
            int plusIndex = fabricApiFilename.lastIndexOf("+");

            version = fabricApiFilename.substring(plusIndex + 1);
        }

        return new Session(version, 4);
    }
}
