package net.druidlabs.updtr.api.response;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public final class FileIndex {

    private final String gameVersion;
    private final String filename;
    private final int fileId;
    private final int releaseType;
    private final int gameVersionTypeId;
    private final int modLoader;

    private FileIndex(String gameVersion, int fileId, String filename, int releaseType, int gameVersionTypeId, int modLoader) {
        this.gameVersion = gameVersion;
        this.fileId = fileId;
        this.filename = filename;
        this.releaseType = releaseType;
        this.gameVersionTypeId = gameVersionTypeId;
        this.modLoader = modLoader;
    }

    public String getGameVersion() {
        return gameVersion;
    }

    public String getFilename() {
        return filename;
    }

    public int getFileId() {
        return fileId;
    }

    public int getReleaseType() {
        return releaseType;
    }

    public int getGameVersionTypeId() {
        return gameVersionTypeId;
    }

    public int getModLoader() {
        return modLoader;
    }
}
