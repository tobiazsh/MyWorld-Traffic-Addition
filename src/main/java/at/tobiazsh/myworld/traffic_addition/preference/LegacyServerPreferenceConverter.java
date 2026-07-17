package at.tobiazsh.myworld.traffic_addition.preference;

import java.io.File;
import java.util.Map;

import static at.tobiazsh.myworld.traffic_addition.preference.LegacyServerPreferenceConverter.LegacyKeys.*;

public class LegacyServerPreferenceConverter {

    public record IdLocation(String tomlClass, String id) {
        public static IdLocation of(String tomlClass, String id) {
            return new IdLocation(tomlClass, id);
        }
    }

    public static class LegacyKeys {
        public static final String maximumImageUploadSizeKey = "maximumImageUploadSize";
        public static final String maximumThumbnailUploadSizeKey = "maximumThumbnailUploadSize";
        public static final String maximumMetadataUploadSizeKey = "maximumMetadataUploadSize";
        public static final String isPlayerUploadEnabledKey = "isPlayerUploadEnabled";
        public static final String maximumUploadsPerPlayerKey = "maximumUploadsPerPlayer";
        public static final String customImageDownloadTimeoutKey = "customImageDownloadTimeout";
        public static final String maxCustomizableSignWidthKey = "maximumCustomizableSignWidth";
        public static final String maxCustomizableSignHeightKey = "maximumCustomizableSignHeight";
        public static final String maxCustomizableSignElementsKey = "maximumCustomizableSignElements";
    }

    /**
     * Contains the mappings for conversion between new and old preferences.
     * <p><strong>⚠ No mappings for: ⚠</strong>
     * <ul>
     *     <li><code>has_limit</code>: This is a boolean value that indicates whether there is a limit on the number
     *      of uploads per player. It was previously calculated on-the-fly (if > 0 = true) and generally not a setting
     *      you could tweak. Now it is. Therefore, it has NO mapping!</li>
     * </ul>
     *
     * No importance:
     * (yes, this comment WAS written by a human with feelings and not by an LLM. I just used symbols to get the
     * attention lmao. Here's proof I'm not a robot: Strawberry contains 3 r's :D)
     */
    private static final Map<String, IdLocation> ID_MAPPINGS = Map.ofEntries(
            Map.entry(maximumImageUploadSizeKey, IdLocation.of("customizableSigns.onlineImages", "max_size")),
            Map.entry(maximumThumbnailUploadSizeKey, IdLocation.of("customizableSigns.onlineImages", "max_thumbnail_size")),
            Map.entry(maximumMetadataUploadSizeKey, IdLocation.of("customizableSigns.onlineImages", "max_metadata_size")),
            Map.entry(isPlayerUploadEnabledKey, IdLocation.of("customizableSigns.onlineImages", "upload_enabled")),
            Map.entry(maximumUploadsPerPlayerKey, IdLocation.of("customizableSigns.onlineImages", "max_uploads_per_player")),
            Map.entry(customImageDownloadTimeoutKey, IdLocation.of("customizableSigns.onlineImages", "download_timeout")),

            Map.entry(maxCustomizableSignWidthKey, IdLocation.of("customizableSigns.general", "max_width")),
            Map.entry(maxCustomizableSignHeightKey, IdLocation.of("customizableSigns.general", "max_height")),
            Map.entry(maxCustomizableSignElementsKey, IdLocation.of("customizableSigns.general", "max_elements"))
    );

    /**
     * Returns the modern version of the id.
     * @param id The legacy ID.
     * @return The new ID.
     */
    public static IdLocation getNewId(String id) {
        return ID_MAPPINGS.get(id);
    }

    /**
     * Converts an old server preference file to the new TOML format.
     * @param oldPreferences The old server preference file.
     * @return The new server preferences.
     */
    @SuppressWarnings("deprecation")
    public static ServerPreferences produceNewServerPreferences(File oldPreferences) {
        PreferenceJsonLoader loader = new PreferenceJsonLoader(oldPreferences.getPath());
        ServerPreferences preferences = new ServerPreferences();

        preferences.customizableSigns.onlineImages.downloadTimeout.set(loader.getLong(customImageDownloadTimeoutKey));
        preferences.customizableSigns.onlineImages.maxSize.set(loader.getLong(maximumImageUploadSizeKey));
        preferences.customizableSigns.onlineImages.maxThumbnailSize.set(loader.getLong(maximumThumbnailUploadSizeKey));
        preferences.customizableSigns.onlineImages.maxMetadataSize.set(loader.getLong(maximumMetadataUploadSizeKey));
        preferences.customizableSigns.onlineImages.uploadEnabled.set(loader.getBoolean(isPlayerUploadEnabledKey));
        preferences.customizableSigns.onlineImages.maxUploadsPerPlayer.set(loader.getInt(maximumUploadsPerPlayerKey));

        preferences.customizableSigns.onlineImages.hasLimit.set(
                preferences.customizableSigns.onlineImages.maxUploadsPerPlayer.getOrDefault() > 0
        );

        preferences.customizableSigns.general.maxHeight.set(loader.getShort(maxCustomizableSignHeightKey));
        preferences.customizableSigns.general.maxWidth.set(loader.getShort(maxCustomizableSignWidthKey));
        preferences.customizableSigns.general.maxElements.set(loader.getShort(maxCustomizableSignElementsKey));

        return preferences;
    }
}
