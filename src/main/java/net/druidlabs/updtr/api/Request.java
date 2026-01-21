package net.druidlabs.updtr.api;

import net.druidlabs.updtr.Constants;
import net.druidlabs.updtr.errorhandling.ErrorLogger;
import net.druidlabs.updtr.util.ResourceNotFoundException;

import javax.imageio.stream.FileCacheImageInputStream;
import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class Request {

    private String response;

    public Request(int modProjectId) {
        try {
            URL url = new URI(ForgeURLs.getModUrl(String.valueOf(modProjectId))).toURL();

            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.addRequestProperty("x-api-key", Constants.FORGE_API_KEY);
            connection.setConnectTimeout(10_000);
            connection.setReadTimeout(15_000);

            int responseCode = connection.getResponseCode();

            if (responseCode == 404) {
                throw new ResourceNotFoundException("Resource under ID " + modProjectId + " could not be found");
            } else if (responseCode == 500) {
                throw new RuntimeException("Resource server error");

            } else if (responseCode == 200) {

                try (InputStreamReader streamReader = new InputStreamReader(connection.getInputStream());
                     BufferedReader reader = new BufferedReader(streamReader)) {

                    String line;

                    StringBuilder builder = new StringBuilder();

                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }

                    response = builder.toString();

                    connection.disconnect();
                }
            }
        } catch (IOException | URISyntaxException | RuntimeException e) {
            ErrorLogger.logError(e);
        }
    }
}
