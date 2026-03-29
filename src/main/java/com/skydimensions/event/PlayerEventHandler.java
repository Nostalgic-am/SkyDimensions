package com.skydimensions.event;

import com.skydimensions.ModDimensions;
import com.skydimensions.SkyDimensions;
import com.skydimensions.config.SkyDimensionsConfig;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.CommandEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Handles player events related to the spawn dimension.
 * Intercepts /spawn and logs dimension changes.
 */
public class PlayerEventHandler {

    /**
     * Intercept /spawn from FTB Essentials or other mods and redirect
     * to SB's spawn island teleport, which our mixin routes to the spawn dimension.
     */
    @SubscribeEvent
    public void onCommand(CommandEvent event) {
        if (!SkyDimensionsConfig.ENABLED.get()) return;

        String command = event.getParseResults().getReader().getString();
        if (!command.equals("spawn") && !command.startsWith("spawn ")) return;

        if (!(event.getParseResults().getContext().getSource().getEntity() instanceof ServerPlayer player)) return;
        if (!WorldUtil.isSkyblock(player.serverLevel())) return;

        // Cancel the original /spawn
        event.setCanceled(true);

        // Do what /sky spawn does — teleport to spawn island
        SkyblockSavedData data = SkyblockSavedData.get(player.serverLevel());
        Team spawn = data.getSpawn();
        WorldUtil.teleportToIsland(player, spawn);

        SkyDimensions.LOGGER.debug("Redirected /spawn to spawn dimension for {}",
                player.getName().getString());
    }

    @SubscribeEvent
    public void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;

        if (ModDimensions.isSpawnDimension(event.getFrom())) {
            SkyDimensions.LOGGER.debug("Player {} left spawn dimension -> {}",
                    serverPlayer.getName().getString(), event.getTo().location());
        } else if (ModDimensions.isSpawnDimension(event.getTo())) {
            SkyDimensions.LOGGER.debug("Player {} entered spawn dimension <- {}",
                    serverPlayer.getName().getString(), event.getFrom().location());
        }
    }
}