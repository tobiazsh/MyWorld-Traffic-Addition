package at.tobiazsh.myworld.traffic_addition.clipboard;

import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.ClientElementInterface;
import org.jspecify.annotations.Nullable;

/**
 * Basic clipboard implementation for copy-pasting elements.
 */
public class ElementClipboard {
    /// Public, sign-editor-instance-independent clipboard used in standard applications.
    /// Special applications may require own clipboard instances.
    private static final ElementClipboard COMMON_CLIPBOARD = new ElementClipboard();

    private @Nullable ClientElementInterface element;

    public static ElementClipboard getCommonClipboard() {
        return COMMON_CLIPBOARD;
    }

    public void setElement(@Nullable ClientElementInterface element) {
        this.element = element;
    }

    /**
     * Returns the current element in clipboard, or null if none is present.
     *
     * @param copy Whether to return a copy of the element or the element itself.
     */
    public @Nullable ClientElementInterface getElement(boolean copy) {
        if (this.element == null)
            return null;

        if (copy)
            return this.element.copy();
        else
            return this.element;
    }

    public void clearClipboard() {
        this.element = null;
    }
}
