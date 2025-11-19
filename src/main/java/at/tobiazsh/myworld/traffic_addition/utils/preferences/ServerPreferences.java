package at.tobiazsh.myworld.traffic_addition.utils.preferences;

import java.util.Objects;

public class ServerPreferences {

    public static Preference generalServerPreferences = new Preference("myworld_traffic_addition/server_config.json");

    public static long maximumImageUploadSize = 1024 * 1024 * 5; // 5MB; Default
    public static final long maximumImageUploadSizeDefault = 1024 * 1024 * 5; // 5MB; Default

    public static long maximumThumbnailUploadSize = 1024 * 512; // 512KB; Default
    public static final long maximumThumbnailUploadSizeDefault = 1024 * 512; // 512KB; Default

    public static long maximumMetadataUploadSize = 1024 * 100; // 100KB; Default
    public static final long maximumMetadataUploadSizeDefault = 1024 * 100; // 100KB; Default

    public static void loadPreferences() {
        // Load server preferences
        Long MImageUP = generalServerPreferences.getLong("maximumImageUploadSize");
        maximumImageUploadSize = Objects.requireNonNullElse(MImageUP, maximumImageUploadSizeDefault); // Fallback to default

        Long MThumbnailUP = generalServerPreferences.getLong("maximumThumbnailUploadSize");
        maximumThumbnailUploadSize = Objects.requireNonNullElse(MThumbnailUP, maximumThumbnailUploadSizeDefault); // Fallback to default

        Long MMetadataUP = generalServerPreferences.getLong("maximumMetadataUploadSize");
        maximumMetadataUploadSize = Objects.requireNonNullElse(MMetadataUP, maximumMetadataUploadSizeDefault); // Fallback to default
    }
}
