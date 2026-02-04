package net.druidlabs.updtr.api.requests;

import net.druidlabs.updtr.Constants;
import net.druidlabs.updtr.api.Request;
import net.druidlabs.updtr.util.SlugExtractor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public final class Requests {

    public static @NotNull String searchSlugResponse(String url) throws IOException {
        String slug = "slug=".concat(SlugExtractor.getSlug(url));

        try (Request request = SearchModRequest.searchMod(Constants.MINECRAFT_GAME_ID_SLUG, slug)) {

            return request.getResponse();
        }
    }

    public static @NotNull String getModResponse(int projectId) throws IOException {
        try (Request request = GetModRequest.requestForMod(projectId)) {

            return request.getResponse();
        }
    }

}
