package com.skydimensions.util;

import com.skydimensions.ModDimensions;
import com.skydimensions.SkyDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;

/**
 * Utility for direct teleportation to the spawn dimension.
 * <p>
 * Most player teleportation is handled by SkyblockBuilder's own
 * {@code WorldUtil.teleportToIsland()} via our mixin. This utility
 * is used for admin commands and edge cases.
 */
public final class TeleportHelper {

    /**
     * Teleport a player to the spawn dimension at its shared spawn position.
     */
    public static boolean teleportToSpawnDimension(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return false;

        ServerLevel spawnLevel = server.getLevel(ModDimensions.SPAWN_DIMENSION);
        if (spawnLevel == null) {
            SkyDimensions.LOGGER.warn("Spawn dimension not found");
            return false;
        }

        BlockPos spawnPos = spawnLevel.getSharedSpawnPos();
        if (spawnPos.equals(BlockPos.ZERO)) {
            spawnPos = new BlockPos(0, 64, 0);
        }

        try {
            player.teleportTo(spawnLevel,
                    spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5,
                    player.getYRot(), player.getXRot());
            return true;
        } catch (Exception e) {
            SkyDimensions.LOGGER.error("Failed to teleport player {}", player.getName().getString(), e);
            return false;
        }
    }

    /**
     * Check if a player is currently in the spawn dimension.
     */
    public static boolean isInSpawnDimension(ServerPlayer player) {
        return ModDimensions.isSpawnDimension(player.level().dimension());
    }

    @Nullable
    public static ServerLevel getSpawnDimension(MinecraftServer server) {
        return server.getLevel(ModDimensions.SPAWN_DIMENSION);
    }

    private TeleportHelper() {}
}
