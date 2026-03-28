package com.skydimensions;

import com.skydimensions.compat.SkyGuisCompat;
import com.skydimensions.config.SkyDimensionsConfig;
import com.skydimensions.event.DimensionEventHandler;
import com.skydimensions.event.PlayerEventHandler;
import com.skydimensions.command.SkyDimensionsCommands;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(SkyDimensions.MOD_ID)
public class SkyDimensions {

    public static final String MOD_ID = "skydimensions";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static boolean skyGuisLoaded = false;

    public SkyDimensions(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("SkyDimensions initializing - spawn dimension addon for Skyblock Builder");

        modContainer.registerConfig(ModConfig.Type.COMMON, SkyDimensionsConfig.SPEC);

        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.register(new PlayerEventHandler());
        NeoForge.EVENT_BUS.register(new DimensionEventHandler());
        NeoForge.EVENT_BUS.register(new SkyDimensionsCommands());
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            skyGuisLoaded = ModList.get().isLoaded("skyguis");
            if (skyGuisLoaded) {
                LOGGER.info("Sky GUIs detected - GUI-based teleports will route through the spawn dimension mixin");
                SkyGuisCompat.init();
            }
            LOGGER.info("SkyDimensions setup complete. Spawn dimension: {}", ModDimensions.SPAWN_DIMENSION.location());
        });
    }

    public static boolean isSkyGuisLoaded() {
        return skyGuisLoaded;
    }
}
