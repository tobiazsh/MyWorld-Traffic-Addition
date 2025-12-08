package at.tobiazsh.myworld.traffic_addition.rendering.renderstates;

import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.ClientElementInterface;
import at.tobiazsh.myworld.traffic_addition.data.CustomizableSignData;
import at.tobiazsh.myworld.traffic_addition.utils.BorderProperty;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.util.math.BlockPos;

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

    public String signDataJsonString = null; // copied from block entity
    public CustomizableSignData signData = new CustomizableSignData();

    public List<ClientElementInterface> clientElements = new ArrayList<>();

    public String signPoleDistancesString = "";
    public String signDistancesString = "";
}
