package com.skydimensions.compat;

import com.skydimensions.ModDimensions;
import com.skydimensions.SkyDimensions;
import com.skydimensions.config.SkyDimensionsConfig;

public class SkyGuisCompat {

    private static boolean initialized = false;

    /**
     * Initialize Sky GUIs compatibility.
     * Called during FMLCommonSetupEvent if Sky GUIs is detected.
     */
    public static void init() {
        if (initialized) return;

        if (!SkyDimensionsConfig.ENABLE_SKY_GUIS_COMPAT.get()) {
            SkyDimensions.LOGGER.info("Sky GUIs compatibility disabled in config");
            return;
        }

        // Verify SkyGUIs classes are present
        try {
            Class.forName("de.melanx.skyguis.SkyGUIs");
            Class.forName("de.melanx.skyguis.network.handler.TeleportToTeam");
            SkyDimensions.LOGGER.info("Sky GUIs compatibility verified — all handlers accessible");
        } catch (ClassNotFoundException e) {
            SkyDimensions.LOGGER.warn("Sky GUIs classes not found: {}", e.getMessage());
            return;
        }

        SkyDimensions.LOGGER.info("Sky GUIs integration active:");
        SkyDimensions.LOGGER.info("  - Spawn button → redirected to {}", ModDimensions.SPAWN_DIMENSION.location());
        SkyDimensions.LOGGER.info("  - Home/Visit buttons → overworld (unmodified)");
        SkyDimensions.LOGGER.info("  - Team creation/management → works from spawn dimension");
        SkyDimensions.LOGGER.info("NOTE: If teleport buttons fail, add '{}' to SB's teleportation dimensions config",
                ModDimensions.SPAWN_DIMENSION.location());

        initialized = true;
    }

    public static boolean isActive() {
        return initialized && SkyDimensions.isSkyGuisLoaded()
                && SkyDimensionsConfig.ENABLE_SKY_GUIS_COMPAT.get();
    }
}
