package com.skydimensions.event;

import com.skydimensions.ModDimensions;
import com.skydimensions.SkyDimensions;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/*
 * This handler logs dimension changes for debugging.
 */
public class PlayerEventHandler {

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
