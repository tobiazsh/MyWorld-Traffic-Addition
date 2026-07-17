package at.tobiazsh.myworld.traffic_addition.preference;

import java.io.File;
import java.util.Map;

import static at.tobiazsh.myworld.traffic_addition.preference.LegacyClientPreferenceConverter.LegacyKeys.*;

public class LegacyClientPreferenceConverter {

    public record IdLocation(String tomlClass, String id) {
        public static LegacyClientPreferenceConverter.IdLocation of(String tomlClass, String id) {
            return new LegacyClientPreferenceConverter.IdLocation(tomlClass, id);
        }
    }

    public static class LegacyKeys {
        public static final String viewDistanceCustomizableSigns = "viewDistanceCustomizableSigns";
        public static final String elementDistancingCustomizableSigns = "elementDistancingCustomizableSigns";
        public static final String imageRenderLayerCacheSize = "imageRenderLayerCacheSize";
        public static final String textRenderLayerCacheSize = "textRenderLayerCacheSize";
        public static final String mwtaLanguage = "mwtaLanguage";
        public static final String viewDistanceSigns = "viewDistanceSigns";
    }

    /**
     * Contains the mappings for conversion between new and old preferences.
     * <p><strong>⚠ No mappings for: ⚠</strong>
     * <ul>
     *     <li><code>has_limit</code>: This is a boolean value that indicates whether there is a limit on the number
     *      of uploads per player. It was previously calculated on-the-fly (if > 0 = true) and generally not a setting
     *      you could tweak. Now it is. Therefore, it has NO mapping!</li>
     * </ul>
     */
    private static final Map<String, LegacyServerPreferenceConverter.IdLocation> ID_MAPPINGS = Map.ofEntries(
            Map.entry(viewDistanceCustomizableSigns, LegacyServerPreferenceConverter.IdLocation.of("customizableSigns", "view_distance")),
            Map.entry(elementDistancingCustomizableSigns, LegacyServerPreferenceConverter.IdLocation.of("customizableSigns", "element_distancing")),
            Map.entry(imageRenderLayerCacheSize, LegacyServerPreferenceConverter.IdLocation.of("rendering", "image_render_layer_cache_size")),
            Map.entry(textRenderLayerCacheSize, LegacyServerPreferenceConverter.IdLocation.of("rendering", "text_render_layer_cache_size")),
            Map.entry(mwtaLanguage, LegacyServerPreferenceConverter.IdLocation.of("general", "language")),
            Map.entry(viewDistanceSigns, LegacyServerPreferenceConverter.IdLocation.of("signs", "view_distance"))
    );

    /**
     * Returns the modern version of the id.
     * @param id The legacy ID.
     * @return The new ID.
     */
    public static LegacyServerPreferenceConverter.IdLocation getNewId(String id) {
        return ID_MAPPINGS.get(id);
    }

    @SuppressWarnings("deprecation")
    public static ClientPreferences produceNewClientPreferences(File oldPreferences) {
        PreferenceJsonLoader loader = new PreferenceJsonLoader(oldPreferences.getPath());
        ClientPreferences preferences = new ClientPreferences();

        preferences.customizableSigns.viewDistance.set(loader.getFloat(viewDistanceCustomizableSigns));
        preferences.customizableSigns.elementDistancing.set(loader.getFloat(elementDistancingCustomizableSigns));
        preferences.general.language.set(loader.getString(mwtaLanguage));
        preferences.rendering.imageRenderLayerCacheSize.set(loader.getInt(imageRenderLayerCacheSize));
        preferences.rendering.textRenderLayerCacheSize.set(loader.getInt(textRenderLayerCacheSize));
        preferences.signs.viewDistance.set(loader.getFloat(viewDistanceSigns));

        return preferences;
    }
}
