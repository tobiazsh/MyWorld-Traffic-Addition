package at.tobiazsh.myworld.traffic_addition.imgui.main_windows;

import at.tobiazsh.myworld.traffic_addition.imgui.child_windows.popups.ConfirmationPopup;
import at.tobiazsh.myworld.traffic_addition.imgui.main_windows.preference_window.*;

import com.google.common.collect.ImmutableList;
import dev.tobiazsh.imguib3d.client.font.ImGuiFontScope;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;

import static at.tobiazsh.myworld.traffic_addition.imgui.utils.ImGuiTools.*;
import static at.tobiazsh.myworld.traffic_addition.language.JenguaTranslator.tr;

public class PreferencesWindow {

    public static final PreferencesWindow INSTANCE = new PreferencesWindow("main_preferences_window");

    public PreferencesWindow(String id) {
        this.id = id;
    }

    private final String id;
    public boolean show = false;

    private final ImGuiFontScope fontScope = ImGuiFontScope.create();

    private final ImmutableList<PreferencePage> pages = ImmutableList.of(
            new LanguagePreferencePage(fontScope),
            new CachingPreferencePage(fontScope),
            new SignPreferencePage(fontScope),
            new CustomizableSignPreferencePage(fontScope),
            new MultiplayerPreferencePage(fontScope)
    );

    private PreferencePage currentPreferencePage = null; // null = menu!

    private void initialize() {
        toMenu();
        pages.forEach(PreferencePage::initialize);
    }

    public void open() {
        initialize();
        show = true;
    }

    public void render() {
        ImGui.begin(tr("Global", "Preferences") + "##" + id, ImGuiWindowFlags.MenuBar); // Preferences Window

        menuBar();

        ConfirmationPopup.render();

        drawPages();

        ImGui.end();
    }

    private void drawPages() {
        if (currentPreferencePage == null) {
            menu();
            return;
        }

        ImGui.text(currentPreferencePage.getTitle());
        drawLineMaxX();
        ImGui.separator();

        currentPreferencePage.draw();
    }

    private void menuBar() {
        ImGui.beginMenuBar();

        if (ImGui.menuItem(tr("ImGui.Main.PreferencesWindow", "Back to Menu") + "##" + id)) toMenu();

        if (ImGui.menuItem(tr("Global", "Exit") + "##" + id)) dispose(); // Exit
        if (ImGui.menuItem(tr("Global", "Apply") + "##" + id)) apply(); // Apply
        if (ImGui.menuItem(tr("Global", "Default Values") + "##" + id)) defaultValues(); // Default Values

        ImGui.endMenuBar();
    }

    private void menu() {
        for (PreferencePage page : pages) {
            ImGui.setNextItemWidth(ImGui.getContentRegionAvailX());

            if (ImGui.button(page.getTitle() + " >" + "##" + id))
                currentPreferencePage = page;
        }
    }

    private void toMenu() {
        currentPreferencePage = null;
    }

    private void apply() {
        pages.forEach(PreferencePage::apply);
    }

    private void defaultValues() {
        pages.forEach(PreferencePage::setDefault);
    }

    private void dispose() {
        ConfirmationPopup.show(
                tr("ImGui.Global.Warn", "Do you really want to exit?"),
                tr("ImGui.Global.Warn", "All unsaved changes will be gone!"), (confirmed) -> {
            if (confirmed) this.show = false;
        });
    }
}
