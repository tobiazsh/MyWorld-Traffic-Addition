package at.tobiazsh.myworld.traffic_addition.screens;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class EmptyScreen extends Screen {

    private final Runnable onExit;

    public EmptyScreen(Component title, Runnable onExit) {
        super(title);
        this.onExit = onExit;
    }

    @Override
    public void onClose() {
        super.onClose();
        onExit.run();
    }
}
