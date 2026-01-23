package net.druidlabs.updtr;

import net.druidlabs.updtr.api.Request;
import net.druidlabs.updtr.api.requests.GetModRequest;
import net.druidlabs.updtr.api.requests.SearchModRequest;
import net.druidlabs.updtr.errorhandling.ErrorLogger;
import net.druidlabs.updtr.io.InOut;

import javax.imageio.stream.FileImageInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

public class Main {

    public static void main(String[] args) {
        ErrorLogger.initiate();

        try {
            try (Request getMod23 = GetModRequest.requestForMod(23);
                 Request searchJeiMod = SearchModRequest.searchMod("gameId=432", "slug=andruids-items")) {

                System.out.println("Get mod response code: " + getMod23.getResponseCode());
                Files.writeString(Path.of("GetModResponse.json"), getMod23.getResponse(),
                        StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

                System.out.println("Search mod response code: " + searchJeiMod.getResponseCode());
                System.out.println(searchJeiMod.requestUrl());
                Files.writeString(Path.of("SearchModResponse.json"), searchJeiMod.getResponse(),
                        StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }

            Thread.sleep(2000);

        } catch (Exception e) {
            ErrorLogger.logError(e);
        }

    }

}
