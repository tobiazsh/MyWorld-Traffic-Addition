package at.tobiazsh.myworld.traffic_addition.font;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class RuntimeFontRegistry {
    private static final List<CustomTrueTypeFontLoader> LOADERS = new CopyOnWriteArrayList<>();

    private RuntimeFontRegistry() {}

    public static void register(CustomTrueTypeFontLoader loader) {
        LOADERS.add(loader);
    }

    public static boolean unregister(CustomTrueTypeFontLoader loader) {
        return LOADERS.remove(loader);
    }

    public static List<CustomTrueTypeFontLoader> getLoaders() {
        return Collections.unmodifiableList(LOADERS);
    }

    public static void clear() {
        LOADERS.clear();
    }
}
