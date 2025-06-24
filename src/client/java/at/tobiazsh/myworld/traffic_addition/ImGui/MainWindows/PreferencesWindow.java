package at.tobiazsh.myworld.traffic_addition.ImGui.MainWindows;

import at.tobiazsh.myworld.traffic_addition.ImGui.ChildWindows.Popups.ConfirmationPopup;
import at.tobiazsh.myworld.traffic_addition.ImGui.ImGuiImpl;
import at.tobiazsh.myworld.traffic_addition.Rendering.Renderers.CustomizableSignBlockEntityRenderer;
import at.tobiazsh.myworld.traffic_addition.Rendering.Renderers.SignBlockEntityRenderer;
import at.tobiazsh.myworld.traffic_addition.Utils.LRUCache;
import at.tobiazsh.myworld.traffic_addition.Utils.ClientPreferences;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import net.minecraft.text.Text;

import java.util.Objects;

import static at.tobiazsh.myworld.traffic_addition.ImGui.Utils.ImGuiTools.*;
import static at.tobiazsh.myworld.traffic_addition.Rendering.CustomRenderLayer.defaultImageCacheSize;
import static at.tobiazsh.myworld.traffic_addition.Rendering.CustomRenderLayer.defaultTextCacheSize;
import static at.tobiazsh.myworld.traffic_addition.Utils.ClientPreferences.gameplayPreference;

public class PreferencesWindow {

    public static boolean show = false;
    private static CURRENT_PAGE currentPage = CURRENT_PAGE.MENU;

    // CUSTOMIZABLE SIGN
    private static float[] viewDistanceCustomizableSigns = {0};
    private static float[] elementDistancingCustomizableSigns = {0};
    private static int[] imageRenderLayerCacheSize = {0};
    private static int[] textRenderLayerCacheSize = {0};

    // SIGN
    private static float[] viewDistanceSigns = {0};

    public static void open() {
        resetValues();
        show = true;
    }

    private static void resetValues() {
        currentPage = CURRENT_PAGE.MENU;

        // CUSTOMIZABLE SIGN
        viewDistanceCustomizableSigns[0] = CustomizableSignBlockEntityRenderer.zOffsetRenderLayer * 128;
        elementDistancingCustomizableSigns[0] = CustomizableSignBlockEntityRenderer.elementDistancingRenderLayer;

        imageRenderLayerCacheSize[0] = Objects.requireNonNullElse(
            gameplayPreference.getInt("textRenderLayerCacheSize"),
            defaultImageCacheSize
        );

        textRenderLayerCacheSize[0] = Objects.requireNonNullElse(
                gameplayPreference.getInt("imageRenderLayerCacheSize"),
                defaultTextCacheSize
        );

        // SIGN
        viewDistanceSigns[0] = SignBlockEntityRenderer.zOffsetRenderLayer * 128;
    }

    public static void render() {
        ImGui.begin(Text.translatable("mwta.imgui.sign.editor.preferences").getString(), ImGuiWindowFlags.MenuBar); // Preferences Window

        menuBar();

        ConfirmationPopup.render();

        switch (currentPage) {
            case MENU -> menu();
            case CUSTOMIZABLE_SIGNS -> customizableSignsPage();
            case SIGNS -> signsPage();
            case GENERAL -> generalPage();
            case CACHING -> drawCachePage();
        }

        ImGui.end();
    }

    private static void menuBar() {
        ImGui.beginMenuBar();

        if (ImGui.menuItem(Text.translatable("mwta.imgui.sign.editor.back_to_menu").getString())) currentPage = CURRENT_PAGE.MENU; // Back to menu

        if (ImGui.menuItem(Text.translatable("mwta.imgui.sign.editor.exit").getString())) dispose(); // Exit
        if (ImGui.menuItem(Text.translatable("mwta.imgui.sign.editor.apply").getString())) apply(); // Apply
        if (ImGui.menuItem(Text.translatable("mwta.imgui.sign.editor.default_values").getString())) defaultValues(); // Default Values

        ImGui.endMenuBar();
    }

    private static void menu() {
        for (CURRENT_PAGE page : CURRENT_PAGE.values()) {
            ImGui.setNextItemWidth(ImGui.getContentRegionAvailX());
            if (ImGui.button(convertEnumToTitle(page) + " >")) {
                currentPage = page;
            }
        }
    }

