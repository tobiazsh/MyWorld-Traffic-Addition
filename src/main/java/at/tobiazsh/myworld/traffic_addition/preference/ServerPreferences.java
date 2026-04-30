package at.tobiazsh.myworld.traffic_addition.preference;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;

public class ServerPreferences {

    public static Preference generalServerPreferences = new Preference("myworld_traffic_addition/server_config.json");

    public static long maximumImageUploadSize = 1024 * 1024 * 5; // 5MB; Default
    private static final long maximumImageUploadSizeDefault = 1024 * 1024 * 5; // 5MB; Default

    public static long maximumThumbnailUploadSize = 1024 * 512; // 512KB; Default
    private static final long maximumThumbnailUploadSizeDefault = 1024 * 512; // 512KB; Default

    public static long maximumMetadataUploadSize = 1024 * 100; // 100KB; Default
    private static final long maximumMetadataUploadSizeDefault = 1024 * 100; // 100KB; Default

    public static boolean isPlayerUploadEnabled = true;
    private static final boolean isPlayerUploadEnabledDefault = true; // Default

    public static boolean isUploadLimitSet = false;
    public static int maximumUploadsPerPlayer = 0;

    public static long customImageDownloadTimeout = 0;
    private static final long customImageDownloadTimeoutDefault = 15_000; // Default; 15 Seconds; Time in milliseconds
    public static final String maximumImageUploadSizeKey = "maximumImageUploadSize";
    public static final String maximumThumbnailUploadSizeKey = "maximumThumbnailUploadSize";
    public static final String maximumMetadataUploadSizeKey = "maximumMetadataUploadSize";
    public static final String isPlayerUploadEnabledKey = "isPlayerUploadEnabled";
    public static final String maximumUploadsPerPlayerKey = "maximumUploadsPerPlayer";
    public static final String customImageDownloadTimeoutKey = "customImageDownloadTimeout";
    public static final String maxCustomizableSignWidthKey = "maximumCustomizableSignWidth";
    public static final String maxCustomizableSignHeightKey = "maximumCustomizableSignHeight";

    public static void loadPreferences() {

        try {
            generalServerPreferences.createFileIfNotExist();
        } catch (URISyntaxException e) {
            MyWorldTrafficAddition.LOGGER.error("Failed to load Server Preferences as URI is faulty!", e);
        } catch (IOException e) {
            MyWorldTrafficAddition.LOGGER.error("Error while reading/writing while checking whether Server Preferences exists.", e);
        }

        // Load server preferences
        Long MImageUP = generalServerPreferences.getLong(maximumImageUploadSizeKey);
        maximumImageUploadSize = Objects.requireNonNullElse(MImageUP, maximumImageUploadSizeDefault); // Fallback to default

        Long MThumbnailUP = generalServerPreferences.getLong(maximumThumbnailUploadSizeKey);
        maximumThumbnailUploadSize = Objects.requireNonNullElse(MThumbnailUP, maximumThumbnailUploadSizeDefault); // Fallback to default

        Long MMetadataUP = generalServerPreferences.getLong(maximumMetadataUploadSizeKey);
        maximumMetadataUploadSize = Objects.requireNonNullElse(MMetadataUP, maximumMetadataUploadSizeDefault); // Fallback to default

        Boolean isPUE = generalServerPreferences.getBoolean(isPlayerUploadEnabledKey);
        isPlayerUploadEnabled = Objects.requireNonNullElse(isPUE, isPlayerUploadEnabledDefault); // Fallback to default

        Integer MUploadsPP = generalServerPreferences.getInt(maximumUploadsPerPlayerKey);
        if (MUploadsPP != null && MUploadsPP > 0) {
            isUploadLimitSet = true;
            maximumUploadsPerPlayer = MUploadsPP;
        } else {
            isUploadLimitSet = false;
            maximumUploadsPerPlayer = 0;
        }

        Long CIDT = generalServerPreferences.getLong(customImageDownloadTimeoutKey);
        customImageDownloadTimeout = Objects.requireNonNullElse(CIDT, customImageDownloadTimeoutDefault); // Fallback to default
    }
}
