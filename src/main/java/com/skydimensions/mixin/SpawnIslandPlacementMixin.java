package com.skydimensions.mixin;

import com.skydimensions.ModDimensions;
import com.skydimensions.SkyDimensions;
import com.skydimensions.config.SkyDimensionsConfig;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.template.ConfiguredTemplate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Redirects spawn island template placement from overworld to skydimensions:spawn.
 * SB's createTeam() calls template.placeInWorld(this.level, ...) where this.level
 * is the overworld. For the spawn team only, we swap the level to our spawn dimension.
 */
@Mixin(targets = "de.melanx.skyblockbuilder.data.SkyblockSavedData", remap = false)
public class SpawnIslandPlacementMixin {

    @Redirect(method = "createTeam(Ljava/lang/String;Lde/melanx/skyblockbuilder/template/ConfiguredTemplate;)Lde/melanx/skyblockbuilder/data/Team;",
            at = @At(value = "INVOKE",
                    target = "Lde/melanx/skyblockbuilder/template/ConfiguredTemplate;placeInWorld(Lnet/minecraft/server/level/ServerLevel;Lde/melanx/skyblockbuilder/data/Team;Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructurePlaceSettings;Lnet/minecraft/util/RandomSource;I)V"))
    private void skydimensions$redirectSpawnPlacement(ConfiguredTemplate template, ServerLevel level,
                                                      Team team, StructurePlaceSettings settings,
                                                      RandomSource random, int flags) {
        if (!SkyDimensionsConfig.ENABLED.get() || !SkyDimensionsConfig.AUTO_CONFIGURE_SKYBLOCK_BUILDER.get()) {
            template.placeInWorld(level, team, settings, random, flags);
            return;
        }

        if (team.getId().equals(SkyblockSavedData.SPAWN_ID)) {
            ServerLevel spawnLevel = level.getServer().getLevel(ModDimensions.SPAWN_DIMENSION);
            if (spawnLevel != null) {
                SkyDimensions.LOGGER.info("Redirecting spawn island placement to {}", ModDimensions.SPAWN_DIMENSION.location());
                template.placeInWorld(spawnLevel, team, settings, random, flags);
                return;
            }
            SkyDimensions.LOGGER.warn("Spawn dimension not found, placing in overworld");
        }

        template.placeInWorld(level, team, settings, random, flags);
    }

    @Redirect(method = "createTeam(Ljava/lang/String;Lde/melanx/skyblockbuilder/template/ConfiguredTemplate;)Lde/melanx/skyblockbuilder/data/Team;",
            at = @At(value = "INVOKE",
                    target = "Lde/melanx/skyblockbuilder/data/SkyblockSavedData;surround(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lde/melanx/skyblockbuilder/template/ConfiguredTemplate;)V"))
    private static void skydimensions$redirectSpawnSurround(ServerLevel level,
                                                            net.minecraft.core.BlockPos center,
                                                            ConfiguredTemplate template) {
        if (!SkyDimensionsConfig.ENABLED.get() || !SkyDimensionsConfig.AUTO_CONFIGURE_SKYBLOCK_BUILDER.get()) {
            SkyblockSavedData.surround(level, center, template);
            return;
        }

        ServerLevel spawnLevel = level.getServer().getLevel(ModDimensions.SPAWN_DIMENSION);
        // Check if this is being called for spawn island (center at/near 0,0)
        // The surround call happens right after placeInWorld for the same team
        // Since we can't easily check the team here, we check if spawn dim has blocks
        if (spawnLevel != null) {
            // If we just placed in spawn dimension, surround there too
            if (!spawnLevel.getBlockState(center).isAir()) {
                SkyblockSavedData.surround(spawnLevel, center, template);
                return;
            }
        }

        SkyblockSavedData.surround(level, center, template);
    }
}