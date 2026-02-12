package net.druidlabs.updtr.api;

import net.druidlabs.updtr.Constants;
import net.druidlabs.updtr.api.requests.GetModFileRequest;
import net.druidlabs.updtr.api.requests.GetModRequest;
import net.druidlabs.updtr.api.requests.SearchModRequest;
import net.druidlabs.updtr.api.response.ResponseHandler;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ForgeURLTest {

    @Test
    void ssd() throws IOException, InterruptedException {
        Request request = GetModFileRequest.getModFileUrl(942925, 7394017);

        String url = ResponseHandler.handleGetFileUrlResponse(request.getResponse()).asString("data");

        Download download = new Download(url, Path.of("Modfile.jar"));

        System.out.println(download.getSavePath());
    }

    @Test
    void urlBuilder() throws MalformedURLException {
        ForgeURL forgeURL = new ForgeURL.Builder("v1", "mods", "932").build();

        assertTrue(forgeURL.getRequestURL().endsWith("mods/932"));
        assertThrows(MalformedURLException.class, () -> new ForgeURL.Builder().build());
    }

    @Test
    void appendParams() throws MalformedURLException {
        ForgeURL forgeURL = new ForgeURL.Builder("v1", "mods", "932")
                .appendParams("name=druid", "id=22").build();

        assertTrue(forgeURL.getRequestURL().endsWith("name=druid&id=22"));
        assertThrows(MalformedURLException.class, () -> new ForgeURL.Builder().appendParams("param=1").build());
    }

}