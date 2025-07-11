package at.tobiazsh.myworld.traffic_addition.components.blocks;


/*
 * @created 04/09/2024 (DD/MM/YYYY) - 14:40
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class SignHolderBlock extends Block {
    public SignHolderBlock(Settings settings) {
        super(settings);
    }

    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;

    VoxelShape SHAPE_N = Stream.of(
            Block.createCuboidShape(7, 7, 6, 9, 9, 7),
            Block.createCuboidShape(6, 7, 6, 7, 9, 10),
            Block.createCuboidShape(6, 7, 10, 7, 9, 16),
            Block.createCuboidShape(6, 4.5, 15, 7, 7, 16),
            Block.createCuboidShape(6, 9, 15, 7, 11.5, 16),
            Block.createCuboidShape(9, 4.5, 15, 10, 7, 16),
            Block.createCuboidShape(9, 9, 15, 10, 11.5, 16),
            Block.createCuboidShape(9, 7, 10, 10, 9, 16),
            Block.createCuboidShape(9, 7, 6, 10, 9, 10),
            Block.createCuboidShape(7, 7, 9, 9, 9, 10)
    ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get();

    VoxelShape SHAPE_S = Stream.of(
            Block.createCuboidShape(7, 7, 6, 9, 9, 7),
            Block.createCuboidShape(6, 7, 6, 7, 9, 10),
            Block.createCuboidShape(6, 7, 0, 7, 9, 6),
            Block.createCuboidShape(6, 4.5, 0, 7, 7, 1),
            Block.createCuboidShape(6, 9, 0, 7, 11.5, 1),
            Block.createCuboidShape(9, 4.5, 0, 10, 7, 1),
            Block.createCuboidShape(9, 9, 0, 10, 11.5, 1),
            Block.createCuboidShape(9, 7, 0, 10, 9, 6),
            Block.createCuboidShape(9, 7, 6, 10, 9, 10),
            Block.createCuboidShape(7, 7, 9, 9, 9, 10)
    ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get();

    VoxelShape SHAPE_W = Stream.of(
            Block.createCuboidShape(7, 7, 6, 9, 9, 7),
            Block.createCuboidShape(6, 7, 6, 7, 9, 10),
            Block.createCuboidShape(10, 7, 6, 16, 9, 7),
            Block.createCuboidShape(15, 4.5, 6, 16, 7, 7),
            Block.createCuboidShape(15, 9, 6, 16, 11.5, 7),
            Block.createCuboidShape(15, 4.5, 9, 16, 7, 10),
            Block.createCuboidShape(15, 9, 9, 16, 11.5, 10),
            Block.createCuboidShape(10, 7, 9, 16, 9, 10),
            Block.createCuboidShape(9, 7, 6, 10, 9, 10),
            Block.createCuboidShape(7, 7, 9, 9, 9, 10)
    ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get();

    VoxelShape SHAPE_E = Stream.of(
            Block.createCuboidShape(7, 7, 6, 9, 9, 7),
            Block.createCuboidShape(6, 7, 6, 7, 9, 10),
            Block.createCuboidShape(0, 7, 9, 6, 9, 10),
            Block.createCuboidShape(0, 4.5, 9, 1, 7, 10),
            Block.createCuboidShape(0, 9, 9, 1, 11.5, 10),
            Block.createCuboidShape(0, 4.5, 6, 1, 7, 7),
            Block.createCuboidShape(0, 9, 6, 1, 11.5, 7),
            Block.createCuboidShape(0, 7, 6, 6, 9, 7),
            Block.createCuboidShape(9, 7, 6, 10, 9, 10),
            Block.createCuboidShape(7, 7, 9, 9, 9, 10)
    ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get();

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        switch(state.get(FACING)) {
            case EAST -> { return SHAPE_E; }
            case SOUTH -> { return SHAPE_S; }
            case WEST -> { return SHAPE_W; }
            default -> { return SHAPE_N; }
        }
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
