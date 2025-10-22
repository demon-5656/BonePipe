package com.bonepipe.gui.widgets;

import com.bonepipe.blocks.AdapterBlockEntity;
import com.bonepipe.network.packets.NetworkHandler;
import com.bonepipe.network.packets.UpdateSideConfigPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

/**
 * Widget for configuring transfer modes for each side of the adapter
 * Shows 6 sides (UP, DOWN, NORTH, SOUTH, EAST, WEST) with Input/Output/Both/Disabled buttons
 */
public class SideConfigWidget extends AbstractWidget {
    
    private final BlockPos blockPos;
    private final AdapterBlockEntity blockEntity;
    private final ResourceType resourceType;
    
    // Layout constants for compact 46px height widget
    private static final int SIDE_SIZE = 12; // Small buttons to fit in compact space
    private static final int MODE_BUTTON_WIDTH = 60;
    private static final int MODE_BUTTON_HEIGHT = 10;
    private static final int SPACING = 2;
    
    private Direction selectedSide = Direction.NORTH;
    
    public enum ResourceType {
        ITEMS("Items"),
        FLUIDS("Fluids"),
        ENERGY("Energy"),
        GAS("Gas");
        
        final String displayName;
        
        ResourceType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public SideConfigWidget(int x, int y, int width, int height, BlockPos blockPos, 
                           AdapterBlockEntity blockEntity, ResourceType resourceType) {
        super(x, y, width, height, Component.literal("Side Configuration"));
        this.blockPos = blockPos;
        this.blockEntity = blockEntity;
        this.resourceType = resourceType;
    }
    
    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        
        // Draw background
        fill(poseStack, x, y, x + width, y + height, 0xFF2B2B2B);
        
        // Draw compact title
        drawString(poseStack, Minecraft.getInstance().font, resourceType.getDisplayName() + " Config", 
            x + 4, y + 2, 0xFFFFFF);
        
        // Draw 6-sided view (simplified 2D representation)
        renderSideButtons(poseStack, mouseX, mouseY);
        
        // Draw mode buttons for selected side
        renderModeButtons(poseStack, mouseX, mouseY);
    }
    
    private void renderSideButtons(PoseStack poseStack, int mouseX, int mouseY) {
        int startX = x + 4;
        int startY = y + 10;
        
        // Layout:
        //       [UP]
        // [WEST][DOWN][EAST]
        //      [NORTH]
        //      [SOUTH]
        
        Direction[] layout = {
            null, Direction.UP, null,
            Direction.WEST, Direction.DOWN, Direction.EAST,
            null, Direction.NORTH, null,
            null, Direction.SOUTH, null
        };
        
        for (int i = 0; i < layout.length; i++) {
            Direction dir = layout[i];
            if (dir == null) continue;
            
            int row = i / 3;
            int col = i % 3;
            int bx = startX + col * (SIDE_SIZE + SPACING);
            int by = startY + row * (SIDE_SIZE + SPACING);
            
            boolean selected = dir == selectedSide;
            boolean hovered = mouseX >= bx && mouseX < bx + SIDE_SIZE &&
                             mouseY >= by && mouseY < by + SIDE_SIZE;
            
            // Get current configuration
            AdapterBlockEntity.SideConfig config = blockEntity.getSideConfig(dir);
            int color = getModeColor(config != null ? config.mode : AdapterBlockEntity.SideConfig.TransferMode.DISABLED);
            
            if (selected) {
                color = 0xFFFFFF00; // Yellow for selected
            } else if (hovered) {
                color = brighten(color);
            }
            
            // Draw side button
            fill(poseStack, bx, by, bx + SIDE_SIZE, by + SIDE_SIZE, color);
            fill(poseStack, bx + 1, by + 1, bx + SIDE_SIZE - 1, by + SIDE_SIZE - 1, 0xFF000000);
            
            // Draw direction label (first letter)
            String label = dir.getName().substring(0, 1).toUpperCase();
            drawCenteredString(poseStack, Minecraft.getInstance().font, label, bx + SIDE_SIZE / 2, by + SIDE_SIZE / 2 - 3, 0xFFFFFF);
        }
    }
    
