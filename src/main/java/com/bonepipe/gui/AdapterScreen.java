package com.bonepipe.gui;

import com.bonepipe.BonePipe;
import com.bonepipe.blocks.AdapterBlockEntity;
import com.bonepipe.network.packets.NetworkHandler;
import com.bonepipe.network.packets.UpdateFrequencyPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Mekanism-style GUI for Wireless Adapter
 * Clean, professional design with side configuration
 * 
 * v3.0.0 - Complete rewrite based on Mekanism best practices
 */
public class AdapterScreen extends AbstractContainerScreen<AdapterMenu> {
    
    private static final ResourceLocation TEXTURE = 
        new ResourceLocation(BonePipe.MODID, "textures/gui/adapter_new.png");
    
    // Mekanism-style dimensions
    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 166;
    private static final int BASE_Y_OFFSET = 84; // Player inventory Y position
    
    // Widget areas
    private static final int CONTENT_X = 8;
    private static final int CONTENT_Y = 18;
    private static final int CONTENT_WIDTH = 160;
    private static final int CONTENT_HEIGHT = 58; // Fits before player inventory (84-18-8)
    
    // Side configuration
    private static final int SIDE_BUTTON_SIZE = 18;
    private static final int SIDE_BUTTONS_START_X = 8;
    private static final int SIDE_BUTTONS_START_Y = 40;
    
    // Widgets
    private EditBox frequencyField;
    private Direction selectedSide = Direction.NORTH;
    
    public AdapterScreen(AdapterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
        this.inventoryLabelY = this.imageHeight - 94; // Mekanism formula
        this.titleLabelY = 6;
    }
    
    @Override
    protected void init() {
        super.init();
        
        int guiLeft = (this.width - this.imageWidth) / 2;
        int guiTop = (this.height - this.imageHeight) / 2;
        
        // Frequency input field
        frequencyField = new EditBox(
            this.font,
            guiLeft + 30, guiTop + 22,
            100, 16,
            Component.literal("Frequency")
        );
        frequencyField.setMaxLength(32);
        frequencyField.setValue(menu.getBlockEntity().getFrequency());
        frequencyField.setResponder(this::onFrequencyChanged);
        this.addRenderableWidget(frequencyField);
    }
    
