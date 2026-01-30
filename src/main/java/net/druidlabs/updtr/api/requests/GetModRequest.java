package net.druidlabs.updtr.api.requests;

import net.druidlabs.updtr.api.ForgeURL;
import net.druidlabs.updtr.api.Request;
import net.druidlabs.updtr.errorhandling.ErrorLogger;
import net.druidlabs.updtr.util.ResourceNotFoundException;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;

public final class GetModRequest extends Request {

    private final int modProjectId;

    private int responseCode;

    private String response;

    private GetModRequest(ForgeURL requestURL, int modProjectId) {
        super(requestURL);
        this.modProjectId = modProjectId;
    }

    @Override
    protected void processRequest(@NotNull HttpsURLConnection connection) {
        try {
            responseCode = connection.getResponseCode();

            if (responseCode == 200) {
                response = processResponse(connection.getInputStream());

            } else if (responseCode == 404) {
                response = processResponse(connection.getErrorStream());

                throw new ResourceNotFoundException("Resource under project ID " + modProjectId + " could not be found");

            } else if (responseCode == 500) {
                response = processResponse(connection.getErrorStream());

                throw new RuntimeException(response);

            }
        } catch (IOException e) {
            ErrorLogger.logError(e);
        }
    }

    @Override
    public @NotNull String getResponse() {
        return response;
    }

    @Override
    public int getResponseCode() {
        return responseCode;
    }

    public static @NotNull GetModRequest requestForMod(int modProjectId) throws IOException {
        ForgeURL requestURL = new ForgeURL.Builder("v1", "mods", String.valueOf(modProjectId)).build();

        return new GetModRequest(requestURL, modProjectId);
    }
}
