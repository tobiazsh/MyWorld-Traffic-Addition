package at.tobiazsh.myworld.traffic_addition.Screens;

import at.tobiazsh.myworld.traffic_addition.ModVars;
import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.Components.BlockEntities.SignBlockEntity;
import at.tobiazsh.myworld.traffic_addition.Components.CustomPayloads.BlockModification.SignBlockTextureChangePayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public class SignSelectionScreen extends Screen {

    private World world;
    private BlockPos pos;
    private BlockEntity entity;
    private ModVars.SIGN_SELECTION_TYPE type;

    private static final Text TITLE = Text.translatable("screen." + MyWorldTrafficAddition.MOD_ID + ".sign_selection_screen");

    private final int margin = 10; // Set universal margin for consistency
    private int currentYPosition = margin; // Add margin
    private int scrollY = 0; // Part of scrolling feature
    private int usedHeight = 0; // Keeps track of the height of all the elements together plus the extra margins;

    private List<String> countryCodes = new ArrayList<>();

    public SignSelectionScreen(World world, BlockPos pos, PlayerEntity entity, ModVars.SIGN_SELECTION_TYPE type) {
        super(TITLE);

        this.world = world;
        this.pos = pos;
        this.entity = world.getBlockEntity(pos);
        this.type = type;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    protected void init() {
        super.init();

        drawChildren();
    }

    private void drawChildren() {
        ResourceFinder finder = new ResourceFinder("textures/sign_pngs", ".png");
        Map<Identifier, List<Resource>> id_map = finder.findAllResources(MinecraftClient.getInstance().getResourceManager());

        List<String[]> resPathPartsList = new ArrayList<>();
        id_map.forEach((id, res) -> {
            Path path = Path.of(id.getPath());
            resPathPartsList.add(path.toString().replaceAll("\\\\", "/").split("/"));
        });

        // Filter out paths based on selection type
        resPathPartsList.removeIf(array -> ModVars.getSignSelectionEnumFromString(array[3]) != this.type);

        for (String[] array : resPathPartsList) {
            if(array.length > 2) {
                String countryCode = array[2];

                if (!this.countryCodes.contains(countryCode)) {
                    this.countryCodes.add(countryCode);
                }
            } else {
                System.err.println("Error (Drawing Children): Path is corrupted! Check folder assets/myworld_traffic_addition/textures/sign_pngs !");
            }
        }

        // Filter out paths based on country code
        for (String code : countryCodes) {
            List<String[]> filteredList = new ArrayList<>();

            for (String[] array : resPathPartsList) {
                if(code.equals(array[2])) {
                    filteredList.add(array);
                }
            }

            List<String> texturePaths = new ArrayList<>();
            List<String> textureNames = new ArrayList<>();
            for (String[] array : filteredList) {
                texturePaths.add(String.join("/", array));
                textureNames.add(array[array.length - 1]);
            }

            textureNames = textureNames.stream()
                    .map(name -> name.substring(0, name.lastIndexOf(".")))
                    .collect(Collectors.toList());

            if (!countryCodes.isEmpty()) {
                newSection(code, textureNames, texturePaths);
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
    }

    // Inserts a new section for a country and draws a button for every texture in the containing folder
    private void newSection(String countryCode, List<String> texturesNames, List<String> texturePaths) {
        String countryCodeId = "screen.signs.settings.section." + MyWorldTrafficAddition.MOD_ID + "." + countryCode;

        List<String> textureNameIds = texturesNames.stream().map(name -> "screen.signs.settings.section." + MyWorldTrafficAddition.MOD_ID + ".texture." + name).collect(Collectors.toList());

        int currentLocalXPosition = margin;
        int buttonWidth = 200;
        int buttonHeight = 20;

        // Put Country Label
        TextWidget sectionText = new TextWidget(10, currentYPosition, this.width - (margin * 2), 20, Text.translatable(countryCodeId), this.textRenderer); // Create Widget
        this.addDrawableChild(sectionText); // Place Widget
        currentYPosition += 10 + 10; // Leave some space
        usedHeight += 10 + 10;

        // Draw one button for each texture in the sign_pngs folder for that specific country and shape folder
        for(int i = 0; i < textureNameIds.size(); i++) {
            String id = textureNameIds.get(i);
            String textureId = texturePaths.get(i);

            // If Line is full, reset X and begin a new line
            if((currentLocalXPosition + (margin * 2) + buttonWidth ) >= this.width) {
                currentYPosition += 5;
                currentYPosition += buttonHeight;
                usedHeight += 5 + buttonHeight;
                currentLocalXPosition = margin;
            }

            ButtonWidget button = ButtonWidget.builder(
                Text.translatable(id),
                btn -> {
                    ClientPlayNetworking.send(new SignBlockTextureChangePayload(this.pos, textureId));
                    ((SignBlockEntity)entity).setTextureId(textureId);
                })
            .dimensions(currentLocalXPosition, currentYPosition, buttonWidth, buttonHeight).tooltip(Tooltip.of(Text.of("Set selected texture"))).build();

            currentLocalXPosition += buttonWidth + 5;

            this.addDrawableChild(button);
        }

        currentYPosition += buttonHeight;
        usedHeight += buttonHeight;
        addSpacing();
    }

    private void addSpacing() {
        currentYPosition += 25;
        usedHeight += 25;
    }

    private void clearAll() {
        this.clearChildren();
        currentYPosition = margin;
    }

    // Scrolling feature
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {

        // Scroll down = decrease verticalAmount
        // Scroll up = increase verticalAmount

        int scrollFactor = 20; // Accelerates scrolling

        if (usedHeight < this.height) return true; // Prevents unnecessary scrolling
        if ((scrollY + (int)(verticalAmount * scrollFactor)) > 0) return true; // Prevents scrolling out of bounds (top)
        if ((currentYPosition + (int)(verticalAmount * scrollFactor)) < this.height) return true; // Prevents scrolling out of bounds (bottom)

        // Redraw everything with new currentYPosition. This works, because when scrolling down, currentYPosition is at a minus number and everything shifts up by that number.

        clearAll();

        scrollY += (int)(verticalAmount * scrollFactor);
        currentYPosition += scrollY;

        drawChildren();

        return true;
    }
}