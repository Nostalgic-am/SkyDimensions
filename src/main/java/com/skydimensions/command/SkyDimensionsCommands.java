package com.skydimensions.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.skydimensions.ModDimensions;
import com.skydimensions.SkyDimensions;
import com.skydimensions.config.SkyDimensionsConfig;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class SkyDimensionsCommands {

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("skydimensions")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.literal("status")
                                .executes(SkyDimensionsCommands::statusCommand))
        );
    }

    private static int statusCommand(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        boolean enabled = SkyDimensionsConfig.ENABLED.get();
        boolean isSkyblock = WorldUtil.isSkyblock(source.getServer().overworld());
        ServerLevel spawnLevel = source.getServer().getLevel(ModDimensions.SPAWN_DIMENSION);
        boolean dimensionLoaded = spawnLevel != null;
        int playersInSpawn = dimensionLoaded ? spawnLevel.players().size() : 0;

        final int teamCount;
        if (isSkyblock) {
            SkyblockSavedData data = SkyblockSavedData.get(source.getServer().overworld());
            teamCount = (int) data.getTeams().stream().filter(t -> !t.isSpawn()).count();
        } else {
            teamCount = 0;
        }

        source.sendSuccess(() -> Component.literal("=== SkyDimensions Status ===")
                .withStyle(ChatFormatting.GOLD), false);
        source.sendSuccess(() -> Component.literal("Enabled: ")
                .append(Component.literal(enabled ? "Yes" : "No")
                        .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED)), false);
        source.sendSuccess(() -> Component.literal("Skyblock World: ")
                .append(Component.literal(isSkyblock ? "Yes" : "No")
                        .withStyle(isSkyblock ? ChatFormatting.GREEN : ChatFormatting.RED)), false);
        source.sendSuccess(() -> Component.literal("Spawn Dimension: ")
                .append(Component.literal(dimensionLoaded ? "Loaded" : "NOT FOUND")
                        .withStyle(dimensionLoaded ? ChatFormatting.GREEN : ChatFormatting.RED)), false);
        source.sendSuccess(() -> Component.literal("Players in Spawn Dim: ")
                .append(Component.literal(String.valueOf(playersInSpawn))
                        .withStyle(ChatFormatting.AQUA)), false);
        source.sendSuccess(() -> Component.literal("Active Teams: ")
                .append(Component.literal(String.valueOf(teamCount))
                        .withStyle(ChatFormatting.AQUA)), false);
        source.sendSuccess(() -> Component.literal("Sky GUIs: ")
                .append(Component.literal(SkyDimensions.isSkyGuisLoaded() ? "Loaded" : "Not installed")
                        .withStyle(SkyDimensions.isSkyGuisLoaded() ? ChatFormatting.GREEN : ChatFormatting.GRAY)), false);

        if (dimensionLoaded) {
            source.sendSuccess(() -> Component.literal("Spawn Pos: ")
                    .append(Component.literal(spawnLevel.getSharedSpawnPos().toShortString())
                            .withStyle(ChatFormatting.WHITE)), false);
        }

        return 1;
    }
}