package at.tobiazsh.myworld.traffic_addition.utils;

import at.tobiazsh.myworld.traffic_addition.block_entities.CustomizableSignBlockEntity;
import at.tobiazsh.myworld.traffic_addition.block_entities.SignPoleBlockEntity;
import at.tobiazsh.myworld.traffic_addition.preference.ServerPreferencesManager;
import at.tobiazsh.myworld.traffic_addition.utils.math.BlockPosExtended;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

import java.util.*;

import static at.tobiazsh.myworld.traffic_addition.block_entities.CustomizableSignBlockEntity.isUsableCustomizableSignBlockEntity;
import static at.tobiazsh.myworld.traffic_addition.utils.DirectionUtils.blockPosInDirection;
import static at.tobiazsh.myworld.traffic_addition.utils.DirectionUtils.getRightSideDirection;

@NullMarked
public class CustomizableSignInitializer {

    public static int getMaxSignHeight() {
        return ServerPreferencesManager.maxCustomizableSignHeight;
    }

    public static int getMaxSignWidth() {
        return ServerPreferencesManager.maxCustomizableSignWidth;
    }

    public record DetectionError(String message) {
        public static DetectionError combine(@Nullable DetectionError signError, @Nullable DetectionError poleError) {
            String signMessage = signError != null ? signError.message() : "";
            String poleMessage = poleError != null ? poleError.message() : "";

            return new DetectionError(String.format("""
                    Sign detection error: %s
                    Pole detection error: %s
                    """, signMessage, poleMessage));
        }
    }

    /**
     * Stores the result from the sign detection loop.
     * @param signRelative All the sign's relative coordinates to the master sign
     * @param signAbsolute All the sign's absolute coordinates (same order as signAbsolute)
     * @param realMaster The real master sign position (in case the original master sign was not the actual master)
     * @param signWidth The sign's detected width
     * @param signHeight The sign's detected height
     * @param error Error if detection failed. Null if OK
     */
    private record SignLoopResult(
            ImmutableList<BlockPosExtended> signRelative,
            ImmutableList<BlockPos> signAbsolute,
            ImmutableList<BorderProperty> borders,
            BlockPos realMaster,
            int signWidth, int signHeight,
            @Nullable DetectionError error
    ) {
        public static SignLoopResult failure(String message) {
            return new SignLoopResult(
                    ImmutableList.of(), ImmutableList.of(), ImmutableList.of(),
                    BlockPos.ZERO,
                    0, 0,
                    new DetectionError(message)
            );
        }
    }

    /**
     * Stores the result from the pole detection loop.
     * @param poleRelative All the pole's relative coordinates to the master sign
     * @param poleAbsolute All the pole's absolute coordinates (same order as poleRelative)
     * @param error Error if detection failed. Null if OK
     */
    private record PoleLoopResult(
            ImmutableList<BlockPosExtended> poleRelative,
            ImmutableList<BlockPos> poleAbsolute,
            @Nullable DetectionError error
    ) {
        public static PoleLoopResult failure(String message) {
            return new PoleLoopResult(
                    ImmutableList.of(), ImmutableList.of(),
                    new DetectionError(message)
            );
        }
    }

