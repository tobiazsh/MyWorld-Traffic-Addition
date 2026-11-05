package at.tobiazsh.myworld.traffic_addition.imgui.fonts;

import at.tobiazsh.myworld.traffic_addition.font.FontLifetime;
import at.tobiazsh.myworld.traffic_addition.font.LogicalFont;
import dev.tobiazsh.imguib3d.client.font.ImGuiFont;
import dev.tobiazsh.imguib3d.client.natives.drawlist.ImDrawListExt;
import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicLong;

public record LivingImGuiFont(
        LogicalFont logicalFont,
        FontLifetime lifetime,
        ImGuiFont imguiFont,
        float fontSize,
        AtomicLong lastUsedTimeSeconds
) {
    public static LivingImGuiFont ofNow(
            LogicalFont logicalFont,
            FontLifetime lifetime,
            dev.tobiazsh.imguib3d.client.font.ImGuiFont imguiFont,
            float fontSize
    ) {
        return new LivingImGuiFont(
                logicalFont,
                lifetime,
                imguiFont,
                fontSize,
                new AtomicLong(System.currentTimeMillis() * 1000L)
        );
    }

    /**
     * Creates a new LivingImGuiFont with a permanent lifetime and sets the last used time to now.
     */
    public static LivingImGuiFont ofNowPerma(
            LogicalFont logicalFont,
            dev.tobiazsh.imguib3d.client.font.ImGuiFont imguiFont,
            float fontSize
    ) {
        return new LivingImGuiFont(
                logicalFont,
                FontLifetime.PERMANENT,
                imguiFont,
                fontSize,
                new AtomicLong(System.currentTimeMillis() * 1000L)
        );
    }

    public void use() {
        lastUsedTimeSeconds.set(System.currentTimeMillis() * 1000L);
    }

    public boolean isStale(long currentTimeSeconds, long staleThresholdSeconds) {
        return (currentTimeSeconds - lastUsedTimeSeconds.get()) > staleThresholdSeconds &&
                lifetime != FontLifetime.PERMANENT;
    }
}
