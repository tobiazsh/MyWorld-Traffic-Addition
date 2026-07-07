package at.tobiazsh.myworld.traffic_addition.preference;

import at.tobiazsh.myworld.traffic_addition.toml.TomlBoolean;
import at.tobiazsh.myworld.traffic_addition.toml.TomlInteger;
import at.tobiazsh.myworld.traffic_addition.toml.TomlLong;
import at.tobiazsh.myworld.traffic_addition.toml.TomlShort;

public class ServerPreferences implements PreferenceHierarchy {

    // DO NOT BLINDLY CHANGE OBJECT NAMES! It's essential for TOML!
    public final CustomizableSigns customizableSigns = new CustomizableSigns();

    // These classes are private
    public static class CustomizableSigns {
        private CustomizableSigns() {}

        // DO NOT BLINDLY CHANGE OBJECT NAMES! It's essential for TOML!
        public final OnlineImages onlineImages = new OnlineImages();
        public final General general = new General();

        public static class OnlineImages {
            private OnlineImages() {}

            public final Preference<TomlLong> maxSize =                new Preference<>(new TomlLong(5_242_880L), "max_size"); // 5 MiB
            public final Preference<TomlLong> maxThumbnailSize =       new Preference<>(new TomlLong(524_288L), "max_thumbnail_size"); // 512 KiB
            public final Preference<TomlLong> maxMetadataSize =        new Preference<>(new TomlLong(12_800L), "max_metadata_size"); // 100 KiB
            public final Preference<TomlBoolean> uploadEnabled =       new Preference<>(new TomlBoolean(true), "upload_enabled");
            public final Preference<TomlBoolean> hasLimit =            new Preference<>(new TomlBoolean(false), "has_limit");
            public final Preference<TomlInteger> maxUploadsPerPlayer = new Preference<>(new TomlInteger(10), "max_uploads_per_player");
            public final Preference<TomlLong> downloadTimeout =        new Preference<>(new TomlLong(15_000L), "download_timeout");
        }

        public static class General {
            private General() {}

            public final Preference<TomlShort> maxWidth =     new Preference<>(new TomlShort((short) 60), "max_width");
            public final Preference<TomlShort> maxHeight =    new Preference<>(new TomlShort((short) 60), "max_height");

            public final Preference<TomlShort> maxElements =  new Preference<>(new TomlShort((short) 30), "max_elements");
        }
    }
}
