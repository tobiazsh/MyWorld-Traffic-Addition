package at.tobiazsh.myworld.traffic_addition.font;

import java.io.IOException;
import java.io.InputStream;

/**
 * A font definition that loads the font from the filesystem (JAR).
 */
public class JarFontDefinition implements FontDefinition {

    private final String jarPath;
    private final String displayName;
    private final String json;
    private final DynamicTrueTypeLoader.Options options;
    private final LogicalFont logicalFont;

    public JarFontDefinition(String jarPath, String displayName, String json, LogicalFont logicalFont) {
        this.jarPath = jarPath;
        this.displayName = displayName;
        this.json = json;
        this.options = DynamicTrueTypeLoader.Options.fromJsonContent(json, "");
        this.logicalFont = logicalFont;
    }

    @Override
    public DynamicTrueTypeLoader.Options getOptions() {
        return options;
    }

    @Override
    public FontLifetime getLifetime() {
        return FontLifetime.PERMANENT; // Fonts loaded from the JAR are considered permanent
    }

    @Override
    public String getId() {
        return jarPath;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public LogicalFont getLogicalFont() {
        return logicalFont;
    }

    @Override
    public InputStream openStream() throws IOException {
        return getClass().getResourceAsStream(jarPath);
    }
}
