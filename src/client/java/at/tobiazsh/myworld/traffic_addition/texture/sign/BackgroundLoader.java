package at.tobiazsh.myworld.traffic_addition.texture.sign;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.filesystem.FileSystem;
import at.tobiazsh.myworld.traffic_addition.texture.SpriteAtlas;
import at.tobiazsh.myworld.traffic_addition.texture.SpriteAtlasManager;
import org.apache.commons.lang3.NotImplementedException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.CopyOnWriteArrayList;

public class BackgroundLoader {

    private BackgroundLoader() {}

    public static final FileSystem.Folder AUTOLOAD_DIRECTORY;

    static {
        try {
            AUTOLOAD_DIRECTORY = FileSystem.listFilesRecursive(
                    String.format("/assets/%s/textures/background/autoload", MyWorldTrafficAddition.MOD_ID),
                    true
            );
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static final CopyOnWriteArrayList<SpriteAtlas> BACKGROUND_SPRITES = new CopyOnWriteArrayList<>();

    /**
     * Automatically loads all background sprite atlases from the autoload folder.
     * @throws IOException If some error occurred during file read.
     * @throws IllegalArgumentException If an error occurred while parsing the sprite atlas parsing
     * @throws NotImplementedException If the resource is outside the JAR, because this is not available yet.
     * @throws NullPointerException If some error occurred during texture initialization (e.g. picture not found)
     */
    public static void autoload() throws IOException, IllegalArgumentException, NotImplementedException, NullPointerException {
        for (FileSystem.DirectoryElement direlm : AUTOLOAD_DIRECTORY.content) {
            if (direlm.isFolder()) continue; // Do not count folders, although non should exist anyway by now

            SpriteAtlas atlas = SpriteAtlasManager.INSTANCE.loadSpriteAtlas((FileSystem.File) direlm);

            BACKGROUND_SPRITES.add(atlas);
        }
    }

}
