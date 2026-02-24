package at.tobiazsh.myworld.traffic_addition.utils;

import net.minecraft.resources.Identifier;

public class BackgroundTextureUtil {

    public static Identifier getBackgroundTextureIdentifier(Identifier spriteAtlasId, BorderProperty borders) {
        String bordersBinary = String.format(
                "%4s",
                Integer.toBinaryString(borders.toBinaryRepresentationNoCorners())
        ).replace(' ', '0');

        String id = spriteAtlasId.toString().replace(":", "_");
        return Identifier.fromNamespaceAndPath(id, bordersBinary);
    }

}
