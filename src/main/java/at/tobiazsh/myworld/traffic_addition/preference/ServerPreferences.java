package at.tobiazsh.myworld.traffic_addition.preference;

import at.tobiazsh.myworld.traffic_addition.toml.serialization.annotation.TomlChild;
import at.tobiazsh.myworld.traffic_addition.toml.serialization.annotation.TomlRoot;
import at.tobiazsh.myworld.traffic_addition.toml.codec.Codecs;

@TomlRoot
public class ServerPreferences implements PreferenceHierarchy {

    // DO NOT BLINDLY CHANGE OBJECT NAMES! It's essential for TOML!
    public final CustomizableSigns customizableSigns = new CustomizableSigns();

    // These classes are private
    @TomlChild(value = "customizable_signs")
    public static class CustomizableSigns {
        private CustomizableSigns() {}

        // DO NOT BLINDLY CHANGE OBJECT NAMES! It's essential for TOML!
        public final OnlineImages onlineImages = new OnlineImages();
        public final General general = new General();

        @TomlChild(value = "online_images")
        public static class OnlineImages {
            private OnlineImages() {}

            public final Preference<Long> maxSize =                new Preference<>(5_242_880L, "max_size", Codecs.LONG); // 5 MiB
            public final Preference<Long> maxThumbnailSize =       new Preference<>(524_288L, "max_thumbnail_size", Codecs.LONG); // 512 KiB
            public final Preference<Long> maxMetadataSize =        new Preference<>(12_800L, "max_metadata_size",Codecs.LONG); // 100 KiB
            public final Preference<Boolean> uploadEnabled =       new Preference<>(true, "upload_enabled", Codecs.BOOLEAN);
            public final Preference<Boolean> hasLimit =            new Preference<>(false, "has_limit", Codecs.BOOLEAN);
            public final Preference<Integer> maxUploadsPerPlayer = new Preference<>(10, "max_uploads_per_player", Codecs.INTEGER);
            public final Preference<Long> downloadTimeout =        new Preference<>(15_000L, "download_timeout", Codecs.LONG);
        }

        @TomlChild(value = "general")
        public static class General {
            private General() {}

            public final Preference<Short> maxWidth =     new Preference<>((short) 60, "max_width", Codecs.SHORT);
            public final Preference<Short> maxHeight =    new Preference<>((short) 60, "max_height", Codecs.SHORT);

            public final Preference<Short> maxElements =  new Preference<>((short) 30, "max_elements", Codecs.SHORT);
        }
    }
}
