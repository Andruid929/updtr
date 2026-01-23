package net.druidlabs.updtr.api;

import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ForgeURLTest {

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