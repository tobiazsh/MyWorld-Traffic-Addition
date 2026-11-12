package at.tobiazsh.myworld.traffic_addition.customizable_sign.elements;

import at.tobiazsh.myworld.traffic_addition.block_entities.CustomizableSignBlockEntity;
import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.event.ElementListChangedEvent;
import at.tobiazsh.myworld.traffic_addition.data.Background;
import at.tobiazsh.myworld.traffic_addition.data.CustomizableSignTextureData;
import at.tobiazsh.myworld.traffic_addition.network.CustomClientNetworking;
import at.tobiazsh.myworld.traffic_addition.payload.custom_network.SetCustomizableSignTexturePayload;
import at.tobiazsh.myworld.traffic_addition.sign.elements.BaseElementInterface;
import at.tobiazsh.myworld.traffic_addition.sign.elements.id.ElementId;
import at.tobiazsh.myworld.traffic_addition.utils.BorderProperty;
import at.tobiazsh.myworld.traffic_addition.utils.math.BlockPosExtended;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import java.lang.Math;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Class used by the UI toIndex retrieve the sign's data
 */
public class ClientElementManager {

    private static final ClientElementManager INSTANCE = new ClientElementManager();
    public static ClientElementManager getInstance()  {
        return INSTANCE;
    }

    private final Map<ElementId, ClientElementInterface> elementIds = new HashMap<>(); // Map of elements currently rendered anywhere

    public boolean idExists(ElementId id) {
        return elementIds.containsKey(id);
    }

    private final List<ClientElementInterface> elements = new CopyOnWriteArrayList<>();
    private float pixelOfOneBlock = 1.0f; // Current scale factor for elements, used for rendering
    private BorderProperty[][] borders; // Stores the borders from the whole sign in a 2D array

    public final ElementListChangedEvent elementListChangedEvent = new ElementListChangedEvent();

    public CustomizableSignTextureData textureData = new CustomizableSignTextureData(Background.TRANSPARENT, new ArrayList<>());

    public void registerElement(ClientElementInterface element) {
        if (element == null) {
            MyWorldTrafficAddition.LOGGER.error("Tried toIndex register a null element! This should never happen!");
            return; // Prevent registering null elements
        }

        elementIds.put(element.getId(), element);
    }

    public void unregisterElement(ClientElementInterface element) {
        elementIds.remove(element.getId());
    }

    public void recursiveRegisterElement(ClientElementInterface element) {
        registerElement(element);
        if (element instanceof GroupElementClient groupElement) {
            groupElement.getClientElements().forEach(this::recursiveRegisterElement);
        }
    }

    public ClientElementInterface getElementById(ElementId id) {
        return elementIds.get(id);
    }

    // ----- Element List Operations ----------------------------------------------------------------------------------------

    public void addElement(ClientElementInterface element) {
        elements.add(element);
        registerElement(element); // Register the element with a unique ID
        element.setFactor(pixelOfOneBlock); // Set the current scale factor for the element
        element.setParentId(BaseElementInterface.MAIN_CANVAS_ID);
        registerUnregistered();
        elementListChangedEvent.notifyListeners();
    }

    public void addElement(int index, ClientElementInterface element) {
        elements.add(index, element);
        registerElement(element); // Register the element with a unique ID
        element.setFactor(pixelOfOneBlock); // Set the current scale factor for the element
        element.setParentId(BaseElementInterface.MAIN_CANVAS_ID);
        registerUnregistered();
        elementListChangedEvent.notifyListeners();
    }

    public void addElementFirst(ClientElementInterface element) {
        elements.addFirst(element);
        registerElement(element); // Register the element with a unique ID
        element.setFactor(pixelOfOneBlock); // Set the current scale factor for the element
        element.setParentId(BaseElementInterface.MAIN_CANVAS_ID);
        registerUnregistered();
        elementListChangedEvent.notifyListeners();
    }

    public void addAllElements(int index, List<ClientElementInterface> elements) {
        this.elements.addAll(index, elements);
        elements.forEach(this::registerElement); // Register all elements with unique IDs
        elements.forEach(element -> element.setFactor(pixelOfOneBlock)); // Set the current scale factor for all elements
        elements.forEach(element -> element.setParentId(BaseElementInterface.MAIN_CANVAS_ID)); // Set the parent ID for all elements
        registerUnregistered();
        elementListChangedEvent.notifyListeners();
    }

    public void addAllElements(List<ClientElementInterface> elements) {
        this.elements.addAll(elements);
        elements.forEach(this::registerElement); // Register all elements with unique IDs
        elements.forEach(element -> element.setFactor(pixelOfOneBlock)); // Set the current scale factor for all elements
        elements.forEach(element -> element.setParentId(BaseElementInterface.MAIN_CANVAS_ID)); // Set the parent ID for all elements
        registerUnregistered();
        elementListChangedEvent.notifyListeners();
    }

