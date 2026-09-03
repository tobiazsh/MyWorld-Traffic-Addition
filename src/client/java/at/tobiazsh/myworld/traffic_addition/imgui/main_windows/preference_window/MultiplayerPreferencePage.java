package at.tobiazsh.myworld.traffic_addition.imgui.main_windows.preference_window;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import dev.tobiazsh.imguib3d.client.font.ImGuiFontScope;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import static at.tobiazsh.myworld.traffic_addition.language.JenguaTranslator.tr;

public class MultiplayerPreferencePage extends PreferencePage {

    public MultiplayerPreferencePage(ImGuiFontScope fontScope) {
        super(fontScope);
    }

    @Override
    public @NonNull Identifier getId() {
        return MyWorldTrafficAddition.createId("preference/mulitplayer");
    }

    @Override
    public @NonNull String getTitle() {
        return tr("ImGui.Main.PreferencesWindow", "Multiplayer Settings");
    }

    @Override
    public void draw() {

    }

    @Override
    public void initialize() {

    }

    @Override
    public void apply() {

    }

    @Override
    public void setDefault() {

    }

}
