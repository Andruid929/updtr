package net.druidlabs.updtr.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;

public final class ResponseHandler {

    public static final String LATEST_FILES_INDEXES = "latestFilesIndexes";
    public static final String MOD_PROJECT_ID = "id";

    private final JsonObject root;

    private ResponseHandler(JsonObject root) {
        this.root = root;
    }

    public JsonObject getRoot() {
        return root;
    }

    public int getModProjectId() {
        return getRoot().get(MOD_PROJECT_ID).getAsInt();
    }

    public JsonObject asJsonObject(String name) {
        return getRoot().get(name).getAsJsonObject();
    }

    public JsonArray asJsonArray(String name) {
        return getRoot().get(name).getAsJsonArray();
    }

    public int asInt(String name) {
        return getRoot().get(name).getAsInt();
    }

    public String asString(String name) {
        return getRoot().get(name).getAsString();
    }

    public static @NotNull ResponseHandler handleGetModResponse(@NotNull String response) {
        JsonObject root = JsonParser.parseString(response).getAsJsonObject();

        return new ResponseHandler(root);
    }

    public static @NotNull ResponseHandler handleSearchModResponse(@NotNull String response, int index) {
        JsonArray root = JsonParser.parseString(response).getAsJsonObject().get("data").getAsJsonArray();

        return new ResponseHandler(root.get(index).getAsJsonObject());
    }

    public static @NotNull ResponseHandler handleSearchModResponse(@NotNull String response) {
        return handleSearchModResponse(response, 0);
    }

}
