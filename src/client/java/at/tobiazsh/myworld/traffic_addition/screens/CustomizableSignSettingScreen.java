package at.tobiazsh.myworld.traffic_addition.screens;

import at.tobiazsh.myworld.traffic_addition.custom_payloads.block_modification.*;
import at.tobiazsh.myworld.traffic_addition.imgui.ImGuiRenderer;
import at.tobiazsh.myworld.traffic_addition.imgui.main_windows.SignEditor;
import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.block_entities.CustomizableSignBlockEntity;
import at.tobiazsh.myworld.traffic_addition.block_entities.SignPoleBlockEntity;
import at.tobiazsh.myworld.traffic_addition.Widgets.DegreeSliderWidget;
import at.tobiazsh.myworld.traffic_addition.utils.BorderProperty;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static at.tobiazsh.myworld.traffic_addition.block_entities.CustomizableSignBlockEntity.*;
import static at.tobiazsh.myworld.traffic_addition.utils.DirectionUtils.blockPosInDirection;
import static at.tobiazsh.myworld.traffic_addition.utils.DirectionUtils.getRightSideDirection;

/**
 * Screen for customizing sign blocks
 */
@Environment(EnvType.CLIENT)
public class CustomizableSignSettingScreen extends Screen {

    // Constants
    private static final Text TITLE = Text.translatable("screen." + MyWorldTrafficAddition.MOD_ID + ".customizable_sign_edit_screen");
    private static final int MARGIN = 10;
    private static final int WIDGET_HEIGHT = 20;
    private static final int WIDGET_WIDTH = 200;
    private static final int SPACING = 30;

    // Block and world data
    private final World world;
    private final BlockPos pos;
    private final PlayerEntity player;

    // UI state
    private int currentYPosition = MARGIN;
    private int scrollY = 0;
    private int usedHeight = 0;
    private boolean showChildren = true;

    // Sign state
    private int initialRotationValue;
    private boolean isInitialized = false;
    private int signWidth = 1;
    private int signHeight = 1;

    /**
     * Creates a new screen for customizing signs
     */
    public CustomizableSignSettingScreen(World world, BlockPos pos, PlayerEntity player) {
        super(TITLE);
        this.world = world;
        this.pos = pos;
        this.player = player;

        loadInitialState();
    }

