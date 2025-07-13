package at.tobiazsh.myworld.traffic_addition.utils;

import at.tobiazsh.myworld.traffic_addition.language.JenguaTranslator;
import at.tobiazsh.myworld.traffic_addition.rendering.renderers.CustomizableSignBlockEntityRenderer;
import at.tobiazsh.myworld.traffic_addition.rendering.renderers.SignBlockEntityRenderer;
import at.tobiazsh.myworld.traffic_addition.utils.preferences.Preference;

import java.util.Objects;

public class ClientPreferences {

    public static final Preference gameplayPreference = new Preference("myworld_traffic_addition/gameplay_config.json");

    public static void loadGameplayPreferences() {
        // SIGNS
        SignBlockEntityRenderer.zOffsetRenderLayer = Objects.requireNonNullElse(
                gameplayPreference.getFloat("viewDistanceSigns"),
                SignBlockEntityRenderer.zOffsetRenderLayerDefault
        );

        // CUSTOMIZABLE SIGNS
        CustomizableSignBlockEntityRenderer.zOffsetRenderLayer = Objects.requireNonNullElse(
                gameplayPreference.getFloat("viewDistanceCustomizableSigns"),
                CustomizableSignBlockEntityRenderer.zOffsetRenderLayerDefault
        );

        CustomizableSignBlockEntityRenderer.elementDistancingRenderLayer = Objects.requireNonNullElse(
                gameplayPreference.getFloat("elementDistancingCustomizableSigns"),
                CustomizableSignBlockEntityRenderer.elementDistancingRenderLayerDefault
        );

        /* LANGUAGE PREFERENCES LOADED INSIDE MinecraftClientMixin.java! */
    }

}
