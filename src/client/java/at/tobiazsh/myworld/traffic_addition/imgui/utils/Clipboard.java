package at.tobiazsh.myworld.traffic_addition.imgui.utils;

import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.ClientElementInterface;
import at.tobiazsh.myworld.traffic_addition.utils.CustomizableSignData;

import java.util.ArrayList;
import java.util.List;

public class Clipboard {

    private static final Clipboard INSTANCE = new Clipboard();

    public static Clipboard getInstance() {
        return INSTANCE;
    }

    private ClientElementInterface copiedElement = null;
    private CustomizableSignData copiedSign = null;
    private final List<CustomizableSignData> undoStack = new ArrayList<>();
    private final List<CustomizableSignData> redoStack = new ArrayList<>();

    public void setCopiedSign(CustomizableSignData sign) {
        copiedSign = sign;
    }

    public CustomizableSignData getCopiedSign() {
        return copiedSign;
    }

    public void setCopiedElement(ClientElementInterface element) {
        copiedElement = element;
    }

    public ClientElementInterface getCopiedElement() {
        if (copiedElement == null) return null;
        return copiedElement.copy();
    }

    public void pushUndoStack(CustomizableSignData sign) {
        if (undoStack.size() > 50) undoStack.removeFirst();
        undoStack.add(sign);
    }

    public void pushRedoStack(CustomizableSignData sign) {
        if (redoStack.size() > 50) undoStack.removeFirst();
        redoStack.add(sign);
    }

    public CustomizableSignData popUndoStack() {
        if (undoStack.isEmpty()) return null;
        return undoStack.removeLast();
    }

    public CustomizableSignData popRedoStack() {
        if (redoStack.isEmpty()) return null;
        return redoStack.removeLast();
    }

    public boolean redoEmpty() {
        return redoStack.isEmpty();
    }

    public boolean undoEmpty() {
        return undoStack.isEmpty();
    }

    public void clearUndoStack() {
        undoStack.clear();
    }

    public void clearRedoStack() {
        redoStack.clear();
    }
}