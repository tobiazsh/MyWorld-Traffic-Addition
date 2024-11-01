package at.tobiazsh.myworld.traffic_addition.components.Blocks;

import at.tobiazsh.myworld.traffic_addition.components.BlockEntities.SignBlockEntity;
import at.tobiazsh.myworld.traffic_addition.components.BlockEntities.SignPoleBlockEntity;
import at.tobiazsh.myworld.traffic_addition.Utils.Coordinates;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class SignBlock extends BlockWithEntity {

    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;

    private VoxelShape SHAPE_N;
    private VoxelShape SHAPE_E;
    private VoxelShape SHAPE_S;
    private VoxelShape SHAPE_W;

    public SignBlock(Settings settings, VoxelShape vn, VoxelShape ve, VoxelShape vs, VoxelShape vw) {
        super(settings);

        SHAPE_N = vn;
        SHAPE_E = ve;
        SHAPE_S = vs;
        SHAPE_W = vw;
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
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);

        BlockPos blockBehindPos = getBehindPos(pos, state);
        if(world.getBlockEntity(blockBehindPos) instanceof SignPoleBlockEntity blockEntityBehind) {
            ((SignBlockEntity) world.getBlockEntity(pos)).setRotation(blockEntityBehind.getRotationValue());
        }

        setBackstepCoords(state, world, pos);
    }

    public static BlockPos getBehindPos(BlockPos pos, BlockState state) {
        switch(state.get(FACING)) {
            case EAST -> { return pos.west(); }
            case SOUTH -> { return pos.north(); }
            case WEST -> { return pos.east(); }
            default -> { return pos.south(); }
        }
    }

    public Coordinates setBackstepCoords(BlockState state, World world, BlockPos pos) {
        Coordinates backstepCoords;
        switch (state.get(FACING)) {
            case EAST -> { backstepCoords = new Coordinates(-1.55f, 0f, 0f, Direction.EAST); }
            case SOUTH -> { backstepCoords = new Coordinates(0f, 0f, -1.55f, Direction.SOUTH); }
            case WEST -> { backstepCoords = new Coordinates(1.55f, 0f, 0f, Direction.WEST); }
            default -> { backstepCoords = new Coordinates(0f, 0f, .55f, Direction.NORTH); }
        }

        return backstepCoords;
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return null;
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return null;
    }
}
