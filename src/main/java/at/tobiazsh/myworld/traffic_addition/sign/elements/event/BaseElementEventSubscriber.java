package at.tobiazsh.myworld.traffic_addition.sign.elements.event;

import at.tobiazsh.myworld.traffic_addition.event.EventListener;

public interface BaseElementEventSubscriber {
    AutoCloseable subscribeGeometryChanged(EventListener onGeometryChanged);
}
