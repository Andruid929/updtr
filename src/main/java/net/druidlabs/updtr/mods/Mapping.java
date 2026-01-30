package net.druidlabs.updtr.mods;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.druidlabs.updtr.errorhandling.ErrorLogger;
import net.druidlabs.updtr.io.InOut;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class Mapping {

    private static final Set<CFMod> CURSEFORGE_MOD_MAPPINGS = ConcurrentHashMap.newKeySet();

    public static final String MAPPINGS_FILE_NAME = "Mod mappings.json";

    private static final Type MAPPING_MAP_TYPE = new TypeToken<Set<CFMod>>(){}.getType();

    private Mapping() {
    }

    public static void persistMappings() {
        try {

            if (CURSEFORGE_MOD_MAPPINGS.isEmpty()) {
                throw new UnsupportedOperationException("Cannot persist mappings, no entries found");
            }

            InOut.persistConfigData(CURSEFORGE_MOD_MAPPINGS, MAPPING_MAP_TYPE, MAPPINGS_FILE_NAME);

        } catch (IOException | UnsupportedOperationException e) {
            ErrorLogger.logError(e);
        }
    }

    public static Set<CFMod> getLoadedMappings() {
        return CURSEFORGE_MOD_MAPPINGS;
    }

    public static Set<CFMod> getLocalMappings() {
        try {

            String mappingJsonData = InOut.readConfigData(MAPPINGS_FILE_NAME);

            Set<CFMod> localMappings = new Gson().fromJson(mappingJsonData, MAPPING_MAP_TYPE);

            if (!localMappings.isEmpty()) {
                CURSEFORGE_MOD_MAPPINGS.addAll(localMappings);
            }

            return localMappings;

        } catch (IOException e) {
            ErrorLogger.logError(e);

            return CURSEFORGE_MOD_MAPPINGS;
        }

    }

    public static void addMappingEntry(Mod mod, int projectId) {
        CFMod cfMod = CFMod.getInstance(mod, projectId);

        CURSEFORGE_MOD_MAPPINGS.add(cfMod);
    }
}
