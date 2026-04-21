package at.tobiazsh.myworld.traffic_addition;

import at.tobiazsh.myworld.traffic_addition.blocks.*;
import at.tobiazsh.myworld.traffic_addition.blocks.utils.RegisteredModBlock;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;

import static at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition.createId;
import static at.tobiazsh.myworld.traffic_addition.blocks.utils.RegisteredModBlock.genKey;

public class ModBlocks {

    // GENERAL

    public static final RegisteredModBlock BORDER_BLOCK = new RegisteredModBlock(
            createId("border_block"),
            new Block(
                    BlockBehaviour.Properties.of()
                            .sound(SoundType.STONE)
                            .setId(genKey(createId("border_block")))
            )
    ).register(true);


    public static final RegisteredModBlock SIGN_POLE_BLOCK = new RegisteredModBlock(
            createId("sign_pole_block"),
            new SignPoleBlock(
                    BlockBehaviour.Properties.of()
                            .strength(4.0f)
                            .noOcclusion()
                            .sound(SoundType.STONE)
                            .setId(genKey(createId("sign_pole_block")))
            )
    ).register(true);



    // SIGNS

    private static final BlockBehaviour.Properties SIGN_SETTINGS = BlockBehaviour.Properties.of().strength(Blocks.IRON_BLOCK.defaultDestroyTime()).sound(SoundType.STONE).noOcclusion();


    public static final RegisteredModBlock TRIANGULAR_SIGN_BLOCK = new RegisteredModBlock(
            createId("triangular_sign_block"),
            new TriangularSignBlock(SIGN_SETTINGS.setId(genKey(createId("triangular_sign_block"))))
    ).register(true);


    public static final RegisteredModBlock UPSIDE_DOWN_TRIANGULAR_SIGN_BLOCK = new RegisteredModBlock(
            createId("upside_down_triangular_sign_block"),
            new UpsideDownTriangularSignBlock(SIGN_SETTINGS.setId(genKey(createId("upside_down_triangular_sign_block"))))
    ).register(true);


    public static final RegisteredModBlock OCTAGONAL_SIGN_BLOCK = new RegisteredModBlock(
            createId("octagonal_sign_block"),
            new OctagonalSignBlock(SIGN_SETTINGS.setId(genKey(createId("octagonal_sign_block"))))
    ).register(true);


    public static final RegisteredModBlock ROUND_SIGN_BLOCK = new RegisteredModBlock(
            createId("round_sign_block"),
            new RoundSignBlock(SIGN_SETTINGS.setId(genKey(createId("round_sign_block"))))
    ).register(true);


    public static final RegisteredModBlock CUSTOMIZABLE_SIGN_BLOCK = new RegisteredModBlock(
            createId("customizable_sign_block"),
            new CustomizableSignBlock(SIGN_SETTINGS.setId(genKey(createId("customizable_sign_block"))))
    ).register(true);



    // UTILS

    public static final RegisteredModBlock SIGN_HOLDER_BLOCK = new RegisteredModBlock(
            createId("sign_holder_block"),
            new SignHolderBlock(
                    BlockBehaviour.Properties.of()
                            .noOcclusion()
                            .sound(SoundType.STONE)
                            .strength(Blocks.IRON_BLOCK.defaultDestroyTime())
                            .setId(genKey(createId("sign_holder_block")))
            )
    ).register(false);


    public static final RegisteredModBlock CUSTOMIZABLE_SIGN_BORDER = new RegisteredModBlock(
            createId("customizable_sign_border"),
            new CustomizableSignBorder(
                    BlockBehaviour.Properties.of()
                            .noOcclusion()
                            .sound(SoundType.STONE)
                            .strength(Blocks.IRON_BLOCK.defaultDestroyTime())
                            .setId(genKey(createId("customizable_sign_border")))
            )
    ).register(false);

    public static final RegisteredModBlock CUSTOMIZABLE_SIGN_CORNER_BIT = new RegisteredModBlock(
            createId("customizable_sign_corner_bit"),
            new CustomizableSignCornerBit(
                    BlockBehaviour.Properties.of()
                            .noOcclusion()
                            .sound(SoundType.STONE)
                            .strength(Blocks.IRON_BLOCK.defaultDestroyTime())
                            .setId(genKey(createId("customizable_sign_corner_bit")))
            )
    ).register(false);

    public static void initialize() {
        CreativeModeTabEvents.modifyOutputEvent(ModGroups.TRAFFIC_ADDITION_ITEM_GROUP_KEY).register((itemGroup) -> {
            itemGroup.accept(ModBlocks.BORDER_BLOCK.getBlock().asItem());
            itemGroup.accept(ModBlocks.SIGN_POLE_BLOCK.getBlock().asItem());
            itemGroup.accept(ModBlocks.TRIANGULAR_SIGN_BLOCK.getBlock().asItem());
            itemGroup.accept(ModBlocks.UPSIDE_DOWN_TRIANGULAR_SIGN_BLOCK.getBlock().asItem());
            itemGroup.accept(ModBlocks.OCTAGONAL_SIGN_BLOCK.getBlock().asItem());
            itemGroup.accept(ModBlocks.ROUND_SIGN_BLOCK.getBlock().asItem());
            itemGroup.accept(ModBlocks.CUSTOMIZABLE_SIGN_BLOCK.getBlock().asItem());
        });
    }
}
