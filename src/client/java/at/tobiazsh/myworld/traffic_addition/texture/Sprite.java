package at.tobiazsh.myworld.traffic_addition.texture;

import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class Sprite {

    public final Identifier spriteId;
    public final int x, y, width, height;
    public final float u1, v1, u2, v2;

    /**
     * Creates a sprite instance
     * @param spriteId Identifier of the sprite
     * @param x Starting X-Coordinate of Sprite in {@link SpriteAtlas}
     * @param y Starting Y-Coordinate of Sprite in {@link SpriteAtlas}
     * @param width Width of the sprite
     * @param height Height of the sprite
     * @param atlasWidth Width of the {@link SpriteAtlas} the sprite is located in
     * @param atlasHeight Height of the {@link SpriteAtlas} the sprite is located in
     */
    public Sprite(Identifier spriteId, int x, int y, int width, int height, int atlasWidth, int atlasHeight) {
        this.spriteId = spriteId;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.u1 = (float) x / (float) atlasWidth;
        this.v1 = (float) y / (float) atlasHeight;
        this.u2 = (float) (x + width) / (float) atlasWidth;
        this.v2 = (float) (y + height) / (float) atlasHeight;
    }

}
