package at.tobiazsh.myworld.traffic_addition.imgui.fonts;

import at.tobiazsh.myworld.traffic_addition.font.FontDefinition;

import java.io.IOException;
import java.io.InputStream;

/**
 * A font definition that loads the font from the filesystem (JAR).
 */
public class JarFontDefinition implements FontDefinition {

    private final String jarPath;
    private final String displayName;
    private final String json;

    public JarFontDefinition(String jarPath, String displayName, String json) {
        this.jarPath = jarPath;
        this.displayName = displayName;
        this.json = json;
    }

    @Override
    public String getId() {
        return jarPath;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }
}
