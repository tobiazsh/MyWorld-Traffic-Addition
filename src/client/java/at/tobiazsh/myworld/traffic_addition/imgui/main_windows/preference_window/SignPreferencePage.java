package at.tobiazsh.myworld.traffic_addition.imgui.main_windows.preference_window;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.preference.ClientPreferences;
import at.tobiazsh.myworld.traffic_addition.rendering.renderers.SignBlockEntityRenderer;
import imgui.ImGui;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import static at.tobiazsh.myworld.traffic_addition.language.JenguaTranslator.tr;

public class SignPreferencePage extends PreferencePage {

    private float[] viewDistanceSigns = {0};
    private static final String VIEW_DISTANCE_SIGNS_KEY = "viewDistanceSigns";

    @Override
    public @NonNull Identifier getId() {
        return MyWorldTrafficAddition.createId("preference/sign");
    }

    @Override
    public @NonNull String getTitle() {
        return tr("ImGui.Main.Preferences.PageTitle", "Sign Settings");
    }

    @Override
    public void draw() {
        settingDrawInfo(
                tr("ImGui.Main.Preferences.SettingTitle", "View Distance (Blocks)"), // View Distance
                tr("ImGui.Main.Preferences.SettingDescription", "The distance in which the signs are visible.\nReduces flickering between the background and the block.\nMay impact performance.") // View Distance Description
        );

        ImGui.dragFloat("##viewDistanceS", viewDistanceSigns, 0.75f, 0, 2048);
    }

    @Override
    public void initialize() {
        // By that time, ClientPreferences has already loaded in the values from disk, so we can directly use them to initialize the variables.
        viewDistanceSigns[0] = SignBlockEntityRenderer.zOffsetRenderLayer * 128;
    }

    @Override
    public void apply() {
        SignBlockEntityRenderer.zOffsetRenderLayer = viewDistanceSigns[0] / 128;
        ClientPreferences.gameplayPreference.saveToDisk(VIEW_DISTANCE_SIGNS_KEY, viewDistanceSigns[0] / 128);
    }

    @Override
    public void setDefault() {
        viewDistanceSigns = new float[]{SignBlockEntityRenderer.zOffsetRenderLayerDefault * 128};
    }

}
