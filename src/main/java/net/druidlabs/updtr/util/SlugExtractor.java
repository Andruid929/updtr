package net.druidlabs.updtr.util;

import org.jetbrains.annotations.NotNull;

public final class SlugExtractor {

    private SlugExtractor() {
    }

    public static @NotNull String getSlug(@NotNull String url) {
        int startIndex = url.lastIndexOf("/") + 1;

        return url.substring(startIndex);
    }

}
