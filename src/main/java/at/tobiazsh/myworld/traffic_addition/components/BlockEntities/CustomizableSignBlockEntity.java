package at.tobiazsh.myworld.traffic_addition.components.BlockEntities;


/*
 * @created 07/09/2024 (DD/MM/YYYY) - 00:30
 * @project MyWorld Traffic Addition
 * @author Tobias
 */

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.Utils.Elements.BaseElement;
import at.tobiazsh.myworld.traffic_addition.Utils.Elements.ImageElement;
import at.tobiazsh.myworld.traffic_addition.Utils.SignStyleJson;
import at.tobiazsh.myworld.traffic_addition.components.Blocks.CustomizableSignBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.List;

import static at.tobiazsh.myworld.traffic_addition.ModBlockEntities.CUSTOMIZABLE_SIGN_BLOCK_ENTITY;

public class CustomizableSignBlockEntity extends BlockEntity {

    private boolean isMaster = true;
    private boolean renderingState = true;
    private boolean isInitialized = false;

    private String borderModelPath = "customizable_sign_block_border_all";
    private BlockPos masterPos;
    private String signPolePositions = "";
    private String signPositions = "";
    private String signTextureJson = "";

    private int rotation = 0;
    private int height = 1;
    private int width = 1;

    // Texture variables
    // These variables are temporary and deleted after the program is closed. It is solely used to reduce the amount of operations it would take to update the textures each render. If it'd be this way, it can easily slow down the game by a lot if there are lots of these signs present.
    public List<String> backgroundStylePieces = new ArrayList<>();
    public List<BaseElement> elements = new ArrayList<>();

    public void updateTextureVars() {
        if (!isMaster) return;
        if (signTextureJson == null || signTextureJson.isEmpty()) return;

        backgroundStylePieces = SignStyleJson.deconstructStyleToArray(new SignStyleJson().convertStringToJson(signTextureJson)).reversed();
        backgroundStylePieces.replaceAll(s -> s.replaceFirst("/assets/".concat(MyWorldTrafficAddition.MOD_ID).concat("/"), ""));
        elements = SignStyleJson.deconstructElementsToArray(new SignStyleJson().convertStringToJson(signTextureJson));

        elements.replaceAll(element -> {
            if (element instanceof ImageElement) {
                ((ImageElement) element).setResourcePath(((ImageElement)element).getResourcePath().replaceFirst("/assets/".concat(MyWorldTrafficAddition.MOD_ID).concat("/"), ""));
            }

            return element;
        });

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
        return renderingState;
    }

    public void setRenderingState(boolean render) {
        renderingState = render;
        updateGame();
    }

    public void setBorderType(String borderModelPath) {
        this.borderModelPath = borderModelPath;
        updateGame();
    }

    public String getBorderType() {
        return borderModelPath;
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

        System.out.println("SignTexture" + json);

        updateGame();
    }

    public String getSignTextureJson() {
        return this.signTextureJson;
    }

    private String constructMasterPosString(BlockPos pos) {
        String[] posList = { String.valueOf(pos.getX()), String.valueOf(pos.getY()), String.valueOf(pos.getZ()) };
        String posStr = String.join("%", posList);
        return posStr;
    }

    private BlockPos deconstructMasterPosString(String posStr) {
        String[] posList = posStr.split("%");
        BlockPos pos = new BlockPos(Integer.parseInt(posList[0]), Integer.parseInt(posList[1]), Integer.parseInt(posList[2]));
        return pos;
    }

    public CustomizableSignBlockEntity(BlockPos pos, BlockState state) {
        super(CUSTOMIZABLE_SIGN_BLOCK_ENTITY, pos, state);

        this.masterPos = pos;
    }

