package at.tobiazsh.myworld.traffic_addition.Components.BlockEntities;


/*
 * @created 29/08/2024 (DD/MM/YYYY) - 21:29
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.ModVars;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

import static at.tobiazsh.myworld.traffic_addition.ModBlockEntities.UPSIDE_DOWN_TRIANGULAR_SIGN_BLOCK_ENTITY;

public class UpsideDownTriangularSignBlockEntity extends SignBlockEntity {
 public UpsideDownTriangularSignBlockEntity(BlockPos pos, BlockState state) {
  super(UPSIDE_DOWN_TRIANGULAR_SIGN_BLOCK_ENTITY, pos, state, ModVars.SIGN_SELECTION_TYPE.TRIANGULAR_UPSIDE_DOWN, "textures/sign_pngs/aut/upside_down_triangular/yield.png");
 }
}
