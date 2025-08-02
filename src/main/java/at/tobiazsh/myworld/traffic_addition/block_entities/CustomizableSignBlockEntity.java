package at.tobiazsh.myworld.traffic_addition.block_entities;


/*
 * @created 07/09/2024 (DD/MM/YYYY) - 00:30
 * @project MyWorld Traffic Addition
 * @author Tobias
 */

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.utils.BorderProperty;
import at.tobiazsh.myworld.traffic_addition.utils.CustomizableSignData;
import at.tobiazsh.myworld.traffic_addition.utils.elements.BaseElement;
import at.tobiazsh.myworld.traffic_addition.utils.elements.BaseElementInterface;
import at.tobiazsh.myworld.traffic_addition.utils.elements.ImageElement;
import at.tobiazsh.myworld.traffic_addition.blocks.CustomizableSignBlock;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static at.tobiazsh.myworld.traffic_addition.ModBlockEntities.CUSTOMIZABLE_SIGN_BLOCK_ENTITY;

public class CustomizableSignBlockEntity extends BlockEntity {

    private int width = 1;

    private boolean isMaster = true;
    private boolean isRendered = true;
    private boolean isInitialized = false;
    private boolean updateBackgroundTexture = false;
    private boolean updateOccurred = false;

    private BorderProperty borders = new BorderProperty(
            true, true, true, true,
            true, true, true, true
    );

    private BlockPos masterPos;
    private String signPolePositions = "";
    private String signPositions = "";
    private String signTextureJson = "";

    private int rotation = 0;
    private int height = 1;

    // Texture variables
    // These variables are temporary and deleted after the program is closed. It is solely used to reduce the amount of operations it would take to update the textures each render. If it'd be this way, it can easily slow down the game by a lot if there are lots of these signs present.
    public List<String> backgroundStylePieces = new ArrayList<>();
    public List<BaseElement> elements = new ArrayList<>();

    public void setUpdateOccurred(boolean updateOccurred) {
        this.updateOccurred = updateOccurred;
    }

    public boolean hasUpdateOccured() {
        return updateOccurred;
    }

    public CustomizableSignBlockEntity(BlockPos pos, BlockState state) {
        super(CUSTOMIZABLE_SIGN_BLOCK_ENTITY, pos, state);

        this.masterPos = pos;
    }



    public void updateTextureVars() {
        if (!isMaster) return;
        if (signTextureJson == null || signTextureJson.isEmpty()) return;
        if (this.world == null) return;

        setUpdateBackgroundTexture(true);

        elements = CustomizableSignData.deconstructElementsToArray(new CustomizableSignData().setJson(signTextureJson));
        elements = BaseElementInterface.unpackList(elements);

        elements.replaceAll(element -> {
            if (element instanceof ImageElement) {
                ((ImageElement) element).setResourcePath(((ImageElement)element).getResourcePath().replaceFirst("/assets/".concat(MyWorldTrafficAddition.MOD_ID).concat("/"), ""));
            }

            return element;
        });

        updateOccurred = true;
    }

    public static void setTransmittedTexture(String json, ServerPlayerEntity player) {
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

        if (!jsonObject.has("texture")) {
            MyWorldTrafficAddition.LOGGER.error("Couldn't set transmitted texture because json data does not contain the texture data! Received Data: {}", json);
            return;
        }

        String texture = jsonObject.get("texture").toString();

        if (!jsonObject.has("blockEntityPosition")) {
            MyWorldTrafficAddition.LOGGER.error("Couldn't set transmitted texture because json data does not contain the block entity position data! Received Data: {}", json);
            return;
        }

        JsonObject blockEntityData = jsonObject.getAsJsonObject("blockEntityPosition");

        if (!blockEntityData.has("x") || !blockEntityData.has("y") || !blockEntityData.has("z")) {
            MyWorldTrafficAddition.LOGGER.error("Couldn't set transmitted texture because json data does not contain intact block entity position data! Received Data: {}", json);
            return;
        }

        BlockPos pos = new BlockPos(blockEntityData.get("x").getAsInt(), blockEntityData.get("y").getAsInt(), blockEntityData.get("z").getAsInt());

        BlockEntity blockEntity = player.getWorld().getBlockEntity(pos);

        if (!(blockEntity instanceof CustomizableSignBlockEntity)) {
            MyWorldTrafficAddition.LOGGER.error("Couldn't set transmitted texture because block entity at position {} is not a CustomizableSignBlockEntity!", pos);
            return;
        }

        Objects.requireNonNull(player.getWorld().getServer()).execute(() -> ((CustomizableSignBlockEntity) blockEntity).setSignTextureJson(texture));
        ((CustomizableSignBlockEntity) blockEntity).updateTextureVars();
    }

