package at.tobiazsh.myworld.traffic_addition.Screens;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.Components.BlockEntities.SignPoleBlockEntity;
import at.tobiazsh.myworld.traffic_addition.Components.CustomPayloads.BlockModification.SignPoleRotationPayload;
import at.tobiazsh.myworld.traffic_addition.Widgets.DegreeSliderWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Environment(EnvType.CLIENT)
public class SignPoleRotationScreen extends Screen {

    private final BlockPos pos;
    private final World world;
    private final BlockEntity entity;

    private int initial_rotation_value;

    private static final Text TITLE = Text.translatable("screen." + MyWorldTrafficAddition.MOD_ID + ".sign_pole_rotation_screen");

    public SignPoleRotationScreen(World world, BlockPos pos, PlayerEntity player) {
        super(TITLE);
        this.pos = pos;
        this.world = world;
        this.entity = world.getBlockEntity(pos);


        if(entity instanceof SignPoleBlockEntity) initial_rotation_value = ((SignPoleBlockEntity) entity).getRotationValue();
    }
    private static final int uniButtonWidth = 200;
    private static final int uniButtonHeight = 20;

    public ButtonWidget confirm;
    public DegreeSliderWidget rotation_slider;

    private void applyRotation(int rotation) {
        SignPoleRotationPayload payload = new SignPoleRotationPayload(pos, rotation);
        ClientPlayNetworking.send(payload);
    }

    @Override
    public void init() {

        rotation_slider = new DegreeSliderWidget(5, 5, uniButtonWidth, uniButtonHeight, Text.of(initial_rotation_value + "°"), initial_rotation_value / 90f + 0.5) {
            @Override
            protected void updateMessage() {
                this.setMessage(Text.of((int)getValue() + "°"));
            }

            @Override
            protected void applyValue() {
                applyRotation((int)getValue());
            }
        };

        confirm = ButtonWidget.builder(Text.translatable("widget." + MyWorldTrafficAddition.MOD_ID + ".rotation_confirmation_button"), button -> {
        }).dimensions(5, 30, 200, 20).tooltip(Tooltip.of(Text.literal("Tooltip of button1"))).build();

        addDrawableChild(confirm);
        addDrawableChild(rotation_slider);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
    }
}
