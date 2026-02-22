package at.tobiazsh.myworld.traffic_addition.debug;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.gui.NativeFileDialogs;
import at.tobiazsh.myworld.traffic_addition.texture.sign.BackgroundLoader;

public class DebugFunctions {

    public static void testNfd_open() {
        String path = NativeFileDialogs.open(
                            "[DEBUG] Select an image",
                            new NativeFileDialogs.FilterItem("Images", new String[]{"*.png", "*.jpg", "*.jpeg", "*.bmp", "*.gif"}),
                            "C:/",
                            MyWorldTrafficAddition.LOGGER::info);

        MyWorldTrafficAddition.LOGGER.info("Selected path: {}", path);
    }

    public static void testNfd_save() {
        String path = NativeFileDialogs.save(
                            "[DEBUG] Save an image",
                            new NativeFileDialogs.FilterItem("Images", new String[]{"*.png", "*.jpg", "*.jpeg", "*.bmp", "*.gif"}),
                            "C:/",
                            "image.png",
                            MyWorldTrafficAddition.LOGGER::info);

        MyWorldTrafficAddition.LOGGER.info("Selected path: {}", path);
    }

    public static void testAutoBackgroundLoad() {
        MyWorldTrafficAddition.LOGGER.info("Testing auto background load...");

        try {
            BackgroundLoader.autoload();
        } catch (Exception e) {
            MyWorldTrafficAddition.LOGGER.error("An error occurred during sprite atlas autoload: ", e);
        }

        MyWorldTrafficAddition.LOGGER.info("Auto background load finished. Loaded backgrounds:");
        BackgroundLoader.BACKGROUND_SPRITES.forEach(System.out::println);
        // TODO: Texture on sprite atlas is not correct yet. currently: mwta:/assets/textures/backgrounds/....   expected: mwta:textures/backgrounds
    }
}
