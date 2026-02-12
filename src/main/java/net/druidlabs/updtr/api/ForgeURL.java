package net.druidlabs.updtr.api;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.util.Objects;

public final class ForgeURL {

    public static final String FORGE_BASE = "https://api.curseforge.com/";

    private final String requestURL;

    private ForgeURL(@NotNull String url, @Nullable String params) {
        if (params == null) {
            requestURL = url;
        } else {
            requestURL = url.concat(params);
        }
    }

    public @NotNull String getRequestURL() {
        return requestURL;
    }

    @Contract("_ -> new")
    public static @NotNull Builder newBuilder(String @NotNull ... bits) throws MalformedURLException {
        return new Builder(bits);
    }

    public static class Builder {

        private final String url;
        private String urlParams;

        public Builder(String @NotNull ... bits) throws MalformedURLException {
            String baseUrl = FORGE_BASE;

            if (bits.length == 1) {
                url = baseUrl.concat(bits[0]);
            } else if (bits.length > 1) {
                url = baseUrl.concat(String.join("/", bits));
            } else {
                throw new MalformedURLException("Invalid URL path");
            }
        }

        public Builder appendParams(String @NotNull ... params) throws MalformedURLException {

            String questionMark = "?";

            if (params.length == 1) {
                urlParams = questionMark.concat(params[0]);
            } else if (params.length > 1) {
                urlParams = questionMark.concat(String.join("&", params));
            } else {
                throw new MalformedURLException("Invalid params");
            }

            return this;
        }

        public ForgeURL build() {
            return new ForgeURL(url, urlParams);
        }
    }

    @Override
    public String toString() {
        return "ForgeURL{" + requestURL + "}";
    }
}
