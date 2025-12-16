package at.tobiazsh.myworld.traffic_addition;


/*
 * @created 14/09/2024 (DD/MM/YYYY) - 19:13
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.command.MwtaCommand;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ModCommands {

    public static void initialize(@NotNull CommandDispatcher<CommandSourceStack> dispatcher,
                                  @Nullable CommandBuildContext access,
                                  @Nullable Commands.CommandSelection env
    ) {
        MwtaCommand.register(dispatcher);
    }
}
