package at.tobiazsh.myworld.traffic_addition.sign.elements.event;

import at.tobiazsh.myworld.traffic_addition.event.EventHandler;
import at.tobiazsh.myworld.traffic_addition.event.EventListener;

public class BaseElementEvents {

    public final EventHandler<EventListener> geometryHandler = new EventHandler<>();

    /**
     * Anonymizes all classes. See {@link EventHandler#anonymize()} for more information about anonymizations.
     */
    public void anonymizeAll() {
        geometryHandler.invoke();
    }

}