    public void setHeight(int height) {
        this.height = height;
        updateGame();
    }

    public void setWidth(int width) {
        this.width = width;
        updateGame();
    }

    public int getHeight() {
        return this.height;
    }

    public int getWidth() {
        return this.width;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
        updateGame();
    }

    public int getRotation() {
        return this.rotation;
    }

    public void setSignPositions(String signPositions) {
        this.signPositions = signPositions;
        updateGame();
    }

    public String getSignPositions() {
        return signPositions;
    }

    public String getSignPolePositions() {
        return signPolePositions;
    }

    public void setSignPolePositions(String signPolePositions) {
        this.signPolePositions = signPolePositions;
        updateGame();
    }

    public boolean isRendering() {
        return isRendered;
    }

    public void setRendered(boolean render) {
        isRendered = render;
        updateGame();
    }

    public void setBorderType(BorderProperty borders) {
        this.borders = borders;
        updateGame();
    }

    public BorderProperty getBorderType() {
        return borders;
    }

    public void setMaster(boolean value) {
        this.isMaster = value;
        updateGame();
    }

    public boolean isMaster() {
        return isMaster;
    }

    public void setMasterPos(BlockPos masterPos) {
        this.masterPos = masterPos;
        updateGame();
    }

    public BlockPos getMasterPos() {
        return masterPos;
    }

    public void setSignTextureJson(String json) {
        this.signTextureJson = json;
        updateGame();
    }

    public String getSignTextureJson() {
        return this.signTextureJson;
    }

    private String constructMasterPosString(BlockPos pos) {
        String[] posList = { String.valueOf(pos.getX()), String.valueOf(pos.getY()), String.valueOf(pos.getZ()) };
        return String.join("%", posList);
    }

    private BlockPos deconstructMasterPosString(String posStr) {
        String[] posList = posStr.split("%");
        BlockPos pos = new BlockPos(Integer.parseInt(posList[0]), Integer.parseInt(posList[1]), Integer.parseInt(posList[2]));
        return pos;
    }

