package net.druidlabs.updtr.mods;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ModTest {

    private final Mod MOD = Mod.getInfo(Path.of( "src","test", "resources", "moreitems-1.9.1-1.21.11.jar"));

    @Test
    void getInfo() {
        assertEquals("Andruid's items", MOD.getModName());
        assertEquals("moreitems", MOD.getModId());
        assertEquals("1.9.1-1.21.11", MOD.getModVersion());
    }

    @Test
    void isValidMod() {
        Mod invalidMod = Mod.getInfo("Hello.jar");

        assertTrue(MOD.isValidMod());
        assertFalse(invalidMod.isValidMod());
    }

    @Test
    void throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> Mod.getInfo("Hello.txt"));
    }
}