    private void renderModeButtons(PoseStack poseStack, int mouseX, int mouseY) {
        int startX = x + 75; // Adjusted for compact layout
        int startY = y + 10;
        
        drawString(poseStack, Minecraft.getInstance().font, "Side: " + selectedSide.getName(), 
            startX, startY, 0xFFFFFF);
        
        AdapterBlockEntity.SideConfig.TransferMode[] modes = AdapterBlockEntity.SideConfig.TransferMode.values();
        AdapterBlockEntity.SideConfig config = blockEntity.getSideConfig(selectedSide);
        AdapterBlockEntity.SideConfig.TransferMode currentMode = config != null ? 
            config.mode : AdapterBlockEntity.SideConfig.TransferMode.DISABLED;
        
        for (int i = 0; i < modes.length; i++) {
            AdapterBlockEntity.SideConfig.TransferMode mode = modes[i];
            int by = startY + 10 + i * (MODE_BUTTON_HEIGHT + SPACING); // Tighter spacing
            
            boolean hovered = mouseX >= startX && mouseX < startX + MODE_BUTTON_WIDTH &&
                             mouseY >= by && mouseY < by + MODE_BUTTON_HEIGHT;
            boolean selected = mode == currentMode;
            
            int color = selected ? getModeColor(mode) : 0xFF444444;
            if (hovered && !selected) {
                color = 0xFF666666;
            }
            
            // Draw button
            fill(poseStack, startX, by, startX + MODE_BUTTON_WIDTH, by + MODE_BUTTON_HEIGHT, color);
            
            // Draw label (smaller font)
            drawCenteredString(poseStack, Minecraft.getInstance().font, mode.name(), 
                startX + MODE_BUTTON_WIDTH / 2, by + 2, 0xFFFFFF); // Adjusted vertical center
        }
    }
    
    private int getModeColor(AdapterBlockEntity.SideConfig.TransferMode mode) {
        return switch (mode) {
            case DISABLED -> 0xFFAA0000;  // Red
            case INPUT -> 0xFF0000AA;     // Blue
            case OUTPUT -> 0xFF00AA00;    // Green
            case BOTH -> 0xFFAAAA00;      // Yellow
        };
    }
    
    private int brighten(int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        
        r = Math.min(255, r + 40);
        g = Math.min(255, g + 40);
        b = Math.min(255, b + 40);
        
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isHovered) return false;
        
        // Check side button clicks
        int startX = x + 4;
        int startY = y + 10;
        
        Direction[] layout = {
            null, Direction.UP, null,
            Direction.WEST, Direction.DOWN, Direction.EAST,
            null, Direction.NORTH, null,
            null, Direction.SOUTH, null
        };
        
        for (int i = 0; i < layout.length; i++) {
            Direction dir = layout[i];
            if (dir == null) continue;
            
            int row = i / 3;
            int col = i % 3;
            int bx = startX + col * (SIDE_SIZE + SPACING);
            int by = startY + row * (SIDE_SIZE + SPACING);
            
            if (mouseX >= bx && mouseX < bx + SIDE_SIZE &&
                mouseY >= by && mouseY < by + SIDE_SIZE) {
                selectedSide = dir;
                playDownSound(Minecraft.getInstance().getSoundManager());
                return true;
            }
        }
        
        // Check mode button clicks
        int modeStartX = x + 75;
        int modeStartY = y + 20; // Adjusted for compact layout
        
        AdapterBlockEntity.SideConfig.TransferMode[] modes = AdapterBlockEntity.SideConfig.TransferMode.values();
        
        for (int i = 0; i < modes.length; i++) {
            AdapterBlockEntity.SideConfig.TransferMode mode = modes[i];
            int by = modeStartY + i * (MODE_BUTTON_HEIGHT + SPACING);
            
            if (mouseX >= modeStartX && mouseX < modeStartX + MODE_BUTTON_WIDTH &&
                mouseY >= by && mouseY < by + MODE_BUTTON_HEIGHT) {
                
                // Send packet to server to update configuration
                boolean enabled = mode != AdapterBlockEntity.SideConfig.TransferMode.DISABLED;
                NetworkHandler.CHANNEL.sendToServer(
                    new UpdateSideConfigPacket(blockPos, selectedSide, enabled, mode)
                );
                
                playDownSound(Minecraft.getInstance().getSoundManager());
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public void updateNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
