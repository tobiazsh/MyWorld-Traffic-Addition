package at.tobiazsh.myworld.traffic_addition.imgui.toast;

import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;

public class Toast {

    private final String title;
    private final String content;
    private final ToastType type;
    private final ToastIdentifier identifier = ToastIdentifier.ofRandom();
    private final long creationTime = System.currentTimeMillis();
    private final long timeToLiveMs;

    private boolean closed = false;

    private static final int TOAST_FLAGS =
            ImGuiWindowFlags.NoTitleBar |
            ImGuiWindowFlags.NoResize |
            ImGuiWindowFlags.NoDocking |
            ImGuiWindowFlags.NoMove |
            ImGuiWindowFlags.NoDecoration |
            ImGuiWindowFlags.NoSavedSettings |
            ImGuiWindowFlags.NoFocusOnAppearing |
            ImGuiWindowFlags.NoNav;

    private static final float LINE_HEIGHT = 2f;

    public Toast(String title, String content, ToastType type, long timeToLiveMs) {
        this.title = title;
        this.content = content;
        this.type = type;
        this.timeToLiveMs = timeToLiveMs;
    }

    public float getHeight(float maxWidth) {
        float itemSpacing = ImGui.getStyle().getItemSpacingY();
        float titleHeight = getTitleHeight();
        float contentHeight = getContentHeight(maxWidth);
        float lineHeight = itemSpacing * 2 + 2f;

        return titleHeight + contentHeight + lineHeight;
    }

    public float getWidth(float maxWidth) {
        return Math.min(maxWidth, ImGui.calcTextSize(content).x + ImGui.getStyle().getItemSpacingX() * 2);
    }

    /// Get absolute title height, including additional spacing
    public float getTitleHeight() {
        float itemSpacing = ImGui.getStyle().getItemSpacingY();
        return itemSpacing * 2 + ImGui.calcTextSizeY(title);
    }

    public float getContentHeight(float maxWidth) {
        float itemSpacing = ImGui.getStyle().getItemSpacingY();
        return itemSpacing * 3 + ImGui.calcTextSizeY(content, maxWidth - itemSpacing * 2); // Space for padding up, down and between;
    }

    /// Get the absolute line height, including additional spacing
    public float getLineHeight() {
        float itemSpacing = ImGui.getStyle().getItemSpacingY();
        return itemSpacing * 2 + LINE_HEIGHT;
    }

    public void render(float x, float y, float maxWidth) {
        float toastWidth = getWidth(maxWidth);
        float toastHeight = getHeight(maxWidth);

        ImGui.setNextWindowPos(x, y);
        ImGui.setNextWindowSize(toastWidth, toastHeight);

        if (ImGui.begin("##toast_" + identifier, TOAST_FLAGS)) {
            float titleHeight = getTitleHeight();
            float textTitleHeight = ImGui.calcTextSizeY(title);

            imgui.internal.ImGui.bringWindowToDisplayFront(imgui.internal.ImGui.getCurrentWindow());

            var drawList = ImGui.getWindowDrawList();
            drawList.addRectFilled(
                    x,
                    y,
                    x + toastWidth,
                    y + titleHeight,
                    ImGui.getColorU32(0.0588f, 0.0588f, 0.0588f, 255)
            );

            long icon = this.type.loadOrGetIcon();
            if (icon > -1) {
                float iconSize = titleHeight * .6f; // 80% of the title height
                float iconPosX = x + (titleHeight - iconSize) / 2;
                float iconPosY = y + (titleHeight - iconSize) / 2;
                ImGui.setCursorScreenPos(iconPosX, iconPosY);
                ImGui.image(icon, iconSize, iconSize);
                ImGui.sameLine();
            }

            float titlePosY = y + (titleHeight - textTitleHeight) / 2;
            ImGui.setCursorScreenPos(ImGui.getCursorScreenPosX(), titlePosY);
            ImGui.text(title);

            ImGui.spacing();

            ImGui.textWrapped(content);

            ImGui.setCursorScreenPos(x + toastWidth - titleHeight, y);
            ImGui.pushStyleColor(ImGuiCol.Button, ImGui.getColorU32(0, 0, 0, 0));

            if (ImGui.button("x", titleHeight, titleHeight)) {
                closed = true;
            }

            ImGui.popStyleColor();

            float percentage = (float) (System.currentTimeMillis() - creationTime) / timeToLiveMs;
            ImGui.getWindowDrawList().addLine(
                    x,
                    y + toastHeight - LINE_HEIGHT,
                    x + toastWidth * (1 - percentage),
                    y + toastHeight - LINE_HEIGHT,
                    ImGui.getColorU32(255, 255, 255, 255),
                    LINE_HEIGHT
            );

            ImGui.end();
        }
    }

    public String getToastIdentifier() {
        return identifier.toString();
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - creationTime >= timeToLiveMs || closed;
    }

    public static final class Builder {
        private String title = "";
        private String content = "";
        private ToastType type = ToastType.INFO;
        private long timeToLiveMs = 3000;

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder type(ToastType type) {
            this.type = type;
            return this;
        }

        public Builder timeToLive(long timeToLiveMs) {
            this.timeToLiveMs = timeToLiveMs;
            return this;
        }

        public Toast build() {
            return new Toast(title, content, type, timeToLiveMs);
        }
    }
}