    private void nbtWrite(NbtCompound nbt) {
        nbt.putString("BorderModelPath", borderModelPath);
        nbt.putBoolean("IsMaster", isMaster);
        nbt.putString("MasterPos", constructMasterPosString(masterPos));
        nbt.putString("SignPolePositions", signPolePositions);
        nbt.putBoolean("RenderingState", renderingState);
        nbt.putString("SignPositions", signPositions);
        nbt.putInt("Rotation", rotation);
        nbt.putInt("Width", width);
        nbt.putInt("Height", height);
        nbt.putBoolean("IsInitialized", isInitialized);
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

        borderModelPath = nbt.getString("BorderModelPath");
        isMaster = nbt.getBoolean("IsMaster");
        masterPos = deconstructMasterPosString(nbt.getString("MasterPos"));
        signPolePositions = nbt.getString("SignPolePositions");
        renderingState = nbt.getBoolean("RenderingState");
        signPositions = nbt.getString("SignPositions");
        rotation = nbt.getInt("Rotation");
        width = nbt.getInt("Width");
        height = nbt.getInt("Height");
        isInitialized = nbt.getBoolean("IsInitialized");
        signTextureJson = nbt.getString("SignTexture");

        updateTextureVars();
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

    // I am VERY well aware that this is awful, but I found that this would be the easiest method for me :). Please correct if possible!
    public static String getBorderName(boolean top, boolean right, boolean left, boolean bottom, String id) {
        Map<List<Boolean>, String> borderMap = new HashMap<>();
        borderMap.put(Arrays.asList(true, false, false, false), id + "_top");
        borderMap.put(Arrays.asList(false, true, false, false), id + "_right");
        borderMap.put(Arrays.asList(false, false, true, false), id + "_left");
        borderMap.put(Arrays.asList(false, false, false, true), id + "_bottom");

        borderMap.put(Arrays.asList(true, true, false, false), id + "_top_right");
        borderMap.put(Arrays.asList(true, false, true, false), id + "_top_left");
        borderMap.put(Arrays.asList(false, true, false, true), id + "_bottom_right");
        borderMap.put(Arrays.asList(false, false, true, true), id + "_bottom_left");

        borderMap.put(Arrays.asList(true, false, false, true), id + "_top_bottom");
        borderMap.put(Arrays.asList(false, true, true, false), id + "_left_right");

        borderMap.put(Arrays.asList(false, true, true, true), id + "_not_top");
        borderMap.put(Arrays.asList(true, false, true, true), id + "_not_right");
        borderMap.put(Arrays.asList(true, true, false, true), id + "_not_left");
        borderMap.put(Arrays.asList(true, true, true, false), id + "_not_bottom");

        borderMap.put(Arrays.asList(true, true, true, true), id + "_all");
        borderMap.put(Arrays.asList(false, false, false, false), id + "_none");

        return borderMap.getOrDefault(Arrays.asList(top, right, left, bottom), "customizable_sign_block_border_all");
    }

    public static List<Boolean> getBorderListBoundingBased(BlockPos masterPos, World world) {
        List<Boolean> borders = new ArrayList<>();
        Direction NOT_FACING = getRightSideDirection(getFacing(masterPos, world).getOpposite());

        if (world.getBlockEntity(masterPos.up()) instanceof CustomizableSignBlockEntity) {
            borders.add(false);
        } else { borders.add(true); }

        if (world.getBlockEntity(getBlockPosAtDirection(NOT_FACING, masterPos, 1)) instanceof CustomizableSignBlockEntity) {
            borders.add(false);
        } else { borders.add(true); }

        if (world.getBlockEntity(getBlockPosAtDirection(NOT_FACING.getOpposite(), masterPos, 1)) instanceof CustomizableSignBlockEntity) {
            borders.add(false);
        } else { borders.add(true); }

        if (world.getBlockEntity(masterPos.down()) instanceof CustomizableSignBlockEntity) {
            borders.add(false);
        } else { borders.add(true); }

        return borders;
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
            List<String> blockCoordinates = new ArrayList<>();

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

    public static Direction getFacing(BlockPos masterPos, World world) {
        return world.getBlockEntity(masterPos).getCachedState().get(CustomizableSignBlock.FACING);
    }

    public static BlockPos getCheckPos(Direction dir, BlockPos masterPos) {
        switch (dir) {
            default -> { return masterPos.west(); }
            case EAST -> { return masterPos.north(); }
            case SOUTH -> { return masterPos.east(); }
            case WEST -> { return masterPos.south(); }
        }
    }

    public static Direction getRightSideDirection(Direction dir) {
        switch (dir) {
            default -> { return Direction.EAST; }
            case EAST -> { return Direction.SOUTH; }
            case SOUTH -> { return Direction.WEST; }
            case WEST -> { return Direction.NORTH; }
        }
    }

    public static BlockPos getBlockPosAtDirection(Direction dir, BlockPos pos, int offset) {
        if (offset == 0) return pos;

        switch (dir) {
            default -> { return pos.north(offset); }
            case EAST -> { return pos.east(offset); }
            case SOUTH -> { return pos.south(offset); }
            case WEST -> { return pos.west(offset); }
        }
    }
}
