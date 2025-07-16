package at.tobiazsh.myworld.traffic_addition.blocks.utils;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class RegisteredModBlock {
    private final Identifier blockId;
    private final RegistryKey<Block> blockKey;
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

    public RegistryKey<Block> getKey(RegistryKey<Block> blockKey) {
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
                    Registries.ITEM,
                    this.blockId,

                    new BlockItem(
                            this.block,
                            new Item.Settings().useBlockPrefixedTranslationKey().registryKey(
                                    RegistryKey.of(RegistryKeys.ITEM, this.blockId)
                            )
                    )
            );
        }

        Registry.register(Registries.BLOCK, this.blockKey, this.block);

        return this;
    }



    // Private Methods

    public static RegistryKey<Block> genKey(Identifier id) {
        return RegistryKey.of(RegistryKeys.BLOCK, id);
    }
}
