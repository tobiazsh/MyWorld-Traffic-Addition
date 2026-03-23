package at.tobiazsh.myworld.traffic_addition.utils;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import org.jetbrains.annotations.NotNull;

/**
 * Creates a BorderProperty with the specified boolean values for each side.
 */
public record BorderProperty(
            boolean up,
            boolean right,
            boolean down,
            boolean left,

            boolean cornerUpRight,
            boolean cornerUpLeft,
            boolean cornerDownRight,
            boolean cornerDownLeft
) implements StringableObject<BorderProperty> {

    public static final BorderProperty INSTANCE = new BorderProperty(
            false, false, false, false,
            false, false, false, false
    );

    public static final String DEFAULT = "BorderProperty{false, false, false, false}";

    /**
     * Converts the BorderProperty to a string representation. Formatted as "BorderProperty{up, right, down, left}".
     */
    @Override
    public @NotNull String toObjectString() {
        return "BorderProperty{%s,%s,%s,%s,%s,%s,%s,%s}".formatted(
                up, right, down, left,
                cornerUpRight, cornerUpLeft, cornerDownRight, cornerDownLeft
        );
    }

    @Override
    public BorderProperty fromString(String borderProperty) {
        String[] parts = borderProperty
                .substring(
                        borderProperty.indexOf("{") + 1,
                        borderProperty.lastIndexOf("}")
                )
                .split(",");

        if (parts.length < 4) {
            throw new IllegalArgumentException("Invalid BorderProperty format. Expected format: up, right, down, left, cornerUpRight, cornerUpLeft, cornerDownRight, cornerDownLeft");
        }

        boolean up = Boolean.parseBoolean(parts[0].trim());
        boolean right = Boolean.parseBoolean(parts[1].trim());
        boolean down = Boolean.parseBoolean(parts[2].trim());
        boolean left = Boolean.parseBoolean(parts[3].trim());

        boolean cornerUpRight = false;
        boolean cornerUpLeft = false;
        boolean cornerDownRight = false;
        boolean cornerDownLeft = false;

        if (parts.length != 8) {
            MyWorldTrafficAddition.LOGGER.error("Unable to determine corners for BorderProperty: {}! Interpreting all as false!", borderProperty);
        } else {
            cornerUpRight = Boolean.parseBoolean(parts[4].trim());
            cornerUpLeft = Boolean.parseBoolean(parts[5].trim());
            cornerDownRight = Boolean.parseBoolean(parts[6].trim());
            cornerDownLeft = Boolean.parseBoolean(parts[7].trim());
        }

        return new BorderProperty(
                up, right, down, left,
                cornerUpRight, cornerUpLeft, cornerDownRight, cornerDownLeft
        );
    }

    public String toBackgroundString() {
        return "%s_%s_%s_%s".formatted(up, right, down, left);
    }

    /**
     * <p>
     *     Returns a binary representation of the border property. Example:
     * </p>
     * <p>
     *     <pre>
     *         {@code
     *         up = false
     *         right = true
     *         down = false
     *         left = true
     *         }
     *     </pre>
     *     ...would equal to
     *     <pre>
     *         {@code
     *         0101
     *         }
     *     </pre>
     *     ... where {@code 0 == false} and {@code 1 == true}.
     * </p>
     */
    public int toBinaryRepresentationNoCorners() {
        int flag = 0x0000;
        if (up)    flag |= 1 << 3;
        if (right) flag |= 1 << 2;
        if (down)  flag |= 1 << 1;
        if (left)  flag |= 1;

        return flag;
    }

    /**
     * Converts the BorderProperty to a normal string representation. Formatted as "up_right_down_left".
     */
    public String toNormalString() {
        return "%s_%s_%s_%s_%s_%s_%s_%s".formatted(
                up, right, down, left,
                cornerUpRight, cornerUpLeft, cornerDownRight, cornerDownLeft
        );
    }

    public String normalStringWithoutCorners() {
        return "%s_%s_%s_%s".formatted(
                up, right, down, left
        );
    }

    /**
     * Check if all borders are active (up, right, down, left). Corners are not considered in this check.
     */
    public boolean hasAllBorders() {
        return up() && right() && left() && down();
    }

}
