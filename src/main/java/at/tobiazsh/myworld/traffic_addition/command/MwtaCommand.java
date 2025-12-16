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
import net.minecraft.server.permissions.Permissions;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.Objects;
import java.util.UUID;

public class MwtaCommand {

    private static void registerCommand(@NotNull CommandDispatcher<CommandSourceStack> dispatcher, @NotNull String name) {
        dispatcher.register(Commands
                .literal(name)
                .then(Commands.literal("toggleImGuiTestScreen").executes(MwtaCommand::toggleImGuiTestScreen))
                .then(Commands.literal("about").executes(MwtaCommand::openAboutWindow))
                .then(Commands.literal("pref").executes(MwtaCommand::openPreferencesWindow))
                .then(Commands.literal("customImages")
                        .then(Commands.literal("blacklist")
                                .requires(c -> c.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                                .executes(MwtaCommand::blacklistInfo)
                                .then(Commands.literal("add")
                                        .then(Commands.argument("player", EntityArgument.player()).executes(MwtaCommand::blacklistAdd))
                                        .then(Commands.argument("uuid", UuidArgument.uuid()).executes(MwtaCommand::blacklistAdd)))
                                .then(Commands.literal("remove")
                                        .then(Commands.argument("player", EntityArgument.player()).executes(MwtaCommand::blacklistRemove))
                                        .then(Commands.argument("uuid", UuidArgument.uuid()).executes(MwtaCommand::blacklistAdd)))
                                .then(Commands.literal("clear")
                                        .executes(MwtaCommand::blacklistClear))
                                .then(Commands.literal("list")
                                        .executes(MwtaCommand::blacklistList))
                                .then(Commands.literal("restore")
                                        .executes(MwtaCommand::blacklistRestore)))
                        .then(Commands.literal("delete")
                                .requires(c -> c.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                                .then(Commands.argument("uuid", UuidArgument.uuid())
                                        .executes(MwtaCommand::deleteImageByUuid))))
                .executes(MwtaCommand::displayInfo));
    }

    /**
     * Registers the command with the given dispatcher.
     *
     * @param dispatcher The command dispatcher to register the command with.
     */
    public static void register(@NotNull CommandDispatcher<CommandSourceStack> dispatcher) {
        registerCommand(dispatcher, "mwta");
        registerCommand(dispatcher, "myworld_traffic_addition");
    }


    // --- Command Execution Methods ---

    /**
     * Displays information about the command.
     */
    private static int displayInfo(@NotNull CommandContext<CommandSourceStack> context) {
        Component l1 = Component.literal("MyWorld Traffic Addition - Commands\n").withStyle(ChatFormatting.BOLD, ChatFormatting.WHITE);
        Component l2 = Component.literal("Attention! Some commands can and will destroy all of your signs!\n").withStyle(ChatFormatting.WHITE);
        Component l3 = Component.literal("To know more about MyWorld Traffic Addition, please execute \"/mwta about\"\n").withStyle(ChatFormatting.WHITE);
        Component l4 = Component.literal("To edit the preferences for MyWorld Traffic Addition, please execute \"/mwta pref\"\n").withStyle(ChatFormatting.WHITE);

        Component l5 = Component.literal("For more information about commands, please visit the GitHub Page (click here)")
                .withStyle(style -> style.withClickEvent(new ClickEvent.OpenUrl(URI.create("https://github.com/tobiazsh/myworld_traffic_addition")))
                .applyFormats(ChatFormatting.BLUE, ChatFormatting.BOLD));

        context.getSource().sendSystemMessage(l1);
        context.getSource().sendSystemMessage(l2);
        context.getSource().sendSystemMessage(l3);
        context.getSource().sendSystemMessage(l4);
        context.getSource().sendSystemMessage(l5);

        return Command.SINGLE_SUCCESS;
    }

    private static int toggleImGuiTestScreen(@NotNull CommandContext<CommandSourceStack> context) {
        if (!context.getSource().isPlayer()) {
            context.getSource().sendFailure(Component.literal("This command can only be executed by a player!").withStyle(ChatFormatting.RED));
            return Command.SINGLE_SUCCESS;
        }

        context.getSource().sendSuccess(() -> Component.literal("Toggling ImGui Test Screen...").withStyle(ChatFormatting.GREEN), false);

        ServerPlayNetworking.send(Objects.requireNonNull(context.getSource().getPlayer()), new ShowImGuiWindow(ModVars.ImGuiWindowIds.DEMO.ordinal()));
        return Command.SINGLE_SUCCESS;
    }

    private static int openAboutWindow(@NotNull CommandContext<CommandSourceStack> context) {
        if (!context.getSource().isPlayer()) {
            context.getSource().sendFailure(Component.literal("This command can only be executed by a player!").withStyle(ChatFormatting.RED));
            return Command.SINGLE_SUCCESS;
        }

        context.getSource().sendSuccess(() -> Component.literal("Opening About Window...").withStyle(ChatFormatting.GREEN), false);

        ServerPlayNetworking.send(Objects.requireNonNull(context.getSource().getPlayer()), new ShowImGuiWindow(ModVars.ImGuiWindowIds.ABOUT.ordinal()));
        return Command.SINGLE_SUCCESS;
    }

    private static int openPreferencesWindow(@NotNull CommandContext<CommandSourceStack> context) {
        if (!context.getSource().isPlayer()) {
            context.getSource().sendFailure(Component.literal("This command can only be executed by a player!").withStyle(ChatFormatting.RED));
            return Command.SINGLE_SUCCESS;
        }

        context.getSource().sendSuccess(() -> Component.literal("Opening Preferences Window...").withStyle(ChatFormatting.GREEN), false);

        ServerPlayNetworking.send(Objects.requireNonNull(context.getSource().getPlayer()), new ShowImGuiWindow(ModVars.ImGuiWindowIds.PREF.ordinal()));
        return Command.SINGLE_SUCCESS;
    }



    // -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    // BLACKLIST COMMANDS ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------


    private static int blacklistInfo(@NotNull CommandContext<CommandSourceStack> context) {
        Component header = Component.literal("MyWorld Traffic Addition - Blacklist Commands\n").withStyle(ChatFormatting.BOLD, ChatFormatting.WHITE);

        Component info = Component.literal("Use the following subcommands to manage the custom image upload blacklist:\n").withStyle(ChatFormatting.WHITE)
                        .append(Component.literal("/mwta customImages blacklist add <player> - Adds a player to the custom image upload blacklist.\n").withStyle(ChatFormatting.GRAY))
                        .append(Component.literal("/mwta customImages blacklist remove <player> - Removes a player from the custom image upload blacklist.\n").withStyle(ChatFormatting.GRAY))
                        .append(Component.literal("/mwta customImages blacklist clear - Clears the custom image upload blacklist.\n").withStyle(ChatFormatting.GRAY))
                        .append(Component.literal("/mwta customImages blacklist restore - Restores the custom image upload blacklist to its previous state before the last clear command.\n").withStyle(ChatFormatting.GRAY))
                        .append(Component.literal("/mwta customImages blacklist list - Lists all players in the custom image upload blacklist.\n").withStyle(ChatFormatting.GRAY));

        context.getSource().sendSuccess(() -> header, false);
        context.getSource().sendSuccess(() -> info, false);

        return Command.SINGLE_SUCCESS;
    }

    private static int blacklistAdd(@NotNull CommandContext<CommandSourceStack> context) {
        UUID targetUuid;

        try {
            targetUuid = EntityArgument.getPlayer(context, "player").getUUID();
        } catch (CommandSyntaxException | IllegalArgumentException e) {
            targetUuid = UuidArgument.getUuid(context, "uuid");
        }

        if (ServerBlacklist.isPlayerBlacklisted(targetUuid)) {
            UUID finalTargetUuid = targetUuid;
            context.getSource().sendSuccess(() -> Component.literal("Player with UUID " + finalTargetUuid + " is already blacklisted.").withStyle(ChatFormatting.RED), false);
            return Command.SINGLE_SUCCESS;
        }

        ServerBlacklist.addToBlacklist(targetUuid);
        UUID finalTargetUuid = targetUuid;
        context.getSource().sendSuccess(() -> Component.literal("Player with UUID " + finalTargetUuid + " has been added to the custom image upload blacklist.").withStyle(ChatFormatting.GREEN), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int blacklistRemove(@NotNull CommandContext<CommandSourceStack> context) {
        UUID targetUuid;

        try {
            targetUuid = EntityArgument.getPlayer(context, "player").getUUID();
        } catch (CommandSyntaxException | IllegalArgumentException e) {
            targetUuid = UuidArgument.getUuid(context, "uuid");
        }

        if (!ServerBlacklist.isPlayerBlacklisted(targetUuid)) {
            UUID finalTargetUuid = targetUuid;
            context.getSource().sendSuccess(() -> Component.literal("Player with UUID " + finalTargetUuid + " is not blacklisted.").withStyle(ChatFormatting.RED), false);
            return Command.SINGLE_SUCCESS;
        }

        ServerBlacklist.removeFromBlacklist(targetUuid);
        UUID finalTargetUuid = targetUuid;
        context.getSource().sendSuccess(() -> Component.literal("Player with UUID " + finalTargetUuid + " has been added to the custom image upload blacklist.").withStyle(ChatFormatting.GREEN), true);

        return Command.SINGLE_SUCCESS;
    }

    private static int blacklistClear(@NotNull CommandContext<CommandSourceStack> context) {
        ServerBlacklist.clearBlacklist();
        context.getSource().sendSuccess(() -> Component.literal("The custom image upload blacklist has been cleared.").withStyle(ChatFormatting.GREEN), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int blacklistList(@NotNull CommandContext<CommandSourceStack> context) {
        Component listHeader = Component.literal("Custom Image Upload Blacklist:\n").withStyle(ChatFormatting.BOLD, ChatFormatting.WHITE);
        context.getSource().sendSuccess(() -> listHeader,false);

        Component info;

        String blacklistAsString = ServerBlacklist.getBlacklistAsString();

        if (blacklistAsString.isEmpty()) {
            info = Component.literal("The blacklist is currently empty.").withStyle(ChatFormatting.GRAY);
            context.getSource().sendSuccess(() -> info, false);
            return Command.SINGLE_SUCCESS;
        }

        info = Component.literal(blacklistAsString).withStyle(ChatFormatting.GRAY);
        context.getSource().sendSuccess(() -> info, false);

        return Command.SINGLE_SUCCESS;
    }

    private static int blacklistRestore(@NotNull CommandContext<CommandSourceStack> context) {
        if (!ServerBlacklist.canRestoreBlacklist()) {
            context.getSource().sendSuccess(() -> Component.literal("There is no previous blacklist state to restore.").withStyle(ChatFormatting.RED), false);
            return Command.SINGLE_SUCCESS;
        }

        ServerBlacklist.restoreBlacklist();
        context.getSource().sendSuccess(() -> Component.literal("The custom image upload blacklist has been restored to the state before the last clear command.").withStyle(ChatFormatting.GREEN), false);

        return Command.SINGLE_SUCCESS;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    // CUSTOM IMAGE COMMANDS ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    private static int deleteImageByUuid(@NotNull CommandContext<CommandSourceStack> context) {
        UUID targetImage;

        try {
            targetImage = UuidArgument.getUuid(context, "uuid");
        } catch (IllegalArgumentException e) {
            context.getSource().sendFailure(Component.literal("Invalid UUID provided!").withStyle(ChatFormatting.RED));
            return Command.SINGLE_SUCCESS;
        }

        if (!OnlineImageBackend.exists(targetImage)) {
            context.getSource().sendFailure(Component.literal("No image with the provided UUID exists!").withStyle(ChatFormatting.RED));
            return Command.SINGLE_SUCCESS;
        }

        OnlineImageBackend.deleteImage(targetImage);
        context.getSource().sendSuccess(() -> Component.literal("Image with UUID " + targetImage + " has been deleted successfully.").withStyle(ChatFormatting.GREEN), true);
        return Command.SINGLE_SUCCESS;
    }
}
