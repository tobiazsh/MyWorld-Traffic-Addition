package at.tobiazsh.myworld.traffic_addition.utils;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import net.minecraft.util.Identifier;

public class IdentifierUtils {

    /**
     * Trims away the "/assets/modid/" part from an Identifier's path if it exists.
     *
     * @param id The original Identifier.
     * @return A new Identifier with the trimmed path.
     */
    public static Identifier trimAssets(Identifier id) {
        String namespace = id.getNamespace();
        String path = id.getPath();

        String trimAway = "/assets/" + MyWorldTrafficAddition.MOD_ID + "/";

        if (path.startsWith(trimAway)) {
            path = path.substring(trimAway.length());
        }

        return Identifier.of(namespace, path);
    }

    /**
     * Trims away the specified string from the start of an Identifier's path if it exists.
     *
     * @param id The original Identifier.
     * @param toTrim The string to trim away from the start of the path.
     * @return A new Identifier with the trimmed path.
     */
    public static Identifier trim(Identifier id, String toTrim) {
        String namespace = id.getNamespace();
        String path = id.getPath();

        if (path.startsWith(toTrim)) {
            path = path.substring(toTrim.length());
        }

        return Identifier.of(namespace, path);
    }
}
