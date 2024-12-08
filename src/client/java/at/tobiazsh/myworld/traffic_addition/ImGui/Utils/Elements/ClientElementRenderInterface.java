package at.tobiazsh.myworld.traffic_addition.ImGui.Utils.Elements;

import at.tobiazsh.myworld.traffic_addition.Utils.Elements.BaseElement;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;

public interface ClientElementRenderInterface {
    float zOffset = 0.075f;

    void renderImGui();
    void renderMinecraft(BaseElement element, int indexInList, int csbeHeight, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, Direction facing);
}
