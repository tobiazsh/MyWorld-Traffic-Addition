package at.tobiazsh.myworld.traffic_addition.blocks;


/*
 * @created 04/09/2024 (DD/MM/YYYY) - 14:40
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class SignHolderBlock extends Block {
    public SignHolderBlock(Properties settings) {
        super(settings);
    }

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    VoxelShape SHAPE_N = Stream.of(
            Block.box(7, 7, 6, 9, 9, 7),
            Block.box(6, 7, 6, 7, 9, 10),
            Block.box(6, 7, 10, 7, 9, 16),
            Block.box(6, 4.5, 15, 7, 7, 16),
            Block.box(6, 9, 15, 7, 11.5, 16),
            Block.box(9, 4.5, 15, 10, 7, 16),
            Block.box(9, 9, 15, 10, 11.5, 16),
            Block.box(9, 7, 10, 10, 9, 16),
            Block.box(9, 7, 6, 10, 9, 10),
            Block.box(7, 7, 9, 9, 9, 10)
    ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

    VoxelShape SHAPE_S = Stream.of(
            Block.box(7, 7, 6, 9, 9, 7),
            Block.box(6, 7, 6, 7, 9, 10),
            Block.box(6, 7, 0, 7, 9, 6),
            Block.box(6, 4.5, 0, 7, 7, 1),
            Block.box(6, 9, 0, 7, 11.5, 1),
            Block.box(9, 4.5, 0, 10, 7, 1),
            Block.box(9, 9, 0, 10, 11.5, 1),
            Block.box(9, 7, 0, 10, 9, 6),
            Block.box(9, 7, 6, 10, 9, 10),
            Block.box(7, 7, 9, 9, 9, 10)
    ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

    VoxelShape SHAPE_W = Stream.of(
            Block.box(7, 7, 6, 9, 9, 7),
            Block.box(6, 7, 6, 7, 9, 10),
            Block.box(10, 7, 6, 16, 9, 7),
            Block.box(15, 4.5, 6, 16, 7, 7),
            Block.box(15, 9, 6, 16, 11.5, 7),
            Block.box(15, 4.5, 9, 16, 7, 10),
            Block.box(15, 9, 9, 16, 11.5, 10),
            Block.box(10, 7, 9, 16, 9, 10),
            Block.box(9, 7, 6, 10, 9, 10),
            Block.box(7, 7, 9, 9, 9, 10)
    ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

    VoxelShape SHAPE_E = Stream.of(
            Block.box(7, 7, 6, 9, 9, 7),
            Block.box(6, 7, 6, 7, 9, 10),
            Block.box(0, 7, 9, 6, 9, 10),
            Block.box(0, 4.5, 9, 1, 7, 10),
            Block.box(0, 9, 9, 1, 11.5, 10),
            Block.box(0, 4.5, 6, 1, 7, 7),
            Block.box(0, 9, 6, 1, 11.5, 7),
            Block.box(0, 7, 6, 6, 9, 7),
            Block.box(9, 7, 6, 10, 9, 10),
            Block.box(7, 7, 9, 9, 9, 10)
    ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        switch(state.getValue(FACING)) {
            case EAST -> { return SHAPE_E; }
            case SOUTH -> { return SHAPE_S; }
            case WEST -> { return SHAPE_W; }
            default -> { return SHAPE_N; }
        }
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
