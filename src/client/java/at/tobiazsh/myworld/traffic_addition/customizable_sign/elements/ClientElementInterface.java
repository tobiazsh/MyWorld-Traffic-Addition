package at.tobiazsh.myworld.traffic_addition.customizable_sign.elements;

import at.tobiazsh.myworld.traffic_addition.utils.elements.BaseElementInterface;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;

public interface ClientElementInterface extends BaseElementInterface {
    float zOffset = 0.08f;

    void renderImGui(float scale);
    void renderMinecraft(int indexInList, int csbeHeight, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, Direction facing);

    /**
     * Executes an action when the element is pasted (must be called in the method associated with pasting!)
     */
    void onPaste();

    /**
     * Executes an action when the element is imported (must be called in the method associated with importing!)
     */
    void onImport();

    /**
     * Creates a copy of the element.
     * @return a new instance of the element with the same properties.
     */
    ClientElementInterface copy();
}
