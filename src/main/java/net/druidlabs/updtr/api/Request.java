package net.druidlabs.updtr.api;

import net.druidlabs.updtr.Constants;
import net.druidlabs.updtr.errorhandling.ErrorLogger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public abstract class Request {

    public static final String ERROR_RESPONSE = "ERR";

    protected String response;
    protected int responseCode;

    private final String requestURL;

    protected Request(@NotNull ForgeURL requestURL) {
        this.requestURL = requestURL.getRequestURL();

        URI uri = URI.create(this.requestURL);

        HttpRequest request = HttpRequest.newBuilder(uri)
                .GET()
                .header(Constants.FORGE_API_KEY_HEADER, Constants.FORGE_API_KEY)
                .timeout(Duration.ofSeconds(15))
                .build();

        try (HttpClient client = HttpClient.newHttpClient()) {

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            processResponse(response);

        } catch (IOException | InterruptedException e) {
            ErrorLogger.logError(e);
        }
    }

    protected abstract void processResponse(@NotNull HttpResponse<String> serverResponse);

    @Contract(pure = true)
    public @NotNull String getResponse() {
        return response;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getRequestURL() {
        return requestURL;
    }
}
