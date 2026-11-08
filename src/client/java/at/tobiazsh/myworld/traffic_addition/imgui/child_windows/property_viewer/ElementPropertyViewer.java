package at.tobiazsh.myworld.traffic_addition.imgui.child_windows.property_viewer;

import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.ClientElementInterface;
import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.ClientElementManager;
import at.tobiazsh.myworld.traffic_addition.imgui.child_windows.element_list.ElementSelectionCallback;
import at.tobiazsh.myworld.traffic_addition.imgui.fonts.DefaultFonts;
import at.tobiazsh.myworld.traffic_addition.imgui.utils.ImGuiTools;
import dev.tobiazsh.imguib3d.client.font.ImGuiFontScope;
import imgui.ImGui;
import imgui.flag.ImGuiColorEditFlags;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAdditionClient.getTranslatable;
import static at.tobiazsh.myworld.traffic_addition.language.MinecraftTranslationHelper.*;

public class ElementPropertyViewer {

    private boolean isVisible = false;

    private @Nullable PropertyViewerExtension extension;
    private @Nullable ClientElementInterface element;

    private final ImGuiFontScope fontScope = ImGuiFontScope.create();
    private final ClientElementManager clientElementManager;

    private final String id;

    private final ImString nameBuffer = new ImString(256);

    private final int[] widthBuffer  = new int[1];
    private final int[] heightBuffer = new int[1];
    private float lockedRatio = 1f; // width / height

    private final int[] xBuffer = new int[1];
    private final int[] yBuffer = new int[1];

    private final float[] rotationBuffer = new float[1];

    private final float[] colorBuffer = new float[4];

    /** Width in pixels */
    private float canvasWidth = 1;

    /** Height in pixels */
    private float canvasHeight = 1;

    ImBoolean lockAspectRatio = new ImBoolean(false);

    public ElementPropertyViewer(String id, ClientElementManager elementManager) {
        this.id = id;
        this.clientElementManager = elementManager;
    }

    // TODO: Will become more relevant if we allow the canvas size to change dynamically for fine-tuning of sign size.
    public void setCanvasSize(float widthPixels, float heightPixels) {
        this.canvasWidth = widthPixels;
        this.canvasHeight = heightPixels;
    }

    public void clean() {
        this.xBuffer[0] = 0;
        this.yBuffer[0] = 0;
        this.widthBuffer[0] = 0;
        this.heightBuffer[0] = 0;
        this.rotationBuffer[0] = 0;
        this.colorBuffer[0] = 0;
        this.colorBuffer[1] = 0;
        this.colorBuffer[2] = 0;
        this.colorBuffer[3] = 0;

        this.element = null;
        this.nameBuffer.set("unset");
        this.extension = null;
    }

    public void prepare(@NonNull ClientElementInterface element) {
        element.getPropertyViewerExtension().ifPresentOrElse(ext -> {
            this.extension = ext;
            ext.setElement(element);
        }, () -> this.extension = null);

        this.element = element;

        this.nameBuffer.set(element.getName());

        this.widthBuffer[0] = (int) element.getWidth();
        this.heightBuffer[0] = (int) element.getHeight();

        this.xBuffer[0] = (int) element.getX();
        this.yBuffer[0] = (int) element.getY();

        captureRatio();

        // Only copy inside to be able to make buffer final to make sure it doesn't change reference and break ImGui
        System.arraycopy(element.getColor(), 0, this.colorBuffer, 0, 4);

        this.rotationBuffer[0] = element.getRotation();
    }

    public void render() {
        if (!isVisible) return;

        if (ImGui.begin("Element Properties##" + id)) {

            if (element == null)
                renderInformation();
            else
                renderContent();
        }

        ImGui.end();
    }

