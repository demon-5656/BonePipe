package com.bonepipe.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

/**
 * Status indicator widget showing connection state
 */
public class StatusIndicator extends AbstractWidget {
    
    public enum Status {
        CONNECTED(0x00FF00, Component.translatable("gui.bonepipe.status.connected")),
        DISCONNECTED(0xFF0000, Component.translatable("gui.bonepipe.status.disconnected")),
        ACTIVE(0xFFFF00, Component.translatable("gui.bonepipe.status.active")),
        ERROR(0xFF8800, Component.translatable("gui.bonepipe.status.error"));
        
        final int color;
        final Component label;
        
        Status(int color, Component label) {
            this.color = color;
            this.label = label;
        }
    }
    
    private Status status;
    
    public StatusIndicator(int x, int y, int width, int height, Status initialStatus) {
        super(x, y, width, height, Component.empty());
        this.status = initialStatus;
    }
    
    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        // Draw indicator circle
        int centerX = this.x + this.width / 2;
        int centerY = this.y + this.height / 2;
        int radius = Math.min(this.width, this.height) / 2 - 2;
        
        // Simple circle using filled rectangles (pixel art style)
        for (int dy = -radius; dy <= radius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                if (dx * dx + dy * dy <= radius * radius) {
                    fill(poseStack, centerX + dx, centerY + dy, 
                         centerX + dx + 1, centerY + dy + 1, 
                         0xFF000000 | status.color);
                }
            }
        }
        
        // Draw border
        drawCircleBorder(poseStack, centerX, centerY, radius + 1, 0xFF000000);
    }
    
    private void drawCircleBorder(PoseStack poseStack, int cx, int cy, int radius, int color) {
        for (int angle = 0; angle < 360; angle += 5) {
            double rad = Math.toRadians(angle);
            int x = cx + (int)(Math.cos(rad) * radius);
            int y = cy + (int)(Math.sin(rad) * radius);
            fill(poseStack, x, y, x + 1, y + 1, color);
        }
    }
    
    @Override
    public void updateNarration(NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
    
    public Status getStatus() {
        return status;
    }
    
    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.visible && 
               mouseX >= this.x && mouseX < this.x + this.width && 
               mouseY >= this.y && mouseY < this.y + this.height;
    }
}
