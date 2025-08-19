package at.tobiazsh.myworld.traffic_addition.imgui.main_windows;

import at.tobiazsh.myworld.traffic_addition.blocks.SignBlock;
import imgui.ImGui;

import static at.tobiazsh.myworld.traffic_addition.language.JenguaTranslator.tr;

/**
 * The window used to select the texture of the sign on a normal sign block (not customizable!)
 */
public class SignSelector {

    private static boolean shouldRender;
    private static SignBlock.SIGN_SHAPE signType;

    public static void render() {
        if (!shouldRender)
            return;

        ImGui.begin(tr("Main.SignSelector", "Sign Selector"));

        if (ImGui.button(tr("Global", "Close")))
            close();

        ImGui.end();
    }

    /**
     * Opens and initialized the sign selector window for the current sign type
     */
    public static void open(SignBlock.SIGN_SHAPE signType) {
        shouldRender = true;
        SignSelector.signType = signType;
    }

    /**
     * Closes the sign selector window but doesn't reset flags!
     */
    public static void close() {
        shouldRender = false;
    }

    /**
     * Whether the sign selector window should be rendered or not
     */
    public static boolean isShouldRender() {
        return shouldRender;
    }

    /**
     * Get the current type of sign block that the sign selector window uses
     */
    public static SignBlock.SIGN_SHAPE getSignSelectionType() {
        return SignSelector.signType;
    }
}
