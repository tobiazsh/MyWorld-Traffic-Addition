package at.tobiazsh.myworld.traffic_addition.block_entities;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import static at.tobiazsh.myworld.traffic_addition.ModBlocks.SPECIAL_BLOCK;

public class SpecialBlockEntity extends BlockEntity {
    public static BlockEntityType<SpecialBlockEntity> SPECIAL_BLOCK_ENTITY;

    public static void initialize () {
        SPECIAL_BLOCK_ENTITY = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                Identifier.of(MyWorldTrafficAddition.MOD_ID, "special_block"),
                FabricBlockEntityTypeBuilder.create(SpecialBlockEntity::new, SPECIAL_BLOCK).build()
        );
    }

    public SpecialBlockEntity(BlockPos pos, BlockState state) {
        super(SPECIAL_BLOCK_ENTITY, pos, state);
    }
}
