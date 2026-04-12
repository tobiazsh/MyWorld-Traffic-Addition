package at.tobiazsh.myworld.traffic_addition.debug;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.data.CustomizableSignTextureData;
import at.tobiazsh.myworld.traffic_addition.gui.NativeFileDialogs;
import at.tobiazsh.myworld.traffic_addition.texture.sign.BackgroundLoader;
import at.tobiazsh.myworld.traffic_addition.utils.JsonUtil;
import com.google.gson.JsonParser;

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
    }

    public static void testNewDataParse() {
        String oldData = """
                {
                  "Elements": [
                    {
                      "Color": [
                        1.0,
                        1.0,
                        1.0,
                        1.0
                      ],
                      "Size": [
                        165.0,
                        263.42096
                      ],
                      "ElementPosition": [
                        631.0,
                        224.0
                      ],
                      "Id": "60dca7cb-bf66-42ef-b7ae-664ec168925a",
                      "Name": "Tab Inconsistency",
                      "Rotation": 0.0,
                      "Factor": 316.66666,
                      "ParentId": "00000000-0000-0000-0000-000000000000",
                      "ElementType": 4,
                      "PictureReference": "675242be-611b-424a-afbd-8ef4945920e5"
                    },
                    {
                      "Color": [
                        0.943038,
                        0.17905782,
                        0.17905782,
                        1.0
                      ],
                      "Size": [
                        487.0,
                        86.57778
                      ],
                      "ElementPosition": [
                        231.5,
                        69.0
                      ],
                      "Id": "8452636f-cb61-43a0-aa2b-718fa9b4e972",
                      "Name": "New Element",
                      "Rotation": 0.0,
                      "Factor": 316.66666,
                      "ParentId": "00000000-0000-0000-0000-000000000000",
                      "ElementType": 2,
                      "Text": "Text Element",
                      "FontPath": "/assets/myworld_traffic_addition/font/dejavu_sans_bold.ttf",
                      "FontSize": 24.0
                    },
                    {
                      "Color": [
                        1.0,
                        1.0,
                        1.0,
                        1.0
                      ],
                      "Size": [
                        268.0,
                        268.0
                      ],
                      "ElementPosition": [
                        341.0,
                        182.66666
                      ],
                      "Id": "19b27e79-91c4-4e0e-a460-f2456f27091f",
                      "Name": "New Element",
                      "Rotation": 0.0,
                      "Factor": 316.66666,
                      "ParentId": "00000000-0000-0000-0000-000000000000",
                      "ElementType": 1,
                      "Texture": "/assets/myworld_traffic_addition/textures/imgui/sign_res/icons/other/arrow.png"
                    }
                  ],
                  "Style": "/assets/myworld_traffic_addition/textures/imgui/sign_res/backgrounds/austria/normal"
                }
                """;

        CustomizableSignTextureData data = CustomizableSignTextureData.fromJson(JsonParser.parseString(oldData).getAsJsonObject());
        MyWorldTrafficAddition.LOGGER.info("DEBUG TEST:");
        MyWorldTrafficAddition.LOGGER.info(JsonUtil.toPrettyJson(data.toJson().toString()));
    }
}
