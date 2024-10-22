package at.tobiazsh.myworld.traffic_addition;


/*
 * @created 14/09/2024 (DD/MM/YYYY) - 19:13
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.components.BlockEntities.SignPoleBlockEntity;
import at.tobiazsh.myworld.traffic_addition.components.CustomPayloads.NonBlockChange.ShowAboutWindow;
import at.tobiazsh.myworld.traffic_addition.components.CustomPayloads.NonBlockChange.ShowDemoWindow;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class ModCommands {

    public static void initialize() { registerCommands(); }

    private static void registerCommands() {
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager
                .literal("myworld_traffic_addition")
                .then(CommandManager.literal("dev")
                    .then(CommandManager.literal("broad")
                        .then(CommandManager.literal("set")
                            .then(CommandManager.literal("SignPole")
                                .then(CommandManager.literal("ShouldRender")
                                    .then(CommandManager.argument("blockPosString", BoolArgumentType.bool()).executes(context -> MainCommand(context, new int[]{1, 0, 0 ,0, 0})))
                                )
                            )
                        )
                    )

                    .then(CommandManager.literal("test")
                        .then(CommandManager.literal("ImGui")
                            .then(CommandManager.literal("ShowTestScreen").executes(context -> MainCommand(context, new int[]{1, 1, 0, 0})))
                        )
                    )

                )
                .then(CommandManager.literal("about").executes(context -> MainCommand(context, new int[]{2})))
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(1))
                .executes(context -> MainCommand(context, new int[]{0})));
        }));
    }

    private static int MainCommand(CommandContext<ServerCommandSource> context, int[] functionNum) {
        ServerCommandSource source = context.getSource();
        String key = "commands." + MyWorldTrafficAddition.MOD_ID;

        switch (functionNum[0]) {
            default -> {
                source.sendFeedback(() -> Text.translatable("commands." + MyWorldTrafficAddition.MOD_ID + ".main_command.root"), false);
            }

            // Sub "dev"
            case 1 -> {
                switch (functionNum[1]) {

                    // Sub "broad"
                    case 0 -> {
                        switch (functionNum[2]) {

                            // Sub "set"
                            case 0 -> {
                                switch (functionNum[3]) {

                                    // Sub "SignPole"
                                    case 0 -> {
                                        switch (functionNum[4]) {

                                            // Sub "ShouldRender"
                                            case 0 -> {
                                                for (SignPoleBlockEntity entity : SignPoleBlockEntity.instances) {
                                                    entity.setShouldRender(BoolArgumentType.getBool(context, "blockPosString"));
                                                }

                                                final String finalKey = key + ".main_command.dev.set";

                                                source.sendFeedback(() -> Text.translatable(finalKey, "ShouldRender", (String.valueOf(SignPoleBlockEntity.instances.size()) + " Sign Poles"), (BoolArgumentType.getBool(context, "blockPosString")) ? "true" : "false"), false);
                                            }

                                        }
                                    }

                                    // Sub CustomizableSignBlock
                                    case 1 -> {

                                    }
                                }
                            }
                        }
                    }

                    case 1 -> {
                        switch (functionNum[2]) {
                            case 0 -> {
                                switch (functionNum[3]) {
                                    case 0 -> {
                                        ServerPlayerEntity player = source.getPlayer();

                                        if (player == null) return 1;

                                        ServerPlayNetworking.send(player, new ShowDemoWindow(1));
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Sub about
            case 2 -> {
                ServerPlayerEntity player = source.getPlayer();

                if (player == null) return 1;

                ServerPlayNetworking.send(player, new ShowAboutWindow(true));
            }
        }

        return 1;
    }
}
