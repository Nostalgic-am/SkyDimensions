package com.skydimensions.event;

import com.skydimensions.ModDimensions;
import com.skydimensions.SkyDimensions;
import com.skydimensions.config.SkyDimensionsConfig;
import de.melanx.skyblockbuilder.config.common.TemplatesConfig;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.data.TemplateData;
import de.melanx.skyblockbuilder.template.ConfiguredTemplate;
import de.melanx.skyblockbuilder.util.TemplateUtil;
import de.melanx.skyblockbuilder.util.WorldUtil;
import de.melanx.skyblockbuilder.world.IslandPos;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

import java.util.Optional;

/**
 * Handles dimension-level events for the spawn dimension.
 * <p>
 * Key responsibility: placing the spawn island template in our spawn
 * dimension. SB normally places it in the overworld (via getConfiguredLevel).
 * We need a copy in our dimension since our mixin redirects teleportation
 * there.
 * <p>
 * NOTE: Mob spawning protection is handled by Skyblock Builder's spawn
 * protection system via our SpawnProtectionMixin.
 */
public class DimensionEventHandler {

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        if (!SkyDimensionsConfig.ENABLED.get()) return;
        if (!SkyDimensionsConfig.USE_SKYBLOCK_BUILDER_TEMPLATE.get()) return;

        MinecraftServer server = event.getServer();

        if (!WorldUtil.isSkyblock(server.overworld())) {
            SkyDimensions.LOGGER.info("Not a Skyblock world, SkyDimensions is inactive");
            return;
        }

        ServerLevel spawnLevel = server.getLevel(ModDimensions.SPAWN_DIMENSION);
        if (spawnLevel == null) {
            SkyDimensions.LOGGER.error("Spawn dimension {} not found! Check that the datapack is loaded.",
                    ModDimensions.SPAWN_DIMENSION.location());
            return;
        }

        SkyDimensions.LOGGER.info("Spawn dimension loaded: {}", ModDimensions.SPAWN_DIMENSION.location());

        SkyblockSavedData data = SkyblockSavedData.get(server.overworld());
        Team spawnTeam = data.getSpawn();

        if (hasSpawnBeenPlaced(spawnLevel)) {
            SkyDimensions.LOGGER.debug("Spawn template already exists in spawn dimension, skipping placement");
            return;
        }

        IslandPos islandPos = data.getTeamIsland(SkyblockSavedData.SPAWN_ID);
        if (islandPos == null) {
            SkyDimensions.LOGGER.error("Could not find spawn island position from SkyblockBuilder data");
            return;
        }

        ConfiguredTemplate template;
        Optional<ConfiguredTemplate> mainSpawn = TemplatesConfig.mainSpawnIsland
                .map(ConfiguredTemplate::new);

        if (mainSpawn.isPresent()) {
            template = mainSpawn.get();
        } else {
            template = TemplateData.get(server.overworld()).getConfiguredTemplate();
        }

        BlockPos center = islandPos.getCenter();
        SkyDimensions.LOGGER.info("Placing spawn template in spawn dimension at {}", center);

        try {
            template.placeInWorld(spawnLevel, spawnTeam,
                    TemplateUtil.STRUCTURE_PLACE_SETTINGS,
                    RandomSource.create(), Block.UPDATE_CLIENTS);
            SkyblockSavedData.surround(spawnLevel, center, template);

            spawnLevel.setDefaultSpawnPos(center.above(), 0.0f);

            SkyDimensions.LOGGER.info("Spawn template placed successfully in spawn dimension");
        } catch (Exception e) {
            SkyDimensions.LOGGER.error("Failed to place spawn template in spawn dimension", e);
        }
    }

    private boolean hasSpawnBeenPlaced(ServerLevel level) {
        BlockPos checkPos = level.getSharedSpawnPos();
        if (checkPos.equals(BlockPos.ZERO)) {
            checkPos = new BlockPos(0, 64, 0);
        }

        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                for (int dy = -5; dy <= 5; dy++) {
                    BlockPos p = checkPos.offset(dx, dy, dz);
                    if (!level.getBlockState(p).isAir()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}