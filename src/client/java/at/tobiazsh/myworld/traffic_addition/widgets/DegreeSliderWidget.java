package at.tobiazsh.myworld.traffic_addition.Widgets;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public class DegreeSliderWidget extends SliderWidget {
    public DegreeSliderWidget(int x, int y, int width, int height, Text text, double value) {
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
