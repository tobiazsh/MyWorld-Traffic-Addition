package at.tobiazsh.myworld.traffic_addition.components.BlockEntities;

import at.tobiazsh.myworld.traffic_addition.ModVars;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

import static at.tobiazsh.myworld.traffic_addition.ModBlockEntities.TRIANGULAR_SIGN_BLOCK_ENTITY;

public class TriangularSignBlockEntity extends SignBlockEntity {

    public TriangularSignBlockEntity(BlockPos pos, BlockState state) {
        super(TRIANGULAR_SIGN_BLOCK_ENTITY, pos, state, ModVars.SIGN_SELECTION_TYPE.TRIANGULAR_UPSIDE_UP, "textures/sign_pngs/aut/triangular/other_danger.png");
    }

}