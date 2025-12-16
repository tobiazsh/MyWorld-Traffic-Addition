package at.tobiazsh.myworld.traffic_addition.blocks;

import at.tobiazsh.myworld.traffic_addition.block_entities.SignBlockEntity;
import at.tobiazsh.myworld.traffic_addition.block_entities.SignPoleBlockEntity;
import at.tobiazsh.myworld.traffic_addition.custom_payloads.block_modification.OpenSignSelectionPayload;
import at.tobiazsh.myworld.traffic_addition.utils.math.Coordinates;
import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public abstract class SignBlock extends BaseEntityBlock {

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    private final VoxelShape SHAPE_N;
    private final VoxelShape SHAPE_E;
    private final VoxelShape SHAPE_S;
    private final VoxelShape SHAPE_W;
    public final SIGN_SHAPE shape;

    public SignBlock(Properties settings, VoxelShape vn, VoxelShape ve, VoxelShape vs, VoxelShape vw, SIGN_SHAPE shape) {
        super(settings);

        SHAPE_N = vn;
        SHAPE_E = ve;
        SHAPE_S = vs;
        SHAPE_W = vw;

        this.shape = shape;
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
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.setPlacedBy(world, pos, state, placer, itemStack);

        BlockPos blockBehindPos = getBehindPos(pos, state);
        if(world.getBlockEntity(blockBehindPos) instanceof SignPoleBlockEntity blockEntityBehind) {
            ((SignBlockEntity) world.getBlockEntity(pos)).setRotation(blockEntityBehind.getRotationValue());
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (
                !(world instanceof ServerLevel) ||
                !(player instanceof ServerPlayer) ||
                !player.isShiftKeyDown()
        )
            return InteractionResult.PASS;

        ServerPlayNetworking.send(
                (ServerPlayer) player,
                new OpenSignSelectionPayload(
                        pos,
                        getSignSelectionEnumInt(this.shape),
                        world.dimension()
                )
        );

        return InteractionResult.SUCCESS;
    }

    public static BlockPos getBehindPos(BlockPos pos, BlockState state) {
        switch(state.getValue(FACING)) {
            case EAST -> { return pos.west(); }
            case SOUTH -> { return pos.north(); }
            case WEST -> { return pos.east(); }
            default -> { return pos.south(); }
        }
    }

    public Coordinates getBackMovementCoordinates(BlockState state) {
        Coordinates backstepCoords;
        switch (state.getValue(FACING)) {
            case EAST -> backstepCoords = new Coordinates(-1.55f, 0f, 0f, Direction.EAST);
            case SOUTH -> backstepCoords = new Coordinates(0f, 0f, -1.55f, Direction.SOUTH);
            case WEST -> backstepCoords = new Coordinates(1.55f, 0f, 0f, Direction.WEST);
            default -> backstepCoords = new Coordinates(0f, 0f, .55f, Direction.NORTH);
        }

        return backstepCoords;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return null;
    }

    public static int getSignSelectionEnumInt (SIGN_SHAPE type) {
        switch (type) {
            case TRIANGULAR -> { return 0; }
            case TRIANGULAR_UPSIDE_DOWN -> { return 1; }
            case OCTAGONAL -> { return 3; }
            case RECT_SMALL -> { return 4; }
            case RECT_MEDIUM -> { return 5; }
            case RECT_LARGE -> { return 6; }
            case RECT_STRETCH_SMALL -> { return 7; }
            case RECT_STRETCH_MEDIUM -> { return 8; }
            case RECT_STRETCH_LARGE -> { return 9; }
            case SQUARE_TURN_45 -> { return 10; }
            default -> { return 2; }
        }
    }

    public static SIGN_SHAPE getSignSelectionEnum (int num) {
        switch (num) {
            case 0 -> { return SIGN_SHAPE.TRIANGULAR; }
            case 1 -> { return SIGN_SHAPE.TRIANGULAR_UPSIDE_DOWN; }
            case 3 -> { return SIGN_SHAPE.OCTAGONAL; }
            case 4 -> { return SIGN_SHAPE.RECT_SMALL; }
            case 5 -> { return SIGN_SHAPE.RECT_MEDIUM; }
            case 6 -> { return SIGN_SHAPE.RECT_LARGE; }
            case 7 -> { return SIGN_SHAPE.RECT_STRETCH_SMALL; }
            case 8 -> { return SIGN_SHAPE.RECT_STRETCH_MEDIUM; }
            case 9 -> { return SIGN_SHAPE.RECT_STRETCH_LARGE; }
            case 10 -> { return SIGN_SHAPE.SQUARE_TURN_45; }
            default -> { return SIGN_SHAPE.ROUND; }
        }
    }

    public enum SIGN_SHAPE {
        TRIANGULAR,
        TRIANGULAR_UPSIDE_DOWN,
        ROUND,
        OCTAGONAL,
        RECT_SMALL,
        RECT_MEDIUM,
        RECT_LARGE,
        RECT_STRETCH_SMALL,
        RECT_STRETCH_MEDIUM,
        RECT_STRETCH_LARGE,
        SQUARE_TURN_45,
    }

}
