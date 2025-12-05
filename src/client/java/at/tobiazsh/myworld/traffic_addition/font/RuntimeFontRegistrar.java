package at.tobiazsh.myworld.traffic_addition.font;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.resource.Location;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TrueTypeFontLoader;

public class RuntimeFontRegistrar {
    private RuntimeFontRegistrar() {}

    public static void registerFontFromFile(
            Location location,
            float fontSize,
            float oversampleIndex,
            TrueTypeFontLoader.Shift shift,
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

        MinecraftClient client = MinecraftClient.getInstance();
        client.execute(() -> {
            try {
                client.reloadResources();
            } catch (Exception e) {
                MyWorldTrafficAddition.LOGGER.error("Failed to reload resources after registering font from location: {}", location, e);
            }
        });
    }
}
