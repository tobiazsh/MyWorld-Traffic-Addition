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
    private Background background;

    private String id;

    private final String windowTitle;

    private final CustomizableSignTextureData textureData;

    private float[] color;

    public BackgroundSelectorPopup(CustomizableSignTextureData textureData, String id) {
        this.oldBackground = textureData.getBackground();
        this.textureData = textureData;
        this.backgroundType = extractTypeFromBackground(oldBackground);
        this.id = id;
        this.windowTitle = tr("ImGui.Child.PopUps.BackgroundSelector", "Choose Background") + "##" + id;
    }

    public void render() {
        ImGui.setNextWindowSize(600, 400);
        ImGui.pushFont(ImGuiImpl.Roboto);
        if (ImGui.beginPopupModal(windowTitle)) {

            ImGui.pushFont(ImGuiImpl.RobotoBold);
            ImGui.text(tr("ImGui.Child.PopUps.BackgroundSelector", "Background Type"));

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

            if (ImGui.beginChild("##controlButtons" + id)) {
                if (ImGui.button(tr("Global", "Cancel"))) {
                    textureData.setBackground(oldBackground);
                    ImGui.closeCurrentPopup();
                }

                if (ImGui.button(tr("Global", "Okay"))) {
                    ImGui.closeCurrentPopup();
                }

                ImGui.endChild();
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
        if (ImGui.colorPicker4(tr("Global", "Color Picker") + "##" + id, this.color, ImGuiColorEditFlags.AlphaBar | ImGuiColorEditFlags.AlphaPreviewHalf)) { // Translatable text for "Color Picker"
            int r = Math.round(color[0] * 255);
            int g = Math.round(color[1] * 255);
            int b = Math.round(color[2] * 255);
            int a = Math.round(color[3] * 255);
            textureData.setBackground(new Background(new Color(a, r, g, b)));
        }
    }

    private void renderPatternOptions() {
        if (background.texture == null) return; // Color
        if (ImGui.beginCombo("##patternSelect_" + id, tr("ImGui.Main.Background", backgroundType.translationName))) {
            BackgroundLoader.BACKGROUND_SPRITES.forEach(bgSprite -> {
                if (ImGui.selectable(
                        tr("ImGui.Main.Background", backgroundType.translationName),
                        Objects.equals(bgSprite, SpriteAtlasManager.INSTANCE.getSpriteAtlas(Identifier.parse(background.texture)))
                )) {
                    textureData.setBackground(new Background(bgSprite.getAtlasId().toString()));
                }
            });
        }
    }

    private static BackgroundType extractTypeFromBackground(Background bg) {
        if (bg == Background.TRANSPARENT)
            return BackgroundType.NONE;

        if (bg.isColor())
            return BackgroundType.COLOR;

        return BackgroundType.PATTERN;
    }

    public void open() {
        shouldOpen = true;
    }
}
