package at.tobiazsh.myworld.traffic_addition.imgui.overlay;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAdditionClient;

public class OverlayStorage {
    public static final EditorOverlay EDITOR_OVERLAY = new EditorOverlay();
    public static final SignSelectionOverlay SIGN_SELECTION_OVERLAY = new SignSelectionOverlay();
    public static final ConfigurationOverlay CONFIGURATION_OVERLAY = new ConfigurationOverlay();
    public static final ToastOverlay TOAST_OVERLAY = new ToastOverlay(MyWorldTrafficAdditionClient::getToaster);
}
