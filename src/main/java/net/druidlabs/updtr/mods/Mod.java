package net.druidlabs.updtr.mods;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import net.druidlabs.updtr.errorhandling.ErrorLogger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Path;
import java.util.Objects;
import java.util.jar.JarFile;

public class Mod implements Serializable {

    public static final String DEFAULT_ID = "NO-ID";
    public static final String DEFAULT_NAME = "NO-NAME";
    public static final String DEFAULT_VERSION = "NO-VERSION";

    private final String modId;
    private final String modVersion;
    private final String modName;
    private final String modFileName;

    protected Mod(String modId, String modVersion, String modName, String modFileName) {
        this.modVersion = modVersion;
        this.modName = modName;
        this.modId = modId;
        this.modFileName = modFileName;
    }

    public String getModId() {
        return modId;
    }

    public String getModVersion() {
        return modVersion;
    }

    public String getModName() {
        return modName;
    }

    public String getModFileName() {
        return modFileName;
    }

    public boolean isValidMod() {
        boolean invalidId = modId.equals(DEFAULT_ID);
        boolean invalidName = modName.equals(DEFAULT_NAME);
        boolean invalidVersion = modVersion.equals(DEFAULT_VERSION);

        return !invalidId && !invalidName && !invalidVersion;
    }

    @Contract("_ -> new")
    public static @NotNull Mod getInfo(@NotNull String path) {
        return getInfo(Path.of(path));
    }

    @Contract("_ -> new")
    public static @NotNull Mod getInfo(@NotNull Path path) {
        String fileName = path.getFileName().toString();

        if (!fileName.endsWith(".jar")) {
            IllegalArgumentException exception = new IllegalArgumentException("\"" + fileName + "\" is not a mod file");

            ErrorLogger.logError(exception);

            throw exception;
        }

        String id;
        String version;
        String name;

        try (JarFile modFile = new JarFile(path.toFile());
             InputStream inputStream = modFile.getInputStream(modFile.getJarEntry("fabric.mod.json"));
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            StringBuilder jsonStringBuilder = new StringBuilder();

            String line;

            while ((line = reader.readLine()) != null) {
                jsonStringBuilder.append(line);
            }

            JsonObject gson = JsonParser.parseString(jsonStringBuilder.toString())
                    .getAsJsonObject();

            id = gson.get("id").getAsString();
            version = gson.get("version").getAsString();
            name = gson.get("name").getAsString();

        } catch (IOException | JsonSyntaxException e) {
            ErrorLogger.logError(e);

            return new Mod(DEFAULT_ID, DEFAULT_VERSION, DEFAULT_NAME, fileName);
        }

        return new Mod(id, version, name, fileName);
    }

    @Override
    public String toString() {
        return "Mod{" + getModName() + " | " + getModId()
                + ":" + getModVersion() + " | " + getModFileName() + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Mod mod = (Mod) o;

        return Objects.equals(modId, mod.modId) && Objects.equals(modVersion, mod.modVersion)
                && Objects.equals(modName, mod.modName) && Objects.equals(modFileName, mod.modFileName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modId, modVersion, modName, modFileName);
    }
}
