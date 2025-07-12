package at.tobiazsh.myworld.traffic_addition.language;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.utils.FileSystem;
import com.mojang.authlib.minecraft.client.MinecraftClient;
import io.github.tobiazsh.jengua.Language;
import io.github.tobiazsh.jengua.LanguageLoader;
import io.github.tobiazsh.jengua.Translator;

import java.io.IOException;
import java.net.URISyntaxException;

import static at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition.MOD_ID;

public class JenguaTranslator {

    public static Translator translator;
    public static Language default_en_US;
    public static final String en_US_path = "/assets/%s/jenglang/en_US.json".formatted(MOD_ID);
    private static String[] availableLanguages;
    private static FileSystem.Folder languagesFolder;

    public static void setup() {
        try {
            default_en_US = LanguageLoader.loadLanguageFromResources(en_US_path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        translator = new Translator(default_en_US, default_en_US); // Use en_US as both default and fallback language

        languagesFolder = getLanguagesFolder();
        registerAvailableLanguages();
        availableLanguages = translator.getAvailableLanguages().toArray(String[]::new);
    }

    public static String tr(String namespace, String key) {
        return translator.tr(namespace, key);
    }

    private static FileSystem.Folder getLanguagesFolder() {
        try {
            return FileSystem.listFiles("/assets/%s/jenglang/".formatted(MOD_ID), true);
        } catch (IOException | URISyntaxException e) {
            MyWorldTrafficAddition.LOGGER.error("Failed to load languages!");
            throw new RuntimeException(e);
        }
    }

    private static void registerAvailableLanguages() {
        languagesFolder.content.stream()
                .filter(FileSystem.DirectoryElement::isFile)
                .map(file -> file.path)
                .forEach(languagePath -> {
                    try {
                        translator.loadLanguageFromResources(languagePath);
                    } catch (IOException e) {
                        MyWorldTrafficAddition.LOGGER.error("Failed to load language from path: {}", languagePath);
                        throw new RuntimeException(e);
                    }
                }
        );
    }

    public static String[] getAvailableLanguages() {
        return availableLanguages;
    }

    /**
     * Converts a Minecraft locale string to a Jengua locale string. (e.g. "en_us" to "en_US")
     */
    private static String convertMinecraftToJenguaLocale(String minecraftLocale) {
        return "%s_%s".formatted(minecraftLocale.split("_")[0], minecraftLocale.split("_")[1].toUpperCase());
    }
}
