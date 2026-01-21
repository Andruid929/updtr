package net.druidlabs.updtr.api;

import org.jetbrains.annotations.NotNull;

public class ForgeURLs {

    private ForgeURLs() {}

    public static final String FORGE_BASE = "https://api.curseforge.com";

    public static @NotNull String getModUrl(String modId) {
        return FORGE_BASE.concat("/v1/mods/").concat(modId);
    }

    public static @NotNull String getFromBaseUrl(@NotNull final String url) {
        return FORGE_BASE.concat(url);
    }



}
