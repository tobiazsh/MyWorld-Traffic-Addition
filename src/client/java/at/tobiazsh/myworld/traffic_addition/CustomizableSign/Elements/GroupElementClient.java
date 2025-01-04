package at.tobiazsh.myworld.traffic_addition.CustomizableSign.Elements;

import at.tobiazsh.myworld.traffic_addition.ImGui.ChildWindows.SignPreview;
import at.tobiazsh.myworld.traffic_addition.Utils.Elements.GroupElement;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;

public class GroupElementClient extends GroupElement implements ClientElementRenderInterface {

    public GroupElementClient(float x, float y, float width, float height, float rotation) {
        super(x, y, width, height, rotation, null);
    }

    @Override
    public void renderImGui() {
        this.getElements().reversed().forEach(SignPreview::renderElement);
    }

    // NOT NECESSARY
    @Override
    public void renderMinecraft(int indexInList, int csbeHeight, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, Direction facing) {

    }

    public static GroupElementClient fromGroupElement(GroupElement groupElement) {
        GroupElementClient groupElementClient = new GroupElementClient(groupElement.getX(), groupElement.getY(), groupElement.getWidth(), groupElement.getHeight(), groupElement.getRotation());
        groupElementClient.setElements(groupElement.getElements());
        groupElementClient.setColor(groupElement.getColor());
        return groupElementClient;
    }
}
