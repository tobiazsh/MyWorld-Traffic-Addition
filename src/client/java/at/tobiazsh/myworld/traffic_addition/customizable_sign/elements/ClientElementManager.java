package at.tobiazsh.myworld.traffic_addition.customizable_sign.elements;

import at.tobiazsh.myworld.traffic_addition.block_entities.CustomizableSignBlockEntity;
import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.data.Background;
import at.tobiazsh.myworld.traffic_addition.data.CustomizableSignTextureData;
import at.tobiazsh.myworld.traffic_addition.network.CustomClientNetworking;
import at.tobiazsh.myworld.traffic_addition.sign.elements.BaseElementInterface;
import at.tobiazsh.myworld.traffic_addition.utils.BorderProperty;
import at.tobiazsh.myworld.traffic_addition.utils.math.BlockPosExtended;
import com.google.gson.JsonObject;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import java.lang.Math;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Class used by the UI to retrieve the sign's data
 */
public class ClientElementManager {

    private static final ClientElementManager INSTANCE = new ClientElementManager();
    public static ClientElementManager getInstance()  {
        return INSTANCE;
    }

    private final Map<UUID, ClientElementInterface> elementIds = new HashMap<>(); // Map of elements currently rendered anywhere

    public boolean idExists(UUID id) {
        return elementIds.containsKey(id);
    }

    private final List<ClientElementInterface> elements = new CopyOnWriteArrayList<>();
    private float pixelOfOneBlock = 1.0f; // Current scale factor for elements, used for rendering
    private BorderProperty[][] borders; // Stores the borders from the whole sign in a 2D array

    public CustomizableSignTextureData textureData = new CustomizableSignTextureData(Background.TRANSPARENT, new ArrayList<>());

    public void registerElement(ClientElementInterface element) {

        UUID elementId = element.getId() != null ? element.getId() : UUID.randomUUID();

        while (idExists(elementId)) { // ASTRONOMICALLY unlikely, but just in case
            elementId = UUID.randomUUID(); // Ensure the ID is unique
        }

        registerElement(element, elementId); // Register the element with a unique ID
    }

    public void registerElement(ClientElementInterface element, UUID id) {
        if (element == null) {
            MyWorldTrafficAddition.LOGGER.error("Tried to register a null element! This should never happen!");
            return; // Prevent registering null elements
        }

        if (id == null) {
            MyWorldTrafficAddition.LOGGER.error("Tried to register an element with a null ID!");
            return; // Prevent registering elements with invalid IDs
        }

        elementIds.put(id, element);
        element.setId(id);
    }

    public void unregisterElement(ClientElementInterface element) {
        if (element.getId() == null) return;
        UUID uuid = element.getId();
        elementIds.remove(uuid);
        element.setId(null); // Clear the ID from the element
    }

    public void recursiveRegisterElement(ClientElementInterface element) {
        registerElement(element);
        if (element instanceof GroupElementClient groupElement) {
            groupElement.getClientElements().forEach(this::recursiveRegisterElement);
        }
    }

    public ClientElementInterface getElementById(UUID id) {
        return elementIds.get(id);
    }

    // ----- Element List Operations ----------------------------------------------------------------------------------------

    public void addElement(ClientElementInterface element) {
        elements.add(element);
        registerElement(element); // Register the element with a unique ID
        element.setFactor(pixelOfOneBlock); // Set the current scale factor for the element
        element.setParentId(BaseElementInterface.MAIN_CANVAS_ID);
        registerUnregistered();
    }

    public void addElement(int index, ClientElementInterface element) {
        elements.add(index, element);
        registerElement(element); // Register the element with a unique ID
        element.setFactor(pixelOfOneBlock); // Set the current scale factor for the element
        element.setParentId(BaseElementInterface.MAIN_CANVAS_ID);
        registerUnregistered();
    }

    public void addElementFirst(ClientElementInterface element) {
        elements.addFirst(element);
        registerElement(element); // Register the element with a unique ID
        element.setFactor(pixelOfOneBlock); // Set the current scale factor for the element
        element.setParentId(BaseElementInterface.MAIN_CANVAS_ID);
        registerUnregistered();
    }

    public void addAllElements(int index, List<ClientElementInterface> elements) {
        this.elements.addAll(index, elements);
        elements.forEach(this::registerElement); // Register all elements with unique IDs
        elements.forEach(element -> element.setFactor(pixelOfOneBlock)); // Set the current scale factor for all elements
        elements.forEach(element -> element.setParentId(BaseElementInterface.MAIN_CANVAS_ID)); // Set the parent ID for all elements
        registerUnregistered();
    }

