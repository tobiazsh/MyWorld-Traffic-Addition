package at.tobiazsh.myworld.traffic_addition.command;

import at.tobiazsh.myworld.traffic_addition.ModVars;
import at.tobiazsh.myworld.traffic_addition.backend.OnlineImageBackend;
import at.tobiazsh.myworld.traffic_addition.custom_payloads.ShowImGuiWindow;
import at.tobiazsh.myworld.traffic_addition.preference.ServerBlacklist;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.UuidArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.Objects;
import java.util.UUID;

public class MwtaCommand {

    private static void registerCommand(@NotNull CommandDispatcher<ServerCommandSource> dispatcher, @NotNull String name) {
        dispatcher.register(CommandManager
                .literal(name)
                .then(CommandManager.literal("toggleImGuiTestScreen").executes(MwtaCommand::toggleImGuiTestScreen))
                .then(CommandManager.literal("about").executes(MwtaCommand::openAboutWindow))
                .then(CommandManager.literal("pref").executes(MwtaCommand::openPreferencesWindow))
                .then(CommandManager.literal("customImages")
                        .then(CommandManager.literal("blacklist")
                                .requires(c -> c.hasPermissionLevel(2))
                                .executes(MwtaCommand::blacklistInfo)
                                .then(CommandManager.literal("add")
                                        .then(CommandManager.argument("player", EntityArgumentType.player()).executes(MwtaCommand::blacklistAdd))
                                        .then(CommandManager.argument("uuid", UuidArgumentType.uuid()).executes(MwtaCommand::blacklistAdd)))
                                .then(CommandManager.literal("remove")
                                        .then(CommandManager.argument("player", EntityArgumentType.player()).executes(MwtaCommand::blacklistRemove))
                                        .then(CommandManager.argument("uuid", UuidArgumentType.uuid()).executes(MwtaCommand::blacklistAdd)))
                                .then(CommandManager.literal("clear")
                                        .executes(MwtaCommand::blacklistClear))
                                .then(CommandManager.literal("list")
                                        .executes(MwtaCommand::blacklistList))
                                .then(CommandManager.literal("restore")
                                        .executes(MwtaCommand::blacklistRestore)))
                        .then(CommandManager.literal("delete")
                                .requires(c -> c.hasPermissionLevel(2))
                                .then(CommandManager.argument("uuid", UuidArgumentType.uuid())
                                        .executes(MwtaCommand::deleteImageByUuid))))
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

        Text l5 = Text.literal("For more information about commands, please visit the GitHub Page (click here)")
                .styled(style -> style.withClickEvent(new ClickEvent.OpenUrl(URI.create("https://github.com/tobiazsh/myworld_traffic_addition")))
                .withFormatting(Formatting.BLUE, Formatting.BOLD));

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



    // -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    // BLACKLIST COMMANDS ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------


    private static int blacklistInfo(@NotNull CommandContext<ServerCommandSource> context) {
        Text header = Text.literal("MyWorld Traffic Addition - Blacklist Commands\n").formatted(Formatting.BOLD, Formatting.WHITE);

        Text info = Text.literal("Use the following subcommands to manage the custom image upload blacklist:\n").formatted(Formatting.WHITE)
                        .append(Text.literal("/mwta customImages blacklist add <player> - Adds a player to the custom image upload blacklist.\n").formatted(Formatting.GRAY))
                        .append(Text.literal("/mwta customImages blacklist remove <player> - Removes a player from the custom image upload blacklist.\n").formatted(Formatting.GRAY))
                        .append(Text.literal("/mwta customImages blacklist clear - Clears the custom image upload blacklist.\n").formatted(Formatting.GRAY))
                        .append(Text.literal("/mwta customImages blacklist restore - Restores the custom image upload blacklist to its previous state before the last clear command.\n").formatted(Formatting.GRAY))
                        .append(Text.literal("/mwta customImages blacklist list - Lists all players in the custom image upload blacklist.\n").formatted(Formatting.GRAY));

        context.getSource().sendFeedback(() -> header, false);
        context.getSource().sendFeedback(() -> info, false);

        return Command.SINGLE_SUCCESS;
    }

    private static int blacklistAdd(@NotNull CommandContext<ServerCommandSource> context) {
        UUID targetUuid;

        try {
            targetUuid = EntityArgumentType.getPlayer(context, "player").getUuid();
        } catch (CommandSyntaxException | IllegalArgumentException e) {
            targetUuid = UuidArgumentType.getUuid(context, "uuid");
        }

        if (ServerBlacklist.isPlayerBlacklisted(targetUuid)) {
            UUID finalTargetUuid = targetUuid;
            context.getSource().sendFeedback(() -> Text.literal("Player with UUID " + finalTargetUuid.toString() + " is already blacklisted.").formatted(Formatting.RED), false);
            return Command.SINGLE_SUCCESS;
        }

        ServerBlacklist.addToBlacklist(targetUuid);
        UUID finalTargetUuid = targetUuid;
        context.getSource().sendFeedback(() -> Text.literal("Player with UUID " + finalTargetUuid.toString() + " has been added to the custom image upload blacklist.").formatted(Formatting.GREEN), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int blacklistRemove(@NotNull CommandContext<ServerCommandSource> context) {
        UUID targetUuid;

        try {
            targetUuid = EntityArgumentType.getPlayer(context, "player").getUuid();
        } catch (CommandSyntaxException | IllegalArgumentException e) {
            targetUuid = UuidArgumentType.getUuid(context, "uuid");
        }

        if (!ServerBlacklist.isPlayerBlacklisted(targetUuid)) {
            UUID finalTargetUuid = targetUuid;
            context.getSource().sendFeedback(() -> Text.literal("Player with UUID " + finalTargetUuid.toString() + " is not blacklisted.").formatted(Formatting.RED), false);
            return Command.SINGLE_SUCCESS;
        }

        ServerBlacklist.removeFromBlacklist(targetUuid);
        UUID finalTargetUuid = targetUuid;
        context.getSource().sendFeedback(() -> Text.literal("Player with UUID " + finalTargetUuid.toString() + " has been added to the custom image upload blacklist.").formatted(Formatting.GREEN), true);

        return Command.SINGLE_SUCCESS;
    }

    private static int blacklistClear(@NotNull CommandContext<ServerCommandSource> context) {
        ServerBlacklist.clearBlacklist();
        context.getSource().sendFeedback(() -> Text.literal("The custom image upload blacklist has been cleared.").formatted(Formatting.GREEN), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int blacklistList(@NotNull CommandContext<ServerCommandSource> context) {
        Text listHeader = Text.literal("Custom Image Upload Blacklist:\n").formatted(Formatting.BOLD, Formatting.WHITE);
        context.getSource().sendFeedback(() -> listHeader,false);

        Text info;

        String blacklistAsString = ServerBlacklist.getBlacklistAsString();

        if (blacklistAsString.isEmpty()) {
            info = Text.literal("The blacklist is currently empty.").formatted(Formatting.GRAY);
            context.getSource().sendFeedback(() -> info, false);
            return Command.SINGLE_SUCCESS;
        }

        info = Text.literal(blacklistAsString).formatted(Formatting.GRAY);
        context.getSource().sendFeedback(() -> info, false);

        return Command.SINGLE_SUCCESS;
    }

    private static int blacklistRestore(@NotNull CommandContext<ServerCommandSource> context) {
        if (!ServerBlacklist.canRestoreBlacklist()) {
            context.getSource().sendFeedback(() -> Text.literal("There is no previous blacklist state to restore.").formatted(Formatting.RED), false);
            return Command.SINGLE_SUCCESS;
        }

        ServerBlacklist.restoreBlacklist();
        context.getSource().sendFeedback(() -> Text.literal("The custom image upload blacklist has been restored to the state before the last clear command.").formatted(Formatting.GREEN), false);

        return Command.SINGLE_SUCCESS;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    // CUSTOM IMAGE COMMANDS ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    private static int deleteImageByUuid(@NotNull CommandContext<ServerCommandSource> context) {
        UUID targetImage;

        try {
            targetImage = UuidArgumentType.getUuid(context, "uuid");
        } catch (IllegalArgumentException e) {
            context.getSource().sendError(Text.literal("Invalid UUID provided!").formatted(Formatting.RED));
            return Command.SINGLE_SUCCESS;
        }

        if (!OnlineImageBackend.exists(targetImage)) {
            context.getSource().sendError(Text.literal("No image with the provided UUID exists!").formatted(Formatting.RED));
            return Command.SINGLE_SUCCESS;
        }

        OnlineImageBackend.deleteImage(targetImage);
        context.getSource().sendFeedback(() -> Text.literal("Image with UUID " + targetImage.toString() + " has been deleted successfully.").formatted(Formatting.GREEN), true);
        return Command.SINGLE_SUCCESS;
    }
}
