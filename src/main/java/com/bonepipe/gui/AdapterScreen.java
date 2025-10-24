package com.bonepipe.gui;

import com.bonepipe.BonePipe;
import com.bonepipe.blocks.AdapterBlockEntity;
import com.bonepipe.network.packets.NetworkHandler;
import com.bonepipe.network.packets.UpdateFrequencyPacket;
import com.bonepipe.network.packets.UpdateChannelConfigPacket;
import com.bonepipe.transfer.TransferChannel;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.client.gui.components.Button;

/**
 * Channel-based GUI for Wireless Adapter
 * Configure transfer modes per channel (ITEMS, FLUIDS, ENERGY, MEK_GAS)
 * 
 * v3.0.11 - Rewritten to use channel configuration instead of sides
 */
public class AdapterScreen extends AbstractContainerScreen<AdapterMenu> {
    
    private static final ResourceLocation TEXTURE = 
        new ResourceLocation(BonePipe.MODID, "textures/gui/adapter_channels.png");
    
    // GUI dimensions - compact without player inventory
    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 120; // Smaller without inventory
    
    // Channel configuration area
    private static final int CHANNELS_START_X = 8;
    private static final int CHANNELS_START_Y = 50;
    private static final int CHANNEL_ROW_HEIGHT = 16;
    
    // Main channels to display
    private static final TransferChannel[] MAIN_CHANNELS = {
        TransferChannel.ITEMS,
        TransferChannel.FLUIDS,
        TransferChannel.ENERGY,
        TransferChannel.MEK_GAS
    };
    
    // Widgets
    private EditBox frequencyField;
    private boolean prevFreqFocused = false;
    
    public AdapterScreen(AdapterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
        this.titleLabelY = 6;
        // No inventory labels
        this.inventoryLabelY = 10000; // Hide it off-screen
    }
    
