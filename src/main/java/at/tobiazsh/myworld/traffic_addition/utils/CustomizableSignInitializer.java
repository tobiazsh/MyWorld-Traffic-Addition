package at.tobiazsh.myworld.traffic_addition.utils;

import at.tobiazsh.myworld.traffic_addition.block_entities.CustomizableSignBlockEntity;
import at.tobiazsh.myworld.traffic_addition.block_entities.SignPoleBlockEntity;
import at.tobiazsh.myworld.traffic_addition.payload.block_modification.*;
import at.tobiazsh.myworld.traffic_addition.utils.math.BlockPosExtended;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static at.tobiazsh.myworld.traffic_addition.block_entities.CustomizableSignBlockEntity.isUsableCustomizableSignBlockEntity;
import static at.tobiazsh.myworld.traffic_addition.utils.DirectionUtils.blockPosInDirection;
import static at.tobiazsh.myworld.traffic_addition.utils.DirectionUtils.getRightSideDirection;

public class CustomizableSignInitializer {

    public record CustomizableSignInitializationResult(
            int signWidth, int signHeight,
            ImmutableList<BlockPos> signPositions,
            ImmutableList<BlockPosExtended> signDistances,
            ImmutableList<BlockPos> polePositions,
            ImmutableList<BlockPosExtended> poleDistances,
            boolean success
    ) {
        public boolean hasError() {
            return !this.success;
        }

        public static CustomizableSignInitializationResult unsuccessful() {
            return new CustomizableSignInitializationResult(
                    0, 0,
                    ImmutableList.of(), ImmutableList.of(),
                    ImmutableList.of(), ImmutableList.of(),
                    false
            );
        }
    }

    /**
     * Initializes the sign structure by determining dimensions and configuring connected blocks
     * @param customizableSignBlockEntity the master block entity of the sign structure
     * @param onError action to perform if an error occurs during initialization, accepts an error message as a parameter
     * @return result of the initialization, containing dimensions and positions of connected blocks if successful, or an error state if unsuccessful
     */
    public static CustomizableSignInitializationResult initializeSign(
            CustomizableSignBlockEntity customizableSignBlockEntity,
            Consumer<String> onError
    ) {
        if (customizableSignBlockEntity == null) {
            onError.accept("Failed to initialize sign structure! Sign is null!");
            return CustomizableSignInitializationResult.unsuccessful();
        }

        Direction facing = customizableSignBlockEntity.getFacing();
        BlockPosExtended masterPos = new BlockPosExtended(customizableSignBlockEntity.getBlockPos());
        Level level = customizableSignBlockEntity.getLevel();

        if (level == null) {
            onError.accept("Failed to initialize sign structure! World of sign is null!");
            return CustomizableSignInitializationResult.unsuccessful();
        }

        int signHeight = checkHeight(customizableSignBlockEntity.getBlockPos(), facing, level);
        int signWidth = checkWidth(customizableSignBlockEntity.getBlockPos(), facing, level);

        // Configure connected blocks
        var signDistancesOpt = checkSigns(customizableSignBlockEntity.getBlockPos(), facing, signWidth, signHeight, level);

        if (signDistancesOpt.isEmpty()) {
            onError.accept("Failed to initialize sign structure!");
            return CustomizableSignInitializationResult.unsuccessful();
        }

        var poleData = checkSignPoles(masterPos, DirectionUtils.getFacing(customizableSignBlockEntity.getBlockPos(), level), level, signHeight, signWidth);

        if (poleData.isEmpty()) {
            onError.accept("Failed to initialize sign polesAction! Please check the structure");
            return CustomizableSignInitializationResult.unsuccessful();
        }

        return new CustomizableSignInitializationResult(
                signWidth, signHeight,
                ImmutableList.copyOf(signDistancesOpt.get().getB()),
                ImmutableList.copyOf(signDistancesOpt.get().getA()),
                ImmutableList.copyOf(poleData.get().getA()),
                ImmutableList.copyOf(poleData.get().getB()),
                true
        );
    }

