package at.tobiazsh.myworld.traffic_addition.imgui.main_windows.preference_window;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAdditionClient;
import at.tobiazsh.myworld.traffic_addition.toml.TomlFloat;
import imgui.ImGui;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import static at.tobiazsh.myworld.traffic_addition.language.JenguaTranslator.tr;

public class SignPreferencePage extends PreferencePage {

    private float[] viewDistanceSigns = {0};

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
        var signs = MyWorldTrafficAdditionClient.getClientPreferences().signs;

        // By that time, ClientPreferences has already loaded in the values from disk, so we can directly use them to initialize the variables.
        viewDistanceSigns[0] = signs.viewDistance.getOrDefault().value();
    }

    @Override
    public void apply() {
        var signs = MyWorldTrafficAdditionClient.getClientPreferences().signs;

        signs.viewDistance.set(new TomlFloat(viewDistanceSigns[0] / 128));

        initialize();
    }

    @Override
    public void setDefault() {
        var signs = MyWorldTrafficAdditionClient.getClientPreferences().signs;

        signs.viewDistance.setDefault();
    }

}
