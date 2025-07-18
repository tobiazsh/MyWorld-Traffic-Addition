package at.tobiazsh.myworld.traffic_addition;

import at.tobiazsh.myworld.traffic_addition.blocks.*;
import at.tobiazsh.myworld.traffic_addition.blocks.utils.RegisteredModBlock;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

import static at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition.createId;
import static at.tobiazsh.myworld.traffic_addition.blocks.utils.RegisteredModBlock.genKey;

public class ModBlocks {

    // GENERAL

    public static final RegisteredModBlock BORDER_BLOCK = new RegisteredModBlock(
            createId("border_block"),
            new Block(
                    AbstractBlock.Settings.create()
                            .sounds(BlockSoundGroup.STONE)
                            .registryKey(genKey(createId("border_block")))
            )
    ).register(true);


    public static final RegisteredModBlock SIGN_POLE_BLOCK = new RegisteredModBlock(
            createId("sign_pole_block"),
            new SignPoleBlock(
                    AbstractBlock.Settings.create()
                            .strength(4.0f)
                            .nonOpaque()
                            .sounds(BlockSoundGroup.STONE)
                            .registryKey(genKey(createId("sign_pole_block")))
            )
    ).register(true);



    // SIGNS

    private static final AbstractBlock.Settings SIGN_SETTINGS = AbstractBlock.Settings.create().strength(Blocks.IRON_BLOCK.getHardness()).sounds(BlockSoundGroup.STONE).nonOpaque();


    public static final RegisteredModBlock TRIANGULAR_SIGN_BLOCK = new RegisteredModBlock(
            createId("triangular_sign_block"),
            new TriangularSignBlock(SIGN_SETTINGS.registryKey(genKey(createId("triangular_sign_block"))))
    ).register(true);


    public static final RegisteredModBlock UPSIDE_DOWN_TRIANGULAR_SIGN_BLOCK = new RegisteredModBlock(
            createId("upside_down_triangular_sign_block"),
            new UpsideDownTriangularSignBlock(SIGN_SETTINGS.registryKey(genKey(createId("upside_down_triangular_sign_block"))))
    ).register(true);


    public static final RegisteredModBlock OCTAGONAL_SIGN_BLOCK = new RegisteredModBlock(
            createId("octagonal_sign_block"),
            new OctagonalSignBlock(SIGN_SETTINGS.registryKey(genKey(createId("octagonal_sign_block"))))
    ).register(true);


    public static final RegisteredModBlock ROUND_SIGN_BLOCK = new RegisteredModBlock(
            createId("round_sign_block"),
            new RoundSignBlock(SIGN_SETTINGS.registryKey(genKey(createId("round_sign_block"))))
    ).register(true);


    public static final RegisteredModBlock CUSTOMIZABLE_SIGN_BLOCK = new RegisteredModBlock(
            createId("customizable_sign_block"),
            new CustomizableSignBlock(SIGN_SETTINGS.registryKey(genKey(createId("customizable_sign_block"))))
    ).register(true);



    // UTILS

    public static final RegisteredModBlock SIGN_HOLDER_BLOCK = new RegisteredModBlock(
            createId("sign_holder_block"),
            new SignHolderBlock(
                    AbstractBlock.Settings.create()
                            .nonOpaque()
                            .sounds(BlockSoundGroup.STONE)
                            .strength(Blocks.IRON_BLOCK.getHardness())
                            .registryKey(genKey(createId("sign_holder_block")))
            )
    ).register(false);


    public static final RegisteredModBlock CUSTOMIZABLE_SIGN_BORDER_BLOCK = new RegisteredModBlock(
            createId("customizable_sign_border_block"),
            new CustomizableSignBorderBlock(
                    AbstractBlock.Settings.create()
                            .nonOpaque()
                            .sounds(BlockSoundGroup.STONE)
                            .strength(Blocks.IRON_BLOCK.getHardness())
                            .registryKey(genKey(createId("customizable_sign_border_block")))
            )
    ).register(false);

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(ModGroups.TRAFFIC_ADDITION_ITEM_GROUP_KEY).register((itemGroup) -> {
            itemGroup.add(ModBlocks.BORDER_BLOCK.getBlock().asItem());
            itemGroup.add(ModBlocks.SIGN_POLE_BLOCK.getBlock().asItem());
            itemGroup.add(ModBlocks.TRIANGULAR_SIGN_BLOCK.getBlock().asItem());
            itemGroup.add(ModBlocks.UPSIDE_DOWN_TRIANGULAR_SIGN_BLOCK.getBlock().asItem());
            itemGroup.add(ModBlocks.OCTAGONAL_SIGN_BLOCK.getBlock().asItem());
            itemGroup.add(ModBlocks.ROUND_SIGN_BLOCK.getBlock().asItem());
            itemGroup.add(ModBlocks.CUSTOMIZABLE_SIGN_BLOCK.getBlock().asItem());
        });
    }
}
