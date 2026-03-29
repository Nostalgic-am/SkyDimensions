package com.skydimensions.event;

import com.skydimensions.ModDimensions;
import com.skydimensions.SkyDimensions;
import com.skydimensions.config.SkyDimensionsConfig;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/**
 * Handles dimension-level events for the spawn dimension.
 * <p>
 * Template placement is handled by SpawnIslandPlacementMixin which redirects
 * SB's createTeam() to place the spawn island in our dimension instead of the overworld.
 */
public class DimensionEventHandler {

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        if (!SkyDimensionsConfig.ENABLED.get()) return;

        MinecraftServer server = event.getServer();

        if (!WorldUtil.isSkyblock(server.overworld())) {
            SkyDimensions.LOGGER.info("Not a Skyblock world, SkyDimensions is inactive");
            return;
        }

        ServerLevel spawnLevel = server.getLevel(ModDimensions.SPAWN_DIMENSION);
        if (spawnLevel != null) {
            SkyDimensions.LOGGER.info("Spawn dimension loaded: {}", ModDimensions.SPAWN_DIMENSION.location());
        } else {
            SkyDimensions.LOGGER.error("Spawn dimension {} not found! Check that the datapack is loaded.",
                    ModDimensions.SPAWN_DIMENSION.location());
        }
    }
}