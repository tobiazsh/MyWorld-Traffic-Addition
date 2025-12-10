package at.tobiazsh.myworld.traffic_addition.block_entities;

import at.tobiazsh.myworld.traffic_addition.blocks.SignBlock;
import at.tobiazsh.myworld.traffic_addition.utils.math.Coordinates;
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

    private int rotation = 0;
    private int shapeType;
    private String texturePath;

    public void setTexturePath(String texturePath) {
        this.texturePath = texturePath;

        markDirty();
        world.emitGameEvent(GameEvent.BLOCK_CHANGE, this.getPos(), GameEvent.Emitter.of(null, this.getCachedState()));
        this.getWorld().updateListeners(this.getPos(), this.getCachedState(), this.getCachedState(), Block.NOTIFY_ALL);
    }

    public String getTexturePath() {
        return texturePath;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;

        markDirty();
    }

    public int getRotation() {
        return this.rotation;
    }

    public SignBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, SignBlock.SIGN_SHAPE shapeType, String texturePath) {
        super(type, pos, state);
        this.shapeType = SignBlock.getSignSelectionEnumInt(shapeType);
        this.texturePath = texturePath;
    }

    @Override
    protected void writeData(WriteView writeView) {
        super.writeData(writeView);
        writeView.putInt("Rotation", this.rotation);
        writeView.putInt("ShapeType", this.shapeType);
        writeView.putString("Texture", this.texturePath);
    }

    @Override
    protected void readData(ReadView readView) {
        super.readData(readView);
        this.rotation = OptionalUtils.getOrDefault("Rotation", readView::getOptionalInt, 0, "SignBlockEntity.readNbt");
        this.shapeType = OptionalUtils.getOrDefault("ShapeType", readView::getOptionalInt, 2, "SignBlockEntity.readNbt"); // Default to 2 (Round Sign)
        this.texturePath = OptionalUtils.getOrDefault("Texture", readView::getOptionalString, "", "SignBlockEntity.readNbt");
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