    public void addAllElements(List<ClientElementInterface> elements) {
        this.elements.addAll(elements);
        elements.forEach(this::registerElement); // Register all elements with unique IDs
        elements.forEach(element -> element.setFactor(pixelOfOneBlock)); // Set the current scale factor for all elements
        elements.forEach(element -> element.setParentId(BaseElementInterface.MAIN_CANVAS_ID)); // Set the parent ID for all elements
        registerUnregistered();
    }

    public void removeElement(ClientElementInterface element) {

        /*
            Removes element by index, because otherwise, if it first gets unregistered, the id is modified (to null) and then it isn't the same as in the list anymore.
            Hence, it would not be found in the list and the removal would fail and throw an NullPointerException.
            This is why we first find the index of the element and then remove it by index, because the index is still valid.
         */

        int index = elements.indexOf(element);
        if (index == -1) {
            MyWorldTrafficAddition.LOGGER.warn("Tried to remove an element that is not in the list: {}", element);
            return;
        }

        this.removeElement(index); // Remove the element by index
    }

    public void removeElement(int index) {
        ClientElementInterface element = elements.get(index);

        unregisterElement(element); // IMPORTANT! First unregister because the element id will be set to null. If first removed, it'll fail to set the id to null and hence crash with a NullPointerException

        if (element instanceof TexturableElementInterface) {
            ((TexturableElementInterface) element).markTextureStale();
        }

        elements.remove(index);
        registerUnregistered();
    }

    public int indexOfElement(ClientElementInterface element) {
        return elements.indexOf(element);
    }

    public int totalElements() {
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
        updateFactor(); // Update the factor of all elements to the current scale factor
        registerUnregistered();
    }

    /**
     * Reads from the sign block entity and imports the background textures and elements from the sign.
     */
    public void importFromSign(CustomizableSignBlockEntity blockEntity) {
        elements.clear();
        elementIds.clear();

        if (!(blockEntity instanceof CustomizableSignBlockEntity)) return; // No BlockEntity found, nothing to import

        CustomizableSignTextureData otherSignTextureData = blockEntity.getTextureData();
        if (otherSignTextureData == null) return; // No JSON found, nothing to import

        borders = calculateBorders(blockEntity.getSignPositionsRelative(), blockEntity, blockEntity.getWidth(), blockEntity.getHeight());

        setData(otherSignTextureData, blockEntity); // Set the data from the sign block entity
    }

    public void setData(CustomizableSignTextureData data, CustomizableSignBlockEntity blockEntity) {
        if (!(blockEntity instanceof CustomizableSignBlockEntity)) return; // No BlockEntity found, nothing to import

        List<ClientElementInterface> elements = data.getElementContainer().getElements().stream().map(CustomizableSignElementFactory::toClientElement).toList();
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
     * Sends the current data to the server to update the block entities data.
     * @param pos Position of the block entity
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
            throw new IllegalStateException("Cannot export to sign: Current JSON is empty! It seems like nothing has been edited!");
        }

        JsonObject blockEntityPosition = new JsonObject();
        blockEntityPosition.addProperty("x", pos.getX());
        blockEntityPosition.addProperty("y", pos.getY());
        blockEntityPosition.addProperty("z", pos.getZ());

        JsonObject constructedJson = new JsonObject();
        constructedJson.add("blockEntityPosition", blockEntityPosition);
        constructedJson.add("texture", textureData.toJson());

        String jsonString = constructedJson.toString();

        CustomClientNetworking.getInstance().sendStringToServer(Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, "set_customizable_sign_texture"), jsonString);
    }

    public void updateFactor() {
        elements.forEach(element -> element.setFactor(pixelOfOneBlock));
    }

    public float getPixelOfOneBlock() {
        return pixelOfOneBlock;
    }

    public void setPixelOfOneBlock(float pixelOfOneBlock) {
        this.pixelOfOneBlock = pixelOfOneBlock;
        updateFactor(); // Update the factor of all elements when the scale factor changes
    }

    public void clearAll() {
        getElements().forEach(e -> {
            try {
                e.dispose();
            } catch (Exception ex) {
                MyWorldTrafficAddition.LOGGER.error("Error disposing element {}: {}", e, ex.getMessage());
            }
        });

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

        // Sort from Y highest to lowest X/Z lowest to highest, because ImGui renders top -> bottom, left -> right
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
