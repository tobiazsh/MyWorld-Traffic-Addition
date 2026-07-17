package at.tobiazsh.myworld.traffic_addition.imgui.main_windows.preference_window;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAdditionClient;
import imgui.ImGui;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import static at.tobiazsh.myworld.traffic_addition.language.JenguaTranslator.tr;

public class CustomizableSignPreferencePage extends PreferencePage {

    private float[] viewDistanceCustomizableSigns = {0};
    private float[] elementDistancingCustomizableSigns = {0};

    @Override
    public @NonNull Identifier getId() {
        return MyWorldTrafficAddition.createId("preference/customizable_sign");
    }

    @Override
    public @NonNull String getTitle() {
        return tr("ImGui.Main.Preferences.PageTitle", "Customizable Sign Settings");
    }

    @Override
    public void draw() {
        settingDrawInfo(
                tr("ImGui.Main.Preferences.SettingTitle", "View Distance (Blocks)"), // View Distance
                tr("ImGui.Main.Preferences.SettingDescription", "The distance in which the signs are visible.\nReduces flickering between the background and the block.\nMay impact performance.") // View Distance Description
        );

        ImGui.dragFloat("##viewDistanceCS", viewDistanceCustomizableSigns, 0.5f, 0f, 2048f);

        ImGui.separator();

        settingDrawInfo(
                tr("ImGui.Main.Preferences.SettingTitle", "Element Distancing"), // Element Distancing
                tr("ImGui.Main.Preferences.SettingDescription", "The distance between the elements of the sign.\nHigher values may cause elements to overlap.") // Element Distancing Description
        );

        ImGui.dragFloat("##elementDistancingCS", elementDistancingCustomizableSigns, 0.1f, 0f, 512f);
    }

    @Override
    public void initialize() {
        var customizableSignPref = MyWorldTrafficAdditionClient.getClientPreferences().customizableSigns;
        // By that time, ClientPreferences has already loaded in the values from disk, so we can directly use them to initialize the variables.
        viewDistanceCustomizableSigns[0] = customizableSignPref.viewDistance.getOrDefault() * 128;
        elementDistancingCustomizableSigns[0] = customizableSignPref.elementDistancing.getOrDefault();
    }

    @Override
    public void apply() {
        var customizableSignPref = MyWorldTrafficAdditionClient.getClientPreferences().customizableSigns;

        customizableSignPref.viewDistance.set(viewDistanceCustomizableSigns[0] / 128);
        customizableSignPref.elementDistancing.set(elementDistancingCustomizableSigns[0]);

        initialize();
    }

    @Override
    public void setDefault() {
        var customizableSignPref = MyWorldTrafficAdditionClient.getClientPreferences().customizableSigns;

        customizableSignPref.viewDistance.setDefault();
        customizableSignPref.elementDistancing.setDefault();
    }
}
