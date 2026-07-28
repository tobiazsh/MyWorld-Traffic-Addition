package at.tobiazsh.myworld.traffic_addition.toml;

import java.util.function.BiFunction;

@FunctionalInterface
public interface NodeFactory<N>  {
    N create(String id, Object instance);

    static <N> NodeFactory<N> of(BiFunction<String, Object, N> factory) {
        return factory::apply;
    }
}
