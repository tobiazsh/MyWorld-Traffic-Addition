package at.tobiazsh.myworld.traffic_addition.debug;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.utils.graphics.NativeFileDialogs;

public class DebugFunctions {

    public static void testNfd_open() {
        String path = NativeFileDialogs.open(
                            new NativeFileDialogs.FilterItem[]{new NativeFileDialogs.FilterItem("Images", new String[]{"png", "jpg", "jpeg", "bmp", "gif"})},
                            "C:/",
                            MyWorldTrafficAddition.LOGGER::info,
                            error -> MyWorldTrafficAddition.LOGGER.info("Error on open: {}", error.getMessage()));

        MyWorldTrafficAddition.LOGGER.info("Selected path: {}", path);
    }

    public static void testNfd_save() {
        String path = NativeFileDialogs.save(
                            new NativeFileDialogs.FilterItem[]{new NativeFileDialogs.FilterItem("Images", new String[]{"png", "jpg", "jpeg", "bmp", "gif"})},
                            "C:/",
                            "image.png",
                            MyWorldTrafficAddition.LOGGER::info,
                            error -> MyWorldTrafficAddition.LOGGER.info("Error on save: {}", error.getMessage()));

        MyWorldTrafficAddition.LOGGER.info("Selected path: {}", path);
    }
}
