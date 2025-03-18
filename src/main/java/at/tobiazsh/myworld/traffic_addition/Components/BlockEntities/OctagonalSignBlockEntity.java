package at.tobiazsh.myworld.traffic_addition.Components.BlockEntities;


/*
 * @created 30/08/2024 (DD/MM/YYYY) - 16:03
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.ModVars;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

import static at.tobiazsh.myworld.traffic_addition.ModBlockEntities.OCTAGONAL_SIGN_BLOCK_ENTITY;

public class OctagonalSignBlockEntity extends SignBlockEntity {
    public OctagonalSignBlockEntity(BlockPos pos, BlockState state) {
        super(OCTAGONAL_SIGN_BLOCK_ENTITY, pos, state, ModVars.SIGN_SELECTION_TYPE.OCTAGONAL, "textures/sign_pngs/aut/octagonal/stop.png");
    }
}