    /**
     * Stores the result from sign initialization.
     * @param signWidth The sign's detected width
     * @param signHeight The sign's detected height
     * @param realMaster The real master sign position (in case the original master sign was not the actual master)
     * @param signRelative All the sign's relative coordinates (same order as signAbsolute)
     * @param signAbsolute All the sign's absolute coordinates to the master sign
     * @param poleRelative All the pole's relative coordinates to the master sign
     * @param poleAbsolute All the pole's absolute coordinates (same order as poleRelative)
     * @param error Error if initialization failed. Null if OK
     */
    public record CustomizableSignInitializationResult(
            int signWidth, int signHeight,
            BlockPos realMaster,
            ImmutableList<BlockPosExtended> signRelative,
            ImmutableList<BlockPos> signAbsolute,
            ImmutableList<BorderProperty> borders,
            ImmutableList<BlockPosExtended> poleRelative,
            ImmutableList<BlockPos> poleAbsolute,
            @Nullable DetectionError error
    ) {
        public boolean hasError() {
            return this.error != null;
        }

        /**
         * Returns an empty result
         */
        public static CustomizableSignInitializationResult failure(String message) {
            return new CustomizableSignInitializationResult(
                    0, 0,
                    BlockPos.ZERO,
                    ImmutableList.of(), ImmutableList.of(), ImmutableList.of(),
                    ImmutableList.of(), ImmutableList.of(),
                    new DetectionError(message)
            );
        }

        /**
         * Combines a signResult and a poleResult to a main result.
         * @param signResult The sign detection result
         * @param poleResult The pole detection result
         * @return Combined result
         */
        private static CustomizableSignInitializationResult combine(SignLoopResult signResult, PoleLoopResult poleResult) {
            boolean hasError = signResult.error() != null || poleResult.error() != null;

            return new CustomizableSignInitializationResult(
                    signResult.signWidth(), signResult.signHeight(),
                    signResult.realMaster(),
                    signResult.signRelative(), signResult.signAbsolute(), signResult.borders(),
                    poleResult.poleRelative(), poleResult.poleAbsolute(),
                    hasError ? DetectionError.combine(signResult.error(), poleResult.error()) : null
            );
        }

        /**
         * Encodes the {@link CustomizableSignInitializationResult} into a {@link FriendlyByteBuf} for network transmission.
         * @param buf The buffer to write to
         */
        public void encode(FriendlyByteBuf buf) {
            buf.writeInt(signWidth);
            buf.writeInt(signHeight);

            buf.writeBlockPos(realMaster);

            buf.writeCollection(signRelative, BlockPosExtended.STREAM_CODEC);
            buf.writeCollection(signAbsolute, BlockPos.STREAM_CODEC);
            buf.writeCollection(borders, BorderProperty.STREAM_CODEC);

            buf.writeCollection(poleRelative, BlockPosExtended.STREAM_CODEC);
            buf.writeCollection(poleAbsolute, BlockPos.STREAM_CODEC);

            buf.writeBoolean(error != null); // Has error?

            String message = error == null ? "" : error.message();
            buf.writeUtf(message); // Error message (empty if no error)
        }

        /**
         * Decodes the {@link CustomizableSignInitializationResult} from a {@link FriendlyByteBuf} received from the network.
         * @param buf The received buffer.
         * @return A new {@link CustomizableSignInitializationResult} instance containing the decoded data.
         */
        public static CustomizableSignInitializationResult decode(FriendlyByteBuf buf) {
            int signWidth = buf.readInt();
            int signHeight = buf.readInt();

            BlockPos realMaster = buf.readBlockPos();

            ImmutableList<BlockPosExtended> signRelative = ImmutableList.copyOf(buf.readList(BlockPosExtended.STREAM_CODEC));
            ImmutableList<BlockPos> signAbsolute = ImmutableList.copyOf(buf.readList(BlockPos.STREAM_CODEC));
            ImmutableList<BorderProperty> borders = ImmutableList.copyOf(buf.readList(BorderProperty.STREAM_CODEC));

            ImmutableList<BlockPosExtended> poleRelative = ImmutableList.copyOf(buf.readList(BlockPosExtended.STREAM_CODEC));
            ImmutableList<BlockPos> poleAbsolute = ImmutableList.copyOf(buf.readList(BlockPos.STREAM_CODEC));

            boolean hasError = buf.readBoolean();
            String message = buf.readUtf();

            return new CustomizableSignInitializationResult(
                    signWidth, signHeight,
                    realMaster,
                    signRelative, signAbsolute, borders,
                    poleRelative, poleAbsolute,
                    hasError ? new DetectionError(message) : null
            );
        }
    }


    /**
     * Initializes the sign structure by determining dimensions and configuring connected blocks
     * @param customizableSignBlockEntity the master block entity of the sign structure
     * @return result of the initialization, containing dimensions and positions of connected blocks if successful, or an error state if unsuccessful
     */
    public static CustomizableSignInitializationResult initializeSign(
            CustomizableSignBlockEntity customizableSignBlockEntity
    ) {
        Direction facingInverse = customizableSignBlockEntity.getFacing().getOpposite();
        Direction right = getRightSideDirection(facingInverse);
        BlockPos pos = customizableSignBlockEntity.getBlockPos(); // NOT master pos! We don't know master yet!
        Level level = customizableSignBlockEntity.getLevel();

        if (level == null)
            return CustomizableSignInitializationResult.failure("Failed to initialize sign structure! Level of sign is null!");

        SignLoopResult signResult = signDetection(pos, level, facingInverse, right);

        if (signResult.error() != null)
            return CustomizableSignInitializationResult.failure("Failed to initialize sign structure! Sign detection failed with error: " + signResult.error().message());

        PoleLoopResult poleResult = poleDetection(
                signResult.realMaster(), level, facingInverse, right, signResult.signWidth, signResult.signHeight
        );

        if (poleResult.error() != null)
            return CustomizableSignInitializationResult.failure("Failed to initialize sign structure! Pole detection failed with error: " + poleResult.error().message());

        return CustomizableSignInitializationResult.combine(signResult, poleResult);
    }

