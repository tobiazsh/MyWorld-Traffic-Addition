package at.tobiazsh.myworld.traffic_addition.imgui.fonts;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import dev.tobiazsh.imguib3d.client.font.FontIdentifier;
import dev.tobiazsh.imguib3d.client.font.FontImportance;
import dev.tobiazsh.imguib3d.client.font.ImGuiFont;
import org.jspecify.annotations.Nullable;

import java.io.InputStream;

public class DefaultFonts {

    public static @Nullable ImGuiFont RobotoNormal;
    public static @Nullable ImGuiFont RobotoBold;
    public static @Nullable ImGuiFont RobotoBoldMedium;
    public static @Nullable ImGuiFont RobotoBoldLarge;

    public static final int NORMAL_SIZE = 20;
    public static final int MEDIUM_SIZE = 30;
    public static final int LARGE_SIZE  = 40;

    public static void registerAll() {
        InputStream isR = DefaultFonts.class.getResourceAsStream("/assets/" + MyWorldTrafficAddition.MOD_ID + "/fonts/roboto_regular.ttf");
        InputStream isRB = DefaultFonts.class.getResourceAsStream("/assets/" + MyWorldTrafficAddition.MOD_ID + "/fonts/roboto_bold.ttf");

        ImGuiFont.loadFromStreamTTF(
                isR,
                FontIdentifier.of(MyWorldTrafficAddition.MOD_ID, "Roboto Regular"),
                NORMAL_SIZE,
                FontImportance.HIGH
        ).thenAccept(font -> RobotoNormal = font);

        ImGuiFont.loadFromStreamTTF(
                isRB,
                FontIdentifier.of(MyWorldTrafficAddition.MOD_ID, "Roboto Bold"),
                NORMAL_SIZE,
                FontImportance.HIGH
        ).thenAccept(font -> RobotoBold = font);

        ImGuiFont.loadFromStreamTTF(
                isRB,
                FontIdentifier.of(MyWorldTrafficAddition.MOD_ID, "Roboto Bold Medium"),
                MEDIUM_SIZE,
                FontImportance.HIGH
        ).thenAccept(font -> RobotoBoldMedium = font);

        ImGuiFont.loadFromStreamTTF(
                isRB,
                FontIdentifier.of(MyWorldTrafficAddition.MOD_ID, "Roboto Bold Large"),
                LARGE_SIZE,
                FontImportance.HIGH
        ).thenAccept(font -> RobotoBoldLarge = font);
    }

}
