package at.tobiazsh.myworld.traffic_addition.customizable_sign.elements;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAdditionClient;
import at.tobiazsh.myworld.traffic_addition.imgui.utils.ImGuiColor;
import at.tobiazsh.myworld.traffic_addition.imgui.utils.ImGuiFont;
import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.font.BasicFont;
import at.tobiazsh.myworld.traffic_addition.texture.Textures;
import at.tobiazsh.myworld.traffic_addition.utils.math.BlockPosFloat;
import at.tobiazsh.myworld.traffic_addition.utils.DirectionUtils;
import at.tobiazsh.myworld.traffic_addition.sign.elements.BaseElementInterface;
import at.tobiazsh.myworld.traffic_addition.sign.elements.TextElement;
import at.tobiazsh.myworld.traffic_addition.rendering.CustomRenderLayer;
import at.tobiazsh.myworld.traffic_addition.rendering.text.CustomTextRenderer;
import imgui.ImFont;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.ImVec4;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.Direction;
import com.mojang.math.Axis;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.jspecify.annotations.NonNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import static at.tobiazsh.myworld.traffic_addition.imgui.utils.FontManager.registerFontAsync;
import static at.tobiazsh.myworld.traffic_addition.font.CustomMinecraftFont.getTextRendererByPath;
import static at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAdditionClient.imgui;
import static at.tobiazsh.myworld.traffic_addition.utils.DirectionUtils.getRightSideDirection;

public class TextElementClient extends TextElement implements ClientElementInterface {

    private static int textIconId;

    static {
        Minecraft.getInstance().execute(() -> textIconId = Textures.smartRegisterTexture("/assets/myworld_traffic_addition/textures/imgui/icons/text.png").getTextureId());
    }

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
        this.font = font;
    }

    /**
     * Renders the text element in an ImGui Context.
     */
    @Override
    public void renderImGui(float scale) {

        if (imGuiFont == null && fontFuture == null) {
            if (font != null)
                fontFuture = registerThisFont(); // No font future yet, register it

            return;
        }

        if (imGuiFont == null && !fontFuture.isDone()) {
            MyWorldTrafficAddition.LOGGER.debug("Font is not ready yet! Can't render text!");
            return; // not ready yet
        }

        if (imGuiFont == null) {
            try {
                imGuiFont = fontFuture.get(); // blockiert nicht, weil isDone() true
                if (imGuiFont == null || imGuiFont.isInvalid()) {
                    return; // safety
                }
            } catch (Exception e) {
                MyWorldTrafficAddition.LOGGER.error("Font loading failed", e);
                return;
            }
        }

        // Another check just to be sure nothing changed in the meantime
        if (imGuiFont.isInvalid() || !imGuiFont.font.isLoaded() || imGuiFont.font.getScale() <= 0) {
            MyWorldTrafficAddition.LOGGER.error("Font is invalid! Can't render text!");
            imGuiFont = null;
            fontFuture = null;
            return;
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
    public void renderMinecraft(
            @NonNull SubmitNodeCollector queue,
            int indexInList,
            int csbeHeight,
            PoseStack poseStack,
            int light,
            Direction facing
    ) {
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

        float textWidth = textRenderer.width(this.getText());
        float textHeight = textRenderer.lineHeight;
        float scaleX = 1 / textWidth;
        float scaleY = 1 / textHeight * 0.6f; // 0.6f is a magic number to make it look not-stretched apparently ¯\_(ツ)_/¯
        float effectiveWidthScale = w * scaleX;
        float effectiveHeightScale = h * scaleY;

        float viewDistance = MyWorldTrafficAdditionClient.getClientPreferences().customizableSigns.viewDistance.getOrDefault();
        float elementDistancing = MyWorldTrafficAdditionClient.getClientPreferences().customizableSigns.elementDistancing.getOrDefault();
        float zOffset = viewDistance + (indexInList + 1) * elementDistancing;
        BlockPosFloat zPos = new BlockPosFloat(0, 0, 0).offset(facing, ClientElementInterface.zOffset + ((indexInList + 1) * 0.00001f));
        BlockPosFloat renderPos = new BlockPosFloat(0, 0, 0)
                .offset(facing.getOpposite(), 1)
                .offset(getRightSideDirection(facing.getOpposite()), x)
                .offset(Direction.UP, csbeHeight - 1)
                .offset(Direction.DOWN, y)
                .offset(Direction.DOWN, h * 0.35f); // Fix Up/Down alignment

        poseStack.pushPose();

        // Move to correct position
        poseStack.translate(zPos.x, zPos.y, zPos.z);
        poseStack.translate(renderPos.x, renderPos.y, renderPos.z);

        // Rotate to face the same direction as the block
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(DirectionUtils.getFacingRotation(facing)));
        poseStack.translate(-0.5, -0.5, -0.5);

        // Turn by 180 degrees, because it's inverted
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Axis.ZN.rotationDegrees(180));
        poseStack.translate(-0.5, -0.5, -0.5);

        // Rotate by given rotation
        poseStack.translate(w * 0.5f, h * 0.6f * 0.5f, 0.0f); // Translate to text center
        poseStack.mulPose(Axis.ZP.rotationDegrees(rotation)); // Apply rotation
        poseStack.translate(-w * 0.5f, -h * 0.6f * 0.5f, 0.0f); // Translate back

        // Scale up to match size
        poseStack.scale(effectiveWidthScale, effectiveHeightScale, 1);

        Matrix4f positionMatrix = poseStack.last().pose();

        textRenderer.draw(
                this.getText(),
                0,0, zOffset,
                ImGuiColor.toHexARGB(color),
                false,
                queue,
                poseStack,
                CustomRenderLayer.TextLayering.LayeringType.VIEW_OFFSET_Z_LAYERING_BACKWARD_INTENSITY,
                0,
                light
        );

        poseStack.popPose();
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

    /**
     * Registers the font of this element asynchronously.
     * @return A CompletableFuture that will complete with the registered ImGuiFont.
     */
    private @Nullable CompletableFuture<ImGuiFont> registerThisFont() {
        if (font == null || font.getFontPath() == null) {
            MyWorldTrafficAddition.LOGGER.warn("Tried to register TextElementClient font but font or its properties are null! Operation aborted!");
            return null;
        }

        return registerFontAsync(font.getFontPath(), font.getFontSize());
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
        if (font == null) {
            MyWorldTrafficAddition.LOGGER.error("Tried to set TextElementClient font to null! Operation aborted!");
            return;
        }

        super.setFont(font);
        this.fontFuture = null;
        this.imGuiFont = null;
        this.fontFuture = registerFontAsync(font.getFontPath(), font.getFontSize()); // Register new font future so you don't have to re-open the GUI to see the new font
    }

    @Override
    public void renderPreview(float w, float h) {
        ImGui.image(textIconId, w, h);
    }

    @Override
    public void dispose() {
        try {
            if (fontFuture != null && !fontFuture.isDone()) {
                fontFuture.cancel(true);
            }
        } catch (Exception e) {
            MyWorldTrafficAddition.LOGGER.error("Failed to dispose TextElementClient font future: {}", e.getMessage());
        }
        fontFuture = null;
        imGuiFont = null;
    }
}
