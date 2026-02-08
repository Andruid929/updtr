package net.druidlabs.updtr.mods;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.druidlabs.updtr.errorhandling.ErrorLogger;
import net.druidlabs.updtr.io.InOut;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class Mappings {

    private static final Set<CFMod> CURSEFORGE_MOD_MAPPINGS = ConcurrentHashMap.newKeySet();

    public static final String MAPPINGS_FILE_NAME = "Mod mappings.json";

    private static final Type MAPPING_MAP_TYPE = new TypeToken<Set<CFMod>>() {
    }.getType();

    private Mappings() {
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
        if (CURSEFORGE_MOD_MAPPINGS.isEmpty()) {
            getLocalMappings();
        }

        return CURSEFORGE_MOD_MAPPINGS;
    }

    public static Set<CFMod> getLocalMappings() {
        try {

            String mappingJsonData = InOut.readConfigData(MAPPINGS_FILE_NAME);

            Set<CFMod> localMappings = new Gson().fromJson(mappingJsonData, MAPPING_MAP_TYPE);

            if (!localMappings.equals(CURSEFORGE_MOD_MAPPINGS)) {
                CURSEFORGE_MOD_MAPPINGS.addAll(localMappings);
            }

            return localMappings;

        } catch (IOException e) {
            ErrorLogger.logError(e);

            return CURSEFORGE_MOD_MAPPINGS;
        }
    }

    public static @Nullable CFMod getMapping(Mod mod) {
        for (CFMod mapping : getLocalMappings()) {

            if (mapping.equalsMod(mod)) {
                return mapping;
            }
        }

        return null;
    }

    public static void updateMappings() {
        try {
            Set<CFMod> currentMappings = getLocalMappings();

            if (currentMappings.isEmpty()) {
                return;
            }

            Set<Mod> currentInstalledMods = InOut.loadLocalMods(null);

            if (currentInstalledMods.isEmpty()) {
                return;
            }

            CURSEFORGE_MOD_MAPPINGS.clear();

            Map<String, Integer> mappedEntry = currentMappings.stream()
                    .collect(Collectors.toMap(CFMod::getModId, CFMod::getProjectId));

            int updatedMappings = 0;

            for (Mod mod : currentInstalledMods) {
                Integer projectId = mappedEntry.get(mod.getModId());

                if (projectId != null) {
                    addMappingEntry(mod, projectId);

                    updatedMappings++;
                }
            }

            persistMappings();

            System.out.println("Updated mappings for " + updatedMappings + "/" + currentInstalledMods.size() + " mods");
        } catch (Exception e) {
            ErrorLogger.logError(e);
        }

    }

    public static void addMappingEntry(Mod mod, int projectId) {
        CFMod cfMod = CFMod.getInstance(mod, projectId);

        CURSEFORGE_MOD_MAPPINGS.add(cfMod);
    }
}
