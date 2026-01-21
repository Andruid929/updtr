package net.druidlabs.updtr;

import net.druidlabs.updtr.api.Request;
import net.druidlabs.updtr.errorhandling.ErrorLogger;
import net.druidlabs.updtr.io.InOut;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        ErrorLogger.initiate();

        InOut.backupMods();

        new Request(248787);

        Thread.sleep(2000);
    }

}
