package at.tobiazsh.myworld.traffic_addition.components.CustomPayloads.Actions;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class GeneralActions {
    public static class ActionDefaults {
        ServerPlayerEntity serverPlayer;
        ServerWorld world;

        public ActionDefaults(ServerPlayerEntity serverPlayer, ServerWorld world) {
            this.serverPlayer = serverPlayer;
            this.world = world;
        }

        public static ActionDefaults ActionDefaultsBuilder(ServerPlayNetworking.Context context) {
            ServerPlayerEntity serverPlayer = context.player();
            return new ActionDefaults(serverPlayer, serverPlayer.getServerWorld());
        }
    }
}
