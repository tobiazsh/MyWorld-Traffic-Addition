package at.tobiazsh.myworld.traffic_addition.command;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import io.github.tobiazsh.jengua.LanguageSaver;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class MwtaDebugCommand {

    public static void register(@NotNull CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("mwta_debug")
                .then(ClientCommandManager.literal("saveLanguageFile").executes(MwtaDebugCommand::saveLanguageFile))
        );
    }

    // --- Command Execution Methods ---

    private static int saveLanguageFile(@NotNull CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();

        File targetFile = FabricLoader.getInstance().getConfigDir().resolve("myworld_traffic_addition/debug/lang_files/en_US.json").toFile();

        source.sendFeedback(Text.of("Saving language file to %s".formatted(targetFile.getAbsoluteFile())));

        if (targetFile.exists() && !targetFile.delete()) {
            source.sendError(Text.of("Failed to delete existing language file!"));
            return Command.SINGLE_SUCCESS;
        }

        if (!targetFile.getParentFile().exists() && !targetFile.getParentFile().mkdirs()) {
            source.sendError(Text.of("Failed to create directory for language file!"));
            return Command.SINGLE_SUCCESS;
        }

        try {
            LanguageSaver.saveLanguageFileTo(MyWorldTrafficAddition.default_en_US, targetFile);
        } catch (Exception e) {
            source.sendError(Text.of("Failed to save language file! More details in log!"));
            MyWorldTrafficAddition.LOGGER.error("Failed to save language file!", e);
            return Command.SINGLE_SUCCESS;
        }

        return Command.SINGLE_SUCCESS;
    }
}
