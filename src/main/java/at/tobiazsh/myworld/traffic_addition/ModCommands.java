package at.tobiazsh.myworld.traffic_addition;


/*
 * @created 14/09/2024 (DD/MM/YYYY) - 19:13
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.command.MwtaCommand;
import at.tobiazsh.myworld.traffic_addition.components.custom_payloads.ShowImGuiWindow;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ModCommands {

    public static void initialize(@NotNull CommandDispatcher<ServerCommandSource> dispatcher,
                                  @Nullable CommandRegistryAccess access,
                                  @Nullable CommandManager.RegistrationEnvironment env
    ) {
        MwtaCommand.register(dispatcher);
    }
}
