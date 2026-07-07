package at.tobiazsh.myworld.traffic_addition.preference;

import at.tobiazsh.myworld.traffic_addition.rendering.renderers.CustomizableSignBlockEntityRenderer;
import at.tobiazsh.myworld.traffic_addition.rendering.renderers.SignBlockEntityRenderer;

import java.util.Objects;

public class ClientPreferences {

    public static final PreferenceJsonLoader GAMEPLAY_PREFERENCE_LOADER = new PreferenceJsonLoader("myworld_traffic_addition/gameplay_config.json");

    public static void loadGameplayPreferences() {
        // SIGNS
        SignBlockEntityRenderer.zOffsetRenderLayer = Objects.requireNonNullElse(
                GAMEPLAY_PREFERENCE_LOADER.getFloat("viewDistanceSigns"),
                SignBlockEntityRenderer.zOffsetRenderLayerDefault
        );

        // CUSTOMIZABLE SIGNS
        CustomizableSignBlockEntityRenderer.zOffsetRenderLayer = Objects.requireNonNullElse(
                GAMEPLAY_PREFERENCE_LOADER.getFloat("viewDistanceCustomizableSigns"),
                CustomizableSignBlockEntityRenderer.zOffsetRenderLayerDefault
        );

        CustomizableSignBlockEntityRenderer.elementDistancingRenderLayer = Objects.requireNonNullElse(
                GAMEPLAY_PREFERENCE_LOADER.getFloat("elementDistancingCustomizableSigns"),
                CustomizableSignBlockEntityRenderer.elementDistancingRenderLayerDefault
        );

        /* LANGUAGE PREFERENCES LOADED INSIDE MinecraftClientMixin.java! */
    }

}
