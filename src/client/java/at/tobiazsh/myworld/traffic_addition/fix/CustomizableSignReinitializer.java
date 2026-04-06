package at.tobiazsh.myworld.traffic_addition.fix;

import at.tobiazsh.myworld.traffic_addition.block_entities.CustomizableSignBlockEntity;
import at.tobiazsh.myworld.traffic_addition.network.CustomClientNetworking;
import at.tobiazsh.myworld.traffic_addition.utils.CustomizableSignInitializer;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class CustomizableSignReinitializer {

    /*
     * Reinitialization is happening at CHUNK_LOAD for a few reasons. Here's why:
     *
     * 1) If done at ClientBlockEntityEvents.BLOCK_ENTITY_LOAD, the Level of the BlockEntity already exists, but the
     *    data inside the BlockEntity is not loaded yet. Therefore, we can't do that.
     *
     * 2) If done at CustomizableSignBlockEntity#loadAdditional(), the data of the CustomizableSignBlockEntity already
     *    exists, but the Level is null in there because BlockEntity#setLevel() has not been called at this point.
     *    Also, we do not have access to CustomClientNetworking in there since we're in the main module.
     *
     * 3) If done at BlockEntity#setLevel(), all the data would've been loaded yet, but we have the problem that
     *    only this BlockEntity got loaded and its neighbours potentially not. Therefore, we can't do neighbour-checking
     *    and hence no reinitialization of the sign.
     *
     * 4) If we go back to Option 1, we could use latches to check if both the Level and the data of the BlockEntity
     *    are already loaded, however, this approach brings the same problem as in Option 3: All the neighbours might
     *    not have been loaded yet, so we can't do neighbour-checking.
     *
     * 5) I could've done it at BLOCK_ENTITY_UNLOAD, but that would be a really bad UX since you want to be able to see
     *    the new signs when they're loaded, not when they're not there. Actually, while I am writing this, I found
     *    another problem with this approach: Neighbours could already have been unloaded, hence still no
     *    neighbour-checking.
     *
     * That brings us to the last option, which we currently use: doing it at ClientChunkEvents.CHUNK_LOAD. This solves
     * all the problems above: When chunk is loaded, it is already available in the world, meaning all the block entites
     * have yet been loaded and the Level is already available. We can do neighbour-checking and reinitialize the sign
     * safely. This is slightly less efficient since we have to iterate through all the entites in the Chunk, which
     * brings us to a total complexity of O(n), which isn't THAT bad and won't make a big impact on performance.
     * There is, however, a possibility of edge case: If the sign spans across multiple chunks, the sign might not fully
     * see all neighbours and thus might be smaller than expected. This, however, is acceptable since this apparently
     * is relatively rare (I haven't experienced it in testing), and we can't forget that the user is still there
     * and able to jump in with manual initialization IF something were to go wrong (it's only one interaction).
     *
     * - Tobiazsh
     */

    public static void register() {
        ClientChunkEvents.CHUNK_LOAD.register((level, chunk) -> { // Reinitialize at CHUNK_LOAD
            chunk.getBlockEntities().values()
                    .stream()
                    .filter(CustomizableSignReinitializer::checkIfReinitNecessary)
                    .map(blockEntity -> (CustomizableSignBlockEntity) blockEntity)
                    .forEach(CustomizableSignReinitializer::reinitializeSign);
        });
    }

    /**
     * Checks if given BlockEntity is a sign. If so, checks if it needs a reinitialization.
     * @param blockEntity Any valid BlockEntity
     */
    private static boolean checkIfReinitNecessary(BlockEntity blockEntity) {
        if (!(blockEntity instanceof CustomizableSignBlockEntity sign))
            return false;

        return !sign.isInitialized() && !sign.getTextureData().getElementContainer().isEmpty();
    }

    /**
     * Reinitializes given {@link CustomizableSignBlockEntity}
     */
    private static void reinitializeSign(CustomizableSignBlockEntity blockEntity) {
        var initializationResult = CustomizableSignInitializer.initializeSign(blockEntity);

        FriendlyByteBuf bytes = new FriendlyByteBuf(Unpooled.buffer());
        initializationResult.encode(bytes);

        CustomClientNetworking.getInstance().sendBytesToServer(
                Identifier.fromNamespaceAndPath("myworld_traffic_addition", "customizable_sign_initialization_transmission"),
                bytes.array(),
                100,
                0 // 32 kB
        );
    }
}
