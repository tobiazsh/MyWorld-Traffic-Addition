package at.tobiazsh.myworld.traffic_addition.sign.elements.id;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Represents the unique identifier of a sign element. Used because otherwise some constructors would clash with each
 * other. Basically a wrapper around a UUID.
 */
public record ElementId(UUID id) {
    public static ElementId of(UUID id) {
        return new ElementId(id);
    }

    public ParentElementId toParent() {
        return ParentElementId.of(this.id);
    }

    public static ElementId random() {
        return new ElementId(UUID.randomUUID());
    }

    @Override
    public @NotNull String toString() {
        return this.id.toString();
    }
}
