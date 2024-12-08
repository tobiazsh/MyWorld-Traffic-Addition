package at.tobiazsh.myworld.traffic_addition.ImGui.Utils.Elements;

import at.tobiazsh.myworld.traffic_addition.ImGui.Utils.Color;
import at.tobiazsh.myworld.traffic_addition.ImGui.Utils.ImGuiFont;
import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.Utils.BlockPosFloat;
import at.tobiazsh.myworld.traffic_addition.Utils.Elements.BaseElement;
import at.tobiazsh.myworld.traffic_addition.Utils.Elements.TextElement;
import imgui.ImFont;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.ImVec4;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

import java.util.Arrays;
import java.util.concurrent.Future;

import static at.tobiazsh.myworld.traffic_addition.ImGui.Utils.FontManager.registerFontAsync;
import static at.tobiazsh.myworld.traffic_addition.Utils.CustomMinecraftFont.getTextRendererByPath;
import static at.tobiazsh.myworld.traffic_addition.components.BlockEntities.CustomizableSignBlockEntity.getRightSideDirection;
import static at.tobiazsh.myworld.traffic_addition.components.Renderers.SignBlockEntityRenderer.getFacingRotation;

public class TextElementClient extends TextElement implements ClientElementRenderInterface {

    private Future<ImGuiFont> fontFuture; // Future for the font
    private ImGuiFont font; // Font after future is done
    private String currentFontPath; // Current font path; Ideally the same as ImGuiFont font
    private float currentFontSize; // Current font size; Ideally the same as ImGuiFont font

    private TextElement referenceElement; // Reference element for the text element

    public TextElementClient(float x, float y, float rotation, float factor, String fontPath, float fontSize, String text, TextElement referenceElement) {
        super(x, y, referenceElement.getWidth(), referenceElement.getHeight(), rotation, factor, null, text, referenceElement.isWidthCalculated());
        this.fontFuture = registerFontAsync(fontPath, fontSize);
        this.referenceElement = referenceElement;
        this.currentFontPath = fontPath;
        this.currentFontSize = fontSize;
    }

    private static ImGui imgui = new ImGui();

    /**
     * Renders the text element in an ImGui Context.
     */
    @Override
    public void renderImGui() {

        if (font == null && fontFuture.isDone()) {
            try {
                font = fontFuture.get();
            } catch (Exception e) {
                MyWorldTrafficAddition.LOGGER.error("Font is null but async task was completed! Exception produced: {}", e.getMessage());
            }
        }

        if (font == null) {
            MyWorldTrafficAddition.LOGGER.debug("Font is null! Can't render text!");
            return;
        }

        // Implement basically everything else

        ImGui.pushFont(this.font.font);

        if (!this.referenceElement.isWidthCalculated()) {
            float width = calculateTextSize(this.font.font, this.getText()).x;
            float height = calculateTextSize(this.font.font, this.getText()).y;
            this.referenceElement.setWidth(width);
            this.referenceElement.setHeight(height);
            this.referenceElement.setWidthCalculated(true);
        }

        float[] color = referenceElement.getColor();
        this.font.renderText(ImGui.getWindowDrawList(), this.getText(), new ImVec2(x, y), new ImVec2(this.width, this.height), rotation, new ImVec4(color[0], color[1], color[2], color[3]));
        ImGui.popFont();
    }

    @Override
    public void renderMinecraft(BaseElement element, int indexInList, int csbeHeight, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, Direction facing) {
        if (!(element instanceof TextElement)) {
            MyWorldTrafficAddition.LOGGER.error("Element provided in renderMinecraft() Method is not a TextElement!");
            return;
        }

        float w = element.calcBlocks(element.getWidth());
        float h = element.calcBlocks(element.getHeight());
        float x = element.calcBlocks(element.getX());
        float y = element.calcBlocks(element.getY());
        float rotation = element.getRotation();
        float[] color = element.getColor();

        TextRenderer textRenderer = getTextRendererByPath(((TextElement) element).getFont().getFontPath());

        if (textRenderer == null) {
            MyWorldTrafficAddition.LOGGER.error("TextRenderer is null! Can't render text!");
            return;
        }

        float textWidth = textRenderer.getWidth(this.getText());
        float textHeight = textRenderer.fontHeight;
        float scaleX = 1 / textWidth;
        float scaleY = 1 / textHeight * 0.6f; // 0.6f is a magic number to make it look not-stretched apparently ¯\_(ツ)_/¯
        float effectiveWidthScale = w * scaleX;
        float effectiveHeightScale = h * scaleY;

        BlockPosFloat zPos = new BlockPosFloat(0, 0, 0).offset(facing, zOffset + ((indexInList + 1) * 0.005f));
        BlockPosFloat renderPos = new BlockPosFloat(0, 0, 0)
                .offset(facing.getOpposite(), 1)
                .offset(getRightSideDirection(facing.getOpposite()), x)
                .offset(Direction.UP, csbeHeight - 1)
                .offset(Direction.DOWN, y)
                .offset(Direction.DOWN, h * 0.5f * 0.5f); // Fix Up/Down alignment

        matrices.push();

        // Move to correct position
        matrices.translate(zPos.x, zPos.y, zPos.z);
        matrices.translate(renderPos.x, renderPos.y, renderPos.z);

        // Rotate to face the same direction as the block
        matrices.translate(0.5, 0.5, 0.5);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(getFacingRotation(facing)));
        matrices.translate(-0.5, -0.5, -0.5);

        // Turn by 180 degrees, because it's inverted
        matrices.translate(0.5, 0.5, 0.5);
        matrices.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees(180));
        matrices.translate(-0.5, -0.5, -0.5);

        // Rotate by given rotation
        matrices.translate(w * 0.5f, h * 0.6f * 0.5f, 0.0f); // Translate to text center
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotation)); // Apply rotation
        matrices.translate(-w * 0.5f, -h * 0.6f * 0.5f, 0.0f); // Translate back

        // Scale up to match size
        matrices.scale(effectiveWidthScale, effectiveHeightScale, 1);

        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();

        textRenderer.draw(
                this.getText(),
                0,0,
                Color.toHexRGB(Arrays.copyOf(color, 3)), // Remove alpha as it's not supported apparently
                false,
                positionMatrix,
                vertexConsumers,
                TextRenderer.TextLayerType.NORMAL,
                0,
                light
        );

        matrices.pop();
    }

    /**
     * Recalculates the width and height the text requires.
     */
    private static ImVec2 calculateTextSize(ImFont font, String text) {
        if (font == null) {
            MyWorldTrafficAddition.LOGGER.debug("Font is not loaded! Can't calculate text size!");
            return new ImVec2(0, 0);
        }

        ImGui.pushFont(font);

        float width = imgui.calcTextSize(text).x;
        float height = imgui.calcTextSize(text).y;

        ImGui.popFont();

        return new ImVec2(width, height);
    }

    /**
     * Creates a new TextElementClient object from a TextElement object
     * @param textElement The TextElement object to create the TextElementClient object from
     * @return The created TextElementClient object
     */
    public static TextElementClient fromTextElement(TextElement textElement) {
        return new TextElementClient(textElement.getX(), textElement.getY(), textElement.getRotation(), textElement.getFactor(), textElement.getFont().getFontPath(), textElement.getFont().getFontSize(), textElement.getText(), textElement);
    }
}
