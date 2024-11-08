package at.tobiazsh.myworld.traffic_addition.components.Screens;


/*
 * @created 07/09/2024 (DD/MM/YYYY) - 23:31
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.ImGui.ImGuiRenderer;
import at.tobiazsh.myworld.traffic_addition.ImGui.Screens.SignEditorScreen;
import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.components.BlockEntities.CustomizableSignBlockEntity;
import at.tobiazsh.myworld.traffic_addition.components.BlockEntities.SignPoleBlockEntity;
import at.tobiazsh.myworld.traffic_addition.components.CustomPayloads.*;
import at.tobiazsh.myworld.traffic_addition.components.Sliders.DegreeSliderWidget;
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

import java.util.ArrayList;
import java.util.List;

import static at.tobiazsh.myworld.traffic_addition.components.BlockEntities.CustomizableSignBlockEntity.*;

@Environment(EnvType.CLIENT)
public class CustomizableSignSettingScreen extends Screen {

    World world;
    BlockPos pos;
    PlayerEntity player;

    private static final Text TITLE = Text.translatable("screen." + MyWorldTrafficAddition.MOD_ID + ".customizable_sign_edit_screen");

    private static final int margin = 10;
    private int currentYPosition = margin;
    private int scrollY = 0; // Part of scrolling feature
    private int usedHeight = 0; // Keeps track of the height of all the elements together plus the extra margins;

    private int initial_rotation_value;

    private boolean isInitialized = false;
    public boolean showChildren = true;

    private int signWidth = 1;
    private int signHeight = 1;

    public CustomizableSignSettingScreen(World world, BlockPos pos, PlayerEntity player) {
        super(TITLE);
        this.world = world;
        this.pos = pos;
        this.player = player;

        BlockEntity blockEntity = world.getBlockEntity(pos);

        if(blockEntity instanceof CustomizableSignBlockEntity csbe) {
            initial_rotation_value = csbe.getRotation();
            isInitialized = csbe.isInitialized();
        }
    }

    private void applyRotation(int rotation) {
        ClientPlayNetworking.send(new SetRotationCustomizableSignBlockPayload(pos, rotation));
    }

    private void drawChildren() {

        if (!showChildren) return;

        ButtonWidget initButton = ButtonWidget.builder(Text.translatable("widget." + MyWorldTrafficAddition.MOD_ID + ".customizable_sign_edit_screen.check_button"), button -> initSign())
                .dimensions(margin, currentYPosition, 200, 20)
                .build();
        currentYPosition += 30;
        usedHeight += 30;

        DegreeSliderWidget rotationWidget = new DegreeSliderWidget(margin, currentYPosition, 200, 20, Text.of(initial_rotation_value + "°"), initial_rotation_value / 90f + 0.5) {
            @Override
            protected void updateMessage() {
                this.setMessage(Text.of((int)getValue() + "°"));
            }

            @Override
            protected void applyValue() {
                applyRotation((int)getValue());
            }
        };
        currentYPosition += 30;
        usedHeight += 30;

        ButtonWidget drawEditorButton = ButtonWidget.builder(Text.translatable("widget." + MyWorldTrafficAddition.MOD_ID + ".draw_editor_button"), button -> showEditorScreen()).dimensions(margin, currentYPosition, 200, 20).build();

        currentYPosition += 30;
        usedHeight += 30;

        // Place last
        this.addDrawableChild(initButton);
        this.addDrawableChild(rotationWidget);
        this.addDrawableChild(drawEditorButton);
    }

    private void showEditorScreen() {
        // When one button is still focused from being pressed and I try to type a space in ImGui, it re-opens the ImGui Editor because Space (or Enter) acts as 'OK' in Minecraft's GUI, which isn't great since all progress is lost.
        // Re-Opening causes the screen to lose focus on the last selected button, and thus you can press space without a problem.
        // Was a bug previously.
        // Mouse is reset... Fix if possible sometime in the future
        reopen(false);
        this.clearAll();

        SignEditorScreen screen = new SignEditorScreen();
        screen.openSignEditorScreen(this.pos, this.world, isInitialized);
    }

    private void reopen(boolean showChildren) {
        this.close();
        CustomizableSignSettingScreen screen = new CustomizableSignSettingScreen(this.world, this.pos, this.player);
        screen.showChildren = showChildren;
        MinecraftClient.getInstance().setScreen(screen);
    }

    @Override
    protected void init() {
        super.init();
        drawChildren();
    }

    private void initSign() {
        signHeight = checkHeight(pos);
        signWidth = checkWidth(pos);
        setRestMaster(pos);
        setBorderTypes(pos);
        checkSignPoles(pos, getFacing(pos, world), signHeight, signWidth);
        checkSigns(pos, getFacing(pos, world));

        ClientPlayNetworking.send(new SetSizeCustomizableSignPayload(pos, signHeight, signWidth));

        isInitialized = true;
    }

    private int checkHeight(BlockPos masterPos) {
        int height = 1;

        while (world.getBlockEntity(masterPos.up()) instanceof CustomizableSignBlockEntity) {
            masterPos = masterPos.up();
            height++;
        }

        return height;
    }

    private int checkWidth(BlockPos masterPos) {
        int width = 1;

        while ((world.getBlockEntity(getCheckPos(getFacing(masterPos, world), masterPos))) instanceof CustomizableSignBlockEntity) {
            masterPos = getCheckPos(getFacing(masterPos, world), masterPos);

            width++;
        }

        return width;
    }

    private void checkSigns(BlockPos masterPos, Direction FACING) {
        List<BlockPos> signPositions = new ArrayList<>();

        BlockPos currentUpPos = masterPos;
        BlockPos currentRightPos = masterPos;
        while (world.getBlockEntity(currentUpPos) instanceof CustomizableSignBlockEntity) {
            while (world.getBlockEntity(currentRightPos) instanceof CustomizableSignBlockEntity) {
                System.out.println("Current Right Pos: " + currentRightPos);
                signPositions.add(currentRightPos);
                currentRightPos = getBlockPosAtDirection(getRightSideDirection(FACING.getOpposite()), currentRightPos, 1);
            }

            currentUpPos = currentUpPos.up();
            currentRightPos = currentUpPos;
        }

        String signPositionString = CustomizableSignBlockEntity.constructBlockPosListString(signPositions);
        ClientPlayNetworking.send(new SetSignPositionsCustomizableSignBlockPayload(masterPos, signPositionString));
    }

    // This sets the rest Master
    private void setRestMaster(BlockPos masterPosY) {
        while (world.getBlockEntity(masterPosY) instanceof CustomizableSignBlockEntity) {

            BlockPos posX = masterPosY;

            while (world.getBlockEntity(posX) instanceof CustomizableSignBlockEntity) {
                Direction FACING = getFacing(posX, world);

                if (posX == pos) posX = getCheckPos(FACING, posX);

                ClientPlayNetworking.send(new SetMasterCustomizableSignBlockPayload(posX, false, pos));
                ClientPlayNetworking.send(new SetRenderStateCustomizableSignBlockPayload(posX, false));

                posX = getCheckPos(FACING, posX);
            }

            masterPosY = masterPosY.up();
        }
    }

    private void checkSignPoles(BlockPos masterPos, Direction FACING, int signHeight, int signWidth) {
        List<BlockPos> poles = new ArrayList<>();

        BlockPos highestSignPolePos = masterPos.up(signHeight - 1); // Highest Point of the sign. -1 because the first y position is already included and without it, it would go up by one too much.
        highestSignPolePos = getBlockPosAtDirection(FACING.getOpposite(), highestSignPolePos, 1); // Go one back to the sign poles

        // First checks column, then row. This way, even the poles that aren't directly connected to a sign get counted.
        BlockPos currentDownPos = highestSignPolePos;
        BlockPos currentRightPos = highestSignPolePos;
        for (int i = 0; i < signWidth; i++) {
            while (world.getBlockEntity(currentDownPos) instanceof SignPoleBlockEntity) {
                poles.add(currentDownPos);
                currentDownPos = currentDownPos.down();
            }

            currentRightPos = getBlockPosAtDirection(getRightSideDirection(FACING.getOpposite()), currentRightPos, 1);
            currentDownPos = currentRightPos;
        }

        for (BlockPos pole : poles) {
            ClientPlayNetworking.send(new SetShouldRenderSignPolePayload(pole, false));
        }

        ClientPlayNetworking.send(new SetSignPolePositionsCustomizableSignBlockPayload(masterPos, CustomizableSignBlockEntity.constructBlockPosListString(poles)));
    }

    // False = No Border
    // True = Yes Border
    private void setBorderTypes(BlockPos masterPos) {
        Direction rightSide = getRightSideDirection(getFacing(masterPos, world).getOpposite());

        while (world.getBlockEntity(masterPos) instanceof CustomizableSignBlockEntity) {

            List<Boolean> borders = getBorderListBoundingBased(masterPos, world);
            String modelPath = CustomizableSignBlockEntity.getBorderName(borders.get(0), borders.get(1), borders.get(2), borders.get(3), "customizable_sign_block_border");
            ClientPlayNetworking.send(new SetBorderTypeCustomizableSignBlockPayload(masterPos, modelPath));

            BlockPos posX = masterPos;

            while (world.getBlockEntity(posX) instanceof CustomizableSignBlockEntity) {

                List<Boolean> bordersX = getBorderListBoundingBased(posX, world);
                String modelPathX = CustomizableSignBlockEntity.getBorderName(bordersX.get(0), bordersX.get(1), bordersX.get(2), bordersX.get(3), "customizable_sign_block_border");
                ClientPlayNetworking.send(new SetBorderTypeCustomizableSignBlockPayload(posX, modelPathX));

                posX = getBlockPosAtDirection(rightSide, posX, 1);
            }

            masterPos = masterPos.up();
        }
    }

    private void clearAll() {
        this.clearChildren();
    }

    private void addSpacing() {
        currentYPosition += 25;
        usedHeight += 25;
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

    @Override
    public boolean shouldCloseOnEsc() {
        ImGuiRenderer.showSignEditor = false;
        showChildren = true;

        return true;
    }
}