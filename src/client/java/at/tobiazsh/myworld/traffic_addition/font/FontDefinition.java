package at.tobiazsh.myworld.traffic_addition.font;

import java.io.IOException;
import java.io.InputStream;

public interface FontDefinition {
    String getId();
    String getDisplayName();

    /**
     * Opens an input stream to the font data (TTF).
     */
    InputStream openStream() throws IOException;
}
