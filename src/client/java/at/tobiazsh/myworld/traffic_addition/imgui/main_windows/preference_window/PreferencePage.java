package at.tobiazsh.myworld.traffic_addition.imgui.main_windows.preference_window;

import at.tobiazsh.myworld.traffic_addition.imgui.fonts.DefaultFonts;
import dev.tobiazsh.imguib3d.client.font.ImGuiFontScope;
import imgui.ImGui;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import static at.tobiazsh.myworld.traffic_addition.language.JenguaTranslator.tr;

public abstract class PreferencePage {

    protected final ImGuiFontScope fontScope;

    protected PreferencePage(ImGuiFontScope fontScope) {
        this.fontScope = fontScope;
    }

    /**
     * Returns the unique identifier for this preference page.
     */
    abstract public @NotNull Identifier getId();

    /**
     * Returns the title of this preference page, which will be displayed in the UI.
     */
    abstract public @NotNull String getTitle();

    /**
     * Draws the preference page.
     */
    abstract public void draw();

    /**
     * Gathers all necessary data before preferences can be modified.
     */
    abstract public void initialize();

    /**
     * Apply changed preferences
     */
    abstract public void apply();

    /**
     * Reset to default values
     */
    abstract public void setDefault();

    /**
     * Draws the title and description for a setting section in the preference page.
     * Usage is not obligated, but nice to keep design uniform.
     * @param title The title of the setting
     * @param description A more detailed description of the setting. (Auto-wrapped)
     */
    protected void settingDrawInfo(String title, String description) {
        fontScope.push(DefaultFonts.RobotoBold);
        ImGui.text(title);
        fontScope.pop();

        ImGui.textWrapped(description);
    }

    /**
     * Draw the warning label that Minecraft needs to restart in order for the setting to take effect.
     */
    protected void drawRestartInfo() {
        fontScope.push(DefaultFonts.RobotoBold);
        ImGui.text(tr("ImGui.Main.Preferences.Misc", "If you change this value, you need to restart the game for it to take effect."));
        fontScope.pop();
    }

}
