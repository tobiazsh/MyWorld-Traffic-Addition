package at.tobiazsh.myworld.traffic_addition.Widgets;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

public class DegreeSliderWidget extends AbstractSliderButton {
    public DegreeSliderWidget(int x, int y, int width, int height, Component text, double value) {
        super(x, y, width, height, text, value);
    }

    @Override
    protected void updateMessage(){}

    @Override
    protected void applyValue(){}

    public double getValue() {
        return this.value * 90 - 45;
    }
}
