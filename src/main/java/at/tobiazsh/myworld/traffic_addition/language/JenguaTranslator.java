package at.tobiazsh.myworld.traffic_addition.language;

import io.github.tobiazsh.jengua.Language;
import io.github.tobiazsh.jengua.LanguageLoader;
import io.github.tobiazsh.jengua.Translator;

import java.io.IOException;

import static at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition.MOD_ID;

public class JenguaTranslator {

    public static Translator translator;
    public static Language default_en_US;
    public static final String en_US_path = "/assets/%s/jenglang/en_US.json".formatted(MOD_ID);

    public static void setup() {
        try {
            default_en_US = LanguageLoader.loadLanguageFromResources(en_US_path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        translator = new Translator(default_en_US, default_en_US); // Use en_US as both default and fallback language
    }

    public static String tr(String namespace, String key) {
        return translator.tr(namespace, key);
    }
}
