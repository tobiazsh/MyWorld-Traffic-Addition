package at.tobiazsh.myworld.traffic_addition.imgui.main_windows.preference_window;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAdditionClient;
import at.tobiazsh.myworld.traffic_addition.language.JenguaTranslator;
import dev.tobiazsh.imguib3d.client.font.ImGuiFontScope;
import imgui.ImGui;
import imgui.type.ImInt;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.List;

import static at.tobiazsh.myworld.traffic_addition.language.JenguaTranslator.tr;

public class LanguagePreferencePage extends PreferencePage {

    private String[] cleartextLanguages;
    private String[] availableLanguages;

    private String currentLanguage = "auto"; // Set auto by default
    private final ImInt currentLanguageIdx = new ImInt(0); // Set idx of "auto" by default

    public LanguagePreferencePage(ImGuiFontScope fontScope) {
        super(fontScope);
    }

    @Override
    public @NonNull Identifier getId() {
        return MyWorldTrafficAddition.createId("preference/language");
    }

    @Override
    public @NonNull String getTitle() {
        return tr("ImGui.Main.Preferences.PageTitle", "Language Settings");
    }

    @Override
    public void draw() {
        settingDrawInfo(
                tr("ImGui.Main.Preferences.SettingTitle", "Language"),
                tr("ImGui.Main.Preferences.SettingDescription", "MyWorld Traffic Addition's Language. Does not apply to base game!")
        );

        if (ImGui.combo("##language", currentLanguageIdx, cleartextLanguages))
            currentLanguage = availableLanguages[currentLanguageIdx.get()];
    }

    @Override
    public void initialize() {
        // NOTE: I've documented this code a bit too much, but I was confused when I read the original code again,
        // so I thought a little over-documentation would be better than under-documentation in this case.

        var general = MyWorldTrafficAdditionClient.getClientPreferences().general;

        // Initialize array
        availableLanguages = new String[JenguaTranslator.getAvailableLanguages().length + 1]; // Leave idx 0 for "auto"

        // Put first entry — "auto"
        availableLanguages[0] = "auto";

        // Copy into array
        System.arraycopy(
                JenguaTranslator.getAvailableLanguages(),           // The array to copy
                0,                                                  // Start index in src array
                availableLanguages,                                 // Destination array
                1,                                                  // Start index in dst array
                JenguaTranslator.getAvailableLanguages().length     // Number of elements to copy
        );

        // Map the language id's to their appropriate translation so the user is able to read them
        cleartextLanguages = Arrays.stream(availableLanguages)
                .map(lang -> tr("Global.Lang", lang))
                .toArray(String[]::new);

        String savedLanguage = general.language
                .getOrDefault(); // Get language from preferences

        // If language is valid, then use it (else just fallback to "auto")
        if (savedLanguage != null && !savedLanguage.isEmpty()) {
            currentLanguageIdx.set(List.of(availableLanguages).indexOf(savedLanguage));
            currentLanguage = savedLanguage;
        }
    }

    @Override
    public void apply() {
        var general = MyWorldTrafficAdditionClient.getClientPreferences().general;

        // Only save if not saved yet
        if (!currentLanguage.equals(general.language.getOrDefault()))
            general.language.set(currentLanguage);

        if (currentLanguage.equals("auto"))
            JenguaTranslator.autoSetLanguage();
        else
            JenguaTranslator.translator.setLanguage(currentLanguage);

        initialize();
    }

    @Override
    public void setDefault() {
        var general = MyWorldTrafficAdditionClient.getClientPreferences().general;

        general.language.setDefault();
    }
}
