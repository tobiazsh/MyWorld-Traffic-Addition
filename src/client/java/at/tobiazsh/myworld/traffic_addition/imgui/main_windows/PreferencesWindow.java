package at.tobiazsh.myworld.traffic_addition.imgui.main_windows;

import at.tobiazsh.myworld.traffic_addition.imgui.child_windows.popups.ConfirmationPopup;
import at.tobiazsh.myworld.traffic_addition.imgui.ImGuiImpl;
import at.tobiazsh.myworld.traffic_addition.language.JenguaTranslator;
import at.tobiazsh.myworld.traffic_addition.rendering.renderers.CustomizableSignBlockEntityRenderer;
import at.tobiazsh.myworld.traffic_addition.rendering.renderers.SignBlockEntityRenderer;
import at.tobiazsh.myworld.traffic_addition.utils.LRUCache;
import at.tobiazsh.myworld.traffic_addition.utils.ClientPreferences;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImInt;

import java.util.Arrays;
import java.util.Objects;

import static at.tobiazsh.myworld.traffic_addition.imgui.utils.ImGuiTools.*;
import static at.tobiazsh.myworld.traffic_addition.language.JenguaTranslator.tr;
import static at.tobiazsh.myworld.traffic_addition.rendering.CustomRenderLayer.defaultImageCacheSize;
import static at.tobiazsh.myworld.traffic_addition.rendering.CustomRenderLayer.defaultTextCacheSize;
import static at.tobiazsh.myworld.traffic_addition.utils.ClientPreferences.gameplayPreference;

public class PreferencesWindow {

    public static boolean show = false;
    private static CURRENT_PAGE currentPage = CURRENT_PAGE.MENU;

    // CUSTOMIZABLE SIGN
    private static float[] viewDistanceCustomizableSigns = {0};
    private static float[] elementDistancingCustomizableSigns = {0};
    private static int[] imageRenderLayerCacheSize = {0};
    private static int[] textRenderLayerCacheSize = {0};
    private static int[] calculationCacheSize = {0}; // Cache size for sign coordinate calculations

    // SIGN
    private static float[] viewDistanceSigns = {0};

    // LANGUAGE
    private static String currentLanguage = "auto"; // Default language is set to "auto"
    private static String[] availableLanguages;
    private static ImInt currentLanguageIndex = new ImInt(0); // Index of the currently selected language setting

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

