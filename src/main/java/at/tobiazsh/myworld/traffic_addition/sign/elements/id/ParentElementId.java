package at.tobiazsh.myworld.traffic_addition.sign.elements.id;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Represents the unique identifier of a sign element. Used because otherwise some constructors would clash with each
 * other. Basically a wrapper around a UUID.
 */
public record ParentElementId(UUID id) {
    public static ParentElementId of(UUID id) {
        return new ParentElementId(id);
    }
    public static ParentElementId ofString(String id) {
        return new ParentElementId(UUID.fromString(id));
    }

    public ElementId toChild() {
        return ElementId.of(this.id);
    }

    @Override
    public @NotNull String toString() {
        return this.id.toString();
    }
}
