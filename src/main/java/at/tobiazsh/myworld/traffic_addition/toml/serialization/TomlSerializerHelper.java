package at.tobiazsh.myworld.traffic_addition.toml.serialization;

import at.tobiazsh.myworld.traffic_addition.toml.serialization.annotation.TomlChild;
import at.tobiazsh.myworld.traffic_addition.toml.serialization.annotation.TomlRoot;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Field;

/**
 * Shared utility for scanning classes annotated with @PreferenceRoot and @PreferenceChild.
 * Can be used by both PreferenceSerializer and PermissionSerializer.
 */
public class TomlSerializerHelper {

    /**
     * Returns the id of a given field or class. If an annotation with a child class contains a value,
     * the value is being used as an id, otherwise the field name is being used.
     * @param field The declared object field, which may be null if the id is being resolved for the root class.
     * @param clazz The class of the object for which the id is being resolved.
     */
    public static String resolveId(@Nullable Field field, Class<?> clazz) {
        TomlChild child = clazz.getAnnotation(TomlChild.class);

        if (child != null && !child.value().isBlank())
            return child.value();

        if (field != null)
            return field.getName();

        return clazz.getSimpleName();
    }

    /**
     * Gets the value of a field from an instance using reflection.
     * @param field The field to get
     * @param instance The instance to get the field from
     * @return The field value or null if not accessible
     */
    public static Object get(Field field, Object instance) {
        try {
            return field.get(instance);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to access field: " + field.getName(), e);
        }
    }

    /**
     * Checks if a class is a serializable node (has @PreferenceRoot or @PreferenceChild annotation).
     * @param clazz The class to check
     * @return true if the class is a serializable node
     */
    public static boolean isSerializableNode(Class<?> clazz) {
        return clazz.isAnnotationPresent(TomlRoot.class)
                || clazz.isAnnotationPresent(TomlChild.class);
    }

    /**
     * Verifies that a class has the @PreferenceRoot annotation.
     * @param clazz The class to check
     * @throws IllegalArgumentException if the class is not annotated with @PreferenceRoot
     */
    public static void verifyRootAnnotation(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(TomlRoot.class)) {
            throw new IllegalArgumentException("Missing @PreferenceRoot annotation on class: " + clazz.getName());
        }
    }
}