    public void renderInformation() {
        // Center the information text so it's at least somewhat beautiful...
        String information = getTranslatable(
                "text",
                "sign-editor.property-viewer.invalid-selection" // TR: "No or too many elements selected."
        ).getString();

        float windowWidth = ImGui.getWindowWidth();
        float windowHeight = ImGui.getWindowHeight();
        float textWidth = ImGui.calcTextSize(information).x;
        float textHeight = ImGui.calcTextSize(information).y;

        ImGui.setCursorPosX((windowWidth - textWidth) / 2);
        ImGui.setCursorPosY((windowHeight - textHeight) / 2);

        this.fontScope.push(DefaultFonts.RobotoBold);
        ImGui.text(information);
        this.fontScope.pop();
    }

    public void renderContent() {
        renderDimensions();
        ImGui.separator();
        renderRotation();
        ImGui.separator();
        renderColor();
        ImGui.separator();
        if (extension != null) extension.render(this.id);
    }

    private void renderDimensions() {
        if (this.element == null) return;

        renderSectionTitle(
                getTranslatable("text", "mwta", "sign-editor.property-viewer.dimensions").getString(),
                fontScope
        );

        final float sliderWidth = 100f;
        final float dimensionBeginY = ImGui.getCursorScreenPosY();

        if (ImGui.checkbox(getTranslatable(
                "text", "mwta", "sign-editor.property-viewer.dimensions.lock-ratio"
        ).getString(), lockAspectRatio)) {
            if (lockAspectRatio.get()) captureRatio();
        }

        ImGui.beginGroup(); {
            ImGui.setNextItemWidth(sliderWidth);

            if (ImGui.dragInt(
                    trIC("text.mwta.sign-editor.property-viewer.dimensions.width", id, "widthInput"),
                    widthBuffer, 1f, 0, (int) (canvasWidth - xBuffer[0]), "%d px")
            ) {
                if (lockAspectRatio.get()) {
                    scaleFromWidth(widthBuffer[0]);
                }

                applySize();
            }

            ImGui.setNextItemWidth(sliderWidth);

            if (ImGui.dragInt(
                    trIC("text.mwta.sign-editor.property-viewer.dimensions.height", id, "heightInput"),
                    heightBuffer, 1f, 0, (int) (canvasHeight - yBuffer[0]), "%d px")
            ) {
                int maxWidth = (int) canvasWidth - xBuffer[0];

                if (lockAspectRatio.get()) {
                    scaleFromHeight(heightBuffer[0]);
                }

                applySize();
            }
        } ImGui.endGroup();

        ImGui.sameLine();
        final float spacingPosDim = 10f;
        ImGui.dummy(spacingPosDim, 0f);
        final float separatorX = ImGui.getItemRectMinX() + spacingPosDim / 2f; // center of the dummy
        ImGui.sameLine();

        ImGui.beginGroup(); {

            ImGui.beginGroup(); {
                ImGui.setNextItemWidth(sliderWidth);

                if (ImGui.dragInt(
                        trIC("text.mwta.sign-editor.property-viewer.dimensions.x", id, "xInput"),
                        xBuffer, 1f, 0 , (int) (canvasWidth - widthBuffer[0]))
                ) element.setX(xBuffer[0]);

                ImGui.sameLine();

                if (ImGui.button(trIC(
                        "text.mwta.sign-editor.property-viewer.dimensions.center-x",
                        id, "centerXBtn"))
                ) {
                    xBuffer[0] = (int) ((canvasWidth - element.getWidth()) / 2);
                    applyPosition();
                }
            } ImGui.endGroup();

            ImGui.beginGroup(); {
                ImGui.setNextItemWidth(sliderWidth);

                if (ImGui.dragInt(
                        trIC("text.mwta.sign-editor.property-viewer.dimensions.y", id, "yInput"),
                        yBuffer, 1f, 0, (int) (canvasHeight - heightBuffer[0]))
                ) element.setY(yBuffer[0]);

                ImGui.sameLine();

                if (ImGui.button(trIC(
                        "text.mwta.sign-editor.property-viewer.dimensions.center-y",
                        id, "centerYBtn"))
                ) {
                    yBuffer[0] = (int) ((canvasHeight - element.getHeight()) / 2);
                    applyPosition();
                }
            } ImGui.endGroup();
        } ImGui.endGroup();

        // Vertical separator between desiredWidth/desiredHeight/ratio and X/Y/center controls
        final float lineThickness = 1f;
        ImGui.getWindowDrawList().addLine(
                separatorX,
                dimensionBeginY,
                separatorX,
                ImGui.getCursorScreenPosY() - ImGui.getStyle().getItemSpacingY(),
                ImGui.getColorU32(1f, 1f, 1f, .2f),
                lineThickness
        );
    }

