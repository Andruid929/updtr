package net.druidlabs.updtr.mods;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class CFMod {

    private final int projectId;
    private final String modName;
    private final String modId;

    private CFMod(@NotNull Mod mod, int projectId) {
        this.projectId = projectId;
        this.modName = mod.getModName();
        this.modId = mod.getModId();
    }

    public int getProjectId() {
        return projectId;
    }

    public String getModId() {
        return modId;
    }

    public String getModName() {
        return modName;
    }

    public boolean equalsMod(@NotNull Mod mod) {
        boolean equalsName = getModName().equals(mod.getModName());
        boolean equalsId = getModId().equals(mod.getModId());

        return equalsId && equalsName;
    }

    @Contract("_, _ -> new")
    public static @NotNull CFMod getInstance(Mod mod, int projectId) {
        if (projectId < 1) {
            throw new IllegalArgumentException("Mod Project ID cannot be zero or negative, got " + projectId);
        }

        return new CFMod(mod, projectId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modName, modId, projectId);
    }

    @Override
    public String toString() {
        return "CFMod{" + getModName() + " | " + getModId() + ": " + getProjectId() + "}";
    }
}
