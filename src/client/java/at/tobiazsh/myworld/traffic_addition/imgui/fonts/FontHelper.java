package at.tobiazsh.myworld.traffic_addition.imgui.fonts;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import dev.tobiazsh.imguib3d.client.font.FontIdentifier;
import dev.tobiazsh.imguib3d.client.font.FontImportance;
import dev.tobiazsh.imguib3d.client.font.ImGuiFont;
import imgui.ImGui;
import org.jspecify.annotations.Nullable;

import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

public class FontHelper {
    private static @Nullable ImGuiFont RobotoNormal;
    private static @Nullable ImGuiFont RobotoBold;
    private static @Nullable ImGuiFont RobotoBoldMedium;
    private static @Nullable ImGuiFont RobotoBoldLarge;

    public static final int NORMAL_SIZE = 20;
    public static final int MEDIUM_SIZE = 30;
    public static final int LARGE_SIZE  = 40;

    public static void registerAll() {
        InputStream isR = FontHelper.class.getResourceAsStream("/assets/" + MyWorldTrafficAddition.MOD_ID + "/fonts/roboto_regular.ttf");
        InputStream isRB = FontHelper.class.getResourceAsStream("/assets/" + MyWorldTrafficAddition.MOD_ID + "/fonts/roboto_bold.ttf");

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

    /**
     * Checks if all fonts are loaded and usable.
     *
     * <p>We can just check if all are usable at once since they will be uploaded on the same frame due to
     * {@link FontImportance#HIGH} anyway. If one is usable, all are usable.
     */
    public static boolean allLoaded() {
        return RobotoNormal != null     && RobotoNormal.isLoaded()     &&
               RobotoBold != null       && RobotoBold.isLoaded()       &&
               RobotoBoldMedium != null && RobotoBoldMedium.isLoaded() &&
               RobotoBoldLarge != null  && RobotoBoldLarge.isLoaded();
    }

    public static ImGuiFont getRobotoNormal() {
        if (RobotoNormal == null) throw new IllegalStateException("Roboto Normal font is not loaded yet!");
        return RobotoNormal;
    }

    public static ImGuiFont getRobotoBold() {
        if (RobotoBold == null) throw new IllegalStateException("Roboto Bold font is not loaded yet!");
        return RobotoBold;
    }

    public static ImGuiFont getRobotoBoldMedium() {
        if (RobotoBoldMedium == null) throw new IllegalStateException("Roboto Bold Medium font is not loaded yet!");
        return RobotoBoldMedium;
    }

    public static ImGuiFont getRobotoBoldLarge() {
        if (RobotoBoldLarge == null) throw new IllegalStateException("Roboto Bold Large font is not loaded yet!");
        return RobotoBoldLarge;
    }

    public static void pushRobotoBold(AtomicBoolean pushed) {
        if (allLoaded()) {
            ImGui.pushFont(getRobotoBold().getImFont(), NORMAL_SIZE);
            pushed.set(true);
        }
    }

    public static void popRobotoBold(AtomicBoolean pushed) {
        if (pushed.get()) {
            ImGui.popFont();
            pushed.set(false);
        }
    }
}