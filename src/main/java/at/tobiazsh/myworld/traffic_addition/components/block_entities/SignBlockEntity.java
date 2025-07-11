package at.tobiazsh.myworld.traffic_addition.components.block_entities;

import at.tobiazsh.myworld.traffic_addition.ModVars;
import at.tobiazsh.myworld.traffic_addition.utils.Coordinates;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
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

    public SignBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, ModVars.SIGN_SELECTION_TYPE shapeType, String textureId) {
        super(type, pos, state);
        this.shapeType = ModVars.getSignSelectionEnumInt(shapeType);
        this.textureId = textureId;
    }

    public Coordinates getBackstepCoords() {
        return backstepCoords;
    }

    public void setBackstepCoords(Coordinates backstepCoords) {
        this.backstepCoords = backstepCoords;
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.putInt("Rotation", this.rotation);
        nbt.putInt("ShapeType", this.shapeType);
        nbt.putString("Texture", this.textureId);
        nbt.putString("Backstep", constructBackstepString(this.backstepCoords));
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);

        this.rotation = nbt.getInt("Rotation");
        this.shapeType = nbt.getInt("ShapeType");
        this.textureId = nbt.getString("Texture");
        this.backstepCoords = deconstructBackstepString(nbt.getString("Backstep"));
    }

    private static String constructBackstepString(Coordinates coordinates) {
        String[] backstepStringParts = {String.valueOf(coordinates.x), String.valueOf(coordinates.y), String.valueOf(coordinates.z), coordinates.direction.getName()};
        String backstepString = String.join("%", backstepStringParts);
        return backstepString;
    }

    private static Coordinates deconstructBackstepString(String string) {
        String[] backstepStringParts = string.split("%");

        if(backstepStringParts.length < 3) return new Coordinates(0, 0, 0, Direction.NORTH);

        Coordinates coordinates = new Coordinates(Float.parseFloat(backstepStringParts[0]), Float.parseFloat(backstepStringParts[1]), Float.parseFloat(backstepStringParts[2]), Direction.byName(backstepStringParts[3]));
        return coordinates;
    }

    @Override
    public @Nullable Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound nbt = super.toInitialChunkDataNbt(registryLookup);
        nbt.putString("Texture", this.textureId);
        nbt.putInt("ShapeType", shapeType);
        nbt.putInt("Rotation", rotation);
        return nbt;
    }
}
