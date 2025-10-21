package com.bonepipe.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

/**
 * Toggle button widget for boolean settings
 */
public class ToggleButton extends Button {
    
    private boolean toggled;
    private final Consumer<Boolean> onToggle;
    
    public ToggleButton(int x, int y, int width, int height, Component message, 
                       boolean initialState, Consumer<Boolean> onToggle) {
        super(x, y, width, height, message, btn -> {}, DEFAULT_NARRATION);
        this.toggled = initialState;
        this.onToggle = onToggle;
        updateMessage();
    }
    
    @Override
    public void onPress() {
        this.toggled = !this.toggled;
        updateMessage();
        if (onToggle != null) {
            onToggle.accept(this.toggled);
        }
    }
    
    private void updateMessage() {
        // Update button appearance based on state
        this.active = true;
    }
    
    public boolean isToggled() {
        return toggled;
    }
    
    public void setToggled(boolean toggled) {
        if (this.toggled != toggled) {
            this.toggled = toggled;
            updateMessage();
        }
    }
    
    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        // Custom rendering for toggle state
        int color = toggled ? 0x00FF00 : 0xFF0000;
        
        // Draw background
        fill(poseStack, this.x, this.y, this.x + this.width, this.y + this.height, 
             toggled ? 0xFF004400 : 0xFF440000);
        
        // Draw border
        fill(poseStack, this.x, this.y, this.x + this.width, this.y + 1, 0xFF000000);
        fill(poseStack, this.x, this.y + this.height - 1, this.x + this.width, this.y + this.height, 0xFF000000);
        fill(poseStack, this.x, this.y, this.x + 1, this.y + this.height, 0xFF000000);
        fill(poseStack, this.x + this.width - 1, this.y, this.x + this.width, this.y + this.height, 0xFF000000);
        
        // Draw text
        drawCenteredString(poseStack, net.minecraft.client.Minecraft.getInstance().font, 
                          this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, color);
    }
}
