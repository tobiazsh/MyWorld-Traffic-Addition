package at.tobiazsh.myworld.traffic_addition.font;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.resource.Location;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.providers.TrueTypeGlyphProviderDefinition;

public class RuntimeFontRegistrar {
    private RuntimeFontRegistrar() {}

    public static void registerFontFromFile(
            Location location,
            float fontSize,
            float oversampleIndex,
            TrueTypeGlyphProviderDefinition.Shift shift,
            String skipChars
    ) {
        CustomTrueTypeFontLoader loader = new CustomTrueTypeFontLoader(
                location,
                fontSize,
                oversampleIndex,
                shift,
                skipChars
        );

        RuntimeFontRegistry.register(loader);

        Minecraft client = Minecraft.getInstance();
        client.execute(() -> {
            try {
                client.reloadResourcePacks();
            } catch (Exception e) {
                MyWorldTrafficAddition.LOGGER.error("Failed to reload resources after registering font from location: {}", location, e);
            }
        });
    }
}
