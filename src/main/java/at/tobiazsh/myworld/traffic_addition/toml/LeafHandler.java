package at.tobiazsh.myworld.traffic_addition.toml;

import java.util.function.BiConsumer;

@FunctionalInterface
public interface LeafHandler<N, L extends TomlLeaf> {
    void handleLeaf(N node, L leaf);

    static <N, L extends TomlLeaf> LeafHandler<N, L> of(BiConsumer<N, L> handler) {
        return handler::accept;
    }
}
