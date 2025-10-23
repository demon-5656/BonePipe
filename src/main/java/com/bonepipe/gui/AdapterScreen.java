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
import net.minecraft.client.gui.components.Button;

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
    // Track focus state to detect when user finishes editing
    private boolean prevFreqFocused = false;
    private Button saveFreqButton;
    
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
        
        // Frequency input field - matches black rectangle in texture
        frequencyField = new EditBox(
            this.font,
            guiLeft + 31, guiTop + 23,
            98, 14,
            Component.literal("Frequency")
        );
        frequencyField.setMaxLength(32);
        frequencyField.setValue(menu.getBlockEntity().getFrequency());
        frequencyField.setResponder(this::onFrequencyChanged);
        frequencyField.setBordered(false); // No border - texture provides it
        frequencyField.setTextColor(0xFFFFFF); // White text on black background
        this.addRenderableWidget(frequencyField);
        
        // Set focus to frequency field
        this.setInitialFocus(frequencyField);
        this.prevFreqFocused = frequencyField.isFocused();

        // Save button next to frequency field
        saveFreqButton = this.addRenderableWidget(new Button(guiLeft + 131, guiTop + 23, 30, 14, Component.literal("Save"), (btn) -> {
            sendFrequencyToServer();
        }));
    }
    
    private void onFrequencyChanged(String newFrequency) {
        // Respond to changes (called on every keystroke)
        // Actual sending happens on Enter or GUI close
    }
    
    /**
     * Send frequency to server
     */
    private void sendFrequencyToServer() {
        String newFrequency = frequencyField.getValue().trim();
        if (!newFrequency.isEmpty()) {
            NetworkHandler.CHANNEL.sendToServer(
                new UpdateFrequencyPacket(menu.getBlockEntity().getBlockPos(), newFrequency)
            );
            BonePipe.LOGGER.info("Sending frequency to server: {}", newFrequency);
        }
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Send frequency on Enter key
        if (keyCode == 257 || keyCode == 335) { // Enter or Numpad Enter
            if (frequencyField.isFocused()) {
                sendFrequencyToServer();
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public void removed() {
        // Send frequency when GUI closes
        sendFrequencyToServer();
        super.removed();
    }
    
    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        this.renderTooltip(poseStack, mouseX, mouseY);
        
        // Render button tooltips
        renderButtonTooltips(poseStack, mouseX, mouseY);

        // If frequency field lost focus since last frame, send update as a fallback
        try {
            if (frequencyField != null) {
                boolean focused = frequencyField.isFocused();
                if (this.prevFreqFocused && !focused) {
                    BonePipe.LOGGER.info("Frequency field lost focus, sending frequency: {}", frequencyField.getValue());
                    sendFrequencyToServer();
                }
                this.prevFreqFocused = focused;
            }
        } catch (Exception e) {
            BonePipe.LOGGER.warn("Error while checking frequency field focus: {}", e.getMessage());
        }
    }
    
    /**
     * Render tooltips for side configuration buttons
     */
    private void renderButtonTooltips(PoseStack poseStack, int mouseX, int mouseY) {
        int guiLeft = (this.width - this.imageWidth) / 2;
        int guiTop = (this.height - this.imageHeight) / 2;
        
        // Tooltip for frequency field
        if (mouseX >= guiLeft + 31 && mouseX < guiLeft + 129 &&
            mouseY >= guiTop + 23 && mouseY < guiTop + 37) {
            renderComponentTooltip(poseStack, java.util.List.of(
                Component.literal("§fWireless Channel"),
                Component.literal(""),
                Component.literal("§7Enter same channel on two"),
                Component.literal("§7adapters to link them."),
                Component.literal(""),
                Component.literal("§7Example: 'base' or '1'"),
                Component.literal(""),
                Component.literal("§8Channel: §e" + 
                    (menu.getBlockEntity().getFrequency().isEmpty() ? "Not set" : menu.getBlockEntity().getFrequency()))
            ), mouseX, mouseY);
            return;
        }
        
        // Tooltips for side buttons
        int startX = guiLeft + SIDE_BUTTONS_START_X;
        int startY = guiTop + SIDE_BUTTONS_START_Y;
        
        Direction[] directions = {
            Direction.UP, Direction.DOWN, Direction.NORTH,
            Direction.SOUTH, Direction.WEST, Direction.EAST
        };
        
        for (int i = 0; i < directions.length; i++) {
            int col = i % 3;
            int row = i / 3;
            int x = startX + col * 20;
            int y = startY + row * 20;
            
            if (mouseX >= x && mouseX < x + 18 && mouseY >= y && mouseY < y + 18) {
                Direction dir = directions[i];
                AdapterBlockEntity be = menu.getBlockEntity();
                AdapterBlockEntity.SideConfig config = be.getSideConfig(dir);
                
                String modeName = config != null ? config.mode.name() : "DISABLED";
                String modeColor = switch(config != null ? config.mode : AdapterBlockEntity.SideConfig.TransferMode.DISABLED) {
                    case OUTPUT -> "§6"; // Orange
                    case INPUT -> "§9"; // Blue
                    case BOTH -> "§e"; // Yellow
                    case DISABLED -> "§7"; // Gray
                };
                
                // Multi-line tooltip
                renderComponentTooltip(poseStack, java.util.List.of(
                    Component.literal("§fSide: §b" + dir.getName().toUpperCase()),
                    Component.literal("§fMode: " + modeColor + modeName),
                    Component.literal(""),
                    Component.literal("§7Left-click: Select"),
                    Component.literal("§7Right-click: Cycle mode"),
                    Component.literal(""),
                    Component.literal("§8OUTPUT: Extract from machine"),
                    Component.literal("§8INPUT: Insert to machine"),
                    Component.literal("§8BOTH: Bidirectional"),
                    Component.literal("§8DISABLED: No transfer")
                ), mouseX, mouseY);
                break;
            }
        }
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
        // Buttons are baked into texture at positions matching our 3x2 grid
        // Just draw selection highlight and mode indicators
        
        int startX = guiLeft + SIDE_BUTTONS_START_X;
        int startY = guiTop + SIDE_BUTTONS_START_Y;
        
        AdapterBlockEntity be = menu.getBlockEntity();
        
        Direction[] directions = {
            Direction.UP, Direction.DOWN, Direction.NORTH,
            Direction.SOUTH, Direction.WEST, Direction.EAST
        };
        
        for (int i = 0; i < directions.length; i++) {
            int col = i % 3;
            int row = i / 3;
            int x = startX + col * 20;
            int y = startY + row * 20;
            
            Direction dir = directions[i];
            
            // Highlight selected button
            if (selectedSide == dir) {
                fill(poseStack, x-1, y-1, x+19, y+19, 0xFFFFFFFF);
            }
            
            // Show mode indicator as colored corner pixel
            AdapterBlockEntity.SideConfig config = be.getSideConfig(dir);
            if (config != null && config.enabled) {
                int modeColor = getModeColor(config.mode);
                fill(poseStack, x+15, y+15, x+18, y+18, modeColor);
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
        
        // Frequency label - moved higher and to the right
        this.font.draw(poseStack, "Channel:", 32, 12, 0x404040);
        
        // Current frequency display - below text field
        String currentFreq = menu.getBlockEntity().getFrequency();
        if (!currentFreq.isEmpty()) {
            this.font.draw(poseStack, "§8Current: §e" + currentFreq, 32, 40, 0x606060);
        } else {
            this.font.draw(poseStack, "§8Not set", 32, 40, 0x606060);
        }
        
        // Status info - top right
        AdapterBlockEntity be = menu.getBlockEntity();
        String status = be.isEnabled() ? "§aActive" : "§cInactive";
        this.font.draw(poseStack, status, 135, 6, 0xFFFFFF);
        
        // Connected machine info - below status
        if (be.isConnected()) {
            String machineName = be.getConnectedMachineName();
            this.font.draw(poseStack, "§a✓ " + machineName, 8, 94, 0x40FF40);
        } else {
            this.font.draw(poseStack, "§c✗ No Machine", 8, 94, 0xFF4040);
        }
        
        // Side configuration label - above buttons
        this.font.draw(poseStack, "§7Configure Sides:", 8, 30, 0x505050);
        
        // Show selected side info - below buttons
        if (selectedSide != null) {
            AdapterBlockEntity.SideConfig config = be.getSideConfig(selectedSide);
            String modeName = config != null ? config.mode.name() : "DISABLED";
            
            // Show selected side and mode below button grid
            this.font.draw(poseStack, "Side: §f" + selectedSide.getName(), 8, 84, 0x303030);
            this.font.draw(poseStack, "Mode: §e" + modeName, 80, 84, 0x303030);
        } else {
            // Show instructions when no side selected
            this.font.draw(poseStack, "§8Click buttons to configure", 8, 84, 0x505050);
        }
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int guiLeft = (this.width - this.imageWidth) / 2;
        int guiTop = (this.height - this.imageHeight) / 2;
        
        // Check side button clicks - 3x2 grid, 20px spacing
        int startX = guiLeft + SIDE_BUTTONS_START_X;
        int startY = guiTop + SIDE_BUTTONS_START_Y;
        
        Direction[] directions = {
            Direction.UP, Direction.DOWN, Direction.NORTH,
            Direction.SOUTH, Direction.WEST, Direction.EAST
        };
        
        for (int i = 0; i < directions.length; i++) {
            int col = i % 3;
            int row = i / 3;
            int x = startX + col * 20;
            int y = startY + row * 20;
            
            if (mouseX >= x && mouseX < x + 18 &&
                mouseY >= y && mouseY < y + 18) {
                
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
