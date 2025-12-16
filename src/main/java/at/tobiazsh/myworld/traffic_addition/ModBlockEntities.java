package at.tobiazsh.myworld.traffic_addition;

import at.tobiazsh.myworld.traffic_addition.block_entities.*;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NullMarked;

import static at.tobiazsh.myworld.traffic_addition.ModBlocks.*;

@NullMarked
public class ModBlockEntities {

    public static BlockEntityType<SignPoleBlockEntity> SIGN_POLE_BLOCK_ENTITY = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, "sign_pole_block"),
            FabricBlockEntityTypeBuilder.create(SignPoleBlockEntity::new, SIGN_POLE_BLOCK.getBlock()).build()
    );

    public static BlockEntityType<TriangularSignBlockEntity> TRIANGULAR_SIGN_BLOCK_ENTITY = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, "triangular_sign_block"),
            FabricBlockEntityTypeBuilder.create(TriangularSignBlockEntity::new, TRIANGULAR_SIGN_BLOCK.getBlock()).build()
    );

    public static BlockEntityType<SignPoleBlockEntity> SIGN_BLOCK_ENTITY = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, "sign_block"),
            FabricBlockEntityTypeBuilder.create(SignPoleBlockEntity::new, SIGN_POLE_BLOCK.getBlock()).build()
    );

    public static BlockEntityType<UpsideDownTriangularSignBlockEntity> UPSIDE_DOWN_TRIANGULAR_SIGN_BLOCK_ENTITY = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, "upsidedown_triangular_sign_block"),
            FabricBlockEntityTypeBuilder.create(UpsideDownTriangularSignBlockEntity::new, UPSIDE_DOWN_TRIANGULAR_SIGN_BLOCK.getBlock()).build()
    );

    public static BlockEntityType<OctagonalSignBlockEntity> OCTAGONAL_SIGN_BLOCK_ENTITY = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, "octagonal_sign_block"),
            FabricBlockEntityTypeBuilder.create(OctagonalSignBlockEntity::new, OCTAGONAL_SIGN_BLOCK.getBlock()).build()
    );

    public static BlockEntityType<RoundSignBlockEntity> ROUND_SIGN_BLOCK_ENTITY = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, "round_sign_block"),
            FabricBlockEntityTypeBuilder.create(RoundSignBlockEntity::new, ROUND_SIGN_BLOCK.getBlock()).build()
    );

    public static BlockEntityType<CustomizableSignBlockEntity> CUSTOMIZABLE_SIGN_BLOCK_ENTITY = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, "customizable_sign_block"),
            FabricBlockEntityTypeBuilder.create(CustomizableSignBlockEntity::new, CUSTOMIZABLE_SIGN_BLOCK.getBlock()).build()
    );

    public static void initialize() {}
}
