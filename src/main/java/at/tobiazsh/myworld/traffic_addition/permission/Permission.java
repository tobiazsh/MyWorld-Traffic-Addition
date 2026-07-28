package at.tobiazsh.myworld.traffic_addition.permission;

import at.tobiazsh.myworld.traffic_addition.exception.PermissionExistsException;
import at.tobiazsh.myworld.traffic_addition.exception.PermissionNotFoundException;
import at.tobiazsh.myworld.traffic_addition.toml.LeafHandler;
import at.tobiazsh.myworld.traffic_addition.toml.NodeFactory;
import at.tobiazsh.myworld.traffic_addition.toml.TomlLeaf;
import at.tobiazsh.myworld.traffic_addition.toml.TomlNode;
import at.tobiazsh.myworld.traffic_addition.toml.codec.Codec;
import at.tobiazsh.myworld.traffic_addition.toml.serialization.TomlScanner;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.Function;

/**
 * Class to store fine-tuned permission for players
 * @param <T> The type of permission (e.g. Integer, Boolean, etc.)
 */
public class Permission<T> implements TomlLeaf {

    @SuppressWarnings("unchecked")
    public static final Class<Permission<?>> LEAF_TYPE =
            (Class<Permission<?>>) (Class<?>) Permission.class;

    public static final TomlScanner<TomlNode<Permission<?>>, Permission<?>> SCANNER =
            new TomlScanner<>(
                    NodeFactory.of(TomlNode<Permission<?>>::new),
                    LeafHandler.of(
                            (node, permission) -> node.entries().put(
                                    permission.getId(),
                                    permission
                            )
                    ),
                    LEAF_TYPE
            );

    private final HashMap<UUID, T> entries = new HashMap<>();
    private final Function<T, Boolean> conditionCheck;
    @NonNull public final String permissionId;
    @NonNull private final Codec<T> codec;

    public Permission(@NonNull String id, @NonNull Function<T, Boolean> conditionCheck, @NonNull Codec<T> codec) {
        this.conditionCheck = conditionCheck;
        this.permissionId = id;
        this.codec = codec;
    }

    /**
     * Gives a player a certain permission of type T.
     * @param player The player to permit sth.
     * @param value Value of the permission to give to the player.
     * @param overrule Whether to ignore current permissions and overrule them.
     * @throws PermissionExistsException If a permission for the player already exists and overrule is `false`.
     */
    public void allow(@NonNull ServerPlayer player, T value, boolean overrule) throws PermissionExistsException {
        allow(player.getUUID(), value, overrule);
    }

    /**
     * Gives a player a certain permission of type T by UUID.
     * @param playerUuid The UUID of the player to permit sth.
     * @param value Value of the permission to give to the player.
     * @param overrule Whether to ignore current permissions and overrule them.
     * @throws PermissionExistsException If a permission for the player already exists and overrule is `false`.
     */
    public void allow(@NonNull UUID playerUuid, T value, boolean overrule) throws PermissionExistsException {
        if (!overrule && entries.containsKey(playerUuid))
            throw new PermissionExistsException("Player " + playerUuid + " already exists");

        entries.put(playerUuid, value);
    }

    /**
     * Gives a player a certain permission of type T.
     * @param player The player to permit sth.
     * @param value Value of the permission to give to the player.
     * @throws PermissionExistsException If a permission for the player already exists.
     */
    public void allow(@NonNull ServerPlayer player, T value) throws PermissionExistsException {
        allow(player.getUUID(), value, false);
    }

    /**
     * Gives a player a certain permission of type T by UUID.
     * @param playerUuid The UUID of the player to permit sth.
     * @param value Value of the permission to give to the player.
     * @throws PermissionExistsException If a permission for the player already exists.
     */
    public void allow(@NonNull UUID playerUuid, T value) throws PermissionExistsException {
        allow(playerUuid, value, false);
    }

    /**
     * Removes a permission from a player.
     * @param player The target player.
     * @throws PermissionNotFoundException If no permission associated with this player could be found.
     */
    public void forbid(@NonNull ServerPlayer player) throws PermissionNotFoundException {
        forbid(player.getUUID());
    }

    /**
     * Removes a permission from a player by UUID.
     * @param playerUuid The UUID of the target player.
     * @throws PermissionNotFoundException If no permission associated with this player could be found.
     */
    public void forbid(@NonNull UUID playerUuid) throws PermissionNotFoundException {
        if (!entries.containsKey(playerUuid))
            throw new PermissionNotFoundException("Player " + playerUuid + " does not exist");

        entries.remove(playerUuid);
    }

    /**
     * Checks if a player is allowed to do sth. based on the permission and the condition. Uses the default permission
     * given at class initialization.
     * @param player The player to test for.
     * @return true if allowed, false if not
     */
    public boolean isAllowed(@NonNull ServerPlayer player) {
        return isAllowed(player.getUUID());
    }

    /**
     * Checks if a player is allowed to do sth. based on the permission and the condition by UUID.
     * @param playerUuid The UUID of the player to test for.
     * @return true if allowed, false if not
     */
    public boolean isAllowed(@NonNull UUID playerUuid) {
        return isAllowed(playerUuid, this.conditionCheck);
    }

    /**
     * Checks if a player is allowed to do sth. based on the permission and the condition.
     * @param player The player to test for.
     * @param conditionCheck The condition to check for the permission.
     * @return true if allowed, false if not
     */
    public boolean isAllowed(@NonNull ServerPlayer player, @NonNull Function<T, Boolean> conditionCheck) {
        return isAllowed(player.getUUID(), conditionCheck);
    }

    /**
     * Checks if a player is allowed to do sth. based on the permission and the condition by UUID.
     * @param playerUuid The UUID of the player to test for.
     * @param conditionCheck The condition to check for the permission.
     * @return true if allowed, false if not
     */
    public boolean isAllowed(@NonNull UUID playerUuid, @NonNull Function<T, Boolean> conditionCheck) {
        return conditionCheck.apply(entries.get(playerUuid));
    }

    /**
     * Returns the current value for the given player
     */
    public @Nullable T get(@NonNull ServerPlayer player) {
        return get(player.getUUID());
    }

    /**
     * Returns the current value for the given player by UUID
     */
    public @Nullable T get(@NonNull UUID playerUuid) {
        return entries.get(playerUuid);
    }

    /**
     * Returns the codec for this permission
     */
    public @NonNull Codec<T> getCodec() {
        return codec;
    }

    /**
     * Returns all permission entries
     */
    public @NonNull HashMap<UUID, T> getEntries() {
        return entries;
    }

    /**
     * Returns the permission ID
     */
    public @NonNull String getId() {
        return permissionId;
    }
}
