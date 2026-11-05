package at.tobiazsh.myworld.traffic_addition.customizable_sign.elements;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAdditionClient;
import at.tobiazsh.myworld.traffic_addition.font.*;
import at.tobiazsh.myworld.traffic_addition.imgui.child_windows.property_viewer.PropertyViewerExtension;
import at.tobiazsh.myworld.traffic_addition.imgui.child_windows.property_viewer.PropertyViewerTextExtension;
import at.tobiazsh.myworld.traffic_addition.imgui.fonts.ImGuiFontManager;
import at.tobiazsh.myworld.traffic_addition.imgui.fonts.ImGuiFontRenderer;
import at.tobiazsh.myworld.traffic_addition.imgui.fonts.LivingImGuiFont;
import at.tobiazsh.myworld.traffic_addition.imgui.utils.ImGuiColor;
import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.sign.elements.TextElement;
import at.tobiazsh.myworld.traffic_addition.sign.elements.id.ElementId;
import at.tobiazsh.myworld.traffic_addition.sign.elements.id.ParentElementId;
import at.tobiazsh.myworld.traffic_addition.texture.Textures;
import at.tobiazsh.myworld.traffic_addition.utils.math.BlockPosFloat;
import at.tobiazsh.myworld.traffic_addition.utils.DirectionUtils;
import at.tobiazsh.myworld.traffic_addition.sign.elements.BaseElementInterface;
import at.tobiazsh.myworld.traffic_addition.sign.elements.LegacyTextElement;
import at.tobiazsh.myworld.traffic_addition.rendering.CustomRenderLayer;
import at.tobiazsh.myworld.traffic_addition.rendering.text.CustomTextRenderer;
import dev.tobiazsh.imguib3d.client.font.ImGuiFont;
import dev.tobiazsh.imguib3d.client.texture.ImGuiTexture;
import dev.tobiazsh.imguib3d.client.texture.ImGuiTextureFactory;
import imgui.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.Direction;
import com.mojang.math.Axis;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static at.tobiazsh.myworld.traffic_addition.utils.DirectionUtils.getRightSideDirection;

public class TextElementClient extends TextElement implements ClientElementInterface {

    private static boolean triedRegisterTexture = false;
    private static @Nullable ImGuiTexture textIcon;

    private FontDefinition fontDefinition;

    /// Whether something happened that generally prevents the text element from rendering.
    private final AtomicBoolean canRender = new AtomicBoolean(true);

    /// Whether something happened that prevents the text element from rendering in ImGui.
    private final AtomicBoolean canRenderImGui = new AtomicBoolean(true);

    /// Whether something happened that prevents the text element from rendering in Minecraft.
    private final AtomicBoolean canRenderMinecraft = new AtomicBoolean(true);

    private CompletableFuture<Void> imguiFontFuture = null;
    private CompletableFuture<Void> minecraftFontFuture = null;

    private LivingImGuiFont imGuiFont = null;
    private DynamicMinecraftFont minecraftFont = null;

    private static final String DEFAULT_TEXT = "Lorem Ipsum";

    public TextElementClient(
            float x, float y,
            float width, float height,
            float rotation,
            float factor,
            boolean shouldCalculateWidth,
            ServerFont font,
            String text,
            ElementId id, ParentElementId parentId
    ) {
        super(x, y, width, height, rotation, factor, null, text, shouldCalculateWidth, id, parentId);
        this.font = font;
    }

