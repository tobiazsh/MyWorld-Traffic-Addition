package at.tobiazsh.myworld.traffic_addition.debug;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.gui.NativeFileDialogs;

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
}