    private static SignLoopResult signDetection(
            BlockPos signPos, Level level, Direction facingInverse, Direction right
    ) {
        Direction left = right.getOpposite();
        Direction facing = facingInverse.getOpposite();

        // Find "real" master on the bottom left
        BlockPos master = signPos;

        while (isUsableCustomizableSignBlockEntity(master.below(), level, facing))
            master = master.below();

        while (isUsableCustomizableSignBlockEntity(blockPosInDirection(left, master, 1), level, facing))
            master = blockPosInDirection(left, master, 1);

        // Scan rectangle while getting width and height as well as absolute and relative positions
        List<BlockPosExtended> signRelative = new ArrayList<>();
        List<BlockPos>         signAbsolute = new ArrayList<>();

        int width = 0;
        int height = 0;

        for (BlockPos rowOrigin = master;
             isUsableCustomizableSignBlockEntity(rowOrigin, level, facing);
             rowOrigin = rowOrigin.above()) {

            int rowWidth = 0;

            for (
                    BlockPos cursor = rowOrigin;
                    isUsableCustomizableSignBlockEntity(cursor, level, facing);
                    cursor = blockPosInDirection(right, cursor, 1)
            ) {

                signRelative.add(BlockPosExtended.getOffset(master, cursor));
                signAbsolute.add(cursor);
                // INTEGRATE HERE

                rowWidth++;
            }

            if (width == 0)
                width = rowWidth;
            else if (rowWidth != width)
                return SignLoopResult.failure("Not a rectangle!");

            height++;

            if (height > getMaxSignHeight() || width > getMaxSignWidth())
                return SignLoopResult.failure(
                        String.format(
                                "Sign exceeds maximum dimensions! Max dimensions: %d x %d",
                                getMaxSignWidth(), getMaxSignHeight()
                        )
                );
        }

        ImmutableList<BorderProperty> borders = determineBorders(signAbsolute, right);

        return new SignLoopResult(
                ImmutableList.copyOf(signRelative),
                ImmutableList.copyOf(signAbsolute),
                borders,
                master,
                width, height,
                null
        );
    }

    /**
     * Determines which sign gets which border
     * @param signAbsolute The list of detected signs in the customizable sign
     * @param rightDir The rightDir direction from the observer's perspective
     * @return {@code ImmutableList<BorderProperty>} in order of {@code signAbsolute}
     */
    private static ImmutableList<BorderProperty> determineBorders(List<BlockPos> signAbsolute, Direction rightDir) {
        Set<BlockPos> hashedSignPositions = new HashSet<>(signAbsolute);
        List<BorderProperty> results = new ArrayList<>(signAbsolute.size());
        Direction leftDir = rightDir.getOpposite();

        for (BlockPos pos : signAbsolute) {
            BlockPos above = pos.above();
            BlockPos below = pos.below();

            boolean up = !hashedSignPositions.contains(above);
            boolean down = !hashedSignPositions.contains(below);
            boolean right = !hashedSignPositions.contains(pos.relative(rightDir));
            boolean left = !hashedSignPositions.contains(pos.relative(leftDir));

            boolean upRight = !hashedSignPositions.contains(above.relative(rightDir));
            boolean upLeft = !hashedSignPositions.contains(above.relative(leftDir));
            boolean downRight = !hashedSignPositions.contains(below.relative(rightDir));
            boolean downLeft = !hashedSignPositions.contains(below.relative(leftDir));

            results.add(new BorderProperty(
                    up, right, down, left,
                    upRight, upLeft, downRight, downLeft
            ));
        }

        return ImmutableList.copyOf(results);
    }

    private static PoleLoopResult poleDetection(
            BlockPos masterPos, Level level, Direction facingInverse, Direction right, int width, int height
    ) {
        List<BlockPosExtended> poleRelative = new ArrayList<>();
        List<BlockPos>         poleAbsolute = new ArrayList<>();

        BlockPos topLeftBehind = blockPosInDirection(facingInverse, masterPos.above(height - 1), 1);

        for (int col = 0; col < width; col++) {
            BlockPos cursor = blockPosInDirection(right, topLeftBehind, col);

            while (level.getBlockEntity(cursor) instanceof SignPoleBlockEntity) {
                poleRelative.add(BlockPosExtended.getOffset(masterPos, cursor));
                poleAbsolute.add(cursor);
                cursor = cursor.below();
            }
        }

        return new PoleLoopResult(
                ImmutableList.copyOf(poleRelative),
                ImmutableList.copyOf(poleAbsolute),
                null
        );
    }
}
