package at.tobiazsh.myworld.traffic_addition.ImGui.Utils;

import at.tobiazsh.myworld.traffic_addition.Utils.CustomizableSignData;
import at.tobiazsh.myworld.traffic_addition.Utils.Elements.BaseElement;

import java.util.ArrayList;
import java.util.List;

public class Clipboard {
    private static BaseElement copiedElement = null;
    private static CustomizableSignData copiedSign = null;
    private static final List<CustomizableSignData> undoStack = new ArrayList<>();
    private static final List<CustomizableSignData> redoStack = new ArrayList<>();

    public static void setCopiedSign(CustomizableSignData sign) {
        copiedSign = sign;
    }

    public static CustomizableSignData getCopiedSign() {
        return copiedSign;
    }

    public static void setCopiedElement(BaseElement element) {
        copiedElement = element;
    }

    public static BaseElement getCopiedElement() {
        if (copiedElement == null) return null;
        return copiedElement.copy();
    }

    public static void pushUndoStack(CustomizableSignData sign) {
        if (undoStack.size() > 50) undoStack.removeFirst();
        undoStack.add(sign);
    }

    public static void pushRedoStack(CustomizableSignData sign) {
        if (redoStack.size() > 50) undoStack.removeFirst();
        redoStack.add(sign);
    }

    public static CustomizableSignData popUndoStack() {
        if (undoStack.isEmpty()) return null;
        return undoStack.removeLast();
    }

    public static CustomizableSignData popRedoStack() {
        if (redoStack.isEmpty()) return null;
        return redoStack.removeLast();
    }

    public static boolean redoEmpty() {
        return redoStack.isEmpty();
    }

    public static boolean undoEmpty() {
        return undoStack.isEmpty();
    }

    public static void clearUndoStack() {
        undoStack.clear();
    }

    public static void clearRedoStack() {
        redoStack.clear();
    }
}