    /**
     * Determines the height of the sign structure by checking blocks above
     */
    private static int checkHeight(BlockPos masterPos, Direction facing, Level level) {
        int height = 1;

        BlockPos currentPos = masterPos;

        while (isUsableCustomizableSignBlockEntity(currentPos.above(), level, facing)) {
            currentPos = currentPos.above();
            height++;
        }

        return height;
    }

    /**
     * Determines the width of the sign structure by checking adjacent blocks
     */
    private static int checkWidth(BlockPos startingPos, Direction facing, Level level) {
        int right = 1;
        Direction rightDirection = getRightSideDirection(facing.getOpposite());

        while (isUsableCustomizableSignBlockEntity(blockPosInDirection(rightDirection, startingPos, right), level, facing))
            right++;

        return right; // Subtract 1 to not double count the master block
    }

    /**
     * Identifies and registers all sign blocks in the structure
     *
     * @param masterPos the position of the master sign block
     * @param facing the direction the sign is facing
     * @param signWidth the width of the sign structure in blocks
     * @param signHeight the height of the sign structure in blocks
     *
     * @return Optional of a Tuple, where A is the distances of the sign blocks to the master block and B is the positions of the sign blocks. The order of both lists is the same, so index 0 in A corresponds to index 0 in B, etc.
     */
    private static Optional<Tuple<List<BlockPosExtended>, List<BlockPos>>> checkSigns(BlockPos masterPos, @NotNull Direction facing, int signWidth, int signHeight, Level level) {
        List<BlockPosExtended> signDistances = new ArrayList<>();
        List<BlockPos> signPositions = new ArrayList<>();
        Direction rightDirection = getRightSideDirection(facing.getOpposite());

        // Scan row by row, starting at master position
        int scannedHeight = 0;
        int scannedWidth = 0;
        BlockPos currentUpPos = masterPos;
        while (isUsableCustomizableSignBlockEntity(currentUpPos, level, facing)) {
            BlockPos currentRightPos = currentUpPos;

            // Scan a single row
            scannedWidth = 0;
            while (isUsableCustomizableSignBlockEntity(currentRightPos, level, facing)) {
                signDistances.add(BlockPosExtended.getOffset(masterPos, currentRightPos));
                signPositions.add(currentRightPos);

                currentRightPos = blockPosInDirection(rightDirection, currentRightPos, 1);

                scannedWidth++;
            }

            if (scannedWidth != signWidth)
                return Optional.empty();

            scannedHeight++;
            currentUpPos = currentUpPos.above();
        }

        if (scannedHeight != signHeight)
            return Optional.empty();

        return Optional.of(new Tuple<>(signDistances, signPositions));
    }

    /**
     * Identifies and configures all sign polesAction connected to the sign structure
     * @return Optional of a Tuple, where A is the Pole poses and B is the distances of the poses to the master block.
     */
    private static Optional<Tuple<List<BlockPos>, List<BlockPosExtended>>> checkSignPoles(BlockPosExtended masterPos, Direction facing, Level level, int signHeight, int signWidth) {
        List<BlockPosExtended> distances = new ArrayList<>();
        List<BlockPos> poles = new ArrayList<>();
        Direction rightDirection = getRightSideDirection(facing.getOpposite());

        // Start position; this is the topmost pole position in the sign structure
        BlockPos start = blockPosInDirection(facing.getOpposite(), masterPos.above(signHeight - 1), 1);

        for (int i = 0; i < signWidth; i++) {
            BlockPos pos = blockPosInDirection(rightDirection, start, i);

            while (level.getBlockEntity(pos) instanceof SignPoleBlockEntity) {
                distances.add(
                        BlockPosExtended.getOffset(masterPos, pos)
                );

                poles.add(pos);

                pos = pos.below();
            }
        }

        return Optional.of(new Tuple<>(poles, distances));
    }

}
