package at.tobiazsh.myworld.traffic_addition.utils;

import net.minecraft.resources.Identifier;

public class BackgroundTextureUtil {

    public static Identifier getBackgroundTextureIdentifier(Identifier spriteAtlasId, BorderProperty borders) {
        String bordersBinary = Integer.toBinaryString(borders.toBinaryRepresentationNoCorners());
        String id = spriteAtlasId.toString().replace(":", "_");
        return Identifier.fromNamespaceAndPath(id, bordersBinary);
    }

}
