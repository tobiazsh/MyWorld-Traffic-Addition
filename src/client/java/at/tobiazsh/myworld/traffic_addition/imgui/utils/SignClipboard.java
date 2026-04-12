package at.tobiazsh.myworld.traffic_addition.imgui.utils;

import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.ClientElementInterface;
import at.tobiazsh.myworld.traffic_addition.data.CustomizableSignTextureData;

import java.util.ArrayList;
import java.util.List;

public class SignClipboard {

    private static final SignClipboard INSTANCE = new SignClipboard();

    public static SignClipboard getInstance() {
        return INSTANCE;
    }

    private ClientElementInterface copiedElement = null;
    private CustomizableSignTextureData copiedSign = null;
    private final List<CustomizableSignTextureData> undoStack = new ArrayList<>();
    private final List<CustomizableSignTextureData> redoStack = new ArrayList<>();

    public void setCopiedSign(CustomizableSignTextureData sign) {
        copiedSign = sign;
    }

    public CustomizableSignTextureData getCopiedSign() {
        return copiedSign;
    }

    public void setCopiedElement(ClientElementInterface element) {
        copiedElement = element;
    }

    public ClientElementInterface getCopiedElement() {
        if (copiedElement == null) return null;
        return copiedElement.copy();
    }

    public void pushUndoStack(CustomizableSignTextureData sign) {
        if (undoStack.size() > 50) undoStack.removeFirst();
        undoStack.add(sign);
    }

    public void pushRedoStack(CustomizableSignTextureData sign) {
        if (redoStack.size() > 50) redoStack.removeFirst();
        redoStack.add(sign);
    }

    public CustomizableSignTextureData popUndoStack() {
        if (undoStack.isEmpty()) return null;
        return undoStack.removeLast();
    }

    public CustomizableSignTextureData popRedoStack() {
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