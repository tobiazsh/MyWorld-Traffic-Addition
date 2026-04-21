package at.tobiazsh.myworld.traffic_addition.screens;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.block_entities.SignPoleBlockEntity;
import at.tobiazsh.myworld.traffic_addition.payload.block_modification.SignPoleRotationPayload;
import at.tobiazsh.myworld.traffic_addition.widgets.DegreeSliderWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.Button;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

@Environment(EnvType.CLIENT)
public class SignPoleRotationScreen extends Screen {

    private final BlockPos pos;
    private final BlockEntity entity;

    private int initial_rotation_value;

    private static final Component TITLE = Component.translatable("screen." + MyWorldTrafficAddition.MOD_ID + ".sign_pole_rotation_screen");

    public SignPoleRotationScreen(Level world, BlockPos pos, Player player) {
        super(TITLE);
        this.pos = pos;
        this.entity = world.getBlockEntity(pos);


        if(entity instanceof SignPoleBlockEntity) initial_rotation_value = ((SignPoleBlockEntity) entity).getRotationValue();
    }
    private static final int uniButtonWidth = 200;
    private static final int uniButtonHeight = 20;

    public Button confirm;
    public DegreeSliderWidget rotation_slider;

    private void applyRotation(int rotation) {
        SignPoleRotationPayload payload = new SignPoleRotationPayload(pos, rotation);
        ClientPlayNetworking.send(payload);
    }

    @Override
    public void init() {

        rotation_slider = new DegreeSliderWidget(5, 5, uniButtonWidth, uniButtonHeight, Component.nullToEmpty(initial_rotation_value + "°"), initial_rotation_value / 90f + 0.5) {
            @Override
            protected void updateMessage() {
                this.setMessage(Component.nullToEmpty((int)getValue() + "°"));
            }

            @Override
            protected void applyValue() {
                applyRotation((int)getValue());
            }
        };

        confirm = Button.builder(Component.translatable("widget." + MyWorldTrafficAddition.MOD_ID + ".rotation_confirmation_button"), button -> {
        }).bounds(5, 30, 200, 20).tooltip(Tooltip.create(Component.literal("Tooltip of button1"))).build();

        addRenderableWidget(confirm);
        addRenderableWidget(rotation_slider);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
    }
}