    private void loadInitialState() {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof CustomizableSignBlockEntity csbe) {
            initialRotationValue = csbe.getRotation();
            isInitialized = csbe.isInitialized();
        }
    }

    @Override
    protected void init() {
        super.init();
        drawChildren();
    }

    /**
     * Creates and adds all UI elements to the screen
     */
    private void drawChildren() {
        if (!showChildren) return;

        // Initialize button
        addButton(
                Text.translatable("widget." + MyWorldTrafficAddition.MOD_ID + ".customizable_sign_edit_screen.check_button"),
                (widget) -> initSign()
        );

        // Rotation slider
        DegreeSliderWidget rotationWidget = new DegreeSliderWidget(
                MARGIN, currentYPosition, WIDGET_WIDTH, WIDGET_HEIGHT,
                Text.of(initialRotationValue + "°"),
                initialRotationValue / 90f + 0.5f
        ) {
            @Override
            protected void updateMessage() {
                this.setMessage(Text.of((int)getValue() + "°"));
            }

            @Override
            protected void applyValue() {
                applyRotation((int)getValue());
            }
        };
        addDrawableChild(rotationWidget);
        advancePosition();

        // Draw editor button
        addButton(
                Text.translatable("widget." + MyWorldTrafficAddition.MOD_ID + ".draw_editor_button"),
                (widget) -> showEditorScreen()
        );
    }

    private void addButton(Text text, ButtonWidget.PressAction action) {
        ButtonWidget button = ButtonWidget.builder(text, action)
                .dimensions(MARGIN, currentYPosition, WIDGET_WIDTH, WIDGET_HEIGHT)
                .build();
        addDrawableChild(button);
        advancePosition();
    }

    private void advancePosition() {
        currentYPosition += SPACING;
        usedHeight += SPACING;
    }

    private void applyRotation(int rotation) {
        ClientPlayNetworking.send(new SetRotationCustomizableSignBlockPayload(pos, rotation));
    }

    /**
     * Opens the ImGui sign editor screen
     */
    private void showEditorScreen() {
        // Re-opening fixes issues with button focus when using space in ImGui
        reopen(false);
        this.clearAll();
        SignEditor.open(this.pos, this.world, isInitialized);
    }

    private void reopen(boolean showChildren) {
        this.close();
        CustomizableSignSettingScreen screen = new CustomizableSignSettingScreen(this.world, this.pos, this.player);
        screen.showChildren = showChildren;
        MinecraftClient.getInstance().setScreen(screen);
    }

    private void clearAll() {
        this.clearChildren();
    }

    /**
     * Initializes the sign structure by determining dimensions and configuring connected blocks
     */
    private void initSign() {
        // Determine sign dimensions
        signHeight = checkHeight(pos);
        signWidth = checkWidth(pos);

        // Configure connected blocks
        informOthersMaster(pos);
        setSignBorder(pos);
        checkSignPoles(pos, getFacing(pos, world), signHeight, signWidth);
        checkSigns(pos, getFacing(pos, world));

        // Send size to server
        ClientPlayNetworking.send(new SetSizeCustomizableSignPayload(pos, signHeight, signWidth));
        isInitialized = true;
    }

    /**
     * Determines the height of the sign structure by checking blocks above
     */
    private int checkHeight(BlockPos masterPos) {
        int height = 1;
        BlockPos currentPos = masterPos;

        while (isUsableCustomizableSignBlockEntity(currentPos.up(), world)) {
            currentPos = currentPos.up();
            height++;
        }

        return height;
    }

    /**
     * Determines the width of the sign structure by checking adjacent blocks
     */
    private int checkWidth(BlockPos masterPos) {
        int width = 1;
        BlockPos currentPos = masterPos;
        Direction facing = getFacing(currentPos, world);

        while (isUsableCustomizableSignBlockEntity(blockPosInDirection(facing, currentPos, 1), world)) {
            currentPos = blockPosInDirection(facing, currentPos, 1);
            width++;
        }

        return width;
    }

    /**
     * Identifies and registers all sign blocks in the structure
     */
    private void checkSigns(BlockPos masterPos, @NotNull Direction facing) {
        List<BlockPos> signPositions = new ArrayList<>();
        Direction rightDirection = getRightSideDirection(facing.getOpposite());

        // Scan row by row, starting at master position
        BlockPos currentUpPos = masterPos;
        while (isUsableCustomizableSignBlockEntity(currentUpPos, world)) {
            BlockPos currentRightPos = currentUpPos;

            // Scan a single row
            while (isUsableCustomizableSignBlockEntity(currentRightPos, world)) {
                signPositions.add(currentRightPos);
                currentRightPos = blockPosInDirection(rightDirection, currentRightPos, 1);
            }

            currentUpPos = currentUpPos.up();
        }

        // Send all sign positions to server
        String signPositionString = CustomizableSignBlockEntity.constructBlockPosListString(signPositions);
        ClientPlayNetworking.send(new SetSignPositionsCustomizableSignBlockPayload(masterPos, signPositionString));
    }

    /**
     * Sets all connected signs to use the master block for rendering
     */
    private void informOthersMaster(BlockPos masterPosY) {
        BlockPos currentYPos = masterPosY;

        while (isUsableCustomizableSignBlockEntity(currentYPos, world)) {
            BlockPos currentXPos = currentYPos;

            while (isUsableCustomizableSignBlockEntity(currentXPos, world)) {
                Direction facing = getFacing(currentXPos, world);

                // Skip the master block itself
                if (currentXPos.equals(pos)) {
                    currentXPos = blockPosInDirection(facing, currentXPos, 1);
                    continue;
                }

                // Configure non-master blocks
                ClientPlayNetworking.send(new SetMasterCustomizableSignBlockPayload(currentXPos, false, pos));
                ClientPlayNetworking.send(new SetRenderStateCustomizableSignBlockPayload(currentXPos, false));

                currentXPos = blockPosInDirection(facing, currentXPos, 1);
            }

            currentYPos = currentYPos.up();
        }
    }

    /**
     * Identifies and configures all sign poles connected to the sign structure
     */
    private void checkSignPoles(BlockPos masterPos, Direction facing, int signHeight, int signWidth) {
        List<BlockPos> poles = new ArrayList<>();
        Direction rightDirection = getRightSideDirection(facing.getOpposite());

        // Find the top-back corner of the sign structure
        BlockPos highestSignPolePos = masterPos.up(signHeight - 1);
        highestSignPolePos = blockPosInDirection(facing.getOpposite(), highestSignPolePos, 1);

        // Scan all potential pole positions column by column
        BlockPos currentRightPos = highestSignPolePos;
        for (int i = 0; i < signWidth; i++) {
            BlockPos currentDownPos = currentRightPos;

            // Scan down to find all poles in this column
            while (world.getBlockEntity(currentDownPos) instanceof SignPoleBlockEntity) {
                poles.add(currentDownPos);
                currentDownPos = currentDownPos.down();
            }

            currentRightPos = blockPosInDirection(rightDirection, currentRightPos, 1);
        }

        // Configure all poles to not render individually
        for (BlockPos pole : poles) {
            ClientPlayNetworking.send(new SetShouldRenderSignPolePayload(pole, false));
        }

        // Register all poles with the master sign
        ClientPlayNetworking.send(new SetSignPolePositionsCustomizableSignBlockPayload(
                masterPos,
                CustomizableSignBlockEntity.constructBlockPosListString(poles)
        ));
    }

    /**
     * Determines and sets appropriate border types for all sign blocks based on position
     */
    private void setSignBorder(BlockPos masterPos) {
        Direction rightSide = getRightSideDirection(getFacing(masterPos, world).getOpposite());
        BlockPos currentYPos = masterPos;

        while (isUsableCustomizableSignBlockEntity(currentYPos, world)) {
            BlockPos currentXPos = currentYPos;

            while (isUsableCustomizableSignBlockEntity(currentXPos, world)) {
                // Determine which sides need borders based on position
                BorderProperty borders = getBorderListBoundingBased(currentXPos, world);

                ClientPlayNetworking.send(new SetBorderTypeCustomizableSignBlockPayload(currentXPos, borders.toString()));
                currentXPos = blockPosInDirection(rightSide, currentXPos, 1);
            }

            currentYPos = currentYPos.up();
        }
    }

    // Scrolling implementation
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        final int scrollFactor = 20;

        // Don't scroll if content fits on screen
        if (usedHeight < this.height) return true;

        // Prevent scrolling past boundaries
        if ((scrollY + (int)(verticalAmount * scrollFactor)) > 0) return true;
        if ((currentYPosition + (int)(verticalAmount * scrollFactor)) < this.height) return true;

        // Apply scroll and redraw UI
        clearAll();
        scrollY += (int)(verticalAmount * scrollFactor);
        currentYPosition += scrollY;
        drawChildren();

        return true;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        ImGuiRenderer.showSignEditor = false;
        showChildren = true;
        return true;
    }
}