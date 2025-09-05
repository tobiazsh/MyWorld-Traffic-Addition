package at.tobiazsh.myworld.traffic_addition.block_entities;


/*
 * @created 04/09/2024 (DD/MM/YYYY) - 00:14
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.blocks.SignBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

import static at.tobiazsh.myworld.traffic_addition.ModBlockEntities.ROUND_SIGN_BLOCK_ENTITY;

public class RoundSignBlockEntity extends SignBlockEntity {

    public RoundSignBlockEntity(BlockPos pos, BlockState state) {
        super(ROUND_SIGN_BLOCK_ENTITY, pos, state, SignBlock.SIGN_SHAPE.ROUND, "textures/sign/austria/regulatory/lane_right_side.png");
    }

}