    private static void generalPage() {
        ImGui.text(Text.translatable("mwta.imgui.sign.editor.general_settings").getString().toUpperCase()); // GENERAL SETTINGS
        drawLineMaxX();
        ImGui.separator();

        drawTitleAndDescription(
                Text.translatable("mwta.imgui.prefs.clear_all_caches").getString(), // Clear all Caches
                Text.translatable("mwta.imgui.prefs.clear_all_caches.description").getString() // Clear all Caches Description
        );

        if (ImGui.button(Text.translatable("mwta.imgui.sign.editor.clear").getString())) LRUCache.clearAllCaches();
    }

    private static void signsPage() {
        ImGui.text(Text.translatable("mwta.imgui.sign.editor.sign_settings").getString().toUpperCase()); // SIGN SETTINGS
        drawLineMaxX();
        ImGui.separator();

        drawTitleAndDescription(
                Text.translatable("mwta.imgui.prefs.view_distance").getString(), // View Distance
                Text.translatable("mwta.imgui.prefs.view_distance.description").getString() // View Distance Description
        );

        ImGui.dragFloat("##viewDistanceS", viewDistanceSigns, 0.75f, 0f, 2048f);
    }

    private static void customizableSignsPage() {
        ImGui.text(Text.translatable("mwta.imgui.sign.editor.customizable_sign_settings").getString().toUpperCase()); // CUSTOMIZABLE SIGN SETTINGS
        drawLineMaxX();
        ImGui.separator();

        drawTitleAndDescription(
                Text.translatable("mwta.imgui.prefs.view_distance").getString(), // View Distance
                Text.translatable("mwta.imgui.prefs.view_distance.description").getString() // View Distance Description
        );

        ImGui.dragFloat("##viewDistanceCS", viewDistanceCustomizableSigns, 0.5f, 0f, 2048f);

        ImGui.separator();

        drawTitleAndDescription(
                Text.translatable("mwta.imgui.prefs.element_distancing").getString(), // Element Distancing
                Text.translatable("mwta.imgui.prefs.element_distancing.description").getString() // Element Distancing Description
        );

        ImGui.dragFloat("##elementDistancingCS", elementDistancingCustomizableSigns, 0.1f, 0f, 512f);
    }

    private static void drawCachePage() {
        ImGui.text(Text.translatable("mwta.imgui.sign.editor.caching_settings").getString().toUpperCase()); // CACHING SETTINGS
        drawLineMaxX();
        ImGui.separator();

        drawTitleAndDescription(
                Text.translatable("mwta.imgui.prefs.clear_all_caches").getString(), // Clear all Caches
                Text.translatable("mwta.imgui.prefs.clear_all_caches.description").getString() // Clear all Caches Description
        );

        if (ImGui.button(Text.translatable("mwta.imgui.sign.editor.clear").getString())) LRUCache.clearAllCaches(); // Clear

        ImGui.separator();

        drawClearCachePage();

        drawTitleAndDescription(
                Text.translatable("mwta.imgui.prefs.clear_cache").getString(),
                Text.translatable("mwta.imgui.prefs.clear_cache.description").getString()
        );

        if (ImGui.button(Text.translatable("mwta.imgui.prefs.clear_cache").getString() + " ...")) ImGui.openPopup(Text.translatable("mwta.imgui.prefs.clear_cache").getString());

        ImGui.separator();

        drawTitleAndDescription(
                Text.translatable("mwta.imgui.prefs.image.renderlayer_cache_size").getString(),
                Text.translatable("mwta.imgui.prefs.image.renderlayer_cache_size.description").getString()
        );

        ImGui.dragInt("##imageRenderLayerCacheSize", imageRenderLayerCacheSize, 1, 1, 512);

        ImGui.pushFont(ImGuiImpl.RobotoBold);
        ImGui.text(Text.translatable("mwta.imgui.prefs.take_effect_restart_game").getString()); // "If you change this value, you need to restart the game for it to take effect."
        ImGui.popFont();

        ImGui.separator();

        drawTitleAndDescription(
                Text.translatable("mwta.imgui.prefs.text.renderlayer_cache_size").getString(),
                Text.translatable("mwta.imgui.prefs.text.renderlayer_cache_size.description").getString()
        );

        ImGui.dragInt("##textRenderLayerCacheSize", textRenderLayerCacheSize, 1, 1, 512);

        ImGui.pushFont(ImGuiImpl.RobotoBold);
        ImGui.text(Text.translatable("mwta.imgui.prefs.take_effect_restart_game").getString()); // "If you change this value, you need to restart the game for it to take effect."
        ImGui.popFont();
    }

