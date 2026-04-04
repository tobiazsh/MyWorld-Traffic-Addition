package at.tobiazsh.myworld.traffic_addition.data;

import at.tobiazsh.myworld.traffic_addition.sign.elements.BaseElement;
import org.jspecify.annotations.NullMarked;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@NullMarked
public class CustomSignElementContainer implements Iterable<BaseElement> {

    private CopyOnWriteArrayList<BaseElement> elements; // CopyOnWriteArrayList to avoid ConcurrentModificationExceptions

    public CustomSignElementContainer() {
        elements = new CopyOnWriteArrayList<>();
    }

    public CustomSignElementContainer(List<BaseElement> elements) {
        this.elements = new CopyOnWriteArrayList<>(elements);
    }

    /**
     * Returns the elements of the sign. Does not only contain BaseElements, can also contain TextElements, ImageElements, etc.
     * @return Collection of elements on the sign
     */
    public List<BaseElement> getElements() {
        return elements;
    }

    /**
     * Sets the elements of the sign. Does not only contain BaseElements, can also contain TextElements, ImageElements, etc. Will delete all previous elements.
     * @param elements Collection of elements to set on the sign
     */
    public void setElements(List<BaseElement> elements) {
        this.elements = new CopyOnWriteArrayList<>(elements);
    }

    /**
     * Removes an element from the sign.
     * @param element Element to remove
     */
    public void removeElement(BaseElement element) {
        this.elements.remove(element);
    }

    /**
     * Adds an element to the sign.
     * @param element Element to add
     */
    public void addElement(BaseElement element) {
        this.elements.add(element);
    }

    /**
     * Adds an element at the beginning of the sign.
     * @param element Element to add
     */
    public void addElementFirst(BaseElement element) {
        this.elements.addFirst(element);
    }

    /**
     * Adds an element at a specific index to the sign.
     * @param index Index to add the element at
     * @param element Element to add
     */
    public void addElement(int index, BaseElement element) {
        this.elements.add(index, element);
    }

    /**
     * @return Whether the sign has no elements.
     */
    public boolean isEmpty() {
        return this.elements.isEmpty();
    }

    @Override
    public Iterator<BaseElement> iterator() {
        return new Iterator<>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < elements.size();
            }

            @Override
            public BaseElement next() {
                return elements.get(index++);
            }
        };
    }
}
