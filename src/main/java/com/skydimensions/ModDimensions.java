package com.skydimensions;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

/**
 * Holds dimension-related resource keys for SkyDimensions.
 * <p>
 * The spawn dimension is registered via datapack JSON and uses
 * Skyblock Builder's {@code skyblockbuilder:noise_based} chunk generator
 * to produce a void world.
 */
public final class ModDimensions {

    public static final ResourceKey<Level> SPAWN_DIMENSION =
            ResourceKey.create(Registries.DIMENSION,
                    ResourceLocation.fromNamespaceAndPath(SkyDimensions.MOD_ID, "spawn"));

    public static final ResourceKey<DimensionType> SPAWN_DIMENSION_TYPE =
            ResourceKey.create(Registries.DIMENSION_TYPE,
                    ResourceLocation.fromNamespaceAndPath(SkyDimensions.MOD_ID, "spawn"));

    public static boolean isSpawnDimension(Level level) {
        return level.dimension().equals(SPAWN_DIMENSION);
    }

    public static boolean isSpawnDimension(ResourceKey<Level> dimensionKey) {
        return dimensionKey.equals(SPAWN_DIMENSION);
    }

    private ModDimensions() {}
}
