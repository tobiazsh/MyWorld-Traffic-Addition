package at.tobiazsh.myworld.traffic_addition.ImGui.Utils;

import at.tobiazsh.myworld.traffic_addition.Utils.CustomizableSignStyle;
import at.tobiazsh.myworld.traffic_addition.Utils.Elements.BaseElement;

public class Clipboard {
    private static BaseElement copiedElement = null;
    private static CustomizableSignStyle copiedSign = null;

    public static void setCopiedSign(CustomizableSignStyle sign) {
        copiedSign = sign;
    }

    public static CustomizableSignStyle getCopiedSign() {
        return copiedSign;
    }

    public static void setCopiedElement(BaseElement element) {
        copiedElement = element;
    }

    public static BaseElement getCopiedElement() {
        return copiedElement.copy();
    }
}
