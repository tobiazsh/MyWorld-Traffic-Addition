package at.tobiazsh.myworld.traffic_addition.Utils;

import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public class Coordinates {
    public float x, y, z;
    public Direction direction;

    public Coordinates(float xs, float ys, float zs, @Nullable Direction direction) {
        this.x = xs;
        this.y = ys;
        this.z = zs;
        this.direction = direction;
    }
}
