package at.tobiazsh.myworld.traffic_addition.ImGui.Utils;

import at.tobiazsh.myworld.traffic_addition.Utils.CustomizableSignStyle;
import at.tobiazsh.myworld.traffic_addition.Utils.Elements.BaseElement;

import java.util.ArrayList;
import java.util.List;

public class Clipboard {
    private static BaseElement copiedElement = null;
    private static CustomizableSignStyle copiedSign = null;
    private static final List<CustomizableSignStyle> undoStack = new ArrayList<>();
    private static final List<CustomizableSignStyle> redoStack = new ArrayList<>();

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

    public static void pushUndoStack(CustomizableSignStyle sign) {
        if (undoStack.size() > 50) undoStack.removeFirst();
        undoStack.add(sign);
    }

    public static void pushRedoStack(CustomizableSignStyle sign) {
        if (redoStack.size() > 50) undoStack.removeFirst();
        redoStack.add(sign);
    }

    public static CustomizableSignStyle popUndoStack() {
        if (undoStack.isEmpty()) return null;
        return undoStack.removeLast();
    }

    public static CustomizableSignStyle popRedoStack() {
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