package at.tobiazsh.myworld.traffic_addition.event;

import java.util.ArrayList;
import java.util.List;

public class EventHandler<L extends EventListener> {
    private boolean anonymized = false;
    private final List<L> listeners = new ArrayList<>();

    public AutoCloseable subscribe(L l) {
        if (anonymized)
            return () -> {};

        listeners.add(l);
        return () -> listeners.remove(l);
    }

    /**
     * "Anonymizes" handler; all current listeners are removed and new listeners cannot be added.
     */
    public void anonymize() {
        listeners.clear();
        anonymized = true;
    }

    public void invoke() {
        if (anonymized)
            return;

        listeners.forEach(EventListener::invoke);
    }
}
