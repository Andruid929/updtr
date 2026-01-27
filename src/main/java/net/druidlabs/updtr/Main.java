package net.druidlabs.updtr;

import io.github.andruid929.leutils.time.TaskTimer;
import net.druidlabs.updtr.api.Request;
import net.druidlabs.updtr.api.requests.GetModRequest;
import net.druidlabs.updtr.api.requests.SearchModRequest;
import net.druidlabs.updtr.errorhandling.ErrorLogger;
import net.druidlabs.updtr.io.InOut;
import net.druidlabs.updtr.mods.Mod;

import javax.imageio.stream.FileImageInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

public class Main {

    public static void main(String[] args) {
        ErrorLogger.initiate();

        try {
        TaskTimer taskTimer = new TaskTimer();

        Mod druidMod = Mod.getInfo("src\\test\\resources\\moreitems-1.9.1-1.21.11.jar");

        String slug = "slug=".concat("not-enough-animations");

            System.out.println(slug);

            try (Request searchJeiMod = SearchModRequest.searchMod("gameId=432", slug)) {

                System.out.println(searchJeiMod.getResponseCode());

                Files.writeString(Path.of("Response.json"), searchJeiMod.getResponse(), StandardCharsets.UTF_8, CREATE, TRUNCATE_EXISTING);

            }

            Thread.sleep(2000);

        } catch (Exception e) {
            ErrorLogger.logError(e);
        }

    }

}
