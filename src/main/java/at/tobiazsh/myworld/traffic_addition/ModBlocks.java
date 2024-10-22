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
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class ModBlocks {
    public static Block registerBlock(Block block, String name, boolean shouldRegisterItem) {
        Identifier id = Identifier.of(MyWorldTrafficAddition.MOD_ID, name);

        if(shouldRegisterItem) {
            BlockItem blockItem = new BlockItem(block, new Item.Settings());
            Registry.register(Registries.ITEM, id, blockItem);
        }

        return Registry.register(Registries.BLOCK, id, block);
    }



    // GENERAL

    public static final Block BORDER_BLOCK = registerBlock(
            new Block(AbstractBlock.Settings.create().sounds(BlockSoundGroup.STONE)), "border_block", true
    );

    public static final Block SPECIAL_BLOCK = registerBlock(
           new SpecialBlock(AbstractBlock.Settings.create().sounds(BlockSoundGroup.STONE)), "special_block", true
    );

    public static final Block SIGN_POLE_BLOCK = registerBlock(
            new SignPoleBlock(AbstractBlock.Settings.create().strength(4.0f).nonOpaque().sounds(BlockSoundGroup.STONE)), "sign_pole_block", true
    );



    // SIGNS

    private static final AbstractBlock.Settings SIGN_SETTINGS = AbstractBlock.Settings.create().strength(Blocks.IRON_BLOCK.getHardness()).sounds(BlockSoundGroup.STONE).nonOpaque();

    public static final Block TRIANGULAR_SIGN_BLOCK = registerBlock(
            new TriangularSignBlock(SIGN_SETTINGS), "triangular_sign_block", true
    );

    public static Block UPSIDE_DOWN_TRIANGULAR_SIGN_BLOCK = registerBlock(
            new UpsideDownTriangularSignBlock(SIGN_SETTINGS), "upsidedown_triangular_sign_block", true
    );

    public static Block OCTAGONAL_SIGN_BLOCK = registerBlock(
            new OctagonalSignBlock(SIGN_SETTINGS), "octagonal_sign_block", true
    );

    public static Block ROUND_SIGN_BLOCK = registerBlock(
            new RoundSignBlock(SIGN_SETTINGS), "round_sign_block", true
    );

    public static Block CUSTOMIZABLE_SIGN_BLOCK = registerBlock(
            new CustomizableSignBlock(SIGN_SETTINGS), "customizable_sign_block", true
    );



    // UTILS

    public static final Block SIGN_HOLDER_BLOCK = registerBlock(
            new SignHolderBlock(AbstractBlock.Settings.create().nonOpaque().sounds(BlockSoundGroup.STONE).strength(Blocks.IRON_BLOCK.getHardness())), "sign_holder_block", true
    );


    
    // ROAD ARROWS

    private static final AbstractBlock.Settings concreteSettings = AbstractBlock.Settings.create().sounds(BlockSoundGroup.STONE).strength(1.8f, 1.8f);

    public static Block AUSTRIA_ROAD_ARROW_STEM_BLOCK_1 = registerBlock(
            new Block(concreteSettings), "austria_road_arrow_stem_block_1", true
    );

    public static Block AUSTRIA_ROAD_ARROW_STEM_BLOCK_2 = registerBlock(
            new Block(concreteSettings), "austria_road_arrow_stem_block_2", true
    );

    public static Block AUSTRIA_ROAD_ARROW_STEM_BLOCK_3 = registerBlock(
            new Block(concreteSettings), "austria_road_arrow_stem_block_3", true
    );

    public static Block AUSTRIA_ROAD_ARROW_STRAIGHT_BLOCK_1 = registerBlock(
            new Block(concreteSettings), "austria_road_arrow_straight_block_1", true
    );

    public static Block AUSTRIA_ROAD_ARROW_STRAIGHT_BLOCK_2 = registerBlock(
            new Block(concreteSettings), "austria_road_arrow_straight_block_2", true
    );


    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(ModGroups.TRAFFIC_ADDITION_ITEM_GROUP_KEY).register((itemGroup) -> {
            itemGroup.add(ModBlocks.BORDER_BLOCK.asItem());
            itemGroup.add(ModBlocks.SPECIAL_BLOCK.asItem());
            itemGroup.add(ModBlocks.SIGN_POLE_BLOCK.asItem());
            itemGroup.add(ModBlocks.TRIANGULAR_SIGN_BLOCK.asItem());
            itemGroup.add(ModBlocks.UPSIDE_DOWN_TRIANGULAR_SIGN_BLOCK.asItem());
            itemGroup.add(ModBlocks.OCTAGONAL_SIGN_BLOCK.asItem());
            itemGroup.add(ModBlocks.ROUND_SIGN_BLOCK.asItem());
        });

        ItemGroupEvents.modifyEntriesEvent(ModGroups.TRAFFIC_ADDITION_AUSTRIA_GROUP_KEY).register((itemGroup) -> {
            itemGroup.add(ModBlocks.AUSTRIA_ROAD_ARROW_STEM_BLOCK_1.asItem());
            itemGroup.add(ModBlocks.AUSTRIA_ROAD_ARROW_STEM_BLOCK_2.asItem());
            itemGroup.add(ModBlocks.AUSTRIA_ROAD_ARROW_STEM_BLOCK_3.asItem());
            itemGroup.add(ModBlocks.AUSTRIA_ROAD_ARROW_STRAIGHT_BLOCK_1.asItem());
            itemGroup.add(ModBlocks.AUSTRIA_ROAD_ARROW_STRAIGHT_BLOCK_2.asItem());
        });
    }
}
