package com.skydimensions.compat;

import com.skydimensions.ModDimensions;
import com.skydimensions.SkyDimensions;
import com.skydimensions.config.SkyDimensionsConfig;

/**
 * Compatibility layer for Sky GUIs ({@code de.melanx.skyguis.SkyGUIs}).
 * <p>
 * Based on analysis of SkyGUIs source code (1.21.x branch), here's how
 * each GUI feature interacts with our spawn dimension:
 * <p>
 * <b>TeleportToTeam handler</b> ({@code de.melanx.skyguis.network.handler.TeleportToTeam}):
 * <ul>
 *   <li>SPAWN type — calls {@code WorldUtil.teleportToIsland(player, spawnTeam)}
 *       → our mixin intercepts this and redirects to {@code skydimensions:spawn}</li>
 *   <li>HOME type — calls {@code WorldUtil.teleportToIsland(player, playerTeam)}
 *       → team is NOT spawn, so mixin passes through → teleports to overworld island</li>
 *   <li>VISIT type — calls {@code WorldUtil.teleportToIsland(player, visitedTeam)}
 *       → same as HOME, passes through to overworld</li>
 * </ul>
 * <p>
 * <b>CreateTeamScreenClick</b> ({@code de.melanx.skyguis.network.handler.CreateTeamScreenClick}):
 * Creates team via {@code SkyblockSavedData.createTeamAndJoin()}, then teleports
 * via standard SB methods. Works without modification.
 * <p>
 * <b>LeaveTeam</b> ({@code de.melanx.skyguis.network.handler.LeaveTeam}):
 * Calls {@code SkyblockSavedData.removePlayerFromTeam()} which puts the player
 * back in the spawn team. They'll then be teleported to spawn island via SB's
 * normal flow (which our mixin catches).
 * <p>
 * <b>AllTeamsScreen / TeamInfoScreen / TeamEditScreen</b>:
 * Client-side GUI screens that read from {@code SkyblockSavedData} synced via
 * network. Dimension-independent — works without modification.
 * <p>
 * <b>Keybind (L)</b> ({@code de.melanx.skyguis.Keybinds}):
 * Opens the GUI via network packet → server handles → works from any dimension.
 * <p>
 * <b>Permission check in TeleportToTeam</b>:
 * SkyGUIs checks {@code PermissionsConfig.Teleports.teleportationDimensions} and
 * cross-dimension permissions. Our spawn dimension must be whitelisted in SB's
 * teleportation dimension config for SkyGUIs teleport buttons to work.
 * If users report "teleportation not allowed from this dimension", they need to
 * add {@code skydimensions:spawn} to SB's allowed teleportation dimensions list.
 * <p>
 * TLDR: Everything works out of the box because SkyGUIs routes all teleportation
 * through {@code WorldUtil.teleportToIsland()} which our mixin intercepts.
 */
public class SkyGuisCompat {

    private static boolean initialized = false;

    /**
     * Initialize Sky GUIs compatibility.
     * Called during FMLCommonSetupEvent if Sky GUIs is detected.
     */
    public static void init() {
        if (initialized) return;

        if (!SkyDimensionsConfig.ENABLE_SKY_GUIS_COMPAT.get()) {
            SkyDimensions.LOGGER.info("Sky GUIs compatibility disabled in config");
            return;
        }

        // Verify SkyGUIs classes are present
        try {
            Class.forName("de.melanx.skyguis.SkyGUIs");
            Class.forName("de.melanx.skyguis.network.handler.TeleportToTeam");
            SkyDimensions.LOGGER.info("Sky GUIs compatibility verified — all handlers accessible");
        } catch (ClassNotFoundException e) {
            SkyDimensions.LOGGER.warn("Sky GUIs classes not found: {}", e.getMessage());
            return;
        }

        SkyDimensions.LOGGER.info("Sky GUIs integration active:");
        SkyDimensions.LOGGER.info("  - Spawn button → redirected to {}", ModDimensions.SPAWN_DIMENSION.location());
        SkyDimensions.LOGGER.info("  - Home/Visit buttons → overworld (unmodified)");
        SkyDimensions.LOGGER.info("  - Team creation/management → works from spawn dimension");
        SkyDimensions.LOGGER.info("NOTE: If teleport buttons fail, add '{}' to SB's teleportation dimensions config",
                ModDimensions.SPAWN_DIMENSION.location());

        initialized = true;
    }

    public static boolean isActive() {
        return initialized && SkyDimensions.isSkyGuisLoaded()
                && SkyDimensionsConfig.ENABLE_SKY_GUIS_COMPAT.get();
    }
}
