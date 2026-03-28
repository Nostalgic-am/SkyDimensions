package com.skydimensions.mixin;

import com.skydimensions.ModDimensions;
import com.skydimensions.SkyDimensions;
import com.skydimensions.config.SkyDimensionsConfig;
import de.melanx.skyblockbuilder.config.common.PermissionsConfig;
import de.melanx.skyblockbuilder.config.common.TemplatesConfig;
import de.melanx.skyblockbuilder.data.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Mixin into {@link de.melanx.skyblockbuilder.util.WorldUtil}.
 * <p>
 * Targets {@code teleportToIsland(ServerPlayer, Team)} which is the single
 * method ALL teleportation in SkyblockBuilder routes through:
 * <ul>
 *   <li>{@code /skyblock spawn} and {@code /sky spawn} — SpawnCommand</li>
 *   <li>{@code /skyblock home} and {@code /sky home} — HomeCommand</li>
 *   <li>{@code /skyblock visit} and {@code /sky visit} — VisitCommand</li>
 *   <li>SkyGUIs TeleportToTeam handler (spawn/home/visit buttons)</li>
 *   <li>EventListener.onPlayerJoin — first join and teamless rejoin</li>
 *   <li>EventListener.onRespawn — death respawn</li>
 *   <li>Invite acceptance teleport</li>
 * </ul>
 * <p>
 * Strategy: When the target team {@code isSpawn()}, redirect the teleport to
 * {@code skydimensions:spawn} instead of the configured level (overworld).
 * Non-spawn team teleports pass through unmodified — islands stay in the overworld.
 */
@Mixin(targets = "de.melanx.skyblockbuilder.util.WorldUtil", remap = false)
public class WorldUtilMixin {

    /**
     * Intercept teleportToIsland at HEAD. If the destination team is the spawn
     * team and our mod is enabled, perform the teleport to the spawn dimension
     * ourselves and cancel the original method.
     */
    @Inject(method = "teleportToIsland", at = @At("HEAD"), cancellable = true)
    private static void skydimensions$redirectSpawnTeleport(ServerPlayer player, Team team,
                                                            CallbackInfo ci) {
        if (!SkyDimensionsConfig.ENABLED.get() || !SkyDimensionsConfig.AUTO_CONFIGURE_SKYBLOCK_BUILDER.get()) {
            return;
        }

        // Only redirect spawn team teleports
        if (!team.isSpawn()) {
            return;
        }

        MinecraftServer server = player.getServer();
        if (server == null) return;

        ServerLevel spawnLevel = server.getLevel(ModDimensions.SPAWN_DIMENSION);
        if (spawnLevel == null) {
            SkyDimensions.LOGGER.warn("Spawn dimension not loaded, falling back to SB default");
            return;
        }

        // Find a valid spawn position from the team's possible spawns
        // This mirrors WorldUtil.validPosition() logic
        List<TemplatesConfig.Spawn> spawns = new ArrayList<>(team.getPossibleSpawns());
        TemplatesConfig.Spawn spawnPos = null;

        Random random = new Random();
        while (!spawns.isEmpty()) {
            TemplatesConfig.Spawn candidate = spawns.get(random.nextInt(spawns.size()));
            // Check if position is valid in our dimension
            if (isValidSpawn(spawnLevel, candidate.pos())) {
                spawnPos = candidate;
                break;
            }
            spawns.remove(candidate);
        }

        // Fallback: use any available spawn position
        if (spawnPos == null) {
            spawnPos = team.getPossibleSpawns().stream().findAny()
                    .orElse(new TemplatesConfig.Spawn(
                            team.getIsland().getCenter(),
                            de.melanx.skyblockbuilder.util.WorldUtil.SpawnDirection.SOUTH));
        }

        // Teleport to our spawn dimension
        player.teleportTo(spawnLevel,
                spawnPos.pos().getX() + 0.5,
                spawnPos.pos().getY() + 0.2,
                spawnPos.pos().getZ() + 0.5,
                spawnPos.direction().getYRot(), 0);

        // Set respawn point if player doesn't have one
        if (player.getRespawnPosition() == null && team.hasPlayer(player)) {
            player.setRespawnPosition(spawnLevel.dimension(),
                    spawnPos.pos(), spawnPos.direction().getYRot(), true, false);
        }

        if (PermissionsConfig.Teleports.negateFallDamage) {
            player.fallDistance = 0;
        }

        SkyDimensions.LOGGER.debug("Redirected spawn teleport for {} to {}",
                player.getName().getString(), ModDimensions.SPAWN_DIMENSION.location());

        // Cancel original method
        ci.cancel();
    }

    /**
     * Simple spawn validity check — matches WorldUtil.isValidSpawn() logic.
     */
    private static boolean isValidSpawn(ServerLevel level, net.minecraft.core.BlockPos pos) {
        return pos.getY() >= level.getMinBuildHeight()
                && pos.getY() <= level.getMaxBuildHeight()
                && (level.getBlockState(pos.below()).canOcclude())
                && !level.getBlockState(pos).canOcclude()
                && !level.getBlockState(pos.above()).canOcclude();
    }
}
