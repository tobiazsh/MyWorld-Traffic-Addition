package at.tobiazsh.myworld.traffic_addition.command;

import at.tobiazsh.myworld.traffic_addition.ModCommands;
import at.tobiazsh.myworld.traffic_addition.ModVars;
import at.tobiazsh.myworld.traffic_addition.components.custom_payloads.ShowImGuiWindow;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class MwtaCommand {

    private static void registerCommand(@NotNull CommandDispatcher<ServerCommandSource> dispatcher, @NotNull String name) {
        dispatcher.register(CommandManager
                .literal(name)
                .then(CommandManager.literal("ToggleImGuiTestScreen").executes(MwtaCommand::toggleImGuiTestScreen))
                .then(CommandManager.literal("about").executes(MwtaCommand::openAboutWindow))
                .then(CommandManager.literal("pref").executes(MwtaCommand::openPreferencesWindow))
                .executes(MwtaCommand::displayInfo));
    }

    /**
     * Registers the command with the given dispatcher.
     *
     * @param dispatcher The command dispatcher to register the command with.
     */
    public static void register(@NotNull CommandDispatcher<ServerCommandSource> dispatcher) {
        registerCommand(dispatcher, "mwta");
        registerCommand(dispatcher, "myworld_traffic_addition");
    }


    // --- Command Execution Methods ---

    /**
     * Displays information about the command.
     */
    private static int displayInfo(@NotNull CommandContext<ServerCommandSource> context) {
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

        return Command.SINGLE_SUCCESS;
    }

    private static int toggleImGuiTestScreen(@NotNull CommandContext<ServerCommandSource> context) {
        if (!context.getSource().isExecutedByPlayer()) {
            context.getSource().sendError(Text.literal("This command can only be executed by a player!").formatted(Formatting.RED));
            return Command.SINGLE_SUCCESS;
        }

        context.getSource().sendFeedback(() -> Text.literal("Toggling ImGui Test Screen...").formatted(Formatting.GREEN), false);

        ServerPlayNetworking.send(Objects.requireNonNull(context.getSource().getPlayer()), new ShowImGuiWindow(ModVars.ImGuiWindowIds.DEMO.ordinal()));
        return Command.SINGLE_SUCCESS;
    }

    private static int openAboutWindow(@NotNull CommandContext<ServerCommandSource> context) {
        if (!context.getSource().isExecutedByPlayer()) {
            context.getSource().sendError(Text.literal("This command can only be executed by a player!").formatted(Formatting.RED));
            return Command.SINGLE_SUCCESS;
        }

        context.getSource().sendFeedback(() -> Text.literal("Opening About Window...").formatted(Formatting.GREEN), false);

        ServerPlayNetworking.send(Objects.requireNonNull(context.getSource().getPlayer()), new ShowImGuiWindow(ModVars.ImGuiWindowIds.ABOUT.ordinal()));
        return Command.SINGLE_SUCCESS;
    }

    private static int openPreferencesWindow(@NotNull CommandContext<ServerCommandSource> context) {
        if (!context.getSource().isExecutedByPlayer()) {
            context.getSource().sendError(Text.literal("This command can only be executed by a player!").formatted(Formatting.RED));
            return Command.SINGLE_SUCCESS;
        }

        context.getSource().sendFeedback(() -> Text.literal("Opening Preferences Window...").formatted(Formatting.GREEN), false);

        ServerPlayNetworking.send(Objects.requireNonNull(context.getSource().getPlayer()), new ShowImGuiWindow(ModVars.ImGuiWindowIds.PREF.ordinal()));
        return Command.SINGLE_SUCCESS;
    }
}
