package at.tobiazsh.myworld.traffic_addition.imgui.main_windows.preference_window;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAdditionClient;
import at.tobiazsh.myworld.traffic_addition.cache.LRUCache;
import at.tobiazsh.myworld.traffic_addition.imgui.ImGuiImpl;
import at.tobiazsh.myworld.traffic_addition.imgui.child_windows.popups.ConfirmationPopup;
import at.tobiazsh.myworld.traffic_addition.toml.TomlInteger;
import imgui.ImGui;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import static at.tobiazsh.myworld.traffic_addition.language.JenguaTranslator.tr;

public class CachingPreferencePage extends PreferencePage {

    private int[] imageRenderLayerCacheSize = {0};
    private int[] textRenderLayerCacheSize = {0};

    @Override
    public @NonNull Identifier getId() {
        return MyWorldTrafficAddition.createId("preference/caching");
    }

    @Override
    public @NonNull String getTitle() {
        return tr("ImGui.Main.Preferences.PageTitle", "Caching Settings");
    }

    @Override
    public void draw() {
        settingDrawInfo(
                tr("ImGui.Main.Preferences.SettingTitle", "Clear all Caches"), // Clear all Caches
                tr("ImGui.Main.Preferences.SettingDescription", "Clear all Caches") // Clear all Caches Description
        );

        if (ImGui.button(tr("Global", "Clear"))) LRUCache.clearAllCaches(); // Clear

        ImGui.separator();

        drawClearCacheModal();

        settingDrawInfo(
                tr("ImGui.Main.Preferences.SettingTitle", "Clear Cache"), // Clear Cache
                tr("ImGui.Main.Preferences.SettingDescription", "Clears cache for a specific cache register. May improve performance.") // Clear Cache Description
        );

        if (ImGui.button(tr("ImGui.Main.Preferences.Misc", "Clear Cache") + " ..."))
            ImGui.openPopup(tr("ImGui.Main.Preferences.Misc", "Clear Cache"));

        ImGui.separator();

        settingDrawInfo(
                tr("ImGui.Main.Preferences.SettingTitle", "Image RenderLayer Cache Size"), // Image Render Layer Cache Size
                tr("ImGui.Main.Preferences.SettingDescription", "The amount of RenderLayers stored in the LRU Cache. Each Element has its own RenderLayer. When lower, CPU usage is higher but RAM usage is lower. When higher, vice versa.")
        );

        ImGui.dragInt("##imageRenderLayerCacheSize", imageRenderLayerCacheSize, 1, 1, 512);

        drawRestartInfo();

        ImGui.separator();

        settingDrawInfo(
                tr("ImGui.Main.Preferences.SettingTitle", "Text RenderLayer Cache Size"),
                tr("ImGui.Main.Preferences.SettingDescription", "The amount of RenderLayers stored in the LRU Cache. Each Element has its own RenderLayer. When lower, CPU usage is higher but RAM usage is lower. When higher, vice versa.")
        );

        ImGui.dragInt("##textRenderLayerCacheSize", textRenderLayerCacheSize, 1, 1, 512);

        drawRestartInfo();

        ImGui.separator();

        settingDrawInfo(
                tr("ImGui.Main.Preferences.SettingTitle", "Calculation cache size"),
                tr("ImGui.Main.Preferences.SettingDescription", "Customizable signs and sign poles are stored based on their distance in the master sign's NBT encoded in Base64. To calculate their position from distance, MyWorld Traffic Addition has to decode the value and then cast it to their positions. This is an intensive operation.")
        );

    }

    private void drawClearCacheModal() {
        if (ImGui.beginPopupModal(tr("ImGui.Main.Preferences.Misc", "Clear Cache"))) {
            ImGui.pushFont(ImGuiImpl.RobotoBold);
            ImGui.text(tr("ImGui.Main.Preferences.Misc", "Clear Caches for") + " ..."); // Clear Cache for ...
            ImGui.popFont();

            LRUCache.getRegisteredCaches().forEach((s, cache) -> {
                if (ImGui.button(s.replaceAll("_", " "))) {
                    ConfirmationPopup.show(
                            tr("ImGui.Main.Preferences.Misc", "Do you really want to clear the cache?"),
                            tr("ImGui.Global.Warn", "This action cannot be undone!"),
                            (confirmed) -> {
                                if (!confirmed) return;

                                LRUCache.clearCache(s);
                                ImGui.closeCurrentPopup();
                            }
                    );
                }
            });

            ImGui.endPopup();
        }
    }

    @Override
    public void initialize() {
        var rendering = MyWorldTrafficAdditionClient.getClientPreferences().rendering;

        imageRenderLayerCacheSize[0] = rendering.imageRenderLayerCacheSize.getOrDefault().value();
        textRenderLayerCacheSize[0] = rendering.textRenderLayerCacheSize.getOrDefault().value();
    }

    @Override
    public void apply() {
        var rendering = MyWorldTrafficAdditionClient.getClientPreferences().rendering;

        rendering.imageRenderLayerCacheSize.set(new TomlInteger(imageRenderLayerCacheSize[0]));
        rendering.textRenderLayerCacheSize.set(new TomlInteger(textRenderLayerCacheSize[0]));
    }

    @Override
    public void setDefault() {
        var rendering = MyWorldTrafficAdditionClient.getClientPreferences().rendering;

        rendering.imageRenderLayerCacheSize.setDefault();
        rendering.textRenderLayerCacheSize.setDefault();

        initialize(); // Reinitialize to update the values on ImGui
    }
}