    private void onFrequencyChanged(String newFrequency) {
        if (!newFrequency.isEmpty()) {
            NetworkHandler.CHANNEL.sendToServer(
                new UpdateFrequencyPacket(menu.getBlockEntity().getBlockPos(), newFrequency)
            );
        }
    }
    
    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        this.renderTooltip(poseStack, mouseX, mouseY);
    }
    
    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        
        int guiLeft = (this.width - this.imageWidth) / 2;
        int guiTop = (this.height - this.imageHeight) / 2;
        
        // Main background - single blit like Mekanism
        this.blit(poseStack, guiLeft, guiTop, 0, 0, this.imageWidth, this.imageHeight);
        
        // Side configuration buttons background
        renderSideConfigArea(poseStack, guiLeft, guiTop);
    }
    
    /**
     * Render side configuration area (6 direction buttons + mode selector)
     */
    private void renderSideConfigArea(PoseStack poseStack, int guiLeft, int guiTop) {
        int startX = guiLeft + SIDE_BUTTONS_START_X;
        int startY = guiTop + SIDE_BUTTONS_START_Y;
        
        AdapterBlockEntity be = menu.getBlockEntity();
        
        // Draw 6 direction buttons in 2 rows
        Direction[] directions = {
            Direction.UP, Direction.DOWN, Direction.NORTH,
            Direction.SOUTH, Direction.WEST, Direction.EAST
        };
        
        for (int i = 0; i < directions.length; i++) {
            int col = i % 3;
            int row = i / 3;
            int x = startX + col * (SIDE_BUTTON_SIZE + 2);
            int y = startY + row * (SIDE_BUTTON_SIZE + 2);
            
            Direction dir = directions[i];
            AdapterBlockEntity.SideConfig config = be.getSideConfig(dir);
            
            // Button background
            int texU = 176;
            int texV = (selectedSide == dir) ? 0 : 18; // Selected vs normal
            this.blit(poseStack, x, y, texU, texV, SIDE_BUTTON_SIZE, SIDE_BUTTON_SIZE);
            
            // Direction icon
            int iconU = 194 + (i * 16);
            int iconV = 0;
            this.blit(poseStack, x + 1, y + 1, iconU, iconV, 16, 16);
            
            // Mode indicator (small colored square)
            if (config != null && config.enabled) {
                int modeColor = getModeColor(config.mode);
                fill(poseStack, x + 14, y + 14, x + 17, y + 17, modeColor);
            }
        }
    }
    
    /**
     * Get color for transfer mode
     */
    private int getModeColor(AdapterBlockEntity.SideConfig.TransferMode mode) {
        return switch (mode) {
            case OUTPUT -> 0xFFFF5500; // Orange - extracting
            case INPUT -> 0xFF0055FF;  // Blue - inserting
            case BOTH -> 0xFFFFFF00;   // Yellow - bidirectional
            case DISABLED -> 0xFF808080; // Gray
        };
    }
    
    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        // Title
        this.font.draw(poseStack, this.title, (float)this.titleLabelX, (float)this.titleLabelY, 0x404040);
        
        // Inventory label
        this.font.draw(poseStack, this.playerInventoryTitle, 
            (float)this.inventoryLabelX, (float)this.inventoryLabelY, 0x404040);
        
        // Frequency label
        this.font.draw(poseStack, "Frequency:", 30, 12, 0x404040);
        
        // Side configuration header
        AdapterBlockEntity be = menu.getBlockEntity();
        if (selectedSide != null) {
            AdapterBlockEntity.SideConfig config = be.getSideConfig(selectedSide);
            String modeName = config != null ? config.mode.name() : "DISABLED";
            this.font.draw(poseStack, "Mode: " + modeName, 
                SIDE_BUTTONS_START_X + 70, SIDE_BUTTONS_START_Y + 10, 0x404040);
        }
        
        // Status info
        String status = be.isEnabled() ? "§aActive" : "§cInactive";
        this.font.draw(poseStack, status, 140, 12, 0xFFFFFF);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int guiLeft = (this.width - this.imageWidth) / 2;
        int guiTop = (this.height - this.imageHeight) / 2;
        
        // Check side button clicks
        int startX = guiLeft + SIDE_BUTTONS_START_X;
        int startY = guiTop + SIDE_BUTTONS_START_Y;
        
        Direction[] directions = {
            Direction.UP, Direction.DOWN, Direction.NORTH,
            Direction.SOUTH, Direction.WEST, Direction.EAST
        };
        
        for (int i = 0; i < directions.length; i++) {
            int col = i % 3;
            int row = i / 3;
            int x = startX + col * (SIDE_BUTTON_SIZE + 2);
            int y = startY + row * (SIDE_BUTTON_SIZE + 2);
            
            if (mouseX >= x && mouseX < x + SIDE_BUTTON_SIZE &&
                mouseY >= y && mouseY < y + SIDE_BUTTON_SIZE) {
                
                Direction clickedDir = directions[i];
                
                if (button == 0) { // Left click - select side
                    selectedSide = clickedDir;
                    return true;
                } else if (button == 1) { // Right click - cycle mode
                    cycleSideMode(clickedDir);
                    return true;
                }
            }
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    /**
     * Cycle through transfer modes for a side
     */
    private void cycleSideMode(Direction direction) {
        AdapterBlockEntity be = menu.getBlockEntity();
        AdapterBlockEntity.SideConfig config = be.getSideConfig(direction);
        
        if (config == null) {
            config = new AdapterBlockEntity.SideConfig();
        }
        
        // Cycle: DISABLED -> OUTPUT -> INPUT -> BOTH -> DISABLED
        config.mode = switch (config.mode) {
            case DISABLED -> AdapterBlockEntity.SideConfig.TransferMode.OUTPUT;
            case OUTPUT -> AdapterBlockEntity.SideConfig.TransferMode.INPUT;
            case INPUT -> AdapterBlockEntity.SideConfig.TransferMode.BOTH;
            case BOTH -> AdapterBlockEntity.SideConfig.TransferMode.DISABLED;
        };
        config.enabled = config.mode != AdapterBlockEntity.SideConfig.TransferMode.DISABLED;
        
        // Send to server
        NetworkHandler.CHANNEL.sendToServer(
            new com.bonepipe.network.packets.UpdateSideConfigPacket(
                be.getBlockPos(), direction, config.enabled, config.mode
            )
        );
    }
    
    @Override
    public void resize(net.minecraft.client.Minecraft minecraft, int width, int height) {
        String freq = frequencyField.getValue();
        super.resize(minecraft, width, height);
        frequencyField.setValue(freq);
    }
}
