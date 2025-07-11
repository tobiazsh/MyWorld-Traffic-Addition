package at.tobiazsh.myworld.traffic_addition;

import at.tobiazsh.myworld.traffic_addition.command.MwtaDebugCommand;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ModCommandsClient {

    public static void initialize(
            @NotNull CommandDispatcher<FabricClientCommandSource> dispatcher,
            @Nullable CommandRegistryAccess access
    ) {
        MwtaDebugCommand.register(dispatcher);
    }
}
