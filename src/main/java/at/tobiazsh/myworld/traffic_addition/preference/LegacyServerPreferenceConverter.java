package at.tobiazsh.myworld.traffic_addition.preference;

import at.tobiazsh.myworld.traffic_addition.toml.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import static at.tobiazsh.myworld.traffic_addition.preference.LegacyServerPreferenceConverter.LegacyKeys.*;

public class LegacyPreferenceConverter {

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

    @SuppressWarnings("deprecation")
    public static ServerPreferences produceNewServerPreferences(File oldPreferences) {
        PreferenceJsonLoader loader = new PreferenceJsonLoader(oldPreferences.getPath());
        PreferencesSetter setter = new PreferencesSetter(loader);
        ServerPreferences preferences = new ServerPreferences();

        setter.setLongPreference(
                preferences.customizableSigns.onlineImages.downloadTimeout,
                customImageDownloadTimeoutKey
        );

        setter.setLongPreference(
                preferences.customizableSigns.onlineImages.maxSize,
                maximumImageUploadSizeKey
        );

        setter.setLongPreference(
                preferences.customizableSigns.onlineImages.maxThumbnailSize,
                maximumThumbnailUploadSizeKey
        );

        setter.setLongPreference(
                preferences.customizableSigns.onlineImages.maxMetadataSize,
                maximumMetadataUploadSizeKey
        );

        setter.setPreference(
                preferences.customizableSigns.onlineImages.uploadEnabled,
                isPlayerUploadEnabledKey,
                PreferenceJsonLoader::getBoolean,
                TomlBoolean::new
        );

        setter.setPreference(
                preferences.customizableSigns.onlineImages.maxUploadsPerPlayer,
                maximumUploadsPerPlayerKey,
                PreferenceJsonLoader::getInt,
                TomlInteger::new
        );

        preferences.customizableSigns.onlineImages.hasLimit.set(
                new TomlBoolean(
                        preferences.customizableSigns.onlineImages.maxUploadsPerPlayer.getValue().value() == 0
                )
        );

        setter.setPreference(
                preferences.customizableSigns.general.maxHeight,
                maxCustomizableSignHeightKey,
                PreferenceJsonLoader::getShort,
                TomlShort::new
        );

        setter.setPreference(
                preferences.customizableSigns.general.maxWidth,
                maxCustomizableSignWidthKey,
                PreferenceJsonLoader::getShort,
                TomlShort::new
        );

        setter.setPreference(
                preferences.customizableSigns.general.maxElements,
                maxCustomizableSignElementsKey,
                PreferenceJsonLoader::getShort,
                TomlShort::new
        );

        return preferences;
    }

    @SuppressWarnings("deprecation")
    public static class PreferencesSetter {
        private final PreferenceJsonLoader loader;

        public PreferencesSetter(PreferenceJsonLoader loader) {
            this.loader = loader;
        }

        /**
         * Helper method for setting a value on a preference from the old loader method.
         * @param preference The preference to set
         * @param key The key of the old preference
         * @param getter The getter for the preference
         * @param constructor The constructor for the TomlValue
         * @param <P> The primitive type of the value
         * @param <T> The TomlValue type
         */
        public <P, T extends TomlValue<P>> void setPreference(
                @NotNull Preference<T> preference,
                @NotNull String key,
                @NotNull BiFunction<PreferenceJsonLoader, String, P> getter,
                @NotNull Function<P, T> constructor
        ) {
            P value = Objects.requireNonNullElse(
                    getter.apply(loader, key),
                    preference.getDefault().value()
            );

            preference.set(constructor.apply(value));
        }

        /**
         * Helper method to quickly set a long preference from the old loader method.
         * @param preference The preference to set
         * @param key The key of the old preference
         */
        public void setLongPreference(
                Preference<TomlLong> preference,
                String key
        ) {
            setPreference(
                    preference,
                    key,
                    PreferenceJsonLoader::getLong,
                    TomlLong::new
            );
        }
    }

}
