package net.druidlabs.updtr.api.requests;

import net.druidlabs.updtr.api.ForgeURL;
import net.druidlabs.updtr.api.Request;
import net.druidlabs.updtr.errorhandling.ErrorLogger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;

public final class SearchModRequest extends Request {

    private int responseCode;

    private String response;

    private SearchModRequest(ForgeURL requestURL) {
        super(requestURL);
    }

    @Override
    protected void processRequest(@NotNull HttpsURLConnection connection) {
        try {
            responseCode = connection.getResponseCode();

            if (responseCode == 200) {
                response = processResponse(connection.getInputStream());

            } else if (responseCode == 400) {
                response = processResponse(connection.getErrorStream());

                throw new RuntimeException(response);

            } else if (responseCode == 500) {
                response = processResponse(connection.getErrorStream());

                throw new RuntimeException(response);
            }

        } catch (IOException e) {
            ErrorLogger.logError(e);
        }
    }

    @Contract(pure = true)
    @Override
    public @NotNull String getResponse() {
        return response;
    }

    @Override
    public int getResponseCode() {
        return responseCode;
    }

    public static @NotNull SearchModRequest searchMod(String @NotNull ... searchParams) throws IOException {
        ForgeURL requestURL = new ForgeURL.Builder("v1", "mods", "search")
                .appendParams("gameId=423")
                .appendParams(searchParams).build();

        return new SearchModRequest(requestURL);
    }
}