    private static void drawClearCachePage() {
        if (ImGui.beginPopupModal(Text.translatable("mwta.imgui.prefs.clear_cache").getString())) {
            ImGui.pushFont(ImGuiImpl.RobotoBold);
            ImGui.text(Text.translatable("mwta.imgui.prefs.clear_cache_for").getString() + " ..."); // Clear Cache for ...
            ImGui.popFont();

            LRUCache.getRegisteredCaches().forEach((s, cache) -> {
                if (ImGui.button(s.replaceAll("_", " "))) {
                    ConfirmationPopup.show(Text.translatable("mwta.imgui.prefs.clear_cache_confirmation").getString(), Text.translatable("mwta.imgui.warnings.action_cannot_be_undone").getString(), (confirmed) -> {
                        if (!confirmed) return;

                        LRUCache.clearCache(s);
                        ImGui.closeCurrentPopup();
                    });
                }
            });

            ImGui.endPopup();
        }

    }

    private static void drawTitleAndDescription(String title, String description) {
        ImGui.pushFont(ImGuiImpl.RobotoBold);
        ImGui.text(title);
        ImGui.popFont();
        ImGui.textWrapped(description);
    }

    private static void dispose() {
        ConfirmationPopup.show(
                Text.translatable("mwta.imgui.warnings.general_exit", Text.translatable("mwta.imgui.window_titles.preferences")).getString(),
                Text.translatable("mwta.imgui.warnings.all_unsaved_changes_gone").getString(), (confirmed) -> {
            if (confirmed) PreferencesWindow.show = false;
        });
    }

    private static String convertEnumToTitle(CURRENT_PAGE page) {
        return page.name().replace("_", " ");
    }

    private static void apply() {
        // CUSTOMIZABLE SIGN
        CustomizableSignBlockEntityRenderer.zOffsetRenderLayer = viewDistanceCustomizableSigns[0] / 128;
        CustomizableSignBlockEntityRenderer.elementDistancingRenderLayer = elementDistancingCustomizableSigns[0];

        gameplayPreference.saveToDisk("viewDistanceCustomizableSigns", viewDistanceCustomizableSigns[0] / 128);
        gameplayPreference.saveToDisk("elementDistancingCustomizableSigns", elementDistancingCustomizableSigns[0]);

        gameplayPreference.saveToDisk("imageRenderLayerCacheSize", imageRenderLayerCacheSize[0]);
        gameplayPreference.saveToDisk("textRenderLayerCacheSize", textRenderLayerCacheSize[0]);

        // SIGN
        SignBlockEntityRenderer.zOffsetRenderLayer = viewDistanceSigns[0] / 128;

        ClientPreferences.gameplayPreference.saveToDisk("viewDistanceSigns", viewDistanceSigns[0] / 128);
    }

    private static void defaultValues() {
        // CUSTOMIZABLE SIGN
        viewDistanceCustomizableSigns = new float[]{CustomizableSignBlockEntityRenderer.zOffsetRenderLayerDefault * 128};
        elementDistancingCustomizableSigns = new float[]{CustomizableSignBlockEntityRenderer.elementDistancingRenderLayerDefault};

        imageRenderLayerCacheSize = new int[]{defaultImageCacheSize};
        textRenderLayerCacheSize = new int[]{defaultTextCacheSize};

        // SIGN
        viewDistanceSigns = new float[]{SignBlockEntityRenderer.zOffsetRenderLayerDefault * 128};
    }

    private enum CURRENT_PAGE {
        MENU,
        CUSTOMIZABLE_SIGNS,
        SIGNS,
        GENERAL,
        CACHING
    }
}
