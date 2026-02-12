package net.druidlabs.updtr.api;

import net.druidlabs.updtr.Constants;
import net.druidlabs.updtr.errorhandling.ErrorLogger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;

public final class Download {

    private int responseCode;

    private Path savePath;

    public Download(String downloadUrl, Path file) {
        try(HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build()) {

            URI uri = URI.create(downloadUrl);

            HttpRequest request = HttpRequest.newBuilder(uri)
                    .header(Constants.FORGE_API_KEY_HEADER, Constants.FORGE_API_KEY)
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(file));

            responseCode = response.statusCode();

            savePath = response.body();

        } catch (Exception e) {
            ErrorLogger.logError(e);
        }
    }


    public int getResponseCode() {
        return responseCode;
    }

    public Path getSavePath() {
        return savePath;
    }
}
