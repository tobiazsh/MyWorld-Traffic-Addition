package at.tobiazsh.myworld.traffic_addition.payload.server_actions;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;

public class GeneralActions {
    public static class ActionDefaults {
        ServerPlayer serverPlayer;
        ServerLevel world;

        public ActionDefaults(ServerPlayer serverPlayer, ServerLevel world) {
            this.serverPlayer = serverPlayer;
            this.world = world;
        }

        public static ActionDefaults ActionDefaultsBuilder(ServerPlayNetworking.Context context) {
            ServerPlayer serverPlayer = context.player();
            return new ActionDefaults(serverPlayer, serverPlayer.level());
        }
    }
}
