package com.skydimensions.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Configuration for SkyDimensions.
 * <p>
 * Config file: config/skydimensions-common.toml
 * <p>
 * NOTE: Mob spawning, damage, hunger, and healing protection are handled by
 * Skyblock Builder's spawn protection system. Configure those in
 * SB's config/skyblockbuilder/spawn.json5 (spawnProtectionRadius + spawnProtectionEvents).
 * <p>
 * Fixed time → dimension type JSON (data/skydimensions/dimension_type/spawn.json).
 * Weather → controlled by biome choice in dimension JSON (data/skydimensions/dimension/spawn.json).
 */
public class SkyDimensionsConfig {

    public static final ModConfigSpec SPEC;

    // Core
    public static final ModConfigSpec.BooleanValue ENABLED;

    // Spawn Island
    public static final ModConfigSpec.BooleanValue USE_SKYBLOCK_BUILDER_TEMPLATE;

    // Integration
    public static final ModConfigSpec.BooleanValue AUTO_CONFIGURE_SKYBLOCK_BUILDER;
    public static final ModConfigSpec.BooleanValue ENABLE_SKY_GUIS_COMPAT;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("SkyDimensions Configuration",
                "This addon creates a dedicated void dimension for the Skyblock Builder spawn island,",
                "keeping it separate from player team islands in the overworld.",
                "",
                "Mob spawning, damage, hunger protection → SB's spawn.json5 (spawnProtectionRadius)",
                "Fixed time of day → dimension type JSON (data/skydimensions/dimension_type/spawn.json)",
                "Weather → biome choice in dimension JSON (data/skydimensions/dimension/spawn.json)");

        // --- Core ---
        builder.push("core");

        ENABLED = builder
                .comment("Master toggle for the spawn dimension redirect.",
                        "When disabled, the mod does nothing and SB uses its default behavior.")
                .define("enabled", true);

        builder.pop();

        // --- Spawn Island ---
        builder.push("island");

        USE_SKYBLOCK_BUILDER_TEMPLATE = builder
                .comment("Use Skyblock Builder's configured spawn template for the spawn dimension.",
                        "If true, SB's spawn island template will be placed in the spawn dimension.",
                        "If false, the spawn dimension will be empty (place structures manually).")
                .define("useSkyblockBuilderTemplate", true);

        builder.pop();

        // --- Integration ---
        builder.push("integration");

        AUTO_CONFIGURE_SKYBLOCK_BUILDER = builder
                .comment("Automatically redirect Skyblock Builder's spawn teleports to skydimensions:spawn.",
                        "This is the core mixin that makes the mod work.",
                        "If false, you must manually configure SB to use the spawn dimension.")
                .define("autoConfigureSkyblockBuilder", true);

        ENABLE_SKY_GUIS_COMPAT = builder
                .comment("Enable Sky GUIs compatibility verification.",
                        "Only takes effect if Sky GUIs is installed.")
                .define("enableSkyGuisCompat", true);

        builder.pop();

        SPEC = builder.build();
    }
}