    /**
     * Renders the text element in an ImGui Context.
     */
    @Override
    public void renderImGui(float scale) {

        createTextures(); // TODO: Check whether to export it to somewhere else

        if (!canRender.get() || !canRenderImGui.get())
            return; // Do not render if the font failed to load

        if (this.font == null)
            return; // Do not render if we have no font.

        if (!loadImGuiFontAsync())
            return; // Font is still loading or something went wrong, so we can't render (yet)

        // Another check just to be sure nothing changed in the meantime
        if (imGuiFont == null || !imGuiFont.imguiFont().isLoaded()) {
            // Should not happen as it SHOULD be caught by the canImGui or loadMinecraftFontAsync
            MyWorldTrafficAddition.LOGGER.error("Font is invalid! Can't render text!");
            imGuiFont = null;
            imguiFontFuture = null;
            return;
        }

        if (!this.isWidthCalculated()) {
            float width = calculateTextSize(this.imGuiFont.imguiFont(), this.getText()).x;
            float height = calculateTextSize(this.imGuiFont.imguiFont(), this.getText()).y;
            this.setWidth(width);
            this.setHeight(height);
            this.setWidthCalculated(true);
        }

        this.imGuiFont.use(); // Mark the font as used so it doesn't get unloaded while rendering

        ImGuiFontRenderer.renderFont(
                this.imGuiFont.imguiFont(),
                (int) this.x,
                (int) this.y,
                (int) this.width,
                (int) this.height,
                ImColor.rgba(color[0], color[1], color[2], color[3]),
                rotation,
                0.5f, 0.5f,
                this.getText()
        );
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

        if (!canRender.get() || !canRenderMinecraft.get())
            return; // Do not render if the font failed to load

        if (this.font == null)
            return; // Do not render if we have no font.

        if (!loadMinecraftFontAsync())
            return; // Font is still loading or something went wrong, so we can't render (yet)

        if (minecraftFont == null) {
            // Should not happen as it SHOULD be caught by the canRenderMinecraft or loadMinecraftFontAsync
            MyWorldTrafficAddition.LOGGER.error("Minecraft font is invalid! Can't render text!");
            minecraftFont = null;
            minecraftFontFuture = null;
            return; // Do not render if the font is not loaded yet.
        }

        this.minecraftFont.use(); // Mark the font as used so it doesn't get unloaded while rendering

        float w = this.calcBlocks(getWidth());
        float h = this.calcBlocks(getHeight());
        float x = this.calcBlocks(getX());
        float y = this.calcBlocks(getY());
        float rotation = this.getRotation();
        float[] color = this.getColor();

        final CustomTextRenderer textRenderer = minecraftFont.renderer();

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
    private static ImVec2 calculateTextSize(
            @NotNull ImGuiFont font,
            String text
    ) {
        if (text.isEmpty()) {
            return new ImVec2(0, 0);
        }

        return font.getImFont().calcTextSizeA(
                font.getFontKey().fontSize(),
                Float.MAX_VALUE,
                -1,
                text
        );
    }

    public static TextElementClient createNew() {
        @Nullable FontDefinition defaultFont = FontRepository.getInstance().getFallbackFont();
        ServerFont font;

        if (defaultFont == null) {
            // Not loaded yet... shouldn't happen, but just in case.
            // Due to the rendering check (this.font == null) in the rendering method(s),
            // this is safe to do, as it will just not render if there's no font present.
            font = null;
        } else {
            font = ServerFont.of(
                    defaultFont.getLogicalFont().fontHash(),
                    FontRepository.DEFAULT_FONT_SIZE
            );
        }

        return new TextElementClient(
                0, 0,
                0,0,
                0,
                1,
                true,
                font,
                DEFAULT_TEXT,
                null, // Null, so it registers itself automatically
                BaseElementInterface.MAIN_CANVAS_ID
        );
    }

    public static TextElementClient fromLegacy(LegacyTextElement legacy) {
        return new TextElementClient(
                legacy.getX(), legacy.getY(),
                legacy.getWidth(), legacy.getHeight(),
                legacy.getRotation(),
                legacy.getFactor(),
                !legacy.isWidthCalculated(),
                ServerFont.of(
                        LegacyFontMapper.LEGACY_FONTS.get(legacy.getFont().getFontPath()).fontHash(),
                        legacy.getFont().getFontSize()
                ),
                legacy.getText(),
                null, // Null, so it registers itself automatically
                BaseElementInterface.MAIN_CANVAS_ID
        );
    }

    /**
     * Tries to load the fallback font from the FontRepository.
     * If no fallback font is found, it logs an error and returns false.
     */
    private boolean loadFallbackFont() {
        FontDefinition fallbackFontDef = FontRepository.getInstance().getFallbackFont();

        if (fallbackFontDef == null) {
            MyWorldTrafficAddition.LOGGER.error("No fallback font found in FontRepository! Can't load font!");
            return false;
        }

        this.fontDefinition = fallbackFontDef;
        return true;
    }

    /**
     * Tries to load the text element's desired font definition or default if not found.
     * <p>Does nothing if the font definition is already loaded.
     * <p>Logs an error if no font definition is available.
     * {@link #canRender} to false.
     *
     * @return Whether any font has been successfully loaded.
     */
    private boolean loadFontDefinitionOrDefault() {
        if (fontDefinition != null)
            return true; // Already loaded

        FontDefinition fontDef = FontRepository.getInstance().getByHash(font.font().fontHash());

        if (fontDef != null) {
            this.fontDefinition = fontDef;
            return true;
        }

        MyWorldTrafficAddition.LOGGER.debug(
                "Font definition not found for font hash: {}. Using fallback font!",
                this.getFont().font().fontHash()
        );

        if (!loadFallbackFont()) {
            MyWorldTrafficAddition.LOGGER.error("No fallback font found! Can't load font!");
            return false;
        }

        return true;
    }

    /**
     * Tries to load the ImGui font asynchronously. If the font is already loaded, it does nothing.
     * <p>If the font fails to load, it sets {@link #canRenderImGui}
     *    or {@link #canRender} to false (depending on the severity) and logs an error.
     * <p>If the font is still loading, it does nothing and waits for the future to complete.
     *
     * @return Whether any font is loaded successfully and rendering can continue.
     */
    private boolean loadImGuiFontAsync() {
        if (imguiFontFuture != null) {
            if (!imguiFontFuture.isDone())
                return false; // Font is still loading, just wait

            if (imguiFontFuture.isCompletedExceptionally() || imguiFontFuture.isCancelled()) {
                MyWorldTrafficAddition.LOGGER.warn("Font future completed exceptionally or cancelled!");
                canRenderImGui.set(false);
                return false;
            }

            return true;
        }

        if (!loadFontDefinitionOrDefault()) {
            this.imguiFontFuture = CompletableFuture.failedFuture(new IOException("No font definition found!"));
            canRender.set(false);
            return false;
        }

        imguiFontFuture = ImGuiFontManager.getInstance().loadOrGet(
                fontDefinition,
                (int) this.getFont().fontSize()
        ).thenAcceptAsync(imguiFont -> {
            if (imguiFont.isEmpty()) {
                MyWorldTrafficAddition.LOGGER.error("Couldn't load ImGui font of any font definition!");
                canRenderImGui.set(false);
                return;
            }

            this.imGuiFont = imguiFont.get();
        }, Minecraft.getInstance());

        return false; // Font has just been scheduled to load, so we can't render yet
    }

    /**
     * Tries to load the Minecraft font asynchronously. If the font is already loaded, it does nothing.
     * <p>If the font fails to load, it sets {@link #canRenderMinecraft}
     *    or {@link #canRender} to false (depending on the severity) and logs an error.
     * <p>If the font is still loading, it does nothing and waits for the future to complete.
     *
     * @return Whether any font is loaded successfully and rendering can continue.
     */
    private boolean loadMinecraftFontAsync() {
        if (minecraftFontFuture != null) {
            if (!minecraftFontFuture.isDone())
                return false; // Still loading, wait

            if (minecraftFontFuture.isCompletedExceptionally() || minecraftFontFuture.isCancelled()) {
                MyWorldTrafficAddition.LOGGER.warn("Minecraft font future completed exceptionally or cancelled!");
                canRenderMinecraft.set(false);
            }

            return true; // Font loaded successfully
        }

        if (!loadFontDefinitionOrDefault()) {
            this.minecraftFontFuture = CompletableFuture.failedFuture(new IOException("No font definition found!"));
            canRender.set(false);
            return false;
        }

        minecraftFontFuture = MinecraftFontManager.getInstance().loadOrGet(
                fontDefinition,
                fontDefinition.getLifetime()
        ).thenAcceptAsync(minecraftFont -> {
            if (minecraftFont == null) {
                MyWorldTrafficAddition.LOGGER.error("Couldn't load Minecraft font of any font definition!");
                canRenderMinecraft.set(false);
                return;
            }

            this.minecraftFont = minecraftFont;
        }, Minecraft.getInstance());

        return false; // Font has just been scheduled to load, so we can't render yet
    }

    @Override
    public void onPaste() {
        // ClientElementManagtryAssignDefaultFonter.getInstance().registerElement(this);
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

    private void createTextures() {
        if (textIcon == null && !triedRegisterTexture) {
            try {
                textIcon = ImGuiTextureFactory.fromStream(
                        Objects.requireNonNull(Textures.class.getResourceAsStream("/assets/myworld_traffic_addition/textures/imgui/icons/text.png")),
                        "text_icon"
                );
            } catch (IOException e) {
                MyWorldTrafficAddition.LOGGER.error("Failed to load text icon texture: {}", e.getMessage());
            }
        }

        triedRegisterTexture = true;
    }

    @Override
    public @Nullable ImGuiTexture getThumbnailTexture() {
        return textIcon;
    }

    @Override
    public String getGenericTranslatableName() {
        return "Text Element";
    }

    @Override
    public Optional<PropertyViewerExtension> getPropertyViewerExtension() {
        return Optional.of(new PropertyViewerTextExtension());
    }

    public void dispose() {
        try {
            if (imguiFontFuture != null && !imguiFontFuture.isDone()) {
                imguiFontFuture.cancel(true);
            }
        } catch (Exception e) {
            MyWorldTrafficAddition.LOGGER.error("Failed to dispose TextElementClient font future: {}", e.getMessage());
        }
        imguiFontFuture = null;
        imGuiFont = null;
    }
}
