package at.tobiazsh.myworld.traffic_addition.language;

import net.minecraft.network.chat.Component;

public final class MinecraftTranslationHelper {

    /**
     * Translate using {@link Component#translatable(String)}.
     */
    public static String tr(String key) {
        return Component.translatable(key).getString();
    }

    /**
     * Translate using {@link Component#translatable(String)} add an ImGui ID to the translation key to avoid duplicate
     * ImGui IDs.
     */
    public static String trI(String key, String id) {
        return Component.translatable(key).getString() + "##" + id;
    }

    /**
     * Translate using {@link Component#translatable(String)} add an ImGui ID and a suffix to the translation key to
     * avoid duplicate ImGui IDs.
     *
     * <p>Keeps ID static, but changes title</p>
     */
    public static String trIC(String key, String id, String suffix) {
        return Component.translatable(key).getString() + "###" + id + suffix;
    }

    /**
     * Translate with arguments using {@link Component#translatable(String, Object...)}.
     */
    public static String trA(String key, Object... args) {
        return Component.translatable(key, args).getString();
    }
}
