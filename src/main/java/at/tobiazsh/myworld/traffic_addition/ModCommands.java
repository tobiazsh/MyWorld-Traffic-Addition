package at.tobiazsh.myworld.traffic_addition;


/*
 * @created 14/09/2024 (DD/MM/YYYY) - 19:13
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.command.MwtaCommand;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ModCommands {

    public static void initialize(@NotNull CommandDispatcher<ServerCommandSource> dispatcher,
                                  @Nullable CommandRegistryAccess access,
                                  @Nullable CommandManager.RegistrationEnvironment env
    ) {
        MwtaCommand.register(dispatcher);
    }
}
