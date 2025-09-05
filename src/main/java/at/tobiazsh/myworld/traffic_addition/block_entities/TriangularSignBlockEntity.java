package at.tobiazsh.myworld.traffic_addition.block_entities;

import at.tobiazsh.myworld.traffic_addition.blocks.SignBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

import static at.tobiazsh.myworld.traffic_addition.ModBlockEntities.TRIANGULAR_SIGN_BLOCK_ENTITY;

public class TriangularSignBlockEntity extends SignBlockEntity {

    public TriangularSignBlockEntity(BlockPos pos, BlockState state) {
        super(TRIANGULAR_SIGN_BLOCK_ENTITY, pos, state, SignBlock.SIGN_SHAPE.TRIANGULAR, "textures/sign/austria/warning/other_danger.png");
    }

}