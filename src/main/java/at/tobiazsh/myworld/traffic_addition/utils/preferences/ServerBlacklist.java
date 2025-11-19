package at.tobiazsh.myworld.traffic_addition.utils.preferences;

import at.tobiazsh.myworld.traffic_addition.utils.UuidUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class ServerBlacklist {

    public static Preference serverBlacklistPreference = new Preference("myworld_traffic_addition/blacklists/server_blacklist.json");

    public static HashSet<UUID> bannedImageUploadPlayers;
    public static HashSet<UUID> restoreList;

    public static void loadBlacklist() {
        loadBannedImageUpload();
    }

    private static void loadBannedImageUpload() {
        JsonArray list = serverBlacklistPreference.getJsonArray("bannedImageUploadPlayers");
        if (list == null) {
            bannedImageUploadPlayers = new HashSet<>();
            return;
        }

        List<UUID> uuidList = list.asList().stream().filter(
                element -> element.isJsonPrimitive() &&
                        element.getAsJsonPrimitive().isString())
                .map(JsonElement::getAsString)
                .filter(UuidUtils::isValidUUID)
                .map(UUID::fromString)
                .toList();

        bannedImageUploadPlayers = new HashSet<>(uuidList);
    }

    public static void saveBlacklist() {
        saveBannedImageUpload();
    }

    public static void saveBannedImageUpload() {
        JsonArray list = new JsonArray();
        bannedImageUploadPlayers.forEach(uuid -> list.add(uuid.toString()));
        serverBlacklistPreference.saveToDisk("bannedImageUploadPlayers", list);
    }

    // ---------------------------------------------------------------
    // ADD -----------------------------------------------------------

    public static void addToBlacklist(UUID playerUUID) {
        bannedImageUploadPlayers.add(playerUUID);
    }

    // ---------------------------------------------------------------
    // REMOVE --------------------------------------------------------

    public static void removeFromBlacklist(UUID playerUUID) {
        bannedImageUploadPlayers.remove(playerUUID);
    }

    // ---------------------------------------------------------------
    // OTHER ---------------------------------------------------------

    public static String getBlacklistAsString() {
        StringBuilder sb = new StringBuilder();
        for (UUID uuid : bannedImageUploadPlayers) {
            sb.append(uuid.toString()).append("\n");
        }
        return sb.toString();
    }

    public static boolean isPlayerBlacklisted(UUID playerUUID) {
        return bannedImageUploadPlayers.contains(playerUUID);
    }

    public static void clearBlacklist() {
        restoreList = new HashSet<>(bannedImageUploadPlayers);
        bannedImageUploadPlayers.clear();
    }

    public static boolean canRestoreBlacklist() {
        return restoreList != null;
    }

    public static void restoreBlacklist() {
        if (restoreList != null) {
            bannedImageUploadPlayers = new HashSet<>(restoreList);
            restoreList = null;
        }
    }
}
