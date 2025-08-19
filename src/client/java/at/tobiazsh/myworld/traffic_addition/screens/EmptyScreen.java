package at.tobiazsh.myworld.traffic_addition.screens;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class EmptyScreen extends Screen {

    private final Runnable onExit;

    public EmptyScreen(Text title, Runnable onExit) {
        super(title);
        this.onExit = onExit;
    }

    @Override
    public void close() {
        super.close();
        onExit.run();
    }
}
