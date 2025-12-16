package at.tobiazsh.myworld.traffic_addition.blocks.utils;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;

public class RegisteredModBlock {
    private final Identifier blockId;
    private final ResourceKey<Block> blockKey;
    private final Block block;
    private Item blockItem;
    private boolean blockItemAvailable = false;

    public RegisteredModBlock(Identifier blockId, Block block) {
        this.blockId = blockId;
        this.blockKey = genKey(blockId);
        this.block = block;
    }



    // Getters

    public Identifier getId(Identifier blockId) {
        return blockId;
    }

    public ResourceKey<Block> getKey(ResourceKey<Block> blockKey) {
        return blockKey;
    }

    public Block getBlock() {
        return block;
    }

    public Item getBlockItem() {
        return blockItem;
    }



    // Other Methods

    public RegisteredModBlock register(boolean shouldRegisterItem) {
        this.blockItemAvailable = shouldRegisterItem;

        if (shouldRegisterItem) {
            this.blockItem = Registry.register(
                    BuiltInRegistries.ITEM,
                    this.blockId,

                    new BlockItem(
                            this.block,
                            new Item.Properties().useBlockDescriptionPrefix().setId(
                                    ResourceKey.create(Registries.ITEM, this.blockId)
                            )
                    )
            );
        }

        Registry.register(BuiltInRegistries.BLOCK, this.blockKey, this.block);

        return this;
    }



    // Private Methods

    public static ResourceKey<Block> genKey(Identifier id) {
        return ResourceKey.create(Registries.BLOCK, id);
    }
}
