package at.tobiazsh.myworld.traffic_addition;

import at.tobiazsh.myworld.traffic_addition.components.Blocks.*;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class ModBlocks {
    public static Block registerBlock(Block block, Identifier id, boolean shouldRegisterItem) {
        RegistryKey<Block> key = RegistryKey.of(RegistryKeys.BLOCK, id);

        if(shouldRegisterItem) {
            RegistryKey<Item> blkItmKey =  RegistryKey.of(RegistryKeys.ITEM, id);
            BlockItem blockItem = new BlockItem(block, new Item.Settings().useBlockPrefixedTranslationKey().registryKey(blkItmKey));
            Registry.register(Registries.ITEM, id, blockItem);
        }

        return Registry.register(Registries.BLOCK, key, block);
    }

    public static Identifier genId(String id) {
        return Identifier.of(MyWorldTrafficAddition.MOD_ID, id);
    }

    public static RegistryKey<Block> genKey(Identifier id) {
        return RegistryKey.of(RegistryKeys.BLOCK, id);
    }

    // GENERAL

    public static final Identifier BORDER_BLOCK_ID = genId("border_block");
    public static final RegistryKey<Block> BORDER_BLOCK_KEY = genKey(BORDER_BLOCK_ID);
    public static final Block BORDER_BLOCK = registerBlock(
            new Block(AbstractBlock.Settings.create().sounds(BlockSoundGroup.STONE).registryKey(BORDER_BLOCK_KEY)), BORDER_BLOCK_ID, true
    );

    public static final Identifier SPECIAL_BLOCK_ID = genId("special_block");
    public static final RegistryKey<Block> SPECIAL_BLOCK_KEY = genKey(SPECIAL_BLOCK_ID);
    public static final Block SPECIAL_BLOCK = registerBlock(
           new SpecialBlock(AbstractBlock.Settings.create().sounds(BlockSoundGroup.STONE).registryKey(SPECIAL_BLOCK_KEY)), SPECIAL_BLOCK_ID, true
    );

    public static final Identifier SIGN_POLE_BLOCK_ID = genId("sign_pole_block");
    public static final RegistryKey<Block> SIGN_POLE_BLOCK_KEY = genKey(SIGN_POLE_BLOCK_ID);
    public static final Block SIGN_POLE_BLOCK = registerBlock(
            new SignPoleBlock(AbstractBlock.Settings.create().strength(4.0f).nonOpaque().sounds(BlockSoundGroup.STONE).registryKey(SIGN_POLE_BLOCK_KEY)), SIGN_POLE_BLOCK_ID, true
    );



    // SIGNS

    private static final AbstractBlock.Settings SIGN_SETTINGS = AbstractBlock.Settings.create().strength(Blocks.IRON_BLOCK.getHardness()).sounds(BlockSoundGroup.STONE).nonOpaque();

    public static final Identifier TRIANGULAR_SIGN_BLOCK_ID = genId("triangular_sign_block");
    public static final RegistryKey<Block> TRIANGULAR_SIGN_BLOCK_KEY = genKey(TRIANGULAR_SIGN_BLOCK_ID);
    public static final Block TRIANGULAR_SIGN_BLOCK = registerBlock(
            new TriangularSignBlock(SIGN_SETTINGS.registryKey(TRIANGULAR_SIGN_BLOCK_KEY)), TRIANGULAR_SIGN_BLOCK_ID, true
    );

    public static final Identifier UPSIDE_DOWN_TRIANGULAR_SIGN_BLOCK_ID = genId("upsidedown_triangular_sign_block");
    public static final RegistryKey<Block> UPSIDE_DOWN_TRIANGULAR_SIGN_BLOCK_KEY = genKey(UPSIDE_DOWN_TRIANGULAR_SIGN_BLOCK_ID);
    public static Block UPSIDE_DOWN_TRIANGULAR_SIGN_BLOCK = registerBlock(
            new UpsideDownTriangularSignBlock(SIGN_SETTINGS.registryKey(UPSIDE_DOWN_TRIANGULAR_SIGN_BLOCK_KEY)), UPSIDE_DOWN_TRIANGULAR_SIGN_BLOCK_ID, true
    );

    public static final Identifier OCTAGONAL_SIGN_BLOCK_ID = genId("octagonal_sign_block");
    public static final RegistryKey<Block> OCTAGONAL_SIGN_BLOCK_KEY = genKey(OCTAGONAL_SIGN_BLOCK_ID);
    public static Block OCTAGONAL_SIGN_BLOCK = registerBlock(
            new OctagonalSignBlock(SIGN_SETTINGS.registryKey(OCTAGONAL_SIGN_BLOCK_KEY)), OCTAGONAL_SIGN_BLOCK_ID, true
    );

    public static final Identifier ROUND_SIGN_BLOCK_ID = genId("round_sign_block");
    public static final RegistryKey<Block> ROUND_SIGN_BLOCK_KEY = genKey(ROUND_SIGN_BLOCK_ID);
    public static Block ROUND_SIGN_BLOCK = registerBlock(
            new RoundSignBlock(SIGN_SETTINGS.registryKey(ROUND_SIGN_BLOCK_KEY)), ROUND_SIGN_BLOCK_ID, true
    );

    public static final Identifier CUSTOMIZABLE_SIGN_BLOCK_ID = genId("customizable_sign_block");
    public static final RegistryKey<Block> CUSTOMIZABLE_SIGN_BLOCK_KEY = genKey(CUSTOMIZABLE_SIGN_BLOCK_ID);
    public static Block CUSTOMIZABLE_SIGN_BLOCK = registerBlock(
            new CustomizableSignBlock(SIGN_SETTINGS.registryKey(CUSTOMIZABLE_SIGN_BLOCK_KEY)), CUSTOMIZABLE_SIGN_BLOCK_ID, true
    );



    // UTILS

    public static final Identifier SIGN_HOLDER_BLOCK_ID = genId("sign_holder_block");
    public static final RegistryKey<Block> SIGN_HOLDER_BLOCK_KEY = genKey(SIGN_HOLDER_BLOCK_ID);
    public static final Block SIGN_HOLDER_BLOCK = registerBlock(
            new SignHolderBlock(AbstractBlock.Settings.create().nonOpaque().sounds(BlockSoundGroup.STONE).strength(Blocks.IRON_BLOCK.getHardness()).registryKey(SIGN_HOLDER_BLOCK_KEY)), SIGN_HOLDER_BLOCK_ID, true
    );



    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(ModGroups.TRAFFIC_ADDITION_ITEM_GROUP_KEY).register((itemGroup) -> {
            itemGroup.add(ModBlocks.BORDER_BLOCK.asItem());
            itemGroup.add(ModBlocks.SIGN_POLE_BLOCK.asItem());
            itemGroup.add(ModBlocks.TRIANGULAR_SIGN_BLOCK.asItem());
            itemGroup.add(ModBlocks.UPSIDE_DOWN_TRIANGULAR_SIGN_BLOCK.asItem());
            itemGroup.add(ModBlocks.OCTAGONAL_SIGN_BLOCK.asItem());
            itemGroup.add(ModBlocks.ROUND_SIGN_BLOCK.asItem());
            itemGroup.add(ModBlocks.CUSTOMIZABLE_SIGN_BLOCK.asItem());
        });
    }
}
