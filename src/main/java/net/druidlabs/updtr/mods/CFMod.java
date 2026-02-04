package net.druidlabs.updtr.mods;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class CFMod extends Mod {

    private final int projectId;

    private CFMod(@NotNull Mod mod, int projectId) {
        super(mod.getModId(), mod.getModVersion(), mod.getModName(), mod.getModFileName());
        this.projectId = projectId;
    }

    public int getProjectId() {
        return projectId;
    }

    @Override
    public String getModId() {
        return super.getModId();
    }

    @Override
    public String getModName() {
        return super.getModName();
    }

    @Override
    public String getModFileName() {
        return super.getModFileName();
    }

    @Override
    public String getModVersion() {
        return super.getModVersion();
    }

    public boolean equalsMod(@NotNull Mod mod, boolean compareFileName, boolean compareVersion) {
        boolean equalsName = getModName().equals(mod.getModName());
        boolean equalsId = getModId().equals(mod.getModId());
        boolean equalsVersion = getModVersion().equals(mod.getModVersion());
        boolean equalsFileName = getModFileName().equals(mod.getModFileName());

        if (compareFileName && compareVersion) {
            return equalsName && equalsId && equalsVersion && equalsFileName;
        } else if (compareFileName) {
            return equalsName && equalsId && equalsFileName;
        } else if (compareVersion) {
            return equalsName && equalsId && equalsVersion;
        }

        return equalsFileName && equalsId && equalsVersion;
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
        return Objects.hash(super.hashCode(), projectId);
    }

    @Override
    public String toString() {
        return "Mod{" + getModName() + "-" + projectId + " | " + getModId()
                + ":" + getModVersion() + "}";
    }
}
