package at.tobiazsh.myworld.traffic_addition.ImGui.MainWindows;

import at.tobiazsh.myworld.traffic_addition.ImGui.ChildWindows.Popups.ConfirmationPopup;
import at.tobiazsh.myworld.traffic_addition.ImGui.ImGuiImpl;
import at.tobiazsh.myworld.traffic_addition.Rendering.Renderers.CustomizableSignBlockEntityRenderer;
import at.tobiazsh.myworld.traffic_addition.Rendering.Renderers.SignBlockEntityRenderer;
import at.tobiazsh.myworld.traffic_addition.Utils.LRUCache;
import at.tobiazsh.myworld.traffic_addition.Utils.PreferenceLogic.PreferenceControl;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;

import static at.tobiazsh.myworld.traffic_addition.ImGui.Utils.ImGuiTools.*;

public class PreferencesWindow {

    public static boolean show = false;
    private static CURRENT_PAGE currentPage = CURRENT_PAGE.MENU;

    // CUSTOMIZABLE SIGN
    private static float[] viewDistanceCustomizableSigns = {0};
    private static float[] elementDistancingCustomizableSigns = {0};

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

        // SIGN
        viewDistanceSigns[0] = SignBlockEntityRenderer.zOffsetRenderLayer * 128;
    }

    public static void render() {
        ImGui.begin("Preferences", ImGuiWindowFlags.MenuBar);

        menuBar();

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

        if (ImGui.menuItem("Back to MENU")) currentPage = CURRENT_PAGE.MENU;

        if (ImGui.menuItem("Exit")) dispose();

        if (ImGui.menuItem("Apply")) apply();
        if (ImGui.menuItem("Default Values")) defaultValues();

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
        ImGui.text("GENERAL SETTINGS");
        drawLineMaxX();
        ImGui.separator();

        drawTitleAndDescription(
                "Clear all caches",
                "Clears all caches."
        );

        if (ImGui.button("Clear")) LRUCache.clearAllCaches();
    }

    private static void signsPage() {
        ImGui.text("SIGN SETTINGS");
        drawLineMaxX();
        ImGui.separator();

        drawTitleAndDescription(
                "View Distance (Blocks)",
                "The distance in which the signs are visible.\nReduces flickering between the background and the block.\nMay impact performance."
        );

        ImGui.dragFloat("##viewDistanceS", viewDistanceSigns, 0.75f, 0f, 2048f);
    }

    private static void customizableSignsPage() {
        ImGui.text("CUSTOMIZABLE SIGN SETTINGS");
        drawLineMaxX();
        ImGui.separator();

        drawTitleAndDescription(
                "View Distance (Blocks)",
                "The distance in which the signs are visible.\nReduces flickering between the background and the block.\nMay impact performance."
        );

        ImGui.dragFloat("##viewDistanceCS", viewDistanceCustomizableSigns, 0.5f, 0f, 2048f);

        ImGui.separator();

        drawTitleAndDescription(
                "Element Distancing",
                "The distance between the elements of the sign.\nHigher values may cause elements to overlap."
        );

        ImGui.dragFloat("##elementDistancingCS", elementDistancingCustomizableSigns, 0.1f, 0f, 512f);
    }

    private static void drawCachePage() {
        ImGui.text("CACHING SETTINGS");
        drawLineMaxX();
        ImGui.separator();

        drawTitleAndDescription(
                "Clear all caches",
                "Clears all caches."
        );

        if (ImGui.button("Clear")) LRUCache.clearAllCaches();

        ImGui.separator();

        drawClearCachePage();

        drawTitleAndDescription(
                "Clear Cache",
                "Clears cache for a specific cache register. May improve performance."
        );

        if (ImGui.button("Clear Cache...")) ImGui.openPopup("Clear Cache");
    }

    private static void drawClearCachePage() {
        if (ImGui.beginPopupModal("Clear Cache")) {
            ImGui.pushFont(ImGuiImpl.DejaVuSansBold);
            ImGui.text("Clear cache for ...");
            ImGui.popFont();

            LRUCache.getRegisteredCaches().forEach((s, cache) -> {
                if (ImGui.button(s.replaceAll("_", " "))) {
                    ConfirmationPopup.show("Do you really want to clear the cache?", "This action cannot be undone", (confirmed) -> {
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
        ImGui.pushFont(ImGuiImpl.DejaVuSansBold);
        ImGui.text(title);
        ImGui.popFont();
        ImGui.text(description);
    }

    private static void dispose() {
        ConfirmationPopup.show("Do you really want to exit?", "All unsaved changes will be gone!", (confirmed) -> {
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

        PreferenceControl.gameplayPreference.saveToDisk("viewDistanceCustomizableSigns", viewDistanceCustomizableSigns[0] / 128);
        PreferenceControl.gameplayPreference.saveToDisk("elementDistancingCustomizableSigns", elementDistancingCustomizableSigns[0]);

        // SIGN
        SignBlockEntityRenderer.zOffsetRenderLayer = viewDistanceSigns[0] / 128;

        PreferenceControl.gameplayPreference.saveToDisk("viewDistanceSigns", viewDistanceSigns[0] / 128);
    }

    private static void defaultValues() {
        // CUSTOMIZABLE SIGN
        viewDistanceCustomizableSigns = new float[]{CustomizableSignBlockEntityRenderer.zOffsetRenderLayerDefault * 128};
        elementDistancingCustomizableSigns = new float[]{CustomizableSignBlockEntityRenderer.elementDistancingRenderLayerDefault};

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
