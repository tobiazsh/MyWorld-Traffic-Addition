package at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.event;

import java.util.ArrayList;
import java.util.List;

public class ElementListChangedEvent {
    private final List<ElementListChangedEventListener> listeners = new ArrayList<>();

    public void addListener(ElementListChangedEventListener listener) {
        listeners.add(listener);
    }

    public void notifyListeners() {
        for (ElementListChangedEventListener listener : listeners) {
            listener.onElementListChanged();
        }
    }
}