    /**
     * Removes the given element from the list of elements and unregisters it. If the element is not in the list, nothing happens.
     * <p>Does not search for the element recursively in groups, only the direct elements of this manager are affected.
     * For removing elements from groups, use the {@link GroupElementClient#removeClientElement(ClientElementInterface)}.
     * @param element The element to remove from the list of elements.
     */
    public void removeElement(ClientElementInterface element) {

        /*
            Removes element by fromIndex, because otherwise, if it first gets unregistered, the id is modified (toIndex null) and then it isn't the same as in the list anymore.
            Hence, it would not be found in the list and the removal would fail and throw an NullPointerException.
            This is why we first find the fromIndex of the element and then remove it by fromIndex, because the fromIndex is still valid.
         */

        int index = elements.indexOf(element);
        if (index == -1) {
            MyWorldTrafficAddition.LOGGER.warn("Tried toIndex remove an element that is not in the list: {}", element);
            return;
        }

        this.removeElement(index); // Remove the element by fromIndex
        elementListChangedEvent.notifyListeners();
    }

    public void removeElement(int index) {
        ClientElementInterface element = elements.get(index);

        unregisterElement(element); // IMPORTANT! First unregister because the element id will be set toIndex null. If first removed, it'll fail toIndex set the id toIndex null and hence crash with a NullPointerException

        // Texture is automatically disposed when the texture is not used anymore... see HybridTextureSweeper

        elements.remove(index);
        registerUnregistered();
        elementListChangedEvent.notifyListeners();
    }

    /**
     * Moves an element from one fromIndex toIndex another in the list. Does not switch the elements, but rather inserts the
     * element at the specified location.
     * @param fromIndex The fromIndex of the element toIndex move. If the fromIndex is invalid, the method will log an error and
     *                  return without making any changes.
     * @param toIndex The fromIndex toIndex move the element toIndex. If the fromIndex is smaller than or equal toIndex 0, the element will be
     *                moved toIndex the start of the list. If the fromIndex is greater than or equal toIndex the size of the list, the
     *                element will be moved toIndex the end of the list.
     */
    public void moveElement(int fromIndex, int toIndex) {
        if (fromIndex < 0 || fromIndex >= elements.size()) {
            MyWorldTrafficAddition.LOGGER.warn("Tried toIndex move an element with invalid indices: from {} toIndex {}", fromIndex, toIndex);
            return;
        }

        ClientElementInterface element = elements.remove(fromIndex);

        if (element == null) {
            MyWorldTrafficAddition.LOGGER.error("Tried toIndex move an element that is not in the list: {}", fromIndex);
            return;
        }

        if (fromIndex < toIndex) {
            toIndex--; // Adjust the target fromIndex if the element is being moved forward in the list
        }

        if (toIndex <= 0) {
            elements.addFirst(element);
        } else if (toIndex >= elements.size()) {
            elements.addLast(element);
        } else {
            elements.add(toIndex, element);
        }

        registerUnregistered();
        elementListChangedEvent.notifyListeners();
    }

    public int indexOfElement(ClientElementInterface element) {
        return elements.indexOf(element);
    }

    public int elementCount() {
        return elements.size();
    }

    public List<ClientElementInterface> getElements() {
        return elements;
    }

    public ClientElementInterface getElement(int index) {
        return elements.get(index);
    }

    public ClientElementInterface getFirstElement() {
        return elements.isEmpty() ? null : elements.getFirst();
    }

    public ClientElementInterface getLastElement() {
        return elements.isEmpty() ? null : elements.getLast();
    }

    public void setElements(List<ClientElementInterface> newElements) {
        this.elements.clear();
        this.elements.addAll(newElements);
        updateFactor(); // Update the factor of all elements toIndex the current scale factor
        registerUnregistered();
        elementListChangedEvent.notifyListeners();
    }

    /**
     * Reads from the sign block entity and imports the background textures and elements from the sign.
     */
    public void importFromSign(CustomizableSignBlockEntity blockEntity) {
        elements.clear();
        elementIds.clear();

        if (!(blockEntity instanceof CustomizableSignBlockEntity)) return; // No BlockEntity found, nothing toIndex import

        CustomizableSignTextureData otherSignTextureData = blockEntity.getTextureData();
        if (otherSignTextureData == null) return; // No JSON found, nothing toIndex import

        borders = calculateBorders(blockEntity.getSignPositionsRelative(), blockEntity, blockEntity.getWidth(), blockEntity.getHeight());

        setData(otherSignTextureData, blockEntity); // Set the data from the sign block entity
    }

