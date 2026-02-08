package net.druidlabs.updtr.api;

import net.druidlabs.updtr.Constants;
import net.druidlabs.updtr.errorhandling.ErrorLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public abstract class Request implements AutoCloseable {

    public static final String ERROR_RESPONSE = "ERR";

    private HttpsURLConnection connection;

    private final String requestURL;

    protected Request(@NotNull ForgeURL requestURL) {
        this.requestURL = requestURL.getRequestURL();

        try {
            URL url = new URI(requestURL.getRequestURL()).toURL();

            connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.addRequestProperty("x-api-key", Constants.FORGE_API_KEY);
            connection.setConnectTimeout(10_000);
            connection.setReadTimeout(15_000);

            processRequest(connection);
        } catch (IOException | URISyntaxException | RuntimeException e) {
            ErrorLogger.logError(e);
        }
    }

    protected String processResponse(InputStream inputStream) throws IOException {

        try (InputStreamReader streamReader = new InputStreamReader(inputStream);
             BufferedReader reader = new BufferedReader(streamReader)) {

            String line;

            StringBuilder builder = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

            return builder.toString();
        }
    }

    protected abstract void processRequest(@NotNull HttpsURLConnection connection);

    public abstract @NotNull String getResponse();

    public abstract int getResponseCode();

    public String requestUrl() {
        return requestURL;
    }

    @Override
    public void close() {
        if (connection != null) {
            connection.disconnect();
        }
    }
}
