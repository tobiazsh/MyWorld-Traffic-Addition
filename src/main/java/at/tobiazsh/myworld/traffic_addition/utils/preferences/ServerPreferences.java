package at.tobiazsh.myworld.traffic_addition.utils.preferences;

public class ServerPreferences {

    public static Preference generalServerPreferences = new Preference("myworld_traffic_addition/server_config.json");

    public static long maximumImageUploadSize = 1024 * 1024 * 5; // 5MB; Default
    public static final long maximumImageUploadSizeDefault = 1024 * 1024 * 5; // 5MB; Default

    public static long maximumThumbnailUploadSize = 1024 * 512; // 512KB; Default
    public static final long maximumThumbnailUploadSizeDefault = 1024 * 512; // 512KB; Default

    public static long maximumMetadataSize = 1024 * 100; // 100KB; Default
    public static final long maximumMetadataSizeDefault = 1024 * 100; //

    public static void loadPreferences() {
        // Load server preferences
        maximumImageUploadSize = generalServerPreferences.getLong("maximumImageUploadSize");
        if (maximumImageUploadSize == Preference.INVALID_LONG)
            maximumImageUploadSize = maximumImageUploadSizeDefault; // Fallback to default

        maximumThumbnailUploadSize = generalServerPreferences.getLong("maximumThumbnailUploadSize");
        if (maximumThumbnailUploadSize == Preference.INVALID_LONG)
            maximumThumbnailUploadSize = maximumThumbnailUploadSizeDefault; // Fallback to default

        maximumMetadataSize = generalServerPreferences.getLong("maximumMetadataSize");
        if (maximumMetadataSize == Preference.INVALID_LONG)
            maximumMetadataSize = maximumMetadataSizeDefault; // Fallback to default
    }
}