    private void nbtWrite(NbtCompound nbt) {
        nbt.putString("Borders", borders.toString());
        nbt.putBoolean("IsMaster", isMaster);
        nbt.putString("MasterPos", constructMasterPosString(masterPos));
        nbt.putString("SignPolePositions", signPolePositions);
        nbt.putBoolean("RenderingState", isRendered);
        nbt.putString("SignPositions", signPositions);
        nbt.putInt("Rotation", rotation);
        nbt.putInt("Width", width);
        nbt.putInt("Height", height);
        nbt.putBoolean("IsInitialized", isInitialized);

        // TODO: REMOVE IN AUGUST 2025
        if (!signTextureJson.isBlank()) {
            CustomizableSignData signData = new CustomizableSignData();
            signData.setJsonString(signTextureJson);

            if (CustomizableSignData.styleMatchesOldVersion(signData)) {
                CustomizableSignData.updateToNewVersion(signData);
                this.signTextureJson = signData.jsonString;
            }
        }
        // REMOVE IN AUGUST 2025 END

        nbt.putString("SignTexture", signTextureJson);
    }



    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);

        nbtWrite(nbt);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);

        BorderProperty borders;

        if (!nbt.contains("Borders") && !getStringOrDefault(nbt, "BorderModelPath", "").isBlank()) {
            borders = convertOldBorderStringToBorderProperty(getStringOrDefault(nbt, "BorderModelPath", ""), "customizable_sign_block");
        } else {
            borders = BorderProperty.valueOf(getStringOrDefault(nbt, "Borders", BorderProperty.DEFAULT));
        } // CONVERSION TO NEW VERSION

        this.borders = borders;
        isMaster = getBooleanOrDefault(nbt, "IsMaster", true);
        masterPos = deconstructMasterPosString(getStringOrDefault(nbt, "MasterPos", constructMasterPosString(getPos())));
        signPolePositions = getStringOrDefault(nbt, "SignPolePositions", "");
        isRendered = getBooleanOrDefault(nbt, "RenderingState", true);
        signPositions = getStringOrDefault(nbt, "SignPositions", "");
        rotation = getIntOrDefault(nbt, "Rotation", 0);
        width = getIntOrDefault(nbt, "Width", 1);
        height = getIntOrDefault(nbt, "Height", 1);
        isInitialized = getBooleanOrDefault(nbt, "IsInitialized", false);
        signTextureJson = getStringOrDefault(nbt, "SignTexture", "{}");

        updateTextureVars();
    }

    private String getStringOrDefault(NbtCompound nbt, String key, String defaultValue) {
        Optional<String> valueOpt = nbt.getString(key);
        if (valueOpt.isEmpty()) {
            MyWorldTrafficAddition.LOGGER.error("CustomizableSignBlockEntity: NBT key '{}' not found, using default value '{}'", key, defaultValue);
            return defaultValue;
        }
        return valueOpt.get();
    }

    private boolean getBooleanOrDefault(NbtCompound nbt, String key, boolean defaultValue) {
        Optional<Boolean> valueOpt = nbt.getBoolean(key);
        if (valueOpt.isEmpty()) {
            MyWorldTrafficAddition.LOGGER.error("CustomizableSignBlockEntity: NBT key '{}' not found, using default value '{}'", key, defaultValue);
            return defaultValue;
        }
        return valueOpt.get();
    }

    private int getIntOrDefault(NbtCompound nbt, String key, int defaultValue) {
        Optional<Integer> valueOpt = nbt.getInt(key);
        if (valueOpt.isEmpty()) {
            MyWorldTrafficAddition.LOGGER.error("CustomizableSignBlockEntity: NBT key '{}' not found, using default value '{}'", key, defaultValue);
            return defaultValue;
        }
        return valueOpt.get();
    }



    @Override
    public @Nullable Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound nbt = super.toInitialChunkDataNbt(registryLookup);

        nbtWrite(nbt);

        return nbt;
    }

    /**
     * Returns a BorderProperty object that represents the bounding box of the CustomizableSignBlockEntity based on the surrounding blocks.
     * Does extensive neighbour-checking to determine which borders and corners should be present.
     */
    public static BorderProperty getBorderListBoundingBased(BlockPos masterPos, World world) {
        Direction rightSideDirection = getRightSideDirection(getFacing(masterPos, world).getOpposite());

        boolean up = false;
        boolean right = false;
        boolean down = false;
        boolean left = false;

        // Corners
        boolean upRight = false;
        boolean upLeft = false;
        boolean downRight = false;
        boolean downLeft = false;

        boolean upIsCustomizableBlockEntity = isUsableCustomizableSignBlockEntity(masterPos.up(), world);
        boolean rightIsCustomizableBlockEntity = isUsableCustomizableSignBlockEntity(getBlockPosAtDirection(rightSideDirection, masterPos, 1), world);
        boolean downIsCustomizableBlockEntity = isUsableCustomizableSignBlockEntity(masterPos.down(), world);
        boolean leftIsCustomizableBlockEntity = isUsableCustomizableSignBlockEntity(getBlockPosAtDirection(rightSideDirection.getOpposite(), masterPos, 1), world);

        boolean downLeftIsCustomizableBlockEntity = isUsableCustomizableSignBlockEntity(getBlockPosAtDirection(rightSideDirection.getOpposite(), masterPos, 1).down(), world);  // Check if down left is a CustomizableSignBlockEntity
        boolean downRightIsCustomizableBlockEntity = isUsableCustomizableSignBlockEntity(getBlockPosAtDirection(rightSideDirection, masterPos, 1).down(), world);               // Check if down right is a CustomizableSignBlockEntity
        boolean upLeftIsCustomizableBlockEntity = isUsableCustomizableSignBlockEntity(getBlockPosAtDirection(rightSideDirection.getOpposite(), masterPos, 1).up(), world);      // Check if up left is a CustomizableSignBlockEntity
        boolean upRightIsCustomizableBlockEntity = isUsableCustomizableSignBlockEntity(getBlockPosAtDirection(rightSideDirection, masterPos, 1).up(), world);                   // Check if up right is a CustomizableSignBlockEntity

        if (!upIsCustomizableBlockEntity) {
            up = true;
            upRight = true;
            upLeft = true;
        }

        if (!rightIsCustomizableBlockEntity) {
            right = true;
            upRight = true;
            downRight = true;
        }

        if (!downIsCustomizableBlockEntity) {
            down = true;
            downRight = true;
            downLeft = true;
        }

        if (!leftIsCustomizableBlockEntity) {
            left = true;
            upLeft = true;
            downLeft = true;
        }

        if (!(world.getBlockEntity(getBlockPosAtDirection(rightSideDirection, masterPos, 1)) instanceof CustomizableSignBlockEntity))
            right = false;

        if (!(world.getBlockEntity(masterPos.down()) instanceof CustomizableSignBlockEntity))
            down = false;

        if (!(world.getBlockEntity(getBlockPosAtDirection(rightSideDirection.getOpposite(), masterPos, 1)) instanceof CustomizableSignBlockEntity))
            left = false;

        return new BorderProperty(up, right, down, left);
    }

    /**
     * Converts the old border string format to a BorderProperty object.
     *
     * @param borderString The old border string format including the name prefix. For example: "customizable_sign_border_top" or "sign_border_not_right".
     * @param name The name prefix that is used in the border string. For example: "customizable_sign" or "sign".
     * @return A BorderProperty object representing the border configuration.
     */
    public static BorderProperty convertOldBorderStringToBorderProperty(String borderString, String name) {
        String withoutName = borderString.replaceFirst(name + "_border_", ""); // Counts the number of underscores in the name and removes the prefix including the underscore

        boolean left = false;
        boolean right = false;
        boolean up = false;
        boolean down = false;

        switch (withoutName) {
            case "top" -> up = true;
            case "right" -> right = true;
            case "bottom" -> down = true;
            case "left" -> left = true;

            case "not_right" -> {
                up = true;
                down = true;
                left = true;
            }

            case "not_left" -> {
                up = true;
                down = true;
                right = true;
            }

            case "not_top" -> {
                right = true;
                down = true;
                left = true;
            }

            case "not_bottom" -> {
                up = true;
                right = true;
                left = true;
            }


            case "top_bottom" -> {
                up = true;
                down = true;
            }

            case "left_right" -> {
                right = true;
                left = true;
            }

            case "bottom_left" -> {
                down = true;
                left = true;
            }

            case "bottom_right" -> {
                down = true;
                right = true;
            }

            case "top_left" -> {
                up = true;
                left = true;
            }

            case "top_right" -> {
                up = true;
                right = true;
            }

            case "all" -> {
                left = true;
                right = true;
                up = true;
                down = true;
            }

            default -> {} // No borders are present
        }

        return new BorderProperty(
                up, right, down, left,
                false, false, false, false // No information about corners. Solution: Re-initialize sign in game or live with it. I am too lazy to implement this right now since FAPI fucked up my whole codebase.
        );
    }

    public static String constructBlockPosListString(List<BlockPos> blockPosList) {
        List<String> blockPoses = new ArrayList<>();

        for (BlockPos pos : blockPosList) {
            List<String> blockPosPiece = new ArrayList<>();
            blockPosPiece.add(java.lang.String.valueOf(pos.getX()));
            blockPosPiece.add(java.lang.String.valueOf(pos.getY()));
            blockPosPiece.add(java.lang.String.valueOf(pos.getZ()));

            String blockPosString = java.lang.String.join("?", blockPosPiece);

            blockPoses.add(blockPosString);
        }

        return java.lang.String.join("%", blockPoses);
    }

    public static List<BlockPos> deconstructBlockPosListString(String blockPosListString) {
        List<String> blockPoses;
        List<BlockPos> blockPosList = new ArrayList<>();

        blockPoses = List.of(blockPosListString.split("%"));

        for (String blockPos : blockPoses) {
            List<String> blockCoordinates;

            blockCoordinates = List.of(blockPos.split("\\?"));

            BlockPos pos = new BlockPos(Integer.parseInt(blockCoordinates.get(0)), Integer.parseInt(blockCoordinates.get(1)), Integer.parseInt(blockCoordinates.get(2)));

            blockPosList.add(pos);
        }

        return blockPosList;
    }

    private void updateGame() {
        markDirty();
        world.emitGameEvent(GameEvent.BLOCK_CHANGE, this.getPos(), GameEvent.Emitter.of(null, this.getCachedState()));
        this.getWorld().updateListeners(this.getPos(), this.getCachedState(), this.getCachedState(), Block.NOTIFY_ALL);
    }

	public boolean isInitialized() {
		return isInitialized;
	}

	public void setInitialized(boolean initialized) {
		isInitialized = initialized;
	}

    public boolean shouldUpdateBackgroundTexture() {
        return updateBackgroundTexture;
    }

    public void setUpdateBackgroundTexture(boolean var) {
        this.updateBackgroundTexture = var;
    }

    public static Direction getFacing(BlockPos pos, World world) {
        return world.getBlockState(pos).get(CustomizableSignBlock.FACING);
    }

    public static Direction getFacing(BlockEntity entity) {
        return entity.getCachedState().get(CustomizableSignBlock.FACING);
    }

    public Direction getFacing() {
        return this.getCachedState().get(CustomizableSignBlock.FACING);
    }


    public static BlockPos getCheckPos(Direction dir, BlockPos masterPos) {
        switch (dir) {
            case EAST -> { return masterPos.north(); }
            case SOUTH -> { return masterPos.east(); }
            case WEST -> { return masterPos.south(); }
            default -> { return masterPos.west(); }
        }
    }

    /**
     * Returns the direction to the right side of the given direction.
     */
    @Contract(pure = true)
    public static Direction getRightSideDirection(Direction dir) {
        switch (dir) {
            case EAST -> { return Direction.SOUTH; }
            case SOUTH -> { return Direction.WEST; }
            case WEST -> { return Direction.NORTH; }
            default -> { return Direction.EAST; }
        }
    }

    /**
     * Returns the position at the given direction and offset from the given position in given direction.
     */
    public static BlockPos getBlockPosAtDirection(Direction dir, BlockPos pos, int offset) {
        if (offset == 0) return pos;

        switch (dir) {
            case EAST -> { return pos.east(offset); }
            case SOUTH -> { return pos.south(offset); }
            case WEST -> { return pos.west(offset); }
            default -> { return pos.north(offset); }
        }
    }

    /**
     * Checks if block entity at given position is a usable CustomizableSignBlockEntity. Usable implies that it's not locked.
     * The check for locking and the locking itself will be implemented in the future.
     * Right now, it only checks if the block entity is an instance of CustomizableSignBlockEntity.
     */
    public static boolean isUsableCustomizableSignBlockEntity(BlockPos pos, World world) {
        return
            world.getBlockEntity(pos) instanceof CustomizableSignBlockEntity;
    }
}