    private void renderRotation() {
        if (this.element == null) return; // Make IntelliJ happy AND it serves as a safety check for my future stupidity

        renderSectionTitle(
                getTranslatable("text", "mwta", "sign-editor.property-viewer.rotation").getString(),
                fontScope
        ); // TR: "Rotation"

        if (ImGui.dragFloat(
                trIC("text.mwta.sign-editor.property-viewer.rotation", id, "rotationInput"),
                rotationBuffer, 1f, 0, 360, "%.1f°"
        )) element.setRotation(rotationBuffer[0]);
    }

    private void renderColor() {
        if (this.element == null) return;

        renderSectionTitle(
                getTranslatable("text", "mwta", "sign-editor.property-viewer.color").getString(),
                fontScope
        ); // TR: "Color"

        ImGui.setNextItemWidth(200f);

        if (ImGui.colorPicker4(
                trIC("text.mwta.sign-editor.property-viewer.color", id, "colorInput"),
                colorBuffer,
                ImGuiColorEditFlags.AlphaBar | ImGuiColorEditFlags.AlphaPreviewHalf | ImGuiColorEditFlags.DisplayHex | ImGuiColorEditFlags.DisplayHSV
        )) element.setColor(colorBuffer);
    }

    private void applySize() {
        if (this.element == null) return;
        this.element.setSize(this.widthBuffer[0], this.heightBuffer[0]);
    }

    private void scaleFromWidth(int desiredWidth) {
        int maxW = Math.max(1, (int) (canvasWidth - xBuffer[0]));
        int maxH = Math.max(1, (int) (canvasHeight - yBuffer[0]));

        int widthLimit = Math.clamp((int) Math.floor(maxH * lockedRatio), 1, maxW);

        int w = Math.clamp(desiredWidth, 1, widthLimit);
        widthBuffer[0]  = w;
        heightBuffer[0] = Math.clamp(Math.round(w / lockedRatio), 1, maxH);
    }

    private void scaleFromHeight(int desiredHeight) {
        int maxW = Math.max(1, (int) (canvasWidth - xBuffer[0]));
        int maxH = Math.max(1, (int) (canvasHeight - yBuffer[0]));

        int heightLimit = Math.clamp((int) Math.floor(maxW / lockedRatio), 1, maxH);

        int h = Math.clamp(desiredHeight, 1, heightLimit);
        heightBuffer[0] = h;
        widthBuffer[0]  = Math.clamp(Math.round(h * lockedRatio), 1, maxW);
    }

    private void applyPosition() {
        if (this.element == null) return;
        this.element.setPosition(this.xBuffer[0], this.yBuffer[0]);
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        this.isVisible = visible;
    }

    public void toggleVisibility() {
        this.isVisible = !this.isVisible;
    }

    public ElementSelectionCallback getSelectionCallback() {
        return selectedIds -> {
            if (selectedIds.size() != 1) {
                clean();
                return;
            }

            var element = clientElementManager.getElementById(selectedIds.iterator().next());
            if (element != null) {
                prepare(element);
            }
        };
    }

    private void captureRatio() {
        if (element == null) return;
        float w = element.getWidth(), h = element.getHeight();
        if (w > 0 && h > 0) lockedRatio = w / h;
    }

    public static void renderSectionTitle(String text, ImGuiFontScope fontScope) {
        fontScope.push(DefaultFonts.RobotoBold);
        ImGui.text(text);
        fontScope.pop();
        ImGuiTools.drawLineMaxX();
    }
}
