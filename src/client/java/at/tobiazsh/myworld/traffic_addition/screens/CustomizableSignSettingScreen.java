package at.tobiazsh.myworld.traffic_addition.screens;

import at.tobiazsh.myworld.traffic_addition.payload.block_modification.*;
import at.tobiazsh.myworld.traffic_addition.imgui.main_windows.SignEditor;
import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.block_entities.CustomizableSignBlockEntity;
import at.tobiazsh.myworld.traffic_addition.utils.CustomizableSignInitializer;
import at.tobiazsh.myworld.traffic_addition.widgets.DegreeSliderWidget;
import at.tobiazsh.myworld.traffic_addition.utils.math.BlockPosExtended;
import at.tobiazsh.myworld.traffic_addition.utils.BorderProperty;
import at.tobiazsh.myworld.traffic_addition.utils.ListUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import java.io.IOException;
import java.util.List;

import static at.tobiazsh.myworld.traffic_addition.block_entities.CustomizableSignBlockEntity.*;
import static at.tobiazsh.myworld.traffic_addition.language.JenguaTranslator.tr;
import static at.tobiazsh.myworld.traffic_addition.utils.DirectionUtils.blockPosInDirection;

/**
 * Screen for customizing sign blocks
 */
@Environment(EnvType.CLIENT)
public class CustomizableSignSettingScreen extends Screen {

    // Constants
    private static final Component TITLE = Component.translatable("screen." + MyWorldTrafficAddition.MOD_ID + ".customizable_sign_edit_screen");
    private static final int MARGIN = 10;
    private static final int WIDGET_HEIGHT = 20;
    private static final int WIDGET_WIDTH = 200;
    private static final int SPACING = 30;

    // Block and world data
    private final Level world;
    private final BlockPos pos;
    private final Player player;

    // UI state
    private int currentYPosition = MARGIN;
    private int scrollY = 0;
    private int usedHeight = 0;
    private boolean showChildren = true;

    // Sign state
    private int initialRotationValue;
    private boolean isInitialized = false;

    /**
     * Creates a new screen for customizing signRelative
     */
    public CustomizableSignSettingScreen(Level world, BlockPos pos, Player player) {
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
                Component.translatable("widget." + MyWorldTrafficAddition.MOD_ID + ".customizable_sign_edit_screen.check_button"),
                (widget) -> initSign()
        );

        // Rotation slider
        DegreeSliderWidget rotationWidget = new DegreeSliderWidget(
                MARGIN, currentYPosition, WIDGET_WIDTH, WIDGET_HEIGHT,
                Component.nullToEmpty(initialRotationValue + "°"),
                initialRotationValue / 90f + 0.5f
        ) {
            @Override
            protected void updateMessage() {
                this.setMessage(Component.nullToEmpty((int)getValue() + "°"));
            }

            @Override
            protected void applyValue() {
                applyRotation((int)getValue());
            }
        };
        addRenderableWidget(rotationWidget);
        advancePosition();

        // Draw editor button
        addButton(
                Component.translatable("widget." + MyWorldTrafficAddition.MOD_ID + ".draw_editor_button"),
                (widget) -> showEditorScreen()
        );
    }

    private void addButton(Component text, Button.OnPress action) {
        Button button = Button.builder(text, action)
                .bounds(MARGIN, currentYPosition, WIDGET_WIDTH, WIDGET_HEIGHT)
                .build();
        addRenderableWidget(button);
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
        this.onClose();
        CustomizableSignSettingScreen screen = new CustomizableSignSettingScreen(this.world, this.pos, this.player);
        screen.showChildren = showChildren;
        Minecraft.getInstance().setScreen(screen);
    }

    private void clearAll() {
        this.clearWidgets();
    }

    private void initSign() {
        CustomizableSignBlockEntity currentSignBlockEntity = (CustomizableSignBlockEntity) world.getBlockEntity(pos);

        if (currentSignBlockEntity == null) {
            player.displayClientMessage(Component.literal("Failed to initialize sign structure!"), false);
            return;
        }

        var result = CustomizableSignInitializer.initializeSign(currentSignBlockEntity);

        if (result.hasError()) {
            player.displayClientMessage(Component.literal(result.error().message()), false);
            return;
        }

        if (!informMaster(result.signAbsolute(), new BlockPosExtended(pos), currentSignBlockEntity.getFacing())) {
            player.displayClientMessage(Component.literal(tr("Minecraft.MWTA.Warn", "No sign found at one or more of the positions! Please check the structure!")), false);
            return;
        }

        ClientPlayNetworking.send(new SetSizeCustomizableSignPayload(pos, result.signHeight(), result.signWidth()));

        setSignBorder(result.signAbsolute());
        result.poleAbsolute().forEach(pole -> ClientPlayNetworking.send(new SetShouldRenderSignPolePayload(pole, false)));

        List<String> distancesString = result.poleRelative().stream()
                .map(BlockPosExtended::toObjectString)
                .toList();

        try {
            // Register poles in the master sign block
            ClientPlayNetworking.send(new SetSignPolePositionsCustomizableSignBlockPayload(
                    pos,
                    ListUtils.toByteArray(distancesString)
            ));
        } catch (IOException e) {
            MyWorldTrafficAddition.LOGGER.error("Failed to serialize sign pole positions for {}: {}", pos, e.getMessage());
            return;
        }

        List<String> signDistancesString = result.signRelative().stream()
                .map(BlockPosExtended::toObjectString)
                .toList();

        byte[] signDistanceBytes;

        try {
            signDistanceBytes = ListUtils.toByteArray(signDistancesString);
        } catch (IOException e) {
            MyWorldTrafficAddition.LOGGER.error("Failed to serialize sign positions for {}: {}", pos, e.getMessage());
            player.displayClientMessage(Component.literal(tr("Minecraft.MWTA.Error", "Failed to initialize sign structure!")), false);
            return;
        }

        ClientPlayNetworking.send(new SetSignPositionsCustomizableSignBlockPayload(pos, signDistanceBytes));
    }

    /**
     * Informs all other signRelative except the master about their new master position
     *
     * @return success status of operation
     */
    private boolean informMaster(List<BlockPos> positions, BlockPosExtended masterPos, Direction facing) {
        for (BlockPos pos : positions) {
            if (!isUsableCustomizableSignBlockEntity(pos, world, facing))
                return false; // If something changed during that time, we can't inform the block of the new master and thus can't continue

            ClientPlayNetworking.send(new SetMasterCustomizableSignBlockPayload(pos, (Boolean) pos.equals(masterPos), masterPos));
            ClientPlayNetworking.send(new SetRenderStateCustomizableSignBlockPayload(pos, pos.equals(masterPos)));
        }

        return true;
    }

    /**
     * Determines and sets appropriate border types for all sign blocks based on position
     */
    private void setSignBorder(List<BlockPos> positions) {
        positions.forEach(pos -> {
            BorderProperty borders = getBorderListBoundingBased(pos, world);
            ClientPlayNetworking.send(new SetBorderTypeCustomizableSignBlockPayload(pos, borders.toObjectString()));
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
        boolean close = SignEditor.isClosed();

        if (close)
            showChildren = true;

        return close;
    }

    @Override
    public void onClose() {
        super.onClose();
        ClientPlayNetworking.send(new CustomizableSignSettingScreenClosed(pos));
    }
}