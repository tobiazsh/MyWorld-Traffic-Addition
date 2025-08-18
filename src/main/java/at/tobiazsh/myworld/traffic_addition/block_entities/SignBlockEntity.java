package at.tobiazsh.myworld.traffic_addition.block_entities;

import at.tobiazsh.myworld.traffic_addition.blocks.SignBlock;
import at.tobiazsh.myworld.traffic_addition.utils.Coordinates;
import at.tobiazsh.myworld.traffic_addition.utils.OptionalUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class SignBlockEntity extends BlockEntity {

    private Coordinates backstepCoords = new Coordinates(0f, 0f, 0.55f, Direction.NORTH);
    private int rotation = 0;
    private int shapeType;
    private String textureId;

    public void setTextureId(String textureId) {
        this.textureId = textureId;

        markDirty();
        world.emitGameEvent(GameEvent.BLOCK_CHANGE, this.getPos(), GameEvent.Emitter.of(null, this.getCachedState()));
        this.getWorld().updateListeners(this.getPos(), this.getCachedState(), this.getCachedState(), Block.NOTIFY_ALL);
    }

    public String getTextureId() {
        return textureId;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;

        markDirty();
    }

    public int getRotation() {
        return this.rotation;
    }

    public SignBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, SignBlock.SIGN_SHAPE shapeType, String textureId) {
        super(type, pos, state);
        this.shapeType = SignBlock.getSignSelectionEnumInt(shapeType);
        this.textureId = textureId;
    }

    public Coordinates getBackstepCoords() {
        return backstepCoords;
    }

    public void setBackstepCoords(Coordinates backstepCoords) {
        this.backstepCoords = backstepCoords;
    }

    @Override
    protected void writeData(WriteView writeView) {
        super.writeData(writeView);
        writeView.putInt("Rotation", this.rotation);
        writeView.putInt("ShapeType", this.shapeType);
        writeView.putString("Texture", this.textureId);
        writeView.putString("Backstep", constructBackstepString(this.backstepCoords));
    }

    @Override
    protected void readData(ReadView readView) {
        super.readData(readView);
        this.rotation = OptionalUtils.getOrDefault("Rotation", readView::getOptionalInt, 0, "SignBlockEntity.readNbt");
        this.shapeType = OptionalUtils.getOrDefault("ShapeType", readView::getOptionalInt, 2, "SignBlockEntity.readNbt"); // Default to 2 (Round Sign)
        this.textureId = OptionalUtils.getOrDefault("Texture", readView::getOptionalString, "", "SignBlockEntity.readNbt");
        this.backstepCoords = deconstructBackstepString(OptionalUtils.getOrDefault("Backstep", readView::getOptionalString, "", "SignBlockEntity.readNbt"));
    }

    private static String constructBackstepString(Coordinates coordinates) {
        String[] backstepStringParts = {String.valueOf(coordinates.x), String.valueOf(coordinates.y), String.valueOf(coordinates.z), coordinates.direction.asString()};
        String backstepString = String.join("%", backstepStringParts);
        return backstepString;
    }

    private static Coordinates deconstructBackstepString(String string) {
        String[] backstepStringParts = string.split("%");

        if(backstepStringParts.length < 3) return new Coordinates(0, 0, 0, Direction.NORTH);

        Coordinates coordinates = new Coordinates(Float.parseFloat(backstepStringParts[0]), Float.parseFloat(backstepStringParts[1]), Float.parseFloat(backstepStringParts[2]), Direction.byId(backstepStringParts[3]));
        return coordinates;
    }

    @Override
    public @Nullable Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return createNbt(registryLookup);
    }
}
