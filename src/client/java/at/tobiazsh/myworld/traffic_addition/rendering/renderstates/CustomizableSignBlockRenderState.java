package at.tobiazsh.myworld.traffic_addition.rendering.renderstates;

import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.ClientElementInterface;
import at.tobiazsh.myworld.traffic_addition.data.Background;
import at.tobiazsh.myworld.traffic_addition.data.CustomizableSignTextureData;
import at.tobiazsh.myworld.traffic_addition.utils.BorderProperty;
import at.tobiazsh.myworld.traffic_addition.utils.math.BlockPosExtended;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class CustomizableSignBlockRenderState extends BlockEntityRenderState {
    public int rotation;
    public int height;
    public int width;

    public boolean isRendering;
    public boolean isMaster;
    public boolean isInitialized;

    public BlockPos masterBlockPos;
    public BorderProperty borders;

    public CustomizableSignTextureData textureData = new CustomizableSignTextureData(Background.TRANSPARENT, new ArrayList<>());

    public List<ClientElementInterface> clientElements = new ArrayList<>();

    public List<BlockPosExtended> signPositionsRelative = new ArrayList<>();
    public List<BlockPosExtended> signPolePositionsRelative = new ArrayList<>();
}