        // LANGUAGE
        resetLanguage();
    }



    private static void resetLanguage() {
        availableLanguages = new String[JenguaTranslator.getAvailableLanguages().length + 1]; // +1 for "auto"

        availableLanguages[0] = "auto";

        System.arraycopy(JenguaTranslator.getAvailableLanguages(), 0, availableLanguages, 1, JenguaTranslator.getAvailableLanguages().length);

        currentLanguage = gameplayPreference.getString("mwtaLanguage");
        if (currentLanguage == null || currentLanguage.isEmpty() || currentLanguage.equalsIgnoreCase("auto")) {
            currentLanguageIndex.set(0); // Default to "auto"
        } else {
            currentLanguageIndex.set(Arrays.stream(availableLanguages).toList().indexOf(currentLanguage));
        }
    }



    public static void render() {
        ImGui.begin(tr("Global", "Preferences"), ImGuiWindowFlags.MenuBar); // Preferences Window

        menuBar();

        ConfirmationPopup.render();

        switch (currentPage) {
            case MENU -> menu();
            case CUSTOMIZABLE_SIGNS -> customizableSignsPage();
            case SIGNS -> signsPage();
            case GENERAL -> generalPage();
            case CACHING -> drawCachePage();
            case LANGUAGE -> drawLanguagePage();
        }

        ImGui.end();
    }



    private static void menuBar() {
        ImGui.beginMenuBar();

        if (ImGui.menuItem(tr("ImGui.Main.PreferencesWindow", "Back to Menu"))) currentPage = CURRENT_PAGE.MENU; // Back to menu

        if (ImGui.menuItem(tr("Global", "Exit"))) dispose(); // Exit
        if (ImGui.menuItem(tr("Global", "Apply"))) apply(); // Apply
        if (ImGui.menuItem(tr("Global", "Default Values"))) defaultValues(); // Default Values

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
        ImGui.text(tr("ImGui.Main.PreferencesWindow", "General Settings").toUpperCase()); // GENERAL SETTINGS
        drawLineMaxX();
        ImGui.separator();

        drawTitleAndDescription(
                tr("ImGui.Main.PreferencesWindow", "Clear all Caches"), // Clear all Caches
                tr("ImGui.Main.PreferencesWindow", "Clear all Caches") // Clear all Caches Description
        );

        if (ImGui.button(tr("Global", "Clear"))) LRUCache.clearAllCaches();
    }



    private static void signsPage() {
        ImGui.text(tr("ImGui.Main.PreferencesWindow", "Sign Settings").toUpperCase()); // SIGN SETTINGS
        drawLineMaxX();
        ImGui.separator();

        drawTitleAndDescription(
                tr("ImGui.Main.PreferencesWindow", "View Distance (Blocks)"), // View Distance
                tr("ImGui.Main.PreferencesWindow", "The distance in which the signs are visible.\nReduces flickering between the background and the block.\nMay impact performance.") // View Distance Description
        );

        ImGui.dragFloat("##viewDistanceS", viewDistanceSigns, 0.75f, 0f, 2048f);
    }



    private static void customizableSignsPage() {
        ImGui.text(tr("ImGui.Main.PreferencesWindow", "Customizable Sign Settings").toUpperCase()); // CUSTOMIZABLE SIGN SETTINGS
        drawLineMaxX();
        ImGui.separator();

        drawTitleAndDescription(
                tr("ImGui.Main.PreferencesWindow", "View Distance (Blocks)"), // View Distance
                tr("ImGui.Main.PreferencesWindow", "The distance in which the signs are visible.\nReduces flickering between the background and the block.\nMay impact performance.") // View Distance Description
        );

        ImGui.dragFloat("##viewDistanceCS", viewDistanceCustomizableSigns, 0.5f, 0f, 2048f);

        ImGui.separator();

        drawTitleAndDescription(
                tr("ImGui.Main.PreferencesWindow", "Element Distancing"), // Element Distancing
                tr("ImGui.Main.PreferencesWindow", "The distance between the elements of the sign.\nHigher values may cause elements to overlap.") // Element Distancing Description
        );

        ImGui.dragFloat("##elementDistancingCS", elementDistancingCustomizableSigns, 0.1f, 0f, 512f);
    }



    private static void drawCachePage() {
        ImGui.text(tr("ImGui.Main.PreferencesWindow", "Caching Settings").toUpperCase()); // CACHING SETTINGS
        drawLineMaxX();
        ImGui.separator();

        drawTitleAndDescription(
                tr("ImGui.Main.PreferencesWindow", "Clear all Caches"), // Clear all Caches
                tr("ImGui.Main.PreferencesWindow", "Clear all Caches") // Clear all Caches Description
        );

        if (ImGui.button(tr("Global", "Clear"))) LRUCache.clearAllCaches(); // Clear

        ImGui.separator(); // ------------------------------------------------------------------------------------------------------------------------

        drawClearCachePage();

        drawTitleAndDescription(
                tr("ImGui.Main.PreferencesWindow", "Clear Cache"), // Clear Cache
                tr("ImGui.Main.PreferencesWindow", "Clears cache for a specific cache register. May improve performance.") // Clear Cache Description
        );

        if (ImGui.button(tr("ImGui.Main.PreferencesWindow", "Clear Cache") + " ...")) ImGui.openPopup(tr("ImGui.Main.PreferencesWindow", "Clear Cache"));

        ImGui.separator(); // ------------------------------------------------------------------------------------------------------------------------

        drawTitleAndDescription(
                tr("ImGui.Main.PreferencesWindow", "Image RenderLayer Cache Size"), // Image Render Layer Cache Size
                tr("ImGui.Main.PreferencesWindow", "The amount of RenderLayers stored in the LRU Cache. Each Element has its own RenderLayer. When lower, CPU usage is higher but RAM usage is lower. When higher, vice versa.")
        );

        ImGui.dragInt("##imageRenderLayerCacheSize", imageRenderLayerCacheSize, 1, 1, 512);

        ImGui.pushFont(ImGuiImpl.RobotoBold);
        ImGui.text(tr("ImGui.Main.PreferencesWindow", "If you change this value, you need to restart the game for it to take effect."));
        ImGui.popFont();

        ImGui.separator(); // ------------------------------------------------------------------------------------------------------------------------

        drawTitleAndDescription(
                tr("ImGui.Main.PreferencesWindow", "Text RenderLayer Cache Size"),
                tr("ImGui.Main.PreferencesWindow", "The amount of RenderLayers stored in the LRU Cache. Each Element has its own RenderLayer. When lower, CPU usage is higher but RAM usage is lower. When higher, vice versa.")
        );

        ImGui.dragInt("##textRenderLayerCacheSize", textRenderLayerCacheSize, 1, 1, 512);

        ImGui.pushFont(ImGuiImpl.RobotoBold);
        ImGui.text(tr("ImGui.Main.PreferencesWindow", "If you change this value, you need to restart the game for it to take effect.")); // "If you change this value, you need to restart the game for it to take effect."
        ImGui.popFont();
    }



    private static void drawClearCachePage() {
        if (ImGui.beginPopupModal(tr("ImGui.Main.PreferencesWindow", "Clear Cache"))) {
            ImGui.pushFont(ImGuiImpl.RobotoBold);
            ImGui.text(tr("ImGui.Main.PreferencesWindow", "Clear Caches for") + " ..."); // Clear Cache for ...
            ImGui.popFont();

            LRUCache.getRegisteredCaches().forEach((s, cache) -> {
                if (ImGui.button(s.replaceAll("_", " "))) {
                    ConfirmationPopup.show(
                            tr("ImGui.Main.PreferencesWindow", "Do you really want to clear the cache?"),
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



    private static void drawLanguagePage() {
        String[] readableLanguages = Arrays.stream(availableLanguages)
                .map(lang -> lang.equals("auto") ? tr("Global.Lang", "auto") : tr("Global.Lang", lang))
                .toArray(String[]::new);

        ImGui.text(tr("ImGui.Main.Pref.Title", "Language Settings"));
        drawLineMaxX();
        drawTitleAndDescription(
                tr("ImGui.Main.Pref.Lang.Lang", "Language"),
                tr("ImGui.Main.Pref.Lang.Lang", "MyWorld Traffic Addition's Language. Does not apply to base game!")
        );
        if (ImGui.combo("##language", currentLanguageIndex, readableLanguages)) {
            currentLanguage = availableLanguages[currentLanguageIndex.get()];
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
                tr("ImGui.Global.Warn", "Do you really want to exit?"),
                tr("ImGui.Global.Warn", "All unsaved changes will be gone!"), (confirmed) -> {
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

        // LANGUAGE
        applyLanguage();

        ClientPreferences.gameplayPreference.saveToDisk("viewDistanceSigns", viewDistanceSigns[0] / 128);
    }



    private static void applyLanguage() {
        gameplayPreference.saveToDisk("mwtaLanguage", currentLanguage);

        if (currentLanguage.equals("auto"))
            JenguaTranslator.autoSetLanguage();
        else
            JenguaTranslator.translator.setLanguage(currentLanguage);
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
        CACHING,
        LANGUAGE
    }
}
