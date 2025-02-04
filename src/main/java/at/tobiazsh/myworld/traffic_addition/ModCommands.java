package at.tobiazsh.myworld.traffic_addition;


/*
 * @created 14/09/2024 (DD/MM/YYYY) - 19:13
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.components.CustomPayloads.ShowImGuiWindow;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Objects;

public class ModCommands {

    public static void initialize() { registerCommands(); }

    private static void registerCommands() {
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager
                .literal("mwta")
                .then(CommandManager.literal("ToggleImGuiTestScreen").executes(context -> {
                    ServerPlayNetworking.send(Objects.requireNonNull(context.getSource().getPlayer()), new ShowImGuiWindow(ModVars.ImGuiWindowIds.DEMO.ordinal()));
                    return 0;
                }))
                .then(CommandManager.literal("about").executes(context -> {
                    ServerPlayNetworking.send(Objects.requireNonNull(context.getSource().getPlayer()), new ShowImGuiWindow(ModVars.ImGuiWindowIds.ABOUT.ordinal()));
                    return 0;
                }))
                .then(CommandManager.literal("pref").executes(context -> {
                    ServerPlayNetworking.send(Objects.requireNonNull(context.getSource().getPlayer()), new ShowImGuiWindow(ModVars.ImGuiWindowIds.PREF.ordinal()));
                    return 0;
                }))
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(1))
                .executes(ModCommands::displayInfo));
        }));
    }

    private static int displayInfo(CommandContext<ServerCommandSource> context) {
        Text l1 = Text.literal("MyWorld Traffic Addition - Commands\n").formatted(Formatting.BOLD, Formatting.WHITE);
        Text l2 = Text.literal("Attention! Some commands can and will destroy all of your signs!\n").formatted(Formatting.WHITE);
        Text l3 = Text.literal("To know more about MyWorld Traffic Addition, please execute \"/mwta about\"\n").formatted(Formatting.WHITE);
        Text l4 = Text.literal("To edit the preferences for MyWorld Traffic Addition, please execute \"/mwta pref\"\n").formatted(Formatting.WHITE);
        Text l5 = Text.literal("For more information about commands, please visit the GitHub Page (click here)").styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/tobiazsh/myworld_traffic_addition")).withFormatting(Formatting.BLUE, Formatting.BOLD));

        context.getSource().sendMessage(l1);
        context.getSource().sendMessage(l2);
        context.getSource().sendMessage(l3);
        context.getSource().sendMessage(l4);
        context.getSource().sendMessage(l5);

        return 0;
    }
}
