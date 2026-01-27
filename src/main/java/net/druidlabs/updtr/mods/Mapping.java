package net.druidlabs.updtr.mods;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Mapping {

    private static final Map<Mod, Integer> CURSEFORGE_MOD_MAPPINGS = new ConcurrentHashMap<>();

    private Mapping() {
    }

    public static void persistMappings() {

    }

    public static Map<Mod, Integer> getMappings() {
        return null;
    }

    public static void addMappingEntry(Mod mod, int projectId) {
        CURSEFORGE_MOD_MAPPINGS.put(mod, projectId);
    }
}
