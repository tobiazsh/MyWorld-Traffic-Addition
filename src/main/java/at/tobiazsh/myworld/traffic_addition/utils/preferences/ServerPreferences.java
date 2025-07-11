package at.tobiazsh.myworld.traffic_addition.utils.preferences;

public class ServerPreferences {

    public static Preference generalServerPreferences = new Preference("myworld_traffic_addition/server_config.json");

    public static long maximumImageUploadSize = 1024 * 1024 * 5; // 5MB; Default
    public static long maximumImageUploadSizeDefault = 1024 * 1024 * 5; // 5MB; Default

    public static void loadPreferences() {
        // Load server preferences
        maximumImageUploadSize = generalServerPreferences.getLong("maximumImageUploadSize");
        if (maximumImageUploadSize == Preference.INVALID_LONG)
            maximumImageUploadSize = maximumImageUploadSizeDefault; // Fallback to default
    }
}
