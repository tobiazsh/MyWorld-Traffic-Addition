package at.tobiazsh.myworld.traffic_addition.toml.serialization;

import at.tobiazsh.myworld.traffic_addition.toml.LeafHandler;
import at.tobiazsh.myworld.traffic_addition.toml.NodeFactory;
import at.tobiazsh.myworld.traffic_addition.toml.TomlLeaf;
import at.tobiazsh.myworld.traffic_addition.toml.TomlNode;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Field;

/**
 * Scanner class that scans a root class annotated with @TomlRoot and its nested children annotated with @TomlChild,
 * building a tree structure of objects extending {@link TomlNode} and handling leafs extending {@link TomlLeaf}.
 * @param <N> The type of Node extending {@link TomlNode}.
 * @param <L> The type of Leaf extending {@link TomlLeaf}.
 */
public class TomlScanner<N extends TomlNode<L>, L extends TomlLeaf> {

    private final NodeFactory<N> factory;
    private final LeafHandler<N, L> leafHandler;
    private final Class<L> leafType;

    public TomlScanner(NodeFactory<N> factory, LeafHandler<N, L> leafHandler, Class<L> leafType) {
        this.factory = factory;
        this.leafHandler = leafHandler;
        this.leafType = leafType;
    }

    /**
     * Scans a class annotated with @TomlRoot and its nested children annotated with @TomlChild and
     * builds a tree from objects extending {@link TomlNode}, representing the structure of the root class.
     * @param root The root class to scan, which must be annotated with @TomlRoot.
     * @return The root {@link TomlNode} representing the scanned structure.
     */
    public N scan(Object root) {
        Class<?> clazz = root.getClass();

        TomlSerializerHelper.verifyRootAnnotation(clazz);

        return scanNode(null, root);
    }

    /**
     * Scans a node for leafs and children and builds the structure.
     * @param field The declared field of the leaf, which should be null if scanning another node.
     * @param instance The instance of the node or leaf.
     * @return The built structure, represented by objects extending TomlNode.
     */
    private N scanNode(@Nullable Field field, Object instance) {
        Class<?> clazz = instance.getClass();

        String id = TomlSerializerHelper.resolveId(field, clazz);

        N node = factory.create(id, instance);

        for (Field childField : clazz.getFields()) {
            Object value = TomlSerializerHelper.get(childField, instance);

            if (value == null)
                continue;

            if (leafType.isInstance(value)) {
                L leaf = leafType.cast(value);
                leafHandler.handleLeaf(node, leaf);
                continue;
            }

            if (TomlSerializerHelper.isSerializableNode(value.getClass())) {
                N childNode = scanNode(childField, value);

                node.children().put(
                        childNode.id(),
                        childNode
                );
            }
        }

        return node;
    }

}
