package at.tobiazsh.myworld.traffic_addition.customizable_sign.elements;

import at.tobiazsh.myworld.traffic_addition.imgui.utils.Color;
import at.tobiazsh.myworld.traffic_addition.imgui.utils.ImGuiFont;
import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.rendering.renderers.CustomizableSignBlockEntityRenderer;
import at.tobiazsh.myworld.traffic_addition.utils.BasicFont;
import at.tobiazsh.myworld.traffic_addition.utils.BlockPosFloat;
import at.tobiazsh.myworld.traffic_addition.utils.DirectionUtils;
import at.tobiazsh.myworld.traffic_addition.utils.elements.BaseElementInterface;
import at.tobiazsh.myworld.traffic_addition.utils.elements.TextElement;
import at.tobiazsh.myworld.traffic_addition.rendering.CustomRenderLayer;
import at.tobiazsh.myworld.traffic_addition.rendering.CustomTextRenderer;
import imgui.ImFont;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.ImVec4;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;

import org.joml.Matrix4f;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Future;

import static at.tobiazsh.myworld.traffic_addition.imgui.utils.FontManager.registerFontAsync;
import static at.tobiazsh.myworld.traffic_addition.utils.CustomMinecraftFont.getTextRendererByPath;
import static at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAdditionClient.imgui;
import static at.tobiazsh.myworld.traffic_addition.utils.DirectionUtils.getRightSideDirection;

public class TextElementClient extends TextElement implements ClientElementInterface {

    private Future<ImGuiFont> fontFuture; // Future for the font
    private ImGuiFont imGuiFont; // Font after future is done

    private static final String defaultFontPath = "/assets/" + MyWorldTrafficAddition.MOD_ID + "/font/dejavu_sans.ttf";
    private static final int defaultFontSize = 24;
    private static final String defaultText = "Lorem Ipsum";

    public TextElementClient(
            float x, float y,
            float width, float height,
            float rotation,
            float factor,
            boolean shouldCalculateWidth,
            BasicFont font,
            String text,
            UUID id, UUID parentId
    ) {
        super(x, y, width, height, rotation, factor, null, text, shouldCalculateWidth, parentId, id);
        this.fontFuture = registerFontAsync(font.getFontPath(), font.getFontSize());
        this.font = font;
    }

    /**
     * Renders the text element in an ImGui Context.
     */
    @Override
    public void renderImGui(float scale) {

        // Without font, no text :)
        if (imGuiFont == null && !fontFuture.isDone()) {
            MyWorldTrafficAddition.LOGGER.debug("Font is null! Can't render text!");
            return;
        }

        // For better readability, so I don't have to compact everything in one bracelet
        boolean isImGuiFontNull = (imGuiFont == null);
        boolean doFontPathsMatch = !isImGuiFontNull && Objects.equals(font.getFontPath(), imGuiFont.getFontPath());
        boolean doFontSizesMatch = !isImGuiFontNull && (imGuiFont.getFontSize() == font.getFontSize());

        // Check font for updates/new fonts
        if (isImGuiFontNull || !doFontPathsMatch || !doFontSizesMatch) {
            try {
                imGuiFont = fontFuture.get();
            } catch (Exception e) {
                MyWorldTrafficAddition.LOGGER.error("Font is null but async task was completed! Exception produced: {}", e.getMessage());
            }
        }

        ImGui.pushFont(this.imGuiFont.font);

        if (!this.isWidthCalculated()) {
            float width = calculateTextSize(this.imGuiFont.font, this.getText()).x;
            float height = calculateTextSize(this.imGuiFont.font, this.getText()).y;
            this.setWidth(width);
            this.setHeight(height);
            this.setWidthCalculated(true);
        }

        float[] color = getColor();

        this.imGuiFont.renderText(
                ImGui.getWindowDrawList(),
                this.getText(),
                new ImVec2(x * scale, y * scale),
                new ImVec2(width * scale, height * scale),
                rotation,
                new ImVec4(color[0], color[1], color[2], color[3])
        );

        ImGui.popFont();
    }

    @Override
    public void renderMinecraft(int indexInList, int csbeHeight, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, Direction facing) {

        float w = this.calcBlocks(getWidth());
        float h = this.calcBlocks(getHeight());
        float x = this.calcBlocks(getX());
        float y = this.calcBlocks(getY());
        float rotation = this.getRotation();
        float[] color = this.getColor();

        CustomTextRenderer textRenderer = (CustomTextRenderer) getTextRendererByPath(this.getFont().getFontPath());

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

        float zOffset = CustomizableSignBlockEntityRenderer.zOffsetRenderLayer + (indexInList + 1) * CustomizableSignBlockEntityRenderer.elementDistancingRenderLayer;
        BlockPosFloat zPos = new BlockPosFloat(0, 0, 0).offset(facing, ClientElementInterface.zOffset + ((indexInList + 1) * 0.00001f));
        BlockPosFloat renderPos = new BlockPosFloat(0, 0, 0)
                .offset(facing.getOpposite(), 1)
                .offset(getRightSideDirection(facing.getOpposite()), x)
                .offset(Direction.UP, csbeHeight - 1)
                .offset(Direction.DOWN, y)
                .offset(Direction.DOWN, h * 0.35f); // Fix Up/Down alignment

        matrices.push();

        // Move to correct position
        matrices.translate(zPos.x, zPos.y, zPos.z);
        matrices.translate(renderPos.x, renderPos.y, renderPos.z);

        // Rotate to face the same direction as the block
        matrices.translate(0.5, 0.5, 0.5);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(DirectionUtils.getFacingRotation(facing)));
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
                0,0, zOffset,
                Color.toHexARGB(color),
                false,
                positionMatrix,
                vertexConsumers,
                CustomRenderLayer.TextLayering.LayeringType.VIEW_OFFSET_Z_LAYERING_BACKWARD_INTENSITY,
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

    public static TextElementClient createNew() {
        return new TextElementClient(
                0, 0,
                0,0,
                0,
                1,
                true,
                new BasicFont(defaultFontPath, defaultFontSize),
                defaultText,
                null, // Null, so it registers itself automatically
                BaseElementInterface.MAIN_CANVAS_ID
        );
    }

    @Override
    public void onPaste() {
        // ClientElementManager.getInstance().registerElement(this);
    }

    @Override
    public void onImport() {
        // ClientElementManager.getInstance().registerElement(this);
    }

    @Override
    public ClientElementInterface copy() {
        TextElementClient copy = new TextElementClient(
                this.getX(), this.getY(),
                this.getWidth(), this.getHeight(),
                this.getRotation(),
                this.getFactor(),
                false,
                this.getFont(),
                this.getText(),
                null,
                this.getParentId()
        );

        copy.setName(this.getName());
        copy.setColor(this.getColor());

        return copy;
    }

    @Override
    public void setFont(BasicFont font) {
        super.setFont(font);
        this.fontFuture = registerFontAsync(font.getFontPath(), font.getFontSize()); // Register new font future so you don't have to re-open the GUI to see the new font
    }
}