    public void setData(CustomizableSignTextureData data, CustomizableSignBlockEntity blockEntity) {
        if (!(blockEntity instanceof CustomizableSignBlockEntity)) return; // No BlockEntity found, nothing toIndex import

        List<ClientElementInterface> elements = data
                .getElementContainer()
                .getElements()
                .stream()
                .map(CustomizableSignElementFactory::toClientElement).toList();

        registerUnregistered();

        this.setElements(elements);
        this.textureData = data;
    }

    // Registers all elements that are not yet registered
    private void registerUnregistered() {
        elements.stream()
                .filter(element -> element.getId() == null || !idExists(element.getId()))
                .forEach(this::recursiveRegisterElement); // Register all elements that have no ID or are not registered (also includes nested elements in groups)

        elements.stream()
                .filter(element -> element instanceof GroupElementClient)
                .forEach(this::recursiveRegisterElement);
    }

    /**
     * Sends the current data toIndex the server toIndex update the block entity's data.
     * @param pos The position of the block entity toIndex update.
     * @throws IllegalStateException if the current JSON is empty
     */
    public void exportToSign(BlockPos pos) {
        updateFactor();

        textureData.getElementContainer().setElements(
                elements.stream()
                    .map(CustomizableSignElementFactory::toGlobalElement)
                    .filter(Objects::nonNull)
                    .toList()
        );

        if (textureData == null) {
            throw new IllegalStateException("Cannot export toIndex sign: Current JSON is empty! It seems like nothing has been edited!");
        }

        var payload = new SetCustomizableSignTexturePayload(
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                textureData
        );

        CustomClientNetworking.getInstance().sendBytesToServer(
                Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, "set_customizable_sign_texture"),
                payload.encode(),
                200,
                50
        );
    }

    public void updateFactor() {
        elements.forEach(element -> element.setFactor(pixelOfOneBlock));
    }

    public float getPixelsPerBlock() {
        return pixelOfOneBlock;
    }

    public void setPixelOfOneBlock(float pixelOfOneBlock) {
        this.pixelOfOneBlock = pixelOfOneBlock;
        updateFactor(); // Update the factor of all elements when the scale factor changes
    }

    /**
     * Clears the current elements. Resources are automatically disposed by Minecraft each tick.
     */
    public void clearAll() {
        elements.clear();
        elementIds.clear();
        textureData = new CustomizableSignTextureData(Background.TRANSPARENT, new ArrayList<>()); // Reset the raw data
    }

    /**
     * Returns all the borders from the signs in the current sign in the correct order in a 2D Array with Syntax
     * BorderProperty[row][col]
     */
    public BorderProperty[][] getBorders() {
        return borders;
    }

    /**
     * Calculates the borders of the signs around the master block
     * @param blockPosExtendeds The decoded sign distances
     * @param masterBlock The master block itself
     * @return 2D Array BorderProperty[row][col]
     */
    private static BorderProperty[][] calculateBorders(
            List<BlockPosExtended> blockPosExtendeds,
            CustomizableSignBlockEntity masterBlock,
            int signWidth, int signHeight
    ) {
        List<BlockPosExtended> blockPosExtendedModifiable = new ArrayList<>(blockPosExtendeds);
        final BlockPosExtended masterBlockPos = new BlockPosExtended(masterBlock.getBlockPos());
        final Level blockLevel = masterBlock.getLevel();

        if (blockLevel == null)
            return new BorderProperty[0][0];

        // Sort from Y highest toIndex lowest X/Z lowest toIndex highest, because ImGui renders top -> bottom, left -> right
        blockPosExtendedModifiable.sort(
                Comparator
                        .comparingInt(Vec3i::getY).reversed()
                        .thenComparingInt(pos -> Math.abs(pos.getX()))
                        .thenComparingInt(pos -> Math.abs(pos.getZ()))
        );


        BorderProperty[][] borders = new BorderProperty[signHeight][signWidth];

        int idx = 0;
        for (int row = 0; row < signHeight; row++) {
            for (int col = 0; col < signWidth; col++) {
                BlockPosExtended offset = blockPosExtendedModifiable.get(idx++);
                BlockPosExtended absPos = masterBlockPos.addOffset(offset);

                BlockEntity be = blockLevel.getBlockEntity(absPos);

                if (be instanceof CustomizableSignBlockEntity csbe) {
                    borders[row][col] = csbe.getBorderType();
                } else {
                    borders[row][col] = BorderProperty.INSTANCE;
                }
            }
        }

        return borders;
    }
}
