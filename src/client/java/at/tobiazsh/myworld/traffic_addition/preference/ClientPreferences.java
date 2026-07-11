package at.tobiazsh.myworld.traffic_addition.preference;

import at.tobiazsh.myworld.traffic_addition.toml.TomlFloat;
import at.tobiazsh.myworld.traffic_addition.toml.TomlInteger;
import at.tobiazsh.myworld.traffic_addition.toml.TomlString;

public class ClientPreferences implements PreferenceHierarchy {

    // DO NOT BLINDLY CHANGE OBJECT NAMES! It's essential for TOML!
    public final CustomizableSigns customizableSigns = new CustomizableSigns();
    public final Signs signs = new Signs();
    public final Rendering rendering = new Rendering();
    public final General general = new General();

    public static class CustomizableSigns {
        private CustomizableSigns() {}

        public final Preference<TomlFloat> elementDistancing = new Preference<>(new TomlFloat(0.75f), "view_distance");
        public final Preference<TomlFloat> viewDistance = new Preference<>(new TomlFloat(3f), "element_distancing");
    }

    public static class Signs {
        private Signs() {}

        public final Preference<TomlFloat> viewDistance = new Preference<>(new TomlFloat(3f), "view_distance");
    }

    public static class Rendering {
        private Rendering() {}

        public final Preference<TomlInteger> imageRenderLayerCacheSize = new Preference<>(new TomlInteger(200), "image_render_layer_cache_size");
        public final Preference<TomlInteger> textRenderLayerCacheSize = new Preference<>(new TomlInteger(100), "text_render_layer_cache_size");
        public final Preference<TomlInteger> calculationCacheSize = new Preference<>(new TomlInteger(256), "calculation_cache_size");
    }

    public static class General {
        private General() {}

        public final Preference<TomlString> language = new Preference<>(new TomlString("auto"), "language");
    }
}