    @Override
    protected void init() {
        super.init();
        
        int guiLeft = (this.width - this.imageWidth) / 2;
        int guiTop = (this.height - this.imageHeight) / 2;
        
        // Frequency input field
        frequencyField = new EditBox(
            this.font,
            guiLeft + 60, guiTop + 22,
            80, 14,
            Component.literal("Frequency")
        );
        frequencyField.setMaxLength(32);
        frequencyField.setValue(menu.getBlockEntity().getFrequency());
        frequencyField.setResponder(this::onFrequencyChanged);
        frequencyField.setBordered(false);
        frequencyField.setTextColor(0xFFFFFF);
        this.addRenderableWidget(frequencyField);
        
        this.setInitialFocus(frequencyField);
        this.prevFreqFocused = frequencyField.isFocused();
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
     * Render tooltips for channel rows
     */
    private void renderButtonTooltips(PoseStack poseStack, int mouseX, int mouseY) {
        int guiLeft = (this.width - this.imageWidth) / 2;
        int guiTop = (this.height - this.imageHeight) / 2;
        
        // Tooltip for frequency field
        if (mouseX >= guiLeft + 60 && mouseX < guiLeft + 140 &&
            mouseY >= guiTop + 22 && mouseY < guiTop + 36) {
            renderComponentTooltip(poseStack, java.util.List.of(
                Component.literal("§fWireless Channel"),
                Component.literal(""),
                Component.literal("§7Enter same channel on two"),
                Component.literal("§7adapters to link them."),
                Component.literal(""),
                Component.literal("§7Example: 'base' or '1'")
            ), mouseX, mouseY);
            return;
        }
        
        // Tooltips for channel rows
        int startX = guiLeft + CHANNELS_START_X;
        int startY = guiTop + CHANNELS_START_Y;
        
        for (int i = 0; i < MAIN_CHANNELS.length; i++) {
            int y = startY + i * CHANNEL_ROW_HEIGHT;
            
            if (mouseX >= startX && mouseX < startX + 160 &&
                mouseY >= y && mouseY < y + 14) {
                
                TransferChannel channel = MAIN_CHANNELS[i];
                AdapterBlockEntity be = menu.getBlockEntity();
                AdapterBlockEntity.ChannelConfig config = be.getChannelConfig(channel);
                
                String modeName = config != null ? config.mode.name() : "DISABLED";
                String modeColor = switch(config != null ? config.mode : AdapterBlockEntity.ChannelConfig.TransferMode.DISABLED) {
                    case OUTPUT -> "§6"; // Orange
                    case INPUT -> "§9"; // Blue
                    case BOTH -> "§e"; // Yellow
                    case DISABLED -> "§7"; // Gray
                };
                
                // Multi-line tooltip
                renderComponentTooltip(poseStack, java.util.List.of(
                    Component.literal("§f" + channel.getDisplayName()),
                    Component.literal("§fMode: " + modeColor + modeName),
                    Component.literal(""),
                    Component.literal("§7Click to cycle mode"),
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
        
        // Main background
        this.blit(poseStack, guiLeft, guiTop, 0, 0, this.imageWidth, this.imageHeight);
        
        // Draw channel configuration buttons
        renderChannelButtons(poseStack, guiLeft, guiTop);
    }
    
    /**
     * Render channel configuration buttons
     */
    private void renderChannelButtons(PoseStack poseStack, int guiLeft, int guiTop) {
        AdapterBlockEntity be = menu.getBlockEntity();
        
        int x = guiLeft + CHANNELS_START_X;
        int y = guiTop + CHANNELS_START_Y;
        
        for (TransferChannel channel : MAIN_CHANNELS) {
            AdapterBlockEntity.ChannelConfig config = be.getChannelConfig(channel);
            if (config != null && config.mode != AdapterBlockEntity.ChannelConfig.TransferMode.DISABLED) {
                // Draw colored indicator bar based on mode
                int color = getModeColor(config.mode);
                fill(poseStack, x, y, x + 160, y + 14, color | 0x40000000); // Semi-transparent
                fill(poseStack, x, y, x + 2, y + 14, color); // Solid left edge
            }
            y += CHANNEL_ROW_HEIGHT;
        }
    }
    
    /**
     * Get color for transfer mode
     */
    private int getModeColor(AdapterBlockEntity.ChannelConfig.TransferMode mode) {
        return switch (mode) {
            case OUTPUT -> 0xFFFF5500; // Orange - extracting
            case INPUT -> 0xFF0055FF;  // Blue - inserting
            case BOTH -> 0xFFFFFF00;   // Yellow - bidirectional
            case DISABLED -> 0xFF404040; // Dark gray
        };
    }
    
    /**
     * Get text color for mode display
     */
    private int getModeTextColor(AdapterBlockEntity.ChannelConfig.TransferMode mode) {
        return switch (mode) {
            case OUTPUT -> 0xFF5500; // Orange
            case INPUT -> 0x0055FF;  // Blue
            case BOTH -> 0xFFFF00;   // Yellow
            case DISABLED -> 0x808080; // Gray
        };
    }
    
    /**
     * Get text representation of mode
     */
    private String getModeText(AdapterBlockEntity.ChannelConfig.TransferMode mode) {
        return switch (mode) {
            case OUTPUT -> "OUTPUT";
            case INPUT -> "INPUT";
            case BOTH -> "BOTH";
            case DISABLED -> "DISABLED";
        };
    }
    
    /**
     * Get icon for channel (simple ASCII)
     */
    private String getChannelIcon(TransferChannel channel) {
        return switch (channel) {
            case ITEMS -> "[I]";
            case FLUIDS -> "[F]";
            case ENERGY -> "[E]";
            case MEK_GAS -> "[G]";
            default -> "[?]";
        };
    }
    
    /**
     * Get short name for channel
     */
    private String getShortChannelName(TransferChannel channel) {
        return switch (channel) {
            case ITEMS -> "Items";
            case FLUIDS -> "Fluids";
            case ENERGY -> "Energy";
            case MEK_GAS -> "Gas";
            default -> channel.getDisplayName();
        };
    }
    
    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        // Title - centered in title bar (y:4-17)
        this.font.draw(poseStack, this.title, 8, 7, 0x404040);
        
        // ID label - in label panel (y:18-35)
        this.font.draw(poseStack, "ID:", 10, 23, 0xFFFFFF);
        
        // Connected machine - in info panel (y:36-48)
        AdapterBlockEntity be = menu.getBlockEntity();
        String machineName = be.getConnectedMachineName();
        if (machineName != null && !machineName.equals("None")) {
            // Truncate long names
            if (machineName.length() > 20) {
                machineName = machineName.substring(0, 17) + "...";
            }
            this.font.draw(poseStack, "§a" + machineName, 10, 39, 0x40FF40);
        } else {
            this.font.draw(poseStack, "§7No Machine", 10, 39, 0x808080);
        }
        
        // Channel labels - rows start at y:50, height 16, so text at y+4 for centering
        int y = CHANNELS_START_Y + 4;
        for (TransferChannel channel : MAIN_CHANNELS) {
            AdapterBlockEntity.ChannelConfig config = be.getChannelConfig(channel);
            
            // Channel icon in icon box - shifted right by 2px
            String icon = getChannelIcon(channel);
            this.font.draw(poseStack, icon, CHANNELS_START_X + 6, y, 0xFFFFFF);
            
            // Channel name after icon box - shifted right by 2px
            String channelName = getShortChannelName(channel);
            this.font.draw(poseStack, channelName, CHANNELS_START_X + 23, y, 0x404040);
            
            // Mode in right box (x:WIDTH-74 to WIDTH-11)
            String modeText = getModeText(config != null ? config.mode : AdapterBlockEntity.ChannelConfig.TransferMode.DISABLED);
            int modeColor = getModeTextColor(config != null ? config.mode : AdapterBlockEntity.ChannelConfig.TransferMode.DISABLED);
            this.font.draw(poseStack, modeText, GUI_WIDTH - 70, y, modeColor);
            
            y += CHANNEL_ROW_HEIGHT;
        }
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int guiLeft = (this.width - this.imageWidth) / 2;
        int guiTop = (this.height - this.imageHeight) / 2;
        
        // Check channel row clicks
        int startX = guiLeft + CHANNELS_START_X;
        int startY = guiTop + CHANNELS_START_Y;
        
        for (int i = 0; i < MAIN_CHANNELS.length; i++) {
            int y = startY + i * CHANNEL_ROW_HEIGHT;
            
            if (mouseX >= startX && mouseX < startX + 160 &&
                mouseY >= y && mouseY < y + 14) {
                
                TransferChannel clickedChannel = MAIN_CHANNELS[i];
                
                if (button == 0 || button == 1) { // Left or right click - cycle mode
                    cycleChannelMode(clickedChannel);
                    return true;
                }
            }
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    /**
     * Cycle through transfer modes for a channel
     */
    private void cycleChannelMode(TransferChannel channel) {
        AdapterBlockEntity be = menu.getBlockEntity();
        AdapterBlockEntity.ChannelConfig config = be.getChannelConfig(channel);
        
        if (config == null) {
            config = new AdapterBlockEntity.ChannelConfig();
        }
        
        // Cycle: DISABLED -> OUTPUT -> INPUT -> BOTH -> DISABLED
        config.mode = switch (config.mode) {
            case DISABLED -> AdapterBlockEntity.ChannelConfig.TransferMode.OUTPUT;
            case OUTPUT -> AdapterBlockEntity.ChannelConfig.TransferMode.INPUT;
            case INPUT -> AdapterBlockEntity.ChannelConfig.TransferMode.BOTH;
            case BOTH -> AdapterBlockEntity.ChannelConfig.TransferMode.DISABLED;
        };
        
        // Send to server
        NetworkHandler.CHANNEL.sendToServer(
            new UpdateChannelConfigPacket(be.getBlockPos(), channel, config.mode)
        );
    }
    
    @Override
    public void resize(net.minecraft.client.Minecraft minecraft, int width, int height) {
        String freq = frequencyField.getValue();
        super.resize(minecraft, width, height);
        frequencyField.setValue(freq);
    }
}
