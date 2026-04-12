package at.tobiazsh.myworld.traffic_addition.imgui.child_windows.popups;

import at.tobiazsh.myworld.traffic_addition.data.Background;
import at.tobiazsh.myworld.traffic_addition.data.CustomizableSignTextureData;
import at.tobiazsh.myworld.traffic_addition.imgui.ImGuiImpl;
import at.tobiazsh.myworld.traffic_addition.texture.SpriteAtlasManager;
import at.tobiazsh.myworld.traffic_addition.texture.sign.BackgroundLoader;
import at.tobiazsh.myworld.traffic_addition.utils.Color;
import imgui.ImGui;
import imgui.flag.ImGuiColorEditFlags;
import net.minecraft.resources.Identifier;

import java.util.Arrays;
import java.util.Objects;

import static at.tobiazsh.myworld.traffic_addition.language.JenguaTranslator.tr;

public class BackgroundSelectorPopup {

    private static final String DEFAULT_TEXTURE = "austria:default"; // Austria Default Road Sign Texture
    private static final Color DEFAULT_COLOR = new Color(255, 255, 255, 255); // White

    private enum BackgroundType {
        COLOR("Color"),
        NONE("None"),
        PATTERN("Pattern");

        public final String translationName;

        BackgroundType(String translationName) {
            this.translationName = translationName;
        }
    }

    private boolean shouldOpen = false;

    private BackgroundType backgroundType;

    private final Background oldBackground;

    private String id;

    private final String windowTitle;

    private final CustomizableSignTextureData textureData;

    private float[] color = { 1f, 1f, 1f, 1f }; // Pure White

    public BackgroundSelectorPopup(CustomizableSignTextureData textureData, String id) {
        this.oldBackground = textureData.getBackground();
        this.textureData = textureData;
        this.backgroundType = extractTypeFromBackground(oldBackground);
        this.id = id;
        this.windowTitle = tr("ImGui.Child.PopUps.BackgroundSelector", "Choose Background") + "##" + id;

        if (textureData.getBackground().isColor()) {
            Color col = textureData.getBackground().color;

            color = new float[] {
                    col.r() / 255f, // Does NOT produce a NPE (see isColor() method)
                    col.g() / 255f,
                    col.b() / 255f,
                    col.a() / 255f
            };
        }
    }

    public void render() {
        ImGui.setNextWindowSize(500, 400);
        ImGui.pushFont(ImGuiImpl.Roboto);
        if (ImGui.beginPopupModal(windowTitle)) {

            ImGui.pushFont(ImGuiImpl.RobotoBold);
            ImGui.text(tr("ImGui.Child.PopUps.BackgroundSelector", "Background Type"));
            ImGui.popFont();

            if (ImGui.beginCombo("##bgType_" + id, tr("ImGui.Main.Background", backgroundType.translationName))) {

                Arrays.stream(BackgroundType.values()).forEach(bgType -> {
                    if (ImGui.selectable(
                        tr("ImGui.Main.Background", bgType.translationName),
                        Objects.equals(bgType, this.backgroundType))
                    ) {
                        this.backgroundType = bgType;
                    }
                });

                ImGui.endCombo();
            }

            switch(backgroundType) {
                case COLOR -> renderColorOptions();
                case PATTERN -> renderPatternOptions();
                default -> {} // Also don't render anything on "None" type
            }

            ImGui.spacing();
            ImGui.separator();
            ImGui.spacing();

            if (ImGui.button(tr("Global", "Cancel"))) {
                textureData.setBackground(oldBackground);
                ImGui.closeCurrentPopup();
            }

            ImGui.sameLine();

            if (ImGui.button(tr("Global", "Okay"))) {
                ImGui.closeCurrentPopup();
            }

            ImGui.endPopup();
        }

        if (shouldOpen) {
            ImGui.openPopup(windowTitle);
            shouldOpen = false;
        }

        ImGui.popFont();
    }

    private void renderColorOptions() {
        if (textureData.getBackground().color == null) {
            this.color = DEFAULT_COLOR.toFloatRGBA();
            textureData.setBackground(new Background(new Color(this.color)));
        } else {
            this.color = textureData.getBackground().color.toFloatRGBA();
        }

        if (ImGui.colorPicker4(
                tr("Global", "Color Picker") + "##" + id,
                this.color,
                ImGuiColorEditFlags.AlphaBar | ImGuiColorEditFlags.AlphaPreviewHalf
        )) { // Translatable text for "Color Picker"
            textureData.setBackground(new Background(new Color(this.color)));
        }
    }

    private void renderPatternOptions() {
        String texture;

        if (textureData.getBackground().texture == null) {
            texture = DEFAULT_TEXTURE;
            textureData.setBackground(new Background(texture)); // Set new background to default texture for preview
        } else {
            texture = textureData.getBackground().texture; // Else just set the current texture
        }

        if (ImGui.beginCombo("##patternSelect_" + id, tr("ImGui.Main.Background", texture))) {
            BackgroundLoader.BACKGROUND_SPRITES.forEach(bgSprite -> {
                if (ImGui.selectable(
                    tr("ImGui.Main.Background", bgSprite.getAtlasId().toString()),
                    Objects.equals(bgSprite, SpriteAtlasManager.INSTANCE.getSpriteAtlas(Identifier.parse(texture)))
                )) {
                    textureData.setBackground(new Background(bgSprite.getAtlasId().toString()));
                }
            });

            ImGui.endCombo();
        }
    }

    private static BackgroundType extractTypeFromBackground(Background bg) {
        if (Objects.equals(bg.color, new Color(0, 0, 0, 0)))
            return BackgroundType.NONE;

        if (bg.isColor())
            return BackgroundType.COLOR;

        return BackgroundType.PATTERN;
    }

    public void open() {
        shouldOpen = true;
    }
}
