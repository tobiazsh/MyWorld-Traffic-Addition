package at.tobiazsh.myworld.traffic_addition.screens;

import at.tobiazsh.myworld.traffic_addition.custom_payloads.block_modification.*;
import at.tobiazsh.myworld.traffic_addition.imgui.ImGuiRenderer;
import at.tobiazsh.myworld.traffic_addition.imgui.main_windows.SignEditor;
import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.block_entities.CustomizableSignBlockEntity;
import at.tobiazsh.myworld.traffic_addition.block_entities.SignPoleBlockEntity;
import at.tobiazsh.myworld.traffic_addition.Widgets.DegreeSliderWidget;
import at.tobiazsh.myworld.traffic_addition.language.JenguaTranslator;
import at.tobiazsh.myworld.traffic_addition.utils.BorderProperty;
import at.tobiazsh.myworld.traffic_addition.utils.DirectionUtils;
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

    private boolean abort = false;

    /**
     * Initializes the sign structure by determining dimensions and configuring connected blocks
     */
    private void initSign() {
        // Determine sign dimensions
        CustomizableSignBlockEntity currentSignBlockEntity = (CustomizableSignBlockEntity) world.getBlockEntity(pos);
        Direction facing = currentSignBlockEntity.getFacing();

        int signHeight = checkHeight(pos, facing);
        int signWidth = checkWidth(pos, facing);

        // Configure connected blocks
        List<BlockPos> signs = checkSigns(pos, facing, signWidth, signHeight);

        if (!abort && signs != null) {
            informMaster(signs, pos);
            setSignBorder(signs);
            checkSignPoles(pos, DirectionUtils.getFacing(pos, world), signHeight, signWidth);

            // Send size to server
            ClientPlayNetworking.send(new SetSizeCustomizableSignPayload(pos, signHeight, signWidth));

            // Send all sign positions to server
            String signPositionString = CustomizableSignBlockEntity.constructBlockPosListString(signs);
            ClientPlayNetworking.send(new SetSignPositionsCustomizableSignBlockPayload(pos, signPositionString));
        }

        isInitialized = true;
    }

    /**
     * Determines the height of the sign structure by checking blocks above
     */
    private int checkHeight(BlockPos masterPos, Direction facing) {
        int height = 1;

        BlockPos currentPos = masterPos;

        while (isUsableCustomizableSignBlockEntity(currentPos.up(), world, facing)) {
            currentPos = currentPos.up();
            height++;
        }

        return height;
    }

    /**
     * Determines the width of the sign structure by checking adjacent blocks
     */
    private int checkWidth(BlockPos startingPos, Direction facing) {
        int right = 1;
        Direction rightDirection = getRightSideDirection(facing.getOpposite());

        while (isUsableCustomizableSignBlockEntity(blockPosInDirection(rightDirection, startingPos, right), world, facing))
            right++;

        return right; // Subtract 1 to not double count the master block
    }

    /**
     * Identifies and registers all sign blocks in the structure
     */
    private List<BlockPos> checkSigns(BlockPos masterPos, @NotNull Direction facing, int signWidth, int signHeight) {
        List<BlockPos> signs = new ArrayList<>();
        Direction rightDirection = getRightSideDirection(facing.getOpposite());

        // Scan row by row, starting at master position
        int scannedHeight = 0;
        int scannedWidth = 0;
        BlockPos currentUpPos = masterPos;
        while (isUsableCustomizableSignBlockEntity(currentUpPos, world, facing)) {
            BlockPos currentRightPos = currentUpPos;

            // Scan a single row
            scannedWidth = 0;
            while (isUsableCustomizableSignBlockEntity(currentRightPos, world, facing)) {
                signs.add(currentRightPos);
                currentRightPos = blockPosInDirection(rightDirection, currentRightPos, 1);

                scannedWidth++;
            }

            if (scannedWidth != signWidth) {
                player.sendMessage(Text.literal(JenguaTranslator.tr("Minecraft.MWTA.Warn.SignNotComplete", "Sign is not complete! Please check the structure")), false);
                abort = true;
                break;
            }

            scannedHeight++;
            currentUpPos = currentUpPos.up();
        }

        if (scannedHeight != signHeight) {
            player.sendMessage(Text.literal(JenguaTranslator.tr("Minecraft.MWTA.Warn.SignNotComplete", "Sign is not complete! Please check the structure")), false);
            abort = true;
        }

        if (abort) {
            return null;
        }

        return signs;
    }

    /**
     * Informs all other signs except the master about their new master position
     */
    private void informMaster(List<BlockPos> signs, BlockPos masterPos) {
        signs.stream()
                .filter(pos -> !pos.equals(masterPos))
                .forEach(pos -> {
                    ClientPlayNetworking.send(new SetMasterCustomizableSignBlockPayload(pos, false, masterPos));
                    ClientPlayNetworking.send(new SetRenderStateCustomizableSignBlockPayload(pos, false));
                });
    }

    /**
     * Identifies and configures all sign poles connected to the sign structure
     */
    private void checkSignPoles(BlockPos masterPos, Direction facing, int signHeight, int signWidth) {
        List<BlockPos> poles = new ArrayList<>();
        Direction rightDirection = getRightSideDirection(facing.getOpposite());

        // Start position; this is the topmost pole position in the sign structure
        BlockPos start = blockPosInDirection(facing.getOpposite(), masterPos.up(signHeight - 1), 1);

        for (int i = 0; i < signWidth; i++) {
            BlockPos pos = blockPosInDirection(rightDirection, start, i);
            // Nach unten suchen, solange ein SignPoleBlockEntity gefunden wird
            while (world.getBlockEntity(pos) instanceof SignPoleBlockEntity) {
                poles.add(pos);
                pos = pos.down();
            }
        }

        // Mark all poles as not renderable
        poles.forEach(pole -> ClientPlayNetworking.send(new SetShouldRenderSignPolePayload(pole, false)));

        // Register poles in the master sign block
        ClientPlayNetworking.send(new SetSignPolePositionsCustomizableSignBlockPayload(
                masterPos,
                CustomizableSignBlockEntity.constructBlockPosListString(poles)
        ));
    }

    /**
     * Determines and sets appropriate border types for all sign blocks based on position
     */
    private void setSignBorder(List<BlockPos> signs) {
        signs.forEach(signPos -> {
            BorderProperty borders = getBorderListBoundingBased(signPos, world);
            ClientPlayNetworking.send(new SetBorderTypeCustomizableSignBlockPayload(signPos, borders.toString()));
        });
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