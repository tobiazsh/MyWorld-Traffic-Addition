package at.tobiazsh.myworld.traffic_addition.widgets;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public class BooleanDisplayWidget extends AbstractWidget {

    private boolean value;

    private final Component onTrue;
    private final Component onFalse;

    static final int TEXT_COLOR = 0xFFF5F5F5; // Light gray, almost white

    public BooleanDisplayWidget(int x, int y, int width, int height, Component onTrue, Component onFalse, boolean initialValue) {
        super(x, y, width, height, Component.empty());
        this.value = initialValue;
        this.onTrue = onTrue;
        this.onFalse = onFalse;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    @Override
    protected void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
            final int backgroundCol = value ? 0xFF83AF4F : 0xFFF44336; // Green : Red
            final Font font = Minecraft.getInstance().font;
            final Component text = this.value ? onTrue : onFalse;

            graphics.fillGradient(getX(), getY(), getX() + this.width, getY() + this.height, 0x83AF4F, 0xF44336);

            Identifier sprite = Identifier.withDefaultNamespace("widget/button");
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, this.getX(), this.getY(), this.getWidth(), this.getHeight(), backgroundCol);

            graphics.text(
                    font,
                    text,
                    this.getX() + (this.getWidth() - font.width(text.getVisualOrderText())) / 2,
                    this.getY() + (this.getHeight() - 7) / 2, // 7 is an adjusted magic number because font.lineHeight apparently doesn't work as expected
                    TEXT_COLOR,
                    true
            );
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, Component.translatable("narration." + MyWorldTrafficAddition.MOD_ID + "status_widget.narration", value));
    }
}
