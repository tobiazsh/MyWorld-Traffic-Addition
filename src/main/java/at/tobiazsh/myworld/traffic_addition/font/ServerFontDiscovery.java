package at.tobiazsh.myworld.traffic_addition.font;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import net.fabricmc.loader.api.FabricLoader;

public final class ServerFontRepository {

    private final static String FONT_DIRECTORY = FabricLoader.getInstance()
            .getConfigDir()
            .resolve(MyWorldTrafficAddition.MOD_ID)
            .resolve("server_fonts")
            .toAbsolutePath()
            .toString();

    private ServerFontRepository() {
    }

    private static final ServerFontRepository INSTANCE = new ServerFontRepository();

    public static ServerFontRepository getInstance() {
        return INSTANCE;
    }

    public void discover() {

    }

    /**
     * Discover fonts in the server's font directory.
     */
    private void discoverDirectory() {

    }

}
