package com.skydimensions.mixin;

import com.skydimensions.ModDimensions;
import com.skydimensions.config.SkyDimensionsConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes SB's spawn protection apply to skydimensions:spawn.
 *
 * SB checks {@code SpawnConfig.spawnDimension == level.dimension()} in isOnSpawn(),
 * which won't match our dimension. This mixin adds our dimension to that check.
 */
@Mixin(targets = "de.melanx.skyblockbuilder.SpawnProtectionEvents", remap = false)
public class SpawnProtectionMixin {

    @Inject(method = "isOnSpawn(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Z",
            at = @At("RETURN"), cancellable = true)
    private static void skydimensions$extendSpawnCheck(Level level, BlockPos blockPos,
                                                       CallbackInfoReturnable<Boolean> cir) {
        // If SB already said true, don't override
        if (cir.getReturnValue()) return;
        if (!SkyDimensionsConfig.ENABLED.get()) return;

        // Apply spawn protection to our dimension too
        if (ModDimensions.isSpawnDimension(level)) {
            int radius = de.melanx.skyblockbuilder.config.common.SpawnConfig.spawnProtectionRadius;
            net.minecraft.world.level.ChunkPos pos = new net.minecraft.world.level.ChunkPos(blockPos);
            if (radius > 0 && Math.abs(pos.x) < radius && Math.abs(pos.z) < radius
                    && !level.isOutsideBuildHeight(blockPos)) {
                cir.setReturnValue(true);
            }
        }
    }
}