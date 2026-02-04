package net.druidlabs.updtr.mods;

public enum ModLoaders {

    FORGE("Forge", 3),
    FABRIC("Fabric", 4),
    QUILT("Quilt", 5),
    NEO_FORGE("NeoForge", 6);

    private final String name;
    private final int modLoaderId;

    ModLoaders(String name, int modLoaderId) {
        this.name = name;
        this.modLoaderId = modLoaderId;
    }

    public String getName() {
        return name;
    }

    public int getModLoaderId() {
        return modLoaderId;
    }
}
